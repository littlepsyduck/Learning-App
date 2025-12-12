package com.example.learning_app.model;

public class UserModel {
    private String uid;
    private String username;
    private String fullName;
    private String avatarUrl;

    public UserModel() {}

    public UserModel(String uid, String username, String fullName, String avatarUrl) {
        this.uid = uid;
        this.username = username;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
