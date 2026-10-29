public interface BankInterface{

    void addCustomer(int customerNum, int[] maxDemand);

    void getState();

    boolean requestResources(int customerNum, int[] request);
    void releaseResources(int customerNum, int[] release);

}
