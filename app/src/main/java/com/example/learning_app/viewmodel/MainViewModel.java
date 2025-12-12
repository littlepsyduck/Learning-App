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

import java.text.ParseException;
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
    // Changed to Integer: will hold the new streak value, or -1 if not the first lesson
    private MutableLiveData<Integer> firstLessonOfDayStreak = new MutableLiveData<>(-1);
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

    public LiveData<Integer> getFirstLessonOfDayStreak() {
        return firstLessonOfDayStreak;
    }

    public void resetFirstLessonFlag() {
        firstLessonOfDayStreak.postValue(-1);
    }

    public void reloadUserStats() {
        loadUserStats();
    }

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

    public void claimLessonRewards(int xpGained, int accuracy, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            UserEntity user = db.userDao().getUser();
            if (user != null) {
                user.xp += xpGained;
                String todayStr = sdf.format(new Date());

                if (!todayStr.equals(user.lastLessonDate)) {
                    boolean isFirstLessonEver = user.lastLessonDate == null || user.lastLessonDate.isEmpty();
                    
                    if (isFirstLessonEver) {
                        user.streak = 1;
                    } else {
                        try {
                            Date lastDate = sdf.parse(user.lastLessonDate);
                            Date today = sdf.parse(todayStr);
                            long diffInMillis = today.getTime() - lastDate.getTime();
                            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                            if (diffInDays == 1) {
                                user.streak += 1;
                            } else if (diffInDays > 1) {
                                if (user.streakFreezes > 0) {
                                    user.streakFreezes--; 
                                    user.lastFreezeDate = user.lastLessonDate;
                                } else {
                                    user.streak = 1; 
                                }
                            }
                        } catch (ParseException e) {
                            user.streak = 1; 
                        }
                    }
                    
                    user.lessonsWithHighAccuracy = 0;
                    user.lastLessonDate = todayStr;
                    firstLessonOfDayStreak.postValue(user.streak); // Post the new streak value
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
                if (user.streakFreezes < 2) {
                    user.streakFreezes += 1;
                    db.userDao().update(user);
                    syncToFirebase(user);
                    userStats.postValue(user);
                }
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
        data.put("lastFreezeDate", user.lastFreezeDate);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(USER_ID)
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> e.printStackTrace());
    }
}