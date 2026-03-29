package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityTrainerBinding;
import lk.codehive.ravishka.fitbro.ui.fragments.ProfileFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerExercisesFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerGymsFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerHomeFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.TrainerPlansFragment;

public class TrainerActivity extends AppCompatActivity {

    private ActivityTrainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default fragment
        if (savedInstanceState == null) {
            replaceFragment(new TrainerHomeFragment());
        }

        binding.trainerBottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.trainer_nav_home) {
                replaceFragment(new TrainerHomeFragment());
                return true;
            } else if (id == R.id.trainer_nav_plans) {
                replaceFragment(new TrainerPlansFragment());
                return true;
            } else if (id == R.id.trainer_nav_gyms) {
                replaceFragment(new TrainerGymsFragment());
                return true;
            } else if (id == R.id.trainer_nav_exercises) {
                replaceFragment(new TrainerExercisesFragment());
                return true;
            } else if (id == R.id.trainer_nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.trainer_nav_host_fragment, fragment);
        fragmentTransaction.commit();
    }
}
