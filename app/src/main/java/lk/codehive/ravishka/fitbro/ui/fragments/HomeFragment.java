package lk.codehive.ravishka.fitbro.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentHomeBinding;
import lk.codehive.ravishka.fitbro.ui.TrainingActivity;
import lk.codehive.ravishka.fitbro.utils.StepCounterManager;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private WorkoutViewModel viewModel;
    private StepCounterManager stepCounterManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startStepCounter();
                } else {
                    Toast.makeText(getContext(), "Step tracking permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        stepCounterManager = new StepCounterManager(requireContext());
        
        setupProfile();
        checkStepCounterPermission();
        setupObservers();
        setupListeners();
    }

    private void setupProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Priority: Firestore fullName > Auth displayName > Email prefix
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                String name = doc.getString("fullName");
                if (name == null || name.isEmpty()) name = user.getDisplayName();
                if (name == null || name.isEmpty()) name = user.getEmail().split("@")[0];
                
                binding.tvGreeting.setText("Hi, " + name + " 👋");
                
                // Set placeholder initials
                String initials = name.substring(0, 1).toUpperCase();
                binding.tvProfilePlaceholder.setText(initials);

                // Handle Image
                String photoUrl = doc.getString("photoUrl");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    binding.tvProfilePlaceholder.setVisibility(View.GONE);
                    binding.ivProfileImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.logo_dark).into(binding.ivProfileImage);
                } else {
                    binding.tvProfilePlaceholder.setVisibility(View.VISIBLE);
                    binding.ivProfileImage.setVisibility(View.GONE);
                }
            });
        }
    }

    private void checkStepCounterPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                startStepCounter();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        } else {
            startStepCounter();
        }
    }

    private void startStepCounter() {
        stepCounterManager.startListening(steps -> {
            // Goal is 10,000 steps
            int progress = (int) ((steps / 10000.0) * 100);
            binding.pbSteps.setProgress(Math.min(progress, 100), true);
        });
    }

    private void setupObservers() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();

        // Calories Progress
        viewModel.getTotalCaloriesSince(startOfToday).observe(getViewLifecycleOwner(), totalCalories -> {
            int calories = (totalCalories != null) ? totalCalories : 0;
            int progress = (int) ((calories / 1000.0) * 100);
            binding.pbCalories.setProgress(Math.min(progress, 100), true);
        });

        // Active Workouts Progress
        viewModel.getWorkoutCountSince(startOfToday).observe(getViewLifecycleOwner(), count -> {
            int workoutCount = (count != null) ? count : 0;
            int progress = (workoutCount > 0) ? 100 : 0;
            binding.pbActiveTime.setProgress(progress, true);
        });

        // Next Workout Session Observer
        viewModel.getActivePlan().observe(getViewLifecycleOwner(), plan -> {
            if (plan != null) {
                binding.tvWorkoutTitle.setText("Ready for Next?");
                binding.tvWorkoutSubtitle.setText("Today: " + plan.getDays().get(plan.getTodayDayIndex()).getDayTitle());
                binding.btnStartWorkout.setEnabled(true);
            } else {
                binding.tvWorkoutTitle.setText("No Active Plan");
                binding.tvWorkoutSubtitle.setText("Choose a plan from the list");
                binding.btnStartWorkout.setEnabled(false);
            }
        });
    }

    private void setupListeners() {
        binding.btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TrainingActivity.class);
            startActivity(intent);
        });

        binding.flProfileContainer.setOnClickListener(v -> {
            // Navigation handled by MainActivity BottomNav, or trigger profile fragment
            Toast.makeText(getContext(), "Switching to Profile...", Toast.LENGTH_SHORT).show();
        });
        
        binding.cardNearbyGym.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Locating Gyms...", Toast.LENGTH_SHORT).show();
            // In MainActivity, trigger the nav_map selection
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkStepCounterPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        stepCounterManager.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
