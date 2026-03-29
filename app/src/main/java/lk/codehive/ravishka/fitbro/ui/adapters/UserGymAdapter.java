package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lk.codehive.ravishka.fitbro.databinding.ItemGymUserBinding;
import lk.codehive.ravishka.fitbro.models.Gym;

public class UserGymAdapter extends RecyclerView.Adapter<UserGymAdapter.ViewHolder> {

    private List<Gym> gymList;
    private Map<String, Float> distanceMap = new HashMap<>();
    private final OnGymClickListener listener;

    public interface OnGymClickListener {
        void onGymClick(Gym gym);
        void onCallClick(Gym gym);
        void onDirectionsClick(Gym gym);
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
        
        Float distance = distanceMap.get(gym.getId());
        if (distance != null && distance < Float.MAX_VALUE) {
            float km = distance / 1000;
            holder.binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", km));
            holder.binding.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvDistance.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onGymClick(gym));
        holder.binding.btnCall.setOnClickListener(v -> listener.onCallClick(gym));
        holder.binding.btnDirections.setOnClickListener(v -> listener.onDirectionsClick(gym));
    }

    @Override
    public int getItemCount() {
        return gymList != null ? gymList.size() : 0;
    }

    public void setData(List<Gym> gyms, Map<String, Float> distances) {
        this.gymList = gyms;
        this.distanceMap = distances;
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
