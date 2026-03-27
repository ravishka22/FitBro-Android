package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import lk.codehive.ravishka.fitbro.databinding.FragmentShopBinding;
import lk.codehive.ravishka.fitbro.models.Product;
import lk.codehive.ravishka.fitbro.ui.adapters.ProductAdapter;

public class ShopFragment extends Fragment {

    private FragmentShopBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupProductList();
    }

    private void setupProductList() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("1", "Gold Standard Whey", "Protein", 89.99, "https://example.com/whey.png", "Protein"));
        products.add(new Product("2", "Micronized Creatine", "Creatine", 34.99, "https://example.com/creatine.png", "Creatine"));
        products.add(new Product("3", "Pre-Workout Blast", "Pre-Workout", 45.00, "https://example.com/pre.png", "Pre-Workout"));
        products.add(new Product("4", "BCAA Recovery", "Recovery", 29.99, "https://example.com/bcaa.png", "Recovery"));

        ProductAdapter adapter = new ProductAdapter(products);
        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}