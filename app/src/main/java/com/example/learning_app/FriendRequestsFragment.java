package com.example.learning_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestsFragment extends Fragment implements FriendRequestAdapter.OnRequestListener {

    private RecyclerView rvFriendRequests;
    private FriendRequestAdapter adapter;
    private List<FriendRequest> requestList;
    private Map<FriendRequest, DocumentReference> requestDocRefMap;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvEmptyState;
    private ImageView ivBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        ivBack = view.findViewById(R.id.ivBack);

        requestList = new ArrayList<>();
        requestDocRefMap = new HashMap<>();
        adapter = new FriendRequestAdapter(getContext(), requestList, this);

        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendRequests.setAdapter(adapter);

        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("friend_requests")
                .whereEqualTo("receiverId", currentUser.getUid())
                .whereEqualTo("status", "PENDING")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();
                    requestDocRefMap.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvFriendRequests.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvFriendRequests.setVisibility(View.VISIBLE);
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            FriendRequest request = doc.toObject(FriendRequest.class);
                            requestList.add(request);
                            requestDocRefMap.put(request, doc.getReference());
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAccept(FriendRequest request, int position) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();
        String senderUid = request.getSenderId();

        DocumentReference currentUserRef = db.collection("users").document(currentUid);
        DocumentReference senderUserRef = db.collection("users").document(senderUid);

        Task<DocumentSnapshot> currentUserProfileTask = currentUserRef.get();
        Task<DocumentSnapshot> senderUserProfileTask = senderUserRef.get();

        Tasks.whenAllSuccess(currentUserProfileTask, senderUserProfileTask).addOnSuccessListener(results -> {
            UserModel currentUserProfile = ((DocumentSnapshot) results.get(0)).toObject(UserModel.class);
            UserModel senderUserProfile = ((DocumentSnapshot) results.get(1)).toObject(UserModel.class);

            if (currentUserProfile == null || senderUserProfile == null) {
                Toast.makeText(getContext(), "Could not find user profiles.", Toast.LENGTH_SHORT).show();
                return;
            }

            WriteBatch batch = db.batch();

            // 1. Add sender to current user's friends list
            DocumentReference friendInMyListRef = currentUserRef.collection("friends").document(senderUid);
            Map<String, Object> senderData = new HashMap<>();
            senderData.put("uid", senderUserProfile.getUid());
            senderData.put("fullName", senderUserProfile.getFullName());
            senderData.put("username", senderUserProfile.getUsername());
            senderData.put("avatarUrl", senderUserProfile.getAvatarUrl());
            senderData.put("xp", senderUserProfile.getXp());
            batch.set(friendInMyListRef, senderData);
            batch.update(currentUserRef, "friendsCount", FieldValue.increment(1));


            // 2. Add current user to sender's friends list
            DocumentReference meInFriendListRef = senderUserRef.collection("friends").document(currentUid);
            Map<String, Object> myData = new HashMap<>();
            myData.put("uid", currentUserProfile.getUid());
            myData.put("fullName", currentUserProfile.getFullName());
            myData.put("username", currentUserProfile.getUsername());
            myData.put("avatarUrl", currentUserProfile.getAvatarUrl());
            myData.put("xp", currentUserProfile.getXp());
            batch.set(meInFriendListRef, myData);
            batch.update(senderUserRef, "friendsCount", FieldValue.increment(1));

            // 3. Update the friend request status to "ACCEPTED"
            DocumentReference requestRef = requestDocRefMap.get(request);
            if(requestRef != null) {
                batch.update(requestRef, "status", "ACCEPTED");
            }

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Accepted " + request.getSenderName() + "'s request", Toast.LENGTH_SHORT).show();
                requestList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, requestList.size());
                if (requestList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to accept request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to get user profiles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDecline(FriendRequest request, int position) {
        DocumentReference requestRef = requestDocRefMap.get(request);
        if (requestRef == null) {
            Toast.makeText(getContext(), "Could not find request to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        requestRef.update("status", "REJECTED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Declined request", Toast.LENGTH_SHORT).show();
                    requestList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, requestList.size());
                    if (requestList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to decline request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}