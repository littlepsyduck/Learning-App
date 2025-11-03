package com.example.learning_app; // Thay bằng package của bạn

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Nhận username
        loggedInUsername = getIntent().getStringExtra("USERNAME");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setItemIconTintList(null);
        // Mặc định chọn tab Profile (icon cuối cùng)
        bottomNav.setSelectedItemId(R.id.nav_profile);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                createProfileFragment()).commit();

        // Xử lý sự kiện click
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_profile) {
                selectedFragment = createProfileFragment();
            } else if (itemId == R.id.nav_home) {
                // selectedFragment = new HomeFragment(); // (Fragment cho tab 1)
            } else if (itemId == R.id.nav_shield) {
                // selectedFragment = new ShieldFragment(); // (Fragment cho tab 2)
            }
            // ... (thêm 2 else if cho 2 icon còn lại)

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    // Hàm helper để tạo ProfileFragment và gửi data (USERNAME)
    private Fragment createProfileFragment() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("USERNAME", loggedInUsername);
        fragment.setArguments(args);
        return fragment;
    }
}