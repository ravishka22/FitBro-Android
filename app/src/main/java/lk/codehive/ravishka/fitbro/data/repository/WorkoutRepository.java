package lk.codehive.ravishka.fitbro.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.codehive.ravishka.fitbro.data.local.AppDatabase;
import lk.codehive.ravishka.fitbro.data.local.WorkoutPlanDao;
import lk.codehive.ravishka.fitbro.data.local.WorkoutSessionDao;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;

public class WorkoutRepository {

    private static final String TAG = "WorkoutRepository";
    private final WorkoutPlanDao workoutPlanDao;
    private final WorkoutSessionDao workoutSessionDao;
    private final LiveData<List<WorkoutPlan>> allPlans;
    private final LiveData<WorkoutPlan> activePlan;
    private final LiveData<List<WorkoutSession>> allSessions;
    
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public WorkoutRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        workoutPlanDao = db.workoutPlanDao();
        workoutSessionDao = db.workoutSessionDao();
        allPlans = workoutPlanDao.getAllPlans();
        activePlan = workoutPlanDao.getActivePlan();
        allSessions = workoutSessionDao.getAllSessions();
        
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Pulls all plans assigned to the current member from Firestore.
     * Clears local cache first to ensure privacy and data integrity.
     */
    public void syncFromCloud() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .collection("plans")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        try {
                            // Clear local cache to ensure no stale data from other users exists
                            workoutPlanDao.deleteAllPlans();
                            
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                WorkoutPlan plan = document.toObject(WorkoutPlan.class);
                                if (plan != null) {
                                    if (plan.getId() == null || plan.getId().isEmpty()) {
                                        plan.setId(document.getId());
                                    }
                                    // Ensure it's assigned to this user
                                    plan.setTargetUserId(user.getUid());
                                    workoutPlanDao.insert(plan);
                                }
                            }
                            Log.d(TAG, "Cloud sync completed for member: " + user.getUid());
                        } catch (Exception e) {
                            Log.e(TAG, "Sync processing error", e);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error syncing from cloud", e));
    }

    // Plan methods with Firestore Sync
    public LiveData<List<WorkoutPlan>> getAllPlans() {
        return allPlans;
    }

    public LiveData<WorkoutPlan> getActivePlan() {
        return activePlan;
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
            deletePlanFromFirestore(plan.getId());
        });
    }

    /**
     * Sets a plan as active locally and synchronizes the state to Firestore.
     * Ensures only one plan is active at a time in both databases.
     */
    public void setActivePlan(String planId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Update Room Database
            workoutPlanDao.deactivateAllPlans();
            workoutPlanDao.setActivePlan(planId);
            
            // 2. Synchronize with Firestore using a Batch to ensure integrity
            firestore.collection("users").document(user.getUid())
                    .collection("plans")
                    .whereEqualTo("active", true)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        WriteBatch batch = firestore.batch();
                        // Deactivate currently active plans in the cloud
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            if (!doc.getId().equals(planId)) {
                                batch.update(doc.getReference(), "active", false);
                            }
                        }
                        // Activate the requested plan
                        batch.update(firestore.collection("users").document(user.getUid())
                                .collection("plans").document(planId), "active", true);
                        
                        batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Active plan synced to cloud"));
                    })
                    .addOnFailureListener(e -> {
                        // Fallback: just try to set the new one to true if query fails
                        Log.w(TAG, "Failed to query active plans, falling back to direct update", e);
                        firestore.collection("users").document(user.getUid())
                                .collection("plans").document(planId).update("active", true);
                    });
        });
    }

    // Firestore Sync Logic (Outgoing)
    private void syncPlanToFirestore(WorkoutPlan plan) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Store only in the user's private collection
            firestore.collection("users")
                    .document(user.getUid())
                    .collection("plans")
                    .document(plan.getId())
                    .set(plan, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Plan synced to member storage"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error syncing plan", e));
        }
    }

    private void deletePlanFromFirestore(String planId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            firestore.collection("users")
                    .document(user.getUid())
                    .collection("plans")
                    .document(planId)
                    .delete();
        }
    }

    // Session methods
    public LiveData<List<WorkoutSession>> getAllSessions() {
        return allSessions;
    }

    public void insertSession(WorkoutSession session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutSessionDao.insert(session);
            syncSessionToFirestore(session);
        });
    }

    private void syncSessionToFirestore(WorkoutSession session) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            firestore.collection("users")
                    .document(user.getUid())
                    .collection("sessions")
                    .add(session);
        }
    }

    public LiveData<Integer> getTotalCaloriesSince(long startTime) {
        return workoutSessionDao.getTotalCaloriesSince(startTime);
    }

    public LiveData<Integer> getWorkoutCountSince(long startTime) {
        return workoutSessionDao.getWorkoutCountSince(startTime);
    }
}
