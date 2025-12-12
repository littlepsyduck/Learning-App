package com.example.learning_app; // Đảm bảo đúng package

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Import Glide & CircleImageView
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

// Import Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileFragment extends Fragment {

    private TextView tvFullName, tvUsername, tvJoinDate, tvFriends, tvAvatarLetter;
    private CircleImageView ivAvatar;
    private ImageView ivSettings;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Ánh xạ View
        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        tvFriends = view.findViewById(R.id.tvFriends);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivSettings = view.findViewById(R.id.ivSettings);
        
        Button btnAddFriends = view.findViewById(R.id.btnAddFriends);
        btnAddFriends.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FriendsFragment())
                    .addToBackStack(null) // Optional: add to back stack so back button works natively
                    .commit();
        });

        // 3. Sự kiện nút Settings
        ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserSettingsActivity.class); // Tên cũ SettingsActivity
            startActivity(intent);
        });

        // 4. Tải dữ liệu
        loadUserProfileFromFirebase();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại dữ liệu khi quay lại từ màn hình Settings
        loadUserProfileFromFirebase();
    }

    private void loadUserProfileFromFirebase() {
        // Lấy user đang đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Chưa đăng nhập -> Có thể chuyển về màn hình Login
            return;
        }

        String uid = currentUser.getUid();

        // Truy vấn Firestore vào collection "users", document = uid
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Chuyển đổi document thành object UserModel
                            UserModel user = document.toObject(UserModel.class);

                            if (user != null) {
                                updateUI(user);
                            }
                        } else {
                            Toast.makeText(getContext(), "Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(UserModel user) {
        tvFullName.setText(user.getFullName());
        tvUsername.setText(user.getUsername());
        tvJoinDate.setText("Joined " + user.getJoinDate());
        tvFriends.setText(user.getFriendsCount() + " Friends");

        // Xử lý Avatar (Base64)
        //comment
        String avatarCode = user.getAvatarUrl(); // Lúc này là chuỗi mã hóa

        if (avatarCode != null && !avatarCode.isEmpty()) {
            try {
                // Giải mã chuỗi thành ảnh
                byte[] imageBytes = android.util.Base64.decode(avatarCode, android.util.Base64.DEFAULT);

                // Dùng Glide load ảnh từ byte[]
                if (isAdded()) {
                    Glide.with(this).load(imageBytes).into(ivAvatar);
                }
                tvAvatarLetter.setVisibility(View.GONE);
            } catch (Exception e) {
                // Nếu lỗi giải mã (ví dụ dữ liệu cũ), hiện mặc định
                showDefaultAvatar(user.getFullName());
            }
        } else {
            showDefaultAvatar(user.getFullName());
        }
    }
    private void showDefaultAvatar(String name) {
        ivAvatar.setImageResource(R.color.duo_green); // Màu mặc định
        tvAvatarLetter.setVisibility(View.VISIBLE);
        if (name != null && !name.isEmpty()) {
            tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }
}