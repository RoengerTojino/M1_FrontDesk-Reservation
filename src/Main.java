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
                case "2" -> System.out.println("Manage Guest Profile - Feature coming soon!");
                case "3" -> System.out.println("Assign Cabin - Feature coming soon!");
                case "4" -> System.out.println("Search Reservation - Feature coming soon!");
                case "5" -> System.out.println("Process Check-In - Feature coming soon!");
                case "6" -> System.out.println("Process Check-Out - Feature coming soon!");
                case "7" -> System.out.println("Cancel Reservation - Feature coming soon!");
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
}