package com.example.learning_app.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learning_app.R;
import com.example.learning_app.entities.User;

import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.UserViewHolder> {

    private final List<User> userList;
    private final Set<String> friendIds;
    private final Set<String> pendingRequestIds;
    private final Set<String> incomingRequestIds;
    private final Context context;
    private final OnAddFriendClickListener listener;

    public interface OnAddFriendClickListener {
        void onAddFriendClick(User user);
    }

    public FriendsAdapter(Context context, List<User> userList, Set<String> friendIds, Set<String> pendingRequestIds, Set<String> incomingRequestIds, OnAddFriendClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.friendIds = friendIds;
        this.pendingRequestIds = pendingRequestIds;
        this.incomingRequestIds = incomingRequestIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvFullName, tvUsername, tvAvatarLetter;
        private final ImageView btnAdd, ivSent, ivIsFriend, ivRequestReceived;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvAvatarLetter = itemView.findViewById(R.id.tvAvatarLetter);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            ivSent = itemView.findViewById(R.id.ivSent);
            ivIsFriend = itemView.findViewById(R.id.ivIsFriend);
            ivRequestReceived = itemView.findViewById(R.id.ivRequestReceived);
        }

        void bind(final User user) {
            tvFullName.setText(user.getFullName());
            tvUsername.setText(user.getUsername());

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                    Glide.with(context).load(imageBytes).into(ivAvatar);
                    tvAvatarLetter.setVisibility(View.GONE);
                } catch (Exception e) {
                    showDefaultAvatar(user.getFullName());
                }
            } else {
                showDefaultAvatar(user.getFullName());
            }

            btnAdd.setVisibility(View.GONE);
            ivSent.setVisibility(View.GONE);
            ivIsFriend.setVisibility(View.GONE);
            ivRequestReceived.setVisibility(View.GONE);

            if (friendIds.contains(user.getUid())) {
                ivIsFriend.setVisibility(View.VISIBLE);
            } else if (pendingRequestIds.contains(user.getUid())) {
                ivSent.setVisibility(View.VISIBLE);
            } else if (incomingRequestIds.contains(user.getUid())) {
                ivRequestReceived.setVisibility(View.VISIBLE);
            }
            else {
                btnAdd.setVisibility(View.VISIBLE);
            }

            btnAdd.setOnClickListener(v -> {
                listener.onAddFriendClick(user);
                btnAdd.setVisibility(View.GONE);
                ivSent.setVisibility(View.VISIBLE);
                pendingRequestIds.add(user.getUid());
            });
        }

        private void showDefaultAvatar(String name) {
            ivAvatar.setImageDrawable(new ColorDrawable(Color.parseColor("#A020F0")));
            tvAvatarLetter.setVisibility(View.VISIBLE);
            if (name != null && !name.isEmpty()) {
                tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
            }
        }
    }
}

