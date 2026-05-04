public class ReservationMoveLog {
    private String logId;
    private String reservationId;
    private String reason;
    private String timeStamp;

    public ReservationMoveLog(String logId, String reservationId, String reason, String timeStamp) {
        this.logId = logId;
        this.reservationId = reservationId;
        this.reason = reason;
        this.timeStamp = timeStamp;
    }
}