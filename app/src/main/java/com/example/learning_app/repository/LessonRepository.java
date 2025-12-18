package com.example.learning_app.repository;

import android.app.Application;
import android.util.Log;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LessonRepository {

    private LearningDao mLearningDao;
    private LiveData<List<Lesson>> mAllLessons;
    private Application mApplication;
    private LessonProgressRepository progressRepository;
    private FirebaseAuth mAuth;

    public LessonRepository(Application application) {
        mApplication = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        mLearningDao = db.learningDao();
        mAllLessons = mLearningDao.getAllLessons();
        progressRepository = new LessonProgressRepository(application);
        mAuth = FirebaseAuth.getInstance();
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
        Log.d("LessonRepository", "updateLessonCompleted: lessonId=" + lessonId + ", completed=" + isCompleted);
        
        // Update local Room database
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.updateLessonCompleted(lessonId, isCompleted);
        });
        
        // Sync to Firestore
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            progressRepository.saveLessonCompleted(user.getUid(), lessonId, isCompleted);
        } else {
            Log.w("LessonRepository", "updateLessonCompleted: No user logged in, cannot save to Firestore");
        }
    }

    public void updateLessonLocked(int lessonId, boolean isLocked) {
        Log.d("LessonRepository", "updateLessonLocked: lessonId=" + lessonId + ", locked=" + isLocked);
        
        // Update local Room database
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLearningDao.updateLessonLocked(lessonId, isLocked);
        });
        
        // Sync to Firestore
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            progressRepository.saveLessonLocked(user.getUid(), lessonId, isLocked);
        } else {
            Log.w("LessonRepository", "updateLessonLocked: No user logged in, cannot save to Firestore");
        }
    }
    
    /**
     * Load lesson progress from Firestore and sync to Room database
     */
    public void syncProgressFromFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            android.util.Log.d("LessonRepository", "syncProgressFromFirestore: No user logged in");
            return;
        }
        
        Log.d("LessonRepository", "syncProgressFromFirestore: Starting sync for user " + user.getUid());
        
        progressRepository.loadLessonProgress(user.getUid(), (completedLessonIds, unlockedLessonIds) -> {
            Log.d("LessonRepository", "Loaded from Firestore: " + completedLessonIds.size() + " completed, " + unlockedLessonIds.size() + " unlocked");
            
            // Update Room database with progress from Firestore
            AppDatabase.databaseWriteExecutor.execute(() -> {
                // Get all lessons to find the first one
                List<Lesson> allLessons = mLearningDao.getAllLessonsSync();
                int firstLessonId = -1;
                if (allLessons != null && !allLessons.isEmpty()) {
                    // Sort by sectionId and id to find the first lesson
                    allLessons.sort((l1, l2) -> {
                        if (l1.sectionId != l2.sectionId) {
                            return Integer.compare(l1.sectionId, l2.sectionId);
                        }
                        return Integer.compare(l1.id, l2.id);
                    });
                    firstLessonId = allLessons.get(0).id;
                }
                
                // Always unlock the first lesson ONLY if no progress exists in Firestore at all
                // This prevents overriding existing progress
                if (firstLessonId != -1 && unlockedLessonIds.isEmpty() && completedLessonIds.isEmpty()) {
                    mLearningDao.updateLessonLocked(firstLessonId, false);
                    Log.d("LessonRepository", "Ensured first lesson " + firstLessonId + " is unlocked (no progress exists yet)");
                    
                    // Also save to Firestore
                    progressRepository.saveLessonLocked(user.getUid(), firstLessonId, false);
                }
                
                // Update completed lessons
                for (Integer lessonId : completedLessonIds) {
                    mLearningDao.updateLessonCompleted(lessonId, true);
                    Log.d("LessonRepository", "Synced completed lesson: " + lessonId);
                }
                
                // Update unlocked lessons
                for (Integer lessonId : unlockedLessonIds) {
                    mLearningDao.updateLessonLocked(lessonId, false);
                    Log.d("LessonRepository", "Synced unlocked lesson: " + lessonId);
                }
                
                // Auto-unlock next lessons for completed lessons (if not already unlocked)
                // This ensures that when syncing from Firestore, the unlock logic is preserved
                for (int i = 0; i < allLessons.size(); i++) {
                    Lesson lesson = allLessons.get(i);
                    if (completedLessonIds.contains(lesson.id)) {
                        // Find next lesson in same section
                        if (i < allLessons.size() - 1) {
                            Lesson nextLesson = allLessons.get(i + 1);
                            if (nextLesson.sectionId == lesson.sectionId && nextLesson.isLocked) {
                                mLearningDao.updateLessonLocked(nextLesson.id, false);
                                // Also save to Firestore
                                progressRepository.saveLessonLocked(user.getUid(), nextLesson.id, false);
                                Log.d("LessonRepository", "Auto-unlocked next lesson: " + nextLesson.id + " (after completing " + lesson.id + ")");
                            }
                        }
                    }
                }
                
                // Check if all lessons in a section are completed, then unlock first lesson of next section
                int maxSectionId = 0;
                for (Lesson lesson : allLessons) {
                    if (lesson.sectionId > maxSectionId) {
                        maxSectionId = lesson.sectionId;
                    }
                }
                
                for (int sectionId = 1; sectionId <= maxSectionId; sectionId++) {
                    boolean allCompleted = true;
                    Lesson firstLessonInNextSection = null;
                    
                    for (Lesson lesson : allLessons) {
                        if (lesson.sectionId == sectionId) {
                            if (!completedLessonIds.contains(lesson.id)) {
                                allCompleted = false;
                                break;
                            }
                        } else if (lesson.sectionId == sectionId + 1 && firstLessonInNextSection == null) {
                            firstLessonInNextSection = lesson;
                        }
                    }
                    
                    if (allCompleted && firstLessonInNextSection != null && firstLessonInNextSection.isLocked) {
                        mLearningDao.updateLessonLocked(firstLessonInNextSection.id, false);
                        // Also save to Firestore
                        progressRepository.saveLessonLocked(user.getUid(), firstLessonInNextSection.id, false);
                        Log.d("LessonRepository", "Auto-unlocked first lesson of next section: " + firstLessonInNextSection.id + " (section " + (sectionId + 1) + ")");
                    }
                }
            });
        });
    }

    public void importDataFromJson() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                mLearningDao.deleteAllQuestions();
                mLearningDao.deleteAllLessons();
                // Reset auto-increment sequence so IDs start from 1
                // NOTE: This will cause existing Firestore progress to become invalid
                // Only use this if you want to reset everything and don't need existing progress
                mLearningDao.resetLessonsSequence();

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

