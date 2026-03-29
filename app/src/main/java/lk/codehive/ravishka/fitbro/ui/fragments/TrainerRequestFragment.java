package lk.codehive.ravishka.fitbro.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import lk.codehive.ravishka.fitbro.databinding.FragmentTrainerRequestBinding;
import lk.codehive.ravishka.fitbro.models.PlanRequest;
import lk.codehive.ravishka.fitbro.models.User;
import lk.codehive.ravishka.fitbro.utils.NotificationHelper;

public class TrainerRequestFragment extends Fragment {

    private static final String TAG = "TrainerRequestFragment";
    private FragmentTrainerRequestBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Uri frontUri, sideUri, backUri;
    private String activePhotoType = "";
    
    private final List<Map<String, String>> trainerList = new ArrayList<>();
    private String selectedTrainerId = "";
    private User currentUserProfile;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    if ("front".equals(activePhotoType)) {
                        frontUri = uri;
                        Glide.with(this).load(uri).into(binding.ivFront);
                    } else if ("side".equals(activePhotoType)) {
                        sideUri = uri;
                        Glide.with(this).load(uri).into(binding.ivSide);
                    } else if ("back".equals(activePhotoType)) {
                        backUri = uri;
                        Glide.with(this).load(uri).into(binding.ivBack);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrainerRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadCurrentUser();
        loadTrainers();
        setupPhotoListeners();
        binding.btnSubmitRequest.setOnClickListener(v -> validateAndSubmit());
    }

    private void loadCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                    .addOnSuccessListener(doc -> {
                        currentUserProfile = doc.toObject(User.class);
                    });
        }
    }

    private void loadTrainers() {
        db.collection("users")
                .whereEqualTo("role", "trainer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> trainerNames = new ArrayList<>();
                    trainerList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("fullName");
                        if (name == null || name.isEmpty()) name = doc.getString("email");
                        
                        Map<String, String> trainer = new HashMap<>();
                        trainer.put("id", id);
                        trainer.put("name", name);
                        trainerList.add(trainer);
                        trainerNames.add(name);
                    }
                    
                    if (isAdded() && getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, trainerNames);
                        binding.spinnerTrainer.setAdapter(adapter);
                        binding.spinnerTrainer.setOnItemClickListener((parent, view, position, id) -> {
                            selectedTrainerId = trainerList.get(position).get("id");
                        });
                    }
                });
    }

    private void setupPhotoListeners() {
        binding.llFrontPhoto.setOnClickListener(v -> {
            activePhotoType = "front";
            launchPicker();
        });
        binding.llSidePhoto.setOnClickListener(v -> {
            activePhotoType = "side";
            launchPicker();
        });
        binding.llBackPhoto.setOnClickListener(v -> {
            activePhotoType = "back";
            launchPicker();
        });
    }

    private void launchPicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void validateAndSubmit() {
        String goal = binding.etGoal.getText().toString().trim();
        String preferredDaysStr = binding.etDays.getText().toString().trim();

        if (selectedTrainerId.isEmpty()) {
            Toast.makeText(getContext(), "Please select a trainer", Toast.LENGTH_SHORT).show();
            return;
        }
        if (goal.isEmpty()) {
            binding.tilGoal.setError("Goal is required");
            return;
        }
        if (preferredDaysStr.isEmpty()) {
            binding.tilDays.setError("Required");
            return;
        }

        int days;
        try {
            days = Integer.parseInt(preferredDaysStr);
        } catch (NumberFormatException e) {
            binding.tilDays.setError("Enter a number");
            return;
        }

        binding.btnSubmitRequest.setEnabled(false);
        uploadPhotosAndSubmit(goal, binding.etMedical.getText().toString().trim(), days);
    }

    private void uploadPhotosAndSubmit(String goal, String medical, int days) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            binding.btnSubmitRequest.setEnabled(true);
            return;
        }

        List<Uri> photosToUpload = new ArrayList<>();
        if (frontUri != null) photosToUpload.add(frontUri);
        if (sideUri != null) photosToUpload.add(sideUri);
        if (backUri != null) photosToUpload.add(backUri);

        if (photosToUpload.isEmpty()) {
            saveToFirestore(goal, medical, days, new ArrayList<>());
            return;
        }

        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        int total = photosToUpload.size();

        for (Uri uri : photosToUpload) {
            String publicId = "request_" + user.getUid() + "_" + System.currentTimeMillis() + "_" + counter.get();
            MediaManager.get().upload(uri)
                    .option("upload_preset", "fitbro")
                    .option("folder", "trainer_requests")
                    .option("public_id", publicId)
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) {}
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            uploadedUrls.add((String) resultData.get("secure_url"));
                            if (counter.incrementAndGet() == total) {
                                saveToFirestore(goal, medical, days, uploadedUrls);
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Upload error: " + error.getDescription());
                            if (counter.incrementAndGet() == total) {
                                if (uploadedUrls.isEmpty()) {
                                    binding.btnSubmitRequest.setEnabled(true);
                                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                                } else {
                                    saveToFirestore(goal, medical, days, uploadedUrls);
                                }
                            }
                        }

                        @Override public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        }
    }

    private void saveToFirestore(String goal, String medical, int days, List<String> urls) {
        FirebaseUser authUser = mAuth.getCurrentUser();
        if (authUser == null) return;

        String requestId = UUID.randomUUID().toString();
        String name = (currentUserProfile != null) ? currentUserProfile.getFullName() : authUser.getDisplayName();
        String photoUrl = (currentUserProfile != null) ? currentUserProfile.getPhotoUrl() : null;

        if (name == null || name.isEmpty()) name = "Athlete";
        
        final String finalName = name;
        final String trainerId = selectedTrainerId;

        PlanRequest request = new PlanRequest(
                requestId,
                authUser.getUid(),
                finalName,
                photoUrl,
                trainerId,
                goal,
                medical,
                days,
                "pending",
                urls,
                System.currentTimeMillis()
        );

        db.collection("trainer_requests").document(requestId)
                .set(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Request sent to trainer!", Toast.LENGTH_LONG).show();
                    
                    // Trigger Push Notification for Trainer
                    NotificationHelper.triggerNotification(
                            trainerId,
                            "New Training Request! 🏋️‍♂️",
                            finalName + " has requested a personalized workout plan.",
                            "new_request"
                    );

                    if (isAdded()) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore Save Error", e);
                    binding.btnSubmitRequest.setEnabled(true);
                    Toast.makeText(getContext(), "Error sending request", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
