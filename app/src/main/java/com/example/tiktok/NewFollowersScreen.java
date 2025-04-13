package com.example.tiktok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewFollowersScreen extends AppCompatActivity {

    private static final String TAG = "NewFollowersScreen";

    private RecyclerView followersRecyclerView;
    private FollowerAdapter followerAdapter;
    private List<Follower> followers;

    private ImageButton backButton;
    private TextView titleText;

    private String currentUserId;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_followers_screen);

        // Initialize views
        followersRecyclerView = findViewById(R.id.followersRecyclerView);
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");

        // Get current user ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("username", null);

        if (currentUserId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize followers list
        followers = new ArrayList<>();
        followerAdapter = new FollowerAdapter(followers, userId -> {
            // Handle follow back action
            followUser(userId);
        });
        followersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        followersRecyclerView.setAdapter(followerAdapter);

        // Set up back button
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Load followers
        loadFollowers();
    }

    private void loadFollowers() {
        // Get follow notifications from Firebase
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        notificationsRef.child(currentUserId).orderByChild("type").equalTo("follow")
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notification notification = snapshot.getValue(Notification.class);
                        if (notification != null) {
                            // Convert notification to follower
                            Follower follower = new Follower(
                                    notification.getUserId(),
                                    notification.getUsername(),
                                    "",
                                    notification.getUserAvatar(),
                                    NotificationManager.getInstance().formatDate(notification.getTimestamp()),
                                    false
                            );
                            followers.add(follower);
                        }
                    }

                    followerAdapter.notifyDataSetChanged();
                } else {
                    // If no notifications, create sample data
                    createSampleFollowers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                createSampleFollowers();
            }
        });
    }

    private void createSampleFollowers() {
        followers.add(new Follower(
                "user1",
                "Long Dương",
                "Những người bạn có thể biết",
                "https://via.placeholder.com/150",
                "3/17",
                false
        ));

        followers.add(new Follower(
                "user2",
                "nguyetbaby0",
                "",
                "https://via.placeholder.com/150",
                "3/14",
                false
        ));

        followers.add(new Follower(
                "user3",
                "Phạm Nguyễn Nhật Nam",
                "",
                "https://via.placeholder.com/150",
                "2/14",
                false
        ));

        followers.add(new Follower(
                "user4",
                "ngochappybeauty",
                "Bạn bè với 👤",
                "https://via.placeholder.com/150",
                "2/7",
                false
        ));

        followers.add(new Follower(
                "user5",
                "Khải Lâm Hoàng",
                "Follow 👤",
                "https://via.placeholder.com/150",
                "2/7",
                false
        ));

        followers.add(new Follower(
                "user6",
                "gungdin7",
                "",
                "https://via.placeholder.com/150",
                "2/6",
                false
        ));

        followers.add(new Follower(
                "user7",
                "V👑",
                "",
                "https://via.placeholder.com/150",
                "2/6",
                false
        ));

        followers.add(new Follower(
                "user8",
                "Nam Lương",
                "",
                "https://via.placeholder.com/150",
                "2/6",
                false
        ));

        followers.add(new Follower(
                "user9",
                "Lưu Ly",
                "",
                "https://via.placeholder.com/150",
                "2/6",
                false
        ));

        followers.add(new Follower(
                "user10",
                "Trường",
                "",
                "https://via.placeholder.com/150",
                "2/6",
                false
        ));

        followerAdapter.notifyDataSetChanged();
    }

    private void followUser(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get current user info
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                if (currentUserSnapshot.exists()) {
                    String currentUserIdName = currentUserSnapshot.child("idName").getValue(String.class);

                    // Get target user info
                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot targetUserSnapshot) {
                            if (targetUserSnapshot.exists()) {
                                String targetUserIdName = targetUserSnapshot.child("idName").getValue(String.class);

                                // Add current user to target user's followers
                                usersRef.child(userId).child("follower").child(currentUserIdName).setValue(true);
                                usersRef.child(userId).child("followerCount").setValue(ServerValue.increment(1));

                                // Add target user to current user's following
                                usersRef.child(currentUserId).child("following").child(targetUserIdName).setValue(true);
                                usersRef.child(currentUserId).child("followingCount").setValue(ServerValue.increment(1));

                                // Update UI
                                for (Follower follower : followers) {
                                    if (follower.getUserId().equals(userId)) {
                                        follower.setFollowing(true);
                                        followerAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                }

                                // Create follow notification
                                NotificationManager.getInstance().createFollowNotification(
                                        currentUserId,
                                        userId
                                );

                                Toast.makeText(NewFollowersScreen.this, "Đã follow lại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(NewFollowersScreen.this, "Lỗi khi follow", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NewFollowersScreen.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Follower model class
    static class Follower {
        private String userId;
        private String username;
        private String subtitle;
        private String avatarUrl;
        private String followDate;
        private boolean isFollowing;

        public Follower(String userId, String username, String subtitle, String avatarUrl,
                        String followDate, boolean isFollowing) {
            this.userId = userId;
            this.username = username;
            this.subtitle = subtitle;
            this.avatarUrl = avatarUrl;
            this.followDate = followDate;
            this.isFollowing = isFollowing;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getSubtitle() { return subtitle; }
        public String getAvatarUrl() { return avatarUrl; }
        public String getFollowDate() { return followDate; }
        public boolean isFollowing() { return isFollowing; }
        public void setFollowing(boolean following) { isFollowing = following; }
    }
}

