public class Cabin {
    private String CABIN_ID;
    private String CATEGORY;

    public Cabin(String cabinId, String category) {
        this.CABIN_ID = cabinId;
        this.CATEGORY = category;
    }

    public String getCabinId() {
        return CABIN_ID;
    }

    public String getCategory() {
        return CATEGORY;
    }
}