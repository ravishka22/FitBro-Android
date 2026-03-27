package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.codehive.ravishka.fitbro.databinding.ItemWorkoutSessionBinding;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;

public class WorkoutSessionAdapter extends RecyclerView.Adapter<WorkoutSessionAdapter.ViewHolder> {

    private List<WorkoutSession> sessions;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public WorkoutSessionAdapter(List<WorkoutSession> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutSessionBinding binding = ItemWorkoutSessionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSession session = sessions.get(position);
        holder.binding.tvSessionPlanName.setText(session.getPlanName());
        holder.binding.tvSessionDayTitle.setText(session.getDayTitle());
        holder.binding.tvSessionDate.setText(dateFormat.format(new Date(session.getTimestamp())));
        holder.binding.tvSessionCalories.setText(session.getCaloriesBurnt() + " kcal");
        
        long mins = session.getDurationMillis() / 60000;
        holder.binding.tvSessionDuration.setText(mins + " mins");
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    public void setSessions(List<WorkoutSession> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemWorkoutSessionBinding binding;
        ViewHolder(ItemWorkoutSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
