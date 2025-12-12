package com.example.learning_app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.learning_app.R;
import com.example.learning_app.ui.fragment.HomeFragment;
import com.example.learning_app.ui.fragment.LessonCompleteFragment;
import com.example.learning_app.ui.fragment.QuestFragment;
import com.example.learning_app.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txtStreak = findViewById(R.id.txtStreakMain);
        TextView txtXP = findViewById(R.id.txtXPMain);
        TextView txtHearts = findViewById(R.id.txtHeartsMain);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        View streakContainer = findViewById(R.id.streakContainer);
        View topBar = findViewById(R.id.topBar);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getUserStats().observe(this, user -> {
            if (user != null) {
                if (txtStreak != null) txtStreak.setText(String.valueOf(user.streak));
                if (txtXP != null) txtXP.setText(String.valueOf(user.xp));
                if (txtHearts != null) txtHearts.setText(String.valueOf(user.hearts));
            }
        });

        if (streakContainer != null) {
            streakContainer.setOnClickListener(v -> {
                // Giả sử StreakActivity nằm trong package ui
                Intent intent = new Intent(MainActivity.this, StreakActivity.class);
                startActivity(intent);
            });
        }

        if (bottomNav != null) {
            bottomNav.setItemIconTintList(null);
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    if (topBar != null) topBar.setVisibility(View.VISIBLE);
                    updateStatusBarColor("#FFFFFF", true);
                } else if (itemId == R.id.nav_shield) {
                    selectedFragment = new HomeFragment();
                    if (topBar != null) topBar.setVisibility(View.VISIBLE);
                    updateStatusBarColor("#FFFFFF", true);
                } else if (itemId == R.id.nav_chest) {
                    selectedFragment = new QuestFragment();
                    if (topBar != null) topBar.setVisibility(View.GONE);
                    updateStatusBarColor("#8761D6", false);
                } else if (itemId == R.id.nav_shop) {
                    selectedFragment = new HomeFragment();
                    if (topBar != null) topBar.setVisibility(View.VISIBLE);
                    updateStatusBarColor("#FFFFFF", true);
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new HomeFragment();
                    if (topBar != null) topBar.setVisibility(View.VISIBLE);
                    updateStatusBarColor("#FFFFFF", true);
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            });
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_home);
            if (topBar != null) topBar.setVisibility(View.VISIBLE);
            updateStatusBarColor("#FFFFFF", true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.reloadUserStats();
        }
    }

    // Hiển thị màn hình hoàn thành bài học (toàn màn hình)
    public void showLessonCompleteScreen(int xp, int accuracy, String timeSpent) {
        LessonCompleteFragment dialogFragment = LessonCompleteFragment.newInstance(xp, accuracy, timeSpent);
        dialogFragment.show(getSupportFragmentManager(), "LessonCompleteFragment");
    }

    private void updateStatusBarColor(String colorHex, boolean isLightIcon) {
        try {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor(colorHex));
            WindowInsetsControllerCompat windowInsetsController = new WindowInsetsControllerCompat(window, window.getDecorView());
            windowInsetsController.setAppearanceLightStatusBars(isLightIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}