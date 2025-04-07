package com.example.tiktok;

public class Comment {
    private String userId;
    private String userAvatar;
    private String commentText;
    private String timestamp;
    private int likes;

    // Empty constructor for Firebase
    public Comment() {
    }

    public Comment(String userId, String userAvatar, String commentText, String timestamp) {
        this.userId = userId;
        this.userAvatar = userAvatar;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.likes = 0;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}

