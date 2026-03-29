package lk.codehive.ravishka.fitbro.models;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {
    private String dayTitle; // e.g., "Day 1: Chest and Shoulders"
    private List<Exercise> exercises;

    // No-argument constructor required for Firestore deserialization
    public WorkoutDay() {
        this.exercises = new ArrayList<>();
    }

    public WorkoutDay(String dayTitle, List<Exercise> exercises) {
        this.dayTitle = dayTitle;
        this.exercises = exercises;
    }

    public String getDayTitle() { return dayTitle; }
    public void setDayTitle(String dayTitle) { this.dayTitle = dayTitle; }
    
    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
}