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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentCreatePlanBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.models.WorkoutDay;
import lk.codehive.ravishka.fitbro.models.WorkoutPlan;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainingAdapter;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class CreatePlanFragment extends Fragment {

    private FragmentCreatePlanBinding binding;
    private WorkoutViewModel viewModel;
    
    private final List<WorkoutDay> tempDays = new ArrayList<>();
    private int selectedDayIndex = 0;
    
    private final List<Exercise> exerciseLibrary = new ArrayList<>();
    private TrainingAdapter currentDayAdapter;

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
        
        loadExerciseLibrary();
        setupDropdowns();
        setupDayTabs(3); 
        setupRecyclerView();
        setupListeners();
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
        tempDays.clear();
        for (int i = 1; i <= count; i++) {
            binding.tabLayoutDays.addTab(binding.tabLayoutDays.newTab().setText("Day " + i));
            tempDays.add(new WorkoutDay("Day " + i, new ArrayList<>()));
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

    private void setupRecyclerView() {
        currentDayAdapter = new TrainingAdapter(new ArrayList<>());
        binding.rvSelectedExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSelectedExercises.setAdapter(currentDayAdapter);
    }

    private void updateExerciseList() {
        if (selectedDayIndex < tempDays.size()) {
            currentDayAdapter = new TrainingAdapter(tempDays.get(selectedDayIndex).getExercises());
            binding.rvSelectedExercises.setAdapter(currentDayAdapter);
        }
    }

    private void setupListeners() {
        binding.btnAddExercise.setOnClickListener(v -> showAddExerciseSheet());
        binding.btnSavePlan.setOnClickListener(v -> savePlan());
    }

    private void showAddExerciseSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_add_exercise_sheet, null);
        dialog.setContentView(view);

        // Ensure the dialog expands fully
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        EditText etSearch = view.findViewById(R.id.etSearch);
        ChipGroup cgFilters = view.findViewById(R.id.cgExFilters);
        androidx.recyclerview.widget.RecyclerView rvLibrary = view.findViewById(R.id.rvExLibrary);

        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        
        updateLibraryList(rvLibrary, "", "All", dialog);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filter = "All";
                int checkedId = cgFilters.getCheckedChipId();
                if (checkedId != View.NO_ID) {
                    Chip chip = cgFilters.findViewById(checkedId);
                    if (chip != null) filter = chip.getText().toString();
                }
                updateLibraryList(rvLibrary, s.toString(), filter, dialog);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String filter = "All";
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) filter = chip.getText().toString();
            }
            updateLibraryList(rvLibrary, etSearch.getText().toString(), filter, dialog);
        });

        dialog.show();
    }

    private void updateLibraryList(androidx.recyclerview.widget.RecyclerView rv, String query, String category, BottomSheetDialog dialog) {
        List<Exercise> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Exercise ex : exerciseLibrary) {
            boolean matchesCategory = category.equals("All") || ex.getMuscleGroup().equalsIgnoreCase(category);
            boolean matchesQuery = lowerQuery.isEmpty() || ex.getName().toLowerCase().contains(lowerQuery);
            if (matchesCategory && matchesQuery) {
                filtered.add(ex);
            }
        }

        rv.setAdapter(new TrainingAdapter(filtered) {
            @Override
            public void onBindViewHolder(@NonNull TrainingAdapter.TrainingViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Exercise item = filtered.get(position);
                
                // Hide dynamic details in library mode
                holder.binding.tvExerciseDetails.setVisibility(View.GONE);
                holder.binding.tvRestDetails.setText(item.getMuscleGroup());
                holder.binding.tvRestDetails.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(v -> showSetsRepsDialog(item, dialog));
            }
        });
    }

    private void showSetsRepsDialog(Exercise baseEx, BottomSheetDialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Target for " + baseEx.getName());
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 48, 64, 16);

        final EditText inputSets = new EditText(getContext());
        inputSets.setHint("Sets (e.g. 3)");
        inputSets.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputSets.setText("3");
        layout.addView(inputSets);

        final EditText inputReps = new EditText(getContext());
        inputReps.setHint("Reps (e.g. 12)");
        inputReps.setText("12");
        layout.addView(inputReps);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String setsStr = inputSets.getText().toString();
            String repsStr = inputReps.getText().toString();
            
            Exercise newEx = new Exercise(
                    baseEx.getName(),
                    baseEx.getMuscleGroup(),
                    setsStr.isEmpty() ? 3 : Integer.parseInt(setsStr),
                    repsStr.isEmpty() ? "12" : repsStr,
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
        String name = binding.etPlanName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter plan name", Toast.LENGTH_SHORT).show();
            return;
        }

        WorkoutPlan plan = new WorkoutPlan(
                UUID.randomUUID().toString(),
                name,
                binding.spinnerLevel.getText().toString() + " • " + binding.etDuration.getText().toString() + " weeks",
                new ArrayList<>(tempDays),
                false
        );

        viewModel.insert(plan);
        Toast.makeText(getContext(), "Plan Created Successfully!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void loadExerciseLibrary() {
        exerciseLibrary.clear();
        // Chest
        exerciseLibrary.add(new Exercise("Barbell Bench Press", "Chest", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Incline Dumbbell Press", "Chest", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Dumbbell Flys", "Chest", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Push-Ups", "Chest", 0, "", "", ""));
        // Legs
        exerciseLibrary.add(new Exercise("Barbell Squat", "Legs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Leg Extension", "Legs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Leg Press", "Legs", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Calf Raise", "Legs", 0, "", "", ""));
        // Back
        exerciseLibrary.add(new Exercise("Deadlift", "Back", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Barbell Row", "Back", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Pull-Up", "Back", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Lat Pulldown", "Back", 0, "", "", ""));
        // Shoulders
        exerciseLibrary.add(new Exercise("Military Press", "Shoulders", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Lateral Raise", "Shoulders", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Front Raise", "Shoulders", 0, "", "", ""));
        // Arms
        exerciseLibrary.add(new Exercise("Bicep Curl", "Arms", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Tricep Dip", "Arms", 0, "", "", ""));
        exerciseLibrary.add(new Exercise("Hammer Curl", "Arms", 0, "", "", ""));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
