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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentAdminGymsBinding;
import lk.codehive.ravishka.fitbro.models.Gym;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainerGymsAdapter;

public class AdminGymsFragment extends Fragment {

    private FragmentAdminGymsBinding binding;
    private FirebaseFirestore db;
    private List<Gym> gymList = new ArrayList<>();
    private TrainerGymsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminGymsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadGyms();

        binding.fabAdminAddGym.setOnClickListener(v -> showAddEditGymSheet(null));
    }

    private void setupRecyclerView() {
        // Pass "admin" as the user ID to bypass ownership checks in the adapter logic (we'll update adapter next)
        adapter = new TrainerGymsAdapter(gymList, "ADMIN_BYPASS", new TrainerGymsAdapter.OnGymActionListener() {
            @Override
            public void onEdit(Gym gym) {
                showAddEditGymSheet(gym);
            }

            @Override
            public void onDelete(Gym gym) {
                confirmDelete(gym);
            }
        });
        binding.rvAdminGyms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminGyms.setAdapter(adapter);
    }

    private void loadGyms() {
        db.collection("gyms").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                gymList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Gym gym = doc.toObject(Gym.class);
                    if (gym.getId() == null) gym.setId(doc.getId());
                    gymList.add(gym);
                }
                adapter.setGyms(gymList);
            }
        });
    }

    private void showAddEditGymSheet(@Nullable Gym gym) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_add_edit_gym_sheet, null);
        dialog.setContentView(view);

        TextInputEditText etName = view.findViewById(R.id.etGymName);
        TextInputEditText etAddress = view.findViewById(R.id.etGymAddress);
        TextInputEditText etPhone = view.findViewById(R.id.etGymPhone);
        TextInputEditText etLat = view.findViewById(R.id.etLatitude);
        TextInputEditText etLon = view.findViewById(R.id.etLongitude);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveGym);

        if (gym != null) {
            etName.setText(gym.getName());
            etAddress.setText(gym.getAddress());
            etPhone.setText(gym.getPhoneNumber());
            etLat.setText(String.valueOf(gym.getLatitude()));
            etLon.setText(String.valueOf(gym.getLongitude()));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String latStr = etLat.getText().toString().trim();
            String lonStr = etLon.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = (gym != null) ? gym.getId() : UUID.randomUUID().toString();
            String creatorId = (gym != null) ? gym.getCreatorId() : "admin";
            
            Gym newGym = new Gym(id, name, address, phone, Double.parseDouble(latStr), Double.parseDouble(lonStr), creatorId);

            db.collection("gyms").document(id).set(newGym)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Gym updated by Admin", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void confirmDelete(Gym gym) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Admin: Delete Gym")
                .setMessage("Permanently remove " + gym.getName() + " from the platform?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("gyms").document(gym.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Gym removed", Toast.LENGTH_SHORT).show());
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
