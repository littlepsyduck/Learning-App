package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.db.UserEntity;
import com.example.learning_app.ui.MainActivity;
import com.example.learning_app.viewmodel.MainViewModel;

public class HomeFragment extends Fragment {
    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Nút để mở màn hình hoàn thành bài học
        Button btnTest = view.findViewById(R.id.btnTestComplete);
        btnTest.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                UserEntity currentUser = viewModel.getUserStats().getValue();
                if (currentUser != null && currentUser.hearts > 0) {
                    ((MainActivity) getActivity()).showLessonCompleteScreen(15, 95, "0:30");
                } else {
                    Toast.makeText(getContext(), "Hết tim rồi, không thể làm bài!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Nút tạm thời để thêm Streak Freeze
        Button btnAddFreeze = new Button(getContext());
        btnAddFreeze.setText("Add 1 Freeze (Test)");
        ((ViewGroup) view).addView(btnAddFreeze); // Thêm nút vào layout
        
        btnAddFreeze.setOnClickListener(v -> {
            viewModel.addFreeze();
            Toast.makeText(getContext(), "Added 1 Freeze", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}