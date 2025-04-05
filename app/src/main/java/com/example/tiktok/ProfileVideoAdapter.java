package com.example.tiktok;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileVideoAdapter extends RecyclerView.Adapter<ProfileVideoAdapter.VideoViewHolder> {

    private List<Video> videoList;
    private List<String> videoIds; // cho việc lấy ID video so sánh vs ID trong videoIdsItem -> lấy ra chỉ số trong list gốc
    private List<String> videoIdsItems;
    private Context context;
    private Map<Integer, ExoPlayer> playerMap = new HashMap<>();

    public ProfileVideoAdapter(List<Video> videoList, List<String> videoIds, List<String> videoIdsItems, Context context) {
        this.videoList = videoList;
        this.videoIds = videoIds;
        this.videoIdsItems = videoIdsItems;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videoList.get(position);
        String videoId = videoIds.get(position);

        // Load thumbnail
        if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(video.getThumbnailUrl())
                    .placeholder(R.drawable.video_placeholder)
                    .error(R.drawable.video_placeholder)
                    .centerCrop()
                    .into(holder.thumbnailView);
        } else {
            // If no thumbnail, try to generate a frame from the video
            Glide.with(context)
                    .load(video.getVideoUri())
                    .placeholder(R.drawable.video_placeholder)
                    .error(R.drawable.video_placeholder)
                    .centerCrop()
                    .into(holder.thumbnailView);
        }

        // Set video stats
        holder.likeCount.setText(video.getLikes());

        // Set click listener to open video
        holder.itemView.setOnClickListener(v -> {
            int pos = videoIdsItems.indexOf(videoId);

            if (pos != -1) {
                // Send position back to HomeScreen
                Intent intent = new Intent(context, HomeScreen.class);
                intent.putExtra("SEARCH_VIDEO_POSITION", pos);
                intent.putExtra("USER_ID", "-OLm4Zc1z1YSUyV7zHDm");
                context.startActivity(intent);
            } else {
                // Case when videoId not found in the list
                Log.e("ProfileVideoAdapter", "VideoId " + videoId + " not found in videoIdsFromHome");
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void releaseAllPlayers() {
        for (ExoPlayer player : playerMap.values()) {
            if (player != null) {
                player.release();
            }
        }
        playerMap.clear();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailView;
        TextView likeCount;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.videoThumbnail);
            likeCount = itemView.findViewById(R.id.likeCount);
        }
    }
}

