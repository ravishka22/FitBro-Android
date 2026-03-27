package lk.codehive.ravishka.fitbro.models;

import java.util.List;

public class WorkoutDay {
    private String dayTitle; // e.g., "Day 1: Chest and Shoulders"
    private List<Exercise> exercises;

    public WorkoutDay(String dayTitle, List<Exercise> exercises) {
        this.dayTitle = dayTitle;
        this.exercises = exercises;
    }

    public String getDayTitle() { return dayTitle; }
    public List<Exercise> getExercises() { return exercises; }
}