public class Reservation {

    // ✅ Immutable fields (ALL CAPS as required)
    private final String RESERVATION_ID;
    private final Guest GUEST;
    private final Cabin CABIN;
    private final int PAX_COUNT;
    private final String CHECK_IN_DATE;
    private final String CHECK_OUT_DATE;
    private final String STATUS;

    // 🔒 Private constructor (Builder only)
    private Reservation(ReservationBuilder builder) {
        this.RESERVATION_ID = builder.RESERVATION_ID;
        this.GUEST = builder.GUEST;
        this.CABIN = builder.CABIN;
        this.PAX_COUNT = builder.PAX_COUNT;
        this.CHECK_IN_DATE = builder.CHECK_IN_DATE;
        this.CHECK_OUT_DATE = builder.CHECK_OUT_DATE;
        this.STATUS = builder.STATUS;
    }

    // ✅ Display method
    public void display() {
        System.out.println("\nReservation Created!");
        System.out.println("ID: " + RESERVATION_ID);
        System.out.println("Guest: " + GUEST.getFullName());
        System.out.println("Cabin: " + CABIN.getCabinId());
        System.out.println("Pax Count: " + PAX_COUNT);
        System.out.println("Check-in: " + CHECK_IN_DATE);
        System.out.println("Check-out: " + CHECK_OUT_DATE);
        System.out.println("Status: " + STATUS);
    }

    // 🔥 BUILDER CLASS
    public static class ReservationBuilder {

        private String RESERVATION_ID;
        private Guest GUEST;
        private Cabin CABIN;
        private int PAX_COUNT;
        private String CHECK_IN_DATE;
        private String CHECK_OUT_DATE;
        private String STATUS = "Pending"; // default

        public ReservationBuilder reservationId(String val) {
            this.RESERVATION_ID = val;
            return this;
        }

        public ReservationBuilder guest(Guest val) {
            this.GUEST = val;
            return this;
        }

        public ReservationBuilder cabin(Cabin val) {
            this.CABIN = val;
            return this;
        }

        public ReservationBuilder paxCount(int val) {
            this.PAX_COUNT = val;
            return this;
        }

        public ReservationBuilder checkInDate(String val) {
            this.CHECK_IN_DATE = val;
            return this;
        }

        public ReservationBuilder checkOutDate(String val) {
            this.CHECK_OUT_DATE = val;
            return this;
        }

        public ReservationBuilder status(String val) {
            this.STATUS = val;
            return this;
        }

        public Reservation build() {
            // ✅ Basic validation
            if (GUEST == null || CABIN == null || CHECK_IN_DATE == null || CHECK_OUT_DATE == null) {
                throw new IllegalStateException("Missing required fields!");
            }

            if (PAX_COUNT <= 0) {
                throw new IllegalStateException("Pax count must be greater than 0!");
            }

            return new Reservation(this);
        }
    }
}