package com.example.learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class UserAllDoneActivity extends AppCompatActivity {

    private Button btnLogin;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_all_done);

        btnLogin = findViewById(R.id.btnLogin);
        ivBack = findViewById(R.id.ivBack);

        // Nút LOGIN: Chuyển sang màn hình Đăng Nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserAllDoneActivity.this, UserLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // == SỬA ĐOẠN NÀY ==
        // Nút Back: Quay lại màn hình ĐĂNG KÝ (UserRegisterActivity)
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserAllDoneActivity.this, UserRegisterActivity.class);
                // Xóa cờ FLAG để nó mở lại màn hình đăng ký mới
                startActivity(intent);
                finish(); // Đóng màn hình All Done hiện tại
            }
        });
    }
}