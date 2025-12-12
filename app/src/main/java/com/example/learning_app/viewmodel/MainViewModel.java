package com.example.learning_app.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.learning_app.db.AppDatabase;
import com.example.learning_app.db.UserEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private AppDatabase db;
    private MutableLiveData<UserEntity> userStats = new MutableLiveData<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final String USER_ID = "user_01";

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

    // Simplified: Just loads the user data without modifying it.
    private void loadUserStats() {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user == null) {
                user = new UserEntity();
                db.userDao().insert(user);
                syncToFirebase(user);
            }
            userStats.postValue(user);
        });
    }

    public void decrementHeart(@Nullable Runnable onComplete) {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user != null && user.hearts > 0) {
                user.hearts -= 1;
                db.userDao().update(user);
                syncToFirebase(user);
                userStats.postValue(user);
            }
            if (onComplete != null) {
                new Handler(Looper.getMainLooper()).post(onComplete);
            }
        });
    }

    // All date and streak logic is now centralized here.
    public void claimLessonRewards(int xpGained, int accuracy, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user != null) {
                user.xp += xpGained;
                String todayStr = sdf.format(new Date());

                // Check if the last lesson was on a different day
                if (user.lastLessonDate == null || user.lastLessonDate.isEmpty() || !todayStr.equals(user.lastLessonDate)) {
                    // It's a new day for learning.
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    String yesterdayStr = sdf.format(cal.getTime());

                    // If last lesson was yesterday, increment streak. Otherwise, reset it to 1.
                    if (yesterdayStr.equals(user.lastLessonDate)) {
                        user.streak += 1;
                    } else {
                        user.streak = 1; // Start a new streak
                    }
                    
                    // Reset daily quests
                    user.lessonsWithHighAccuracy = 0;
                    user.lastLessonDate = todayStr;
                }

                if (accuracy >= 90) {
                    user.lessonsWithHighAccuracy += 1;
                }

                db.userDao().update(user);
                syncToFirebase(user);
                userStats.postValue(user);
            }
             if (onComplete != null) {
                new Handler(Looper.getMainLooper()).post(onComplete);
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

    private void syncToFirebase(UserEntity user) {
        Map<String, Object> data = new HashMap<>();
        data.put("xp", user.xp);
        data.put("streak", user.streak);
        data.put("streakFreezes", user.streakFreezes);
        data.put("lastLessonDate", user.lastLessonDate);
        data.put("hearts", user.hearts);
        data.put("lessonsWithHighAccuracy", user.lessonsWithHighAccuracy);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(USER_ID)
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> e.printStackTrace());
    }
}