package com.example.learning_app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learning_app.entities.User;
import com.example.learning_app.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class UserViewModel extends AndroidViewModel {

    private UserRepository repository;

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<User> getCurrentUser() {
        return repository.getCurrentUser();
    }

    public LiveData<Boolean> getLoginResult() {
        return repository.getLoginResult();
    }

    public LiveData<Boolean> getRegisterResult() {
        return repository.getRegisterResult();
    }

    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return repository.getCurrentFirebaseUser();
    }

    public void login(String email, String password) {
        repository.login(email, password);
    }

    public void register(String email, String password, String fullName, String username, int age,
                        String whyLearn, String status) {
        repository.register(email, password, fullName, username, age, whyLearn, status);
    }

    public void loadUserProfile(String uid) {
        repository.loadUserProfile(uid);
    }

    public void updateUserProfile(String uid, String fullName, String username, int age, String avatarUrl, String password) {
        repository.updateUserProfile(uid, fullName, username, age, avatarUrl, password);
    }

    public void deleteUserAccount(String uid) {
        repository.deleteUserAccount(uid);
    }

    public void logout() {
        repository.logout();
    }

    public void sendPasswordResetEmail(String email) {
        repository.sendPasswordResetEmail(email);
    }

    public void decrementHeart(String uid) {
        repository.decrementHeart(uid);
    }

    public void addFreeze(String uid) {
        repository.addFreeze(uid);
    }

    public void checkStreakStatus(String uid) {
        repository.checkStreakStatus(uid);
    }
}
