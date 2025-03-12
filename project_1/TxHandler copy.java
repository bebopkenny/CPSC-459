import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {

    // The current UTXO pool that this TxHandler works with
    private UTXOPool utxoPool;

    /* Creates a public ledger whose current UTXOPool (collection of unspent 
     * transaction outputs) is utxoPool. This makes a defensive copy using the 
     * UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // Defensive copy of the given UTXOPool
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /* Returns true if:
     * (1) all outputs claimed by tx are in the current UTXO pool, 
     * (2) the signatures on each input of tx are valid, 
     * (3) no UTXO is claimed multiple times by tx, 
     * (4) all of tx’s output values are non-negative, and
     * (5) the sum of tx’s input values is greater than or equal to the sum of its output values;
     * and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        HashSet<UTXO> claimedUTXOs = new HashSet<>();
        double inputSum = 0;
        double outputSum = 0;

        // Check each input of the transaction
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // (1) The UTXO claimed by the input must be in the current pool
            if (!utxoPool.contains(utxo)) {
                return false;
            }

            // (3) No UTXO should be claimed multiple times
            if (claimedUTXOs.contains(utxo)) {
                return false;
            }
            claimedUTXOs.add(utxo);

            // (2) The signature on each input must be valid
            Transaction.Output prevTxOutput = utxoPool.getTxOutput(utxo);
            byte[] rawData = tx.getRawDataToSign(i);
            // Assuming Crypto.verifySignature exists and works as expected:
            if (!Crypto.verifySignature(prevTxOutput.address, rawData, input.signature)) {
                return false;
            }

            // Accumulate the input values
            inputSum += prevTxOutput.value;
        }

        // Check each output of the transaction
        for (int j = 0; j < tx.numOutputs(); j++) {
            Transaction.Output output = tx.getOutput(j);
            // (4) All output values must be non-negative
            if (output.value < 0) {
                return false;
            }
            outputSum += output.value;
        }

        // (5) The sum of input values must be greater than or equal to the sum of output values
        if (inputSum < outputSum) {
            return false;
        }

        // All conditions are satisfied
        return true;
    }

    /* Handles each epoch by receiving an unordered array of proposed 
     * transactions, checking each transaction for correctness, returning 
     * a mutually valid array of accepted transactions, and updating the 
     * current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();
        boolean progress = true;

        // Continue processing until no new valid transactions are found
        while (progress) {
            progress = false;
            for (Transaction tx : possibleTxs) {
                // Skip if this transaction has already been accepted
                if (acceptedTxs.contains(tx)) {
                    continue;
                }
                if (isValidTx(tx)) {
                    // Accept the transaction
                    acceptedTxs.add(tx);
                    
                    // Remove the UTXOs that are consumed by this transaction's inputs
                    for (int i = 0; i < tx.numInputs(); i++) {
                        Transaction.Input input = tx.getInput(i);
                        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                        utxoPool.removeUTXO(utxo);
                    }
                    
                    // Finalize the transaction (compute its hash) if not already done
                    tx.finalize();
                    byte[] txHash = tx.getHash();
                    
                    // Add the new UTXOs corresponding to the transaction's outputs
                    for (int j = 0; j < tx.numOutputs(); j++) {
                        UTXO utxo = new UTXO(txHash, j);
                        utxoPool.addUTXO(utxo, tx.getOutput(j));
                    }
                    progress = true;
                }
            }
        }
        
        Transaction[] validTxArray = new Transaction[acceptedTxs.size()];
        return acceptedTxs.toArray(validTxArray);
    }
}


// public class TxHandler {

// 	/* Creates a public ledger whose current UTXOPool (collection of unspent 
// 	 * transaction outputs) is utxoPool. This should make a defensive copy of 
// 	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
// 	 */
// 	public TxHandler(UTXOPool utxoPool) {
// 		// IMPLEMENT THIS
// 	}

// 	/* Returns true if 
// 	 * (1) all outputs claimed by tx are in the current UTXO pool, 
// 	 * (2) the signatures on each input of tx are valid, 
// 	 * (3) no UTXO is claimed multiple times by tx, 
// 	 * (4) all of tx’s output values are non-negative, and
// 	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
// 	        its output values;
// 	   and false otherwise.
// 	 */

// 	public boolean isValidTx(Transaction tx) {
// 		// IMPLEMENT THIS
// 		return false;
// 	}

// 	/* Handles each epoch by receiving an unordered array of proposed 
// 	 * transactions, checking each transaction for correctness, 
// 	 * returning a mutually valid array of accepted transactions, 
// 	 * and updating the current UTXO pool as appropriate.
// 	 */
// 	public Transaction[] handleTxs(Transaction[] possibleTxs) {
// 		// IMPLEMENT THIS
// 		return null;
// 	}

// } 
