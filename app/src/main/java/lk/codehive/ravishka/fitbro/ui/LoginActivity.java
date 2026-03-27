package lk.codehive.ravishka.fitbro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.codehive.ravishka.fitbro.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (validateInput(email, password)) {
                // Navigate to Main Activity (the central hub)
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Prevent back button to login
            }
        });

        binding.tvRegisterPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            return false;
        }
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        return true;
    }
}