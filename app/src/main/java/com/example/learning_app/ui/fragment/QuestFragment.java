package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
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
import com.example.learning_app.entities.User;
import com.example.learning_app.viewmodel.ProgressViewModel;

import java.util.Calendar;

public class QuestFragment extends Fragment {

    private ProgressViewModel viewModel;
    private TextView tvTimeRemaining;
    private Handler handler;
    private Runnable updateTimeRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);

        TextView txtProgressXP = view.findViewById(R.id.txtProgressXP);
        ProgressBar progressBarXP = view.findViewById(R.id.progressBarXP);
        ImageView imgChestXP = view.findViewById(R.id.imgChestXP);
        TextView txtProgressStreak = view.findViewById(R.id.txtProgressStreak);
        ProgressBar progressBarStreak = view.findViewById(R.id.progressBarStreak);
        ImageView imgChestStreak = view.findViewById(R.id.imgChestStreak);
        TextView txtProgressAccuracy = view.findViewById(R.id.txtProgressAccuracy);
        ProgressBar progressBarAccuracy = view.findViewById(R.id.progressBarAccuracy);
        ImageView imgChestAccuracy = view.findViewById(R.id.imgChestAccuracy);
        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);

        // Start timer to update remaining time
        startTimer();

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                int currentXP = user.getXp();
                int targetXP = 50;

                txtProgressXP.setText(currentXP + " / " + targetXP + " XP");
                progressBarXP.setMax(targetXP);
                progressBarXP.setProgress(currentXP);

                if (currentXP >= targetXP) {
                    imgChestXP.setImageResource(R.drawable.ic_chest1_open);
                } else {
                    imgChestXP.setImageResource(R.drawable.ic_chest);
                }

                int currentStreak = user.getStreak();
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

                int currentPerfectLessons = user.getPerfectLessonCount();
                int targetPerfectLessons = 2;

                txtProgressAccuracy.setText("Complete " + currentPerfectLessons + " / " + targetPerfectLessons + " lessons with >90% accuracy");
                progressBarAccuracy.setMax(targetPerfectLessons);
                progressBarAccuracy.setProgress(currentPerfectLessons);

                if (currentPerfectLessons >= targetPerfectLessons) {
                    imgChestAccuracy.setImageResource(R.drawable.ic_chest3_open);
                } else {
                    imgChestAccuracy.setImageResource(R.drawable.ic_chest);
                }
            }
        });

        return view;
    }

    private void startTimer() {
        handler = new Handler();
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateRemainingTime();
                handler.postDelayed(this, 60000); // Update every minute
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void updateRemainingTime() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 23);
        midnight.set(Calendar.MINUTE, 59);
        midnight.set(Calendar.SECOND, 59);

        long diffInMillis = midnight.getTimeInMillis() - now.getTimeInMillis();
        long diffInHours = diffInMillis / (60 * 60 * 1000);
        long diffInMinutes = (diffInMillis / (60 * 1000)) % 60;

        if (tvTimeRemaining != null) {
            tvTimeRemaining.setText("🕒 " + diffInHours + " HOURS " + diffInMinutes + " MINUTES");
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.reloadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && updateTimeRunnable != null) {
            handler.removeCallbacks(updateTimeRunnable);
        }
    }
}