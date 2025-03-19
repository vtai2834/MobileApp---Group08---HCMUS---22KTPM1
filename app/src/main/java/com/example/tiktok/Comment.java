package com.example.tiktok;

public class Comment {
    private String username;
    private String avatarUrl;
    private String commentText;
    private String timestamp;

    // 🔹 Constructor mặc định (bắt buộc để Firebase có thể deserialize)
    public Comment() {}

    // 🔹 Constructor đầy đủ
    public Comment(String username, String avatarUrl, String commentText, String timestamp) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    // 🔹 Getters
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCommentText() { return commentText; }
    public String getTimestamp() { return timestamp; }
}
