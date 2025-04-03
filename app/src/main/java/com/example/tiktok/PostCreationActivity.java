package com.example.tiktok;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class PostCreationActivity extends AppCompatActivity {
    private static final String TAG = "PostCreationActivity";

    private ImageButton backButton;
    private EditText captionInput;
    private ImageView thumbnailPreview;
    private TextView previewButton;
    private TextView editCoverButton;
    private Button hashtagButton;
    private Button mentionButton;
    private View locationSection;
    private View addLinkSection;
    private View privacySection;
    private View moreOptionsSection;
    private Button draftButton;
    private Button postButton;

    private Uri videoUri;
    private Bitmap thumbnailBitmap;
    private String userID;
    private String userIdName;
    private long videoDuration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        initializeViews();

        // Get data from intent
        userID = getIntent().getStringExtra("USER_ID");
        userIdName = getIntent().getStringExtra("USER_ID_NAME");
        videoUri = getIntent().getParcelableExtra("video_uri");
        videoDuration = getIntent().getLongExtra("video_duration", 0);

        if (videoUri == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate and set thumbnail
        generateThumbnail();

        setupClickListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        captionInput = findViewById(R.id.captionInput);
        thumbnailPreview = findViewById(R.id.thumbnailPreview);
        previewButton = findViewById(R.id.previewButton);
        editCoverButton = findViewById(R.id.editCoverButton);
        hashtagButton = findViewById(R.id.hashtagButton);
        mentionButton = findViewById(R.id.mentionButton);
        locationSection = findViewById(R.id.locationSection);
        addLinkSection = findViewById(R.id.addLinkSection);
        privacySection = findViewById(R.id.privacySection);
        moreOptionsSection = findViewById(R.id.moreOptionsSection);
        draftButton = findViewById(R.id.draftButton);
        postButton = findViewById(R.id.postButton);
    }

    private void generateThumbnail() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            // Get thumbnail from the middle of the video
            thumbnailBitmap = retriever.getFrameAtTime(
                    videoDuration > 0 ? videoDuration * 1000 / 2 : 0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

            if (thumbnailBitmap != null) {
                thumbnailPreview.setImageBitmap(thumbnailBitmap);
            }

            retriever.release();
        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            // Show confirmation dialog if there's text
            if (captionInput.getText().toString().trim().isEmpty()) {
                finish();
            } else {
                // Simple confirmation dialog
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Hủy bài đăng?")
                        .setMessage("Bạn có chắc muốn hủy bài đăng này?")
                        .setPositiveButton("Hủy bài đăng", (dialog, which) -> finish())
                        .setNegativeButton("Tiếp tục chỉnh sửa", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        previewButton.setOnClickListener(v -> {
            // Open video preview
            Intent intent = new Intent(this, VideoPreviewActivity.class);
            intent.putExtra("USER_ID", userID);
            intent.putExtra("USER_ID_NAME", userIdName);
            intent.putExtra("video_uri", videoUri);
            startActivity(intent);
        });

        editCoverButton.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng sửa ảnh bìa chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        hashtagButton.setOnClickListener(v -> {
            // Insert hashtag at cursor position
            int cursorPosition = captionInput.getSelectionStart();
            String currentText = captionInput.getText().toString();
            String newText = currentText.substring(0, cursorPosition) + "#" +
                    currentText.substring(cursorPosition);
            captionInput.setText(newText);
            captionInput.setSelection(cursorPosition + 1);
        });

        mentionButton.setOnClickListener(v -> {
            // Insert mention at cursor position
            int cursorPosition = captionInput.getSelectionStart();
            String currentText = captionInput.getText().toString();
            String newText = currentText.substring(0, cursorPosition) + "@" +
                    currentText.substring(cursorPosition);
            captionInput.setText(newText);
            captionInput.setSelection(cursorPosition + 1);
        });

        locationSection.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng vị trí chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        addLinkSection.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thêm liên kết chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        privacySection.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng quyền riêng tư chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        moreOptionsSection.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng tùy chọn khác chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        draftButton.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu vào bản nháp", Toast.LENGTH_SHORT).show();
            finish();
        });

        postButton.setOnClickListener(v -> {
            uploadVideo();
        });
    }

    private void uploadVideo() {
        Toast.makeText(this, "Đang tải video lên...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // Use ContentResolver to open an input stream from the URI
                InputStream inputStream = getContentResolver().openInputStream(videoUri);
                File videoFile = createFileFromInputStream(inputStream);

                if (videoFile != null) {
                    String videoUploadUrl = SpbaseSv.uploadVideo(videoFile);

                    // Upload thumbnail if available
                    String thumbnailUrl = "";
                    if (thumbnailBitmap != null) {
                        File thumbnailFile = createThumbnailFile(thumbnailBitmap);
                        if (thumbnailFile != null) {
                            thumbnailUrl = SpbaseSv.uploadThumbnail(thumbnailFile);
                        }
                    }

                    if (videoUploadUrl != null) {
                        String caption = captionInput.getText().toString().trim();
                        saveVideoToFirebase(videoUploadUrl, thumbnailUrl, caption);
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Tải lên thất bại!", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi tạo file!", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi tải video: " + e.getMessage());
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

    // Create a temporary file from the thumbnail bitmap
    private File createThumbnailFile(Bitmap bitmap) {
        try {
            File tempFile = File.createTempFile("thumbnail", ".jpg", getCacheDir());
            tempFile.deleteOnExit();

            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error creating thumbnail file: " + e.getMessage());
            return null;
        }
    }

    // Save the video URL to Firebase
    private void saveVideoToFirebase(String videoUrl, String thumbnailUrl, String caption) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");
        String videoId = databaseReference.push().getKey();

        Video video = new Video();
        video.setVideoUri(videoUrl);
        video.setUsername(userIdName);
        video.setLikes("0");
        video.setComments("0");
        video.setMusic("Original sound - " + userIdName);
        video.setTitle(caption.isEmpty() ? "Video by " + userIdName : caption);
        video.setThumbnailUrl(thumbnailUrl);

        databaseReference.child(videoId).setValue(video)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(this, "Video đã được đăng!", Toast.LENGTH_SHORT).show();

                    // Return to home screen
                    Intent intent = new Intent(this, HomeScreen.class);
                    intent.putExtra("USER_ID", userID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi cơ sở dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
    }
}