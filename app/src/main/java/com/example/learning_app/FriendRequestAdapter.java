package com.example.learning_app;

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
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private final List<FriendRequest> requestList;
    private final Context context;
    private final OnRequestListener listener;

    public interface OnRequestListener {
        void onAccept(FriendRequest request, int position);
        void onDecline(FriendRequest request, int position);
    }

    public FriendRequestAdapter(Context context, List<FriendRequest> requestList, OnRequestListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvName;
        private final ImageView btnAccept;
        private final ImageView btnDecline;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }

        void bind(final FriendRequest request) {
            String displayName = request.getSenderName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = request.getSenderUsername();
            }
            tvName.setText(displayName);

            if (request.getSenderAvatar() != null && !request.getSenderAvatar().isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(request.getSenderAvatar(), Base64.DEFAULT);
                    Glide.with(context).load(imageBytes).placeholder(new ColorDrawable(Color.GRAY)).into(ivAvatar);
                } catch (Exception e) {
                    ivAvatar.setImageResource(R.color.duo_purple);
                }
            } else {
                ivAvatar.setImageResource(R.color.duo_purple);
            }

            btnAccept.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAccept(requestList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            btnDecline.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDecline(requestList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }
}