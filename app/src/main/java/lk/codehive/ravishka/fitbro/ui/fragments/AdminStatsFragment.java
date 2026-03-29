package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import lk.codehive.ravishka.fitbro.databinding.FragmentAdminStatsBinding;
import lk.codehive.ravishka.fitbro.models.Payment;
import lk.codehive.ravishka.fitbro.ui.adapters.PaymentHistoryAdapter;

public class AdminStatsFragment extends Fragment {

    private FragmentAdminStatsBinding binding;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        fetchGlobalStats();
        setupRecentTransactions();
    }

    private void fetchGlobalStats() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int total = queryDocumentSnapshots.size();
            long trainers = queryDocumentSnapshots.getDocuments().stream()
                    .filter(doc -> "trainer".equals(doc.getString("role")))
                    .count();
            long pros = queryDocumentSnapshots.getDocuments().stream()
                    .filter(doc -> Boolean.TRUE.equals(doc.getBoolean("isPro")))
                    .count();

            if (binding != null) {
                binding.tvTotalUsersCount.setText(String.valueOf(total));
                binding.tvTotalTrainersCount.setText(String.valueOf(trainers));
                binding.tvTotalProCount.setText(String.valueOf(pros));
                binding.tvRevenue.setText("$" + String.format("%.2f", pros * 19.99));
            }
        });
    }

    private void setupRecentTransactions() {
        List<Payment> allPayments = new ArrayList<>();
        PaymentHistoryAdapter adapter = new PaymentHistoryAdapter(allPayments);
        binding.rvRecentTransactions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvRecentTransactions.setAdapter(adapter);

        // Fetch payments from all users (Global View for Admin)
        db.collectionGroup("payments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        allPayments.add(doc.toObject(Payment.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
