package com.example.learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment {

    private TextView tvFullName, tvUsername, tvJoinDate, tvFriends, tvAvatarLetter;
    private CircleImageView ivAvatar;
    private ImageView ivSettings;
    private View viewRequestBadge;
    private Button btnFriendRequests, btnAddFriends;
    private RecyclerView rvFriendsList;
    private CurrentFriendsAdapter friendsAdapter;
    private List<UserModel> friendList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration friendRequestRegistration;
    private ListenerRegistration friendsCountRegistration;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Standard Views
        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        tvFriends = view.findViewById(R.id.tvFriends);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivSettings = view.findViewById(R.id.ivSettings);

        // Friend-related Views
        viewRequestBadge = view.findViewById(R.id.viewRequestBadge);
        btnFriendRequests = view.findViewById(R.id.btnFriendRequests);
        btnAddFriends = view.findViewById(R.id.btnAddFriends);
        rvFriendsList = view.findViewById(R.id.rvFriendsList);

        setupRecyclerView();
        setupClickListeners();
        loadUserProfileFromFirebase();
    }

    private void setupRecyclerView() {
        friendList = new ArrayList<>();
        friendsAdapter = new CurrentFriendsAdapter(getContext(), friendList);
        rvFriendsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFriendsList.setAdapter(friendsAdapter);
    }

    private void setupClickListeners() {
        btnAddFriends.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FriendsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnFriendRequests.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FriendRequestsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserSettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForFriendRequests();
        listenForFriendListChanges();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (friendRequestRegistration != null) {
            friendRequestRegistration.remove();
        }
        if (friendsCountRegistration != null) {
            friendsCountRegistration.remove();
        }
    }

    private void listenForFriendRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Query query = db.collection("friend_requests")
                .whereEqualTo("receiverId", currentUser.getUid())
                .whereEqualTo("status", "PENDING");

        friendRequestRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) return;
            viewRequestBadge.setVisibility(snapshots != null && !snapshots.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void listenForFriendListChanges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        friendsCountRegistration = db.collection("users").document(currentUser.getUid()).collection("friends")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    if (snapshots != null) {
                        int count = snapshots.size();
                        tvFriends.setText(count + " Friends");
                        // Also update the friends list
                        friendList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            UserModel friend = doc.toObject(UserModel.class);
                            friendList.add(friend);
                        }
                        friendsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadUserProfileFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserModel user = document.toObject(UserModel.class);
                            if (user != null) {
                                updateUI(user);
                            }
                        } else {
                            Toast.makeText(getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load data: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(UserModel user) {
        if (!isAdded() || getContext() == null) return;

        tvFullName.setText(user.getFullName());
        tvUsername.setText(user.getUsername());
        tvJoinDate.setText("Joined " + user.getJoinDate());

        String avatarCode = user.getAvatarUrl();
        if (avatarCode != null && !avatarCode.isEmpty()) {
            try {
                byte[] imageBytes = android.util.Base64.decode(avatarCode, android.util.Base64.DEFAULT);
                Glide.with(this).load(imageBytes).into(ivAvatar);
                tvAvatarLetter.setVisibility(View.GONE);
            } catch (Exception e) {
                showDefaultAvatar(user.getFullName());
            }
        } else {
            showDefaultAvatar(user.getFullName());
        }
    }

    private void showDefaultAvatar(String name) {
        ivAvatar.setImageResource(R.color.duo_green);
        tvAvatarLetter.setVisibility(View.VISIBLE);
        if (name != null && !name.isEmpty()) {
            tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }
}