package com.example.tiktok;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Video {
    private String videoUri;
    private String username;
    private String likes;
    private String comments;
    private String music;
    private String title;
    private String thumbnailUrl;

    public Video() {}

    public Video(String videoUri, String username, String likes, String comments, String music, String title) {
        this.videoUri = videoUri;
        this.username = username;
        this.likes = likes;
        this.comments = comments;
        this.music = music;
        this.title = title;
    }

    public Video(String videoUrl, String thumbnailUrl, String finalUserId, String title, String likes, String num_cmt, String music) {
        this.videoUri = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.username = finalUserId;
        this.title = title;
        this.likes = likes;
        this.comments = num_cmt;
        this.music = music;
    }

    public String getTitle() { return title; }
    public String getVideoUri() { return videoUri; }
    public String getUsername() { return username; }
    public String getLikes() { return likes; }
    public String getComments() { return comments; }
    public String getMusic() { return music; }
    public String getThumbnailUrl() { return thumbnailUrl; }

    public void setTitle(String title) { this.title = title; }
    public void setVideoUri(String videoUri) { this.videoUri = videoUri; }
    public void setUsername(String username) { this.username = username; }
    public void setLikes(String likes) { this.likes = likes; }
    public void setComments(String comments) { this.comments = comments; }
    public void setMusic(String music) { this.music = music; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
