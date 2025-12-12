package com.example.learning_app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.learning_app.db.AppDatabase;
import com.example.learning_app.db.UserEntity;
// Bỏ comment các import Firebase nếu đã cài
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainViewModel extends AndroidViewModel {

    private AppDatabase db;
    private MutableLiveData<UserEntity> userStats = new MutableLiveData<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final String USER_ID = "user_01"; // ID người dùng giả định

    public MainViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        loadUserStats();
    }

    public LiveData<UserEntity> getUserStats() {
        return userStats;
    }

    public void reloadUserStats() {
        loadUserStats();
    }

    private void loadUserStats() {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user == null) {
                user = new UserEntity();
                user.xp = 0;
                user.streak = 0;
                user.hearts = 5; // Cấp 5 tim ban đầu
                user.streakFreezes = 2;
                user.lastLessonDate = "";
                db.userDao().insert(user);
                syncToFirebase(user);
            } else {
                checkStreakStatus(user);
            }
            userStats.postValue(user);
        });
    }

    private void checkStreakStatus(UserEntity user) {
        // [Logic kiểm tra và xử lý mất streak/freeze giữ nguyên]
        if (user.lastLessonDate == null || user.lastLessonDate.isEmpty()) {
            return;
        }
        // ... (Logic kiểm tra ngày lỡ và trừ freeze) ...
    }


    // --- HÀM MỚI: CHỈ TRỪ ĐI 1 TIM (GỌI KHI LÀM SAI) ---
    public void decrementHeart() {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user != null) {
                if (user.hearts > 0) {
                    user.hearts -= 1; // CHỈ TRỪ 1 TIM

                    db.userDao().update(user);
                    syncToFirebase(user);
                    userStats.postValue(user);
                }
            }
        });
    }


    // --- HÀM CLAIM REWARDS (CHỈ TĂNG XP/STREAK - KHÔNG TRỪ TIM) ---
    public void claimLessonRewards(int xpGained) {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user != null) {
                user.xp += xpGained; // Tăng XP

                // LOGIC TRỪ TIM ĐÃ BỊ XÓA KHỎI ĐÂY!

                String today = sdf.format(new Date());

                if (user.lastLessonDate == null || !user.lastLessonDate.equals(today)) {
                    user.streak += 1;
                    user.lastLessonDate = today;
                }

                db.userDao().update(user);
                syncToFirebase(user);
                userStats.postValue(user);
            }
        });
    }

    public void addFreeze() {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user != null) {
                user.streakFreezes += 1;
                db.userDao().update(user);
                syncToFirebase(user);
                userStats.postValue(user);
            }
        });
    }

    // Hàm đồng bộ Firebase (giữ nguyên logic Firestore cơ bản)
    private void syncToFirebase(UserEntity user) {
        Map<String, Object> data = new HashMap<>();
        data.put("xp", user.xp);
        data.put("streak", user.streak);
        data.put("streakFreezes", user.streakFreezes);
        data.put("lastLessonDate", user.lastLessonDate);
        data.put("hearts", user.hearts); // Thêm trường hearts

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(USER_ID)
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> e.printStackTrace());
    }
}