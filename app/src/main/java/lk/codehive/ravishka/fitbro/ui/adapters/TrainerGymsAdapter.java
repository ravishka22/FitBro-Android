package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.ItemGymManageBinding;
import lk.codehive.ravishka.fitbro.models.Gym;

public class TrainerGymsAdapter extends RecyclerView.Adapter<TrainerGymsAdapter.ViewHolder> {

    private List<Gym> gymList;
    private final OnGymActionListener listener;
    private final String currentUserId;

    public interface OnGymActionListener {
        void onEdit(Gym gym);
        void onDelete(Gym gym);
    }

    public TrainerGymsAdapter(List<Gym> gymList, String currentUserId, OnGymActionListener listener) {
        this.gymList = gymList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGymManageBinding binding = ItemGymManageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gym gym = gymList.get(position);
        holder.binding.tvGymName.setText(gym.getName());
        holder.binding.tvGymAddress.setText(gym.getAddress());
        holder.binding.tvGymPhone.setText("📞 " + gym.getPhoneNumber());

        // Check ownership or Admin bypass
        boolean isOwner = gym.getCreatorId() != null && gym.getCreatorId().equals(currentUserId);
        boolean isAdmin = "ADMIN_BYPASS".equals(currentUserId);

        if (isOwner || isAdmin) {
            holder.binding.ivEdit.setVisibility(View.VISIBLE);
            holder.binding.ivDelete.setVisibility(View.VISIBLE);
        } else {
            holder.binding.ivEdit.setVisibility(View.GONE);
            holder.binding.ivDelete.setVisibility(View.GONE);
        }

        holder.binding.ivEdit.setOnClickListener(v -> listener.onEdit(gym));
        holder.binding.ivDelete.setOnClickListener(v -> listener.onDelete(gym));
    }

    @Override
    public int getItemCount() {
        return gymList != null ? gymList.size() : 0;
    }

    public void setGyms(List<Gym> gyms) {
        this.gymList = gyms;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemGymManageBinding binding;
        ViewHolder(ItemGymManageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
