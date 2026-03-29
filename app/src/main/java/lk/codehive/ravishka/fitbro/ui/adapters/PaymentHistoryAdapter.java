package lk.codehive.ravishka.fitbro.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.codehive.ravishka.fitbro.databinding.ItemPaymentHistoryBinding;
import lk.codehive.ravishka.fitbro.models.Payment;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {

    private final List<Payment> payments;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public PaymentHistoryAdapter(List<Payment> payments) {
        this.payments = payments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentHistoryBinding binding = ItemPaymentHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.binding.tvTransactionPlan.setText("FitBro " + payment.getPlanName() + " Plan");
        holder.binding.tvTransactionDate.setText(sdf.format(new Date(payment.getTimestamp())));
        holder.binding.tvTransactionAmount.setText("+$" + payment.getAmount());
    }

    @Override
    public int getItemCount() {
        return payments != null ? payments.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPaymentHistoryBinding binding;
        ViewHolder(ItemPaymentHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
