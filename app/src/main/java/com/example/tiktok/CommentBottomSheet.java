package com.example.tiktok;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private ImageButton buttonSendComment;
    private TextView textCommentCount;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private ImageButton btn_close;
    private String videoID;
    private String userID;
    private NotificationManager notificationManager;
    private Video videoItem;

    private User current_user;
    public CommentBottomSheet(String videoId, String userID, Video videoItem) {
        this.videoID = videoId;
        this.userID = userID;
        this.videoItem = videoItem;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.activity_comment_bottom_sheet, container, false);


        // Initialize views
        recyclerViewComments = view.findViewById(R.id.rv_comments);
        editTextComment = view.findViewById(R.id.et_comment_input);
        buttonSendComment = view.findViewById(R.id.btn_send_comment);
        textCommentCount = view.findViewById(R.id.tv_comment_count);
        btn_close = view.findViewById(R.id.btn_close);

        // Initialize notification manager
        notificationManager = NotificationManager.getInstance();

        // Đầu tiên, khởi tạo commentList và adapter
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewComments.setAdapter(commentAdapter);

        DatabaseReference cmtRef = FirebaseDatabase.getInstance().getReference("comments").child(videoID);
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoID);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        DatabaseReference cur_userRef = userRef.child(userID);
        cur_userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                    current_user = snapshot.getValue(User.class);
                else
                    Log.d("CMTBOTTOMSHEET", "User not found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                current_user = new User(
                        "null",
                        "null",
                        0,
                        0,
                        0,
                        "null",
                        "null",
                        "null",
                        "null"
                );
            }
        });

        cmtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear(); // Clear old data
                final int[] pendingRequests = {0}; // Đếm số lượng request đang chờ

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    // Không có comment nào
                    textCommentCount.setText("0 Comments");
                    return;
                }

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { // Iterate through users
                    for (DataSnapshot snapshot : userSnapshot.getChildren()) { // Iterate through comments
                        Comment comment = snapshot.getValue(Comment.class);
                        if (comment != null) {
                            String userId = comment.getUserId();
                            Log.d("CMTBOTTOMSHEET", "check userId: " + userId);

                            pendingRequests[0]++; // Tăng số lượng request

                            userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String userIdName = snapshot.child("idName").getValue(String.class);
                                        String avatar = snapshot.child("avatar").getValue(String.class);
                                        if (userIdName != null && !userIdName.isEmpty()) {
                                            comment.setUserId(userIdName);
                                            comment.setUserAvatar(avatar);
                                        }

                                        commentList.add(comment);
                                        Log.d("Firebase", "Comment added: " + comment.getCommentText() + " | Total: " + commentList.size());
                                    }

                                    pendingRequests[0]--; // Giảm số lượng request

                                    // Nếu đây là request cuối cùng, cập nhật UI
                                    if (pendingRequests[0] == 0) {
                                        commentAdapter.notifyDataSetChanged();
                                        textCommentCount.setText(String.format("%d Comments", commentList.size()));
                                        Log.d("Firebase", "All comments loaded. Total: " + commentList.size());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error updating comment count: " + error.getMessage());
                                    pendingRequests[0]--; // Giảm số lượng request ngay cả khi có lỗi

                                    // Nếu đây là request cuối cùng, cập nhật UI
                                    if (pendingRequests[0] == 0) {
                                        commentAdapter.notifyDataSetChanged();
                                        textCommentCount.setText(String.format("%d Comments", commentList.size()));
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });

        // Handle close button click
        btn_close.setOnClickListener(v -> dismiss());

        // Handle comment submission
        buttonSendComment.setOnClickListener(v -> {
            String commentText = editTextComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                // Create a unique ID for the comment
                String commentId = UUID.randomUUID().toString();

                // Get current timestamp
                String timestamp = String.valueOf(System.currentTimeMillis());

                // Create comment object
                Comment newComment = new Comment(
                        userID,
                        current_user.getAvatar(), // Default avatar
                        commentText,
                        timestamp
                );

                // Save comment to Firebase
                DatabaseReference userCommentRef = FirebaseDatabase.getInstance()
                        .getReference("comments")
                        .child(videoID)
                        .child(userID)
                        .child(commentId);

                userCommentRef.setValue(newComment)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firebase", "Comment added successfully");

                            // Update comment count in video
                            videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String currentComments = snapshot.child("comments").getValue(String.class);
                                        int commentCount = 0;
                                        try {
                                            commentCount = Integer.parseInt(currentComments);
                                        } catch (NumberFormatException e) {
                                            Log.e("Firebase", "Error parsing comment count: " + e.getMessage());
                                        }

                                        commentCount += 1;
                                        // Increment comment count
                                        videoRef.child("comments").setValue(String.valueOf(commentCount));
                                        videoItem.setComments(String.valueOf(commentCount));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error updating comment count: " + error.getMessage());
                                }
                            });

                            // Get video owner ID to send notification
                            videoRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String videoOwnerId = snapshot.getValue(String.class);

                                        // Create notification for comment
                                        if (videoOwnerId != null && !videoOwnerId.equals(userID)) {
                                            notificationManager.createCommentNotification(
                                                    userID,
                                                    videoID,
                                                    videoOwnerId,
                                                    commentText
                                            );
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error getting video owner: " + error.getMessage());
                                }
                            });

                            // Add comment to local list and update UI
                            commentList.add(new Comment(
                                    current_user.getIdName(),
                                    current_user.getAvatar(),
                                    commentText,
                                    timestamp
                            ));
                            commentAdapter.notifyDataSetChanged();
                            textCommentCount.setText(String.format("%d Comments", commentList.size()));

                            // Clear input field
                            editTextComment.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Error adding comment: " + e.getMessage());
                        });
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set the bottom sheet to expanded state when opened
        if (getDialog() != null) {
            // Better approach: Use BottomSheetBehavior instead of trying to find the design_bottom_sheet
            View view = getView();
            if (view != null) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(params);
            }
        }
    }
}

