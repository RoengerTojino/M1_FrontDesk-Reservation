public abstract class PaymentFramework {
    protected double amount;
    protected double discountRate;
    protected double vatRate = 0.12;

    protected double discountedAmount;
    protected double vatAmount;
    protected double totalPayable;
    protected boolean isSuccess;

    public PaymentFramework(double amount, double discountRate) {
        this.amount = amount;
        this.discountRate = discountRate;
    }

    abstract boolean isValidDatePayment();

    protected void computeAmounts() {
        discountedAmount = amount - (amount * discountRate);
        vatAmount = discountedAmount * vatRate;
        totalPayable = discountedAmount + vatAmount;
    }

    public void processInvoice() {
        if (!isValidDatePayment()) {
            isSuccess = false;
            return;
        }
        computeAmounts();
        finalizeTransaction();
        isSuccess = true;
    }

    protected void finalizeTransaction() {}

    public boolean isSuccessful() { return isSuccess; }
    public double getTotalPayable() { return totalPayable; }
    public double getDiscountedAmount() { return discountedAmount; }
    public double getVatAmount() { return vatAmount; }
}