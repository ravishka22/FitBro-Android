package lk.codehive.ravishka.fitbro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.codehive.ravishka.fitbro.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etFullName.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();

            if (validateInput(name, email, password, confirmPassword)) {
                // Mock Register Logic
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to Login
            }
        });

        binding.tvLoginPrompt.setOnClickListener(v -> {
            finish(); // Go back to Login
        });
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
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return false;
        }
        
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        return true;
    }
}