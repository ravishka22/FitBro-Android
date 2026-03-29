package lk.codehive.ravishka.fitbro.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentProfileBinding;
import lk.codehive.ravishka.fitbro.models.User;
import lk.codehive.ravishka.fitbro.ui.AdminActivity;
import lk.codehive.ravishka.fitbro.ui.SplashActivity;
import lk.codehive.ravishka.fitbro.ui.TrainerActivity;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;
    private SharedPreferences sharedPreferences;
    private User currentUserProfile;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uploadProfilePicture(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        
        setupInitialPreferences();
        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningForUserData();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void setupInitialPreferences() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        binding.switchDarkMode.setChecked(isDarkMode);
    }

    private void startListeningForUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userListener = db.collection("users").document(user.getUid())
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.e(TAG, "UserData Sync Error", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            currentUserProfile = documentSnapshot.toObject(User.class);
                            updateUI(documentSnapshot, user);
                        }
                    });
        }
    }

    private void updateUI(DocumentSnapshot doc, FirebaseUser authUser) {
        if (binding == null) return;

        String name = doc.getString("fullName");
        if (name == null || name.isEmpty()) name = authUser.getDisplayName();
        if (name == null || name.isEmpty()) name = "FitBro User";
        
        binding.tvUserName.setText(name);
        binding.tvUserEmail.setText(authUser.getEmail());
        
        // Image logic with cache busting
        String photoUrl = doc.getString("photoUrl");
        if (photoUrl != null && !photoUrl.isEmpty()) {
            binding.tvProfilePlaceholder.setVisibility(View.GONE);
            binding.ivProfileImage.setVisibility(View.VISIBLE);
            
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.logo_dark)
                    .signature(new ObjectKey(photoUrl)) 
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(binding.ivProfileImage);
        } else {
            binding.tvProfilePlaceholder.setVisibility(View.VISIBLE);
            binding.ivProfileImage.setVisibility(View.GONE);
            binding.tvProfilePlaceholder.setText(name.substring(0, 1).toUpperCase());
        }

        // Membership Badge
        String role = doc.getString("role");
        boolean isPro = Boolean.TRUE.equals(doc.getBoolean("isPro"));
        
        if ("trainer".equalsIgnoreCase(role)) {
            binding.chipMembership.setText("OFFICIAL TRAINER");
            binding.chipMembership.setChipBackgroundColorResource(R.color.electric_green);
        } else if (isPro) {
            binding.chipMembership.setText("PRO MEMBER");
            binding.chipMembership.setChipBackgroundColorResource(R.color.electric_green);
        } else {
            binding.chipMembership.setText("FREE MEMBER");
            binding.chipMembership.setChipBackgroundColorResource(R.color.text_secondary);
        }
    }

    private void setupListeners() {
        binding.btnChangePhoto.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
        
        binding.btnUpgrade.setOnClickListener(v -> showMembershipPlansSheet());

        binding.btnResetPassword.setOnClickListener(v -> resetPassword());
        
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnLogout.setOnClickListener(v -> confirmLogout());
        
        binding.btnDeleteAccount.setOnClickListener(v -> confirmAccountDeletion());
    }

    private void uploadProfilePicture(Uri uri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        showToast("Uploading photo...");
        
        MediaManager.get().upload(uri)
                .option("upload_preset", "fitbro")
                .option("folder", "profile_pics")
                .option("public_id", user.getUid())
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String photoUrl = (String) resultData.get("secure_url");
                        Object version = resultData.get("version");
                        String versionedUrl = photoUrl + "?v=" + (version != null ? version.toString() : System.currentTimeMillis());

                        db.collection("users").document(user.getUid())
                                .update("photoUrl", versionedUrl)
                                .addOnSuccessListener(aVoid -> showToast("Photo updated!"))
                                .addOnFailureListener(e -> showToast("Failed to update profile data"));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        showToast("Upload failed: " + error.getDescription());
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void navigateToEditProfile() {
        int containerId = R.id.nav_host_fragment;
        if (getActivity() instanceof TrainerActivity) {
            containerId = R.id.trainer_nav_host_fragment;
        } else if (getActivity() instanceof AdminActivity) {
            containerId = R.id.admin_nav_host_fragment;
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(containerId, new EditProfileFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showMembershipPlansSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_membership_plans_sheet, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnSelectFree).setOnClickListener(v -> {
            updateUserPlan("member", false);
            dialog.dismiss();
        });

        view.findViewById(R.id.btnSelectPro).setOnClickListener(v -> {
            updateUserPlan("member", true);
            dialog.dismiss();
        });

        view.findViewById(R.id.btnSelectTrainer).setOnClickListener(v -> {
            updateUserPlan("trainer", true);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateUserPlan(String role, boolean isPro) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", role);
        updates.put("isPro", isPro);

        db.collection("users").document(mAuth.getUid()).set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    showToast("Plan updated");
                    Intent intent = new Intent(getContext(), SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (d, w) -> {
                    mAuth.signOut();
                    navigateToSplash();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmAccountDeletion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("WARNING: This will permanently delete your account, workout plans, and all associated data. This action cannot be undone.")
                .setPositiveButton("DELETE PERMANENTLY", (d, w) -> deleteAccountAndData())
                .setNegativeButton("CANCEL", null)
                .setCancelable(false)
                .show();
    }

    private void deleteAccountAndData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        WriteBatch batch = db.batch();

        db.collection("workout_plans").whereEqualTo("targetUserId", uid).get()
            .addOnSuccessListener(plans -> {
                for (DocumentSnapshot doc : plans) batch.delete(doc.getReference());
                
                db.collection("trainer_requests").whereEqualTo("userId", uid).get()
                    .addOnSuccessListener(requests -> {
                        for (DocumentSnapshot doc : requests) batch.delete(doc.getReference());
                        
                        db.collection("sessions").whereEqualTo("userId", uid).get()
                            .addOnSuccessListener(sessions -> {
                                for (DocumentSnapshot doc : sessions) batch.delete(doc.getReference());
                                
                                db.collection("gyms").whereEqualTo("creatorId", uid).get()
                                    .addOnSuccessListener(gyms -> {
                                        for (DocumentSnapshot doc : gyms) batch.delete(doc.getReference());
                                        
                                        db.collection("users").document(uid).collection("payments").get()
                                            .addOnSuccessListener(payments -> {
                                                for (DocumentSnapshot doc : payments) batch.delete(doc.getReference());
                                                
                                                batch.delete(db.collection("users").document(uid));
                                                executeAccountDeletion(user, batch);
                                            });
                                    });
                            });
                    });
            });
    }

    private void executeAccountDeletion(FirebaseUser user, WriteBatch batch) {
        batch.commit().addOnSuccessListener(aVoid -> {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToast("Account and all data deleted");
                    navigateToSplash();
                } else {
                    Log.e(TAG, "Auth deletion failed", task.getException());
                    showToast("Security: Please re-login to delete account.");
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Batch deletion failed", e);
            showToast("Error deleting data");
        });
    }

    private void resetPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            mAuth.sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(aVoid -> showToast("Reset link sent to " + user.getEmail()));
        }
    }

    private void navigateToSplash() {
        Intent intent = new Intent(getContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
