package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentCreatePlanBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutDay;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainingAdapter;
import lk.codehive.ravishka.fitbro.utils.NotificationHelper;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class CreatePlanFragment extends Fragment {

    private static final String TAG = "CreatePlanFragment";
    private FragmentCreatePlanBinding binding;
    private WorkoutViewModel viewModel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private final List<WorkoutDay> tempDays = new ArrayList<>();
    private int selectedDayIndex = 0;
    
    private final List<Exercise> exerciseLibrary = new ArrayList<>();
    private TrainingAdapter currentDayAdapter;

    private String targetUserId;
    private String requestId;
    private String editPlanId;
    private WorkoutPlan existingPlan;
    private String userRole = "member";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            targetUserId = getArguments().getString("USER_ID");
            requestId = getArguments().getString("REQUEST_ID");
            editPlanId = getArguments().getString("EDIT_PLAN_ID");
        }
        
        fetchUserRoleAndInitialize();
        loadExerciseLibrary();
        setupDropdowns();
        setupRecyclerView();
        setupListeners();

        if (editPlanId != null) {
            loadExistingPlan();
        } else {
            setupDayTabs(3); 
        }
    }

    private void fetchUserRoleAndInitialize() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists() && binding != null) {
                    userRole = doc.getString("role");
                    if (userRole == null) userRole = "member";
                    
                    if ("trainer".equalsIgnoreCase(userRole)) {
                        binding.switchSaveAsTemplate.setVisibility(View.VISIBLE);
                        if (targetUserId != null) {
                            binding.tvCreateTitle.setText("Build Custom Plan");
                            binding.btnSavePlan.setText("FINISH & SEND");
                            binding.switchSaveAsTemplate.setChecked(false);
                        } else {
                            binding.tvCreateTitle.setText("Build Workout Template");
                            binding.switchSaveAsTemplate.setChecked(true);
                        }
                    } else {
                        binding.tvCreateTitle.setText("My Custom Plan");
                        binding.switchSaveAsTemplate.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void loadExistingPlan() {
        db.collection("workout_plans").document(editPlanId).get().addOnSuccessListener(documentSnapshot -> {
            existingPlan = documentSnapshot.toObject(WorkoutPlan.class);
            if (existingPlan != null && binding != null) {
                binding.etPlanName.setText(existingPlan.getTitle());
                
                tempDays.clear();
                tempDays.addAll(existingPlan.getDays());
                
                binding.tabLayoutDays.removeAllTabs();
                for (int i = 0; i < tempDays.size(); i++) {
                    binding.tabLayoutDays.addTab(binding.tabLayoutDays.newTab().setText("Day " + (i + 1)));
                }
                
                selectedDayIndex = 0;
                updateExerciseList();
                
                binding.tabLayoutDays.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        selectedDayIndex = tab.getPosition();
                        updateExerciseList();
                    }
                    @Override public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override public void onTabReselected(TabLayout.Tab tab) {}
                });
            }
        });
    }

    private void setupDropdowns() {
        String[] levels = {"Beginner", "Intermediate", "Advanced", "Professional"};
        String[] goals = {"Build Muscle", "Lose Weight", "Increase Strength", "Endurance", "Competition Prep"};
        String[] daysOptions = {"1", "2", "3", "4", "5", "6", "7"};

        ((AutoCompleteTextView) binding.spinnerLevel).setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, levels));
        ((AutoCompleteTextView) binding.spinnerGoal).setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, goals));
        
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, daysOptions);
        ((AutoCompleteTextView) binding.spinnerDaysPerWeek).setAdapter(daysAdapter);
        ((AutoCompleteTextView) binding.spinnerDaysPerWeek).setOnItemClickListener((parent, view, position, id) -> {
            int days = Integer.parseInt(daysOptions[position]);
            setupDayTabs(days);
        });
    }

    private void setupDayTabs(int count) {
        binding.tabLayoutDays.removeAllTabs();
        
        List<WorkoutDay> newDays = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            binding.tabLayoutDays.addTab(binding.tabLayoutDays.newTab().setText("Day " + i));
            if (i <= tempDays.size()) {
                newDays.add(tempDays.get(i-1));
            } else {
                newDays.add(new WorkoutDay("Day " + i, new ArrayList<>()));
            }
        }
        tempDays.clear();
        tempDays.addAll(newDays);
        
        selectedDayIndex = 0;
        updateExerciseList();
        
        binding.tabLayoutDays.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedDayIndex = tab.getPosition();
                updateExerciseList();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        currentDayAdapter = new TrainingAdapter(new ArrayList<>());
        binding.rvSelectedExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSelectedExercises.setAdapter(currentDayAdapter);
    }

    private ItemTouchHelper currentSwipeHelper;

    private void updateExerciseList() {
        if (selectedDayIndex < tempDays.size()) {
            List<Exercise> currentExercises = tempDays.get(selectedDayIndex).getExercises();
            currentDayAdapter = new TrainingAdapter(currentExercises);
            binding.rvSelectedExercises.setAdapter(currentDayAdapter);

            if (currentSwipeHelper != null) {
                currentSwipeHelper.attachToRecyclerView(null);
            }

            ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < currentExercises.size()) {
                        Exercise removed = currentExercises.remove(position);
                        currentDayAdapter.notifyItemRemoved(position);
                        currentDayAdapter.notifyItemRangeChanged(position, currentExercises.size());
                        Toast.makeText(getContext(), removed.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            currentSwipeHelper = new ItemTouchHelper(swipeCallback);
            currentSwipeHelper.attachToRecyclerView(binding.rvSelectedExercises);
        }
    }

    private void setupListeners() {
        binding.btnAddExercise.setOnClickListener(v -> showAddExerciseSheet());
        binding.btnSavePlan.setOnClickListener(v -> savePlan());
    }

    private String getSelectedCategory(ChipGroup chipGroup) {
        if (chipGroup == null) return "All";
        int checkedId = chipGroup.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = chipGroup.findViewById(checkedId);
            if (chip != null && chip.getText() != null) {
                return chip.getText().toString();
            }
        }
        return "All";
    }

    private void showAddExerciseSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_add_exercise_sheet, null);
        dialog.setContentView(view);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        EditText etSearch = view.findViewById(R.id.etSearch);
        ChipGroup cgFilters = view.findViewById(R.id.cgExFilters);
        RecyclerView rvLibrary = view.findViewById(R.id.rvExLibrary);

        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        updateLibraryList(rvLibrary, "", getSelectedCategory(cgFilters), dialog);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLibraryList(rvLibrary, s.toString(), getSelectedCategory(cgFilters), dialog);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String category = getSelectedCategory(group);
            String query = etSearch.getText().toString();
            updateLibraryList(rvLibrary, query, category, dialog);
        });

        dialog.show();
    }

    private void updateLibraryList(RecyclerView rv, String query, String category, BottomSheetDialog dialog) {
        List<Exercise> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Exercise ex : exerciseLibrary) {
            String muscle = ex.getMuscleGroup() != null ? ex.getMuscleGroup() : "";
            boolean matchesCategory = category.equalsIgnoreCase("All")
                    || muscle.equalsIgnoreCase(category)
                    || muscle.toLowerCase().contains(category.toLowerCase());
            boolean matchesQuery = lowerQuery.isEmpty()
                    || ex.getName().toLowerCase().contains(lowerQuery)
                    || muscle.toLowerCase().contains(lowerQuery);

            if (matchesCategory && matchesQuery) {
                filtered.add(ex);
            }
        }

        rv.setAdapter(new TrainingAdapter(filtered) {
            @Override
            public void onBindViewHolder(@NonNull TrainingAdapter.TrainingViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Exercise item = filtered.get(position);
                holder.binding.tvExerciseDetails.setVisibility(View.GONE);
                holder.binding.tvRestDetails.setText(item.getMuscleGroup());
                holder.binding.tvRestDetails.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(v -> {
                    if (isExerciseAlreadyAdded(item.getName())) {
                        Toast.makeText(getContext(), "Exercise already added to this day", Toast.LENGTH_SHORT).show();
                    } else {
                        showSetsRepsDialog(item, dialog);
                    }
                });
            }
        });
    }

    private boolean isExerciseAlreadyAdded(String name) {
        if (selectedDayIndex >= tempDays.size()) return false;
        for (Exercise ex : tempDays.get(selectedDayIndex).getExercises()) {
            if (ex.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private void showSetsRepsDialog(Exercise baseEx, BottomSheetDialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Target for " + baseEx.getName());
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 48, 64, 16);

        final EditText inputSets = new EditText(getContext());
        inputSets.setHint("Sets");
        inputSets.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputSets.setText("3");
        layout.addView(inputSets);

        final EditText inputReps = new EditText(getContext());
        inputReps.setHint("Reps");
        inputReps.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputReps.setText("12");
        layout.addView(inputReps);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            Exercise newEx = new Exercise(
                    baseEx.getName(),
                    baseEx.getMuscleGroup(),
                    Integer.parseInt(inputSets.getText().toString()),
                    inputReps.getText().toString(),
                    "60s",
                    baseEx.getImageUrl()
            );
            tempDays.get(selectedDayIndex).getExercises().add(newEx);
            updateExerciseList();
            parentDialog.dismiss();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void savePlan() {
        String name = binding.etPlanName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Enter plan name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String planId = (editPlanId != null) ? editPlanId : UUID.randomUUID().toString();
        
        String finalTargetUserId = targetUserId;
        if (finalTargetUserId == null && "member".equalsIgnoreCase(userRole)) {
            finalTargetUserId = user.getUid();
        }

        boolean isTemplate = binding.switchSaveAsTemplate.isChecked();
        if ("trainer".equalsIgnoreCase(userRole) && !isTemplate && finalTargetUserId == null) {
            showMemberSelectionDialog(name);
            return;
        }

        executeSavePlan(planId, name, finalTargetUserId, isTemplate);
    }

    private void showMemberSelectionDialog(String planName) {
        db.collection("users").whereEqualTo("role", "member").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> names = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("fullName");
                if (name == null) name = doc.getString("email");
                names.add(name);
                ids.add(doc.getId());
            }
            
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Member to Assign")
                    .setItems(names.toArray(new String[0]), (dialog, which) -> {
                        executeSavePlan(UUID.randomUUID().toString(), planName, ids.get(which), false);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void executeSavePlan(String planId, String name, String targetId, boolean isTemplate) {
        FirebaseUser user = auth.getCurrentUser();
        
        WorkoutPlan plan = new WorkoutPlan(
                planId,
                name,
                binding.spinnerLevel.getText().toString() + " • " + binding.etDuration.getText().toString() + " weeks",
                new ArrayList<>(tempDays),
                (targetId != null) 
        );
        
        plan.setCreatorId(user.getUid());
        plan.setCreatorName(user.getDisplayName());
        plan.setTargetUserId(targetId);
        plan.setRequestId(requestId);
        
        long currentTime = System.currentTimeMillis();
        plan.setCreatedAt(currentTime);
        plan.setValidUntil(currentTime + (4L * 7L * 24L * 60L * 60L * 1000L));

        if (targetId != null) {
            // Assign to member: Deactivate other plans first
            db.collection("users").document(targetId).collection("plans")
                    .whereEqualTo("active", true)
                    .get()
                    .addOnSuccessListener(activePlans -> {
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : activePlans) {
                            batch.update(doc.getReference(), "active", false);
                        }
                        
                        // Save the new plan as active
                        plan.setActive(true);
                        batch.set(db.collection("users").document(targetId).collection("plans").document(planId), plan);
                        batch.set(db.collection("workout_plans").document(planId), plan);
                        
                        if (requestId != null) {
                            // Fulfillment: Mark request as 'completed'
                            batch.update(db.collection("trainer_requests").document(requestId), "status", "completed");
                        }
                        
                        batch.commit().addOnSuccessListener(aVoid -> {
                            // Trigger Notification for Member
                            NotificationHelper.triggerNotification(
                                    targetId,
                                    "Plan Ready! 💪",
                                    "Your coach has finished your custom workout plan. Check it out now!",
                                    "plan_ready"
                            );

                            if (targetId.equals(user.getUid())) {
                                viewModel.syncWithCloud();
                            }
                            Toast.makeText(getContext(), "Plan assigned successfully!", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        });
                    });
        } else {
            // Reusable Template
            db.collection("workout_plans").document(planId).set(plan)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Template saved to library!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
        }
    }

    private void loadExerciseLibrary() {
        exerciseLibrary.clear();
        populateDefaultExercises();

        db.collection("exercises").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                exerciseLibrary.clear();
                for (var doc : queryDocumentSnapshots) {
                    exerciseLibrary.add(new Exercise(doc.getString("name"), doc.getString("muscleGroup"), 0, "", "", doc.getString("imageUrl")));
                }
            }
        });
    }

    private void populateDefaultExercises() {
        exerciseLibrary.add(new Exercise("Bench Press", "Chest", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Incline Dumbbell Press", "Chest", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Chest Fly", "Chest", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Pull Ups", "Back", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Lat Pulldown", "Back", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Barbell Row", "Back", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Squats", "Legs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Leg Press", "Legs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Leg Extensions", "Legs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Overhead Press", "Shoulders", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Lateral Raises", "Shoulders", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Barbell Bicep Curl", "Arms", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Tricep Pushdown", "Arms", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Hammer Curls", "Arms", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Plank", "Abs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Treadmill Run", "Cardio", 0, "", "", ""));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
