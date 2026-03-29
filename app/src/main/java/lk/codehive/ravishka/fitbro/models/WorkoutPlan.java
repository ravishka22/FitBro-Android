package lk.codehive.ravishka.fitbro.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

import lk.codehive.ravishka.fitbro.data.local.Converters;

@Entity(tableName = "workout_plans")
public class WorkoutPlan {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String description;
    
    @TypeConverters(Converters.class)
    private List<WorkoutDay> days;
    
    private boolean active; 
    private int lastCompletedDayIndex;
    
    private String creatorId;
    private String creatorName; 
    private String targetUserId;
    private String requestId;
    private long createdAt;
    private long validUntil; 
    private int todayDayIndex; // Added field for Firestore mapping if present

    @Ignore
    public WorkoutPlan() {
        this.id = "";
        this.lastCompletedDayIndex = -1;
    }

    public WorkoutPlan(@NonNull String id, String title, String description, List<WorkoutDay> days, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.days = days;
        this.active = active;
        this.lastCompletedDayIndex = -1;
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<WorkoutDay> getDays() { return days; }
    public void setDays(List<WorkoutDay> days) { this.days = days; }

    @PropertyName("active") // Match common Firestore naming
    public boolean isActive() { return active; }
    
    @PropertyName("active")
    public void setActive(boolean active) { this.active = active; }

    public int getLastCompletedDayIndex() { return lastCompletedDayIndex; }
    public void setLastCompletedDayIndex(int lastCompletedDayIndex) { this.lastCompletedDayIndex = lastCompletedDayIndex; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getValidUntil() { return validUntil; }
    public void setValidUntil(long validUntil) { this.validUntil = validUntil; }

    @PropertyName("todayDayIndex")
    public int getTodayDayIndex() {
        if (days == null || days.isEmpty()) return 0;
        return (lastCompletedDayIndex + 1) % days.size();
    }

    @PropertyName("todayDayIndex")
    public void setTodayDayIndex(int todayDayIndex) {
        this.todayDayIndex = todayDayIndex;
    }
}
