package lk.codehive.ravishka.fitbro.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityTrainerBinding;
import lk.codehive.ravishka.fitbro.ui.fragments.ProfileFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerExercisesFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerGymsFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerHomeFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerPlansFragment;
import lk.codehive.ravishka.fitbro.utils.NotificationSyncManager;

public class TrainerActivity extends AppCompatActivity {

    private ActivityTrainerBinding binding;
    private static final String TAG = "TrainerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestNotificationPermission();
        syncFcmToken();
        
        // Start real-time notification sync for the logged-in coach
        NotificationSyncManager.getInstance().startListening(this);

        // Default fragment
        if (savedInstanceState == null) {
            replaceFragment(new TrainerHomeFragment());
        }

        binding.trainerBottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.trainer_nav_home) {
                replaceFragment(new TrainerHomeFragment());
                return true;
            } else if (id == R.id.trainer_nav_plans) {
                replaceFragment(new TrainerPlansFragment());
                return true;
            } else if (id == R.id.trainer_nav_gyms) {
                replaceFragment(new TrainerGymsFragment());
                return true;
            } else if (id == R.id.trainer_nav_exercises) {
                replaceFragment(new TrainerExercisesFragment());
                return true;
            } else if (id == R.id.trainer_nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void syncFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("fcmToken", token);
                FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update(data)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Token synced for trainer"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync token", e));
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.trainer_nav_host_fragment, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationSyncManager.getInstance().stopListening();
    }
}
