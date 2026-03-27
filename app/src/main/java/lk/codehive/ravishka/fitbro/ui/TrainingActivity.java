package lk.codehive.ravishka.fitbro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.ActivityTrainingBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainingAdapter;

public class TrainingActivity extends AppCompatActivity {

    private ActivityTrainingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrainingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWorkout();
        setupListeners();
    }

    private void setupWorkout() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Dumbbell Incline Fly With A Twist", "Chest", 3, "8", "120 s", "https://example.com/ex1.png"));
        exercises.add(new Exercise("Barbell Inverted Row", "Back", 3, "8", "120 s", "https://example.com/ex2.png"));
        exercises.add(new Exercise("Barbell Bench Press", "Chest", 3, "12", "120 s", "https://example.com/ex3.png"));
        exercises.add(new Exercise("Push-Up", "Chest", 3, "12", "75 s", "https://example.com/ex4.png"));
        exercises.add(new Exercise("Dumbbell Lateral Raise", "Shoulders", 3, "16", "90 s", "https://example.com/ex5.png"));
        exercises.add(new Exercise("Dumbbell Incline Curl", "Biceps", 3, "12", "90 s", "https://example.com/ex6.png"));

        binding.tvDayTitle.setText("Day 1 Upper Body Strength");
        binding.tvExerciseCount.setText(exercises.size() + " exercises");

        TrainingAdapter adapter = new TrainingAdapter(exercises);
        binding.rvTrainingExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTrainingExercises.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnSkipDay.setOnClickListener(v -> {
            Toast.makeText(this, "Skipped Day 1. Switching to Day 2...", Toast.LENGTH_SHORT).show();
            // Logic to switch days would go here
        });

        binding.btnStartWorkout.setOnClickListener(v -> {
            // Open the active workout timer screen
            Intent intent = new Intent(TrainingActivity.this, ActiveWorkoutActivity.class);
            startActivity(intent);
        });
    }
}