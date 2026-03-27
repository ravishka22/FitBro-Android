package lk.codehive.ravishka.fitbro.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import lk.codehive.ravishka.fitbro.data.repository.WorkoutRepository;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;

public class WorkoutViewModel extends AndroidViewModel {

    private final WorkoutRepository repository;
    private final LiveData<List<WorkoutPlan>> allPlans;
    private final LiveData<WorkoutPlan> activePlan;
    private final LiveData<List<WorkoutSession>> allSessions;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allPlans = repository.getAllPlans();
        activePlan = repository.getActivePlan();
        allSessions = repository.getAllSessions();
    }

    public LiveData<List<WorkoutPlan>> getAllPlans() {
        return allPlans;
    }

    public LiveData<WorkoutPlan> getActivePlan() {
        return activePlan;
    }

    public void insert(WorkoutPlan plan) {
        repository.insert(plan);
    }

    public void update(WorkoutPlan plan) {
        repository.update(plan);
    }

    public void setActivePlan(String planId) {
        repository.setActivePlan(planId);
    }

    // Session methods
    public LiveData<List<WorkoutSession>> getAllSessions() {
        return allSessions;
    }

    public void insertSession(WorkoutSession session) {
        repository.insertSession(session);
    }

    public LiveData<Integer> getTotalCaloriesSince(long startTime) {
        return repository.getTotalCaloriesSince(startTime);
    }

    public LiveData<Integer> getWorkoutCountSince(long startTime) {
        return repository.getWorkoutCountSince(startTime);
    }
}
