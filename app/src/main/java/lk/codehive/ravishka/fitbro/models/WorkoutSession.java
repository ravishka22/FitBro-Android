package lk.codehive.ravishka.fitbro.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sessions")
public class WorkoutSession {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String planName;
    private String dayTitle;
    private long timestamp;
    private int caloriesBurnt;
    private long durationMillis;

    public WorkoutSession(String planName, String dayTitle, long timestamp, int caloriesBurnt, long durationMillis) {
        this.planName = planName;
        this.dayTitle = dayTitle;
        this.timestamp = timestamp;
        this.caloriesBurnt = caloriesBurnt;
        this.durationMillis = durationMillis;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlanName() { return planName; }
    public String getDayTitle() { return dayTitle; }
    public long getTimestamp() { return timestamp; }
    public int getCaloriesBurnt() { return caloriesBurnt; }
    public long getDurationMillis() { return durationMillis; }
}
