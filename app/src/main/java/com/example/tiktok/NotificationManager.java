package com.example.tiktok;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static NotificationManager instance;
    private final DatabaseReference notificationsRef;
    private final DatabaseReference usersRef;

    private NotificationManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationsRef = database.getReference("notifications");
        usersRef = database.getReference("Users");
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Create a follow notification
     * @param followerId ID of the user who is following
     * @param targetUserId ID of the user being followed
     */
    public void createFollowNotification(String followerId, String targetUserId) {
        if (followerId.equals(targetUserId)) return; // Don't notify if user follows themselves

        getUserInfo(followerId, (username, avatarUrl) -> {
            String notificationId = UUID.randomUUID().toString();
            String message = "đã bắt đầu follow bạn";

            Notification notification = new Notification(
                    notificationId,
                    followerId,
                    username,
                    avatarUrl,
                    targetUserId,
                    "follow",
                    "",
                    message,
                    System.currentTimeMillis()
            );

            notificationsRef.child(targetUserId).child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Follow notification created"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating follow notification", e));
        });
    }

    /**
     * Create a like notification
     * @param likerId ID of the user who liked the video
     * @param videoId ID of the video that was liked
     * @param videoOwnerId ID of the video owner
     */
    public void createLikeNotification(String likerId, String videoId, String videoOwnerId) {
        if (likerId.equals(videoOwnerId)) return; // Don't notify if user likes their own video

        getUserInfo(likerId, (username, avatarUrl) -> {
            String notificationId = UUID.randomUUID().toString();
            String message = "đã thích video của bạn";

            Notification notification = new Notification(
                    notificationId,
                    likerId,
                    username,
                    avatarUrl,
                    videoOwnerId,
                    "like",
                    videoId,
                    message,
                    System.currentTimeMillis()
            );

            notificationsRef.child(videoOwnerId).child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Like notification created"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating like notification", e));
        });
    }

    /**
     * Create a comment notification
     * @param commenterId ID of the user who commented
     * @param videoId ID of the video that was commented on
     * @param videoOwnerId ID of the video owner
     * @param commentText The text of the comment
     */
    public void createCommentNotification(String commenterId, String videoId, String videoOwnerId, String commentText) {
        if (commenterId.equals(videoOwnerId)) return; // Don't notify if user comments on their own video

        getUserInfo(commenterId, (username, avatarUrl) -> {
            String notificationId = UUID.randomUUID().toString();
            String message = "đã bình luận: " +
                    (commentText.length() > 30 ? commentText.substring(0, 27) + "..." : commentText);

            Notification notification = new Notification(
                    notificationId,
                    commenterId,
                    username,
                    avatarUrl,
                    videoOwnerId,
                    "comment",
                    videoId,
                    message,
                    System.currentTimeMillis()
            );

            notificationsRef.child(videoOwnerId).child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Comment notification created"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating comment notification", e));
        });
    }

    /**
     * Format date for display in notifications
     */
    public String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar today = Calendar.getInstance();

        // If it's today, return the time
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "1 giờ"; // Simplified for demo
        }

        // If it's within the last week, return the day number
        int diff = today.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR);
        if (diff > 0 && diff <= 30) {
            return diff + "/" + (calendar.get(Calendar.MONTH) + 1);
        }

        // Otherwise return the full date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get user information (username and avatar URL)
     * @param userId ID of the user
     * @param callback Callback to receive the username and avatar URL
     */
    private void getUserInfo(String userId, UserInfoCallback callback) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("idName").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = dataSnapshot.child("account").getValue(String.class);
                    }

                    String avatarUrl = dataSnapshot.child("avatar").getValue(String.class);
                    if (avatarUrl == null || avatarUrl.isEmpty()) {
                        avatarUrl = "https://via.placeholder.com/150"; // Default avatar
                    }

                    callback.onUserInfoReceived(username, avatarUrl);
                } else {
                    Log.e(TAG, "User not found: " + userId);
                    callback.onUserInfoReceived("Unknown User", "https://via.placeholder.com/150");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting user info", databaseError.toException());
                callback.onUserInfoReceived("Unknown User", "https://via.placeholder.com/150");
            }
        });
    }

    /**
     * Callback interface for getting user information
     */
    interface UserInfoCallback {
        void onUserInfoReceived(String username, String avatarUrl);
    }
}

