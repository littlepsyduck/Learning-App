package com.example.learning_app; // Thay bằng package của bạn

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etFullName, etAge, etEmail;
    private TextView tvUsernameError, tvPasswordError, tvEmailError;
    private ImageView iconUsernameError, iconPasswordError, iconEmailError, ivBack;
    private Button btnContinue;
    private DatabaseHelper dbHelper; // Biến Database
    private String whyLearn; // Biến lưu data được truyền
    private String status;   // Biến lưu data được truyền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Khởi tạo DB Helper
        dbHelper = new DatabaseHelper(this);

        // Nhận data từ ChoosePathActivity
        whyLearn = getIntent().getStringExtra("WHY_LEARN");
        status = getIntent().getStringExtra("STATUS");

        // Lấy tất cả ID từ layout
        ivBack = findViewById(R.id.ivBack);
        btnContinue = findViewById(R.id.btnContinue);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvEmailError = findViewById(R.id.tvEmailError);
        iconUsernameError = findViewById(R.id.iconUsernameError);
        iconPasswordError = findViewById(R.id.iconPasswordError);
        iconEmailError = findViewById(R.id.iconEmailError);

        ivBack.setOnClickListener(v -> finish());

        // Thêm TextWatcher
        etUsername.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(validationWatcher);
        etFullName.addTextChangedListener(validationWatcher);
        etAge.addTextChangedListener(validationWatcher);
        etEmail.addTextChangedListener(validationWatcher);

        // Sửa OnClick của btnContinue
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy data từ EditText
                String fullName = etFullName.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                int age = Integer.parseInt(etAge.getText().toString().trim()); // Cần xử lý lỗi nếu rỗng

                // Thêm user vào DB
                boolean success = dbHelper.addUser(fullName, username, password, age, email, whyLearn, status);

                if (success) {
                    // Mở AllDoneActivity
                    Intent intent = new Intent(RegistrationActivity.this, AllDoneActivity.class);
                    // Gửi USERNAME cho màn hình tiếp theo
                    intent.putExtra("USERNAME", username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // Xử lý lỗi (ví dụ: hiển thị Toast "Đăng ký thất bại")
                }
            }
        });
    }

    private TextWatcher validationWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            validateForm();
        }
    };

    private boolean validateForm() {
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();
        boolean isEmailValid = validateEmail();
        boolean isFullNameValid = !etFullName.getText().toString().trim().isEmpty();
        boolean isAgeValid = !etAge.getText().toString().trim().isEmpty();

        if (isUsernameValid && isPasswordValid && isEmailValid && isFullNameValid && isAgeValid) {
            activateContinueButton();
            return true;
        } else {
            deactivateContinueButton();
            return false;
        }
    }

    // Sửa hàm validateUsername (dùng DB)
    private boolean validateUsername() {
        String username = etUsername.getText().toString().trim();
        if (dbHelper.checkUsernameExists(username)) { // KIỂM TRA DB
            showError(etUsername, tvUsernameError, iconUsernameError, "Username không hợp lệ!");
            return false;
        } else if (username.length() < 4) {
            showError(etUsername, tvUsernameError, iconUsernameError, "Username phải có ít nhất 4 ký tự!");
            return false;
        } else {
            hideError(etUsername, tvUsernameError, iconUsernameError);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();
        if (password.length() < 8) {
            showError(etPassword, tvPasswordError, iconPasswordError, "Password phải có ít nhất 8 ký tự!");
            return false;
        } else {
            hideError(etPassword, tvPasswordError, iconPasswordError);
            return true;
        }
    }

    // Sửa hàm validateEmail (dùng DB)
    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvEmailError, iconEmailError, "Email không đúng định dạng!");
            return false;
        }
        if (dbHelper.checkEmailExists(email)) { // KIỂM TRA DB
            showError(etEmail, tvEmailError, iconEmailError, "Email không hợp lệ!");
            return false;
        }
        hideError(etEmail, tvEmailError, iconEmailError);
        return true;
    }

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
    private void activateContinueButton() {
        btnContinue.setEnabled(true);
        btnContinue.setBackgroundResource(R.drawable.button_background_green);
        btnContinue.setTextColor(Color.WHITE);
    }
    private void deactivateContinueButton() {
        btnContinue.setEnabled(false);
        btnContinue.setBackgroundResource(R.drawable.button_background_disabled);
        btnContinue.setTextColor(Color.parseColor("#AFAFAF"));
    }
}