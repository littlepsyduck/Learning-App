package com.example.learning_app.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.model.FriendRequest;
import com.example.learning_app.model.UserModel;
import com.example.learning_app.ui.adapter.FriendsAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendsSearchFragment extends Fragment {

    private RecyclerView rvUsers;
    private FriendsAdapter userAdapter;
    private EditText etSearch;
    private ImageView ivBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> displayedUsers = new ArrayList<>();
    private Set<String> friendIds = new HashSet<>();
    private Set<String> pendingRequestIds = new HashSet<>();
    private UserModel currentUserProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvUsers = view.findViewById(R.id.rvFriends);
        etSearch = view.findViewById(R.id.etSearch);
        ivBack = view.findViewById(R.id.ivBack);

        setupRecyclerView();
        loadData();

        ivBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        userAdapter = new FriendsAdapter(getContext(), displayedUsers, friendIds, pendingRequestIds, this::sendFriendRequest);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(userAdapter);
    }

    private void loadData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String currentUid = currentUser.getUid();

        Task<DocumentSnapshot> currentUserTask = db.collection("users").document(currentUid).get();
        Task<QuerySnapshot> allUsersTask = db.collection("users").get();
        Task<QuerySnapshot> friendsTask = db.collection("users").document(currentUid).collection("friends").get();
        Task<QuerySnapshot> pendingRequestsTask = db.collection("friend_requests")
                .whereEqualTo("senderId", currentUid)
                .whereEqualTo("status", "PENDING").get();

        Tasks.whenAllSuccess(currentUserTask, allUsersTask, friendsTask, pendingRequestsTask).addOnSuccessListener(results -> {
            if (getContext() == null) return; // Prevent crash if fragment is detached

            currentUserProfile = ((DocumentSnapshot) results.get(0)).toObject(UserModel.class);
            List<UserModel> tempAllUsers = ((QuerySnapshot) results.get(1)).toObjects(UserModel.class);

            friendIds.clear();
            for (DocumentSnapshot doc : ((QuerySnapshot) results.get(2)).getDocuments()) {
                friendIds.add(doc.getId());
            }

            pendingRequestIds.clear();
            for (DocumentSnapshot doc : ((QuerySnapshot) results.get(3)).getDocuments()) {
                pendingRequestIds.add(doc.getString("receiverId"));
            }

            allUsers = tempAllUsers.stream()
                    .filter(user -> !user.getUid().equals(currentUid))
                    .collect(Collectors.toList());

            filterUsers(etSearch.getText().toString());
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        displayedUsers.clear();
        if (query.isEmpty()) {
            displayedUsers.addAll(allUsers);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserModel user : allUsers) {
                if ((user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseQuery)) || 
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerCaseQuery))) {
                    displayedUsers.add(user);
                }
            }
        }
        if (userAdapter != null) {
            userAdapter.notifyDataSetChanged();
        }
    }

    private void sendFriendRequest(UserModel receiver) {
        if (currentUserProfile == null || mAuth.getUid() == null) {
            Toast.makeText(getContext(), "Cannot send request. Your profile is not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = mAuth.getUid();
        FriendRequest request = new FriendRequest(
                currentUid,
                receiver.getUid(),
                currentUserProfile.getUsername(),
                currentUserProfile.getFullName(),
                currentUserProfile.getAvatarUrl()
        );

        // Add to pending list immediately for instant UI update
        pendingRequestIds.add(receiver.getUid());
        userAdapter.notifyDataSetChanged();

        db.collection("friend_requests").add(request)
                .addOnSuccessListener(documentReference -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Friend request sent to " + receiver.getFullName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Revert UI on failure
                        pendingRequestIds.remove(receiver.getUid());
                        if (userAdapter != null) {
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
