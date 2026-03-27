package lk.codehive.ravishka.fitbro.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentProfileBinding;
import lk.codehive.ravishka.fitbro.ui.LoginActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private boolean isProMember = false; // Mock membership state

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        updateMembershipUI();
        setupListeners();
    }

    private void updateMembershipUI() {
        if (isProMember) {
            binding.tvMembershipStatus.setText("GYM MEMBER / PRO");
            binding.cardMembership.setCardBackgroundColor(getResources().getColor(R.color.electric_green));
            binding.btnUpgrade.setVisibility(View.GONE);
            binding.btnRequestPlan.setVisibility(View.VISIBLE);
        } else {
            binding.tvMembershipStatus.setText("FREE MEMBER");
            binding.cardMembership.setCardBackgroundColor(getResources().getColor(R.color.text_secondary));
            binding.btnUpgrade.setVisibility(View.VISIBLE);
            binding.btnRequestPlan.setVisibility(View.GONE);
        }
        
        binding.tvUserName.setText("Alex Johnson");
        binding.tvUserEmail.setText("alex.fitness@email.com");
    }

    private void setupListeners() {
        // Toggle for demo purposes
        binding.cardMembership.setOnClickListener(v -> {
            isProMember = !isProMember;
            updateMembershipUI();
            String msg = isProMember ? "Switched to PRO Membership" : "Switched to FREE Membership";
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        binding.btnUpgrade.setOnClickListener(v -> {
            isProMember = true;
            updateMembershipUI();
            Toast.makeText(getContext(), "Account Upgraded to PRO!", Toast.LENGTH_SHORT).show();
        });

        binding.btnRequestPlan.setOnClickListener(v -> {
            // Navigate to Trainer Request
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, new TrainerRequestFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        binding.btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}