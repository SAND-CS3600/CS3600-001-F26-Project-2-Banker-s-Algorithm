public class BankImpl implements BankInterface {

    private int numberOfCustomers;
    private int numberOfResources;

    // Banker's Algorithm data structures
    private int[] available;
    private int[][] maximum;
    private int[][] allocation;
    private int[][] need;

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
     */
    private boolean isSafeState(int customerNum, int[] request) {

        // TODO:
        // Implement the Banker's Safety Algorithm.

        return false;
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
}
