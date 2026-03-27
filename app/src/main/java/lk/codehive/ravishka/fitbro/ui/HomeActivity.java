package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.ActivityHomeBinding;
import lk.codehive.ravishka.fitbro.models.Product;
import lk.codehive.ravishka.fitbro.ui.adapters.ProductAdapter;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupProductList();
    }

    private void setupProductList() {
        // Mock Data
        List<Product> products = new ArrayList<>();
        products.add(new Product("1", "Gold Standard Whey", "High quality protein", 89.99, "https://example.com/whey.png", "Protein"));
        products.add(new Product("2", "Micronized Creatine", "Strength & Power", 34.99, "https://example.com/creatine.png", "Creatine"));
        products.add(new Product("3", "Pre-Workout Blast", "Explosive energy", 45.00, "https://example.com/pre.png", "Pre-Workout"));
        products.add(new Product("4", "BCAA Recovery", "Muscle repair", 29.99, "https://example.com/bcaa.png", "Recovery"));
        products.add(new Product("5", "Mass Gainer Pro", "For maximum size", 75.50, "https://example.com/gainer.png", "Mass Gainer"));
        products.add(new Product("6", "Multi-Vitamin Pack", "Daily health", 19.99, "https://example.com/vit.png", "Vitamins"));

        // Setup RecyclerView
        ProductAdapter adapter = new ProductAdapter(products);
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);
    }
}