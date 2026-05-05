public class Cabin {
    private String cabinId;
    private String category;

    public Cabin(String cabinId, String category) {
        this.cabinId = cabinId;
        this.category = category;
    }

    public String getCabinId() {
        return cabinId;
    }

    public String getCategory() {
        return category;
    }
}