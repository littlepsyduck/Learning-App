package com.example.learning_app.model;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.learning_app.db.AppDatabase;
import com.example.learning_app.db.UserDao;
import com.example.learning_app.db.UserEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {
    private UserDao userDao;
    private LiveData<UserEntity> userStats;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        userStats = userDao.getUserStats();
        // Tạo user mặc định nếu chưa có
        executor.execute(() -> {
            if (userDao.getUserStatsSync() == null) userDao.insert(new UserEntity());
        });
    }

    public LiveData<UserEntity> getUserStats() { return userStats; }

    public void completeLesson() {
        executor.execute(() -> {
            UserEntity user = userDao.getUserStatsSync();
            if (user != null) {
                user.xp += 15; // Tăng XP
                user.streak += 1; // Tăng Streak demo
                userDao.update(user);
            }
        });
    }
}