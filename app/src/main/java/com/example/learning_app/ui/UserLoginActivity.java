package com.example.learning_app.ui;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import com.example.learning_app.R;
import com.example.learning_app.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvPasswordError, tvForgotPassword;
    private ImageView iconUsernameError, iconPasswordError, ivBack;
    private Button btnLogin;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        initViews();
        setupObservers();

        ivBack.setOnClickListener(v -> finish());
        btnLogin.setOnClickListener(v -> loginWithFirebase());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        etUsername.addTextChangedListener(loginTextWatcher);
        etPassword.addTextChangedListener(loginTextWatcher);
    }

    private void setupObservers() {
        userViewModel.getLoginResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success != null) {
                    if (success) {
                        FirebaseUser user = userViewModel.getCurrentFirebaseUser();
                        if (user != null) {
                            updateLastDateAndGoHome(user);
                        }
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN");
                        String error = userViewModel.getErrorMessage().getValue();
                        if (error != null && error.contains("xác thực")) {
                            Toast.makeText(UserLoginActivity.this, error, Toast.LENGTH_LONG).show();
                        } else {
                            showError(etPassword, tvPasswordError, iconPasswordError, error != null ? error : "Sai email hoặc mật khẩu!");
                        }
                    }
                }
            }
        });
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        iconUsernameError = findViewById(R.id.iconUsernameError);
        iconPasswordError = findViewById(R.id.iconPasswordError);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void loginWithFirebase() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");
        userViewModel.login(email, password);
    }

    private void updateLastDateAndGoHome(FirebaseUser user) {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .update("lastDate", today);

        hideError(etUsername, tvUsernameError, iconUsernameError);
        hideError(etPassword, tvPasswordError, iconPasswordError);

        Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showForgotPasswordDialog() {
        EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Đặt lại mật khẩu");
        passwordResetDialog.setMessage("Nhập email của bạn để nhận link đổi mật khẩu.");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Gửi", (dialog, which) -> {
            String mail = resetMail.getText().toString().trim();
            if (mail.isEmpty()) {
                Toast.makeText(UserLoginActivity.this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
                return;
            }
            userViewModel.sendPasswordResetEmail(mail);
            Toast.makeText(UserLoginActivity.this, "Link đổi mật khẩu đã gửi về Email của bạn.", Toast.LENGTH_LONG).show();
        });

        passwordResetDialog.setNegativeButton("Hủy", (dialog, which) -> {});
        passwordResetDialog.create().show();
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



