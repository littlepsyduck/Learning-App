package com.example.learning_app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "skills")
public class SkillEntity {
    @PrimaryKey(autoGenerate = true) public int id;
    public String title;
    public int section;
    public int unit;
    public boolean unlocked;
    public boolean completed;
    public int positionIndex;

    public SkillEntity(String title, int section, int unit, boolean unlocked, boolean completed, int positionIndex) {
        this.title = title; this.section = section; this.unit = unit;
        this.unlocked = unlocked; this.completed = completed; this.positionIndex = positionIndex;
    }
}
