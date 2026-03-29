package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ItemWorkoutPlanSummaryBinding;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;

public class WorkoutPlanSummaryAdapter extends RecyclerView.Adapter<WorkoutPlanSummaryAdapter.ViewHolder> {

    private List<WorkoutPlan> plans;
    private final OnPlanActionListener listener;
    private boolean isTrainerPortal = false;
    private boolean isTemplateTab = false;
    private final String currentUserId;

    public interface OnPlanActionListener {
        void onActivate(WorkoutPlan plan);
        void onDelete(WorkoutPlan plan);
        void onEdit(WorkoutPlan plan);
        default void onAssign(WorkoutPlan plan) {}
    }

    public WorkoutPlanSummaryAdapter(List<WorkoutPlan> plans, OnPlanActionListener listener) {
        this.plans = plans;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public void setTrainerPortal(boolean trainerPortal) {
        isTrainerPortal = trainerPortal;
    }

    public void setTemplateTab(boolean templateTab) {
        isTemplateTab = templateTab;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutPlanSummaryBinding binding = ItemWorkoutPlanSummaryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutPlan plan = plans.get(position);
        holder.binding.tvPlanTitle.setText(plan.getTitle());
        holder.binding.tvPlanDesc.setText(plan.getDescription());
        
        int exCount = 0;
        if (plan.getDays() != null) {
            for (var day : plan.getDays()) {
                if (day.getExercises() != null) exCount += day.getExercises().size();
            }
        }
        holder.binding.tvExerciseCount.setText(exCount + " Exercises");
        holder.binding.tvCompletedSessions.setText((plan.getLastCompletedDayIndex() + 1) + " Completed");

        if (plan.getCreatorName() != null && !plan.getCreatorName().isEmpty()) {
            holder.binding.tvPlanCreator.setVisibility(View.VISIBLE);
            holder.binding.tvPlanCreator.setText("By: " + plan.getCreatorName());
        } else {
            holder.binding.tvPlanCreator.setVisibility(View.GONE);
        }

        // Standardized Buttons & Menus
        if (!isTrainerPortal) {
            holder.binding.btnActivate.setVisibility(View.VISIBLE);
            holder.binding.btnActivate.setText("ACTIVATE");
            boolean canEdit = currentUserId != null && currentUserId.equals(plan.getCreatorId());
            holder.binding.ivPlanMenu.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        } else {
            holder.binding.btnActivate.setVisibility(isTemplateTab ? View.VISIBLE : View.GONE);
            if (isTemplateTab) {
                holder.binding.btnActivate.setText("ASSIGN");
            }
            holder.binding.ivPlanMenu.setVisibility(View.VISIBLE);
        }

        holder.binding.btnActivate.setOnClickListener(v -> {
            if (isTrainerPortal && isTemplateTab) {
                listener.onAssign(plan);
            } else {
                listener.onActivate(plan);
            }
        });
        
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(plan));
        holder.binding.ivPlanMenu.setOnClickListener(v -> listener.onEdit(plan));
        
        holder.itemView.setOnClickListener(v -> {
            if (isTrainerPortal || (currentUserId != null && currentUserId.equals(plan.getCreatorId()))) {
                listener.onEdit(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plans != null ? plans.size() : 0;
    }

    public void setPlans(List<WorkoutPlan> plans) {
        this.plans = plans;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemWorkoutPlanSummaryBinding binding;
        ViewHolder(ItemWorkoutPlanSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
