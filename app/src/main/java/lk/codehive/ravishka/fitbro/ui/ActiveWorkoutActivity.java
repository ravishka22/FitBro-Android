package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityActiveWorkoutBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutDay;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class ActiveWorkoutActivity extends AppCompatActivity {

    private ActivityActiveWorkoutBinding binding;
    private WorkoutViewModel viewModel;
    private List<Exercise> workoutExercises = new ArrayList<>();
    private int currentExerciseIndex = 0;
    private int currentSet = 1;
    private long startTime;
    private String planName;
    private String dayTitle;

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

        planName = getIntent().getStringExtra("PLAN_NAME");
        dayTitle = getIntent().getStringExtra("DAY_TITLE");

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getActivePlan().observe(this, plan -> {
            if (plan != null && workoutExercises.isEmpty()) {
                for (WorkoutDay day : plan.getDays()) {
                    if (day.getDayTitle().equals(dayTitle)) {
                        workoutExercises = day.getExercises();
                        updateSessionProgress();
                        displayExercise(currentExerciseIndex);
                        break;
                    }
                }
            }
        });
    }

    private void updateSessionProgress() {
        if (workoutExercises.isEmpty()) return;
        int progress = (int) (((float) currentExerciseIndex / workoutExercises.size()) * 100);
        binding.sessionProgress.setProgress(progress, true);
    }

    private void displayExercise(int index) {
        if (workoutExercises == null || workoutExercises.isEmpty() || index < 0 || index >= workoutExercises.size()) return;

        Exercise ex = workoutExercises.get(index);
        binding.tvExerciseTitle.setText(ex.getName());
        binding.tvExerciseSub.setText("Set " + currentSet + " of " + ex.getSets() + " • " + ex.getReps() + " Reps");
        binding.tvInstructions.setText("1. Prepare your equipment.\n2. Maintain correct form throughout the movement.\n3. Breathe out on exertion.\n4. Complete all " + ex.getReps() + " repetitions.");

        if (ex.getImageUrl() != null && !ex.getImageUrl().isEmpty()) {
            Glide.with(this).load(ex.getImageUrl()).placeholder(R.drawable.logo_dark).into(binding.ivExerciseLarge);
        }

        if (index == workoutExercises.size() - 1 && currentSet == ex.getSets()) {
            binding.btnDone.setText("FINISH WORKOUT");
        } else {
            binding.btnDone.setText("COMPLETE SET");
        }
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> finish());

        binding.btnNext.setOnClickListener(v -> {
            if (currentExerciseIndex < workoutExercises.size() - 1) {
                resetTimerState();
                currentExerciseIndex++;
                currentSet = 1;
                updateSessionProgress();
                displayExercise(currentExerciseIndex);
            }
        });

        binding.btnPrevious.setOnClickListener(v -> {
            if (currentExerciseIndex > 0) {
                resetTimerState();
                currentExerciseIndex--;
                currentSet = 1;
                updateSessionProgress();
                displayExercise(currentExerciseIndex);
            }
        });

        binding.btnDone.setOnClickListener(v -> {
            Exercise currentEx = workoutExercises.get(currentExerciseIndex);
            if (currentSet < currentEx.getSets()) {
                showRestTimerSheet();
            } else {
                if (currentExerciseIndex == workoutExercises.size() - 1) {
                    finishWorkout();
                } else {
                    showRestTimerSheet();
                }
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
        resetTimerState();

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
        long restSeconds = 60;
        try {
            String restStr = currentEx.getRest().replaceAll("[^0-9]", "");
            if (!restStr.isEmpty()) restSeconds = Long.parseLong(restStr);
        } catch (Exception ignored) {}
        
        startRestTimer(restSeconds * 1000, tvTimer, pbTimer);

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
            long newTime = remainingRestTime + 10000;
            startRestTimer(newTime, tvTimer, pbTimer);
            isTimerPaused = false;
            btnPause.setText("PAUSE");
        });

        btnSkip.setOnClickListener(v -> {
            resetTimerState();
            advanceWorkout();
        });

        restSheet.show();
    }

    private void startRestTimer(long duration, TextView tvTimer, android.widget.ProgressBar pbTimer) {
        if (restTimer != null) restTimer.cancel();
        remainingRestTime = duration;
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
                advanceWorkout();
            }
        }.start();
    }

    private void advanceWorkout() {
        Exercise currentEx = workoutExercises.get(currentExerciseIndex);
        if (currentSet < currentEx.getSets()) {
            currentSet++;
        } else {
            currentExerciseIndex++;
            currentSet = 1;
        }
        updateSessionProgress();
        displayExercise(currentExerciseIndex);
    }

    private void finishWorkout() {
        long duration = System.currentTimeMillis() - startTime;
        WorkoutSession session = new WorkoutSession(
                planName, dayTitle, System.currentTimeMillis(), workoutExercises.size() * 45, duration
        );
        
        // Use standard showToast helper or safety check
        if (!isFinishing()) {
            viewModel.insertSession(session);
            Toast.makeText(this, "Excellent! Workout Saved.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (restTimer != null) restTimer.cancel();
    }
}
