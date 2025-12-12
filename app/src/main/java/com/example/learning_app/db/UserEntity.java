package com.example.learning_app.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_stats")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int xp;
    public int streak;
    public int hearts;
    public long lastLoginDate;
    public String lastLessonDate;
    public int streakFreezes;
    public int lessonsWithHighAccuracy;
    public String lastFreezeDate; // New field to track when a freeze was used

    public UserEntity() {
        this.xp = 0;
        this.streak = 0;
        this.hearts = 5;
        this.lastLoginDate = System.currentTimeMillis();
        this.lessonsWithHighAccuracy = 0;
        this.lastLessonDate = "";
        this.lastFreezeDate = "";
    }
}