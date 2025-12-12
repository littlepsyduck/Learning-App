package com.example.learning_app;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> friendsList;
    private boolean isFollowersTab; // True if displaying Followers, False for Following

    public FriendsAdapter(Context context, List<UserModel> friendsList, boolean isFollowersTab) {
        this.context = context;
        this.friendsList = friendsList;
        this.isFollowersTab = isFollowersTab;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = friendsList.get(position);

        String displayName = user.getFullName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getUsername();
        }
        holder.tvName.setText(displayName);
        holder.tvSubtitle.setText(user.getXp() + " XP");

        // Avatar
        String avatarCode = user.getAvatarUrl();
        boolean loadDefault = true;
        if (avatarCode != null && !avatarCode.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(avatarCode, Base64.DEFAULT);
                Glide.with(context)
                        .load(imageBytes)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .into(holder.ivAvatar);
                loadDefault = false;
            } catch (Exception e) {
                // Fallback
            }
        }
        if (loadDefault) {
            holder.ivAvatar.setImageResource(R.drawable.ic_owl_basic); // Or any default
        }

        // Action Button
        if (isFollowersTab) {
            // For followers tab, maybe show "Add back" or nothing?
            // For now let's just use the add icon as generic action or hide it
             holder.ivAction.setVisibility(View.GONE);
        } else {
            // For Following tab
             holder.ivAction.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivAction;
        TextView tvName, tvSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivAction = itemView.findViewById(R.id.ivAction);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
        }
    }
}
