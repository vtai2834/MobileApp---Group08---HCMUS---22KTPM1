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
    private static final String SUPABASE_URL = "https://buvzptsvgxckgiwisrwn.supabase.co"; // Replace with your Supabase URL
    private static final String SUPABASE_BUCKET = "videos"; // Replace with your bucket name
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ1dnpwdHN2Z3hja2dpd2lzcnduIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTcxMzEwNiwiZXhwIjoyMDY1Mjg5MTA2fQ.bteXSCYMCYrxiDcspN6OIXpo4CWxDySjXccoyBAKhB8"; // Replace with your Supabase API key

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

    public static String uploadThumbnail(File thumbnailFile) throws IOException {
        Log.d("Supabase", "Uploading thumbnail: " + thumbnailFile.getAbsolutePath());

        String uniqueFileName = UUID.randomUUID().toString() + ".jpg"; // Generate UUID + file extension
        RequestBody requestBody = RequestBody.create(thumbnailFile, MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + uniqueFileName)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d("Supabase", "Thumbnail uploaded successfully!");
                return SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + uniqueFileName;
            } else {
                Log.e("Supabase", "Thumbnail upload failed: " + response.message());
                Log.d(
                        "Supabase",
                        "Thumbnail upload failed: " + response.message()
                );
                return null;
            }
        }
    }

    public static String uploadAvatar(File avatarFile) throws IOException {
        Log.d("Supabase", "Uploading avatar: " + avatarFile.getAbsolutePath());

        String uniqueFileName = UUID.randomUUID().toString() + ".jpg";
        RequestBody requestBody = RequestBody.create(avatarFile, MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + uniqueFileName)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d("Supabase", "Avatar uploaded successfully!");
                return SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + uniqueFileName;
            } else {
                Log.e("Supabase", "Avatar upload failed: " + response.message());
                return null;
            }
        }
    }

}