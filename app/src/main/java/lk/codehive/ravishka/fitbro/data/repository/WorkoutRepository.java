package lk.codehive.ravishka.fitbro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import lk.codehive.ravishka.fitbro.data.local.AppDatabase;
import lk.codehive.ravishka.fitbro.data.local.WorkoutPlanDao;
import lk.codehive.ravishka.fitbro.data.local.WorkoutSessionDao;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;

public class WorkoutRepository {

    private final WorkoutPlanDao workoutPlanDao;
    private final WorkoutSessionDao workoutSessionDao;
    private final LiveData<List<WorkoutPlan>> allPlans;
    private final LiveData<WorkoutPlan> activePlan;
    private final LiveData<List<WorkoutSession>> allSessions;

    public WorkoutRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        workoutPlanDao = db.workoutPlanDao();
        workoutSessionDao = db.workoutSessionDao();
        allPlans = workoutPlanDao.getAllPlans();
        activePlan = workoutPlanDao.getActivePlan();
        allSessions = workoutSessionDao.getAllSessions();
    }

    // Plan methods
    public LiveData<List<WorkoutPlan>> getAllPlans() {
        return allPlans;
    }

    public LiveData<WorkoutPlan> getActivePlan() {
        return activePlan;
    }

    public void insert(WorkoutPlan plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.insert(plan);
        });
    }

    public void update(WorkoutPlan plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.update(plan);
        });
    }

    public void delete(WorkoutPlan plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.delete(plan);
        });
    }

    public void setActivePlan(String planId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.deactivateAllPlans();
            workoutPlanDao.setActivePlan(planId);
        });
    }

    // Session methods
    public LiveData<List<WorkoutSession>> getAllSessions() {
        return allSessions;
    }

    public void insertSession(WorkoutSession session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutSessionDao.insert(session);
        });
    }

    public LiveData<Integer> getTotalCaloriesSince(long startTime) {
        return workoutSessionDao.getTotalCaloriesSince(startTime);
    }

    public LiveData<Integer> getWorkoutCountSince(long startTime) {
        return workoutSessionDao.getWorkoutCountSince(startTime);
    }
}
