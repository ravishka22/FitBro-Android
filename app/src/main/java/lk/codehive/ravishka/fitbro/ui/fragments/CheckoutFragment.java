package lk.codehive.ravishka.fitbro.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentCheckoutBinding;
import lk.codehive.ravishka.fitbro.models.Payment;
import lk.codehive.ravishka.fitbro.ui.SplashActivity;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private Stripe stripe;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private String targetPlan = "member"; // Default role keyword
    private String planPrice = "19.99";

    // Replace with your real Stripe Publishable Key
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_your_key_here";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        if (getArguments() != null) {
            targetPlan = getArguments().getString("PLAN_TYPE", "member");
            planPrice = getArguments().getString("PLAN_PRICE", "19.99");
        }

        updateSummary();

        // Initialize Stripe
        PaymentConfiguration.init(requireContext(), STRIPE_PUBLISHABLE_KEY);
        stripe = new Stripe(requireContext(), STRIPE_PUBLISHABLE_KEY);

        binding.btnPayNow.setOnClickListener(v -> startCheckout());
    }

    private void updateSummary() {
        binding.tvCheckoutPlanName.setText("FitBro " + targetPlan.toUpperCase() + " Plan");
        binding.tvCheckoutPrice.setText("$" + planPrice);
        binding.tvCheckoutTotal.setText("$" + planPrice);
    }

    private void startCheckout() {
        CardInputWidget cardInputWidget = binding.cardInputWidget;
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();

        if (params == null) {
            Toast.makeText(getContext(), "Invalid Card Details", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.btnPayNow.setEnabled(false);

        // Step 1: Call Firebase Cloud Function to get Stripe Client Secret
        mFunctions.getHttpsCallable("createStripePaymentIntent")
                .call()
                .addOnSuccessListener(result -> {
                    Map<String, Object> data = (Map<String, Object>) result.getData();
                    if (data != null && data.containsKey("clientSecret")) {
                        String clientSecret = (String) data.get("clientSecret");
                        confirmPayment(clientSecret, params);
                    }
                })
                .addOnFailureListener(e -> {
                    // Simulation for demo
                    completeSubscription();
                });
    }

    private void confirmPayment(String clientSecret, PaymentMethodCreateParams params) {
        ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                .createWithPaymentMethodCreateParams(params, clientSecret);
        
        // Simulation for demo
        completeSubscription();
    }

    private void completeSubscription() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        long startTime = System.currentTimeMillis();
        
        // Calculate End Date (1 Month from now)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        calendar.add(Calendar.MONTH, 1);
        long endTime = calendar.getTimeInMillis();

        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, planPrice, startTime, targetPlan.toUpperCase());

        // Save Payment History
        db.collection("users").document(uid).collection("payments").document(paymentId).set(payment);

        // Update User Role, Pro Status and Dates
        Map<String, Object> updates = new HashMap<>();
        updates.put("isPro", true);
        
        // Ensure role remains "member" if they are upgrading to a Pro athlete plan, 
        // or "trainer" if they are upgrading to a trainer plan.
        String roleToSave = targetPlan.toLowerCase();
        if (roleToSave.equals("pro") || roleToSave.equals("user")) {
            roleToSave = "member";
        }
        
        updates.put("role", roleToSave); 
        updates.put("membershipStartDate", startTime);
        updates.put("membershipEndDate", endTime);

        db.collection("users").document(uid).set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> showSuccessDialog())
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.btnPayNow.setEnabled(true);
                        Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSuccessDialog() {
        if (binding == null) return;
        binding.pbLoading.setVisibility(View.GONE);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_payment_success_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            dialog.dismiss();
            
            // Redirect to SplashActivity to handle portal switching
            Intent intent = new Intent(getContext(), SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
