public class Cabin {
    private String cabinId;
    private String category;
    private boolean isAvailable;

    public Cabin(String cabinId, String category) {
        this.cabinId = cabinId;
        this.category = category;
        this.isAvailable = true;
    }

    public String getCabinId() {
        return cabinId;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}