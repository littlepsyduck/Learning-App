package com.example.learning_app; // Thay bằng package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class UserAllDoneActivity extends AppCompatActivity {

    private Button btnLetsStart;
    private String loggedInUsername; // Thêm biến này

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_all_done);

        // Nhận USERNAME từ RegistrationActivity
        loggedInUsername = getIntent().getStringExtra("USERNAME");

        btnLetsStart = findViewById(R.id.btnLetsStart);

        btnLetsStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở HomeActivity và gửi username
                Intent intent = new Intent(UserAllDoneActivity.this, UserDashboardActivity.class);
                intent.putExtra("USERNAME", loggedInUsername);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Đóng Activity này
            }
        });
    }
}