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
    public void assignCabin(String reservationId, String category) {

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            conn.setAutoCommit(false);

            // 1. Check reservation exists
            String checkRes = "SELECT * FROM reservations WHERE reservation_id = ?";
            String cabinId = null;

            try (PreparedStatement ps = conn.prepareStatement(checkRes)) {
                ps.setString(1, reservationId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Reservation not found.");
                    return;
                }
            }

            // 2. Find available cabin
            String findCabin = "SELECT cabin_id FROM cabins WHERE category = ? AND is_available = 1 LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(findCabin)) {
                ps.setString(1, category);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    cabinId = rs.getString("cabin_id");
                } else {
                    System.out.println("❌ No available cabin for this category.");
                    conn.rollback();
                    return;
                }
            }

            // 3. Update reservation with cabin
            String updateRes = "UPDATE reservations SET cabin_id = ? WHERE reservation_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
                ps.setString(1, cabinId);
                ps.setString(2, reservationId);
                ps.executeUpdate();
            }

            // 4. Mark cabin as unavailable
            String updateCabin = "UPDATE cabins SET is_available = 0 WHERE cabin_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateCabin)) {
                ps.setString(1, cabinId);
                ps.executeUpdate();
            }

            conn.commit();

            System.out.println("✅ Cabin assigned successfully!");
            System.out.println("Cabin ID: " + cabinId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}