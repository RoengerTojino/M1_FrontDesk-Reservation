import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            showMenu();
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> System.out.println("Create Reservation - Feature coming soon!");
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
                default -> System.out.println("Invalid option! Please select 1-9.");
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
}