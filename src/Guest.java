public class Guest {
    private String GUEST_ID;
    private String FIRST_NAME;
    private String LAST_NAME;

    public Guest(String guestId, String firstName, String lastName) {
        this.GUEST_ID = guestId;
        this.FIRST_NAME = firstName;
        this.LAST_NAME = lastName;
    }

    public String getGuestId() {
        return GUEST_ID;
    }

    public String getFirstName() {
        return FIRST_NAME;
    }

    public String getLastName() {
        return LAST_NAME;
    }

    public String getFullName() {
        return FIRST_NAME + " " + LAST_NAME;
    }
}