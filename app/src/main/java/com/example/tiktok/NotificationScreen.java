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

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationScreen extends AppCompatActivity {

    private static final String TAG = "NotificationScreen";

    private RecyclerView notificationRecyclerView;
    private ActivityNotificationAdapter notificationAdapter;
    private List<Notification> notifications;
    private List<Notification> filteredNotifications;

    private TextView titleText;
    private ImageButton backButton;
    private TabLayout tabLayout;

    private String currentUserId;
    private DatabaseReference notificationsRef;
    private String currentTab = "all"; // "all", "priority", "other"
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);

        language = getIntent().getStringExtra("language");

        if (language != null) {
            if (language.equals("English") || language.equals("Tiếng Anh")) {
                LocaleHelper.setLocale(this, "en");
            } else {
                LocaleHelper.setLocale(this, "vi");
            }
        } else {
            LocaleHelper.setLocale(this, "vi");
        }

        // Initialize views
        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        titleText = findViewById(R.id.titleText);
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationsRef = database.getReference("notifications");

        // Get current user ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("username", null);

        if (currentUserId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize notifications list
        notifications = new ArrayList<>();
        filteredNotifications = new ArrayList<>();

        // Set up RecyclerView
        notificationAdapter = new ActivityNotificationAdapter(filteredNotifications);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationRecyclerView.setAdapter(notificationAdapter);

        // Set up TabLayout
        setupTabLayout();

        // Set up back button
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Load notifications
        loadNotifications();
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Mức độ ưu tiên"));
        tabLayout.addTab(tabLayout.newTab().setText("Khác"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "priority";
                        break;
                    case 1:
                        currentTab = "other";
                        break;
                }
                filterNotifications();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void loadNotifications() {
        // Check if notifications node exists, if not create sample data
        notificationsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    createSampleNotifications();
                } else {
                    fetchNotifications();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking notifications", databaseError.toException());
                Toast.makeText(NotificationScreen.this,
                        "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchNotifications() {
        notificationsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifications.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notifications.add(notification);
                    }
                }

                // Sort notifications by timestamp (newest first)
                Collections.sort(notifications, (n1, n2) ->
                        Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                // Default to priority tab
                tabLayout.selectTab(tabLayout.getTabAt(0));
                currentTab = "priority";
                filterNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading notifications", databaseError.toException());
                Toast.makeText(NotificationScreen.this,
                        "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createSampleNotifications() {
        // Create sample notifications
        List<Notification> sampleNotifications = new ArrayList<>();

        // Comment notification
        sampleNotifications.add(new Notification(
                "comment1",
                "user1",
                "Binhtinhluilai",
                "https://via.placeholder.com/150",
                currentUserId,
                "comment",
                "video1",
                "đã bình luận: Anh tài ơi",
                System.currentTimeMillis() - 3600000 // 1 hour ago
        ));

        // Like notifications
        sampleNotifications.add(new Notification(
                "like1",
                "user2",
                "Tuấn Phùng8252",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video1",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 3 // 3 days ago
        ));

        sampleNotifications.add(new Notification(
                "like2",
                "user3",
                "anhha",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video2",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 5 // 5 days ago
        ));

        sampleNotifications.add(new Notification(
                "like3",
                "user4",
                "Thảo ._.Ngân",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video3",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 7 // 7 days ago
        ));

        sampleNotifications.add(new Notification(
                "like4",
                "user5",
                "Thao Nguyen Nguyen 🌼",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video4",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 8 // 8 days ago
        ));

        sampleNotifications.add(new Notification(
                "like5",
                "user6",
                "qdung",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video5",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 8 // 8 days ago
        ));

        sampleNotifications.add(new Notification(
                "like6",
                "user7",
                "sua_tuoi",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video6",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 9 // 9 days ago
        ));

        sampleNotifications.add(new Notification(
                "like7",
                "user8",
                "sua_tuoi",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video7",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 9 // 9 days ago
        ));

        sampleNotifications.add(new Notification(
                "like8",
                "user9",
                "dầu tây nhiệt đới, Tiểu Hy",
                "https://via.placeholder.com/150",
                currentUserId,
                "like",
                "video8",
                "đã thích video của bạn",
                System.currentTimeMillis() - 86400000 * 10 // 10 days ago
        ));

        // Save sample notifications to Firebase
        for (Notification notification : sampleNotifications) {
            notificationsRef.child(currentUserId).child(notification.getId()).setValue(notification);
        }

        // Load the notifications
        fetchNotifications();
    }

    private void filterNotifications() {
        filteredNotifications.clear();

        for (Notification notification : notifications) {
            if (currentTab.equals("priority") && notification.getType().equals("comment")) {
                filteredNotifications.add(notification);
            } else if (currentTab.equals("other") && !notification.getType().equals("comment")) {
                filteredNotifications.add(notification);
            }
        }

        notificationAdapter.notifyDataSetChanged();
    }
}

