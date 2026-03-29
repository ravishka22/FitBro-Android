package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentTrainerPlansBinding;
import lk.codehive.ravishka.fitbro.models.PlanRequest;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.ui.adapters.PlanRequestAdapter;
import lk.codehive.ravishka.fitbro.ui.adapters.WorkoutPlanSummaryAdapter;

public class TrainerPlansFragment extends Fragment {

    private static final String TAG = "TrainerPlansFragment";
    private FragmentTrainerPlansBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private PlanRequestAdapter requestAdapter;
    private WorkoutPlanSummaryAdapter allPlansAdapter;
    
    private ListenerRegistration activeListener;
    private int currentTab = 0; // 0: Incoming, 1: All Plans

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrainerPlansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupAdapters();
        setupTabLayout();
        setupListeners();
        loadTabData();
    }

    private void setupAdapters() {
        requestAdapter = new PlanRequestAdapter(new ArrayList<>(), new PlanRequestAdapter.OnRequestActionListener() {
            @Override
            public void onReviewRequest(PlanRequest request) {
                showRequestDetailSheet(request);
            }

            @Override
            public void onRejectRequest(PlanRequest request) {
                confirmRejectRequest(request);
            }

            @Override
            public void onDeleteRecord(PlanRequest request) {
                confirmDeleteRequestRecord(request);
            }
        });

        WorkoutPlanSummaryAdapter.OnPlanActionListener actionListener = new WorkoutPlanSummaryAdapter.OnPlanActionListener() {
            @Override
            public void onActivate(WorkoutPlan plan) {}

            @Override
            public void onDelete(WorkoutPlan plan) {
                confirmDeletePlan(plan);
            }

            @Override
            public void onEdit(WorkoutPlan plan) {
                navigateToPlanCreator(plan.getTargetUserId(), plan.getRequestId(), plan.getId());
            }

            @Override
            public void onAssign(WorkoutPlan plan) {
                showAssignMemberSheet(plan);
            }
        };

        allPlansAdapter = new WorkoutPlanSummaryAdapter(new ArrayList<>(), actionListener);
        allPlansAdapter.setTrainerPortal(true);
        allPlansAdapter.setTemplateTab(true); 

        binding.rvPlansOrRequests.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void confirmRejectRequest(PlanRequest request) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Request")
                .setMessage("Are you sure you want to reject " + request.getUserName() + "'s request? This will permanently delete the request.")
                .setPositiveButton("REJECT", (dialog, which) -> {
                    // Changed to DELETE from database as requested
                    db.collection("trainer_requests").document(request.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Request rejected and deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void confirmDeleteRequestRecord(PlanRequest request) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Record")
                .setMessage("Remove this request record? The assigned plan will remain active for the user.")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    db.collection("trainer_requests").document(request.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Record deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void showRequestDetailSheet(PlanRequest request) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_request_detail_sheet, null);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvUserNameDetail);
        TextView tvGoal = view.findViewById(R.id.tvGoalDetail);
        TextView tvMedical = view.findViewById(R.id.tvMedicalDetail);
        TextView tvDays = view.findViewById(R.id.tvDaysDetail);
        ShapeableImageView ivFront = view.findViewById(R.id.ivFrontDetail);
        ShapeableImageView ivSide = view.findViewById(R.id.ivSideDetail);
        ShapeableImageView ivBack = view.findViewById(R.id.ivBackDetail);

        tvName.setText(request.getUserName());
        tvGoal.setText(request.getGoal());
        tvMedical.setText("Medical: " + (request.getMedicalConditions() != null && !request.getMedicalConditions().isEmpty() ? request.getMedicalConditions() : "None reported"));
        tvDays.setText("Preferred Frequency: " + request.getPreferredDays() + " days/week");

        List<String> photos = request.getPhotoUrls();
        if (photos != null && photos.size() >= 1) Glide.with(this).load(photos.get(0)).placeholder(R.color.surface_dark).into(ivFront);
        if (photos != null && photos.size() >= 2) Glide.with(this).load(photos.get(1)).placeholder(R.color.surface_dark).into(ivSide);
        if (photos != null && photos.size() >= 3) Glide.with(this).load(photos.get(2)).placeholder(R.color.surface_dark).into(ivBack);

        view.findViewById(R.id.btnCreatePlanFromDetail).setOnClickListener(v -> {
            if ("pending".equalsIgnoreCase(request.getStatus())) {
                db.collection("trainer_requests").document(request.getId())
                        .update("status", "accepted")
                        .addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            navigateToPlanCreator(request.getUserId(), request.getId(), null);
                        });
            } else {
                dialog.dismiss();
                navigateToPlanCreator(request.getUserId(), request.getId(), null);
            }
        });

        dialog.show();
    }

    private void setupTabLayout() {
        binding.tabLayoutPlans.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadTabData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupListeners() {
        binding.fabCreateNewPlan.setOnClickListener(v -> navigateToPlanCreator(null, null, null));
    }

    private void loadTabData() {
        if (auth.getUid() == null) return;

        if (activeListener != null) {
            activeListener.remove();
        }

        if (currentTab == 0) {
            binding.rvPlansOrRequests.setAdapter(requestAdapter);
            binding.fabCreateNewPlan.setVisibility(View.GONE);
            startUserRequestsListener();
        } else {
            binding.rvPlansOrRequests.setAdapter(allPlansAdapter);
            binding.fabCreateNewPlan.setVisibility(View.VISIBLE);
            startAllPlansListener();
        }
    }

    private void startUserRequestsListener() {
        activeListener = db.collection("trainer_requests")
                .whereEqualTo("trainerId", auth.getUid())
                .whereIn("status", List.of("pending", "accepted"))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && binding != null) {
                        List<PlanRequest> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            PlanRequest req = doc.toObject(PlanRequest.class);
                            if (req.getId() == null) req.setId(doc.getId());
                            requests.add(req);
                        }
                        requestAdapter.setRequests(requests);
                    }
                });
    }

    private void startAllPlansListener() {
        activeListener = db.collection("workout_plans")
                .whereEqualTo("creatorId", auth.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && binding != null) {
                        List<WorkoutPlan> plans = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            WorkoutPlan plan = doc.toObject(WorkoutPlan.class);
                            if (plan.getId() == null) plan.setId(doc.getId());
                            plans.add(plan);
                        }
                        allPlansAdapter.setPlans(plans);
                    }
                });
    }

    private void showAssignMemberSheet(WorkoutPlan plan) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_assign_member_sheet, null);
        dialog.setContentView(view);

        AutoCompleteTextView spinnerMembers = view.findViewById(R.id.spinnerSelectMember);
        MaterialButton btnAssign = view.findViewById(R.id.btnConfirmAssign);

        List<Map<String, String>> membersList = new ArrayList<>();
        db.collection("users")
                .whereEqualTo("role", "member")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> memberNames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("fullName");
                        if (name == null || name.isEmpty()) name = doc.getString("email");
                        memberNames.add(name);
                        Map<String, String> member = new HashMap<>();
                        member.put("id", doc.getId());
                        member.put("name", name);
                        membersList.add(member);
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, memberNames);
                        spinnerMembers.setAdapter(adapter);
                    }
                });

        btnAssign.setOnClickListener(v -> {
            String selectedName = spinnerMembers.getText().toString();
            String targetId = "";
            for (Map<String, String> m : membersList) {
                if (m.get("name").equals(selectedName)) {
                    targetId = m.get("id");
                    break;
                }
            }
            if (targetId.isEmpty()) {
                Toast.makeText(getContext(), "Please select a member", Toast.LENGTH_SHORT).show();
                return;
            }
            assignPlanToMember(plan, targetId);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void assignPlanToMember(WorkoutPlan template, String userId) {
        String newPlanId = UUID.randomUUID().toString();
        WorkoutPlan assignedPlan = new WorkoutPlan(
                newPlanId,
                template.getTitle(),
                template.getDescription(),
                template.getDays(),
                true 
        );
        assignedPlan.setCreatorId(auth.getUid());
        assignedPlan.setCreatorName(auth.getCurrentUser().getDisplayName());
        assignedPlan.setTargetUserId(userId);
        assignedPlan.setCreatedAt(System.currentTimeMillis());
        assignedPlan.setValidUntil(System.currentTimeMillis() + (4L * 7L * 24L * 60L * 60L * 1000L));

        db.collection("users").document(userId).collection("plans").document(newPlanId).set(assignedPlan)
                .addOnSuccessListener(aVoid -> {
                    db.collection("workout_plans").document(newPlanId).set(assignedPlan);
                    Toast.makeText(getContext(), "Plan assigned successfully!", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDeletePlan(WorkoutPlan plan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Plan")
                .setMessage("Are you sure you want to delete '" + plan.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("workout_plans").document(plan.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                if (plan.getTargetUserId() != null) {
                                    db.collection("users").document(plan.getTargetUserId())
                                            .collection("plans").document(plan.getId()).delete();
                                }
                                Toast.makeText(getContext(), "Plan deleted", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToPlanCreator(@Nullable String userId, @Nullable String requestId, @Nullable String planId) {
        Bundle bundle = new Bundle();
        if (userId != null) bundle.putString("USER_ID", userId);
        if (requestId != null) bundle.putString("REQUEST_ID", requestId);
        if (planId != null) bundle.putString("EDIT_PLAN_ID", planId);
        
        CreatePlanFragment fragment = new CreatePlanFragment();
        fragment.setArguments(bundle);
        
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.trainer_nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeListener != null) {
            activeListener.remove();
        }
        binding = null;
    }
}
