package com.example.tiktok;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpbaseSv {
    private static final String SUPABASE_URL = "https://kzmyevwdlqjbtevzmksy.supabase.co"; // Replace with your Supabase URL
    private static final String SUPABASE_BUCKET = "video_bucket"; // Replace with your bucket name
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt6bXlldndkbHFqYnRldnpta3N5Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MTY5MjY2OSwiZXhwIjoyMDU3MjY4NjY5fQ.osTE7aLyQ_63o_yYw7n6eerDMuBNkw5ynB0AUGhBkeU"; // Replace with your Supabase API key

    private static final OkHttpClient client = new OkHttpClient();

    // 🔹 1. Upload video with UUID
    public static String uploadVideo(File videoFile) throws IOException {
        Log.d("Supabase", "Uploading video: " + videoFile.getAbsolutePath());

        String uniqueFileName = UUID.randomUUID().toString() + ".mp4"; // Generate UUID + file extension
        RequestBody requestBody = RequestBody.create(videoFile, MediaType.parse("video/mp4"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + uniqueFileName)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d("Supabase", "Video uploaded successfully!");
                return SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + uniqueFileName;
            } else {
                Log.e("Supabase", "Upload failed: " + response.message());
                Log.d(
                        "Supabase",
                        "Upload failed: " + response.message()
                );
                return null;
            }
        }
    }
}