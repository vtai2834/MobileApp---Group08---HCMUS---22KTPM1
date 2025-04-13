package com.example.tiktok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxScreen extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private List<ChatPreview> chatPreviews;
    private ChatPreviewAdapter chatPreviewAdapter;

    private String currentUserId;
    private DatabaseReference usersRef;
    private DatabaseReference notificationsRef;

    private CircleImageView profileImage1, profileImage2, profileImage3, profileImage4;
    private TextView newFollowersText, activityText, shopText, systemText;
    private View newFollowersRow, activityRow, shopRow, systemRow;
    private View shopBadge, systemBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_screen);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");

        // Create notifications node if it doesn't exist
        notificationsRef = database.getReference("notifications");
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // If notifications node doesn't exist, create it
                if (!dataSnapshot.exists()) {
                    notificationsRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Get current user ID
//        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        currentUserId = sharedPreferences.getString("username", null);
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUserID();
        }

        if (currentUserId == null) {
            // If user is not logged in, redirect to login screen
            // In a real app, you would have a login screen
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Set up chat preview recycler view
        chatPreviews = new ArrayList<>();
        chatPreviewAdapter = new ChatPreviewAdapter(chatPreviews);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatPreviewAdapter);

        // Add sample chat previews (in a real app, these would come from Firebase)
        addSampleChatPreviews();

        // Load notification counts
        loadNotificationCounts();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        // Top profile images
        profileImage1 = findViewById(R.id.profileImage1);
        profileImage2 = findViewById(R.id.profileImage2);
        profileImage3 = findViewById(R.id.profileImage3);
        profileImage4 = findViewById(R.id.profileImage4);

        // Notification rows
        newFollowersRow = findViewById(R.id.newFollowersRow);
        activityRow = findViewById(R.id.activityRow);
        shopRow = findViewById(R.id.shopRow);
        systemRow = findViewById(R.id.systemRow);

        // Notification texts
        newFollowersText = findViewById(R.id.newFollowersText);
        activityText = findViewById(R.id.activityText);
        shopText = findViewById(R.id.shopText);
        systemText = findViewById(R.id.systemText);

        // Badges
        shopBadge = findViewById(R.id.shopBadge);
        systemBadge = findViewById(R.id.systemBadge);

        // Chat recycler view
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // Load profile images
        loadProfileImages();
    }

    private void loadProfileImages() {
        // In a real app, these would come from Firebase
        // For now, we'll use placeholder images
        String[] imageUrls = {
                "https://via.placeholder.com/150",
                "https://via.placeholder.com/150",
                "https://via.placeholder.com/150",
                "https://via.placeholder.com/150"
        };

        // Load images with Glide
        Glide.with(this).load(imageUrls[0]).placeholder(R.drawable.default_profile).into(profileImage1);
        Glide.with(this).load(imageUrls[1]).placeholder(R.drawable.default_profile).into(profileImage2);
        Glide.with(this).load(imageUrls[2]).placeholder(R.drawable.default_profile).into(profileImage3);
        Glide.with(this).load(imageUrls[3]).placeholder(R.drawable.default_profile).into(profileImage4);
    }

    private void setupClickListeners() {
        // New followers row click
        newFollowersRow.setOnClickListener(v -> {
            Intent intent = new Intent(InboxScreen.this, NewFollowersScreen.class);
            startActivity(intent);
        });

        // Activity row click
        activityRow.setOnClickListener(v -> {
            Intent intent = new Intent(InboxScreen.this, NotificationScreen.class);
            startActivity(intent);
        });

        // Shop row click
        shopRow.setOnClickListener(v -> {
            // In a real app, this would navigate to the TikTok Shop screen
            shopBadge.setVisibility(View.GONE);
        });

        // System row click
        systemRow.setOnClickListener(v -> {
            // In a real app, this would navigate to the system notifications screen
            systemBadge.setVisibility(View.GONE);
        });

        // Navigation buttons
        findViewById(R.id.home_button).setOnClickListener(v -> {
            // Navigate to home screen
            Intent intent = new Intent(InboxScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            // Navigate to profile screen
            Intent intent = new Intent(InboxScreen.this, ProfileScreen.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.upload_button).setOnClickListener(v -> {
            // Navigate to camera screen
            Intent intent = new Intent(InboxScreen.this, CameraScreen.class);
            startActivity(intent);
        });

        findViewById(R.id.discover_button).setOnClickListener(v -> {
            // Navigate to discover screen
            Intent intent = new Intent(InboxScreen.this, SearchScreen.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadNotificationCounts() {
        // Load new followers count from Users node
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(currentUserId).child("follower").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    // Get the most recent follower
                    for (DataSnapshot followerSnapshot : dataSnapshot.getChildren()) {
                        String followerIdName = followerSnapshot.getKey();

                        // Find user with this idName to get their username
                        userRef.orderByChild("idName").equalTo(followerIdName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    for (DataSnapshot user : userSnapshot.getChildren()) {
                                        String username = user.child("idName").getValue(String.class);
                                        newFollowersText.setText(username + " đã bắt đầu follow bạn.");
                                        break; // Just get the first one
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                newFollowersText.setText("Chưa có người follow mới.");
                            }
                        });

                        break; // Just get the first one
                    }
                } else {
                    newFollowersText.setText("Chưa có người follow mới.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                newFollowersText.setText("Chưa có người follow mới.");
            }
        });

        // Load activity notification
        notificationsRef.child(currentUserId).orderByChild("type").equalTo("comment")
                .limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Notification notification = snapshot.getValue(Notification.class);
                                if (notification != null) {
                                    activityText.setText(notification.getUsername() + " đã bình luận về video của bạn ");
                                }
                            }
                        } else {
                            activityText.setText("Binhtinhluilai đã bình luận: Anh tài ơi");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });

        // Set sample shop notification
        shopText.setText("7 tin cập nhật của cửa hàng và... • 1 ngày");
        shopBadge.setVisibility(View.VISIBLE);

        // Set sample system notification
        systemText.setText("LIVE: LIVE Studio hiện đã khả d... • 1 ngày");
        systemBadge.setVisibility(View.VISIBLE);
    }

    private void addSampleChatPreviews() {
        // These would come from Firebase in a real app
        chatPreviews.add(new ChatPreview(
                "Bánh Mỳ bột kem ...Dau 🍓",
                "Hãy chào Bánh Mỳ bột kem ...Dau 🍓",
                "https://via.placeholder.com/150",
                false,
                System.currentTimeMillis()
        ));

        chatPreviews.add(new ChatPreview(
                "Na",
                "Hãy chào Na",
                "https://via.placeholder.com/150",
                false,
                System.currentTimeMillis()
        ));

        chatPreviews.add(new ChatPreview(
                "i'm tired :')",
                "Đã xem",
                "https://via.placeholder.com/150",
                true,
                System.currentTimeMillis()
        ));

        chatPreviews.add(new ChatPreview(
                "86tranquangkhai",
                "Tài khoản bạn đang liên hệ... • 11/10/2024",
                "https://via.placeholder.com/150",
                true,
                System.currentTimeMillis()
        ));

        chatPreviewAdapter.notifyDataSetChanged();
    }

    // Inner class for chat previews
    static class ChatPreview {
        private String name;
        private String lastMessage;
        private String avatarUrl;
        private boolean isRead;
        private long timestamp;

        public ChatPreview(String name, String lastMessage, String avatarUrl, boolean isRead, long timestamp) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.avatarUrl = avatarUrl;
            this.isRead = isRead;
            this.timestamp = timestamp;
        }

        // Getters
        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public String getAvatarUrl() { return avatarUrl; }
        public boolean isRead() { return isRead; }
        public long getTimestamp() { return timestamp; }
    }

    // Adapter for chat previews
    static class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.ViewHolder> {
        private List<ChatPreview> chatPreviews;

        public ChatPreviewAdapter(List<ChatPreview> chatPreviews) {
            this.chatPreviews = chatPreviews;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_preview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatPreview chatPreview = chatPreviews.get(position);

            holder.nameText.setText(chatPreview.getName());
            holder.messageText.setText(chatPreview.getLastMessage());

            Glide.with(holder.itemView.getContext())
                    .load(chatPreview.getAvatarUrl())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(holder.avatarImage);

            // Show camera icon for unread messages
            holder.cameraIcon.setVisibility(chatPreview.isRead() ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return chatPreviews.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView avatarImage;
            TextView nameText;
            TextView messageText;
            ImageView cameraIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarImage = itemView.findViewById(R.id.chatAvatarImage);
                nameText = itemView.findViewById(R.id.chatNameText);
                messageText = itemView.findViewById(R.id.chatMessageText);
                cameraIcon = itemView.findViewById(R.id.chatCameraIcon);
            }
        }
    }
}

