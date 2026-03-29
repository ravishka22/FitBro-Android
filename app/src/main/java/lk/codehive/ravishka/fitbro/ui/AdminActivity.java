package lk.codehive.ravishka.fitbro.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.ActivityAdminBinding;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminStatsFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminUsersFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminGymsFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.AdminPlansFragment;
import lk.codehive.ravishka.fitbro.ui.fragments.ProfileFragment;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            replaceFragment(new AdminStatsFragment());
        }

        binding.adminBottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_nav_home) {
                replaceFragment(new AdminStatsFragment());
                return true;
            } else if (id == R.id.admin_nav_users) {
                replaceFragment(new AdminUsersFragment());
                return true;
            } else if (id == R.id.admin_nav_gyms) {
                replaceFragment(new AdminGymsFragment());
                return true;
            } else if (id == R.id.admin_nav_membership) {
                replaceFragment(new AdminPlansFragment());
                return true;
            } else if (id == R.id.admin_nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.admin_nav_host_fragment, fragment);
        fragmentTransaction.commit();
    }
}
