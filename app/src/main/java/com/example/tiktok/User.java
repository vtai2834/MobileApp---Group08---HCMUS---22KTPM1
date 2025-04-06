package com.example.tiktok;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String account;
    private String password;
    private int followerCount;
    private int followingCount;
    private int likeCount;
    private String avatar;
    private String name;
    private String idName;
    private String bio;
    private Map<String, Boolean> followers;
    private Map<String, Boolean> following;

    public User() {
    }

    public User(String account, String password, int followerCount, int followingCount, int likeCount, String avatar, String name, String idName, String bio, Map<String, Boolean> followers, Map<String, Boolean> following) {
        this.account = account;
        this.password = password;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.likeCount = likeCount;
        this.avatar = avatar;
        this.name = name;
        this.idName = idName;
        this.bio = bio;
        this.followers = followers;
        this.following = following;
    }


    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

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

    public Map<String, Boolean> getFollowers() {
        return followers;
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
    }

    public Map<String, Boolean> getFollowing() {
        return following;
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
    }
}
