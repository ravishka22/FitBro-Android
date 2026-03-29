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

import lk.codehive.ravishka.fitbro.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedRole = getIntent().getStringExtra("ROLE");
        if (selectedRole == null) selectedRole = "member";

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateUIBasedOnRole();
        setupListeners();
    }

    private void updateUIBasedOnRole() {
        if ("trainer".equals(selectedRole)) {
            binding.tvSubtitle.setText("Trainer Portal Login");
            binding.tvRegisterPrompt.setVisibility(View.VISIBLE);
        } else if ("admin".equals(selectedRole)) {
            binding.tvSubtitle.setText("Admin Portal Login");
            // Admins cannot register from the frontend
            binding.tvRegisterPrompt.setVisibility(View.GONE);
        } else {
            binding.tvSubtitle.setText("Member Portal Login");
            binding.tvRegisterPrompt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Existing session found for UID: " + currentUser.getUid());
            checkUserRoleAndNavigate(currentUser.getUid());
        }
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                Log.d(TAG, "Attempting login for: " + email);
                binding.btnLogin.setEnabled(false);
                signIn(email, password);
            }
        });

        binding.tvRegisterPrompt.setOnClickListener(v -> {
            if (!"admin".equals(selectedRole)) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("ROLE", selectedRole);
                startActivity(intent);
            }
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FirebaseAuth: Sign-in successful");
                        checkUserRoleAndNavigate(mAuth.getCurrentUser().getUid());
                    } else {
                        Log.e(TAG, "FirebaseAuth: Sign-in failed", task.getException());
                        binding.btnLogin.setEnabled(true);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown authentication error";
                        Toast.makeText(LoginActivity.this, "Auth failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "FirebaseAuth: Global failure", e);
                    binding.btnLogin.setEnabled(true);
                });
    }

    private void checkUserRoleAndNavigate(String uid) {
        Log.d(TAG, "Firestore: Fetching user role for UID: " + uid);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Log.d(TAG, "Firestore: User role found: " + role);
                        
                        if (role == null || role.equalsIgnoreCase("user")) role = "member";
                        
                        Intent intent;
                        if ("trainer".equalsIgnoreCase(role)) {
                            intent = new Intent(LoginActivity.this, TrainerActivity.class);
                        } else if ("admin".equalsIgnoreCase(role)) {
                            intent = new Intent(LoginActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "Firestore: User document does not exist for UID: " + uid);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore: Failed to fetch user role", e);
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email required");
            return false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password required");
            return false;
        }
        return true;
    }
}
