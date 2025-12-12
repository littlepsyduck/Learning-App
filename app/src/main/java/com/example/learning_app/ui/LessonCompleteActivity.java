package com.example.learning_app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.viewmodel.MainViewModel;

import java.util.concurrent.atomic.AtomicInteger;

public class LessonCompleteActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lesson_complete);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        int totalXP = getIntent().getIntExtra("totalXP", 0);
        int accuracy = getIntent().getIntExtra("accuracy", 0);
        String timeSpent = getIntent().getStringExtra("timeSpent");

        ImageView ivCharacterFinish = findViewById(R.id.ivCharacterFinish);
        TextView tvTotalXP = findViewById(R.id.tvTotalXP);
        TextView tvAccuracy = findViewById(R.id.tvAccuracy);
        TextView tvTime = findViewById(R.id.tvTime);
        Button btnClaimXP = findViewById(R.id.btnClaimXP);

        int characterFinishResId = getResources().getIdentifier("character_duo_1", "drawable", getPackageName());
        if (characterFinishResId != 0) {
            ivCharacterFinish.setImageResource(characterFinishResId);
        }

        tvTotalXP.setText(String.valueOf(totalXP));
        tvAccuracy.setText(accuracy + "%");
        tvTime.setText(timeSpent);

        btnClaimXP.setOnClickListener(v -> {
            btnClaimXP.setEnabled(false); // Disable button to prevent double-clicking

            // Use a counter to close the activity only when all tasks are complete
            AtomicInteger tasksCompleted = new AtomicInteger(0);
            Runnable onTaskComplete = () -> {
                if (tasksCompleted.incrementAndGet() == 2) {
                    setResult(RESULT_OK);
                    finish();
                }
            };

            viewModel.claimLessonRewards(totalXP, accuracy, onTaskComplete);
            viewModel.decrementHeart(onTaskComplete);
        });
    }
}