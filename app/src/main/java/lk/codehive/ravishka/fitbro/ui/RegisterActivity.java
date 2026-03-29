package lk.codehive.ravishka.fitbro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.codehive.ravishka.fitbro.databinding.ActivityRegisterBinding;
import lk.codehive.ravishka.fitbro.models.User;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedRole = getIntent().getStringExtra("ROLE");
        // Default to "member" instead of "user"
        if (selectedRole == null || selectedRole.equalsIgnoreCase("user")) selectedRole = "member";

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvTitle.setText("Join FitBro " + selectedRole.toUpperCase());
        setupListeners();
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (validateInput(name, email, password, confirmPassword)) {
                binding.btnRegister.setEnabled(false);
                createAccount(name, email, password);
            }
        });

        binding.tvLoginPrompt.setOnClickListener(v -> {
            finish();
        });
    }

    private void createAccount(String name, String email, String password) {
        Log.d(TAG, "Creating account for: " + email + " with role: " + selectedRole);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        saveUserToFirestore(user.getUid(), name, email);
                                    });
                        }
                    } else {
                        Log.e(TAG, "Account creation failed", task.getException());
                        binding.btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email) {
        // Standardized role "member" used here
        User user = new User(uid, name, email, selectedRole.toLowerCase());
        user.setPro("trainer".equalsIgnoreCase(selectedRole) || "admin".equalsIgnoreCase(selectedRole));

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore");
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    navigateToPortal(selectedRole.toLowerCase());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToPortal(String role) {
        Intent intent;
        if ("trainer".equalsIgnoreCase(role)) {
            intent = new Intent(RegisterActivity.this, TrainerActivity.class);
        } else if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(RegisterActivity.this, AdminActivity.class);
        } else {
            // Role is "member"
            intent = new Intent(RegisterActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            binding.tilFullName.setError("Name is required");
            return false;
        }
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}
