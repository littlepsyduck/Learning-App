package com.example.learning_app.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.Lesson;
import com.example.learning_app.ui.adapter.LessonAdapter;
import com.example.learning_app.ui.fragment.FriendsFragment;
import com.example.learning_app.ui.fragment.LeaderboardFragment;
import com.example.learning_app.ui.fragment.QuestFragment;
import com.example.learning_app.ui.fragment.UserProfileFragment;
import com.example.learning_app.viewmodel.LessonViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LessonAdapter adapter;
    private RecyclerView rvLessons;
    private TextView tvSectionTitle, tvLessonName;
    private LessonViewModel mLessonViewModel;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(MainActivity.this, UserWelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        rvLessons = findViewById(R.id.rvLessons);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvLessonName = findViewById(R.id.tvLessonName);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        if (bottomNav != null) {
            bottomNav.setItemIconTintList(null);
            bottomNav.setSelectedItemId(R.id.nav_home);
            setupBottomNavigation();
        }

        // Đảo ngược layout để scroll từ dưới lên
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvLessons.setLayoutManager(layoutManager);
        rvLessons.setNestedScrollingEnabled(true);
        rvLessons.setHasFixedSize(false);

        adapter = new LessonAdapter(this, lesson -> {
            updateHeader(lesson);
            showStartDialog(lesson);
        });
        rvLessons.setAdapter(adapter);

        // Get ViewModel instance
        mLessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Import data from JSON if database is empty
        mLessonViewModel.importDataFromJson();

        // Observe LiveData from ViewModel
        observeLessons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // LiveData sẽ tự động cập nhật khi có thay đổi trong database
        // Không cần observe lại vì đã observe trong onCreate
        // Chỉ cần đảm bảo ViewModel đã được khởi tạo
    }

    private void showStartDialog(Lesson lesson) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_start_lesson);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvDialogSubtitle);
        Button btnStart = dialog.findViewById(R.id.btnStartLesson);

        tvTitle.setText(lesson.name);
        tvSubtitle.setText("Section " + lesson.sectionId);

        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            // Mở LessonActivity
            Intent intent = new Intent(MainActivity.this, LessonActivity.class);
            intent.putExtra("lessonId", lesson.id);
            startActivity(intent);
            // Không finish MainActivity để có thể quay về sau
        });

        dialog.show();
    }

    // Observe LiveData from ViewModel
    private void observeLessons() {
        LiveData<List<Lesson>> lessonsLiveData = mLessonViewModel.getAllLessons();
        
        Observer<List<Lesson>> lessonsObserver = new Observer<List<Lesson>>() {
            @Override
            public void onChanged(List<Lesson> lessons) {
                if (lessons != null && !lessons.isEmpty()) {
                    adapter.setLessons(lessons);
                    Lesson currentLesson = findCurrentLesson(lessons);
                    updateHeader(currentLesson);
                    scrollToCurrentLesson(lessons);
                }
            }
        };
        
        lessonsLiveData.observe(this, lessonsObserver);
    }

    private Lesson findCurrentLesson(List<Lesson> lessons) {
        for (Lesson l : lessons) {
            if (!l.isLocked) return l;
        }
        return !lessons.isEmpty() ? lessons.get(0) : null;
    }
    
    private void scrollToCurrentLesson(List<Lesson> lessons) {
        int position = -1;
        for (int i = 0; i < lessons.size(); i++) {
            if (!lessons.get(i).isLocked) {
                position = i;
                break;
            }
        }
        
        if (position >= 0) {
            final int finalPosition = position;
            rvLessons.post(() -> {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvLessons.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(finalPosition, 0);
                }
            });
        }
    }

    private void updateHeader(Lesson lesson) {
        if (lesson == null) return;
        tvLessonName.setText(lesson.name);

        String sectionName = "";
        switch (lesson.sectionId) {
            case 1: sectionName = "SECTION 1: EASY STARTER"; break;
            case 2: sectionName = "SECTION 2: MEDIUM BOOST"; break;
            case 3: sectionName = "SECTION 3: HARD MASTER"; break;
            default: sectionName = "SECTION " + lesson.sectionId; break;
        }
        
        if (lesson.topic != null && !lesson.topic.isEmpty()) {
            tvSectionTitle.setText(sectionName + " - " + lesson.topic);
        } else {
            tvSectionTitle.setText(sectionName);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_chest || itemId == R.id.nav_shop || 
                       itemId == R.id.nav_shield || itemId == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
                if (itemId == R.id.nav_chest) {
                    intent.putExtra("SELECTED_TAB", "quest");
                } else if (itemId == R.id.nav_shop) {
                    intent.putExtra("SELECTED_TAB", "friends");
                } else if (itemId == R.id.nav_shield) {
                    intent.putExtra("SELECTED_TAB", "leaderboard");
                } else if (itemId == R.id.nav_profile) {
                    intent.putExtra("SELECTED_TAB", "profile");
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

}

