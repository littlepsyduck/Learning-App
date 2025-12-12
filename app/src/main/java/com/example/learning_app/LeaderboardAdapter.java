package com.example.learning_app;

import android.content.Context;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public LeaderboardAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);

        // Rank
        holder.tvRank.setText(String.valueOf(position + 1));

        // Name
        String displayName = user.getFullName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getUsername();
        }
        holder.tvName.setText(displayName);

        // XP
        holder.tvXP.setText(user.getXp() + " XP");

        // Hide Flag for now as we don't have country data in UserModel
        holder.ivFlag.setVisibility(View.GONE);

        // Avatar Logic
        String avatarCode = user.getAvatarUrl();
        boolean loadDefault = true;

        if (avatarCode != null && !avatarCode.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(avatarCode, Base64.DEFAULT);
                Glide.with(context)
                        .load(imageBytes)
                        .placeholder(R.color.duo_border_grey)
                        .into(holder.ivAvatar);
                loadDefault = false;
            } catch (Exception e) {
                // Fallback to default
            }
        }

        if (loadDefault) {
             // Use a default icon if decoding fails or no avatar
             // We can use a default drawable or a color
             holder.ivAvatar.setImageResource(R.drawable.ic_owl_basic); 
        }

        // Optional: styling for top ranks
        if (position == 0) {
            holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Gold
        } else if (position == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Silver
        } else if (position == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#4B4B4B"));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvXP;
        CircleImageView ivAvatar;
        ImageView ivFlag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvXP = itemView.findViewById(R.id.tvXP);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivFlag = itemView.findViewById(R.id.ivFlag);
        }
    }
}
