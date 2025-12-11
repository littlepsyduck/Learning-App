package com.example.learning_app.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "lessons")
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String imageRes;

    public int sectionId;

    public boolean isLocked;
    public boolean isCompleted;

    public Lesson(String name, String imageRes, int sectionId, boolean isLocked) {
        this.name = name;
        this.imageRes = imageRes;
        this.sectionId = sectionId;
        this.isLocked = isLocked;
        this.isCompleted = false;
    }
}
