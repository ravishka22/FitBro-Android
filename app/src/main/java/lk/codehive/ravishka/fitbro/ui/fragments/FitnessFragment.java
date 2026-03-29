package lk.codehive.ravishka.fitbro.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentFitnessBinding;
import lk.codehive.ravishka.fitbro.models.WeightLog;
import lk.codehive.ravishka.fitbro.models.WorkoutSession;
import lk.codehive.ravishka.fitbro.ui.adapters.WeightHistoryAdapter;
import lk.codehive.ravishka.fitbro.ui.adapters.WorkoutSessionAdapter;
import lk.codehive.ravishka.fitbro.viewmodels.WorkoutViewModel;

public class FitnessFragment extends Fragment {

    private FragmentFitnessBinding binding;
    private WorkoutViewModel viewModel;
    private WorkoutSessionAdapter workoutAdapter;
    private WeightHistoryAdapter weightAdapter;
    private final List<WorkoutSession> allSessions = new ArrayList<>();
    private final List<WeightLog> allWeightLogs = new ArrayList<>();
    private long selectedDateMillis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFitnessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        selectedDateMillis = System.currentTimeMillis();
        
        setupTabs();
        setupRecyclerViews();
        setupCalendar();
        setupCharts();
        observeViewModel();

        binding.btnAddWeight.setOnClickListener(v -> handleWeightLogAttempt());
    }

    private void handleWeightLogAttempt() {
        if (hasAlreadyLoggedToday()) {
            Toast.makeText(getContext(), "You have already logged your weight today. Long-press an entry to edit.", Toast.LENGTH_LONG).show();
        } else {
            showWeightDialog(null);
        }
    }

    private boolean hasAlreadyLoggedToday() {
        Calendar today = Calendar.getInstance();
        for (WeightLog log : allWeightLogs) {
            Calendar logDate = Calendar.getInstance();
            logDate.setTimeInMillis(log.getTimestamp());
            if (today.get(Calendar.YEAR) == logDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == logDate.get(Calendar.DAY_OF_YEAR)) {
                return true;
            }
        }
        return false;
    }

    private void setupTabs() {
        binding.tabLayoutProgress.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.containerActivities.setVisibility(View.VISIBLE);
                    binding.containerWeight.setVisibility(View.GONE);
                } else {
                    binding.containerActivities.setVisibility(View.GONE);
                    binding.containerWeight.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerViews() {
        workoutAdapter = new WorkoutSessionAdapter(new ArrayList<>());
        binding.rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWorkoutHistory.setAdapter(workoutAdapter);

        weightAdapter = new WeightHistoryAdapter(new ArrayList<>(), new WeightHistoryAdapter.OnLogActionListener() {
            @Override
            public void onEdit(WeightLog log) {
                showManageLogOptions(log);
            }

            @Override
            public void onDelete(WeightLog log) {
                // Handled in dialog
            }
        });
        binding.rvWeightHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWeightHistory.setAdapter(weightAdapter);
    }

    private void showManageLogOptions(WeightLog log) {
        String[] options = {"Edit Value", "Delete Entry"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Manage Log")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showWeightDialog(log);
                    else confirmDeleteWeight(log);
                }).show();
    }

    private void confirmDeleteWeight(WeightLog log) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to remove this weight log?")
                .setPositiveButton("DELETE", (d, w) -> {
                    viewModel.deleteWeightLog(log);
                    Toast.makeText(getContext(), "Log deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void setupCalendar() {
        binding.calendarViewWorkouts.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = calendar.getTimeInMillis();
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.tvSelectedDateTitle.setText("Activity on " + sdf.format(new Date(selectedDateMillis)));
            
            filterSessionsByDate();
        });
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        binding.tvSelectedDateTitle.setText("Activity on " + sdf.format(new Date()));
    }

    private void setupCharts() {
        binding.lineChartWeight.getDescription().setEnabled(false);
        binding.lineChartWeight.getLegend().setEnabled(false);
        
        XAxis xAxis = binding.lineChartWeight.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Day " + (int) value;
            }
        });

        binding.lineChartWeight.getAxisLeft().setDrawGridLines(false);
        binding.lineChartWeight.getAxisLeft().setTextColor(Color.GRAY);
        binding.lineChartWeight.getAxisRight().setEnabled(false);
    }

    private void observeViewModel() {
        viewModel.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (sessions != null) {
                allSessions.clear();
                allSessions.addAll(sessions);
                filterSessionsByDate();
            }
        });

        viewModel.getWeightLogs().observe(getViewLifecycleOwner(), logs -> {
            if (logs != null) {
                allWeightLogs.clear();
                allWeightLogs.addAll(logs);
                weightAdapter.setLogs(logs);
                updateWeightChart(logs);
            }
        });
    }

    private void filterSessionsByDate() {
        List<WorkoutSession> filtered = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(selectedDateMillis);
        
        for (WorkoutSession session : allSessions) {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(session.getTimestamp());
            
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                filtered.add(session);
            }
        }
        workoutAdapter.setSessions(filtered);
    }

    private void updateWeightChart(List<WeightLog> logs) {
        if (logs.isEmpty()) {
            binding.lineChartWeight.clear();
            return;
        }
        
        List<Entry> entries = new ArrayList<>();
        Calendar currentMonthCal = Calendar.getInstance();
        int currentMonth = currentMonthCal.get(Calendar.MONTH);
        int currentYear = currentMonthCal.get(Calendar.YEAR);

        for (WeightLog log : logs) {
            Calendar logCal = Calendar.getInstance();
            logCal.setTimeInMillis(log.getTimestamp());
            if (logCal.get(Calendar.MONTH) == currentMonth && logCal.get(Calendar.YEAR) == currentYear) {
                entries.add(new Entry(logCal.get(Calendar.DAY_OF_MONTH), log.getWeight()));
            }
        }

        if (entries.isEmpty()) {
            binding.lineChartWeight.clear();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight");
        dataSet.setColor(getResources().getColor(R.color.electric_green_variant));
        dataSet.setCircleColor(getResources().getColor(R.color.electric_green));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.electric_green));
        dataSet.setFillAlpha(40);
        dataSet.setValueTextColor(Color.GRAY);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        binding.lineChartWeight.setData(lineData);
        binding.lineChartWeight.invalidate();
    }

    private void showWeightDialog(@Nullable WeightLog existingLog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(existingLog == null ? "Log Weight (kg)" : "Edit Weight (kg)");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("e.g. 75.5");
        if (existingLog != null) input.setText(String.valueOf(existingLog.getWeight()));
        builder.setView(input);

        builder.setPositiveButton(existingLog == null ? "LOG" : "UPDATE", (dialog, which) -> {
            String weightStr = input.getText().toString();
            if (!weightStr.isEmpty()) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    if (existingLog == null) {
                        viewModel.addWeightLog(weight);
                    } else {
                        existingLog.setWeight(weight);
                        viewModel.updateWeightLog(existingLog);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid weight format", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
