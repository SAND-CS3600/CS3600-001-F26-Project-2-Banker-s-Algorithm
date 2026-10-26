import java.util.ArrayList;
import java.util.List;

public class BankImpl implements BankInterface {

    private int numberOfCustomers;
    private int numberOfResources;

    // Banker's Algorithm data structures
    private int[] available;
    private int[][] maximum;
    private int[][] allocation;
    private int[][] need;

    /*
     * Order in which customers finished during the most recent run of
     * isSafeState(). If the state was safe, it holds every customer
     * (the safe sequence). If unsafe, it holds only the customers that
     * could finish, so the missing ones are the customers that are stuck.
     *
     * Diagnostic only: not part of the bank's state, and never used to
     * make a decision. It is rewritten by every safety check, so it must
     * only be read right after isSafeState() within the same
     * synchronized call.
     */
    private List<Integer> lastSafeSequence = new ArrayList<>();

    /**
     * Creates a new bank with the specified resources.
     */
    public BankImpl(int numberOfCustomers, int[] resources) {

        this.numberOfCustomers = numberOfCustomers;

        // The number of resource types comes from the command line,
        // so it is never hard-coded
        this.numberOfResources = resources.length;

        // Copy so the bank owns its array; later changes to the
        // caller's array cannot affect the bank
        available = resources.clone();

        // Java fills new int arrays with 0, so every customer starts
        // with an empty allocation. Maximum and Need are filled in
        // by addCustomer().
        maximum = new int[numberOfCustomers][numberOfResources];
        allocation = new int[numberOfCustomers][numberOfResources];
        need = new int[numberOfCustomers][numberOfResources];
    }

    /**
     * Adds a customer and records the customer's
     * maximum resource demand.
     *
     * Meant to be called once per customer at startup, before any
     * requests. Calling it again for a customer that holds resources
     * would discard that allocation without returning it to Available.
     */
    @Override
    public void addCustomer(int customerNum, int[] maxDemand) {

        // Reject invalid input without changing the bank
        if (customerNum < 0 || customerNum >= numberOfCustomers) {
            System.out.println("Error: invalid customer number " + customerNum
                    + " (must be 0-" + (numberOfCustomers - 1) + ")");
            return;
        }
        if (maxDemand == null || maxDemand.length != numberOfResources) {
            System.out.println("Error: customer " + customerNum
                    + " must have exactly " + numberOfResources + " maximum values");
            return;
        }
        for (int j = 0; j < numberOfResources; j++) {
            if (maxDemand[j] < 0) {
                System.out.println("Error: customer " + customerNum
                        + " has a negative maximum for R" + j);
                return;
            }
        }

        // Separate copies, so changing Need later never changes Maximum
        maximum[customerNum] = maxDemand.clone();
        need[customerNum] = maxDemand.clone();

        // A new customer holds nothing, so Need = Maximum - 0 = Maximum
        allocation[customerNum] = new int[numberOfResources];

        // Warn if this customer could need more of a resource than the
        // whole system has, since it could then never finish.
        // At startup, Available still equals the system total.
        for (int j = 0; j < numberOfResources; j++) {
            if (maxDemand[j] > available[j]) {
                System.out.println("Warning: customer " + customerNum
                        + " maximum for R" + j + " (" + maxDemand[j]
                        + ") exceeds total R" + j + " in the system ("
                        + available[j] + "); this customer can never finish");
            }
        }
    }

    /**
     * Displays the current state of the bank:
     *
     * Available
     * Maximum
     * Allocation
     * Need
     */
    @Override
    public void getState() {

        // TODO
    }

    /**
     * Determines whether the system would remain
     * in a safe state if the request were granted.
     *
     * Works only on temporary copies, so the bank's real state is never
     * changed here. Records the order customers finished in
     * lastSafeSequence.
     *
     * Precondition: the request has already been validated by
     * requestResources() (no negative values, request <= Need,
     * request <= Available). Must only be called from a synchronized
     * method, since it rewrites lastSafeSequence.
     */
    private boolean isSafeState(int customerNum, int[] request) {

        // Step 1 - Create a temporary state (deep copies of the real state)
        int[] work = available.clone();
        int[][] tempAllocation = copyMatrix(allocation);
        int[][] tempNeed = copyMatrix(need);

        // Step 2 - Simulate granting the request in the temporary state
        for (int j = 0; j < numberOfResources; j++) {
            work[j] -= request[j];
            tempAllocation[customerNum][j] += request[j];
            tempNeed[customerNum][j] -= request[j];
        }

        // Step 3 - Track customer completion: no customer has finished yet
        boolean[] finish = new boolean[numberOfCustomers];
        lastSafeSequence.clear();

        // Steps 4-6 - Search for a customer whose remaining Need fits in
        // Work, let it finish and return its resources, and keep
        // searching until a full pass finishes nobody
        boolean progressMade = true;
        while (progressMade) {
            progressMade = false;

            for (int i = 0; i < numberOfCustomers; i++) {
                if (!finish[i] && lessOrEqual(tempNeed[i], work)) {

                    // Step 5 - Customer i completes and returns everything it holds
                    for (int j = 0; j < numberOfResources; j++) {
                        work[j] += tempAllocation[i][j];
                    }
                    finish[i] = true;
                    lastSafeSequence.add(i);
                    progressMade = true;
                }
            }
        }

        // Step 7 - Safe only if every customer was able to finish
        // (each customer is added to the sequence exactly once, when it finishes)
        return lastSafeSequence.size() == numberOfCustomers;
    }

    /**
     * Attempts to allocate resources to a customer.
     *
     * @return true if approved
     *         false if denied
     */
    @Override
    public synchronized boolean requestResources(
            int customerNum, int[] request) {

        // TODO:
        // 1. Validate the request.
        // 2. Check whether the request exceeds Need.
        // 3. Check whether the resources are available.
        // 4. Check whether the resulting state is safe.
        // 5. Update the bank if the request is approved.

        return false;
    }

    /**
     * Releases resources held by a customer.
     */
    @Override
    public synchronized void releaseResources(
            int customerNum, int[] release) {

        // TODO:
        // Validate and release the resources.
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    /**
     * Returns the first index j where a[j] > b[j], or -1 if
     * a[j] <= b[j] for every j. Both arrays must be the same length.
     */
    private static int firstExceedingIndex(int[] a, int[] b) {
        for (int j = 0; j < a.length; j++) {
            if (a[j] > b[j]) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Returns true if a[j] <= b[j] for every j (a <= b as vectors).
     * Both arrays must be the same length.
     */
    private static boolean lessOrEqual(int[] a, int[] b) {
        return firstExceedingIndex(a, b) == -1;
    }

    /**
     * Returns a deep copy of a 2D array. clone() on a 2D array copies
     * only the outer array and shares the rows, so each row is cloned.
     */
    private static int[][] copyMatrix(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }
}
