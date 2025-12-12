package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.Calendar;
import java.util.Locale;

public class QuestFragment extends Fragment {

    private MainViewModel viewModel;
    private TextView tvTimeRemaining;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);
        startDailyQuestTimer();

        // Map Views for Quests
        TextView txtProgressXP = view.findViewById(R.id.txtProgressXP);
        ProgressBar progressBarXP = view.findViewById(R.id.progressBarXP);
        ImageView imgChestXP = view.findViewById(R.id.imgChestXP);
        TextView txtProgressStreak = view.findViewById(R.id.txtProgressStreak);
        ProgressBar progressBarStreak = view.findViewById(R.id.progressBarStreak);
        ImageView imgChestStreak = view.findViewById(R.id.imgChestStreak);
        TextView txtProgressAccuracy = view.findViewById(R.id.txtProgressAccuracy);
        ProgressBar progressBarAccuracy = view.findViewById(R.id.progressBarAccuracy);
        ImageView imgChestAccuracy = view.findViewById(R.id.imgChestAccuracy);

        viewModel.getUserStats().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // XP Quest
                int targetXP = 50;
                txtProgressXP.setText(user.xp + " / " + targetXP + " XP");
                progressBarXP.setMax(targetXP);
                progressBarXP.setProgress(user.xp);
                imgChestXP.setImageResource(user.xp >= targetXP ? R.drawable.ic_chest1_open : R.drawable.ic_chest1);

                // Streak Quest
                int targetStreak = 1;
                int displayStreak = Math.min(user.streak, targetStreak);
                txtProgressStreak.setText(displayStreak + " / " + targetStreak);
                progressBarStreak.setMax(targetStreak);
                progressBarStreak.setProgress(displayStreak);
                imgChestStreak.setImageResource(user.streak >= targetStreak ? R.drawable.ic_chest2_open : R.drawable.ic_chest2);

                // Accuracy Quest
                int targetAccuracyLessons = 2;
                txtProgressAccuracy.setText("Hoàn thành " + user.lessonsWithHighAccuracy + "/" + targetAccuracyLessons + " bài với độ chính xác >90%");
                progressBarAccuracy.setMax(targetAccuracyLessons);
                progressBarAccuracy.setProgress(user.lessonsWithHighAccuracy);
                imgChestAccuracy.setImageResource(user.lessonsWithHighAccuracy >= targetAccuracyLessons ? R.drawable.ic_chest3_open : R.drawable.ic_chest3);
            }
        });

        return view;
    }

    private void startDailyQuestTimer() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long timeUntilMidnight = calendar.getTimeInMillis() - now;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeUntilMidnight, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (1000 * 60 * 60);
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimeRemaining.setText(String.format(Locale.getDefault(), "🕒 %02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimeRemaining.setText("🕒 00:00:00");
                // Reload quests or data
                if (viewModel != null) {
                    viewModel.reloadUserStats();
                }
            }
        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.reloadUserStats();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}