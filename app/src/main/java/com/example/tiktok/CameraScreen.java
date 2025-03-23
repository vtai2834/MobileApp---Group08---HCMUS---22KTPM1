package com.example.tiktok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

public class CameraScreen extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 10;
    private PreviewView viewFinder;
    private ImageButton recordButton;
    private ImageButton flipCameraButton;
    private ImageButton closeButton;
    private ImageButton gallery;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private boolean isRecording = false;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private static final int PICK_VIDEO_REQUEST = 1;

    //userID infor
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);

        userID = getIntent().getStringExtra("USER_ID");

        initializeViews();
        setupClickListeners();

        if (checkPermissions()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    private void initializeViews() {
        viewFinder = findViewById(R.id.viewFinder);
        recordButton = findViewById(R.id.recordButton);
        flipCameraButton = findViewById(R.id.flipCameraButton);
        closeButton = findViewById(R.id.closeButton);
        gallery = findViewById(R.id.gallery);
    }

    private void setupClickListeners() {
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        flipCameraButton.setOnClickListener(v -> flipCamera());

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraScreen.this, HomeScreen.class);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, PICK_VIDEO_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                Intent intent = new Intent(this, VideoPreviewActivity.class);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("video_uri", selectedVideoUri);
                startActivity(intent);
            }
        }
    }



    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
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

    private static final int REQUEST_PERMISSION = 100;
    private void startRecording() {
        // Kiểm tra quyền ghi vào bộ nhớ ngoài và sử dụng camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Nếu chưa có quyền, yêu cầu quyền từ người dùng
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return;
        }

        if (videoCapture == null) return;

        String name = "CameraX-recording-" + System.currentTimeMillis() + ".mp4";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        recording = videoCapture.getOutput().prepareRecording(this, options)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        isRecording = true;
                        updateRecordingUI();
                    }
                });
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
            isRecording = false;
            updateRecordingUI();
        }
    }

    private void flipCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ?
                CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }

    private void updateRecordingUI() {
        recordButton.setBackgroundResource(isRecording ?
                R.drawable.record_button_recording : R.drawable.record_button_background);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkPermissions()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}