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

    public LiveData<List<Lesson>> getAllLessons() {
        return mAllLessons;
    }

    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        return mLearningDao.getQuestionsByLesson(lessonId);
    }

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

    public void deleteAllQuestions() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.deleteAllQuestions();
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

    public void importDataFromJson() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                mLearningDao.deleteAllQuestions();
                mLearningDao.deleteAllLessons();

                String jsonFileString = getJsonFromAssets();
                if (jsonFileString == null) {
                    return;
                }

                Gson gson = new Gson();
                Type listLessonType = new TypeToken<List<LessonJson>>() {}.getType();
                List<LessonJson> lessonsJson = gson.fromJson(jsonFileString, listLessonType);

                if (lessonsJson == null || lessonsJson.isEmpty()) {
                    return;
                }

                for (LessonJson lessonJson : lessonsJson) {
                    Lesson lesson = new Lesson(lessonJson.name, lessonJson.imageRes, 
                            lessonJson.topic != null ? lessonJson.topic : "", 
                            lessonJson.sectionId, lessonJson.isLocked);
                    long lessonId = mLearningDao.insertLesson(lesson);

                    if (lessonJson.questions != null) {
                        for (Question q : lessonJson.questions) {
                            q.lessonId = (int) lessonId;
                            mLearningDao.insertQuestion(q);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

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

