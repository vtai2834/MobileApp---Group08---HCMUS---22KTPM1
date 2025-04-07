package com.example.tiktok;

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

import com.example.tiktok.R;

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

    public CommentBottomSheet(String videoId, String userID) {
        this.videoID = videoId;
        this.userID = userID;
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

        // Initialize list
        commentList = new ArrayList<>();

        DatabaseReference cmtRef = FirebaseDatabase.getInstance().getReference("comments").child(videoID);
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoID);

        cmtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear(); // Clear old data

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { // Iterate through users
                    for (DataSnapshot snapshot : userSnapshot.getChildren()) { // Iterate through comments
                        Comment comment = snapshot.getValue(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                            Log.d("Firebase", "Comment: " + comment.getCommentText() + " | Total: " + commentList.size());
                        }
                    }
                }

                // Notify adapter
                commentAdapter.notifyDataSetChanged();
                textCommentCount.setText(String.format("%d Comments", commentList.size()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });

        // Set up RecyclerView
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext())); // Fix context issue
        recyclerViewComments.setAdapter(commentAdapter);

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
                        "https://via.placeholder.com/150", // Default avatar
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

                                        // Increment comment count
                                        videoRef.child("comments").setValue(String.valueOf(commentCount + 1));
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
                            commentList.add(newComment);
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

