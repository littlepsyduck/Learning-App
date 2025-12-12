package com.example.learning_app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.viewmodel.MainViewModel;

public class LessonCompleteActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lesson_complete);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Lấy dữ liệu từ Intent
        int totalXP = getIntent().getIntExtra("totalXP", 0);
        int accuracy = getIntent().getIntExtra("accuracy", 0);
        String timeSpent = getIntent().getStringExtra("timeSpent");

        // Ánh xạ Views
        ImageView ivCharacterFinish = findViewById(R.id.ivCharacterFinish);
        TextView tvTotalXP = findViewById(R.id.tvTotalXP);
        TextView tvAccuracy = findViewById(R.id.tvAccuracy);
        TextView tvTime = findViewById(R.id.tvTime);
        Button btnClaimXP = findViewById(R.id.btnClaimXP);

        // Set character image
        int characterFinishResId = getResources().getIdentifier("character_duo_1", "drawable", getPackageName());
        if (characterFinishResId != 0) {
            ivCharacterFinish.setImageResource(characterFinishResId);
        }

        // Hiển thị dữ liệu
        tvTotalXP.setText(String.valueOf(totalXP));
        tvAccuracy.setText(accuracy + "%");
        tvTime.setText(timeSpent);

        // Xử lý sự kiện click
        btnClaimXP.setOnClickListener(v -> {
            // 1. Cập nhật dữ liệu thông qua ViewModel
            viewModel.claimLessonRewards(totalXP);
            viewModel.decrementHeart();

            // 2. Đặt kết quả thành công
            setResult(RESULT_OK);

            // 3. Đóng Activity
            finish();
        });
    }
}