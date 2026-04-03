public class ReservationMoveLog {
    private String logId;
    private String reservationId;
    private String reason;
    private String timestamp;

    public ReservationMoveLog(String logId, String reservationId, String reason, String timestamp) {
        this.logId = logId;
        this.reservationId = reservationId;
        this.reason = reason;
        this.timestamp = timestamp;
    }
}