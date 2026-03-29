package lk.codehive.ravishka.fitbro.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.FragmentMapBinding;
import lk.codehive.ravishka.fitbro.models.Gym;
import lk.codehive.ravishka.fitbro.ui.adapters.UserGymAdapter;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private final List<Gym> gymList = new ArrayList<>();
    private UserGymAdapter gymAdapter;
    private Gym selectedGym;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    enableMyLocation();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (binding != null) {
            binding.mapView.onCreate(savedInstanceState);
            binding.mapView.getMapAsync(this);

            setupRecyclerView();

            binding.btnCallGym.setOnClickListener(v -> {
                if (selectedGym != null && selectedGym.getPhoneNumber() != null) {
                    callGym(selectedGym.getPhoneNumber());
                }
            });
            
            binding.cardGymInfo.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        gymAdapter = new UserGymAdapter(gymList, new UserGymAdapter.OnGymClickListener() {
            @Override
            public void onGymClick(Gym gym) {
                showGymDetails(gym);
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(gym.getLatitude(), gym.getLongitude()), 15f));
                }
            }

            @Override
            public void onCallClick(Gym gym) {
                callGym(gym.getPhoneNumber());
            }
        });
        binding.rvGyms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGyms.setAdapter(gymAdapter);
    }

    private void callGym(String phoneNumber) {
        if (phoneNumber != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        loadGymsFromFirestore();
        checkLocationPermissions();

        googleMap.setOnMarkerClickListener(marker -> {
            selectedGym = (Gym) marker.getTag();
            if (selectedGym != null) {
                showGymDetails(selectedGym);
            }
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> {
            binding.cardGymInfo.setVisibility(View.GONE);
            selectedGym = null;
        });
    }

    private void loadGymsFromFirestore() {
        db.collection("gyms").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            
            if (googleMap != null) googleMap.clear();
            gymList.clear();
            for (QueryDocumentSnapshot doc : value) {
                Gym gym = doc.toObject(Gym.class);
                gymList.add(gym);
                
                if (googleMap != null) {
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(gym.getLatitude(), gym.getLongitude()))
                            .title(gym.getName()));
                    if (marker != null) marker.setTag(gym);
                }
            }
            gymAdapter.setGyms(gymList);

            if (!gymList.isEmpty() && googleMap != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(gymList.get(0).getLatitude(), gymList.get(0).getLongitude()), 12f));
            }
        });
    }

    private void showGymDetails(Gym gym) {
        selectedGym = gym;
        binding.tvGymName.setText(gym.getName());
        binding.tvGymAddress.setText(gym.getAddress());
        binding.cardGymInfo.setVisibility(View.VISIBLE);
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void enableMyLocation() {
        if (googleMap != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) binding.mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null) binding.mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.mapView.onDestroy();
        super.onDestroyView();
        binding = null;
    }
}
