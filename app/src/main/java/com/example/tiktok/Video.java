package com.example.tiktok;

import android.net.Uri;

public class Video {
    private String videoUri;
    private String username;
    private String likes;
    private String comments;
    private String music;
    private String title;

    public Video(String videoUri, String username, String likes, String comments, String music, String title) {
        this.videoUri = videoUri;
        this.username = username;
        this.title = title;
        this.likes = likes;
        this.comments = comments;
        this.music = music;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public String getUsername() {
        return username;
    }

    public String getLikes() {
        return likes;
    }

    public String getComments() {
        return comments;
    }

    public String getMusic() {
        return music;
    }
}
