import java.io.*;
import java.util.*;

public class TestBankers {

    public static final int NUMBER_OF_CUSTOMERS = 5;

    public static void main(String[] args) throws IOException {

        /*
         * Usage:
         *
         * java TestBankers <input-file> <R0> <R1> ... <Rm-1>
         *
         * Example:
         *
         * java TestBankers inPutFile.txt 10 5 7 8
         */

        if (args.length < 2) {
            System.err.println(
                "Usage: java TestBankers "
                + "<input-file> <R0> <R1> ... <Rm-1>"
            );
            System.exit(1);
        }

        /*
         * TODO:
         *
         * 1. Read the input filename.
         *
         * 2. Determine the number of resource types.
         *
         * 3. Read the initial resources from
         *    the command-line arguments.
         *
         * 4. Create the BankImpl object.
         *
         * 5. Open the input file.
         *
         * 6. Read the maximum demand for each customer.
         *
         * 7. Call addCustomer() for each customer.
         */
        
        // 1. input filename
        String fileName = args[0];

        // 2. the number of recourse types comes from the argument count
        int numberOfResources = args.length - 1;

        // 3. ininial resources from the command line
        int[] recourses = new int[numberOfResources];
        for (int i = 0; i < numberOfResources; i++) {
            Integer value = parseNonNegative(args[i + 1]);
            if (value == null) {
                System.err.println("Error: recurse value '" + args[i + 1]
                    + "' is not a non-negative integer.");
                System.exit(1);
            }
            recourses[i] = value;
        }

        // 4. create the bank
        BankImpl bank = new BankImpl(NUMBER_OF_CUSTOMERS, recourses);

        // 5-7. Read the input file and add each customer 
        int[][] maxDemands = readInputFile(fileName, numberOfResources);
        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            bank.addCustomer(i, maxDemands[i]);
        }

        //Interactive command loop
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            System.out.flush();
            if (!in.hasNextLine()) {
                break; // end of input (Ctrl+D / Ctrl+Z)
            }
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                break;
            }
            processCommand(bank, line, numberOfResources);
        }
        in.close();
    }

    /**
     * Reads one line per customer, each with numberOfResources
     * comma-separated non-negative integers. Exits with an error
     * message if the file is missing or badly formatted.
     */
    private static int[][] readInputFile(String fileName, int numberOfResources) {
        int[][] maxDemands = new int[NUMBER_OF_CUSTOMERS][];
 
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            int customer = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // ignore blank lines
                }
                if (customer >= NUMBER_OF_CUSTOMERS) {
                    fail("input file has more than " + NUMBER_OF_CUSTOMERS
                            + " customer lines");
                }
 
                String[] parts = line.split(",", -1);
                if (parts.length != numberOfResources) {
                    fail("line for customer " + customer + " has " + parts.length
                            + " values but " + numberOfResources + " were expected");
                }
 
                int[] row = new int[numberOfResources];
                for (int j = 0; j < numberOfResources; j++) {
                    Integer value = parseNonNegative(parts[j].trim());
                    if (value == null) {
                        fail("invalid value '" + parts[j].trim()
                                + "' for customer " + customer + ", R" + j);
                    }
                    row[j] = value;
                }
                maxDemands[customer++] = row;
            }
            if (customer < NUMBER_OF_CUSTOMERS) {
                fail("input file has only " + customer + " customer lines, expected "
                        + NUMBER_OF_CUSTOMERS);
            }
        } catch (FileNotFoundException e) {
            fail("cannot open input file '" + fileName + "'");
        } catch (IOException e) {
            fail("problem reading input file: " + e.getMessage());
        }
        return maxDemands;
    }

        /*
         * TODO:
         *
         * Repeatedly read commands from the user.
         *
         * Required commands:
         *
         * RQ <customer> <R0> <R1> ... <Rm-1>
         *
         * RL <customer> <R0> <R1> ... <Rm-1>
         *
         * *
         *
         *
         * RQ:
         * Call requestResources()
         *
         * RL:
         * Call releaseResources()
         *
         * *:
         * Call getState()
         */
        

         /**
         * Parses and validates one command line, then calls the bank.
         * Invalid input is rejected here, before the bank is touched.
         */
        private static void processCommand(BankImpl bank, String line, int numberOfResources) {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toUpperCase();
 
        if (command.equals("*")) {
            if (tokens.length != 1) {
                System.out.println("Error: '*' takes no arguments");
                return;
            }
            bank.getState();
            return;
        }
 
        if (!command.equals("RQ") && !command.equals("RL")) {
            System.out.println("Error: unknown command '" + tokens[0]
                    + "' (use RQ, RL, or *)");
            return;
        }
 
        // RQ/RL <customer> <R0> ... <Rm-1>
        int expected = 2 + numberOfResources;
        if (tokens.length != expected) {
            System.out.println("Error: " + command + " needs a customer number and "
                    + numberOfResources + " resource values (got "
                    + (tokens.length - 1) + " values after the command)");
            return;
        }
 
        Integer customer = parseNonNegative(tokens[1]);
        if (customer == null || customer >= NUMBER_OF_CUSTOMERS) {
            System.out.println("Error: invalid customer number '" + tokens[1]
                    + "' (must be 0-" + (NUMBER_OF_CUSTOMERS - 1) + ")");
            return;
        }
 
        int[] amounts = new int[numberOfResources];
        for (int j = 0; j < numberOfResources; j++) {
            Integer value = parseNonNegative(tokens[j + 2]);
            if (value == null) {
                System.out.println("Error: value '" + tokens[j + 2]
                        + "' for R" + j + " must be a non-negative integer");
                return;
            }
            amounts[j] = value;
        }
 
        if (command.equals("RQ")) {
            // BankImpl prints "Approved ..." or "Denied: <reason>" itself.
            // If your team's BankImpl stops printing, print it here instead:
            //   System.out.println(approved ? "Approved" : "Denied");
            bank.requestResources(customer, amounts);
        } else {
            bank.releaseResources(customer, amounts);
        }
    }
    

    /** Returns the value if s is a non-negative integer, otherwise null. */
    private static Integer parseNonNegative(String s) {
        try {
            int value = Integer.parseInt(s);
            return value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
 
    /** Prints an error and exits (used for startup problems). */
    private static void fail(String message) {
        System.err.println("Error: " + message);
        System.exit(1);
    }
}