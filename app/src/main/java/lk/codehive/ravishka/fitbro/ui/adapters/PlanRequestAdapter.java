package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ItemPlanRequestBinding;
import lk.codehive.ravishka.fitbro.models.PlanRequest;

public class PlanRequestAdapter extends RecyclerView.Adapter<PlanRequestAdapter.ViewHolder> {

    private List<PlanRequest> requests;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onReviewRequest(PlanRequest request);
        void onRejectRequest(PlanRequest request);
        void onDeleteRecord(PlanRequest request);
    }

    public PlanRequestAdapter(List<PlanRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlanRequestBinding binding = ItemPlanRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanRequest request = requests.get(position);
        holder.binding.tvUserName.setText(request.getUserName());
        holder.binding.tvTimestamp.setText(getRelativeTime(request.getTimestamp()));

        // Handle Thumbnail
        if (request.getUserPhotoUrl() != null && !request.getUserPhotoUrl().isEmpty()) {
            holder.binding.ivUserAvatar.setVisibility(View.VISIBLE);
            holder.binding.tvUserInitials.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext())
                    .load(request.getUserPhotoUrl())
                    .placeholder(R.drawable.logo_dark)
                    .circleCrop()
                    .into(holder.binding.ivUserAvatar);
        } else {
            holder.binding.ivUserAvatar.setVisibility(View.GONE);
            holder.binding.tvUserInitials.setVisibility(View.VISIBLE);
            holder.binding.tvUserInitials.setText(getInitials(request.getUserName()));
        }

        // Status Based UI
        String status = request.getStatus();
        if ("accepted".equalsIgnoreCase(status)) {
            holder.binding.tvGoal.setText("IN PROGRESS: " + request.getGoal());
            holder.binding.tvGoal.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.electric_green_variant));
            holder.binding.btnViewDetails.setText("CONTINUE BUILD");
            holder.binding.btnCancelRequest.setText("DELETE");
            holder.binding.btnCancelRequest.setOnClickListener(v -> listener.onDeleteRecord(request));
        } else {
            holder.binding.tvGoal.setText("GOAL: " + request.getGoal());
            holder.binding.tvGoal.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.electric_green_variant));
            holder.binding.btnViewDetails.setText("REVIEW");
            holder.binding.btnCancelRequest.setText("REJECT");
            holder.binding.btnCancelRequest.setOnClickListener(v -> listener.onRejectRequest(request));
        }

        holder.binding.btnViewDetails.setOnClickListener(v -> listener.onReviewRequest(request));
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] words = name.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, 2); i++) {
            if (!words[i].isEmpty()) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }
        return initials.length() > 0 ? initials.toString() : "U";
    }

    private String getRelativeTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    public void setRequests(List<PlanRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPlanRequestBinding binding;
        ViewHolder(ItemPlanRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
