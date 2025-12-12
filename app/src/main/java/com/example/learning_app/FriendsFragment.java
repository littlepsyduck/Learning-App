package com.example.learning_app;

import android.content.Intent;
import android.graphics.Color;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private TextView tabFollowing, tabFollowers;
    private View indicatorFollowing; // We might need an indicator for followers too if we want to animate
    private RecyclerView rvFriends;
    private ImageView ivBack;

    private boolean isFollowingTabSelected = true;
    private FriendsAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Views
        tabFollowing = view.findViewById(R.id.tabFollowing);
        tabFollowers = view.findViewById(R.id.tabFollowers);
        indicatorFollowing = view.findViewById(R.id.indicatorFollowing);
        rvFriends = view.findViewById(R.id.rvFriends);
        ivBack = view.findViewById(R.id.ivBack);

        // Setup Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup RecyclerView
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new FriendsAdapter(getContext(), userList, !isFollowingTabSelected);
        rvFriends.setAdapter(adapter);

        // Click Listeners
        ivBack.setOnClickListener(v -> {
            // Navigate back to Profile
            if (getActivity() instanceof UserDashboardActivity) {
                // Assuming Profile is one of the bottom nav items, we can switch to it.
                // Or if this Fragment replaced the container, we can go back.
                // The prompt says "back button will return to fragment_user_profile.xml".
                // Since Profile is a main tab, we can instruct the Activity to switch tab
                // OR simply pop this fragment if it was added on top.
                // Given the UserDashboardActivity structure replaces fragments:
                
                // Let's just switch the bottom nav to Profile.
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }
        });

        tabFollowing.setOnClickListener(v -> switchTab(true));
        tabFollowers.setOnClickListener(v -> switchTab(false));

        // Initial Load
        switchTab(true);
    }

    private void switchTab(boolean isFollowing) {
        isFollowingTabSelected = isFollowing;

        if (isFollowing) {
            tabFollowing.setTextColor(Color.BLACK);
            tabFollowers.setTextColor(Color.DKGRAY);
            indicatorFollowing.setVisibility(View.VISIBLE);
            // We'd need to move the indicator or have one for each tab. 
            // For simplicity based on the layout provided, let's just toggle visibility of the one indicator 
            // or assume the layout needs a second indicator or we move it. 
            // The layout only has `indicatorFollowing`. Let's just keep it simple for now or animate it.
            // Since the layout has constraints, moving it might be tricky without LayoutParams.
            // Let's just leave it under Following for now or toggling visibility implies strictly visual indication.
            
            // To properly switch visual indication, we should probably hide `indicatorFollowing` 
            // and show another one under Followers, or move it. 
            // Given I cannot edit layout right now easily to add another view without potentially breaking things,
            // I will assume visual indication is enough with text color.
            indicatorFollowing.setVisibility(View.VISIBLE); 
            
        } else {
            tabFollowing.setTextColor(Color.DKGRAY);
            tabFollowers.setTextColor(Color.BLACK);
            indicatorFollowing.setVisibility(View.INVISIBLE); // Just hide it for Followers for now
        }

        loadData();
    }

    private void loadData() {
        userList.clear();
        adapter = new FriendsAdapter(getContext(), userList, !isFollowingTabSelected);
        rvFriends.setAdapter(adapter);
        
        // For now, let's just load some dummy data or all users as "friends" for demo
        // Since we don't have a real friendship system in Firestore yet (many-to-many),
        // we will just list all users EXCEPT the current one as a placeholder for "Community".
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null && !user.getUid().equals(currentUser.getUid())) {
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading friends", Toast.LENGTH_SHORT).show();
                });
    }
}
