package lk.codehive.ravishka.fitbro.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.codehive.ravishka.fitbro.databinding.FragmentMapBinding;
import lk.codehive.ravishka.fitbro.models.Gym;
import lk.codehive.ravishka.fitbro.ui.adapters.UserGymAdapter;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private final List<Gym> gymList = new ArrayList<>();
    private final Map<String, Float> distanceMap = new HashMap<>();
    private UserGymAdapter gymAdapter;
    private Location lastKnownLocation;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    enableMyLocation();
                    fetchCurrentLocationAndLoadGyms();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (binding != null) {
            binding.mapView.onCreate(savedInstanceState);
            binding.mapView.getMapAsync(this);
            setupRecyclerView();
        }
    }

    private void setupRecyclerView() {
        gymAdapter = new UserGymAdapter(gymList, new UserGymAdapter.OnGymClickListener() {
            @Override
            public void onGymClick(Gym gym) {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(gym.getLatitude(), gym.getLongitude()), 15f));
                }
            }

            @Override
            public void onCallClick(Gym gym) {
                if (gym.getPhoneNumber() != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + gym.getPhoneNumber()));
                    startActivity(intent);
                }
            }

            @Override
            public void onDirectionsClick(Gym gym) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + gym.getLatitude() + "," + gym.getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + gym.getLatitude() + "," + gym.getLongitude())));
                }
            }
        });
        binding.rvGyms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGyms.setAdapter(gymAdapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            fetchCurrentLocationAndLoadGyms();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void enableMyLocation() {
        if (googleMap != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private void fetchCurrentLocationAndLoadGyms() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                lastKnownLocation = location;
                if (location != null && googleMap != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 12f));
                }
                loadGymsAndCalculateDistances();
            });
        }
    }

    private void loadGymsAndCalculateDistances() {
        db.collection("gyms").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            
            if (googleMap != null) googleMap.clear();
            gymList.clear();
            distanceMap.clear();

            for (QueryDocumentSnapshot doc : value) {
                Gym gym = doc.toObject(Gym.class);
                if (gym.getId() == null) gym.setId(doc.getId());
                
                if (lastKnownLocation != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                            gym.getLatitude(), gym.getLongitude(), results);
                    distanceMap.put(gym.getId(), results[0]);
                } else {
                    distanceMap.put(gym.getId(), Float.MAX_VALUE);
                }
                
                gymList.add(gym);
                
                if (googleMap != null) {
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(gym.getLatitude(), gym.getLongitude()))
                            .title(gym.getName()));
                    if (marker != null) marker.setTag(gym);
                }
            }

            // Sort by distance (Nearest First)
            Collections.sort(gymList, (g1, g2) -> {
                Float d1 = distanceMap.get(g1.getId());
                Float d2 = distanceMap.get(g2.getId());
                return d1.compareTo(d2);
            });

            // Pass both sorted list and distance map to adapter
            gymAdapter.setData(gymList, distanceMap);
        });
    }

    @Override public void onResume() { super.onResume(); if (binding != null) binding.mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if (binding != null) binding.mapView.onPause(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (binding != null) binding.mapView.onLowMemory(); }
    @Override public void onDestroyView() { if (binding != null) binding.mapView.onDestroy(); super.onDestroyView(); binding = null; }
}
