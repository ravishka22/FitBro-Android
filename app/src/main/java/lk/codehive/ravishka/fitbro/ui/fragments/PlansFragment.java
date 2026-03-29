package lk.codehive.ravishka.fitbro.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentPlansBinding;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.ui.TrainingActivity;
import lk.codehive.ravishka.fitbro.ui.adapters.WorkoutPlanSummaryAdapter;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class PlansFragment extends Fragment {

    private static final String TAG = "PlansFragment";
    private FragmentPlansBinding binding;
    private WorkoutViewModel viewModel;
    private WorkoutPlanSummaryAdapter adapter;
    private boolean isProMember = false;
    private int totalPlansCount = 0;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration membershipListener;
    private ListenerRegistration requestListener;
    
    private String activeRequestId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
        setupListeners();
        checkMembership();
        listenForRequests();
    }

    private void checkMembership() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            membershipListener = db.collection("users").document(user.getUid()).addSnapshotListener((doc, e) -> {
                if (doc != null && doc.exists()) {
                    isProMember = Boolean.TRUE.equals(doc.getBoolean("isPro"));
                }
            });
        }
    }

    private void listenForRequests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            requestListener = db.collection("trainer_requests")
                    .whereEqualTo("userId", user.getUid())
                    .whereEqualTo("status", "pending")
                    .addSnapshotListener((value, error) -> {
                        if (binding == null) return;
                        if (value != null && !value.isEmpty()) {
                            DocumentSnapshot doc = value.getDocuments().get(0);
                            activeRequestId = doc.getId();
                            
                            binding.cardRequestedStatus.setVisibility(View.VISIBLE);
                            binding.tvRequestStatus.setText("Trainer is building your plan...");
                            binding.btnRequestPlan.setEnabled(false); // Block new requests
                            binding.btnRequestPlan.setAlpha(0.5f);
                        } else {
                            activeRequestId = null;
                            binding.cardRequestedStatus.setVisibility(View.GONE);
                            binding.btnRequestPlan.setEnabled(true);
                            binding.btnRequestPlan.setAlpha(1.0f);
                        }
                    });
        }
    }

    private void setupRecyclerView() {
        adapter = new WorkoutPlanSummaryAdapter(new ArrayList<>(), new WorkoutPlanSummaryAdapter.OnPlanActionListener() {
            @Override
            public void onActivate(WorkoutPlan plan) {
                viewModel.setActivePlan(plan.getId());
                Toast.makeText(getContext(), plan.getTitle() + " Activated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(WorkoutPlan plan) {
                confirmDeletePlan(plan);
            }

            @Override
            public void onEdit(WorkoutPlan plan) {
                navigateToEditPlan(plan);
            }
        });
        binding.rvOtherPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOtherPlans.setAdapter(adapter);
    }

    private void confirmDeletePlan(WorkoutPlan plan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Plan")
                .setMessage("Are you sure you want to delete '" + plan.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.delete(plan))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmCancelRequest() {
        if (activeRequestId == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to withdraw your training request?")
                .setPositiveButton("Withdraw", (dialog, which) -> {
                    db.collection("trainer_requests").document(activeRequestId).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Request cancelled", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Keep Request", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getActivePlan().observe(getViewLifecycleOwner(), activePlan -> {
            if (activePlan != null) {
                binding.cardActivePlan.setVisibility(View.VISIBLE);
                binding.tvActivePlanTitle.setText(activePlan.getTitle());
                
                String dayTitle = (activePlan.getDays() != null && !activePlan.getDays().isEmpty()) 
                        ? activePlan.getDays().get(activePlan.getTodayDayIndex()).getDayTitle() 
                        : "No days found";
                
                StringBuilder desc = new StringBuilder(dayTitle);
                if (activePlan.getCreatorName() != null && !activePlan.getCreatorName().isEmpty()) {
                    desc.append("\nBy: ").append(activePlan.getCreatorName());
                }
                binding.tvActivePlanDesc.setText(desc.toString());
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
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.syncWithCloud();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.btnStartTraining.setOnClickListener(v -> startActivity(new Intent(getContext(), TrainingActivity.class)));

        binding.btnCreatePlan.setOnClickListener(v -> {
            if (!isProMember && totalPlansCount >= 1) {
                showUpgradeProSheet();
            } else {
                navigateToCreatePlan();
            }
        });

        binding.btnRequestPlan.setOnClickListener(v -> {
            if (activeRequestId != null) {
                Toast.makeText(getContext(), "You already have a pending request.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isProMember) {
                showUpgradeProSheet();
            } else {
                navigateToRequestPlan();
            }
        });

        binding.btnCancelRequest.setOnClickListener(v -> confirmCancelRequest());
        
        binding.btnEditRequest.setOnClickListener(v -> {
            // Implementation for editing request could go here or navigate to TrainerRequestFragment with data
            navigateToRequestPlan(); 
        });
    }

    private void navigateToCreatePlan() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new CreatePlanFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToEditPlan(WorkoutPlan plan) {
        Bundle bundle = new Bundle();
        bundle.putString("EDIT_PLAN_ID", plan.getId());
        CreatePlanFragment fragment = new CreatePlanFragment();
        fragment.setArguments(bundle);
        
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToRequestPlan() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new TrainerRequestFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showUpgradeProSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_upgrade_pro_sheet, null);
        dialog.setContentView(view);
        view.findViewById(R.id.btnGoPro).setOnClickListener(v -> {
            dialog.dismiss();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, new CheckoutFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        if (membershipListener != null) membershipListener.remove();
        if (requestListener != null) requestListener.remove();
        super.onDestroyView();
        binding = null;
    }
}
