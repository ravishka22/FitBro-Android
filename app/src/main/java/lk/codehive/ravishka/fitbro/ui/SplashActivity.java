package lk.codehive.ravishka.fitbro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.codehive.ravishka.fitbro.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Session Check - do this before inflating any UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRoleAndNavigate(currentUser.getUid());
            return;
        }

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnUserPortal.setOnClickListener(v -> navigateToLogin("member"));
        binding.btnTrainerPortal.setOnClickListener(v -> navigateToLogin("trainer"));
        binding.btnAdminPortal.setOnClickListener(v -> navigateToLogin("admin"));
    }

    private void navigateToLogin(String role) {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }

    private void checkUserRoleAndNavigate(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role == null || role.equalsIgnoreCase("user")) role = "member";
                        
                        Log.d(TAG, "User logged in with role: " + role);
                        
                        Intent intent;
                        if ("trainer".equalsIgnoreCase(role)) {
                            intent = new Intent(SplashActivity.this, TrainerActivity.class);
                        } else if ("admin".equalsIgnoreCase(role)) {
                            intent = new Intent(SplashActivity.this, AdminActivity.class);
                        } else {
                            // "member" role
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "No user document found for UID: " + uid);
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user role", e);
                    mAuth.signOut();
                    Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
                    binding = ActivitySplashBinding.inflate(getLayoutInflater());
                    setContentView(binding.getRoot());
                    setupListeners();
                });
    }
}
