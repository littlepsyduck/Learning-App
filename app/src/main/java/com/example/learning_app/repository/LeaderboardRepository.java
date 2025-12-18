package com.example.learning_app.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learning_app.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MutableLiveData<List<User>> leaderboardLiveData;
    private MutableLiveData<User> currentUserLiveData;
    private MutableLiveData<String> errorMessage;

    public LeaderboardRepository(Application application) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        leaderboardLiveData = new MutableLiveData<>(new ArrayList<>());
        currentUserLiveData = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<List<User>> getLeaderboard() {
        return leaderboardLiveData;
    }

    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadLeaderboard() {
        db.collection("users")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                if (user != null) {
                                    users.add(user);
                                }
                            }
                            leaderboardLiveData.setValue(users);
                        } else {
                            errorMessage.setValue("Failed to load leaderboard: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void loadCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                currentUserLiveData.setValue(user);
                            }
                        }
                    }
                });
    }
}

