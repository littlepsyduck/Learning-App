package com.example.learning_app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learning_app.R;
import com.example.learning_app.entities.User;
import com.example.learning_app.ui.StreakActivity;
import com.example.learning_app.ui.UserSettingsActivity;
import com.example.learning_app.ui.adapter.CurrentFriendsAdapter;
import com.example.learning_app.viewmodel.FriendsViewModel;
import com.example.learning_app.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment {

    private TextView tvFullName, tvUsername, tvJoinDate, tvFriends, tvAvatarLetter, tvStreakValue;
    private CircleImageView ivAvatar;
    private ImageView ivSettings;
    private View viewRequestBadge, streakSection;
    private Button btnFriendRequests, btnAddFriends;
    private RecyclerView rvFriendsList;
    private CurrentFriendsAdapter friendsAdapter;
    private List<User> friendList;

    private UserViewModel userViewModel;
    private FriendsViewModel friendsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvJoinDate = view.findViewById(R.id.tvJoinDate);
        tvFriends = view.findViewById(R.id.tvFriends);
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        tvStreakValue = view.findViewById(R.id.tvStreakValue);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivSettings = view.findViewById(R.id.ivSettings);

        viewRequestBadge = view.findViewById(R.id.viewRequestBadge);
        streakSection = view.findViewById(R.id.streak_section);
        btnFriendRequests = view.findViewById(R.id.btnFriendRequests);
        btnAddFriends = view.findViewById(R.id.btnAddFriends);
        rvFriendsList = view.findViewById(R.id.rvFriendsList);

        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        
        if (userViewModel.getCurrentFirebaseUser() != null) {
            userViewModel.loadUserProfile(userViewModel.getCurrentFirebaseUser().getUid());
        }
        friendsViewModel.loadFriends();
        friendsViewModel.loadFriendRequests();
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

        streakSection.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StreakActivity.class);
            startActivity(intent);
        });
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    updateUI(user);
                }
            }
        });

        friendsViewModel.getFriends().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> friends) {
                if (friends != null) {
                    tvFriends.setText(friends.size() + " Friends");
                    friendList.clear();
                    friendList.addAll(friends);
                    friendsAdapter.notifyDataSetChanged();
                }
            }
        });

        friendsViewModel.getFriendRequestsCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (count != null) {
                    viewRequestBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private void updateUI(User user) {
        if (!isAdded() || getContext() == null) return;

        tvFullName.setText(user.getFullName());
        tvUsername.setText(user.getUsername());
        tvJoinDate.setText("Joined " + user.getJoinDate());
        
        if (tvStreakValue != null) {
            int streak = user.getStreak();
            tvStreakValue.setText(streak + " ngày");
        }

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

