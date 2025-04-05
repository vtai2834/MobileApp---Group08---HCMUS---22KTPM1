package com.example.tiktok;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityNotificationAdapter extends RecyclerView.Adapter<ActivityNotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;

    public ActivityNotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Set user avatar
        Glide.with(holder.itemView.getContext())
                .load(notification.getUserAvatar())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(holder.avatarImageView);

        // Set notification text
        String notificationText = notification.getUsername() + " " + notification.getMessage();
        holder.notificationTextView.setText(notificationText);

        // Set time
        holder.timeTextView.setText(formatDate(notification.getTimestamp()));

        // Set video thumbnail if available
        if (notification.getContentId() != null && !notification.getContentId().isEmpty()) {
            holder.videoThumbnailView.setVisibility(View.VISIBLE);
            // In a real app, load the video thumbnail here
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.video_placeholder)
                    .into(holder.videoThumbnailView);
        } else {
            holder.videoThumbnailView.setVisibility(View.GONE);
        }

        // Set action buttons for comments
        if (notification.getType().equals("comment")) {
            holder.replyButton.setVisibility(View.VISIBLE);
            holder.likeButton.setVisibility(View.VISIBLE);
        } else {
            holder.replyButton.setVisibility(View.GONE);
            holder.likeButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar today = Calendar.getInstance();

        // If it's today, return "1 giờ" (simplified for demo)
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "1 giờ";
        }

        // If it's within a month, return the day/month
        int diffMonth = today.get(Calendar.MONTH) - calendar.get(Calendar.MONTH) +
                (today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)) * 12;

        if (diffMonth == 0) {
            return (calendar.get(Calendar.DAY_OF_MONTH)) + "/" + (calendar.get(Calendar.MONTH) + 1);
        }

        // Otherwise return the full date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarImageView;
        TextView notificationTextView;
        TextView timeTextView;
        ImageView videoThumbnailView;
        TextView replyButton;
        TextView likeButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            notificationTextView = itemView.findViewById(R.id.notificationTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            videoThumbnailView = itemView.findViewById(R.id.videoThumbnailView);
            replyButton = itemView.findViewById(R.id.replyButton);
            likeButton = itemView.findViewById(R.id.likeButton);
        }
    }
}

