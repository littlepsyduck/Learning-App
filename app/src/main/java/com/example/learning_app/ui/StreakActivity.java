package com.example.learning_app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.learning_app.R;
import com.example.learning_app.ui.fragment.FriendsTabFragment;
import com.example.learning_app.ui.fragment.PersonalStreakFragment;

public class StreakActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak);

        TextView tabPersonal = findViewById(R.id.tabPersonal);
        TextView tabFriends = findViewById(R.id.tabFriends);
        ImageView btnClose = findViewById(R.id.btnClose);

        // Load the initial fragment
        if (savedInstanceState == null) {
            loadFragment(new PersonalStreakFragment(), "personal");
        }

        // Handle tab clicks
        tabPersonal.setOnClickListener(v -> {
            loadFragment(new PersonalStreakFragment(), "personal");
            tabPersonal.setBackgroundResource(R.drawable.bg_tab_indicator);
            tabPersonal.setTextColor(Color.WHITE);
            tabFriends.setBackground(null);
            tabFriends.setTextColor(Color.parseColor("#CCFFFFFF"));
        });

        tabFriends.setOnClickListener(v -> {
            loadFragment(new FriendsTabFragment(), "friends");
            tabFriends.setBackgroundResource(R.drawable.bg_tab_indicator);
            tabFriends.setTextColor(Color.WHITE);
            tabPersonal.setBackground(null);
            tabPersonal.setTextColor(Color.parseColor("#CCFFFFFF"));
        });

        btnClose.setOnClickListener(v -> finish());
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.streak_content_container, fragment, tag);
        transaction.commit();
    }
}