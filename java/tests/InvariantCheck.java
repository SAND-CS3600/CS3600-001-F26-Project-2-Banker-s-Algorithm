import java.lang.reflect.Field;
import java.util.Arrays;

public class InvariantCheck {

    private static final int NUMBER_OF_CUSTOMERS = 5;

    // Not the README's 10 5 7 8, since that makes every request unsafe
    private static final int[] RESOURCES = {10, 6, 7, 8};

    // Same rows as inPutFile.txt
    private static final int[][] MAX_DEMAND = {
        {6, 4, 7, 3},
        {4, 2, 3, 2},
        {2, 5, 3, 3},
        {6, 3, 3, 2},
        {5, 6, 7, 5}
    };

    // Total of each resource in the bank under test
    private static int[] systemTotal;

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {

        test1InitialState();
        test2ValidSafeRequest();
        test3RequestExceedsNeed();
        test4InsufficientAvailable();
        test5UnsafeRequest();
        test6ReleaseResources();
        test7InvalidRelease();
        test8MultipleTransactions();
        testInvalidRequestInput();
        testInvalidReleaseInput();
        testReadmeExampleIsUnsafe();

        System.out.println();
        System.out.println("Passed: " + passed + "  Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------
    // Section 20 tests
    // ---------------------------------------------------------------

    /**
     * Test 1 - Initial State
     */
    private static void test1InitialState() throws Exception {
        heading("Test 1 - Initial State");
        BankImpl bank = newBank(RESOURCES);

        Snapshot state = Snapshot.of(bank);
        check("Available = initial resources",
                Arrays.equals(state.available, RESOURCES));
        check("Maximum = input file", Arrays.deepEquals(state.maximum, MAX_DEMAND));
        check("Allocation = 0",
                Arrays.deepEquals(state.allocation, new int[NUMBER_OF_CUSTOMERS][RESOURCES.length]));
        check("Need = Maximum", Arrays.deepEquals(state.need, MAX_DEMAND));
        checkInvariants(bank, "initial state");
    }

    /**
     * Test 2 - Valid Safe Request
     */
    private static void test2ValidSafeRequest() throws Exception {
        heading("Test 2 - Valid Safe Request");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        expectCustomer(bank, 0, new int[] {7, 5, 5, 7},
                new int[] {3, 1, 2, 1}, new int[] {3, 3, 5, 2});
    }

    /**
     * Test 3 - Request Exceeds Need
     */
    private static void test3RequestExceedsNeed() throws Exception {
        heading("Test 3 - Request Exceeds Need");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 1, new int[] {5, 0, 0, 0}, false);
    }

    /**
     * Test 4 - Insufficient Available Resources
     */
    private static void test4InsufficientAvailable() throws Exception {
        heading("Test 4 - Insufficient Available Resources");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        request(bank, 4, new int[] {5, 6, 7, 5}, false);
    }

    /**
     * Test 5 - Unsafe Request
     *
     * C0 and C4 can each need all 7 of R2, so once both hold some R2
     * neither can finish.
     */
    private static void test5UnsafeRequest() throws Exception {
        heading("Test 5 - Unsafe Request");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        request(bank, 4, new int[] {0, 0, 1, 0}, false);
    }

    /**
     * Test 6 - Release Resources
     */
    private static void test6ReleaseResources() throws Exception {
        heading("Test 6 - Release Resources");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        release(bank, 0, new int[] {1, 0, 1, 0}, true);
        expectCustomer(bank, 0, new int[] {8, 5, 6, 7},
                new int[] {2, 1, 1, 1}, new int[] {4, 3, 6, 2});
    }

    /**
     * Test 7 - Invalid Release
     */
    private static void test7InvalidRelease() throws Exception {
        heading("Test 7 - Invalid Release");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        release(bank, 0, new int[] {4, 0, 0, 0}, false);
        release(bank, 1, new int[] {1, 0, 0, 0}, false);
    }

    /**
     * Test 8 - Multiple Transactions
     */
    private static void test8MultipleTransactions() throws Exception {
        heading("Test 8 - Multiple Transactions");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        request(bank, 1, new int[] {2, 1, 1, 1}, true);
        release(bank, 0, new int[] {1, 0, 1, 0}, true);
        request(bank, 2, new int[] {1, 2, 1, 1}, true);

        expectCustomer(bank, 0, new int[] {5, 2, 4, 5},
                new int[] {2, 1, 1, 1}, new int[] {4, 3, 6, 2});
        expectCustomer(bank, 1, new int[] {5, 2, 4, 5},
                new int[] {2, 1, 1, 1}, new int[] {2, 1, 2, 1});
        expectCustomer(bank, 2, new int[] {5, 2, 4, 5},
                new int[] {1, 2, 1, 1}, new int[] {1, 3, 2, 2});
    }

    // ---------------------------------------------------------------
    // Section 19 edge cases
    // ---------------------------------------------------------------

    /**
     * Bad requests are denied without changing the bank.
     */
    private static void testInvalidRequestInput() throws Exception {
        heading("Edge cases - Invalid Requests");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 5, new int[] {1, 0, 0, 0}, false);
        request(bank, -1, new int[] {1, 0, 0, 0}, false);
        request(bank, 0, new int[] {1, 0, 0}, false);
        request(bank, 0, new int[] {1, 0, 0, 0, 0}, false);
        request(bank, 0, new int[] {-1, 0, 0, 0}, false);
        request(bank, 0, null, false);
    }

    /**
     * Bad releases are rejected without changing the bank.
     */
    private static void testInvalidReleaseInput() throws Exception {
        heading("Edge cases - Invalid Releases");
        BankImpl bank = newBank(RESOURCES);

        request(bank, 0, new int[] {3, 1, 2, 1}, true);
        release(bank, 5, new int[] {1, 0, 0, 0}, false);
        release(bank, -1, new int[] {1, 0, 0, 0}, false);
        release(bank, 0, new int[] {1, 0, 0}, false);
        release(bank, 0, new int[] {1, 0, 0, 0, 0}, false);
        release(bank, 0, new int[] {-1, 0, 0, 0}, false);
        release(bank, 0, null, false);
    }

    /**
     * With 10 5 7 8, C4 can never finish, so every request is denied.
     */
    private static void testReadmeExampleIsUnsafe() throws Exception {
        heading("Gotcha - README example resources 10 5 7 8");
        BankImpl bank = newBank(new int[] {10, 5, 7, 8});

        request(bank, 0, new int[] {3, 1, 2, 1}, false);
    }

    // ---------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------

    private static BankImpl newBank(int[] resources) {
        systemTotal = resources.clone();
        BankImpl bank = new BankImpl(NUMBER_OF_CUSTOMERS, resources);
        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            bank.addCustomer(i, MAX_DEMAND[i]);
        }
        return bank;
    }

    /**
     * Sends a request and checks that a denied one changed nothing.
     */
    private static void request(BankImpl bank, int customerNum, int[] request,
            boolean expectApproved) throws Exception {

        String label = "RQ " + customerNum + " " + format(request);
        Snapshot before = Snapshot.of(bank);

        boolean approved = bank.requestResources(customerNum, request);

        check(label + (expectApproved ? " is approved" : " is denied"),
                approved == expectApproved);
        if (!approved) {
            check(label + " leaves the bank unchanged", before.equals(Snapshot.of(bank)));
        }
        checkInvariants(bank, "after " + label);
    }

    /**
     * Sends a release. releaseResources() returns nothing, so whether it
     * was accepted is worked out from what changed.
     */
    private static void release(BankImpl bank, int customerNum, int[] release,
            boolean expectAccepted) throws Exception {

        String label = "RL " + customerNum + " " + format(release);
        Snapshot before = Snapshot.of(bank);

        bank.releaseResources(customerNum, release);
        Snapshot after = Snapshot.of(bank);

        if (expectAccepted) {
            boolean moved = true;
            for (int j = 0; j < release.length; j++) {
                moved &= after.available[j] == before.available[j] + release[j];
                moved &= after.allocation[customerNum][j]
                        == before.allocation[customerNum][j] - release[j];
                moved &= after.need[customerNum][j] == before.need[customerNum][j] + release[j];
            }
            check(label + " is applied", moved);
        } else {
            check(label + " is rejected and leaves the bank unchanged", before.equals(after));
        }
        checkInvariants(bank, "after " + label);
    }

    // ---------------------------------------------------------------
    // Checks
    // ---------------------------------------------------------------

    /**
     * Checks the Section 21 relationships that must hold after
     * every operation.
     */
    private static void checkInvariants(BankImpl bank, String when) throws Exception {
        Snapshot state = Snapshot.of(bank);

        boolean needMatches = true;
        boolean nothingNegative = true;
        boolean totalsConserved = true;

        for (int j = 0; j < RESOURCES.length; j++) {
            int total = state.available[j];
            nothingNegative &= state.available[j] >= 0;

            for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
                needMatches &= state.need[i][j] == state.maximum[i][j] - state.allocation[i][j];
                nothingNegative &= state.allocation[i][j] >= 0;
                total += state.allocation[i][j];
            }
            totalsConserved &= total == systemTotal[j];
        }

        check("Need = Maximum - Allocation " + when, needMatches);
        check("No negative Available or Allocation " + when, nothingNegative);
        check("Available + Allocation = system total " + when, totalsConserved);
    }

    /**
     * Checks Available and one customer's Allocation and Need.
     */
    private static void expectCustomer(BankImpl bank, int customerNum, int[] available,
            int[] allocation, int[] need) throws Exception {

        Snapshot state = Snapshot.of(bank);
        check("Available = [" + format(available) + "]",
                Arrays.equals(state.available, available));
        check("C" + customerNum + " Allocation = [" + format(allocation) + "]",
                Arrays.equals(state.allocation[customerNum], allocation));
        check("C" + customerNum + " Need = [" + format(need) + "]",
                Arrays.equals(state.need[customerNum], need));
    }

    private static void check(String description, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("  PASS  " + description);
        } else {
            failed++;
            System.out.println("  FAIL  " + description);
        }
    }

    private static void heading(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static String format(int[] values) {
        if (values == null) {
            return "null";
        }
        StringBuilder text = new StringBuilder();
        for (int j = 0; j < values.length; j++) {
            if (j > 0) {
                text.append(" ");
            }
            text.append(values[j]);
        }
        return text.toString();
    }

    // ---------------------------------------------------------------
    // Reading the bank's state
    // ---------------------------------------------------------------

    /**
     * Deep copy of the bank's four data structures. They are private,
     * so they are read with reflection.
     */
    private static final class Snapshot {
        int[] available;
        int[][] maximum;
        int[][] allocation;
        int[][] need;

        static Snapshot of(BankImpl bank) throws Exception {
            Snapshot s = new Snapshot();
            s.available = ((int[]) read(bank, "available")).clone();
            s.maximum = copyMatrix((int[][]) read(bank, "maximum"));
            s.allocation = copyMatrix((int[][]) read(bank, "allocation"));
            s.need = copyMatrix((int[][]) read(bank, "need"));
            return s;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Snapshot)) {
                return false;
            }
            Snapshot s = (Snapshot) other;
            return Arrays.equals(available, s.available)
                    && Arrays.deepEquals(maximum, s.maximum)
                    && Arrays.deepEquals(allocation, s.allocation)
                    && Arrays.deepEquals(need, s.need);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(available);
        }

        private static Object read(BankImpl bank, String fieldName) throws Exception {
            Field field = BankImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(bank);
        }

        private static int[][] copyMatrix(int[][] source) {
            int[][] copy = new int[source.length][];
            for (int i = 0; i < source.length; i++) {
                copy[i] = source[i].clone();
            }
            return copy;
        }
    }
}
