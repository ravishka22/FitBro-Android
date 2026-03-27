package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.ItemWorkoutPlanSummaryBinding;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;

public class WorkoutPlanSummaryAdapter extends RecyclerView.Adapter<WorkoutPlanSummaryAdapter.ViewHolder> {

    private List<WorkoutPlan> plans;
    private OnPlanActionListener listener;

    public interface OnPlanActionListener {
        void onActivate(WorkoutPlan plan);
    }

    public WorkoutPlanSummaryAdapter(List<WorkoutPlan> plans, OnPlanActionListener listener) {
        this.plans = plans;
        this.listener = listener;
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
        holder.binding.btnActivate.setOnClickListener(v -> listener.onActivate(plan));
    }

    @Override
    public int getItemCount() {
        return plans.size();
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
