/* CompliantNode refers to a node that follows the rules (not malicious)*/
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CompliantNode implements Node {

    // Provided parameters from Simulation
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;

    // This node’s knowledge of which nodes it follows
    private boolean[] followees;

    // Stores all transactions that this node believes are valid so far
    private Set<Transaction> myTransactions;

    // Track which round we are on
    private int currentRound;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;

        // Initialize our set of transactions and the round counter
        this.myTransactions = new HashSet<>();
        this.currentRound = 0;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        // Add any initially provided transactions to our set
        this.myTransactions.addAll(pendingTransactions);
    }

    public Set<Transaction> getProposals() {
        // IMPLEMENT THIS
        // Called each round when this node is asked to broadcast its current proposals
        currentRound++;

        // If it’s not the final round, or even if it is, we return the set of all
        // transactions we consider valid so far. After the final round, these
        // same transactions represent the consensus we arrived at.
        return new HashSet<>(myTransactions);
    }

    public void receiveCandidates(ArrayList<Integer[]> candidates) {
        // IMPLEMENT THIS
        // Each Integer[] has [0] = tx id, [1] = proposer node index
        // For a compliant node, we will simply add all proposed transactions
        // to our known set. We are not filtering here, assuming transactions are valid.
        for (Integer[] pair : candidates) {
            int txID = pair[0];
            myTransactions.add(new Transaction(txID));
        }
    }
}