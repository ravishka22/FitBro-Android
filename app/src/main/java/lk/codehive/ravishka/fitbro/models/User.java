package lk.codehive.ravishka.fitbro.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class User {
    private String id;
    private String fullName;
    private String email;
    private String role; // Standardized: "member", "trainer", "admin"
    private boolean pro;
    private boolean deactivated;
    private String weight;
    private String height;
    private String targetWeight;
    private String age;
    private String photoUrl;
    private String fcmToken;
    private long membershipStartDate;
    private long membershipEndDate;
    private long createdAt;

    public User() {} // Required for Firestore

    public User(String id, String fullName, String email, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = (role == null || role.equalsIgnoreCase("user")) ? "member" : role.toLowerCase();
        this.pro = false;
        this.deactivated = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { 
        this.role = (role == null || role.equalsIgnoreCase("user")) ? "member" : role.toLowerCase(); 
    }

    @PropertyName("isPro")
    public boolean isPro() { return pro; }
    @PropertyName("isPro")
    public void setPro(boolean pro) { this.pro = pro; }

    public boolean isDeactivated() { return deactivated; }
    public void setDeactivated(boolean deactivated) { this.deactivated = deactivated; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public String getTargetWeight() { return targetWeight; }
    public void setTargetWeight(String targetWeight) { this.targetWeight = targetWeight; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public long getMembershipStartDate() { return membershipStartDate; }
    public void setMembershipStartDate(long membershipStartDate) { this.membershipStartDate = membershipStartDate; }

    public long getMembershipEndDate() { return membershipEndDate; }
    public void setMembershipEndDate(long membershipEndDate) { this.membershipEndDate = membershipEndDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
