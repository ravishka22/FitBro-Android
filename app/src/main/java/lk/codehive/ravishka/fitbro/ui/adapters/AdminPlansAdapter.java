package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;
import lk.codehive.ravishka.fitbro.databinding.ItemAdminMembershipPlanBinding;

public class AdminPlansAdapter extends RecyclerView.Adapter<AdminPlansAdapter.ViewHolder> {

    private List<Map<String, Object>> plans;
    private final OnPlanActionListener listener;

    public interface OnPlanActionListener {
        void onEdit(Map<String, Object> plan);
        void onDelete(Map<String, Object> plan);
    }

    public AdminPlansAdapter(List<Map<String, Object>> plans, OnPlanActionListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminMembershipPlanBinding binding = ItemAdminMembershipPlanBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> plan = plans.get(position);
        holder.binding.tvPlanName.setText((String) plan.get("name"));
        holder.binding.tvPlanPrice.setText("$" + plan.get("price"));
        holder.binding.tvPlanFeatures.setText((String) plan.get("features"));

        holder.binding.btnEditPlan.setOnClickListener(v -> listener.onEdit(plan));
        holder.binding.btnDeletePlan.setOnClickListener(v -> listener.onDelete(plan));
    }

    @Override
    public int getItemCount() {
        return plans != null ? plans.size() : 0;
    }

    public void setPlans(List<Map<String, Object>> plans) {
        this.plans = plans;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdminMembershipPlanBinding binding;
        ViewHolder(ItemAdminMembershipPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
