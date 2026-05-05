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
        this.RESERVATION_ID = builder.reservationId;
        this.GUEST = builder.guest;
        this.CABIN = builder.cabin;
        this.PAX_COUNT = builder.paxCount;
        this.CHECK_IN_DATE = builder.checkInDate;
        this.CHECK_OUT_DATE = builder.checkOutDate;
        this.STATUS = builder.status;
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

        private String reservationId;
        private Guest guest;
        private Cabin cabin;
        private int paxCount;
        private String checkInDate;
        private String checkOutDate;
        private String status = "Pending"; // default

        public ReservationBuilder reservationId(String val) {
            this.reservationId = val;
            return this;
        }

        public ReservationBuilder guest(Guest val) {
            this.guest = val;
            return this;
        }

        public ReservationBuilder cabin(Cabin val) {
            this.cabin = val;
            return this;
        }

        public ReservationBuilder paxCount(int val) {
            this.paxCount = val;
            return this;
        }

        public ReservationBuilder checkInDate(String val) {
            this.checkInDate = val;
            return this;
        }

        public ReservationBuilder checkOutDate(String val) {
            this.checkOutDate = val;
            return this;
        }

        public ReservationBuilder status(String val) {
            this.status = val;
            return this;
        }

        public Reservation build() {
            // ✅ Basic validation
            if (guest == null || cabin == null || checkInDate == null || checkOutDate == null) {
                throw new IllegalStateException("Missing required fields!");
            }

            if (paxCount <= 0) {
                throw new IllegalStateException("Pax count must be greater than 0!");
            }

            return new Reservation(this);
        }
    }
}
