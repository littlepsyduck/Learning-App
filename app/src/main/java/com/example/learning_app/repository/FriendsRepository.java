package com.example.learning_app.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learning_app.entities.FriendRequest;
import com.example.learning_app.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendsRepository {

    private static final String TAG = "FriendsRepository";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MutableLiveData<List<User>> allUsersLiveData;
    private MutableLiveData<List<User>> friendsLiveData;
    private MutableLiveData<List<FriendRequest>> friendRequestsLiveData;
    private MutableLiveData<Map<FriendRequest, String>> friendRequestsWithIdsLiveData;
    private MutableLiveData<Integer> friendRequestsCountLiveData;
    private MutableLiveData<String> errorMessage;

    public FriendsRepository(Application application) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        allUsersLiveData = new MutableLiveData<>(new ArrayList<>());
        friendsLiveData = new MutableLiveData<>(new ArrayList<>());
        friendRequestsLiveData = new MutableLiveData<>(new ArrayList<>());
        friendRequestsWithIdsLiveData = new MutableLiveData<>(new HashMap<>());
        friendRequestsCountLiveData = new MutableLiveData<>(0);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsersLiveData;
    }

    public LiveData<List<User>> getFriends() {
        return friendsLiveData;
    }

    public LiveData<List<FriendRequest>> getFriendRequests() {
        return friendRequestsLiveData;
    }

    public LiveData<Map<FriendRequest, String>> getFriendRequestsWithIds() {
        return friendRequestsWithIdsLiveData;
    }

    public LiveData<Integer> getFriendRequestsCount() {
        return friendRequestsCountLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadAllUsers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                if (user != null && !user.getUid().equals(currentUid)) {
                                    users.add(user);
                                }
                            }
                            allUsersLiveData.setValue(users);
                        } else {
                            errorMessage.setValue("Failed to load users: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void loadFriends() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "loadFriends: No current user.");
            return;
        }
        Log.d(TAG, "Loading friends for user: " + currentUser.getUid());

        db.collection("users").document(currentUser.getUid()).collection("friends")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        errorMessage.setValue("Failed to load friends: " + e.getMessage());
                        Log.e(TAG, "Error loading friends", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<User> friends = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            User friend = doc.toObject(User.class);
                            if (friend != null) {
                                friends.add(friend);
                            }
                        }
                        Log.d(TAG, "Successfully loaded " + friends.size() + " friends.");
                        friendsLiveData.setValue(friends);
                    } else {
                        Log.d(TAG, "loadFriends: Snapshots is null");
                    }
                });
    }

    public void loadFriendRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("friend_requests")
                .whereEqualTo("receiverId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        errorMessage.setValue("Failed to load requests: " + e.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        List<FriendRequest> requests = new ArrayList<>();
                        Map<FriendRequest, String> requestsWithIds = new HashMap<>();
                        int pendingCount = 0;
                        for (QueryDocumentSnapshot doc : snapshots) {
                            FriendRequest request = doc.toObject(FriendRequest.class);
                            requests.add(request);
                            requestsWithIds.put(request, doc.getId());
                            if ("PENDING".equals(request.getStatus())) {
                                pendingCount++;
                            }
                        }
                        friendRequestsLiveData.setValue(requests);
                        friendRequestsWithIdsLiveData.setValue(requestsWithIds);
                        friendRequestsCountLiveData.setValue(pendingCount);
                    }
                });
    }

    public void sendFriendRequest(User sender, User receiver) {
        FriendRequest request = new FriendRequest(
                sender.getUid(),
                receiver.getUid(),
                sender.getUsername(),
                sender.getFullName(),
                sender.getAvatarUrl()
        );

        db.collection("friend_requests").add(request)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            errorMessage.setValue("Failed to send request: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void acceptFriendRequest(FriendRequest request, String requestDocId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();
        String senderUid = request.getSenderId();

        Task<DocumentSnapshot> currentUserTask = db.collection("users").document(currentUid).get();
        Task<DocumentSnapshot> senderUserTask = db.collection("users").document(senderUid).get();

        Tasks.whenAllSuccess(currentUserTask, senderUserTask).addOnSuccessListener(results -> {
            User currentUserProfile = ((DocumentSnapshot) results.get(0)).toObject(User.class);
            User senderUserProfile = ((DocumentSnapshot) results.get(1)).toObject(User.class);

            if (currentUserProfile == null || senderUserProfile == null) {
                errorMessage.setValue("Could not find user profiles.");
                return;
            }

            WriteBatch batch = db.batch();

            // Add sender to current user's friend list
            DocumentReference friendInMyListRef = db.collection("users").document(currentUid)
                    .collection("friends").document(senderUid);
            Map<String, Object> senderData = new HashMap<>();
            senderData.put("uid", senderUserProfile.getUid());
            senderData.put("fullName", senderUserProfile.getFullName());
            senderData.put("username", senderUserProfile.getUsername());
            senderData.put("avatarUrl", senderUserProfile.getAvatarUrl());
            senderData.put("xp", senderUserProfile.getXp());
            batch.set(friendInMyListRef, senderData);

            // Add current user to sender's friend list
            DocumentReference meInFriendListRef = db.collection("users").document(senderUid)
                    .collection("friends").document(currentUid);
            Map<String, Object> myData = new HashMap<>();
            myData.put("uid", currentUserProfile.getUid());
            myData.put("fullName", currentUserProfile.getFullName());
            myData.put("username", currentUserProfile.getUsername());
            myData.put("avatarUrl", currentUserProfile.getAvatarUrl());
            myData.put("xp", currentUserProfile.getXp());
            batch.set(meInFriendListRef, myData);
            // We are not updating the other user's friendscount from the client
            // to avoid needing insecure rules. This can be handled by a cloud function later.

            if (requestDocId != null) {
                batch.update(db.collection("friend_requests").document(requestDocId), "status", "ACCEPTED");
            }

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (!task.isSuccessful()) {
                        errorMessage.setValue("Failed to accept request: " + (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                }
            });
        }).addOnFailureListener(e -> {
            errorMessage.setValue("Failed to get user profiles: " + e.getMessage());
        });
    }

    public void declineFriendRequest(String requestDocId) {
        db.collection("friend_requests").document(requestDocId)
                .update("status", "REJECTED")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (!task.isSuccessful()) {
                            errorMessage.setValue("Failed to decline request: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    public void getPendingRequestIds(OnPendingRequestIdsCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("friend_requests")
                .whereEqualTo("senderId", currentUser.getUid())
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Set<String> pendingIds = new HashSet<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String receiverId = doc.getString("receiverId");
                                if (receiverId != null) {
                                    pendingIds.add(receiverId);
                                }
                            }
                            callback.onResult(pendingIds);
                        }
                    }
                });
    }

    public void getIncomingPendingRequestSenderIds(OnPendingRequestIdsCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onResult(new HashSet<>());
            return;
        }

        db.collection("friend_requests")
                .whereEqualTo("receiverId", currentUser.getUid())
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> senderIds = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            senderIds.add(document.getString("senderId"));
                        }
                        callback.onResult(senderIds);
                    } else {
                        callback.onResult(new HashSet<>());
                    }
                });
    }

    public interface OnPendingRequestIdsCallback {
        void onResult(Set<String> pendingIds);
    }
}

