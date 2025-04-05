package com.example.tiktok;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.notificationTextView.setText(notification.getText());
        // Set Avatar using Glide or Picasso
        Glide.with(holder.itemView.getContext())
                .load(notification.getAvatarUrl())
                .into(holder.avatarImageView);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView avatarImageView;
        TextView notificationTextView;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            notificationTextView = itemView.findViewById(R.id.notificationTextView);
        }
    }

    public static class Notification {
        private String text;
        private String avatarUrl;

        public Notification(String text, String avatarUrl) {
            this.text = text;
            this.avatarUrl = avatarUrl;
        }

        public String getText() {
            return text;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }
}
