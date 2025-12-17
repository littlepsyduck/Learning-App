package com.example.learning_app.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "lesson_progress",
        foreignKeys = @ForeignKey(entity = Lesson.class,
                parentColumns = "id",
                childColumns = "lessonId",
                onDelete = ForeignKey.CASCADE))
public class LessonProgress {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(index = true)
    public int lessonId;

    public int totalXP;           // Tổng XP đạt được
    public int correctAnswers;    // Số câu trả lời đúng
    public int totalQuestions;    // Tổng số câu hỏi
    public long timeSpent;        // Thời gian làm bài (milliseconds)
    public long completedAt;      // Thời điểm hoàn thành (timestamp)

    public LessonProgress() {
    }

    @Ignore
    public LessonProgress(int lessonId, int totalXP, int correctAnswers, int totalQuestions, long timeSpent) {
        this.lessonId = lessonId;
        this.totalXP = totalXP;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.timeSpent = timeSpent;
        this.completedAt = System.currentTimeMillis();
    }

    // Tính phần trăm đúng
    public int getAccuracyPercentage() {
        if (totalQuestions == 0) return 0;
        return (int) ((correctAnswers * 100.0) / totalQuestions);
    }
}






