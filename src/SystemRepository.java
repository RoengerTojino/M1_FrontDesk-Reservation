import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SystemRepository {

    private static final String DB_URL = "jdbc:sqlite:Cruise_System.db";
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    public int createReservation(int guestId, String cabinId, int pax, int sailingId) {

        int reservationId = 0; // 🔥 ADD THIS

        if (pax <= 0) {
            System.out.println("❌ Invalid pax count.");
            return 0;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // ================= CHECK CABIN =================
            String checkCabin = """
            SELECT max_pax
            FROM cabins c
            WHERE c.cabin_id = ?
            AND NOT EXISTS (
                SELECT 1 FROM reservations r
                WHERE r.cabin_id = c.cabin_id
                AND r.sailing_id = ?
                AND r.status != 'Cancelled'
            )
        """;

            int maxPax = 0;

            try (PreparedStatement ps = conn.prepareStatement(checkCabin)) {
                ps.setString(1, cabinId);
                ps.setInt(2, sailingId);

                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Cabin not available for this sailing.");
                    conn.rollback();
                    return 0;
                }

                maxPax = rs.getInt("max_pax");
            }

            // ================= VALIDATE PAX =================
            if (pax > maxPax) {
                System.out.println("❌ Too many guests! Max allowed: " + maxPax);
                conn.rollback();
                return 0;
            }

            // ================= INSERT =================
            String insertReservation = """
            INSERT INTO reservations (guest_id, cabin_id, pax_count, sailing_id, status)
            VALUES (?, ?, ?, ?, ?)
        """;

            try (PreparedStatement ps = conn.prepareStatement(insertReservation, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, guestId);
                ps.setString(2, cabinId);
                ps.setInt(3, pax);
                ps.setInt(4, sailingId);
                ps.setString(5, "Pending");

                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    reservationId = keys.getInt(1); // 🔥 STORE IT
                    System.out.println("✅ Reservation Created! ID: RES-" + String.format("%03d", reservationId));
                }
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reservationId; // 🔥 RETURN IT
    }

    public void checkIn(String reservationId) {
        String cabinId = null; // ✅ ADD THIS

        String sql = "SELECT cabin_id, status FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Reservation not found.");
                return;
            }
            cabinId = rs.getString("cabin_id"); // ✅ ADD THIS
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

            // 🔑 ISSUE KEYCARD
            KeyCard card = new KeyCard(
                    "KC-" + reservationId,
                    reservationId,
                    cabinId
            );

            System.out.println("🔑 KeyCard Issued: KC-" + reservationId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public void cancelReservationWithRefund(String reservationId) {

        // get payment info
        double paidAmount = getTotalPaid(reservationId);

        if (paidAmount <= 0) {
            System.out.println("❌ No payment found. No refund.");
            cancelReservation(reservationId);
            return;
        }

        // example: full refund (you can change logic)
        double refundAmount = paidAmount;

        // generate refund reference
        String refundRef = "RF-" + String.format("%06d", Integer.parseInt(reservationId.replace("RES-", "")));

        // update reservation
        cancelReservation(reservationId);

        // save refund
        saveRefund(reservationId, refundAmount, refundRef);

        // display
        System.out.println("💸 REFUND PROCESSED");
        System.out.println("Reservation : " + reservationId);
        System.out.println("Refund Amt  : ₱" + refundAmount);
        System.out.println("Refund Ref  : " + refundRef);
    }

    public void moveReservation(String reservationId, String cabinId, int sailingId) {
        String sql = """
    UPDATE reservations
    SET cabin_id = ?, sailing_id = ?
    WHERE reservation_id = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ps.setInt(2, sailingId);
            ps.setString(3, reservationId);

            ps.executeUpdate();

            System.out.println("✅ Reservation moved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAllReservations(String viewChoice) {
        String sql;

        if (viewChoice.equals("1")) {
            sql = """
        SELECT 
            r.reservation_id,
            r.guest_id,
            g.first_name || ' ' || g.last_name AS guest_name,
            r.cabin_id,
            r.pax_count,
            s.destination,
            s.embarkation_date,
            s.disembarkation_date,
            r.status
        FROM reservations r
        JOIN guests g ON r.guest_id = g.guest_id
        JOIN sailings s ON r.sailing_id = s.sailing_id
        WHERE r.status IN ('Pending', 'Checked-In')
        """;
        } else {
            sql = """
        SELECT 
            r.reservation_id,
            r.guest_id,
            g.first_name || ' ' || g.last_name AS guest_name,
            r.cabin_id,
            r.pax_count,
            s.destination,
            s.embarkation_date,
            s.disembarkation_date,
            r.status
        FROM reservations r
        JOIN guests g ON r.guest_id = g.guest_id
        JOIN sailings s ON r.sailing_id = s.sailing_id
        WHERE r.status IN ('Checked-Out', 'Cancelled')
        """;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            // ✅ HEADER
            System.out.printf("%-10s %-10s %-18s %-10s %-5s %-15s %-12s %-12s %-12s%n",
                    "Res ID","Guest ID","Guest Name","Cabin","Pax",
                    "Destination","Embark","Disembark","Status");
            System.out.println("-------------------------------------------------------------------------------------------------------");

            // ✅ DATA
            while (rs.next()) {
                System.out.printf("%-10d %-10d %-18s %-10s %-5d %-15s %-12s %-12s %-12s%n",
                        rs.getInt("reservation_id"),
                        rs.getInt("guest_id"),
                        rs.getString("guest_name"),
                        rs.getString("cabin_id"),
                        rs.getInt("pax_count"),
                        rs.getString("destination"),
                        rs.getString("embarkation_date"),
                        rs.getString("disembarkation_date"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public void showAllSailings() {
        String sql = "SELECT sailing_id, destination, embarkation_date, disembarkation_date FROM sailings";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== SAILINGS ===");
            System.out.printf("%-5s %-20s %-15s %-15s%n",
                    "ID", "Destination", "Embark", "Disembark");
            System.out.println("----------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-20s %-15s %-15s%n",
                        rs.getInt("sailing_id"),
                        rs.getString("destination"),
                        rs.getString("embarkation_date"),
                        rs.getString("disembarkation_date")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAvailableCabinsBySailing(int sailingId) {
        String sql = """
        SELECT c.cabin_id, c.category
        FROM cabins c
        WHERE NOT EXISTS (
            SELECT 1 FROM reservations r
            WHERE r.cabin_id = c.cabin_id
            AND r.sailing_id = ?
            AND r.status != 'Cancelled'
        )
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sailingId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== AVAILABLE CABINS ===");
            while (rs.next()) {
                System.out.println(rs.getString("cabin_id") + " | "
                        + rs.getString("category") + " | Available");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSailingExists(int sailingId) {
        String sql = "SELECT 1 FROM sailings WHERE sailing_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sailingId);
            return ps.executeQuery().next();

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCabinAvailableForSailing(String cabinId, int sailingId) {
        String sql = """
        SELECT 1 FROM cabins c
        WHERE c.cabin_id = ?
        AND NOT EXISTS (
            SELECT 1 FROM reservations r
            WHERE r.cabin_id = c.cabin_id
            AND r.sailing_id = ?
            AND r.status != 'Cancelled'
        )
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ps.setInt(2, sailingId);

            return ps.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void savePayment(int reservationId, double amount,
                            double discount, double vat,
                            double total, String reference) {

        String sql = "INSERT INTO payments (reservation_id, amount, discount, vat, total, reference) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            ps.setDouble(2, amount);
            ps.setDouble(3, discount);
            ps.setDouble(4, vat);
            ps.setDouble(5, total);
            ps.setString(6, reference);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getCabinType(String cabinId) {
        String sql = "SELECT category FROM cabins WHERE cabin_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cabinId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("category");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "STANDARD";
    }
    public String getDestinationBySailing(int sailingId) {
        String sql = "SELECT destination FROM sailings WHERE sailing_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sailingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("destination");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public void saveRefund(String reservationId, double amount, String ref) {
        String sql = "INSERT INTO refunds(reservation_id, amount, reference_no) VALUES(?,?,?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ps.setDouble(2, amount);
            ps.setString(3, ref);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getTotalPaid(String reservationId) {
        String sql = "SELECT total FROM payments WHERE reservation_id = ?";
        double total = 0;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int id = Integer.parseInt(reservationId.replace("RES-", ""));
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    public void cancelReservation(String reservationId) {
        String sql = "UPDATE reservations SET status = 'Cancelled' WHERE reservation_id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDestinationByReservation(String reservationId) {
        String sql = """
        SELECT s.destination
        FROM reservations r
        JOIN sailings s ON r.sailing_id = s.sailing_id
        WHERE r.reservation_id = ?
    """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("destination");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getCabinTypeByReservation(String reservationId) {
        String sql = """
        SELECT c.category
        FROM reservations r
        JOIN cabins c ON r.cabin_id = c.cabin_id
        WHERE r.reservation_id = ?
    """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("category");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean showAvailableCabinsByTypeAndSailing(int sailingId, String type) {
        String sql = """
        SELECT c.cabin_id, c.category
        FROM cabins c
        WHERE c.category = ?
        AND NOT EXISTS (
            SELECT 1 FROM reservations r
            WHERE r.cabin_id = c.cabin_id
            AND r.sailing_id = ?
            AND r.status != 'Cancelled'
        )
    """;

        boolean hasData = false;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setInt(2, sailingId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                hasData = true;
                System.out.println(rs.getString("cabin_id") + " | "
                        + rs.getString("category") + " | Available");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasData;
    }

    public void showSailingsByDestination(String destination) {
        String sql = """
        SELECT sailing_id, destination, embarkation_date, disembarkation_date
        FROM sailings
        WHERE destination = ?
    """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, destination);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== SAILINGS (" + destination + ") ===");
            System.out.printf("%-5s %-20s %-15s %-15s%n",
                    "ID", "Destination", "Embark", "Disembark");
            System.out.println("----------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-20s %-15s %-15s%n",
                        rs.getInt("sailing_id"),
                        rs.getString("destination"),
                        rs.getString("embarkation_date"),
                        rs.getString("disembarkation_date")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}