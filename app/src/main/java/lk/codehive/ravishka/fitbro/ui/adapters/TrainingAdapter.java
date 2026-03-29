package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.ItemExerciseTrainingBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.TrainingViewHolder> {

    private List<Exercise> exercises;

    public TrainingAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public TrainingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseTrainingBinding binding = ItemExerciseTrainingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TrainingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.binding.tvExerciseName.setText(exercise.getName());
        holder.binding.tvExerciseDetails.setText(exercise.getSets() + " x " + exercise.getReps() + " reps");
        holder.binding.tvRestDetails.setText("rest " + exercise.getRest());

        if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            holder.binding.ivExerciseThumbnail.setVisibility(View.VISIBLE);
            holder.binding.tvExerciseInitials.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext())
                    .load(exercise.getImageUrl())
                    .into(holder.binding.ivExerciseThumbnail);
        } else {
            holder.binding.ivExerciseThumbnail.setVisibility(View.GONE);
            holder.binding.tvExerciseInitials.setVisibility(View.VISIBLE);
            holder.binding.tvExerciseInitials.setText(getInitials(exercise.getName()));
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] words = name.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, 2); i++) {
            if (!words[i].isEmpty()) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }
        return initials.toString();
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    public static class TrainingViewHolder extends RecyclerView.ViewHolder {
        public ItemExerciseTrainingBinding binding;

        public TrainingViewHolder(@NonNull ItemExerciseTrainingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}