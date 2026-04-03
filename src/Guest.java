public class Guest {
    private String guestId;
    private String firstName;
    private String lastName;

    public Guest(String guestId, String firstName, String lastName) {
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}