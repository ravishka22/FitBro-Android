package lk.codehive.ravishka.fitbro.models;

import java.util.List;

public class PlanRequest {
    private String id;
    private String userId;
    private String userName;
    private String userPhotoUrl;
    private String trainerId; // Explicitly target a trainer
    private String goal;
    private String medicalConditions;
    private int preferredDays;
    private String status; // "pending", "completed"
    private List<String> photoUrls;
    private long timestamp;

    public PlanRequest() {} // Required for Firestore

    public PlanRequest(String id, String userId, String userName, String userPhotoUrl, String trainerId, String goal, String medicalConditions, int preferredDays, String status, List<String> photoUrls, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.trainerId = trainerId;
        this.goal = goal;
        this.medicalConditions = medicalConditions;
        this.preferredDays = preferredDays;
        this.status = status;
        this.photoUrls = photoUrls;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhotoUrl() { return userPhotoUrl; }
    public void setUserPhotoUrl(String userPhotoUrl) { this.userPhotoUrl = userPhotoUrl; }

    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

    public int getPreferredDays() { return preferredDays; }
    public void setPreferredDays(int preferredDays) { this.preferredDays = preferredDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}