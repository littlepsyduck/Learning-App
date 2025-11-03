package com.example.learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Layout từ câu hỏi trước

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyHaveAccount);

        // Xử lý sự kiện click
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một Intent để mở Activity mới
                // Chúng ta sẽ gọi Activity mới là LearningReasonActivity
                Intent intent = new Intent(MainActivity.this, LearningReasonActivity.class);
                startActivity(intent);
            }
        });
        btnAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở Activity Đăng nhập mới
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        // (Code cho nút "I ALREADY HAVE AN ACCOUNT" có thể thêm ở đây)
    }
}