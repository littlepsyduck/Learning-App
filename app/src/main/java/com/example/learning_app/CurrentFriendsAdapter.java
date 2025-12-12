package com.example.learning_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CurrentFriendsAdapter extends RecyclerView.Adapter<CurrentFriendsAdapter.FriendViewHolder> {

    private final List<UserModel> friendList;
    private final Context context;

    public CurrentFriendsAdapter(Context context, List<UserModel> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_current_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvFullName;
        private final TextView tvAvatarLetter;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvAvatarLetter = itemView.findViewById(R.id.tvAvatarLetter);
        }

        void bind(final UserModel friend) {
            tvFullName.setText(friend.getFullName());

            if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(friend.getAvatarUrl(), Base64.DEFAULT);
                    Glide.with(context).load(imageBytes).into(ivAvatar);
                    tvAvatarLetter.setVisibility(View.GONE);
                } catch (Exception e) {
                    showDefaultAvatar(friend.getFullName());
                }
            } else {
                showDefaultAvatar(friend.getFullName());
            }
        }

        private void showDefaultAvatar(String name) {
            ivAvatar.setImageDrawable(new ColorDrawable(Color.parseColor("#673AB7")));
            tvAvatarLetter.setVisibility(View.VISIBLE);
            if (name != null && !name.isEmpty()) {
                tvAvatarLetter.setText(String.valueOf(name.charAt(0)).toUpperCase());
            }
        }
    }
}