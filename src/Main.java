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
            System.out.println("[1] View Pricing"); // ✅ NEW
            System.out.println("[2] Create Reservation");
            System.out.println("[3] Manage Reservation");
            System.out.println("[4] Manage Guests");
            System.out.println("[5] Back");
            System.out.print("Select: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> showPricing(); // ✅ NEW
                case "2" -> createReservation();
                case "3" -> manageReservation();
                case "4" -> manageGuests();
                case "5" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void createReservation() {
        System.out.println("\n=== CREATE RESERVATION ===");

        Guest guest = getGuest();
        int guestId = repo.addGuest(guest);
        int sailingId = selectSailing();
        String cabinId = selectCabin(sailingId);
        int pax = getPax(cabinId);

        String cabinType = repo.getCabinType(cabinId);
        String destination = repo.getDestinationBySailing(sailingId);

        double cabinPrice = switch (cabinType.toUpperCase()) {
            case "SUITE" -> 30000;
            case "DELUXE" -> 20000;
            default -> 12000;
        };

        double destinationFee = getDestinationFee(destination);

        double baseAmount = (cabinPrice * pax) + destinationFee;

        double discountRate = applyCoupon();

        handlePayment(baseAmount, discountRate,
                guestId, cabinId, pax, sailingId,
                cabinType, destination,
                guest.getFirstName(), guest.getLastName());
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
                    case "2" -> { continue; }
                    default -> System.out.println("Invalid option.");
                }
            }

            else if (status.equalsIgnoreCase("Pending")) {
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
    }//====================================================================================================

    private static Guest getGuest() {
        System.out.print("First Name: ");
        String first = sc.nextLine();

        System.out.print("Last Name: ");
        String last = sc.nextLine();

        return new Guest(null, first, last);
    }

    private static int selectSailing() {
        repo.showAllSailings();
        while (true) {
            try {
                System.out.print("Enter Sailing ID: ");
                int id = Integer.parseInt(sc.nextLine());
                if (repo.isSailingExists(id)) return id;
                System.out.println("❌ Invalid sailing.");
            } catch (Exception e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }

    private static String selectCabin(int sailingId) {
        repo.showAvailableCabinsBySailing(sailingId);
        while (true) {
            System.out.print("Enter Cabin ID: ");
            String cabinId = sc.nextLine().toUpperCase();
            if (repo.isCabinAvailableForSailing(cabinId, sailingId)) return cabinId;
            System.out.println("❌ Cabin not available.");
        }
    }

    private static int getPax(String cabinId) {
        while (true) {
            try {
                System.out.print("Number of Guests: ");
                int pax = Integer.parseInt(sc.nextLine());
                int max = repo.getMaxPax(cabinId);

                if (pax > 0 && pax <= max) return pax;

                System.out.println("❌ Max allowed: " + max);
            } catch (Exception e) {
                System.out.println("❌ Invalid number");
            }
        }
    }

    private static double applyCoupon() {
        while (true) {
            System.out.print("Coupon? (Y/N): ");
            String ans = sc.nextLine();

            if (ans.equalsIgnoreCase("N")) {
                return 0.0;
            }

            if (ans.equalsIgnoreCase("Y")) {
                System.out.print("Enter code: ");
                String code = sc.nextLine().toUpperCase();

                switch (code) {
                    case "DISC10": return 0.10;
                    case "DISC20": return 0.20;
                    case "VIP50": return 0.50;
                    default:
                        System.out.println("❌ Invalid coupon.");
                        // 🔥 goes back to Coupon? (Y/N)
                }
            } else {
                System.out.println("❌ Please enter Y or N.");
            }
        }
    }

    private static void handlePayment(double baseAmount, double discountRate,
                                      int guestId, String cabinId, int pax, int sailingId,
                                      String cabinType, String destination,
                                      String first, String last) {

        // PREVIEW
        double discounted = baseAmount - (baseAmount * discountRate);
        double vat = discounted * 0.12;
        double total = discounted + vat;

        System.out.println("\n=== PAYMENT PREVIEW ===");
        System.out.println("Estimated Total: ₱" + total);

        // CONFIRM LOOP
        while (true) {
            System.out.print("Proceed? (Y/N): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("Y")) break;
            if (confirm.equalsIgnoreCase("N")) return;

            System.out.println("❌ Invalid input.");
        }

        // PAYMENT METHOD LOOP
        String choice;
        while (true) {
            System.out.println("\n[1] Cash [2] Card [3] E-Wallet");
            System.out.print("Select: ");
            choice = sc.nextLine();

            if (choice.equals("1") || choice.equals("2") || choice.equals("3")) break;
            System.out.println("❌ Invalid choice.");
        }

        PaymentFramework payment = null;
        String card = "", wallet = "";
        double cash = 0;

        // CARD LOOP
        if (choice.equals("2")) {
            while (true) {
                System.out.print("Card (16 digits): ");
                card = sc.nextLine();

                if (!card.matches("\\d+")) {
                    System.out.println("❌ Numbers only.");
                    continue;
                }
                if (card.length() != 16) {
                    System.out.println("❌ Must be 16 digits.");
                    continue;
                }
                break;
            }
            payment = new CardPayment(baseAmount, discountRate, card);
        }

        // WALLET LOOP
        if (choice.equals("3")) {
            while (true) {
                System.out.print("Wallet (11 digits): ");
                wallet = sc.nextLine();

                if (!wallet.matches("\\d+")) {
                    System.out.println("❌ Numbers only.");
                    continue;
                }
                if (wallet.length() != 11) {
                    System.out.println("❌ Must be 11 digits.");
                    continue;
                }
                break;
            }
            payment = new EWalletPayment(baseAmount, discountRate, wallet);
        }

        // CASH LOOP
        if (choice.equals("1")) {
            while (true) {
                try {
                    System.out.print("Cash: ");
                    cash = Double.parseDouble(sc.nextLine());

                    if (cash <= 0) {
                        System.out.println("❌ Must be > 0.");
                        continue;
                    }
                    if (cash < total) {
                        System.out.println("❌ Insufficient. Need ₱" + total);
                        continue;
                    }
                    break;

                } catch (Exception e) {
                    System.out.println("❌ Invalid amount.");
                }
            }
            payment = new CashPayment(baseAmount, discountRate);
        }

        // PROCESS
        payment.processInvoice();

        if (!payment.isSuccessful()) {
            System.out.println("❌ Payment failed");
            return;
        }

        // RESULTS
        double finalTotal = payment.getTotalPayable();
        double discountAmt = baseAmount - payment.getDiscountedAmount();
        double vatAmt = payment.getVatAmount();

        // SAVE
        int reservationId = repo.createReservation(guestId, cabinId, pax, sailingId);
        String ref = "CR-" + String.format("%06d", reservationId);

        repo.savePayment(reservationId, baseAmount, discountAmt, vatAmt, finalTotal, ref);

        // ✅ CALL RECEIPT HERE
        printReceipt(first, last, cabinType, cabinId, destination, pax,
                baseAmount, discountAmt, vatAmt, finalTotal, ref);

        // OUTPUT
        System.out.println("\n🎟 Ref: " + ref);

        if (choice.equals("1")) {
            System.out.println("💵 Change: ₱" + (cash - finalTotal));
        }

        System.out.println("✅ Reservation + Payment Completed!");
    }

    private static double getDestinationFee(String destination) {
        return switch (destination) {
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
    }

    private static void printReceipt(String first, String last, String cabinType,
                                     String cabinId, String destination, int pax,
                                     double baseAmount, double discountAmt,
                                     double vatAmt, double finalTotal, String reference) {

        System.out.println("\n=== RESERVATION RECEIPT ===");
        System.out.println("Guest : " + first + " " + last);
        System.out.println("Cabin : " + cabinType + " (" + cabinId + ")");
        System.out.println("Destination : " + destination);
        System.out.println("Guests : " + pax);

        System.out.println("\nSUBTOTAL : ₱" + baseAmount);
        System.out.println("Discount : ₱" + discountAmt);
        System.out.println("VAT : ₱" + vatAmt);
        System.out.println("FINAL TOTAL : ₱" + finalTotal);

        System.out.println("\n🎟 Reference No: " + reference);
        System.out.println("✅ PAYMENT CONFIRMED");
    }
}