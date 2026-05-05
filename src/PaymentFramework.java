public abstract class PaymentFramework {

    protected double amount;
    protected double discountRate;
    protected double vatRate = 0.12;

    // computed values (NEW)
    protected double discountedAmount;
    protected double vatAmount;
    protected double totalPayable;
    protected boolean isSuccess;

    private static final int BOX_WIDTH = 52;

    public PaymentFramework(double amount, double discountRate) {
        this.amount = amount;
        this.discountRate = discountRate;
    }

    // ── ABSTRACT STEP ──
    abstract boolean isValidDatePayment();

    // ── CORE LOGIC ──
    protected void computeAmounts() {
        discountedAmount = amount - (amount * discountRate);
        vatAmount = discountedAmount * vatRate;
        totalPayable = discountedAmount + vatAmount;
    }

    // ── TEMPLATE METHOD ──
    public void processInvoice() {

        printHeader();

        printSectionDivider("INITIATING TRANSACTION");

        // Step 1 – Validate
        printStep("[Step 1] Validating payment...");
        if (!isValidDatePayment()) {
            System.out.println("        ERROR  Payment validation failed.");
            isSuccess = false;
            return;
        }
        System.out.println("        OK  Payment validated successfully.");

        // Step 2 – Compute (Discount + VAT)
        printStep("[Step 2] Computing totals...");
        computeAmounts();

        double discountAmount = amount - discountedAmount;

        printOk("Discount",
                String.format("%.2f%% (-%.2f)", discountRate * 100, discountAmount));

        printOk("VAT (12%)",
                String.format("%.2f", vatAmount));

        printOk("Total Payable",
                String.format("%.2f", totalPayable));

        // Step 3 – Finalize
        printStep("[Step 3] Finalizing transaction...");
        finalizeTransaction();

        isSuccess = true;
    }

    protected void finalizeTransaction() {
        System.out.println("        OK  Transaction completed.");
    }

    // getter for BookingManager (IMPORTANT)
    public boolean isSuccessful() {
        return isSuccess;
    }

    public double getTotalPayable() {
        return totalPayable;
    }

    // ── FORMATTING HELPERS ──

    private static String repeat(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    private static void printHeader() {
        printBoxTop();
        printBoxLine("PAYMENT PROCESSING SYSTEM");
        printBoxLine("Payment Framework");
        printBoxBottom();
        System.out.println();
    }

    private static void printBoxTop() {
        System.out.println("+" + repeat('=', BOX_WIDTH) + "+");
    }

    private static void printBoxBottom() {
        System.out.println("+" + repeat('=', BOX_WIDTH) + "+");
    }

    private static void printBoxLine(String text) {
        int padding = BOX_WIDTH - text.length();
        int left = padding / 2;
        int right = padding - left;
        System.out.println("|" + repeat(' ', left) + text + repeat(' ', right) + "|");
    }

    private static void printSectionDivider(String label) {
        String prefix = "+--- " + label + " ";
        int dashes = BOX_WIDTH + 2 - prefix.length() - 1;
        System.out.println(prefix + repeat('-', Math.max(0, dashes)) + "+");
    }

    private static void printStep(String step) {
        System.out.println(step);
    }

    private static void printOk(String label, String value) {
        System.out.printf("        OK  %-22s : %s%n", label, value);
    }

    public double getDiscountedAmount() {
        return discountedAmount;
    }

    public double getVatAmount() {
        return vatAmount;
    }
}