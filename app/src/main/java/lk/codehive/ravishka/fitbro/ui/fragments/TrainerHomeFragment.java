package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentTrainerHomeBinding;
import lk.codehive.ravishka.fitbro.models.PlanRequest;
import lk.codehive.ravishka.fitbro.ui.adapters.PlanRequestAdapter;

public class TrainerHomeFragment extends Fragment {

    private static final String TAG = "TrainerHomeFragment";
    private FragmentTrainerHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PlanRequestAdapter adapter;
    private ListenerRegistration requestsListener;
    private ListenerRegistration athletesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrainerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        setupRecyclerView();
        setupProfile();
        updateFcmToken();
        listenForRequests();
        listenForMyAthletes();
    }

    private void setupProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists() && binding != null) {
                    String name = doc.getString("fullName");
                    if (name == null || name.isEmpty()) name = user.getDisplayName();
                    if (name == null || name.isEmpty()) name = "Coach";
                    
                    binding.tvTrainerGreeting.setText("Welcome Back, " + name);
                    
                    String photoUrl = doc.getString("photoUrl");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.logo_dark).circleCrop().into(binding.ivTrainerProfile);
                    }
                }
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new PlanRequestAdapter(new ArrayList<>(), new PlanRequestAdapter.OnRequestActionListener() {
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
                confirmDeleteRequest(request);
            }
        });
        binding.rvPendingRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPendingRequests.setAdapter(adapter);
    }

    private void confirmRejectRequest(PlanRequest request) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Request")
                .setMessage("Are you sure you want to reject " + request.getUserName() + "'s request? This will permanently delete the request.")
                .setPositiveButton("REJECT", (dialog, which) -> {
                    // Logic changed: Delete from database as requested
                    db.collection("trainer_requests").document(request.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Request rejected and deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting request", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void confirmDeleteRequest(PlanRequest request) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Record")
                .setMessage("This will remove the request record from your view.")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    db.collection("trainer_requests").document(request.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Record deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void updateFcmToken() {
        if (mAuth.getCurrentUser() == null) return;
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated for trainer"));
        });
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
                            navigateToPlanCreator(request);
                        });
            } else {
                dialog.dismiss();
                navigateToPlanCreator(request);
            }
        });

        dialog.show();
    }

    private void navigateToPlanCreator(PlanRequest request) {
        Bundle bundle = new Bundle();
        bundle.putString("USER_ID", request.getUserId());
        bundle.putString("REQUEST_ID", request.getId());
        
        CreatePlanFragment fragment = new CreatePlanFragment();
        fragment.setArguments(bundle);
        
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.trainer_nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void listenForRequests() {
        if (mAuth.getCurrentUser() == null) return;

        String trainerId = mAuth.getCurrentUser().getUid();
        if (requestsListener != null) requestsListener.remove();

        requestsListener = db.collection("trainer_requests")
                .whereEqualTo("trainerId", trainerId)
                .whereIn("status", List.of("pending", "accepted"))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Requests Error: " + error.getMessage());
                        return;
                    }
                    if (value != null && binding != null) {
                        List<PlanRequest> requests = new ArrayList<>();
                        int pendingCount = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            PlanRequest req = doc.toObject(PlanRequest.class);
                            if (req.getId() == null) req.setId(doc.getId());
                            requests.add(req);
                            if ("pending".equalsIgnoreCase(req.getStatus())) {
                                pendingCount++;
                            }
                        }
                        adapter.setRequests(requests);
                        binding.tvPendingCount.setText(String.valueOf(pendingCount));
                        binding.tvNoRequests.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void listenForMyAthletes() {
        if (mAuth.getCurrentUser() == null) return;

        String trainerId = mAuth.getCurrentUser().getUid();
        if (athletesListener != null) athletesListener.remove();

        athletesListener = db.collection("workout_plans")
                .whereEqualTo("creatorId", trainerId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null && binding != null) {
                        java.util.Set<String> uniqueAthletes = new java.util.HashSet<>();
                        for (QueryDocumentSnapshot doc : value) {
                            String targetUserId = doc.getString("targetUserId");
                            if (targetUserId != null) uniqueAthletes.add(targetUserId);
                        }
                        binding.tvActiveUsersCount.setText(String.valueOf(uniqueAthletes.size()));
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestsListener != null) requestsListener.remove();
        if (athletesListener != null) athletesListener.remove();
        binding = null;
    }
}
