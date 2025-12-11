package com.example.learning_app; // Đảm bảo đúng package của bạn

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Firebase Import
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserRegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etFullName, etAge, etEmail;
    private TextView tvUsernameError, tvPasswordError, tvEmailError;
    private ImageView iconUsernameError, iconPasswordError, iconEmailError, ivBack;
    private Button btnContinue;

    private String whyLearn;
    private String status;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register); // Đảm bảo tên layout XML đúng

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu
        Intent intent = getIntent();
        if (intent != null) {
            whyLearn = intent.getStringExtra("WHY_LEARN");
            status = intent.getStringExtra("STATUS");
        }

        initViews();

        ivBack.setOnClickListener(v -> finish());

        etUsername.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(validationWatcher);
        etFullName.addTextChangedListener(validationWatcher);
        etAge.addTextChangedListener(validationWatcher);
        etEmail.addTextChangedListener(validationWatcher);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserOnFirebase();
            }
        });
    }

    private void initViews() {
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
    }

    private void registerUserOnFirebase() {
        btnContinue.setEnabled(false);
        btnContinue.setText("Đang tạo..."); // Hoặc "Creating..."

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Tạo User Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // 2. Lưu thông tin vào Firestore
                                saveUserInfoToFirestore(firebaseUser.getUid());
                            }
                        } else {
                            btnContinue.setEnabled(true);
                            btnContinue.setText("CONTINUE");
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                showError(etEmail, tvEmailError, iconEmailError, "Email này đã tồn tại!");
                            } else {
                                Toast.makeText(UserRegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void saveUserInfoToFirestore(String uid) {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        // Lưu cả password vào Firestore (theo yêu cầu của bạn)
        String passwordRaw = etPassword.getText().toString().trim();

        int age = 0;
        try {
            age = Integer.parseInt(etAge.getText().toString().trim());
        } catch (NumberFormatException e) { age = 0; }

        // Ngày hiện tại
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // == KHỞI TẠO USER MODEL VỚI CÁC TRƯỜNG MỚI ==
        UserModel newUser = new UserModel(
                uid,
                fullName,
                username,
                email,
                passwordRaw, // Lưu password
                age,
                whyLearn,
                status,
                currentDate, // joinDate (ngày tham gia)
                currentDate  // lastDate (ngày đăng nhập cuối cũng là hôm nay)
        );

        // Các chỉ số streak, xp, freeze, friendsCount đã được set = 0 trong Constructor

        // Lưu lên Firestore
        db.collection("users").document(uid)
                .set(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(UserRegisterActivity.this, UserAllDoneActivity.class);
                            intent.putExtra("USERNAME", username);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            btnContinue.setEnabled(true);
                            btnContinue.setText("CONTINUE");
                            Toast.makeText(UserRegisterActivity.this, "Lỗi lưu data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        public void afterTextChanged(Editable s) { validateForm(); }
    };

    private void validateForm() {
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();
        boolean isEmailValid = validateEmail();
        boolean isFullNameValid = !etFullName.getText().toString().trim().isEmpty();
        boolean isAgeValid = !etAge.getText().toString().trim().isEmpty();

        if (isUsernameValid && isPasswordValid && isEmailValid && isFullNameValid && isAgeValid) {
            activateContinueButton();
        } else {
            deactivateContinueButton();
        }
    }

    private boolean validateUsername() {
        String username = etUsername.getText().toString().trim();
        if (username.length() < 4) {
            if (!username.isEmpty()) {
                showError(etUsername, tvUsernameError, iconUsernameError, "Username phải có ít nhất 4 ký tự!");
            }
            return false;
        }
        hideError(etUsername, tvUsernameError, iconUsernameError);
        return true;
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();
        if (password.length() < 6) {
            if (!password.isEmpty()) {
                showError(etPassword, tvPasswordError, iconPasswordError, "Mật khẩu quá ngắn!");
            }
            return false;
        }
        hideError(etPassword, tvPasswordError, iconPasswordError);
        return true;
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) return false;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvEmailError, iconEmailError, "Email không hợp lệ!");
            return false;
        }
        hideError(etEmail, tvEmailError, iconEmailError);
        return true;
    }

    // == SỬA TÊN DRAWABLE Ở ĐÂY CHO KHỚP VỚI FILE CŨ CỦA BẠN ==
    private void showError(EditText et, TextView tv, ImageView icon, String message) {
        // Dùng tên file cũ: edit_text_background_light_error
        et.setBackgroundResource(R.drawable.edit_text_background_light_error);
        tv.setText(message);
        tv.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);
    }

    private void hideError(EditText et, TextView tv, ImageView icon) {
        // Dùng tên file cũ: edit_text_background_light
        et.setBackgroundResource(R.drawable.edit_text_background_light);
        tv.setVisibility(View.GONE);
        icon.setVisibility(View.GONE);
    }

    private void activateContinueButton() {
        btnContinue.setEnabled(true);
        // Dùng tên file cũ: button_background_green
        btnContinue.setBackgroundResource(R.drawable.button_background_green);
        btnContinue.setTextColor(Color.WHITE);
    }

    private void deactivateContinueButton() {
        btnContinue.setEnabled(false);
        // Dùng tên file cũ: button_background_disabled
        btnContinue.setBackgroundResource(R.drawable.button_background_disabled);
        btnContinue.setTextColor(Color.parseColor("#AFAFAF"));
    }
}