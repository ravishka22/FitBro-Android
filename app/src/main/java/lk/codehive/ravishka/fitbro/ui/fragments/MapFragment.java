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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.FragmentMapBinding;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;

    private static class Gym {
        String name;
        String address;
        String phoneNumber;
        LatLng location;

        Gym(String name, String address, String phoneNumber, LatLng location) {
            this.name = name;
            this.address = address;
            this.phoneNumber = phoneNumber;
            this.location = location;
        }
    }

    private List<Gym> mockGyms;
    private Gym selectedGym;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    enableMyLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
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

        initMockGyms();

        if (binding != null) {
            binding.mapView.onCreate(savedInstanceState);
            binding.mapView.getMapAsync(this);

            binding.btnCallGym.setOnClickListener(v -> {
                if (selectedGym != null && selectedGym.phoneNumber != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + selectedGym.phoneNumber));
                    startActivity(intent);
                }
            });
            
            binding.cardGymInfo.setVisibility(View.GONE);
        }
    }

    private void initMockGyms() {
        mockGyms = new ArrayList<>();
        mockGyms.add(new Gym("FitBro Central Gym", "123 Fitness Ave, Colombo", "0112345678", new LatLng(6.9271, 79.8612)));
        mockGyms.add(new Gym("Power House Gym", "45 Galle Road, Colombo 03", "0112345679", new LatLng(6.9145, 79.8510)));
        mockGyms.add(new Gym("Iron Paradise", "12 Havelock Road, Colombo 05", "0112345680", new LatLng(6.8910, 79.8650)));
        mockGyms.add(new Gym("Urban Fitness", "78 Ward Place, Colombo 07", "0112345681", new LatLng(6.9180, 79.8720)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        for (Gym gym : mockGyms) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(gym.location)
                    .title(gym.name));
            if (marker != null) {
                marker.setTag(gym);
            }
        }

        googleMap.setOnMarkerClickListener(marker -> {
            Gym gym = (Gym) marker.getTag();
            if (gym != null) {
                selectedGym = gym;
                showGymDetails(gym);
            }
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> {
            binding.cardGymInfo.setVisibility(View.GONE);
            selectedGym = null;
        });

        if (!mockGyms.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mockGyms.get(0).location, 13f));
        }

        checkLocationPermissions();
    }

    private void showGymDetails(Gym gym) {
        binding.tvGymName.setText(gym.name);
        binding.tvGymAddress.setText(gym.address);
        binding.cardGymInfo.setVisibility(View.VISIBLE);
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        if (googleMap != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
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