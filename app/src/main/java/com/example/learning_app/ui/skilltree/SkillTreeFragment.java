package com.example.learning_app.ui.skilltree;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.learning_app.databinding.FragmentSkillTreeBinding;

public class SkillTreeFragment extends Fragment {

    private FragmentSkillTreeBinding binding;
    private SkillTreeViewModel viewModel;
    private SkillAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSkillTreeBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(SkillTreeViewModel.class);

        adapter = new SkillAdapter(skill -> {
            SkillInfoSheet sheet = new SkillInfoSheet(skill);
            sheet.show(getParentFragmentManager(), "info");
        });

        binding.rvSkillTree.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSkillTree.setAdapter(adapter);

        viewModel.skills.observe(getViewLifecycleOwner(), adapter::setData);

        return binding.getRoot();
    }
}
