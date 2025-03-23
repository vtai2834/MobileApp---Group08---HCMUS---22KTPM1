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

    public CommentBottomSheet(String videoId, String userID) {
        this.videoID = videoId;
        this.userID = userID;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.activity_comment_screen, container, false);

        // Initialize views
        recyclerViewComments = view.findViewById(R.id.rv_comments);
        editTextComment = view.findViewById(R.id.et_comment_input);
        buttonSendComment = view.findViewById(R.id.btn_send_comment);
        textCommentCount = view.findViewById(R.id.tv_comment_count);
        btn_close = view.findViewById(R.id.btn_close);

        // Initialize list
        commentList = new ArrayList<>();

        DatabaseReference cmtRef = FirebaseDatabase.getInstance().getReference("comments").child(videoID);

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

        // Handle comment submission
        buttonSendComment.setOnClickListener(v -> {
            String commentText = editTextComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                Comment newComment = new Comment(
                        "Bạn " + userID,
                        "data:image/jpeg;base64,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",
                        commentText,
                        "Vừa xong"
                );

                commentList.add(newComment);
                textCommentCount.setText(String.format("%d Comments", commentList.size()));
                commentAdapter.notifyItemInserted(commentList.size() - 1);
                recyclerViewComments.scrollToPosition(commentList.size() - 1);
                editTextComment.setText("");

                cmtRef.child(userID).push().setValue(newComment);
            }
        });

        // Handle close button
        btn_close.setOnClickListener(v -> dismiss()); // Correct way to close BottomSheetDialogFragment

        return view;
    }
}
