package lk.codehive.ravishka.fitbro.ui.fragments;

import android.Manifest;
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

import java.util.Calendar;

import lk.codehive.ravishka.fitbro.databinding.FragmentHomeBinding;
import lk.codehive.ravishka.fitbro.utils.StepCounterManager;
import lk.codehive.ravishka.fitbro.utils.StepCounterManager.StepListener;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private WorkoutViewModel viewModel;
    private StepCounterManager stepCounterManager;
    
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
        
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        stepCounterManager = new StepCounterManager(requireContext());
        
        checkStepCounterPermission();
        setupObservers();
        setupListeners();
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
            binding.pbSteps.setProgress(Math.min(progress, 100));
        });
    }

    private void setupObservers() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();

        viewModel.getTotalCaloriesSince(startOfToday).observe(getViewLifecycleOwner(), totalCalories -> {
            int calories = (totalCalories != null) ? totalCalories : 0;
            int progress = (int) ((calories / 1000.0) * 100);
            binding.pbCalories.setProgress(Math.min(progress, 100));
        });

        viewModel.getWorkoutCountSince(startOfToday).observe(getViewLifecycleOwner(), count -> {
            int workoutCount = (count != null) ? count : 0;
            int progress = (workoutCount > 0) ? 100 : 0;
            binding.pbActiveTime.setProgress(progress);
        });
    }

    private void setupListeners() {
        binding.btnStartWorkout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Starting Workout...", Toast.LENGTH_SHORT).show();
        });

        binding.ivProfileImage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Profile clicked", Toast.LENGTH_SHORT).show();
        });
        
        binding.cardNearbyGym.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening Map...", Toast.LENGTH_SHORT).show();
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
