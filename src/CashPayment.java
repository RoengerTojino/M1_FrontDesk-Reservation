public class CashPayment extends PaymentFramework {
    private double cashGiven;

    public CashPayment(double amount, double discountRate, double cashGiven) {
        super(amount, discountRate);
        this.cashGiven = cashGiven;
    }

    @Override
    boolean validatePayment() {
        computeAmounts(); // ensure totalPayable is computed
        return cashGiven >= totalPayable;
    }
}