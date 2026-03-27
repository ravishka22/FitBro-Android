package lk.codehive.ravishka.fitbro.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import lk.codehive.ravishka.fitbro.models.WorkoutPlan;

@Dao
public interface WorkoutPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutPlan plan);

    @Update
    void update(WorkoutPlan plan);

    @Delete
    void delete(WorkoutPlan plan);

    @Query("SELECT * FROM workout_plans")
    LiveData<List<WorkoutPlan>> getAllPlans();

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    LiveData<WorkoutPlan> getActivePlan();

    @Query("UPDATE workout_plans SET isActive = 0")
    void deactivateAllPlans();

    @Query("UPDATE workout_plans SET isActive = 1 WHERE id = :planId")
    void setActivePlan(String planId);
}