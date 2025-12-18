package com.example.learning_app.ui.fragment;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.entities.FriendRequest;
import com.example.learning_app.ui.adapter.FriendRequestAdapter;
import com.example.learning_app.viewmodel.FriendsViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestsFragment extends Fragment implements FriendRequestAdapter.OnRequestListener {

    private RecyclerView rvFriendRequests;
    private FriendRequestAdapter adapter;
    private List<FriendRequest> requestList;
    private Map<FriendRequest, String> requestDocIdMap;

    private FriendsViewModel friendsViewModel;

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

        friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        ivBack = view.findViewById(R.id.ivBack);

        requestList = new ArrayList<>();
        requestDocIdMap = new HashMap<>();
        adapter = new FriendRequestAdapter(getContext(), requestList, this);

        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendRequests.setAdapter(adapter);

        ivBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupObservers();
        friendsViewModel.loadFriendRequests();
    }

    private void setupObservers() {
        friendsViewModel.getFriendRequests().observe(getViewLifecycleOwner(), new Observer<List<FriendRequest>>() {
            @Override
            public void onChanged(List<FriendRequest> requests) {
                if (requests != null) {
                    requestList.clear();
                    requestList.addAll(requests);
                    adapter.notifyDataSetChanged();
                    if (requestList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvFriendRequests.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvFriendRequests.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        friendsViewModel.getFriendRequestsWithIds().observe(getViewLifecycleOwner(), new Observer<Map<FriendRequest, String>>() {
            @Override
            public void onChanged(Map<FriendRequest, String> requestsWithIds) {
                if (requestsWithIds != null) {
                    requestDocIdMap.clear();
                    requestDocIdMap.putAll(requestsWithIds);
                }
            }
        });
    }

    @Override
    public void onAccept(FriendRequest request, int position) {
        String requestDocId = findRequestDocId(request);
        if (requestDocId != null) {
            friendsViewModel.acceptFriendRequest(request, requestDocId);
            Toast.makeText(getContext(), "Accepted " + request.getSenderName() + "'s request", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Could not find request document ID.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDecline(FriendRequest request, int position) {
        String requestDocId = findRequestDocId(request);
        if (requestDocId != null) {
            friendsViewModel.declineFriendRequest(requestDocId);
            Toast.makeText(getContext(), "Declined request", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Could not find request to update.", Toast.LENGTH_SHORT).show();
        }
    }

    private String findRequestDocId(FriendRequest request) {
        String requestDocId = requestDocIdMap.get(request);
        if (requestDocId == null) {
            for (Map.Entry<FriendRequest, String> entry : requestDocIdMap.entrySet()) {
                if (entry.getKey().getSenderId().equals(request.getSenderId()) &&
                    entry.getKey().getReceiverId().equals(request.getReceiverId())) {
                    requestDocId = entry.getValue();
                    break;
                }
            }
        }
        return requestDocId;
    }
}



