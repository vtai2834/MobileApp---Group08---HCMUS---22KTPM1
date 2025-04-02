package com.example.tiktok;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.UUID;

public class ThumbnailGenerator {

    private static final String TAG = "ThumbnailGenerator";

    public interface ThumbnailCallback {
        void onThumbnailGenerated(String thumbnailUrl);
        void onError(Exception e);
    }

    /**
     * Generate a thumbnail from a video URI and upload it to Firebase Storage
     *
     * @param context Application context
     * @param videoUri URI of the video (can be local or remote)
     * @param callback Callback to receive the thumbnail URL or error
     */
    public static void generateAndUploadThumbnail(Context context, String videoUri, ThumbnailCallback callback) {
        try {
            // Create a MediaMetadataRetriever to extract a frame from the video
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            if (videoUri.startsWith("http") || videoUri.startsWith("https")) {
                // For remote videos
                retriever.setDataSource(videoUri, new HashMap<String, String>());
            } else {
                // For local videos
                retriever.setDataSource(context, Uri.parse(videoUri));
            }

            // Extract a frame at 1 second (1000000 microseconds)
            Bitmap bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            retriever.release();

            if (bitmap == null) {
                callback.onError(new Exception("Failed to extract thumbnail from video"));
                return;
            }

            // Compress the bitmap to JPEG format
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            // Upload the thumbnail to Firebase Storage
            uploadThumbnail(data, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail: " + e.getMessage());
            callback.onError(e);
        }
    }

    /**
     * Upload a thumbnail image to Firebase Storage
     *
     * @param thumbnailData Byte array of the thumbnail image
     * @param callback Callback to receive the thumbnail URL or error
     */
    private static void uploadThumbnail(byte[] thumbnailData, ThumbnailCallback callback) {
        // Get a reference to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a unique filename for the thumbnail
        String filename = "thumbnails/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference thumbnailRef = storageRef.child(filename);

        // Upload the thumbnail
        UploadTask uploadTask = thumbnailRef.putBytes(thumbnailData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL
                thumbnailRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String thumbnailUrl = uri.toString();
                        callback.onThumbnailGenerated(thumbnailUrl);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e);
            }
        });
    }
}