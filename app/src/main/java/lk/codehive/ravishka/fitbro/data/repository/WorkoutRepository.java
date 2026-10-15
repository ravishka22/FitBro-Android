package lk.codehive.ravishka.fitbro.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.codehive.ravishka.fitbro.data.local.AppDatabase;
import lk.codehive.ravishka.fitbro.data.local.WorkoutPlanDao;
import lk.codehive.ravishka.fitbro.data.local.WorkoutSessionDao;
import lk.codehive.ravishka.fitbro.data.local.WeightLogDao;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;
import lk.codehive.ravishka.fitbro.models.WeightLog;

public class WorkoutRepository {

    private static final String TAG = "WorkoutRepository";
    private final WorkoutPlanDao workoutPlanDao;
    private final WorkoutSessionDao workoutSessionDao;
    private final WeightLogDao weightLogDao;
    private final LiveData<List<WorkoutPlan>> allPlans;
    private final LiveData<WorkoutPlan> activePlan;
    private final LiveData<List<WorkoutSession>> allSessions;
    
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private ListenerRegistration plansListener;
    private ListenerRegistration sessionsListener;
    private ListenerRegistration weightListener;

    public WorkoutRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        workoutPlanDao = db.workoutPlanDao();
        workoutSessionDao = db.workoutSessionDao();
        weightLogDao = db.weightLogDao();
        allPlans = workoutPlanDao.getAllPlans();
        activePlan = workoutPlanDao.getActivePlan();
        allSessions = workoutSessionDao.getAllSessions();
        
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        syncFromCloud();
    }

    public void syncFromCloud() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // Sync Plans
        if (plansListener != null) plansListener.remove();
        plansListener = firestore.collection("users").document(user.getUid()).collection("plans")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            workoutPlanDao.deleteAllPlans();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                WorkoutPlan plan = document.toObject(WorkoutPlan.class);
                                if (plan != null) workoutPlanDao.insert(plan);
                            }
                        });
                    }
                });

        // Sync Sessions
        if (sessionsListener != null) sessionsListener.remove();
        sessionsListener = firestore.collection("users").document(user.getUid()).collection("sessions")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            workoutSessionDao.deleteAllSessions();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                WorkoutSession session = document.toObject(WorkoutSession.class);
                                if (session != null) workoutSessionDao.insert(session);
                            }
                        });
                    }
                });

        // Sync Weight Logs
        if (weightListener != null) weightListener.remove();
        weightListener = firestore.collection("users").document(user.getUid()).collection("weight_logs")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            weightLogDao.deleteAllWeightLogs();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                WeightLog log = document.toObject(WeightLog.class);
                                if (log != null) {
                                    log.setFirestoreId(document.getId());
                                    weightLogDao.insert(log);
                                }
                            }
                        });
                    }
                });
    }

    public LiveData<List<WorkoutPlan>> getAllPlans() { return allPlans; }
    public LiveData<WorkoutPlan> getActivePlan() { return activePlan; }
    public LiveData<List<WorkoutSession>> getAllSessions() { return allSessions; }
    public LiveData<List<WeightLog>> getWeightLogs() { 
        return weightLogDao.getWeightLogs(auth.getUid() != null ? auth.getUid() : ""); 
    }

    public void insertWeightLog(WeightLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                log.setUserId(user.getUid());
                firestore.collection("users").document(user.getUid())
                        .collection("weight_logs").add(log);
            }
        });
    }

    public void updateWeightLog(WeightLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && log.getFirestoreId() != null) {
                weightLogDao.insert(log); // Updates local Room
                firestore.collection("users").document(user.getUid())
                        .collection("weight_logs").document(log.getFirestoreId())
                        .set(log, SetOptions.merge());
            }
        });
    }

    public void deleteWeightLog(WeightLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && log.getFirestoreId() != null) {
                weightLogDao.delete(log);
                firestore.collection("users").document(user.getUid())
                        .collection("weight_logs").document(log.getFirestoreId())
                        .delete();
            }
        });
    }

    public void insert(WorkoutPlan plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.insert(plan);
            syncPlanToFirestore(plan);
        });
    }

    public void update(WorkoutPlan plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.update(plan);
            syncPlanToFirestore(plan);
        });
    }

    public void delete(WorkoutPlan plan) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.delete(plan);
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                firestore.collection("users").document(user.getUid())
                        .collection("plans").document(plan.getId()).delete();
            }
        });
    }

    public void setActivePlan(String planId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlanDao.deactivateAllPlans();
            workoutPlanDao.setActivePlan(planId);
            firestore.collection("users").document(user.getUid()).collection("plans")
                    .whereEqualTo("active", true).get()
                    .addOnSuccessListener(snapshots -> {
                        WriteBatch batch = firestore.batch();
                        for (DocumentSnapshot doc : snapshots) batch.update(doc.getReference(), "active", false);
                        batch.update(firestore.collection("users").document(user.getUid()).collection("plans").document(planId), "active", true);
                        batch.commit();
                    });
        });
    }

    private void syncPlanToFirestore(WorkoutPlan plan) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            firestore.collection("users")
                    .document(user.getUid())
                    .collection("plans")
                    .document(plan.getId()).set(plan, SetOptions.merge());
        }
    }

    public void insertSession(WorkoutSession session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                session.setUserId(user.getUid());
                workoutSessionDao.insert(session);
                firestore.collection("users").document(user.getUid())
                        .collection("sessions").add(session);
            }
        });
    }

    public LiveData<Integer> getTotalCaloriesSince(long startTime) { return workoutSessionDao.getTotalCaloriesSince(startTime); }
    public LiveData<Integer> getWorkoutCountSince(long startTime) { return workoutSessionDao.getWorkoutCountSince(startTime); }
}
