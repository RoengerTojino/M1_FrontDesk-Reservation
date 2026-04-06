import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final SystemRepository repo = new SystemRepository();

    public static void main(String[] args) {
        while (true) {
            showMenu();
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> createReservation();
                case "2" -> manageGuestProfile();
                case "3" -> assignCabin();
                case "4" -> searchReservation();
                case "5" -> processCheckIn();
                case "6" -> processCheckOut();
                case "7" -> cancelReservation();
                case "8" -> System.out.println("Move Reservation - Feature coming soon!");
                case "9" -> {
                    System.out.println("Exiting Front Desk System. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n=======================================");
        System.out.println("      Luxury Cruise Ship - Front Desk");
        System.out.println("=======================================");
        System.out.println("[1] Create Reservation");
        System.out.println("[2] Manage Guest Profile");
        System.out.println("[3] Assign Cabin");
        System.out.println("[4] Search Reservation");
        System.out.println("[5] Process Check-In");
        System.out.println("[6] Process Check-Out");
        System.out.println("[7] Cancel Reservation");
        System.out.println("[8] Move Reservation");
        System.out.println("[9] Exit");
        System.out.print("\nSelect an option: ");
    }

    private static void createReservation() {
        System.out.println("\n=== CREATE RESERVATION ===");

        System.out.print("Guest First Name: ");
        String firstName = sc.nextLine();

        System.out.print("Guest Last Name: ");
        String lastName = sc.nextLine();

        String guestId = "G" + System.currentTimeMillis();
        Guest guest = new Guest(guestId, firstName, lastName);

        System.out.print("Cabin Category (Standard/Deluxe/Suite): ");
        String category = sc.nextLine();

        // ✅ Pax validation
        int pax;
        while (true) {
            System.out.print("Number of Guests: ");
            try {
                pax = Integer.parseInt(sc.nextLine());
                if (pax <= 0) {
                    System.out.println("❌ Pax must be greater than 0.");
                    continue;
                }

                // Fetch cabin max_pax from DB
                int maxPax = repo.getMaxPaxForCategory(category);
                if (pax > maxPax) {
                    System.out.println("❌ Exceeds maximum allowed pax for this cabin (" + maxPax + ").");
                    continue;
                }

                break;
            } catch (Exception e) {
                System.out.println("❌ Invalid number.");
            }
        }

        System.out.print("Check-in Date (YYYY-MM-DD): ");
        String checkIn = sc.nextLine();

        System.out.print("Check-out Date (YYYY-MM-DD): ");
        String checkOut = sc.nextLine();

        repo.createReservation(guest, category, pax, checkIn, checkOut);

        System.out.println("\nPress Enter to return...");
        sc.nextLine();
    }
    private static void manageGuestProfile() {
        while (true) {
            System.out.println("\n=== MANAGE GUEST PROFILE ===");
            System.out.println("[1] Add Guest");
            System.out.println("[2] Search Guest");
            System.out.println("[3] Back");
            System.out.print("Select option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> addGuest();
                case "2" -> searchGuest();
                case "3" -> {
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }
    private static void addGuest() {
        System.out.println("\n=== ADD GUEST ===");

        System.out.print("First Name: ");
        String firstName = sc.nextLine();

        System.out.print("Last Name: ");
        String lastName = sc.nextLine();

        String guestId = "G" + System.currentTimeMillis();
        Guest guest = new Guest(guestId, firstName, lastName);

        repo.addGuest(guest);

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    private static void searchGuest() {
        System.out.println("\n=== SEARCH GUEST ===");
        System.out.println("[1] By Guest ID");
        System.out.println("[2] By Name");
        System.out.print("Select: ");

        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> {
                System.out.print("Enter Guest ID: ");
                repo.searchGuestById(sc.nextLine());
            }
            case "2" -> {
                System.out.print("Enter Name: ");
                repo.searchGuestByName(sc.nextLine());
            }
            default -> System.out.println("Invalid option!");
        }

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

    private static void assignCabin() {
        System.out.println("\n=== ASSIGN CABIN ===");

        System.out.print("Enter Reservation ID: ");
        String reservationId = sc.nextLine();

        System.out.print("Enter Cabin Category (Standard/Deluxe/Suite): ");
        String category = sc.nextLine();

        repo.assignCabin(reservationId, category);

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    private static void searchReservation() {
        System.out.println("\n=== SEARCH RESERVATION ===");
        System.out.println("[1] By Reservation ID");
        System.out.println("[2] By Guest ID");
        System.out.print("Select: ");

        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> {
                System.out.print("Enter Reservation ID: ");
                repo.searchReservationById(sc.nextLine());
            }
            case "2" -> {
                System.out.print("Enter Guest ID: ");
                repo.searchReservationByGuest(sc.nextLine());
            }
            default -> System.out.println("Invalid option!");
        }

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    private static void processCheckIn() {
        System.out.println("\n=== PROCESS CHECK-IN ===");

        System.out.print("Enter Reservation ID: ");
        String reservationId = sc.nextLine();

        repo.processCheckIn(reservationId);

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    private static void processCheckOut() {
        System.out.println("\n=== PROCESS CHECK-OUT ===");

        System.out.print("Enter Reservation ID: ");
        String reservationId = sc.nextLine();

        repo.processCheckOut(reservationId);

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    private static void cancelReservation() {
        System.out.println("\n=== CANCEL RESERVATION ===");

        System.out.print("Enter Reservation ID: ");
        String reservationId = sc.nextLine();

        repo.cancelReservation(reservationId);

        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
}