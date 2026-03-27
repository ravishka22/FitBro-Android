package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ItemWorkoutDayBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutDay;

public class WorkoutDayAdapter extends RecyclerView.Adapter<WorkoutDayAdapter.WorkoutDayViewHolder> {

    private List<WorkoutDay> workoutDays;

    public WorkoutDayAdapter(List<WorkoutDay> workoutDays) {
        this.workoutDays = workoutDays;
    }

    @NonNull
    @Override
    public WorkoutDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutDayBinding binding = ItemWorkoutDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WorkoutDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutDayViewHolder holder, int position) {
        WorkoutDay day = workoutDays.get(position);
        holder.binding.tvDayTitle.setText(day.getDayTitle());

        // Clear existing views if any (important for view recycling)
        holder.binding.llExercisesContainer.removeAllViews();

        // Add header row for clarity
        addHeaderRow(holder.binding.llExercisesContainer);

        // Dynamically add exercises
        for (Exercise exercise : day.getExercises()) {
            View exerciseView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_exercise_row, holder.binding.llExercisesContainer, false);

            TextView tvName = exerciseView.findViewById(R.id.tvExerciseName);
            TextView tvMuscle = exerciseView.findViewById(R.id.tvMuscleGroup);
            TextView tvSets = exerciseView.findViewById(R.id.tvSets);
            TextView tvReps = exerciseView.findViewById(R.id.tvReps);
            TextView tvRest = exerciseView.findViewById(R.id.tvRest);

            tvName.setText(exercise.getName());
            tvMuscle.setText(exercise.getMuscleGroup());
            tvSets.setText(String.valueOf(exercise.getSets()));
            tvReps.setText(exercise.getReps());
            tvRest.setText(exercise.getRest());

            holder.binding.llExercisesContainer.addView(exerciseView);
        }
    }

    private void addHeaderRow(LinearLayout container) {
        View header = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_exercise_row, container, false);

        ((TextView) header.findViewById(R.id.tvExerciseName)).setText("Exercise");
        ((TextView) header.findViewById(R.id.tvMuscleGroup)).setText("Group");
        ((TextView) header.findViewById(R.id.tvSets)).setText("Sets");
        ((TextView) header.findViewById(R.id.tvReps)).setText("Reps");
        ((TextView) header.findViewById(R.id.tvRest)).setText("Rest");

        // Style header
        header.findViewById(R.id.tvExerciseName).setAlpha(0.7f);
        header.setAlpha(0.6f);

        container.addView(header);
    }

    @Override
    public int getItemCount() {
        return workoutDays.size();
    }

    static class WorkoutDayViewHolder extends RecyclerView.ViewHolder {
        ItemWorkoutDayBinding binding;

        public WorkoutDayViewHolder(@NonNull ItemWorkoutDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}