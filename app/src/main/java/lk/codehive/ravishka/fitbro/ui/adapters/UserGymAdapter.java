package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.codehive.ravishka.fitbro.databinding.ItemGymUserBinding;
import lk.codehive.ravishka.fitbro.models.Gym;

public class UserGymAdapter extends RecyclerView.Adapter<UserGymAdapter.ViewHolder> {

    private List<Gym> gymList;
    private final OnGymClickListener listener;

    public interface OnGymClickListener {
        void onGymClick(Gym gym);
        void onCallClick(Gym gym);
    }

    public UserGymAdapter(List<Gym> gymList, OnGymClickListener listener) {
        this.gymList = gymList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGymUserBinding binding = ItemGymUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gym gym = gymList.get(position);
        holder.binding.tvGymName.setText(gym.getName());
        holder.binding.tvGymAddress.setText(gym.getAddress());
        
        holder.itemView.setOnClickListener(v -> listener.onGymClick(gym));
        holder.binding.btnCall.setOnClickListener(v -> listener.onCallClick(gym));
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
        ItemGymUserBinding binding;
        ViewHolder(ItemGymUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
