package com.example.tiktok;

public class Notification {
    private String id;
    private String userId;  // ID of the user who performed the action
    private String username; // Username of the user who performed the action
    private String userAvatar; // Avatar URL of the user who performed the action
    private String targetUserId; // ID of the user who receives the notification
    private String type; // "follow", "like", "comment", "view"
    private String contentId; // ID of the video (for likes/comments)
    private String message; // Notification message
    private long timestamp; // When the notification was created
    private boolean isRead; // Whether the notification has been read

    // Empty constructor for Firebase
    public Notification() {
    }

    public Notification(String id, String userId, String username, String userAvatar,
                        String targetUserId, String type, String contentId,
                        String message, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.targetUserId = targetUserId;
        this.type = type;
        this.contentId = contentId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}

