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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.User;
import com.example.learning_app.repository.FriendsRepository;
import com.example.learning_app.ui.adapter.FriendsAdapter;
import com.example.learning_app.viewmodel.FriendsViewModel;
import com.example.learning_app.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendsFragment extends Fragment {

    private RecyclerView rvUsers;
    private FriendsAdapter userAdapter;
    private EditText etSearch;
    private ImageView ivBack;

    private FriendsViewModel friendsViewModel;
    private UserViewModel userViewModel;

    private List<User> allUsers = new ArrayList<>();
    private List<User> displayedUsers = new ArrayList<>();
    private Set<String> friendIds = new HashSet<>();
    private Set<String> pendingRequestIds = new HashSet<>();
    private Set<String> incomingRequestIds = new HashSet<>();
    private User currentUserProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        rvUsers = view.findViewById(R.id.rvFriends);
        etSearch = view.findViewById(R.id.etSearch);
        ivBack = view.findViewById(R.id.ivBack);

        setupRecyclerView();
        setupObservers();
        loadData();

        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

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

    private void setupObservers() {
        friendsViewModel.getAllUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null) {
                    allUsers.clear();
                    allUsers.addAll(users);
                    filterUsers(etSearch.getText().toString());
                }
            }
        });

        friendsViewModel.getFriends().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> friends) {
                if (friends != null) {
                    friendIds.clear();
                    for (User friend : friends) {
                        friendIds.add(friend.getUid());
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }
        });

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    currentUserProfile = user;
                }
            }
        });
    }

    private void setupRecyclerView() {
        userAdapter = new FriendsAdapter(getContext(), displayedUsers, friendIds, pendingRequestIds, incomingRequestIds, user -> sendFriendRequest(user));
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(userAdapter);
    }

    private void loadData() {
        if (userViewModel.getCurrentFirebaseUser() != null) {
            userViewModel.loadUserProfile(userViewModel.getCurrentFirebaseUser().getUid());
        }
        friendsViewModel.loadAllUsers();
        friendsViewModel.loadFriends();
        friendsViewModel.getPendingRequestIds(new FriendsRepository.OnPendingRequestIdsCallback() {
            @Override
            public void onResult(Set<String> pendingIds) {
                pendingRequestIds.clear();
                pendingRequestIds.addAll(pendingIds);
                userAdapter.notifyDataSetChanged();
            }
        });
        friendsViewModel.getIncomingPendingRequestSenderIds(new FriendsRepository.OnPendingRequestIdsCallback() {
            @Override
            public void onResult(Set<String> senderIds) {
                incomingRequestIds.clear();
                incomingRequestIds.addAll(senderIds);
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    private void filterUsers(String query) {
        displayedUsers.clear();
        if (query.isEmpty()) {
            displayedUsers.addAll(allUsers);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lowerCaseQuery) || user.getFullName().toLowerCase().contains(lowerCaseQuery)) {
                    displayedUsers.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    private void sendFriendRequest(User receiver) {
        if (currentUserProfile == null) {
            Toast.makeText(getContext(), "Cannot send request. Your profile is not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        friendsViewModel.sendFriendRequest(currentUserProfile, receiver);
        pendingRequestIds.add(receiver.getUid());
        userAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Friend request sent to " + receiver.getFullName(), Toast.LENGTH_SHORT).show();
    }
}

