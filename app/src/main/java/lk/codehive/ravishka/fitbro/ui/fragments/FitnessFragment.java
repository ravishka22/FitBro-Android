package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;

import lk.codehive.ravishka.fitbro.databinding.FragmentFitnessBinding;
import lk.codehive.ravishka.fitbro.ui.adapters.WorkoutSessionAdapter;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class FitnessFragment extends Fragment {

    private FragmentFitnessBinding binding;
    private WorkoutViewModel viewModel;
    private WorkoutSessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFitnessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new WorkoutSessionAdapter(new ArrayList<>());
        binding.rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWorkoutHistory.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Observe all workout history
        viewModel.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (sessions != null) {
                adapter.setSessions(sessions);
                
                // Update weekly goal (e.g., 5 workouts per week)
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                long startOfWeek = calendar.getTimeInMillis();
                
                int weeklyCount = 0;
                for (var session : sessions) {
                    if (session.getTimestamp() >= startOfWeek) {
                        weeklyCount++;
                    }
                }
                
                binding.pbWeeklyGoal.setProgress((weeklyCount * 100) / 5);
                binding.tvGoalText.setText(weeklyCount + " / 5 Workouts Completed");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}