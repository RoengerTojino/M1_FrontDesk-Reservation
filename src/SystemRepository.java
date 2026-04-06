import java.sql.*;

public class SystemRepository {

    private static final String DB_URL = "jdbc:sqlite:Cruise_System.db";

    public void createReservation(Guest guest, String category, int pax,
                                  String checkIn, String checkOut) {

        if (pax <= 0) {
            System.out.println("❌ Invalid pax count.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            conn.setAutoCommit(false);

            // =========================
            // 1. INSERT GUEST
            // =========================
            String insertGuest = "INSERT OR IGNORE INTO guests VALUES (?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertGuest)) {
                ps.setString(1, guest.getGuestId());
                ps.setString(2, guest.getFirstName());
                ps.setString(3, guest.getLastName());
                ps.executeUpdate();
            }

            // =========================
            // 2. FIND CABIN
            // =========================
            String findCabin = "SELECT * FROM cabins WHERE category = ? AND is_available = 1 LIMIT 1";

            String cabinId = null;

            try (PreparedStatement ps = conn.prepareStatement(findCabin)) {
                ps.setString(1, category);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    cabinId = rs.getString("cabin_id");
                } else {
                    System.out.println("❌ No available cabin.");
                    conn.rollback();
                    return;
                }
            }

            // =========================
            // 3. BUILD RESERVATION
            // =========================
            String reservationId = "RES" + System.currentTimeMillis();

            Cabin cabin = new Cabin(cabinId, category);

            Reservation reservation = new Reservation.ReservationBuilder()
                    .reservationId(reservationId)
                    .guest(guest)
                    .cabin(cabin)
                    .paxCount(pax)
                    .checkInDate(checkIn)
                    .checkOutDate(checkOut)
                    .build();

            // =========================
            // 4. INSERT RESERVATION
            // =========================
            String insertReservation = "INSERT INTO reservations VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertReservation)) {
                ps.setString(1, reservationId);
                ps.setString(2, guest.getGuestId());
                ps.setString(3, cabinId);
                ps.setInt(4, pax);
                ps.setString(5, checkIn);
                ps.setString(6, checkOut);
                ps.setString(7, "Pending");
                ps.executeUpdate();
            }

            // =========================
            // 5. UPDATE CABIN
            // =========================
            String updateCabin = "UPDATE cabins SET is_available = 0 WHERE cabin_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateCabin)) {
                ps.setString(1, cabinId);
                ps.executeUpdate();
            }

            conn.commit();

            System.out.println("\n✅ Reservation Created Successfully!");
            reservation.display();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int getMaxPaxForCategory(String category) {
        String sql = "SELECT max_pax FROM cabins WHERE category = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("max_pax");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // fallback
    }
    public void addGuest(Guest guest) {
        String sql = "INSERT INTO guests (guest_id, first_name, last_name) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guest.getGuestId());
            ps.setString(2, guest.getFirstName());
            ps.setString(3, guest.getLastName());

            ps.executeUpdate();

            System.out.println("✅ Guest added! ID: " + guest.getGuestId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void searchGuestById(String guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guestId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\n✅ Guest Found:");
                System.out.println("ID: " + rs.getString("guest_id"));
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                System.out.println("❌ No guest found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void searchGuestByName(String name) {
        String sql = "SELECT * FROM guests WHERE first_name LIKE ? OR last_name LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            ps.setString(2, "%" + name + "%");

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("\n--- Guest ---");
                System.out.println("ID: " + rs.getString("guest_id"));
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            }

            if (!found) {
                System.out.println("❌ No guest found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}