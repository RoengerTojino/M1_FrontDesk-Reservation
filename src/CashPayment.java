public class CashPayment extends PaymentFramework {
    public CashPayment(double amount, double discountRate) {
        super(amount, discountRate);
    }
    boolean isValidDatePayment() {
        return amount > 0;
    }
}