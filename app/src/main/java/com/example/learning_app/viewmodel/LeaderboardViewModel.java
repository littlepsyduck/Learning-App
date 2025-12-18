package com.example.learning_app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learning_app.entities.User;
import com.example.learning_app.repository.LeaderboardRepository;

import java.util.List;

public class LeaderboardViewModel extends AndroidViewModel {

    private LeaderboardRepository repository;

    public LeaderboardViewModel(Application application) {
        super(application);
        repository = new LeaderboardRepository(application);
    }

    public LiveData<List<User>> getLeaderboard() {
        return repository.getLeaderboard();
    }

    public LiveData<User> getCurrentUser() {
        return repository.getCurrentUser();
    }

    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }

    public void loadLeaderboard() {
        repository.loadLeaderboard();
    }

    public void loadCurrentUser() {
        repository.loadCurrentUser();
    }
}



