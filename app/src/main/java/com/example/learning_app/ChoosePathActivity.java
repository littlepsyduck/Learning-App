package com.example.learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ChoosePathActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout cardNewLearner;
    private LinearLayout cardExistingLearner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_path);

        // Lấy IDs
        ivBack = findViewById(R.id.ivBack);
        cardNewLearner = findViewById(R.id.card_new_learner);
        cardExistingLearner = findViewById(R.id.card_existing_learner);

        // Xử lý nút Back
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng Activity hiện tại và quay lại
            }
        });
        // Nhận data "WHY_LEARN"
        String whyLearn = getIntent().getStringExtra("WHY_LEARN");
        // Xử lý sự kiện click cho cả 2 thẻ
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SỬA Ở ĐÂY: Mở màn hình Đăng ký
                Intent intent = new Intent(ChoosePathActivity.this, RegistrationActivity.class);
                // 1. Gửi tiếp data đã nhận
                intent.putExtra("WHY_LEARN", whyLearn);
                // 2. Gửi data "status" mới
                if (v.getId() == R.id.card_new_learner) {
                    intent.putExtra("STATUS", "newbie");
                } else if (v.getId() == R.id.card_existing_learner) {
                    intent.putExtra("STATUS", "alreadyknow");
                }
                startActivity(intent);
            }
        };

        cardNewLearner.setOnClickListener(listener);
        cardExistingLearner.setOnClickListener(listener);
    }
}