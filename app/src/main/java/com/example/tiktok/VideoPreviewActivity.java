package com.example.tiktok;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class VideoPreviewActivity extends AppCompatActivity {
    private static final String TAG = "VideoPreviewActivity";

    private VideoView videoView;
    private ImageButton backButton;
    private TextView chooseButton;
    private Button autoCutButton;
    private Button nextButton;
    private Uri videoUri;

    // User info
    private String userID;
    private String userIdName;
    private long videoDuration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        initializeViews();

        // Get user info from intent
//        userID = getIntent().getStringExtra("USER_ID");
//        userIdName = getIntent().getStringExtra("USER_ID_NAME");

        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUserID();
            userIdName = currentUser.getIdName();
        }

        // Get video URI from intent
        videoUri = getIntent().getParcelableExtra("video_uri");
        if (videoUri != null) {
            setupVideoView();
            getVideoDuration();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy video", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    private void initializeViews() {
        videoView = findViewById(R.id.videoView);
        backButton = findViewById(R.id.backButton);
        chooseButton = findViewById(R.id.chooseButton);
        autoCutButton = findViewById(R.id.autoCutButton);
        nextButton = findViewById(R.id.nextButton);
    }

    private void setupVideoView() {
        // Hide media controller
        videoView.setMediaController(null);

        // Set video URI
        videoView.setVideoURI(videoUri);

        // Start playing and loop
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Error playing video: " + what + ", " + extra);
            Toast.makeText(VideoPreviewActivity.this, "Lỗi phát video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void getVideoDuration() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoDuration = Long.parseLong(time) / 1000; // Convert to seconds
            retriever.release();
        } catch (Exception e) {
            Log.e(TAG, "Error getting video duration: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            videoView.stopPlayback();
            finish();
        });

        chooseButton.setOnClickListener(v -> {
            // For now, just show a toast
            Toast.makeText(this, "Chức năng chọn chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        autoCutButton.setOnClickListener(v -> {
            // For now, just show a toast
            Toast.makeText(this, "Chức năng AutoCut chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        nextButton.setOnClickListener(v -> {
            // Go to post creation screen
            Intent intent = new Intent(this, PostCreationActivity.class);
//            intent.putExtra("USER_ID", userID);
//            intent.putExtra("USER_ID_NAME", userIdName);
            intent.putExtra("video_uri", videoUri);
            intent.putExtra("video_duration", videoDuration);
            startActivity(intent);
        });
    }
}