package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ItemAdminUserBinding;
import lk.codehive.ravishka.fitbro.models.User;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.ViewHolder> {

    private List<User> users;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onUserClick(User user);
    }

    public AdminUsersAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        
        holder.binding.tvAdminUserName.setText(user.getFullName() != null ? user.getFullName() : "FitBro User");
        holder.binding.tvAdminUserEmail.setText(user.getEmail());
        
        String role = user.getRole();
        holder.binding.chipUserRole.setText(role != null ? role.toUpperCase() : "USER");
        
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            holder.binding.ivUserAvatar.setVisibility(View.VISIBLE);
            holder.binding.tvUserInitials.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.logo_dark)
                    .into(holder.binding.ivUserAvatar);
        } else {
            holder.binding.ivUserAvatar.setVisibility(View.GONE);
            holder.binding.tvUserInitials.setVisibility(View.VISIBLE);
            holder.binding.tvUserInitials.setText(getInitials(user.getFullName()));
        }

        if (user.isDeactivated()) {
            holder.itemView.setAlpha(0.5f);
            holder.binding.chipUserRole.setText("DEACTIVATED");
            holder.binding.chipUserRole.setChipBackgroundColorResource(R.color.error_red);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.binding.chipUserRole.setChipBackgroundColorResource(R.color.electric_green);
        }

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
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

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdminUserBinding binding;
        ViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
