import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SystemRepository {

    private static final String DB_URL = "jdbc:sqlite:Cruise_System.db";

    // =========================
    // CREATE RESERVATION
    // =========================
    public void createReservation(int guestId, String cabinId, int pax, String checkIn, String checkOut) {

        if (pax <= 0) {
            System.out.println("❌ Invalid pax count.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // 1. Check if cabin exists & available
             String checkCabin = """
                        SELECT max_pax FROM cabins c
                        WHERE c.cabin_id = ?
                        AND NOT EXISTS (
                        SELECT 1 FROM reservations r
                        WHERE r.cabin_id = c.cabin_id
                        AND r.status != 'Cancelled'
                        AND (
                        (? < r.check_out_date) AND (? > r.check_in_date)
                        )
                        )
                        """;
            int maxPax = 0;

            try (PreparedStatement ps = conn.prepareStatement(checkCabin)) {
                ps.setString(1, cabinId);
                ps.setString(2, checkIn);
                ps.setString(3, checkOut);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Cabin not available.");
                    conn.rollback();
                    return;
                }

                maxPax = rs.getInt("max_pax");
            }

// ✅ validate pax
            if (pax > maxPax) {
                System.out.println("❌ Too many guests! Max allowed: " + maxPax);
                conn.rollback();
                return;
            }


            // 2. Insert reservation (NO manual ID)
            String insertReservation =
                    "INSERT INTO reservations (guest_id, cabin_id, pax_count, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertReservation, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, guestId);
                ps.setString(2, cabinId);
                ps.setInt(3, pax);
                ps.setString(4, checkIn);
                ps.setString(5, checkOut);
                ps.setString(6, "Pending");

                ps.executeUpdate();

                // get auto ID
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int reservationId = keys.getInt(1);
                    System.out.println("✅ Reservation Created! ID: RES-" + String.format("%03d", reservationId));
                }
            }

            // 3. Occupy cabin
            String updateCabin = "UPDATE cabins SET is_available = 0 WHERE cabin_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateCabin)) {
                ps.setString(1, cabinId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // CHECK-IN
    // =========================
    public void checkIn(String reservationId) {
        String sql = "SELECT status FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Reservation not found.");
                return;
            }

            String status = rs.getString("status");

            if (status.equalsIgnoreCase("Checked-In")) {
                System.out.println("⚠️ Already checked-in.");
                return;
            }

            if (status.equalsIgnoreCase("Checked-Out") || status.equalsIgnoreCase("Cancelled")) {
                System.out.println("❌ Cannot check-in.");
                return;
            }

            String update = "UPDATE reservations SET status = 'Checked-In' WHERE reservation_id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(update)) {
                updatePs.setString(1, reservationId);
                updatePs.executeUpdate();
            }

            System.out.println("✅ Check-in successful!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // CHECK-OUT
    // =========================
    public void checkOut(String reservationId) {
        String getRes = "SELECT cabin_id, status FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            String cabinId = null;

            try (PreparedStatement ps = conn.prepareStatement(getRes)) {
                ps.setString(1, reservationId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Reservation not found.");
                    return;
                }

                String status = rs.getString("status");
                cabinId = rs.getString("cabin_id");

                if (!status.equalsIgnoreCase("Checked-In")) {
                    System.out.println("❌ Must be checked-in first.");
                    return;
                }
            }

            String updateRes = "UPDATE reservations SET status = 'Checked-Out' WHERE reservation_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRes)) {
                ps.setString(1, reservationId);
                ps.executeUpdate();
            }

            if (cabinId != null) {
                String freeCabin = "UPDATE cabins SET is_available = 1 WHERE cabin_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(freeCabin)) {
                    ps.setString(1, cabinId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("✅ Check-out successful!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // CANCEL
    // =========================
    public void cancelReservation(String reservationId) {
        String getRes = "SELECT cabin_id, status FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            String cabinId = null;

            try (PreparedStatement ps = conn.prepareStatement(getRes)) {
                ps.setString(1, reservationId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Reservation not found.");
                    return;
                }

                String status = rs.getString("status");
                cabinId = rs.getString("cabin_id");

                if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Checked-Out")) {
                    System.out.println("❌ Cannot cancel.");
                    return;
                }
            }

            String update = "UPDATE reservations SET status = 'Cancelled' WHERE reservation_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setString(1, reservationId);
                ps.executeUpdate();
            }

            if (cabinId != null) {
                String freeCabin = "UPDATE cabins SET is_available = 1 WHERE cabin_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(freeCabin)) {
                    ps.setString(1, cabinId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("✅ Reservation cancelled!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // MOVE / MODIFY
    // =========================
    public void moveReservation(String reservationId, String cabinId, String checkIn, String checkOut) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // get current cabin
            String getRes = "SELECT cabin_id FROM reservations WHERE reservation_id=?";
            String currentCabin = null;

            try (PreparedStatement ps = conn.prepareStatement(getRes)) {
                ps.setString(1, reservationId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Reservation not found.");
                    return;
                }
                currentCabin = rs.getString("cabin_id");
            }

            // ===== VALIDATE DATES =====
            if (checkIn != null && checkOut != null) {
                try {
                    LocalDate in = LocalDate.parse(checkIn);
                    LocalDate out = LocalDate.parse(checkOut);

                    if (!out.isAfter(in)) {
                        System.out.println("❌ Check-out must be after check-in");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Invalid date format!");
                    return;
                }
            }

            // ===== CHECK AVAILABILITY EVEN IF CABIN NOT CHANGED =====
            if (checkIn != null && checkOut != null) {

                String targetCabin = (cabinId != null) ? cabinId : currentCabin;

                if (!isCabinAvailable(targetCabin, checkIn, checkOut)) {
                    System.out.println("❌ Cabin not available for selected dates.");
                    conn.rollback();
                    return;
                }
            }

            // ===== UPDATE =====
            StringBuilder sql = new StringBuilder("UPDATE reservations SET ");
            boolean hasUpdate = false;

            if (cabinId != null) {
                sql.append("cabin_id=?");
                hasUpdate = true;
            }

            if (checkIn != null && checkOut != null) {
                if (hasUpdate) sql.append(", ");
                sql.append("check_in_date=?, check_out_date=?");
            }

            sql.append(" WHERE reservation_id=?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int i = 1;

                if (cabinId != null) ps.setString(i++, cabinId);
                if (checkIn != null && checkOut != null) {
                    ps.setString(i++, checkIn);
                    ps.setString(i++, checkOut);
                }

                ps.setString(i, reservationId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Reservation updated!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // =========================
    // SHOW TABLES
    // =========================
    public void showAllReservations(String viewChoice) {
        String sql;

        if (viewChoice.equals("1")) {
            // ACTIVE
            sql = """
        SELECT r.reservation_id, r.guest_id,
        g.first_name || ' ' || g.last_name AS guest_name,
        r.cabin_id, r.pax_count, r.check_in_date,
        r.check_out_date, r.status
        FROM reservations r
        JOIN guests g ON r.guest_id = g.guest_id
        WHERE r.status IN ('Pending', 'Checked-In')
        """;
        } else {
            // HISTORY
            sql = """
        SELECT r.reservation_id, r.guest_id,
        g.first_name || ' ' || g.last_name AS guest_name,
        r.cabin_id, r.pax_count, r.check_in_date,
        r.check_out_date, r.status
        FROM reservations r
        JOIN guests g ON r.guest_id = g.guest_id
        WHERE r.status IN ('Checked-Out', 'Cancelled')
        """;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.printf("%-12s %-10s %-18s %-10s %-5d %-12s %-12s %-12s%n",
                        rs.getString("reservation_id"),
                        rs.getString("guest_id"),
                        rs.getString("guest_name"),
                        rs.getString("cabin_id"),
                        rs.getInt("pax_count"),
                        rs.getString("check_in_date"),
                        rs.getString("check_out_date"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAllCabins() {
        String sql = "SELECT * FROM cabins";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== CABINS ===");
            while (rs.next()) {
                System.out.println(rs.getString("cabin_id") + " | "
                        + rs.getString("category") + " | "
                        + (rs.getInt("is_available") == 1 ? "Available" : "Occupied"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // HELPERS
    // =========================
    private String getReservationStatus(Connection conn, String reservationId) throws SQLException {
        String sql = "SELECT status FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("status");
        }
        return null;
    }

    public boolean guestExists(String guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guestId);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }
    public String getReservationStatus(String reservationId) {
        String sql = "SELECT status FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public int addGuest(Guest guest) {
        String sql = "INSERT INTO guests (first_name, last_name) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // return auto ID
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public boolean isCabinExists(String cabinId) {
        String sql = "SELECT * FROM cabins WHERE cabin_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public int getCabinMaxPax(String cabinId) {
        String sql = "SELECT max_pax FROM cabins WHERE cabin_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_pax");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    public void showAvailableCabins(String checkIn, String checkOut) {
        String sql = """
        SELECT c.cabin_id, c.category
        FROM cabins c
        WHERE NOT EXISTS (
            SELECT 1 FROM reservations r
            WHERE r.cabin_id = c.cabin_id
            AND r.status != 'Cancelled'
            AND (? < r.check_out_date)
            AND (? > r.check_in_date)
        )
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, checkIn);
            ps.setString(2, checkOut);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== AVAILABLE CABINS ===");

            while (rs.next()) {
                System.out.println(
                        rs.getString("cabin_id") + " | " +
                                rs.getString("category") + " | Available"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isCabinAvailable(String cabinId, String checkIn, String checkOut) {
        String sql = """
        SELECT 1 FROM cabins c
        WHERE c.cabin_id = ?
        AND NOT EXISTS (
            SELECT 1 FROM reservations r
            WHERE r.cabin_id = c.cabin_id
            AND r.status != 'Cancelled'
            AND (? < r.check_out_date)
            AND (? > r.check_in_date)
        )
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ps.setString(2, checkIn);
            ps.setString(3, checkOut);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public int getMaxPax(String cabinId) {
        String sql = "SELECT max_pax FROM cabins WHERE cabin_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_pax");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public String[] getReservationDates(String reservationId) {
        String sql = "SELECT check_in_date, check_out_date FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[]{
                        rs.getString("check_in_date"),
                        rs.getString("check_out_date")
                };
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void showAllGuests() {
        String sql = "SELECT guest_id, first_name, last_name FROM guests";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            // 🔥 HEADER
            System.out.printf("%-10s %-15s %-15s%n",
                    "Guest ID", "First Name", "Last Name");
            System.out.println("----------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10s %-15s %-15s%n",
                        rs.getString("guest_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}