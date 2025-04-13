package com.example.tiktok;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder> {

    private List<NewFollowersScreen.Follower> followers;
    private OnFollowClickListener listener;

    public interface OnFollowClickListener {
        void onFollowClick(String userId);
    }

    public FollowerAdapter(List<NewFollowersScreen.Follower> followers, OnFollowClickListener listener) {
        this.followers = followers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FollowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follower, parent, false);
        return new FollowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerViewHolder holder, int position) {
        NewFollowersScreen.Follower follower = followers.get(position);

        // Set username
        holder.usernameText.setText(follower.getUsername());

        // Set subtitle if available
        if (follower.getSubtitle() != null && !follower.getSubtitle().isEmpty()) {
            holder.subtitleText.setVisibility(View.VISIBLE);
            holder.subtitleText.setText(follower.getSubtitle());
        } else {
            holder.subtitleText.setVisibility(View.GONE);
        }

        // Set follow date
        holder.followDateText.setText("đã bắt đầu follow bạn. " + follower.getFollowDate());

        // Load avatar
        Glide.with(holder.itemView.getContext())
                .load(follower.getAvatarUrl())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(holder.avatarImageView);

        // Set follow button state
        if (follower.isFollowing()) {
            holder.followButton.setText("Đang follow");
            holder.followButton.setBackgroundResource(R.drawable.button_outline_background);
            holder.followButton.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        } else {
            holder.followButton.setText("Follow lại");
            holder.followButton.setBackgroundResource(R.drawable.button_primary_background);
            holder.followButton.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        }

        // Set follow button click listener
        holder.followButton.setOnClickListener(v -> {
            if (!follower.isFollowing() && listener != null) {
                listener.onFollowClick(follower.getUserId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return followers.size();
    }

    static class FollowerViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarImageView;
        TextView usernameText;
        TextView subtitleText;
        TextView followDateText;
        MaterialButton followButton;

        public FollowerViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            usernameText = itemView.findViewById(R.id.usernameText);
            subtitleText = itemView.findViewById(R.id.subtitleText);
            followDateText = itemView.findViewById(R.id.followDateText);
            followButton = itemView.findViewById(R.id.followButton);
            followButton.setBackgroundColor(0xFFFF0000);
        }
    }
}

