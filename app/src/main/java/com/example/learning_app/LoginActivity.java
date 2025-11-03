package com.example.learning_app; // Thay bằng package của bạn

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvPasswordError;
    private ImageView iconUsernameError, iconPasswordError, ivBack;
    private Button btnLogin;
    private DatabaseHelper dbHelper; // Biến Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo DB Helper
        dbHelper = new DatabaseHelper(this);

        // Lấy IDs
        ivBack = findViewById(R.id.ivBack);
        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        iconUsernameError = findViewById(R.id.iconUsernameError);
        iconPasswordError = findViewById(R.id.iconPasswordError);

        ivBack.setOnClickListener(v -> finish());

        // Sửa OnClick của btnLogin
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // KIỂM TRA BẰNG DB
                if (dbHelper.checkUserLogin(username, password)) {
                    // Đăng nhập thành công
                    hideError(etUsername, tvUsernameError, iconUsernameError);
                    hideError(etPassword, tvPasswordError, iconPasswordError);

                    // Mở màn hình Home
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USERNAME", username); // Gửi username
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    // Báo lỗi chung
                    showError(etUsername, tvUsernameError, iconUsernameError, "");
                    showError(etPassword, tvPasswordError, iconPasswordError, "Username hoặc mật khẩu không đúng!");
                }
            }
        });

        // Thêm TextWatcher
        etUsername.addTextChangedListener(loginTextWatcher);
        etPassword.addTextChangedListener(loginTextWatcher);
    }

    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                activateLoginButton();
            } else {
                deactivateLoginButton();
            }
        }
    };

    // (Các hàm showError, hideError, activate/deactivate Button giữ nguyên)
    private void showError(EditText et, TextView tv, ImageView icon, String message) {
        et.setBackgroundResource(R.drawable.edit_text_background_light_error);
        tv.setText(message);
        tv.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);
    }
    private void hideError(EditText et, TextView tv, ImageView icon) {
        et.setBackgroundResource(R.drawable.edit_text_background_light);
        tv.setVisibility(View.GONE);
        icon.setVisibility(View.GONE);
    }
    private void activateLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setBackgroundResource(R.drawable.button_background_green);
        btnLogin.setTextColor(Color.WHITE);
    }
    private void deactivateLoginButton() {
        btnLogin.setEnabled(false);
        btnLogin.setBackgroundResource(R.drawable.button_background_disabled);
        btnLogin.setTextColor(Color.parseColor("#AFAFAF"));
    }
}