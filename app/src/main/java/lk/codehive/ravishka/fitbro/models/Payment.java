package lk.codehive.ravishka.fitbro.models;

public class Payment {
    private String id;
    private String amount;
    private long timestamp;
    private String planName;

    public Payment() {} // Required for Firestore

    public Payment(String id, String amount, long timestamp, String planName) {
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
        this.planName = planName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
}