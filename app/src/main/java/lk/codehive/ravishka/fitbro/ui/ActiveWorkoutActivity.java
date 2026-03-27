package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityActiveWorkoutBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class ActiveWorkoutActivity extends AppCompatActivity {

    private ActivityActiveWorkoutBinding binding;
    private WorkoutViewModel viewModel;
    private List<Exercise> workoutExercises;
    private int currentExerciseIndex = 0;
    private long startTime;

    // Rest Timer variables
    private BottomSheetDialog restSheet;
    private CountDownTimer restTimer;
    private long remainingRestTime;
    private boolean isTimerPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActiveWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        startTime = System.currentTimeMillis();

        loadMockExercises();
        displayExercise(currentExerciseIndex);
        setupListeners();
    }

    private void loadMockExercises() {
        workoutExercises = new ArrayList<>();
        workoutExercises.add(new Exercise("Dumbbell Incline Fly", "Chest", 3, "12", "60", ""));
        workoutExercises.add(new Exercise("Bench Press", "Chest", 3, "10", "90", ""));
        workoutExercises.add(new Exercise("Push-Ups", "Chest", 3, "15", "45", ""));
    }

    private void displayExercise(int index) {
        if (index < 0 || index >= workoutExercises.size()) return;

        Exercise ex = workoutExercises.get(index);
        binding.tvExerciseTitle.setText(ex.getName());
        binding.tvExerciseSub.setText("Exercise " + (index + 1) + " of " + workoutExercises.size() + " • " + ex.getSets() + " Sets");
        
        binding.tvInstructions.setText("1. Position yourself correctly.\n2. Perform " + ex.getReps() + " repetitions with controlled motion.\n3. Keep your core tight and breathe steadily.");

        if (index == workoutExercises.size() - 1) {
            binding.btnDone.setText("FINISH WORKOUT");
        } else {
            binding.btnDone.setText("DONE");
        }
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> finish());

        binding.btnNext.setOnClickListener(v -> {
            if (currentExerciseIndex < workoutExercises.size() - 1) {
                resetTimerState();
                currentExerciseIndex++;
                displayExercise(currentExerciseIndex);
            }
        });

        binding.btnPrevious.setOnClickListener(v -> {
            if (currentExerciseIndex > 0) {
                resetTimerState();
                currentExerciseIndex--;
                displayExercise(currentExerciseIndex);
            }
        });

        binding.btnDone.setOnClickListener(v -> {
            if (currentExerciseIndex == workoutExercises.size() - 1) {
                finishWorkout();
            } else {
                showRestTimerSheet();
            }
        });
    }

    private void resetTimerState() {
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
        }
        isTimerPaused = false;
        if (restSheet != null && restSheet.isShowing()) {
            restSheet.dismiss();
        }
    }

    private void showRestTimerSheet() {
        resetTimerState(); // Ensure a clean start for every rest period

        restSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_rest_timer_sheet, null);
        restSheet.setContentView(view);
        restSheet.setCancelable(false);

        TextView tvTimer = view.findViewById(R.id.tvRestTimerCount);
        android.widget.ProgressBar pbTimer = view.findViewById(R.id.pbRestTimer);
        MaterialButton btnPause = view.findViewById(R.id.btnPauseRest);
        MaterialButton btnExtend = view.findViewById(R.id.btnExtendRest);
        MaterialButton btnSkip = view.findViewById(R.id.btnSkipRest);

        Exercise currentEx = workoutExercises.get(currentExerciseIndex);
        long restMillis = Long.parseLong(currentEx.getRest()) * 1000;
        
        startRestTimer(restMillis, tvTimer, pbTimer);

        btnPause.setOnClickListener(v -> {
            if (isTimerPaused) {
                startRestTimer(remainingRestTime, tvTimer, pbTimer);
                btnPause.setText("PAUSE");
                isTimerPaused = false;
            } else {
                if (restTimer != null) restTimer.cancel();
                btnPause.setText("RESUME");
                isTimerPaused = true;
            }
        });

        btnExtend.setOnClickListener(v -> {
            // Extend remaining time and restart timer
            long newTime = remainingRestTime + 10000;
            startRestTimer(newTime, tvTimer, pbTimer);
            isTimerPaused = false;
            btnPause.setText("PAUSE");
        });

        btnSkip.setOnClickListener(v -> {
            resetTimerState();
            moveToNextExercise();
        });

        restSheet.show();
    }

    private void startRestTimer(long duration, TextView tvTimer, android.widget.ProgressBar pbTimer) {
        if (restTimer != null) restTimer.cancel();
        
        remainingRestTime = duration;
        // Using a fixed max for progress or update max dynamically
        pbTimer.setMax((int) duration);
        
        restTimer = new CountDownTimer(duration, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingRestTime = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format("00:%02d", seconds));
                pbTimer.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                pbTimer.setProgress(0);
                resetTimerState();
                moveToNextExercise();
            }
        }.start();
    }

    private void moveToNextExercise() {
        if (currentExerciseIndex < workoutExercises.size() - 1) {
            currentExerciseIndex++;
            displayExercise(currentExerciseIndex);
        }
    }

    private void finishWorkout() {
        long duration = System.currentTimeMillis() - startTime;
        WorkoutSession session = new WorkoutSession(
                "3-Day Split", "Upper Body", System.currentTimeMillis(), 350, duration
        );
        viewModel.insertSession(session);
        Toast.makeText(this, "Workout Completed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (restTimer != null) restTimer.cancel();
    }
}