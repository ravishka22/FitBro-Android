package lk.codehive.ravishka.fitbro.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import lk.codehive.ravishka.fitbro.models.WorkoutSession;

@Dao
public interface WorkoutSessionDao {
    @Insert
    void insert(WorkoutSession session);

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    LiveData<List<WorkoutSession>> getAllSessions();

    @Query("SELECT SUM(caloriesBurnt) FROM workout_sessions WHERE timestamp >= :startTime")
    LiveData<Integer> getTotalCaloriesSince(long startTime);

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE timestamp >= :startTime")
    LiveData<Integer> getWorkoutCountSince(long startTime);
}
