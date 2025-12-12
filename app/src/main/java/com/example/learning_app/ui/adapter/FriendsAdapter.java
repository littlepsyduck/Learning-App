package com.example.learning_app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learning_app.R;
import com.example.learning_app.model.UserModel;

import java.util.List;
import java.util.Set;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> users;
    private Set<String> friendIds;
    private Set<String> pendingRequestIds;
    private OnAddFriendClickListener listener;

    public interface OnAddFriendClickListener {
        void onAddFriendClick(UserModel user);
    }

    public FriendsAdapter(Context context, List<UserModel> users, Set<String> friendIds, Set<String> pendingRequestIds, OnAddFriendClickListener listener) {
        this.context = context;
        this.users = users;
        this.friendIds = friendIds;
        this.pendingRequestIds = pendingRequestIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvFullName.setText(user.getFullName());

        if (friendIds.contains(user.getUid())) {
            holder.btnAddFriend.setText("FRIENDS");
            holder.btnAddFriend.setEnabled(false);
        } else if (pendingRequestIds.contains(user.getUid())) {
            holder.btnAddFriend.setText("PENDING");
            holder.btnAddFriend.setEnabled(false);
        } else {
            holder.btnAddFriend.setText("ADD FRIEND");
            holder.btnAddFriend.setEnabled(true);
            holder.btnAddFriend.setOnClickListener(v -> listener.onAddFriendClick(user));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvFullName;
        Button btnAddFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}