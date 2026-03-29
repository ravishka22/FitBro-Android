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
import lk.codehive.ravishka.fitbro.databinding.ActivityAdminBinding;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminStatsFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminUsersFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminGymsFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminPlansFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.ProfileFragment;
import lk.codehive.ravishka.fitbro.utils.NotificationSyncManager;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private static final String TAG = "AdminActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestNotificationPermission();
        syncFcmToken();
        
        // Start real-time notification sync for the admin
        NotificationSyncManager.getInstance().startListening(this);

        if (savedInstanceState == null) {
            replaceFragment(new AdminStatsFragment());
        }

        binding.adminBottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_nav_home) {
                replaceFragment(new AdminStatsFragment());
                return true;
            } else if (id == R.id.admin_nav_users) {
                replaceFragment(new AdminUsersFragment());
                return true;
            } else if (id == R.id.admin_nav_gyms) {
                replaceFragment(new AdminGymsFragment());
                return true;
            } else if (id == R.id.admin_nav_membership) {
                replaceFragment(new AdminPlansFragment());
                return true;
            } else if (id == R.id.admin_nav_profile) {
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
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Token synced for admin"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync token", e));
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.admin_nav_host_fragment, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationSyncManager.getInstance().stopListening();
    }
}
