package com.example.learning_app.model;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public class UserProgressModel {
    private SharedPreferences prefs;

    private static final String KEY_STREAK = "current_streak";
    private static final String KEY_XP = "current_xp";
    private static final String KEY_DATES = "completed_dates";
    private static final String KEY_HEARTS = "user_hearts";

    public UserProgressModel(Context context) {
        prefs = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
    }

    /**
     * Updates user progress after completing a lesson.
     * @param xpGained The amount of XP earned from the lesson.
     */
    public void updateProgress(int xpGained) {
        // Add XP
        int currentXP = getCurrentXP();
        prefs.edit().putInt(KEY_XP, currentXP + xpGained).apply();

        // Update streak
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Set<String> completedDates = getCompletedDates();

        if (!completedDates.contains(today)) {
            completedDates.add(today);
            prefs.edit().putStringSet(KEY_DATES, completedDates).apply();

            int currentStreak = getStreak();
            prefs.edit().putInt(KEY_STREAK, currentStreak + 1).apply();
        }
    }
    
    /**
     * Decreases the number of hearts.
     */
    public void decreaseHearts() {
        int currentHearts = getHearts();
        if (currentHearts > 0) {
            prefs.edit().putInt(KEY_HEARTS, currentHearts - 1).apply();
        }
    }


    public Set<String> getCompletedDates() {
        return prefs.getStringSet(KEY_DATES, new HashSet<>());
    }

    public int getStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    public int getCurrentXP() {
        return prefs.getInt(KEY_XP, 0);
    }
    
    public int getHearts() {
        return prefs.getInt(KEY_HEARTS, 5); // Default 5 hearts
    }
}
