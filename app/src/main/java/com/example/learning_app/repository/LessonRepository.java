package com.example.learning_app.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.learning_app.dao.LearningDao;
import com.example.learning_app.database.AppDatabase;
import com.example.learning_app.entities.Lesson;
import com.example.learning_app.entities.Question;
import com.example.learning_app.utils.LessonJson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LessonRepository {

    private LearningDao mLearningDao;
    private LiveData<List<Lesson>> mAllLessons;
    private Application mApplication;

    public LessonRepository(Application application) {
        mApplication = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        mLearningDao = db.learningDao();
        mAllLessons = mLearningDao.getAllLessons();
    }

    // LiveData automatically runs on a background thread
    public LiveData<List<Lesson>> getAllLessons() {
        return mAllLessons;
    }

    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        return mLearningDao.getQuestionsByLesson(lessonId);
    }

    // Insert operations must be executed on a background thread
    public void insertLesson(Lesson lesson) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.insertLesson(lesson);
        });
    }

    public void insertQuestion(Question question) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.insertQuestion(question);
        });
    }

    public void deleteAllLessons() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.deleteAllLessons();
        });
    }

    public void updateLessonCompleted(int lessonId, boolean isCompleted) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.updateLessonCompleted(lessonId, isCompleted);
        });
    }

    public void updateLessonLocked(int lessonId, boolean isLocked) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.updateLessonLocked(lessonId, isLocked);
        });
    }

    // Method to import data from JSON file
    public void importDataFromJson() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Check if database is empty
                int lessonCount = mLearningDao.getLessonCount();
                if (lessonCount > 0) {
                    return; // Data already exists
                }

                // 1. Delete old data (Reset)
                mLearningDao.deleteAllLessons();

                // 2. Read JSON file from assets
                String jsonFileString = getJsonFromAssets();
                if (jsonFileString == null) return;

                // 3. Parse JSON to List of LessonJson
                Gson gson = new Gson();
                Type listLessonType = new TypeToken<List<LessonJson>>() {}.getType();
                List<LessonJson> lessonsJson = gson.fromJson(jsonFileString, listLessonType);

                // 4. Insert lessons and questions
                for (LessonJson lessonJson : lessonsJson) {
                    // A. Insert Lesson first
                    Lesson lesson = new Lesson(lessonJson.name, lessonJson.imageRes, 
                            lessonJson.sectionId, lessonJson.isLocked);
                    long lessonId = mLearningDao.insertLesson(lesson);

                    // B. Insert questions for this lesson
                    if (lessonJson.questions != null) {
                        for (Question q : lessonJson.questions) {
                            q.lessonId = (int) lessonId;
                            mLearningDao.insertQuestion(q);
                        }
                    }
                }

                System.out.println("DATA_IMPORT: Đã nhập thành công " + lessonsJson.size() + " bài học.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Helper method to read JSON from assets
    private String getJsonFromAssets() {
        String jsonString;
        try {
            InputStream is = mApplication.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }
}

