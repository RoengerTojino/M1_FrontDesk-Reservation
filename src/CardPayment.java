public class CardPayment extends PaymentFramework {
    private String cardNumber;

    public CardPayment(double amount, double discountRate, String cardNumber) {
        super(amount, discountRate);
        this.cardNumber = cardNumber;
    }

    boolean validatePayment() {
        return cardNumber != null && cardNumber.length() == 16;
    }
}