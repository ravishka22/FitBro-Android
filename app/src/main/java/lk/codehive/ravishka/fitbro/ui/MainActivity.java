package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityMainBinding;
import lk.codehive.ravishka.fitbro.ui.fragments.FitnessFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.HomeFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.MapFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.PlansFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set Home Dashboard as default fragment
        replaceFragment(new HomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_plans) {
                replaceFragment(new PlansFragment());
                return true;
            } else if (id == R.id.nav_map) {
                replaceFragment(new MapFragment());
                return true;
            } else if (id == R.id.nav_progress) {
                replaceFragment(new FitnessFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment);
        fragmentTransaction.commit();
    }
}