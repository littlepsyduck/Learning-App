package com.example.learning_app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.example.learning_app.R;
import com.example.learning_app.entities.User;
import com.example.learning_app.viewmodel.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettingsActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword, etAge, etEmail;
    private Button btnSave, btnLogout, btnDelete, btnChooseFile;
    private ImageView ivBack;
    private TextView tvFileSelected;
    private CircleImageView ivAvatarPreview;

    private UserViewModel userViewModel;

    private ActivityResultLauncher<String> mGetContent;
    private String encodedImageBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        if (userViewModel.getCurrentFirebaseUser() == null) {
            finish();
            return;
        }

        initViews();
        setupImagePicker();
        setupObservers();
        
        if (userViewModel.getCurrentFirebaseUser() != null) {
            userViewModel.loadUserProfile(userViewModel.getCurrentFirebaseUser().getUid());
        }

        ivBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSave());
        btnLogout.setOnClickListener(v -> handleLogout());
        btnDelete.setOnClickListener(v -> handleDelete());
        btnChooseFile.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    loadUserData(user);
                }
            }
        });
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        btnDelete = findViewById(R.id.btnDelete);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        tvFileSelected = findViewById(R.id.tvFileSelected);
        ivAvatarPreview = findViewById(R.id.ivAvatarPreview);
    }

    private void setupImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                encodedImageBase64 = encodeImageToBase64(uri);
                if (encodedImageBase64 != null) {
                    byte[] imageBytes = Base64.decode(encodedImageBase64, Base64.DEFAULT);
                    Glide.with(this).load(imageBytes).into(ivAvatarPreview);
                    ivAvatarPreview.setVisibility(View.VISIBLE);
                    tvFileSelected.setText("Đã chọn ảnh xong! Bấm SAVE.");
                } else {
                    Toast.makeText(this, "Ảnh quá lớn hoặc lỗi!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = getResizedBitmap(bitmap, 300);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void loadUserData(User user) {
        etEmail.setEnabled(false);
        etUsername.setEnabled(true);

        etFullName.setText(user.getFullName());
        etUsername.setText(user.getUsername());
        etEmail.setText(user.getEmail());
        etAge.setText(String.valueOf(user.getAge()));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                Glide.with(this).load(imageBytes).into(ivAvatarPreview);
                ivAvatarPreview.setVisibility(View.VISIBLE);
                tvFileSelected.setText("Avatar hiện tại");
            } catch (Exception e) {
                // Bỏ qua lỗi nếu ảnh cũ là link http
            }
        }
    }

    private void handleSave() {
        String newFullName = etFullName.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        int newAge = 0;
        try { newAge = Integer.parseInt(etAge.getText().toString().trim()); } catch (Exception e) {}

        String newPass = etPassword.getText().toString().trim();
        String passwordToUpdate = newPass.isEmpty() ? null : newPass;

        if (userViewModel.getCurrentFirebaseUser() != null) {
            userViewModel.updateUserProfile(
                    userViewModel.getCurrentFirebaseUser().getUid(),
                    newFullName,
                    newUsername,
                    newAge,
                    encodedImageBase64,
                    passwordToUpdate
            );
            Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handleLogout() {
        userViewModel.logout();
        Intent intent = new Intent(this, UserWelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage("Xóa tài khoản vĩnh viễn?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    if (userViewModel.getCurrentFirebaseUser() != null) {
                        userViewModel.deleteUserAccount(userViewModel.getCurrentFirebaseUser().getUid());
                        Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserWelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("HỦY", null).show();
    }
}



