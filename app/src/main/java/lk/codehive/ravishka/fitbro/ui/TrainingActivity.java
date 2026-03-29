package lk.codehive.ravishka.fitbro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityTrainingBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutDay;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainingAdapter;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class TrainingActivity extends AppCompatActivity {

    private ActivityTrainingBinding binding;
    private WorkoutViewModel viewModel;
    private WorkoutPlan activePlan;
    private int currentDayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrainingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        setupToolbar();
        setupObservers();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupObservers() {
        viewModel.getActivePlan().observe(this, plan -> {
            if (plan != null) {
                this.activePlan = plan;
                this.currentDayIndex = plan.getTodayDayIndex();
                updateUIWithPlan(plan);
                displayDay(currentDayIndex);
            } else {
                Toast.makeText(this, "No active plan found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUIWithPlan(WorkoutPlan plan) {
        binding.collapsingToolbar.setTitle(plan.getTitle());
        // For a real app, you might have a header image for the plan
        // Glide.with(this).load(plan.getImageUrl()).into(binding.ivDayHeader);
    }

    private void displayDay(int dayIndex) {
        if (activePlan == null || activePlan.getDays() == null || dayIndex >= activePlan.getDays().size()) return;

        WorkoutDay day = activePlan.getDays().get(dayIndex);
        binding.collapsingToolbar.setTitle(day.getDayTitle());
        
        List<Exercise> exercises = day.getExercises();
        if (exercises == null) exercises = new ArrayList<>();
        
        binding.tvExerciseCount.setText(exercises.size() + " Ex");
        
        // Mocking duration based on exercise count
        binding.tvDuration.setText((exercises.size() * 8) + "m");
        
        // Mocking difficulty from plan metadata if available, otherwise "Int."
        binding.tvDifficulty.setText("Int.");

        TrainingAdapter adapter = new TrainingAdapter(exercises);
        binding.rvTrainingExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTrainingExercises.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSkipDay.setOnClickListener(v -> {
            if (activePlan != null && activePlan.getDays() != null) {
                currentDayIndex = (currentDayIndex + 1) % activePlan.getDays().size();
                displayDay(currentDayIndex);
                Toast.makeText(this, "Switched to " + activePlan.getDays().get(currentDayIndex).getDayTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnStartWorkout.setOnClickListener(v -> {
            if (activePlan != null && activePlan.getDays() != null) {
                WorkoutDay day = activePlan.getDays().get(currentDayIndex);
                Intent intent = new Intent(TrainingActivity.this, ActiveWorkoutActivity.class);
                intent.putExtra("PLAN_NAME", activePlan.getTitle());
                intent.putExtra("DAY_TITLE", day.getDayTitle());
                // We will load the exercises from the active plan in the next activity
                startActivity(intent);
            }
        });
    }
}