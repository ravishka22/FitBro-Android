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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentPlansBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutDay;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.ui.TrainingActivity;
import lk.codehive.ravishka.fitbro.ui.adapters.WorkoutPlanSummaryAdapter;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class PlansFragment extends Fragment {

    private FragmentPlansBinding binding;
    private WorkoutViewModel viewModel;
    private WorkoutPlanSummaryAdapter adapter;
    private boolean isProMember = false; // Mock membership status
    private int totalPlansCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
        setupListeners();
        
        // Seed database if empty for demo
        seedDatabase();
    }

    private void setupRecyclerView() {
        adapter = new WorkoutPlanSummaryAdapter(new ArrayList<>(), plan -> {
            viewModel.setActivePlan(plan.getId());
        });
        binding.rvOtherPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOtherPlans.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getActivePlan().observe(getViewLifecycleOwner(), activePlan -> {
            if (activePlan != null) {
                binding.cardActivePlan.setVisibility(View.VISIBLE);
                binding.tvActivePlanTitle.setText(activePlan.getTitle());
                binding.tvActivePlanDesc.setText("Next: " + activePlan.getDays().get(activePlan.getTodayDayIndex()).getDayTitle());
            } else {
                binding.cardActivePlan.setVisibility(View.GONE);
            }
        });

        viewModel.getAllPlans().observe(getViewLifecycleOwner(), plans -> {
            if (plans != null) {
                totalPlansCount = plans.size();
                List<WorkoutPlan> inactivePlans = new ArrayList<>();
                for (WorkoutPlan p : plans) {
                    if (!p.isActive()) inactivePlans.add(p);
                }
                adapter.setPlans(inactivePlans);
            }
        });
    }

    private void setupListeners() {
        binding.btnStartTraining.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TrainingActivity.class);
            startActivity(intent);
        });

        binding.btnCreatePlan.setOnClickListener(v -> {
            // Restriction: Free user can only have 1 plan (including the active one)
            if (!isProMember && totalPlansCount >= 1) {
                showUpgradeProSheet();
            } else {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, new CreatePlanFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        binding.btnRequestPlan.setOnClickListener(v -> {
            if (!isProMember) {
                showUpgradeProSheet();
            } else {
                showRequestPlanSheet();
            }
        });
    }

    private void showUpgradeProSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_upgrade_pro_sheet, null);
        dialog.setContentView(view);
        
        view.findViewById(R.id.btnGoPro).setOnClickListener(v -> {
            isProMember = true;
            updateMembershipUI();
            Toast.makeText(getContext(), "Account Upgraded to PRO!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void updateMembershipUI() {
        // Method to update UI based on membership if needed
    }

    private void showRequestPlanSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_request_plan_sheet, null);
        dialog.setContentView(view);
        
        view.findViewById(R.id.btnSubmitFinalRequest).setOnClickListener(v -> {
            binding.cardRequestedStatus.setVisibility(View.VISIBLE);
            binding.tvRequestStatus.setText("Pending Trainer Approval");
            Toast.makeText(getContext(), "Plan Request Sent!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        // Mocking photo upload clicks
        view.findViewById(R.id.btnUploadFront).setOnClickListener(v -> Toast.makeText(getContext(), "Front Photo Selected", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnUploadSide).setOnClickListener(v -> Toast.makeText(getContext(), "Side Photo Selected", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnUploadBack).setOnClickListener(v -> Toast.makeText(getContext(), "Back Photo Selected", Toast.LENGTH_SHORT).show());
        
        dialog.show();
    }

    private void seedDatabase() {
        viewModel.getAllPlans().observe(getViewLifecycleOwner(), plans -> {
            if (plans == null || plans.isEmpty()) {
                List<WorkoutDay> days = new ArrayList<>();
                List<Exercise> ex = new ArrayList<>();
                ex.add(new Exercise("Bench Press", "Chest", 3, "10", "90", ""));
                days.add(new WorkoutDay("Day 1 Chest", ex));
                
                WorkoutPlan defaultPlan = new WorkoutPlan("default_1", "3-Day Split Workout", "Intermediate Level", days, true);
                viewModel.insert(defaultPlan);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
