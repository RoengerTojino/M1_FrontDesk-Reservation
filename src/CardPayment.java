public class CardPayment extends PaymentFramework {
    private String cardNumber;

    public CardPayment(double amount, double discountRate, String cardNumber) {
        super(amount, discountRate);
        this.cardNumber = cardNumber;
    }
    @Override
    boolean validatePayment() { return cardNumber != null && cardNumber.matches("\\d{16}");
    }
}