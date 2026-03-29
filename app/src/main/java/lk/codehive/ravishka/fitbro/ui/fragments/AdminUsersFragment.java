package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.codehive.ravishka.fitbro.R;
import lk.codehive.ravishka.fitbro.databinding.FragmentAdminUsersBinding;
import lk.codehive.ravishka.fitbro.models.User;
import lk.codehive.ravishka.fitbro.ui.adapters.AdminUsersAdapter;
import lk.codehive.ravishka.fitbro.utils.NotificationHelper;

public class AdminUsersFragment extends Fragment {

    private static final String TAG = "AdminUsersFragment";
    private FragmentAdminUsersBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final List<User> allUsersList = new ArrayList<>();
    private final List<User> filteredList = new ArrayList<>();
    private AdminUsersAdapter adapter;
    private int currentTab = 0; // 0: Members, 1: Trainers
    private ListenerRegistration usersListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupTabLayout();
        setupSearch();
        loadUsers();

        binding.fabAddUser.setOnClickListener(v -> showCreateUserSheet());
    }

    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(filteredList, user -> showUserDetailSheet(user));
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void setupTabLayout() {
        binding.tabLayoutRoles.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        binding.etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        if (usersListener != null) usersListener.remove();
        
        usersListener = db.collection("users").addSnapshotListener((value, error) -> {
            if (binding == null) return; 
            if (error != null || value == null) {
                Log.e(TAG, "LoadUsers error", error);
                return;
            }
            
            allUsersList.clear();
            for (QueryDocumentSnapshot doc : value) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setId(doc.getId());
                    String role = user.getRole();
                    if (role == null || !role.equalsIgnoreCase("admin")) {
                        allUsersList.add(user);
                    }
                }
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        if (binding == null) return;

        filteredList.clear();
        String query = binding.etSearchUsers.getText().toString().toLowerCase().trim();
        boolean showTrainers = (currentTab == 1);

        for (User user : allUsersList) {
            String role = user.getRole();
            if (role == null || role.equalsIgnoreCase("user")) role = "member";
            role = role.toLowerCase();

            boolean isTrainer = role.contains("trainer");
            boolean matchesTab = showTrainers ? isTrainer : !isTrainer;

            if (matchesTab) {
                String name = user.getFullName();
                String email = user.getEmail();
                if (query.isEmpty() || (name != null && name.toLowerCase().contains(query)) || 
                    (email != null && email.toLowerCase().contains(query))) {
                    filteredList.add(user);
                }
            }
        }
        adapter.setUsers(filteredList);
    }

    private void showUserDetailSheet(User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_admin_user_detail_sheet, null);
        dialog.setContentView(view);

        ShapeableImageView ivAvatar = view.findViewById(R.id.ivDetailAvatar);
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvEmail = view.findViewById(R.id.tvDetailEmail);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        AutoCompleteTextView spinnerRole = view.findViewById(R.id.spinnerDetailRole);
        MaterialSwitch switchPro = view.findViewById(R.id.switchDetailPro);
        MaterialSwitch switchActive = view.findViewById(R.id.switchAccountActive);
        MaterialButton btnUpdate = view.findViewById(R.id.btnUpdateUser);
        MaterialButton btnDelete = view.findViewById(R.id.btnDeleteUser);

        tvName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());
        
        boolean isDeactivated = user.isDeactivated();
        tvStatus.setText(isDeactivated ? "Account Deactivated" : "Account Active");
        tvStatus.setTextColor(isDeactivated ? getResources().getColor(R.color.error_red) : getResources().getColor(R.color.electric_green));

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.logo_dark).into(ivAvatar);
        }

        String[] roles = {"member", "trainer"};
        spinnerRole.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, roles));
        
        String currentRole = user.getRole();
        if (currentRole == null || currentRole.equalsIgnoreCase("user") || currentRole.equalsIgnoreCase("pro")) {
            currentRole = "member";
        }
        spinnerRole.setText(currentRole.toLowerCase(), false);
        
        switchPro.setChecked(user.isPro());
        switchActive.setChecked(!isDeactivated);

        btnUpdate.setOnClickListener(v -> {
            if (user.getId() == null) return;
            Map<String, Object> updates = new HashMap<>();
            updates.put("role", spinnerRole.getText().toString().toLowerCase());
            updates.put("isPro", switchPro.isChecked());
            updates.put("deactivated", !switchActive.isChecked());

            db.collection("users").document(user.getId()).update(updates).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Account details updated", Toast.LENGTH_SHORT).show();
                
                // Trigger notification for the user
                NotificationHelper.triggerNotification(
                        user.getId(),
                        "Account Updated",
                        "An administrator has updated your account settings.",
                        "admin_update"
                );
                
                dialog.dismiss();
            });
        });

        btnDelete.setOnClickListener(v -> {
            if (user.getId() == null) return;
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Permanently remove this account?")
                    .setPositiveButton("DELETE", (d, w) -> {
                        db.collection("users").document(user.getId()).delete().addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });

        dialog.show();
    }

    private void showCreateUserSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_admin_create_user_sheet, null);
        dialog.setContentView(view);

        TextInputEditText etName = view.findViewById(R.id.etNewFullName);
        TextInputEditText etEmail = view.findViewById(R.id.etNewEmail);
        TextInputEditText etPass = view.findViewById(R.id.etNewPassword);
        AutoCompleteTextView spinnerRole = view.findViewById(R.id.spinnerNewRole);
        MaterialButton btnCreate = view.findViewById(R.id.btnCreateUserConfirm);

        String[] roles = {"member", "trainer"};
        spinnerRole.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, roles));
        spinnerRole.setText("member", false);

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String role = spinnerRole.getText().toString().toLowerCase();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreate.setEnabled(false);

            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            FirebaseApp secondaryApp = null;
            try {
                secondaryApp = FirebaseApp.getInstance("admin_creation");
            } catch (IllegalStateException e) {
                secondaryApp = FirebaseApp.initializeApp(requireContext(), options, "admin_creation");
            }

            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
            secondaryAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser newUserAuth = task.getResult().getUser();
                            if (newUserAuth != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                
                                newUserAuth.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                    User newUser = new User(newUserAuth.getUid(), name, email, role);
                                    newUser.setPro("trainer".equalsIgnoreCase(role));

                                    db.collection("users").document(newUserAuth.getUid()).set(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Account created successfully", Toast.LENGTH_LONG).show();
                                                secondaryAuth.signOut();
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                btnCreate.setEnabled(true);
                                                Toast.makeText(getContext(), "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }
                        } else {
                            btnCreate.setEnabled(true);
                            Toast.makeText(getContext(), "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersListener != null) {
            usersListener.remove();
        }
        binding = null;
    }
}
