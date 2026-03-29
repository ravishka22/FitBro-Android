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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import lk.codehive.ravishka.fitbro.databinding.FragmentTrainerGymsBinding;
import lk.codehive.ravishka.fitbro.models.Gym;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainerGymsAdapter;

public class TrainerGymsFragment extends Fragment implements OnMapReadyCallback {

    private FragmentTrainerGymsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Gym> gymList = new ArrayList<>();
    private TrainerGymsAdapter adapter;
    private GoogleMap googleMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrainerGymsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        setupRecyclerView();
        loadGyms();

        binding.fabAddGym.setOnClickListener(v -> showAddEditGymSheet(null));
    }

    private void setupRecyclerView() {
        adapter = new TrainerGymsAdapter(gymList, auth.getUid(), new TrainerGymsAdapter.OnGymActionListener() {
            @Override
            public void onEdit(Gym gym) {
                // Double check ownership before allowing edit UI
                if (gym.getCreatorId() != null && gym.getCreatorId().equals(auth.getUid())) {
                    showAddEditGymSheet(gym);
                } else {
                    Toast.makeText(getContext(), "You can only edit gyms you added.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDelete(Gym gym) {
                // Double check ownership before allowing delete
                if (gym.getCreatorId() != null && gym.getCreatorId().equals(auth.getUid())) {
                    confirmDelete(gym);
                } else {
                    Toast.makeText(getContext(), "You can only delete gyms you added.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvGyms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGyms.setAdapter(adapter);
    }

    private void loadGyms() {
        db.collection("gyms")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        gymList.clear();
                        if (googleMap != null) googleMap.clear();
                        
                        for (QueryDocumentSnapshot doc : value) {
                            Gym gym = doc.toObject(Gym.class);
                            if (gym.getId() == null) gym.setId(doc.getId());
                            gymList.add(gym);
                            
                            if (googleMap != null) {
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(gym.getLatitude(), gym.getLongitude()))
                                        .title(gym.getName()));
                            }
                        }
                        adapter.setGyms(gymList);
                        
                        if (!gymList.isEmpty() && googleMap != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(gymList.get(0).getLatitude(), gymList.get(0).getLongitude()), 10f));
                        }
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        // Refresh markers if data already loaded
        if (!gymList.isEmpty()) {
            for (Gym gym : gymList) {
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(gym.getLatitude(), gym.getLongitude()))
                        .title(gym.getName()));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(gymList.get(0).getLatitude(), gymList.get(0).getLongitude()), 10f));
        }
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
            Gym newGym = new Gym(id, name, address, phone, Double.parseDouble(latStr), Double.parseDouble(lonStr), auth.getUid());

            db.collection("gyms").document(id)
                    .set(newGym)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Gym saved successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void confirmDelete(Gym gym) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Gym")
                .setMessage("Are you sure you want to delete '" + gym.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("gyms").document(gym.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Gym deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override public void onResume() { super.onResume(); binding.mapView.onResume(); }
    @Override public void onPause() { super.onPause(); binding.mapView.onPause(); }
    @Override public void onDestroyView() { binding.mapView.onDestroy(); super.onDestroyView(); binding = null; }
    @Override public void onLowMemory() { super.onLowMemory(); binding.mapView.onLowMemory(); }
}
