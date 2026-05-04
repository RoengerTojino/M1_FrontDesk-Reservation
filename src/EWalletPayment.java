public class EWalletPayment extends PaymentFramework {
    private String walletId;

    public EWalletPayment(double amount, double discountRate, String walletId) {
        super(amount, discountRate);
        this.walletId = walletId;
    }

    boolean isValidDatePayment() {
        return walletId != null && !walletId.isEmpty();
    }
}