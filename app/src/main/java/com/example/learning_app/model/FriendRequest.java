package com.example.learning_app.model;

public class FriendRequest {
    private String senderId;
    private String receiverId;
    private String senderUsername;
    private String senderFullName;
    private String senderAvatarUrl;
    private String status;

    public FriendRequest() {}

    public FriendRequest(String senderId, String receiverId, String senderUsername, String senderFullName, String senderAvatarUrl) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderUsername = senderUsername;
        this.senderFullName = senderFullName;
        this.senderAvatarUrl = senderAvatarUrl;
        this.status = "PENDING";
    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderFullName() { return senderFullName; }
    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public String getStatus() { return status; }
}
