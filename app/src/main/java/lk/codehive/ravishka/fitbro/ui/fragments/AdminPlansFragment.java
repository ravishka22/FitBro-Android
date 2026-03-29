package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentAdminPlansBinding;
import lk.codehive.ravishka.fitbro.ui.adapters.AdminPlansAdapter;

public class AdminPlansFragment extends Fragment {

    private FragmentAdminPlansBinding binding;
    private FirebaseFirestore db;
    private List<Map<String, Object>> plansList = new ArrayList<>();
    private AdminPlansAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminPlansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        
        setupRecyclerView();
        loadPlans();
        
        binding.fabAdminAddPlan.setOnClickListener(v -> showAddEditPlanSheet(null));
    }

    private void setupRecyclerView() {
        adapter = new AdminPlansAdapter(plansList, new AdminPlansAdapter.OnPlanActionListener() {
            @Override
            public void onEdit(Map<String, Object> plan) {
                showAddEditPlanSheet(plan);
            }

            @Override
            public void onDelete(Map<String, Object> plan) {
                confirmDelete(plan);
            }
        });
        binding.rvAdminPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminPlans.setAdapter(adapter);
    }

    private void loadPlans() {
        db.collection("membership_plans")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        plansList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, Object> plan = doc.getData();
                            plan.put("id", doc.getId());
                            plansList.add(plan);
                        }
                        adapter.setPlans(plansList);
                    }
                });
    }

    private void showAddEditPlanSheet(@Nullable Map<String, Object> plan) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_add_edit_plan_sheet, null);
        dialog.setContentView(view);

        TextInputEditText etName = view.findViewById(R.id.etPlanName);
        TextInputEditText etPrice = view.findViewById(R.id.etPlanPrice);
        TextInputEditText etFeatures = view.findViewById(R.id.etPlanFeatures);
        MaterialButton btnSave = view.findViewById(R.id.btnSavePlan);

        if (plan != null) {
            etName.setText((String) plan.get("name"));
            etName.setEnabled(false);
            etPrice.setText((String) plan.get("price"));
            etFeatures.setText((String) plan.get("features"));
            btnSave.setText("UPDATE GLOBAL PLAN");
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String price = etPrice.getText().toString().trim();
            String features = etFeatures.getText().toString().trim();

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(getContext(), "Name and Price are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> newPlan = new HashMap<>();
            newPlan.put("name", name);
            newPlan.put("price", price);
            newPlan.put("features", features);

            db.collection("membership_plans").document(name).set(newPlan)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Membership Plan Saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void confirmDelete(Map<String, Object> plan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Plan")
                .setMessage("Delete " + plan.get("name") + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("membership_plans").document((String) plan.get("name")).delete();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
