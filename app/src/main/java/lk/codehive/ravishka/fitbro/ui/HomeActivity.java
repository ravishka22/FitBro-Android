package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityHomeBinding;
import lk.codehive.ravishka.fitbro.ui.fragments.FitnessFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.HomeFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.MapFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.PlansFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_plans) {
                fragment = new PlansFragment();
            } else if (id == R.id.nav_map) {
                fragment = new MapFragment();
            } else if (id == R.id.nav_progress) {
                fragment = new FitnessFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}
