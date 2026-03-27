package lk.codehive.ravishka.fitbro.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

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
    
    private boolean isActive;
    private int lastCompletedDayIndex;

    public WorkoutPlan(@NonNull String id, String title, String description, List<WorkoutDay> days, boolean isActive) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.days = days;
        this.isActive = isActive;
        this.lastCompletedDayIndex = -1;
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

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getLastCompletedDayIndex() { return lastCompletedDayIndex; }
    public void setLastCompletedDayIndex(int lastCompletedDayIndex) { this.lastCompletedDayIndex = lastCompletedDayIndex; }

    public int getTodayDayIndex() {
        if (days == null || days.isEmpty()) return 0;
        return (lastCompletedDayIndex + 1) % days.size();
    }
}