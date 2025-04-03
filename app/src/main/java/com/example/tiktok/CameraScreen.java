package com.example.tiktok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraScreen extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    private PreviewView viewFinder;
    private ImageButton recordButton;
    private ImageButton flipButton;
    private ImageButton closeButton;
    private ImageButton speedButton;
    private ImageButton beautyButton;
    private ImageButton filtersButton;
    private ImageButton timerButton;
    private ImageView uploadGallery;
    private ImageView effectsGallery;
    private TextView duration15s;
    private TextView duration60s;
    private TextView duration10min;
    private TextView photoOption;
    private TextView textOption;
    private ProgressBar recordingProgress;
    private TabLayout tabLayout;

    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private boolean isRecording = false;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private int selectedDuration = 15; // Default 15 seconds
    private ExecutorService cameraExecutor;

    // User info
    private String userID;
    private String userIdName;

    // Timer for recording progress
    private CountDownTimer recordingTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);

        userID = getIntent().getStringExtra("USER_ID");
        userIdName = getIntent().getStringExtra("USER_ID_NAME");


        initializeViews();
        setupClickListeners();
        setupTabLayout();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (checkPermissions()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (recordingTimer != null) {
            recordingTimer.cancel();
        }
    }

    private void initializeViews() {
        viewFinder = findViewById(R.id.viewFinder);
        recordButton = findViewById(R.id.recordButton);
        flipButton = findViewById(R.id.flipButton);
        closeButton = findViewById(R.id.closeButton);
        speedButton = findViewById(R.id.speedButton);
        beautyButton = findViewById(R.id.beautyButton);
        filtersButton = findViewById(R.id.filtersButton);
        timerButton = findViewById(R.id.timerButton);
        uploadGallery = findViewById(R.id.uploadGallery);
        effectsGallery = findViewById(R.id.effectsGallery);
        duration15s = findViewById(R.id.duration15s);
        duration60s = findViewById(R.id.duration60s);
        duration10min = findViewById(R.id.duration10min);
        photoOption = findViewById(R.id.photoOption);
        textOption = findViewById(R.id.textOption);
        recordingProgress = findViewById(R.id.recordingProgress);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupClickListeners() {
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        flipButton.setOnClickListener(v -> flipCamera());

        closeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CameraScreen.this, HomeScreen.class);
            intent.putExtra("USER_ID", userID);
            startActivity(intent);
            finish();
        });

        uploadGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        effectsGallery.setOnClickListener(v -> {
            Toast.makeText(this, "Hiệu ứng chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        // Duration selection
        duration15s.setOnClickListener(v -> {
            selectedDuration = 15;
            updateDurationSelection();
        });

        duration60s.setOnClickListener(v -> {
            selectedDuration = 60;
            updateDurationSelection();
        });

        duration10min.setOnClickListener(v -> {
            selectedDuration = 600;
            updateDurationSelection();
        });

        photoOption.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        textOption.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng văn bản chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        // Right side controls
        speedButton.setOnClickListener(v -> {
            Toast.makeText(this, "Tốc độ chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        beautyButton.setOnClickListener(v -> {
            Toast.makeText(this, "Làm đẹp chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        filtersButton.setOnClickListener(v -> {
            Toast.makeText(this, "Bộ lọc chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        timerButton.setOnClickListener(v -> {
            Toast.makeText(this, "Hẹn giờ chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTabLayout() {
        tabLayout.getTabAt(1).select(); // Select "BÀI ĐĂNG" tab by default

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() != 1) { // If not "BÀI ĐĂNG"
                    Toast.makeText(CameraScreen.this,
                            "Chức năng này chưa được hỗ trợ",
                            Toast.LENGTH_SHORT).show();
                    tabLayout.getTabAt(1).select(); // Reselect "BÀI ĐĂNG"
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateDurationSelection() {
        // Reset all backgrounds
        duration15s.setBackground(null);
        duration60s.setBackground(null);
        duration10min.setBackground(null);

        // Set selected background
        int backgroundResId = R.drawable.selected_duration_background;

        switch (selectedDuration) {
            case 15:
                duration15s.setBackground(ResourcesCompat.getDrawable(getResources(), backgroundResId, null));
                break;
            case 60:
                duration60s.setBackground(ResourcesCompat.getDrawable(getResources(), backgroundResId, null));
                break;
            case 600:
                duration10min.setBackground(ResourcesCompat.getDrawable(getResources(), backgroundResId, null));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_VIDEO_REQUEST) {
                Uri selectedVideoUri = data.getData();
                if (selectedVideoUri != null) {
                    Intent intent = new Intent(this, VideoPreviewActivity.class);
                    intent.putExtra("USER_ID", userID);
                    intent.putExtra("USER_ID_NAME", userIdName);
                    intent.putExtra("video_uri", selectedVideoUri);
                    startActivity(intent);
                }
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                Toast.makeText(this, "Chức năng ảnh chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Lỗi khởi động camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
    }

    private void startRecording() {
        if (videoCapture == null) return;

        // Create filename with timestamp
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String fileName = "TikTok_" + sdf.format(new Date()) + ".mp4";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(this, options)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        isRecording = true;
                        updateRecordingUI(true);
                        startRecordingTimer();
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                        if (finalizeEvent.hasError()) {
                            Toast.makeText(this, "Lỗi quay video: " + finalizeEvent.getCause(), Toast.LENGTH_SHORT).show();
                        } else {
                            Uri videoUri = finalizeEvent.getOutputResults().getOutputUri();
                            openVideoPreview(videoUri);
                        }
                        isRecording = false;
                        updateRecordingUI(false);
                        if (recordingTimer != null) {
                            recordingTimer.cancel();
                        }
                    }
                });
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
    }

    private void startRecordingTimer() {
        recordingProgress.setVisibility(View.VISIBLE);
        recordingProgress.setMax(selectedDuration * 1000); // Convert to milliseconds
        recordingProgress.setProgress(0);

        recordingTimer = new CountDownTimer(selectedDuration * 1000L, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (selectedDuration * 1000 - millisUntilFinished);
                recordingProgress.setProgress(progress);
            }

            @Override
            public void onFinish() {
                recordingProgress.setProgress(selectedDuration * 1000);
                stopRecording();
            }
        }.start();
    }

    private void flipCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ?
                CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }

    private void updateRecordingUI(boolean isRecording) {
        if (isRecording) {
            recordButton.setBackgroundResource(R.drawable.record_button_recording);
            recordingProgress.setVisibility(View.VISIBLE);
        } else {
            recordButton.setBackgroundResource(R.drawable.record_button_background);
            recordingProgress.setVisibility(View.GONE);
        }
    }

    private void openVideoPreview(Uri videoUri) {
        Intent intent = new Intent(this, VideoPreviewActivity.class);
        intent.putExtra("USER_ID", userID);
        intent.putExtra("USER_ID_NAME", userIdName);
        intent.putExtra("video_uri", videoUri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkPermissions()) {
                startCamera();
            } else {
                Toast.makeText(this, "Cần cấp quyền để sử dụng camera", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}