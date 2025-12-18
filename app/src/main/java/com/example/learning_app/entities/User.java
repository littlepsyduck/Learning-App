package com.example.learning_app.entities;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String username;
    private String fullName;
    private String email;
    private String password;
    private int age;
    private String whyLearn;
    private String status;
    private String avatarUrl;
    private String joinDate;
    private String lastDate;
    private int friendsCount;
    private int streak;
    private int xp;
    private int freeze;
    private int hearts;
    private String lastLessonDate;
    private int perfectLessonCount;
    private String dailyChallengeResetDate;
    private int monthlyFreezesUsed;
    private List<String> frozenDates;

    public User() {
        this.frozenDates = new ArrayList<>();
    }

    public User(String uid, String fullName, String username, String email, String password, int age,
            String whyLearn, String status, String joinDate, String lastDate) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.age = age;
        this.whyLearn = whyLearn;
        this.status = status;
        this.joinDate = joinDate;
        this.lastDate = lastDate;
        this.avatarUrl = "";
        this.friendsCount = 0;
        this.streak = 0;
        this.xp = 0;
        this.freeze = 0;
        this.hearts = 5;
        this.lastLessonDate = "";
        this.perfectLessonCount = 0;
        this.dailyChallengeResetDate = "";
        this.monthlyFreezesUsed = 0;
        this.frozenDates = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getWhyLearn() {
        return whyLearn;
    }

    public void setWhyLearn(String whyLearn) {
        this.whyLearn = whyLearn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getFreeze() {
        return freeze;
    }

    public void setFreeze(int freeze) {
        this.freeze = freeze;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
    }

    public String getLastLessonDate() {
        return lastLessonDate;
    }

    public void setLastLessonDate(String lastLessonDate) {
        this.lastLessonDate = lastLessonDate;
    }

    public int getPerfectLessonCount() {
        return perfectLessonCount;
    }

    public void setPerfectLessonCount(int perfectLessonCount) {
        this.perfectLessonCount = perfectLessonCount;
    }

    public String getDailyChallengeResetDate() {
        return dailyChallengeResetDate;
    }

    public void setDailyChallengeResetDate(String dailyChallengeResetDate) {
        this.dailyChallengeResetDate = dailyChallengeResetDate;
    }

    public int getMonthlyFreezesUsed() {
        return monthlyFreezesUsed;
    }

    public void setMonthlyFreezesUsed(int monthlyFreezesUsed) {
        this.monthlyFreezesUsed = monthlyFreezesUsed;
    }

    public List<String> getFrozenDates() {
        return frozenDates;
    }

    public void setFrozenDates(List<String> frozenDates) {
        this.frozenDates = frozenDates;
    }
}