package com.example.learning_app; // Thay bằng package của bạn

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettingsActivity extends AppCompatActivity {

    // ... (các biến cũ)
    private EditText etFullName, etUsername, etPassword, etAge, etEmail;
    private Button btnSave, btnLogout, btnDelete;
    private ImageView ivBack;

    // == THÊM CÁC BIẾN MỚI ==
    private Button btnChooseFile;
    private TextView tvFileSelected;
    private CircleImageView ivAvatarPreview;
    private String newAvatarPath = null; // Đường dẫn ảnh mới (nếu user chọn)
    private String currentAvatarPath = null; // Đường dẫn ảnh cũ (từ DB)

    private UserDatabaseHelper dbHelper;
    private String loggedInUsername;
    private String currentPassword;

    // Trình khởi chạy để chọn ảnh
    private ActivityResultLauncher<String> mGetContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        dbHelper = new UserDatabaseHelper(this);
        loggedInUsername = getIntent().getStringExtra("USERNAME");

        // Ánh xạ (Map) các views cũ
        ivBack = findViewById(R.id.ivBack);
        // ... (etFullName, etUsername, ... btnDelete) ...
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        btnDelete = findViewById(R.id.btnDelete);

        // == ÁNH XẠ VIEWS MỚI ==
        btnChooseFile = findViewById(R.id.btnChooseFile);
        tvFileSelected = findViewById(R.id.tvFileSelected);
        ivAvatarPreview = findViewById(R.id.ivAvatarPreview);

        // Đăng ký trình chọn ảnh
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Callback: Khi ảnh đã được chọn
                    if (uri != null) {
                        // 1. Copy ảnh vào thư mục riêng của app
                        newAvatarPath = saveImageToInternalStorage(uri, loggedInUsername);

                        // 2. Hiển thị ảnh xem trước
                        Glide.with(this).load(newAvatarPath).into(ivAvatarPreview);
                        ivAvatarPreview.setVisibility(View.VISIBLE);
                        tvFileSelected.setText(loggedInUsername + ".jpg"); // Tên file
                    }
                });

        // Load data hiện tại (bao gồm cả avatar)
        loadCurrentData();

        // ---- XỬ LÝ SỰ KIỆN CLICK ----
        ivBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSave());
        btnLogout.setOnClickListener(v -> handleLogout());
        btnDelete.setOnClickListener(v -> handleDelete());

        // Click nút "Choose File"
        btnChooseFile.setOnClickListener(v -> {
            // Yêu cầu quyền trước khi mở (nếu cần - Android 6.0+)
            // ... (Code xin quyền có thể thêm ở đây) ...

            // Mở thư viện ảnh
            mGetContent.launch("image/*");
        });
    }

    private void loadCurrentData() {
        Cursor cursor = dbHelper.getUserDetails(loggedInUsername);
        if (cursor != null && cursor.moveToFirst()) {
            // ... (set text cho etFullName, etUsername...)
            etFullName.setText(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_FULL_NAME)));
            etUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USERNAME)));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_EMAIL)));
            etAge.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_AGE))));
            currentPassword = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_PASSWORD));

            // == LOAD AVATAR HIỆN TẠI ==
            currentAvatarPath = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_AVATAR_PATH));
            if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
                Glide.with(this).load(new File(currentAvatarPath)).into(ivAvatarPreview);
                ivAvatarPreview.setVisibility(View.VISIBLE);
                tvFileSelected.setText(new File(currentAvatarPath).getName());
            }

            cursor.close();
        }
    }

    private void handleSave() {
        // ... (code lấy newFullName, newUsername, newPassword... của bạn)
        String newFullName = etFullName.getText().toString().trim();
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        int newAge = Integer.parseInt(etAge.getText().toString().trim());
        String newPassword = etPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            newPassword = currentPassword;
        }

        // 1. Cập nhật thông tin (Tên, pass, email, v.v.)
        dbHelper.updateUser(loggedInUsername, newFullName, newUsername, newPassword, newAge, newEmail);

        // 2. Cập nhật Avatar (nếu người dùng chọn ảnh mới)
        if (newAvatarPath != null) {
            dbHelper.updateAvatarPath(newUsername, newAvatarPath); // Dùng newUsername vì user có thể đổi cả username
        }

        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // == THÊM HÀM HELPER NÀY ==
    // Hàm này copy ảnh từ thư viện vào thư mục "avatars" riêng của app
    private String saveImageToInternalStorage(Uri uri, String username) {
        // Tạo thư mục "avatars" nếu chưa có
        File directory = getDir("avatars", Context.MODE_PRIVATE);

        // Tạo file đích (ví dụ: /data/data/com.example.learning_app/app_avatars/username.jpg)
        File destinationFile = new File(directory, username + ".jpg");

        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Trả về đường dẫn tuyệt đối của file đã lưu
            return destinationFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ... (code handleLogout() và handleDelete() giữ nguyên) ...
    private void handleLogout() {
        // Quay về màn hình MainActivity và xóa tất cả Activity
        Intent intent = new Intent(UserSettingsActivity.this, UserWelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void handleDelete() {
        // Hiển thị hộp thoại xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản này? Toàn bộ dữ liệu sẽ bị mất vĩnh viễn.")
                .setPositiveButton("XÓA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ti hành xóa
                        dbHelper.deleteUser(loggedInUsername);
                        Toast.makeText(UserSettingsActivity.this, "Tài khoản đã bị xóa.", Toast.LENGTH_SHORT).show();
                        // Quay về màn hình chính (giống Logout)
                        handleLogout();
                    }
                })
                .setNegativeButton("HỦY", null) // Không làm gì cả
                .show();
    }
}