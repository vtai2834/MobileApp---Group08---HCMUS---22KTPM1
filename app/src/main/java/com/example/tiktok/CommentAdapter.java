package com.example.tiktok;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Set username
        holder.tvUsername.setText(comment.getUserId());

        // Set comment text
        holder.tvCommentText.setText(comment.getCommentText());

        // Load avatar with Glide
        Glide.with(holder.ivAvatar.getContext())
                .load(comment.getUserAvatar())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivAvatar);

        // Set time
        try {
            long timestamp = Long.parseLong(comment.getTimestamp());
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            holder.tvTime.setText(timeAgo);
        } catch (NumberFormatException e) {
            holder.tvTime.setText("Unknown");
        }

        // Set like count (if available)
        // holder.tvLikes.setText(String.valueOf(comment.getLikes()));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUsername;
        TextView tvCommentText;
        TextView tvTime;
        TextView tvLikes;
        ImageView ivLike;
        TextView tvReply;
        TextView tvLike;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_comment_avatar);
            tvUsername = itemView.findViewById(R.id.tv_comment_username);
            tvCommentText = itemView.findViewById(R.id.tv_comment_text);
            tvTime = itemView.findViewById(R.id.tv_comment_time);
            tvLikes = itemView.findViewById(R.id.tv_comment_likes);
            ivLike = itemView.findViewById(R.id.iv_comment_like);
            tvReply = itemView.findViewById(R.id.tv_reply);
            tvLike = itemView.findViewById(R.id.tv_like);
        }
    }
}

