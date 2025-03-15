package com.example.tiktok;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    // ViewHolder
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvUsername;
        TextView tvComment;
        TextView tvTimestamp;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Sử dụng Glide để load ảnh đại diện
        Glide.with(holder.itemView.getContext())
                .load(comment.getAvatarUrl())
                .placeholder(R.drawable.default_profile)
                .into(holder.ivProfile);

        holder.tvUsername.setText(comment.getUsername());
        holder.tvComment.setText(comment.getCommentText());
        holder.tvTimestamp.setText(comment.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}

