public abstract class PaymentFramework {
    protected double amount;
    protected double discountRate;
    protected double vatRate = 0.12;

    protected double discountedAmount;
    protected double vatAmount;
    protected double totalPayable;
    protected boolean success;

    public PaymentFramework(double amount, double discountRate) {
        this.amount = amount;
        this.discountRate = discountRate;
    }

    abstract boolean validatePayment();

    protected void computeAmounts() {
        discountedAmount = amount - (amount * discountRate);
        vatAmount = discountedAmount * vatRate;
        totalPayable = discountedAmount + vatAmount;
    }

    public void processInvoice() {
        if (!validatePayment()) {
            success = false;
            return;
        }
        computeAmounts();
        finalizeTransaction();
        success = true;
    }

    protected void finalizeTransaction() {}

    public boolean isSuccessful() { return success; }
    public double getTotalPayable() { return totalPayable; }
    public double getDiscountedAmount() { return discountedAmount; }
    public double getVatAmount() { return vatAmount; }
}