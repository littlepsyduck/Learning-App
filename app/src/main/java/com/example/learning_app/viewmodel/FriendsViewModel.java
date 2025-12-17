package com.example.learning_app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.learning_app.entities.FriendRequest;
import com.example.learning_app.entities.User;
import com.example.learning_app.repository.FriendsRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FriendsViewModel extends AndroidViewModel {

    private FriendsRepository repository;

    public FriendsViewModel(Application application) {
        super(application);
        repository = new FriendsRepository(application);
    }

    public LiveData<List<User>> getAllUsers() {
        return repository.getAllUsers();
    }

    public LiveData<List<User>> getFriends() {
        return repository.getFriends();
    }

    public LiveData<List<FriendRequest>> getFriendRequests() {
        return repository.getFriendRequests();
    }

    public LiveData<Map<FriendRequest, String>> getFriendRequestsWithIds() {
        return repository.getFriendRequestsWithIds();
    }

    public LiveData<Integer> getFriendRequestsCount() {
        return repository.getFriendRequestsCount();
    }

    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }

    public void loadAllUsers() {
        repository.loadAllUsers();
    }

    public void loadFriends() {
        repository.loadFriends();
    }

    public void loadFriendRequests() {
        repository.loadFriendRequests();
    }

    public void sendFriendRequest(User sender, User receiver) {
        repository.sendFriendRequest(sender, receiver);
    }

    public void acceptFriendRequest(FriendRequest request, String requestDocId) {
        repository.acceptFriendRequest(request, requestDocId);
    }

    public void declineFriendRequest(String requestDocId) {
        repository.declineFriendRequest(requestDocId);
    }

    public void getPendingRequestIds(FriendsRepository.OnPendingRequestIdsCallback callback) {
        repository.getPendingRequestIds(callback);
    }
}

