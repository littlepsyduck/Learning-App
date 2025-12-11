package com.example.learning_app;

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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettingsActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword, etAge, etEmail;
    private Button btnSave, btnLogout, btnDelete, btnChooseFile;
    private ImageView ivBack;
    private TextView tvFileSelected;
    private CircleImageView ivAvatarPreview;

    // Firebase (Chỉ cần Auth và Firestore, bỏ Storage)
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ActivityResultLauncher<String> mGetContent;
    private String encodedImageBase64 = null; // Biến lưu chuỗi ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        initViews();
        setupImagePicker();
        loadUserDataFromFirebase();

        ivBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSave());
        btnLogout.setOnClickListener(v -> handleLogout());
        btnDelete.setOnClickListener(v -> handleDelete());

        btnChooseFile.setOnClickListener(v -> mGetContent.launch("image/*"));
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
                // Xử lý nén ảnh và chuyển thành Base64
                encodedImageBase64 = encodeImageToBase64(uri);
                if (encodedImageBase64 != null) {
                    // Hiển thị ảnh vừa chọn lên màn hình
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

    // == HÀM MỚI: Nén ảnh và chuyển thành chuỗi Base64 ==
    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Nén ảnh xuống kích thước nhỏ (quan trọng để lưu vào Firestore)
            // Resize ảnh về tối đa 300x300
            bitmap = getResizedBitmap(bitmap, 300);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // Nén thành JPEG chất lượng 80%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm phụ trợ resize ảnh
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

    private void loadUserDataFromFirebase() {
        String uid = currentUser.getUid();
        etEmail.setEnabled(false);
        etUsername.setEnabled(true);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            etFullName.setText(user.getFullName());
                            etUsername.setText(user.getUsername());
                            etEmail.setText(user.getEmail());
                            etAge.setText(String.valueOf(user.getAge()));

                            // Load Avatar (giải mã Base64)
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
                    }
                });
    }

    private void handleSave() {
        String newFullName = etFullName.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        int newAge = 0;
        try { newAge = Integer.parseInt(etAge.getText().toString().trim()); } catch (Exception e) {}

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName);
        updates.put("age", newAge);
        updates.put("username", newUsername);

        // Lưu chuỗi ảnh Base64 vào cột avatarUrl
        if (encodedImageBase64 != null) {
            updates.put("avatarUrl", encodedImageBase64);
        }

        String newPass = etPassword.getText().toString().trim();
        if (!newPass.isEmpty()) {
            currentUser.updatePassword(newPass);
            updates.put("password", newPass);
        }

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleLogout() {
        mAuth.signOut();
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
                    db.collection("users").document(currentUser.getUid()).delete();
                    currentUser.delete().addOnCompleteListener(task -> {
                        Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserWelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                })
                .setNegativeButton("HỦY", null).show();
    }
}