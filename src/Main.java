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
                case "4" -> { return; }
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

        // 2. SELECT SAILING 🔥
        repo.showAllSailings(); // you must create this
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

        // 3. SHOW AVAILABLE CABINS (based on sailing)
        repo.showAvailableCabinsBySailing(sailingId);

        // 4. Select cabin
        String cabinId;
        while (true) {
            System.out.print("Enter Cabin ID: ");
            cabinId = sc.nextLine().toUpperCase();

            if (repo.isCabinAvailableForSailing(cabinId, sailingId)) break;
            System.out.println("❌ Cabin not available for this sailing.");
        }

        // 5. Pax
        int pax;
        while (true) {
            try {
                System.out.print("Number of Guests: ");
                pax = Integer.parseInt(sc.nextLine());

                if (pax <= 0) {
                    System.out.println("❌ Must be greater than 0");
                    continue;
                }

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

        PaymentFramework payment = null;

        switch (choice) {
            case "1" -> payment = new CashPayment(baseAmount, discountRate);
            case "2" -> payment = new CardPayment(baseAmount, discountRate, card);
            case "3" -> payment = new EWalletPayment(baseAmount, discountRate, wallet);
        }

        payment.processInvoice();

        if (!payment.isSuccessful()) {
            System.out.println("❌ Payment Failed.");
            return;
        }

// computed values
        double discountAmount = baseAmount - payment.getDiscountedAmount();
        double vat = payment.getVatAmount();
        double finalTotal = payment.getTotalPayable();

// SAVE PAYMENT RECORD (Requirement 1)
        repo.savePayment(
                reservationId,
                baseAmount,
                discountAmount,
                vat,
                finalTotal,
                reference
        );



        // 6. SAVE 🔥
        repo.createReservation(guestId, cabinId, pax, sailingId);
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
                    case "3" -> repo.cancelReservation(reservationId);
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

        repo.showAllSailings();

        int sailingId;
        while (true) {
            try {
                System.out.print("Enter New Sailing ID: ");
                sailingId = Integer.parseInt(sc.nextLine());

                if (repo.isSailingExists(sailingId)) break;

                System.out.println("❌ Invalid sailing.");
            } catch (Exception e) {
                System.out.println("❌ Invalid input.");
            }
        }

        repo.showAvailableCabinsBySailing(sailingId);

        String cabinId;
        while (true) {
            System.out.print("Enter New Cabin ID: ");
            cabinId = sc.nextLine().toUpperCase();

            if (repo.isCabinAvailableForSailing(cabinId, sailingId)) break;

            System.out.println("❌ Cabin not available.");
        }

        repo.moveReservation(reservationId, cabinId, sailingId);
    }
}