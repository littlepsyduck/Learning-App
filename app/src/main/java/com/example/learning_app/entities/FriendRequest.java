package com.example.learning_app.entities;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class FriendRequest {
    private String senderId;
    private String receiverId;
    private String senderUsername;
    private String senderName;
    private String senderAvatar;
    private String status;
    @ServerTimestamp
    private Date timestamp;

    public FriendRequest() {
    }

    public FriendRequest(String senderId, String receiverId, String senderUsername, String senderName,
            String senderAvatar) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderUsername = senderUsername;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.status = "PENDING";
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
