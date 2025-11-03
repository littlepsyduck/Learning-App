package com.example.learning_app; // Thay bằng package của bạn

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvUsername, tvJoinDate, tvFriends, tvAvatarLetter;
    private DatabaseHelper dbHelper;
    private String loggedInUsername;

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

        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        tvFriends = view.findViewById(R.id.tvFriends);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);

        loadUserProfile();
    }

    private void loadUserProfile() {
        if (loggedInUsername == null) return;

        Cursor cursor = dbHelper.getUserDetails(loggedInUsername);
        if (cursor != null && cursor.moveToFirst()) {

            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            String joinDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_JOIN_DATE));
            int friendsCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FRIENDS));

            tvFullName.setText(fullName);
            tvUsername.setText(username);
            tvJoinDate.setText("Joined " + joinDate);
            tvFriends.setText(friendsCount + " Friends");

            if (fullName != null && !fullName.isEmpty()) {
                tvAvatarLetter.setText(String.valueOf(fullName.charAt(0)));
            }

            cursor.close();
        }
    }
}