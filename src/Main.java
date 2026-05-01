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

    // ================= MAIN MENU =================
    private static void showMainMenu() {
        System.out.println("\n=======================================");
        System.out.println(" Luxury Cruise Ship System");
        System.out.println("=======================================");
        System.out.println("[1] Front Desk & Reservation");
        System.out.println("[2] Exit");
    }

    // ================= FRONT DESK MENU =================
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

    // ================= CREATE RESERVATION =================
    private static void createReservation() {
        System.out.println("\n=== CREATE RESERVATION ===");

        // 1. Guest
        System.out.print("Guest First Name: ");
        String first = sc.nextLine();

        System.out.print("Guest Last Name: ");
        String last = sc.nextLine();

        Guest guest = new Guest(null, first, last);
        int guestId = repo.addGuest(guest);

        // 2. DATE FIRST 🔥
        String checkIn;
        String checkOut;

        while (true) {
            try {
                System.out.print("Check-in Date (YYYY-MM-DD): ");
                checkIn = sc.nextLine();
                LocalDate in = LocalDate.parse(checkIn);

                System.out.print("Check-out Date (YYYY-MM-DD): ");
                checkOut = sc.nextLine();
                LocalDate out = LocalDate.parse(checkOut);

                if (!out.isAfter(in)) {
                    System.out.println("❌ Check-out must be after check-in");
                    continue;
                }

                if (in.isBefore(LocalDate.now())) {
                    System.out.println("❌ Cannot book past dates");
                    continue;
                }

                break;

            } catch (Exception e) {
                System.out.println("❌ Invalid date format! Use YYYY-MM-DD");
            }
        }

        // 3. SHOW ONLY AVAILABLE CABINS 🔥
        repo.showAvailableCabins(checkIn, checkOut);

        // 4. Select cabin
        String cabinId;
        while (true) {
            System.out.print("Enter Cabin ID: ");
            cabinId = sc.nextLine().toUpperCase();

            if (repo.isCabinAvailable(cabinId, checkIn, checkOut)) {
                break;
            } else {
                System.out.println("❌ Cabin not available for selected dates.");
            }
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

                // 🔥 GET MAX PAX FROM DB
                int maxPax = repo.getMaxPax(cabinId);

                if (pax > maxPax) {
                    System.out.println("❌ Too many guests! Max allowed: " + maxPax);
                    continue; // 🔥 THIS FIXES YOUR LOOP
                }

                break;

            } catch (Exception e) {
                System.out.println("❌ Invalid number");
            }
        }

        // 6. Save
        repo.createReservation(guestId, cabinId, pax, checkIn, checkOut);
    }

    // ================= MANAGE RESERVATION =================
    private static void manageReservation() {
        while (true) {

            // ===== VIEW CHOICE =====
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

            // ===== TABLE =====
            System.out.println("\n=== RESERVATION LIST ===");
            System.out.printf("%-12s %-10s %-18s %-10s %-5s %-12s %-12s %-12s%n",
                    "Res ID","Guest ID","Guest Name","Cabin","Pax","Check-In","Check-Out","Status");
            System.out.println("-------------------------------------------------------------------------------------------------------");

            repo.showAllReservations(viewChoice);

            // ===== SELECT =====
            System.out.print("\nEnter Reservation ID (or 0 to return): ");
            String reservationId = sc.nextLine();
            if (reservationId.equals("0")) return;

            String status = repo.getReservationStatus(reservationId);
            if (status == null) {
                System.out.println("❌ Reservation not found.");
                continue;
            }

            System.out.println("Status: " + status);

            // ===== BLOCK FINISHED =====
            if (status.equalsIgnoreCase("Checked-Out") || status.equalsIgnoreCase("Cancelled")) {
                System.out.println("❌ Cannot modify.");
                continue;
            }

            // ===== CHECKED-IN =====
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
                    default -> System.out.println("Invalid.");
                }
            }

            // ===== PENDING =====
            else {
                System.out.println("\n[1] Check-In");
                System.out.println("[2] Move Reservation");
                System.out.println("[3] Cancel Reservation");
                System.out.println("[4] Return");
                System.out.print("Select: ");
                String action = sc.nextLine();

                switch (action) {
                    case "1" -> repo.checkIn(reservationId);
                    case "2" -> moveReservation(reservationId);
                    case "3" -> repo.cancelReservation(reservationId);
                    case "4" -> {
                        continue;
                    }
                    default -> System.out.println("Invalid.");
                }
            }

            System.out.println("\nPress Enter to continue...");
            sc.nextLine();
        }
    }

    // ================= MOVE RESERVATION =================
    private static void moveReservation(String reservationId) {
        System.out.println("\n=== MOVE RESERVATION ===");
        System.out.println("[1] Cabin Only");
        System.out.println("[2] Dates Only");
        System.out.println("[3] Both");
        System.out.print("Select: ");
        String choice = sc.nextLine();

        String cabinId = null;
        String checkIn = null;
        String checkOut = null;

        switch (choice) {

            // ===== CABIN ONLY =====
            case "1" -> {
                String[] dates = repo.getReservationDates(reservationId);
                checkIn = dates[0];
                checkOut = dates[1];

                repo.showAvailableCabins(checkIn, checkOut);

                while (true) {
                    System.out.print("Enter New Cabin ID: ");
                    cabinId = sc.nextLine().toUpperCase();

                    if (repo.isCabinAvailable(cabinId, checkIn, checkOut)) break;
                    System.out.println("❌ Cabin not available for selected dates.");
                }
            }

            // ===== DATES ONLY =====
            case "2" -> {
                String[] dates = getValidDates();
                checkIn = dates[0];
                checkOut = dates[1];
            }

            // ===== BOTH =====
            case "3" -> {
                String[] dates = getValidDates();
                checkIn = dates[0];
                checkOut = dates[1];

                repo.showAvailableCabins(checkIn, checkOut);

                while (true) {
                    System.out.print("Enter New Cabin ID: ");
                    cabinId = sc.nextLine().toUpperCase();

                    if (repo.isCabinAvailable(cabinId, checkIn, checkOut)) break;
                    System.out.println("❌ Cabin not available for selected dates.");
                }
            }

            default -> {
                System.out.println("Invalid.");
                return;
            }
        }

        repo.moveReservation(reservationId, cabinId, checkIn, checkOut);
    }

    // ================= MANAGE GUESTS =================
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
}