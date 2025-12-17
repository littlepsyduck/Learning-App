package com.example.learning_app.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "lessons")
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String imageRes;
    public String topic; // Chủ đề của lesson (ví dụ: "Countries", "Food", "Travel")

    public int sectionId; // 1 = Easy, 2 = Medium, 3 = Hard

    public boolean isLocked;
    public boolean isCompleted;

    public Lesson(String name, String imageRes, String topic, int sectionId, boolean isLocked) {
        this.name = name;
        this.imageRes = imageRes;
        this.topic = topic;
        this.sectionId = sectionId;
        this.isLocked = isLocked;
        this.isCompleted = false;
    }
}
