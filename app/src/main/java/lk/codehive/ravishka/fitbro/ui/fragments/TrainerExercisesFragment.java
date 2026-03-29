package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentTrainerExercisesBinding;
import lk.codehive.ravishka.fitbro.models.Exercise;
import lk.codehive.ravishka.fitbro.ui.adapters.TrainingAdapter;

public class TrainerExercisesFragment extends Fragment {

    private static final String TAG = "TrainerExFragment";
    private FragmentTrainerExercisesBinding binding;
    private FirebaseFirestore db;
    private final List<Exercise> exerciseList = new ArrayList<>();
    private TrainingAdapter adapter;
    private ListenerRegistration exListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrainerExercisesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        startRealtimeListener();

        binding.fabAddExercise.setOnClickListener(v -> showAddEditExerciseSheet(null));
    }

    private void setupRecyclerView() {
        adapter = new TrainingAdapter(exerciseList) {
            @Override
            public void onBindViewHolder(@NonNull TrainingViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Exercise exercise = exerciseList.get(position);
                
                holder.binding.tvExerciseDetails.setVisibility(View.VISIBLE);
                holder.binding.tvExerciseDetails.setText(exercise.getMuscleGroup());
                holder.binding.tvRestDetails.setText("Tap to Manage");
                
                holder.itemView.setOnClickListener(v -> showActionOptions(exercise));
            }
        };
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvExercises.setAdapter(adapter);
    }

    private void startRealtimeListener() {
        if (exListener != null) exListener.remove();
        
        exListener = db.collection("exercises").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Exercise sync error", error);
                return;
            }
            if (value != null && binding != null) {
                exerciseList.clear();
                for (QueryDocumentSnapshot document : value) {
                    exerciseList.add(new Exercise(
                            document.getString("name"),
                            document.getString("muscleGroup"),
                            0, "", "", 
                            document.getString("imageUrl")
                    ));
                }
                adapter.setExercises(exerciseList);
            }
        });
    }

    private void showActionOptions(Exercise exercise) {
        String[] options = {"Edit Movement", "Delete from Library"};
        new AlertDialog.Builder(requireContext())
                .setTitle(exercise.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddEditExerciseSheet(exercise);
                    } else {
                        confirmDelete(exercise);
                    }
                })
                .show();
    }

    private void confirmDelete(Exercise exercise) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Exercise")
                .setMessage("Remove '" + exercise.getName() + "'? This will not affect existing workout plans.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("exercises").document(exercise.getName()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Removed from library", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddEditExerciseSheet(@Nullable Exercise exercise) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_add_edit_exercise_sheet, null);
        dialog.setContentView(view);

        TextInputEditText etName = view.findViewById(R.id.etExName);
        AutoCompleteTextView spinnerMuscle = view.findViewById(R.id.spinnerMuscleGroup);
        TextInputEditText etImage = view.findViewById(R.id.etImageUrl);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveExercise);

        String[] muscleGroups = {"Chest", "Back", "Legs", "Shoulders", "Arms", "Abs", "Cardio"};
        spinnerMuscle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, muscleGroups));

        if (exercise != null) {
            etName.setText(exercise.getName());
            etName.setEnabled(false); 
            spinnerMuscle.setText(exercise.getMuscleGroup(), false);
            etImage.setText(exercise.getImageUrl());
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String muscle = spinnerMuscle.getText().toString();
            String imageUrl = etImage.getText().toString().trim();

            if (name.isEmpty()) return;

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("muscleGroup", muscle);
            data.put("imageUrl", imageUrl);

            db.collection("exercises").document(name).set(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Exercise library updated!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exListener != null) exListener.remove();
        binding = null;
    }
}
