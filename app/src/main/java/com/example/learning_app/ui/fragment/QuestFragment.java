package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.viewmodel.MainViewModel;

public class QuestFragment extends Fragment {

    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest, container, false);

        // Lấy ViewModel chung từ Activity
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Ánh xạ Views
        TextView txtProgressXP = view.findViewById(R.id.txtProgressXP);
        ProgressBar progressBarXP = view.findViewById(R.id.progressBarXP);
        ImageView imgChestXP = view.findViewById(R.id.imgChestXP);
        TextView txtProgressStreak = view.findViewById(R.id.txtProgressStreak);
        ProgressBar progressBarStreak = view.findViewById(R.id.progressBarStreak);
        ImageView imgChestStreak = view.findViewById(R.id.imgChestStreak);

        // Quan sát (observe) dữ liệu từ ViewModel
        viewModel.getUserStats().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // --- XỬ LÝ NHIỆM VỤ XP (Mục tiêu 50) ---
                int currentXP = user.xp;
                int targetXP = 50;

                txtProgressXP.setText(currentXP + " / " + targetXP + " XP");
                progressBarXP.setMax(targetXP);
                progressBarXP.setProgress(currentXP);

                if (currentXP >= targetXP) {
                    imgChestXP.setImageResource(R.drawable.ic_chest1_open);
                } else {
                    imgChestXP.setImageResource(R.drawable.ic_chest);
                }

                // --- XỬ LÝ NHIỆM VỤ STREAK (Mục tiêu 1) ---
                int currentStreak = user.streak;
                int targetStreak = 1;

                int displayStreak = Math.min(currentStreak, targetStreak);
                txtProgressStreak.setText(displayStreak + " / " + targetStreak);

                progressBarStreak.setMax(targetStreak);
                progressBarStreak.setProgress(displayStreak);

                if (currentStreak >= targetStreak) {
                    imgChestStreak.setImageResource(R.drawable.ic_chest2_open);
                } else {
                    imgChestStreak.setImageResource(R.drawable.ic_chest);
                }
            }
        });

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Yêu cầu ViewModel tải lại dữ liệu khi Fragment được hiển thị
        if (viewModel != null) {
            viewModel.reloadUserStats();
        }
    }
}