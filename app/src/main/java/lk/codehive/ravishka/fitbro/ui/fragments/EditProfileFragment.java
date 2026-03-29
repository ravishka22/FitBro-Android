package lk.codehive.ravishka.fitbro.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentEditProfileBinding;
import lk.codehive.ravishka.fitbro.models.User;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";
    private FragmentEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private User currentUser;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (binding != null) {
                        Glide.with(this).load(uri).into(binding.ivEditProfileImage);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadCurrentProfile();

        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
        binding.btnChangeImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void loadCurrentProfile() {
        FirebaseUser authUser = mAuth.getCurrentUser();
        if (authUser != null) {
            db.collection("users").document(authUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (binding == null) return;
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            binding.etFullName.setText(currentUser.getFullName());
                            binding.etWeight.setText(currentUser.getWeight());
                            binding.etHeight.setText(currentUser.getHeight());
                            binding.etTargetWeight.setText(currentUser.getTargetWeight());
                            binding.etAge.setText(currentUser.getAge());
                            
                            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                                Glide.with(this).load(currentUser.getPhotoUrl())
                                        .placeholder(R.drawable.logo_dark)
                                        .into(binding.ivEditProfileImage);
                            }
                        } else {
                            binding.etFullName.setText(authUser.getDisplayName());
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading profile", e));
        }
    }

    private void saveProfile() {
        if (binding == null) return;
        String name = binding.etFullName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilFullName.setError("Name is required");
            return;
        }

        binding.btnSaveProfile.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(name);
        } else {
            updateProfileInfo(name, currentUser != null ? currentUser.getPhotoUrl() : null);
        }
    }

    private void uploadImageAndSaveProfile(String name) {
        FirebaseUser authUser = mAuth.getCurrentUser();
        if (authUser == null) return;

        Log.d(TAG, "Starting Cloudinary image upload for UID: " + authUser.getUid());
        
        MediaManager.get().upload(selectedImageUri)
                .option("upload_preset", "fitbro")
                .option("folder", "profile_pics")
                .option("public_id", authUser.getUid())
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String photoUrl = (String) resultData.get("secure_url");
                        updateProfileInfo(name, photoUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (binding != null) binding.btnSaveProfile.setEnabled(true);
                        showToast("Upload failed: " + error.getDescription());
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void updateProfileInfo(String name, String photoUrl) {
        FirebaseUser authUser = mAuth.getCurrentUser();
        if (authUser == null) return;

        UserProfileChangeRequest.Builder profileBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);
        
        if (photoUrl != null) {
            profileBuilder.setPhotoUri(Uri.parse(photoUrl));
        }
        
        UserProfileChangeRequest profileUpdates = profileBuilder.build();

        authUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (currentUser == null) {
                    currentUser = new User(authUser.getUid(), name, authUser.getEmail(), "member");
                }
                
                if (binding != null) {
                    currentUser.setFullName(name);
                    currentUser.setWeight(binding.etWeight.getText().toString().trim());
                    currentUser.setHeight(binding.etHeight.getText().toString().trim());
                    currentUser.setTargetWeight(binding.etTargetWeight.getText().toString().trim());
                    currentUser.setAge(binding.etAge.getText().toString().trim());
                }
                if (photoUrl != null) currentUser.setPhotoUrl(photoUrl);

                db.collection("users").document(authUser.getUid())
                        .set(currentUser, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            showToast("Profile updated successfully!");
                            if (isAdded()) {
                                getParentFragmentManager().popBackStack();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (binding != null) binding.btnSaveProfile.setEnabled(true);
                            showToast("Firestore error: " + e.getMessage());
                        });
            } else {
                if (binding != null) binding.btnSaveProfile.setEnabled(true);
                showToast("Auth update failed");
            }
        });
    }

    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
