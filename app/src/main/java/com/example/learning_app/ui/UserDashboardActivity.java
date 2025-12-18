package com.example.learning_app.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.learning_app.R;
import com.example.learning_app.ui.fragment.FriendsFragment;
import com.example.learning_app.ui.fragment.LeaderboardFragment;
import com.example.learning_app.ui.fragment.QuestFragment;
import com.example.learning_app.ui.fragment.SpeechPracticeFragment;
import com.example.learning_app.ui.fragment.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserDashboardActivity extends AppCompatActivity {

    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(UserDashboardActivity.this, UserWelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_user_dashboard);

        loggedInUsername = getIntent().getStringExtra("USERNAME");
        if (loggedInUsername == null && currentUser.getEmail() != null) {
            loggedInUsername = currentUser.getEmail();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setItemIconTintList(null);

        String selectedTab = getIntent().getStringExtra("SELECTED_TAB");
        Fragment initialFragment = null;
        int selectedItemId = R.id.nav_profile;

        if (selectedTab != null) {
            switch (selectedTab) {
                case "quest":
                    initialFragment = new QuestFragment();
                    selectedItemId = R.id.nav_chest;
                    break;
                case "speech":
                    initialFragment = new SpeechPracticeFragment();
                    selectedItemId = R.id.nav_shop;
                    break;
                case "leaderboard":
                    initialFragment = new LeaderboardFragment();
                    selectedItemId = R.id.nav_shield;
                    break;
                case "profile":
                    initialFragment = createProfileFragment();
                    selectedItemId = R.id.nav_profile;
                    break;
            }
        } else {
            initialFragment = createProfileFragment();
        }

        bottomNav.setSelectedItemId(selectedItemId);

        if (initialFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, initialFragment)
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // Finish DashboardActivity to maintain consistent back stack
                return true;
            } else if (itemId == R.id.nav_chest) {
                selectedFragment = new QuestFragment();
            } else if (itemId == R.id.nav_shop) {
                selectedFragment = new SpeechPracticeFragment();
            } else if (itemId == R.id.nav_shield) {
                selectedFragment = new LeaderboardFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = createProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    private Fragment createProfileFragment() {
        UserProfileFragment fragment = new UserProfileFragment();
        if (loggedInUsername != null) {
            Bundle args = new Bundle();
            args.putString("USERNAME", loggedInUsername);
            fragment.setArguments(args);
        }
        return fragment;
    }
}
