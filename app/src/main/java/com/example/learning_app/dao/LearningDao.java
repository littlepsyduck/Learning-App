package com.example.learning_app.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.learning_app.entities.Lesson;
import com.example.learning_app.entities.Question;

import java.util.List;

@Dao
public interface LearningDao {
    @Insert
    long insertLesson(Lesson lesson);

    @Insert
    void insertQuestion(Question question);

    @Query("SELECT * FROM lessons ORDER BY sectionId ASC, id ASC")
    LiveData<List<Lesson>> getAllLessons();

    @Query("SELECT * FROM questions WHERE lessonId = :lessonId")
    LiveData<List<Question>> getQuestionsByLesson(int lessonId);

    @Query("DELETE FROM lessons")
    void deleteAllLessons();

    @Query("DELETE FROM questions")
    void deleteAllQuestions();

    @Query("SELECT COUNT(*) FROM lessons")
    int getLessonCount();

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    void updateLessonCompleted(int lessonId, boolean isCompleted);

    @Query("UPDATE lessons SET isLocked = :isLocked WHERE id = :lessonId")
    void updateLessonLocked(int lessonId, boolean isLocked);
}