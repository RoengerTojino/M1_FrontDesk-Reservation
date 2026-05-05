public class CashPayment extends PaymentFramework {
    public CashPayment(double amount, double discountRate) {
        super(amount, discountRate);
    }
    @Override
    boolean isValidatePayment() {
        return amount > 0;
    }
}