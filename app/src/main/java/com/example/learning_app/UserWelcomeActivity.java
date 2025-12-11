package com.example.learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UserWelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_welcome); // Layout từ câu hỏi trước

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyHaveAccount);

        // Xử lý sự kiện click
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một Intent để mở Activity mới
                // Chúng ta sẽ gọi Activity mới là LearningReasonActivity
                Intent intent = new Intent(UserWelcomeActivity.this, UserLearningReasonActivity.class);
                startActivity(intent);
            }
        });
        btnAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở Activity Đăng nhập mới
                Intent intent = new Intent(UserWelcomeActivity.this, UserLoginActivity.class);
                startActivity(intent);
            }
        });
        // (Code cho nút "I ALREADY HAVE AN ACCOUNT" có thể thêm ở đây)
    }
}