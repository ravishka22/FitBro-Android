package lk.codehive.ravishka.fitbro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lk.codehive.ravishka.fitbro.databinding.FragmentTrainerRequestBinding;

public class TrainerRequestFragment extends Fragment {

    private FragmentTrainerRequestBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrainerRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSubmitRequest.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Request Sent! A trainer will contact you soon.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}