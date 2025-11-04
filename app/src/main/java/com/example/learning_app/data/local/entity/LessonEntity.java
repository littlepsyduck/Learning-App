package com.example.learning_app.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "lessons",
        foreignKeys = @ForeignKey(
                entity = SkillEntity.class,
                parentColumns = "id",
                childColumns = "skillId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("skillId")}
)
public class LessonEntity {
    @PrimaryKey(autoGenerate = true) public int id;
    public int skillId;
    public String name;
    public int level;
    public boolean completed;

    public LessonEntity(int skillId, String name, int level, boolean completed) {
        this.skillId = skillId; this.name = name; this.level = level; this.completed = completed;
    }
}
