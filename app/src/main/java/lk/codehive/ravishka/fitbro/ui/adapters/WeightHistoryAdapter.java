package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lk.codehive.ravishka.fitbro.databinding.ItemWeightLogBinding;
import lk.codehive.ravishka.fitbro.models.WeightLog;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {

    private List<WeightLog> logs;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final OnLogActionListener listener;

    public interface OnLogActionListener {
        void onEdit(WeightLog log);
        void onDelete(WeightLog log);
    }

    public WeightHistoryAdapter(List<WeightLog> logs, OnLogActionListener listener) {
        this.logs = logs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeightLogBinding binding = ItemWeightLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightLog log = logs.get(position);
        holder.binding.tvWeightValue.setText(String.format(Locale.getDefault(), "%.1f kg", log.getWeight()));
        holder.binding.tvLogDate.setText(sdf.format(new Date(log.getTimestamp())));
        
        // 3 Hour Logic: Only show edit/delete if log is less than 3 hours old
        long threeHoursMillis = 3 * 60 * 60 * 1000;
        boolean canManage = (System.currentTimeMillis() - log.getTimestamp()) < threeHoursMillis;
        
        if (canManage) {
            holder.itemView.setOnLongClickListener(v -> {
                listener.onEdit(log);
                return true;
            });
        }

        if (position < logs.size() - 1) {
            float diff = log.getWeight() - logs.get(position + 1).getWeight();
            if (diff > 0) {
                holder.binding.tvWeightDiff.setText("+" + String.format("%.1f", diff));
                holder.binding.tvWeightDiff.setTextColor(0xFFE57373); 
            } else if (diff < 0) {
                holder.binding.tvWeightDiff.setText(String.format("%.1f", diff));
                holder.binding.tvWeightDiff.setTextColor(0xFF81C784); 
            } else {
                holder.binding.tvWeightDiff.setText("0.0");
            }
        } else {
            holder.binding.tvWeightDiff.setText("--");
        }
    }

    @Override
    public int getItemCount() {
        return logs != null ? logs.size() : 0;
    }

    public void setLogs(List<WeightLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemWeightLogBinding binding;
        ViewHolder(ItemWeightLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
