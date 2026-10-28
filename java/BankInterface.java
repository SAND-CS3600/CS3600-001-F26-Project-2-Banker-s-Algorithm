public interface BankInterface{

    void addCustomer(int customerNum, int[] maxDemand);

    void getState();

    void requestResources(int customerNum, int[] request);
    void releaseResources(int customerNum, int[] release);

}
