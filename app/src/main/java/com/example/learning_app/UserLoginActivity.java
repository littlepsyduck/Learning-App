package com.example.learning_app; // Đảm bảo đúng package

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Firebase Imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword; // etUsername sẽ dùng để nhập Email
    private TextView tvUsernameError, tvPasswordError;
    private ImageView iconUsernameError, iconPasswordError, ivBack;
    private Button btnLogin;

    // Khai báo Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login); // Đảm bảo tên layout đúng

        // 1. Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Ánh xạ View
        ivBack = findViewById(R.id.ivBack);
        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername); // Lưu ý: Người dùng sẽ nhập Email vào đây
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        iconUsernameError = findViewById(R.id.iconUsernameError);
        iconPasswordError = findViewById(R.id.iconPasswordError);

        ivBack.setOnClickListener(v -> finish());

        // 3. Xử lý nút Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithFirebase();
            }
        });

        // TextWatcher để kích hoạt nút
        etUsername.addTextChangedListener(loginTextWatcher);
        etPassword.addTextChangedListener(loginTextWatcher);
    }

    private void loginWithFirebase() {
        String email = etUsername.getText().toString().trim(); // Lấy email từ ô username
        String password = etPassword.getText().toString().trim();

        // Kiểm tra sơ bộ
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(UserLoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // GỌI HÀM ĐĂNG NHẬP CỦA FIREBASE
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN"); // Hoặc dùng string resource

                        if (task.isSuccessful()) {
                            // 1. Cập nhật ngày đăng nhập cuối cùng (last_date)
                            FirebaseUser user = mAuth.getCurrentUser();
                            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                            // Gọi Firestore update
                            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                    .update("lastDate", today); // Chỉ update đúng 1 cột này

                            // 2. Chuyển màn hình
                            hideError(etUsername, tvUsernameError, iconUsernameError);
                            hideError(etPassword, tvPasswordError, iconPasswordError);

                            Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            // Đăng nhập thất bại
                            showError(etPassword, tvPasswordError, iconPasswordError, "Sai email hoặc mật khẩu!");
                            // Toast.makeText(UserLoginActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            String email = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (!email.isEmpty() && !pass.isEmpty()) {
                activateLoginButton();
            } else {
                deactivateLoginButton();
            }
        }
    };

    // Hàm hỗ trợ giao diện (Sửa tên Drawable cho khớp file cũ của bạn)
    private void showError(EditText et, TextView tv, ImageView icon, String message) {
        et.setBackgroundResource(R.drawable.edit_text_background_light_error); // Tên file cũ
        tv.setText(message);
        tv.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);
    }

    private void hideError(EditText et, TextView tv, ImageView icon) {
        et.setBackgroundResource(R.drawable.edit_text_background_light); // Tên file cũ
        tv.setVisibility(View.GONE);
        icon.setVisibility(View.GONE);
    }

    private void activateLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setBackgroundResource(R.drawable.button_background_green); // Tên file cũ
        btnLogin.setTextColor(Color.WHITE);
    }

    private void deactivateLoginButton() {
        btnLogin.setEnabled(false);
        btnLogin.setBackgroundResource(R.drawable.button_background_disabled); // Tên file cũ
        btnLogin.setTextColor(Color.parseColor("#AFAFAF"));
    }
}