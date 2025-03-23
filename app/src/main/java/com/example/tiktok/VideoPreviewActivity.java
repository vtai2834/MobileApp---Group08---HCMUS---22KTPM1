package com.example.tiktok;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class VideoPreviewActivity extends AppCompatActivity {
    private VideoView videoView;
    private ImageButton closeButton;
    private Button uploadButton;
    private Uri videoUri;

    //thong tin cua user
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        videoView = findViewById(R.id.videoView);
        closeButton = findViewById(R.id.closeButton);
        uploadButton = findViewById(R.id.uploadButton);

        // Get the userID from the intent
        userID = getIntent().getStringExtra("USER_ID");

        // Get the video URI from the intent
        videoUri = getIntent().getParcelableExtra("video_uri");
        if (videoUri != null) {
            videoView.setVideoURI(videoUri);
            videoView.setMediaController(new MediaController(this));
            videoView.start();
            videoView.setOnCompletionListener(mp -> videoView.start());
        }

        // Close the activity when the close button is clicked
        closeButton.setOnClickListener(v -> finish());

        // Upload the video when the upload button is clicked
        uploadButton.setOnClickListener(v -> uploadVideo());
    }

    private void uploadVideo() {
        Toast.makeText(this, "Uploading video...", Toast.LENGTH_SHORT).show();
        Log.d("Supabase", "Video URI: " + videoUri.toString());

        new Thread(() -> {
            try {
                // Use ContentResolver to open an input stream from the URI
                InputStream inputStream = getContentResolver().openInputStream(videoUri);
                File videoFile = createFileFromInputStream(inputStream);

                if (videoFile != null) {
                    String videoUploadUrl = SpbaseSv.uploadVideo(videoFile);
                    if (videoUploadUrl != null) {
                        saveVideoToFirebase(videoUploadUrl);
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error creating file!", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Supabase", "Uploading video error: " + e.getMessage());
                });
            }
        }).start();
    }

    // Create a temporary file from the input stream
    private File createFileFromInputStream(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("video", ".mp4", getCacheDir());
        tempFile.deleteOnExit();

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    // Save the video URL to Firebase
    private void saveVideoToFirebase(String videoUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");
        String videoId = databaseReference.push().getKey();

        Video video = new Video(videoUrl, userID, "0", "0", "new music", "Title of new Video");
        databaseReference.child(videoId).setValue(video)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(this, "Video uploaded: " + videoUrl, Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(this, "Database error!", Toast.LENGTH_SHORT).show()));
    }
}