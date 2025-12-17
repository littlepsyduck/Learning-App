package com.example.learning_app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learning_app.entities.User;
import com.example.learning_app.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProgressViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private FirebaseAuth mAuth;

    public ProgressViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public void reloadUserProfile() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.loadUserProfile(firebaseUser.getUid());
        }
    }

    public void claimLessonRewards(int xpGained, int accuracy) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.claimLessonRewards(firebaseUser.getUid(), xpGained, accuracy);
        }
    }

    public void decrementHeart() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.decrementHeart(firebaseUser.getUid());
        }
    }

    public void addFreeze() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.addFreeze(firebaseUser.getUid());
        }
    }

    public void checkStreakStatus() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.checkStreakStatus(firebaseUser.getUid());
        }
    }
}
