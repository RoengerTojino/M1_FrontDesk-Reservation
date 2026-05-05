public class CashPayment extends PaymentFramework {
    public CashPayment(double amount, double discountRate) {
        super(amount, discountRate);
    }
    @Override
    boolean isValidDatePayment() {
        return amount > 0;
    }
}