package com.example.learning_app; // Thay bằng package của bạn

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import android.content.Intent; // Import Intent

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// == THÊM IMPORT ==
import com.bumptech.glide.Glide;
import java.io.File;
import de.hdodenhof.circleimageview.CircleImageView; // Import CircleImageView

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvUsername, tvJoinDate, tvFriends, tvAvatarLetter;
    private DatabaseHelper dbHelper;
    private String loggedInUsername;
    private ImageView ivSettings; // Biến nút settings

    // == THÊM BIẾN AVATAR ==
    private CircleImageView ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            loggedInUsername = getArguments().getString("USERNAME");
        }

        dbHelper = new DatabaseHelper(getContext());

        // Ánh xạ (Map) các view
        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        tvFriends = view.findViewById(R.id.tvFriends);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);

        // == ÁNH XẠ AVATAR VÀ SETTINGS ==
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivSettings = view.findViewById(R.id.ivSettings);

        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra("USERNAME", loggedInUsername);
                startActivity(intent);
            }
        });

        loadUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile(); // Tự động load lại khi quay về từ Settings
    }

    // == SỬA LẠI HÀM NÀY ==
    private void loadUserProfile() {
        if (loggedInUsername == null) return;

        Cursor cursor = dbHelper.getUserDetails(loggedInUsername);
        if (cursor != null && cursor.moveToFirst()) {

            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            String joinDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_JOIN_DATE));
            int friendsCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FRIENDS));
            String avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR_PATH));

            // Set data cho các TextView
            tvFullName.setText(fullName);
            tvUsername.setText(username);
            tvJoinDate.setText("Joined " + joinDate);
            tvFriends.setText(friendsCount + " Friends");

            // Xử lý hiển thị Avatar
            if (avatarPath != null && !avatarPath.isEmpty()) {
                // Nếu có đường dẫn ảnh -> Load ảnh bằng Glide
                Glide.with(this).load(new File(avatarPath)).into(ivAvatar);
                tvAvatarLetter.setVisibility(View.GONE); // Ẩn chữ cái
            } else {
                // Nếu không có ảnh -> Hiển thị chữ cái đầu
                ivAvatar.setImageResource(R.color.duo_green); // (Bạn có thể đổi màu này)
                tvAvatarLetter.setVisibility(View.VISIBLE); // Hiện chữ cái
                if (fullName != null && !fullName.isEmpty()) {
                    tvAvatarLetter.setText(String.valueOf(fullName.charAt(0)));
                }
            }
            cursor.close();
        }
    }
}