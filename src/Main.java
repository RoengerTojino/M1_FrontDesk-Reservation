import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


public class Main {


    private static final Scanner sc = new Scanner(System.in);
    private static final SystemRepository repo = new SystemRepository();

    public static void main(String[] args) {
        while (true) {
            showMainMenu();
            System.out.print("Select: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> frontDeskMenu();
                case "2" -> {
                    System.out.println("Exiting system...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n=======================================");
        System.out.println(" Luxury Cruise Ship System");
        System.out.println("=======================================");
        System.out.println("[1] Front Desk & Reservation");
        System.out.println("[2] Exit");
    }

    private static void frontDeskMenu() {
        while (true) {
            System.out.println("\n=======================================");
            System.out.println(" Front Desk Menu");
            System.out.println("=======================================");
            System.out.println("[1] Create Reservation");
            System.out.println("[2] Manage Reservation");
            System.out.println("[3] Manage Guests");
            System.out.println("[4] Back");

            System.out.print("Select: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> createReservation();
                case "2" -> manageReservation();
                case "3" -> manageGuests();
                case "4" -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void createReservation() {
        System.out.println("\n=== CREATE RESERVATION ===");

        // 1. Guest
        System.out.print("Guest First Name: ");
        String first = sc.nextLine();
        System.out.print("Guest Last Name: ");
        String last = sc.nextLine();
        Guest guest = new Guest(null, first, last);
        int guestId = repo.addGuest(guest);

        // 2. Sailing
        repo.showAllSailings();
        int sailingId;
        while (true) {
            try {
                System.out.print("Enter Sailing ID: ");
                sailingId = Integer.parseInt(sc.nextLine());
                if (repo.isSailingExists(sailingId)) break;
                System.out.println("❌ Invalid sailing.");
            } catch (Exception e) {
                System.out.println("❌ Invalid input.");
            }
        }

        // 3. Cabin
        repo.showAvailableCabinsBySailing(sailingId);
        String cabinId;
        while (true) {
            System.out.print("Enter Cabin ID: ");
            cabinId = sc.nextLine().toUpperCase();
            if (repo.isCabinAvailableForSailing(cabinId, sailingId)) break;
            System.out.println("❌ Cabin not available.");
        }

        // 4. Pax
        int pax;
        while (true) {
            try {
                System.out.print("Number of Guests: ");
                pax = Integer.parseInt(sc.nextLine());
                if (pax <= 0) continue;

                int maxPax = repo.getMaxPax(cabinId);
                if (pax > maxPax) {
                    System.out.println("❌ Too many guests! Max: " + maxPax);
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println("❌ Invalid number");
            }
        }

        // DATA
        String cabinType = repo.getCabinType(cabinId);
        String destination = repo.getDestinationBySailing(sailingId);

        double cabinPrice = switch (cabinType.toUpperCase()) {
            case "SUITE" -> 30000;
            case "DELUXE" -> 20000;
            default -> 12000;
        };

        double destinationFee = switch (destination) {
            case "Alaska" -> 8000;
            case "Mediterranean" -> 12000;
            case "Caribbean" -> 10000;
            case "Greek Isles" -> 13000;
            case "Norwegian Fjords" -> 15000;
            case "Japan" -> 11000;
            case "Australia" -> 18000;
            case "Bali" -> 9000;
            case "Hawaii" -> 16000;
            default -> 10000;
        };

        double subtotal = cabinPrice * pax;
        double baseAmount = subtotal + destinationFee;

        // COUPON
        double discountRate = 0.0;

        while (true) {
            System.out.print("\nDo you have a coupon? (Y/N): ");
            String ans = sc.nextLine().trim();

            if (ans.equalsIgnoreCase("N")) {
                break;

            } else if (ans.equalsIgnoreCase("Y")) {

                System.out.print("Enter coupon code: ");
                String code = sc.nextLine().trim().toUpperCase();

                switch (code) {
                    case "DISC10" -> discountRate = 0.10;
                    case "DISC20" -> discountRate = 0.20;
                    case "VIP50" -> discountRate = 0.50;
                    default -> {
                        System.out.println("❌ Invalid coupon.");
                        continue; // 🔥 goes back to Y/N question
                    }
                }

                System.out.println("✅ Discount applied!");
                break;

            } else {
                System.out.println("❌ Invalid input. Please enter Y or N.");
            }
        }

        // PREVIEW (ESTIMATE ONLY)
        double previewDiscount = baseAmount * discountRate;
        double previewDiscounted = baseAmount - previewDiscount;
        double previewVat = previewDiscounted * 0.12;
        double previewTotal = previewDiscounted + previewVat;

        System.out.println("\n=== PAYMENT PREVIEW ===");
        System.out.println("Estimated Total: ₱" + previewTotal);

        // CONFIRM
        while (true) {
            System.out.print("\nProceed to payment? (Y/N): ");
            String confirm = sc.nextLine().trim();

            if (confirm.equalsIgnoreCase("Y")) {
                break;
            }
            else if (confirm.equalsIgnoreCase("N")) {
                System.out.println("❌ Reservation cancelled.");
                return;
            }
            else {
                System.out.println("❌ Invalid input. Please enter Y or N.");
            }
        }

        // PAYMENT
        String choice;
        while (true) {
            System.out.println("\n=== PAYMENT ===");
            System.out.println("[1] Cash");
            System.out.println("[2] Card");
            System.out.println("[3] E-Wallet");
            System.out.print("Select: ");
            choice = sc.nextLine();

            if (choice.equals("1") || choice.equals("2") || choice.equals("3")) break;
            System.out.println("❌ Invalid choice. Try again.");
        }

        PaymentFramework payment = null;
        String card = "";
        String wallet = "";

// CARD INPUT (FIXED)
        if (choice.equals("2")) {
            while (true) {
                System.out.print("Enter Card Number (16 digits): ");
                card = sc.nextLine();

                if (!card.matches("\\d+")) {
                    System.out.println("❌ Card must contain numbers only.");
                    continue;
                }

                if (card.length() != 16) {
                    System.out.println("❌ Card must be exactly 16 digits.");
                    continue;
                }

                System.out.println("✅ Card accepted.");
                break;
            }
        }

// E-WALLET INPUT (FIXED)
        if (choice.equals("3")) {
            while (true) {
                System.out.print("Enter Wallet Number (11 digits): ");
                wallet = sc.nextLine();

                if (!wallet.matches("\\d+")) {
                    System.out.println("❌ Must be numbers only.");
                    continue;
                }

                if (wallet.length() != 11) {
                    System.out.println("❌ Must be exactly 11 digits.");
                    continue;
                }

                System.out.println("✅ Wallet accepted.");
                break;
            }
        }

/// CASH INPUT (FIXED - use previewTotal)
        double cashGiven = 0;
        if (choice.equals("1")) {
            while (true) {
                try {
                    System.out.print("Enter Amount: ");
                    cashGiven = Double.parseDouble(sc.nextLine());

                    if (cashGiven <= 0) {
                        System.out.println("❌ Amount must be greater than 0.");
                        continue;
                    }

                    // ✅ USE previewTotal (closest estimate BEFORE framework)
                    if (cashGiven < previewTotal) {
                        System.out.println("❌ Insufficient amount. Need at least ₱" + previewTotal);
                        continue;
                    }

                    System.out.println("✅ Payment accepted.");
                    break;

                } catch (Exception e) {
                    System.out.println("❌ Invalid amount.");
                }
            }
        }

// CREATE PAYMENT
        switch (choice) {
            case "1" -> payment = new CashPayment(baseAmount, discountRate);
            case "2" -> payment = new CardPayment(baseAmount, discountRate, card);
            case "3" -> payment = new EWalletPayment(baseAmount, discountRate, wallet);
        }

// PROCESS
        payment.processInvoice();

        if (!payment.isSuccessful()) {
            System.out.println("❌ Payment Failed.");
            return;
        }

        // ✅ GET VALUES FROM FRAMEWORK (CORRECT DESIGN)
        double discountAmount = baseAmount - payment.getDiscountedAmount();
        double vat = payment.getVatAmount();
        double finalTotal = payment.getTotalPayable();

        // RECEIPT (NOW CORRECT)
        System.out.println("\n=== RESERVATION RECEIPT ===");
        System.out.println("Guest        : " + first + " " + last);
        System.out.println("Cabin        : " + cabinType + " (" + cabinId + ")");
        System.out.println("Destination  : " + destination);
        System.out.println("Guests       : " + pax);

        System.out.println("\nSUBTOTAL     : ₱" + baseAmount);
        System.out.println("Discount     : ₱" + discountAmount);
        System.out.println("VAT          : ₱" + vat);
        System.out.println("FINAL TOTAL  : ₱" + finalTotal);

        // SAVE
        int reservationId = repo.createReservation(guestId, cabinId, pax, sailingId);

        String reference = "CR-" + String.format("%06d", reservationId);

        repo.savePayment(
                reservationId,
                baseAmount,
                discountAmount,
                vat,
                finalTotal,
                reference
        );

        double change = cashGiven - finalTotal;

        System.out.println("🎟 Reference No: " + reference);

        if (choice.equals("1")) {
            System.out.println("💵 Change: ₱" + change);
        }

        System.out.println("✅ Reservation + Payment Completed!");
    }

    private static void manageReservation() {
        while (true) {

            String viewChoice;
            while (true) {
                System.out.println("\n=== MANAGE RESERVATION ===");
                System.out.println("[1] Active Reservations");
                System.out.println("[2] Completed / Cancelled");
                System.out.print("Select view: ");
                viewChoice = sc.nextLine();

                if (viewChoice.equals("1") || viewChoice.equals("2")) break;
                System.out.println("❌ Invalid choice.");
            }

            System.out.println("\n=== RESERVATION LIST ===");
            repo.showAllReservations(viewChoice);

            System.out.print("\nEnter Reservation ID (or 0 to return): ");
            String reservationId = sc.nextLine();

            if (reservationId.equals("0")) return;

            String status = repo.getReservationStatus(reservationId);

            if (status == null) {
                System.out.println("❌ Reservation not found.");
                continue;
            }

            System.out.println("Status: " + status);

            if (status.equalsIgnoreCase("Checked-Out") ||
                    status.equalsIgnoreCase("Cancelled")) {
                System.out.println("❌ Cannot modify this reservation.");
                continue;
            }

            if (status.equalsIgnoreCase("Checked-In")) {
                System.out.println("\n[1] Check-Out");
                System.out.println("[2] Return");
                System.out.print("Select: ");
                String action = sc.nextLine();

                switch (action) {
                    case "1" -> repo.checkOut(reservationId);
                    case "2" -> {
                        continue;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } else if (status.equalsIgnoreCase("Pending")) {
                System.out.println("\n[1] Check-In");
                System.out.println("[2] Move Reservation");
                System.out.println("[3] Cancel Reservation");
                System.out.println("[4] Return");
                System.out.print("Select: ");
                String action = sc.nextLine();

                switch (action) {
                    case "1" -> repo.checkIn(reservationId);
                    case "2" -> moveReservation(reservationId); // ✅ works now
                    case "3" -> repo.cancelReservationWithRefund(reservationId);
                    case "4" -> { continue; }
                    default -> System.out.println("Invalid option.");
                }
            }

            System.out.println("\nPress Enter to continue...");
            sc.nextLine();
        }
    }

    private static void manageGuests() {

        while (true) {

            System.out.println("\n=== MANAGE GUESTS ===");
            System.out.println("[1] Add Guest");
            System.out.println("[2] View Guests");
            System.out.println("[3] Back");
            System.out.print("Select: ");

            String choice = sc.nextLine();

            switch (choice) {

                // ================= ADD =================
                case "1" -> {
                    System.out.print("First Name: ");
                    String first = sc.nextLine();

                    System.out.print("Last Name: ");
                    String last = sc.nextLine();

                    Guest guest = new Guest(null, first, last);
                    int id = repo.addGuest(guest);

                    System.out.println("✅ Guest added! ID: " + id);
                }

                // ================= VIEW =================
                case "2" -> {
                    System.out.println("\n=== GUEST LIST ===");
                    repo.showAllGuests();
                }

                // ================= BACK =================
                case "3" -> {
                    return;
                }

                default -> System.out.println("Invalid option.");
            }

            System.out.println("\nPress Enter to continue...");
            sc.nextLine();
        }
    }

    private static String[] getValidDates() {
        String checkIn, checkOut;

        while (true) {
            try {
                System.out.print("New Check-in (YYYY-MM-DD): ");
                checkIn = sc.nextLine();
                LocalDate in = LocalDate.parse(checkIn);

                System.out.print("New Check-out (YYYY-MM-DD): ");
                checkOut = sc.nextLine();
                LocalDate out = LocalDate.parse(checkOut);

                if (!out.isAfter(in)) {
                    System.out.println("❌ Check-out must be after check-in");
                    continue;
                }

                return new String[]{checkIn, checkOut};

            } catch (Exception e) {
                System.out.println("❌ Invalid date format! Use YYYY-MM-DD");
            }
        }
    }

    private static void moveReservation(String reservationId) {
        System.out.println("\n=== MOVE RESERVATION ===");

        String currentDestination = repo.getDestinationByReservation(reservationId);
        String currentCabinType = repo.getCabinTypeByReservation(reservationId);

        repo.showSailingsByDestination(currentDestination);

        int sailingId;
        String newDestination;

        while (true) {
            try {
                System.out.print("Enter New Sailing ID: ");
                sailingId = Integer.parseInt(sc.nextLine());

                if (!repo.isSailingExists(sailingId)) {
                    System.out.println("❌ Invalid sailing.");
                    continue;
                }

                newDestination = repo.getDestinationBySailing(sailingId);

                // ✅ SAME DESTINATION CHECK
                if (!newDestination.equalsIgnoreCase(currentDestination)) {
                    System.out.println("❌ Must be same destination (" + currentDestination + ")");
                    continue;
                }

                break;

            } catch (Exception e) {
                System.out.println("❌ Invalid input.");
            }
        }

        // ✅ SHOW ONLY SAME CABIN TYPE
        System.out.println("\nAvailable cabins (" + currentCabinType + " only):");
        boolean hasAvailable = repo.showAvailableCabinsByTypeAndSailing(sailingId, currentCabinType);

        // ❌ STOP if none available
        if (!hasAvailable) {
            System.out.println("❌ No available " + currentCabinType + " cabins. Move cancelled.");
            return;
        }

        String cabinId;
        while (true) {
            System.out.print("Enter New Cabin ID: ");
            cabinId = sc.nextLine().toUpperCase();

            if (!repo.isCabinAvailableForSailing(cabinId, sailingId)) {
                System.out.println("❌ Cabin not available.");
                continue;
            }

            // ✅ CHECK SAME TYPE
            String selectedType = repo.getCabinType(cabinId);
            if (!selectedType.equalsIgnoreCase(currentCabinType)) {
                System.out.println("❌ Must select same cabin type (" + currentCabinType + ")");
                continue;
            }

            break;
        }

        repo.moveReservation(reservationId, cabinId, sailingId);
    }

    private static void showPricing() {
        System.out.println("\n=== CABIN PRICING ===");
        System.out.println("SUITE     : ₱30000 per guest(Max pax: 4)");
        System.out.println("DELUXE    : ₱20000 per guest(Max pax: 4)");
        System.out.println("STANDARD  : ₱12000 per guest(Max pax: 6)");

        System.out.println("\n=== DESTINATION FEE ===");
        System.out.println("Alaska            : +₱8000");
        System.out.println("Mediterranean     : +₱12000");
        System.out.println("Caribbean         : +₱10000");
        System.out.println("Greek Isles       : +₱13000");
        System.out.println("Norwegian Fjords  : +₱15000");
        System.out.println("Japan             : +₱11000");
        System.out.println("Australia         : +₱18000");
        System.out.println("Bali              : +₱9000");
        System.out.println("Hawaii            : +₱16000");

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
}
