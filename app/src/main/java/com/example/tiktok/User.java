package com.example.tiktok;

public class User {
    private String account;
    private String password;
    private int followerCount;
    private int followingCount;
    private int likeCount;
    private String avatar;
    private String name;
    private String idName;
    private String bio; // Thêm trường bio

    // Constructor mặc định (bắt buộc cho Firebase)
    public User() {
    }

    // Constructor có tham số (cập nhật để bao gồm bio)
    public User(String account, String password, int followerCount, int followingCount, int likeCount, String avatar, String name, String idName, String bio) {
        this.account = account;
        this.password = password;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.likeCount = likeCount;
        this.avatar = avatar;
        this.name = name;
        this.idName = idName;
        this.bio = bio;
    }

    // Getter và Setter cho bio
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // Các Getter và Setter khác
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }
}