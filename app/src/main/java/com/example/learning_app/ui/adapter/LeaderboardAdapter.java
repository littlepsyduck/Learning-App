package com.example.learning_app.ui.adapter;

import android.content.Context;
import android.graphics.Color;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public LeaderboardAdapter(Context context, List<User> userList) {
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
        User user = userList.get(position);

        holder.tvRank.setText(String.valueOf(position + 1));

        String displayName = user.getFullName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getUsername();
        }
        holder.tvName.setText(displayName);

        holder.tvXP.setText(user.getXp() + " XP");

        holder.ivFlag.setVisibility(View.GONE);

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
            holder.ivAvatar.setImageResource(R.drawable.ic_owl_basic);
        }

        if (position == 0) {
            holder.tvRank.setTextColor(Color.parseColor("#FFD700"));
        } else if (position == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (position == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#CD7F32"));
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

