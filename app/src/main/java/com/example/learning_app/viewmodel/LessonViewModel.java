package com.example.learning_app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learning_app.entities.Lesson;
import com.example.learning_app.entities.Question;
import com.example.learning_app.repository.LessonRepository;

import java.util.List;

public class LessonViewModel extends AndroidViewModel {

    private LessonRepository mRepository;
    private LiveData<List<Lesson>> mAllLessons;

    public LessonViewModel(Application application) {
        super(application);
        mRepository = new LessonRepository(application);
        mAllLessons = mRepository.getAllLessons();
    }

    // Return LiveData so the UI can observe it
    public LiveData<List<Lesson>> getAllLessons() {
        return mAllLessons;
    }

    public LiveData<List<Question>> getQuestionsByLesson(int lessonId) {
        return mRepository.getQuestionsByLesson(lessonId);
    }

    public void insertLesson(Lesson lesson) {
        mRepository.insertLesson(lesson);
    }

    public void insertQuestion(Question question) {
        mRepository.insertQuestion(question);
    }

    public void deleteAllLessons() {
        mRepository.deleteAllLessons();
    }

    public void importDataFromJson() {
        mRepository.importDataFromJson();
    }

    public void updateLessonCompleted(int lessonId, boolean isCompleted) {
        mRepository.updateLessonCompleted(lessonId, isCompleted);
    }

    public void updateLessonLocked(int lessonId, boolean isLocked) {
        mRepository.updateLessonLocked(lessonId, isLocked);
    }
    
    public void syncProgressFromFirestore() {
        mRepository.syncProgressFromFirestore();
    }
}

