package com.example.learning_app.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
@Entity(tableName = "questions",
        foreignKeys = @ForeignKey(entity = Lesson.class,
                parentColumns = "id",
                childColumns = "lessonId",
                onDelete = ForeignKey.CASCADE))
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(index = true)
    public int lessonId;

    public int type;
    public String questionText;
    public String optionsData;
    public String correctAnswer;

    public Question(int lessonId, int type, String questionText, String optionsData, String correctAnswer) {
        this.lessonId = lessonId;
        this.type = type;
        this.questionText = questionText;
        this.optionsData = optionsData;
        this.correctAnswer = correctAnswer;
    }
}
