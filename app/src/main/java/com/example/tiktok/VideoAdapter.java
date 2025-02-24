package com.example.tiktok;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoItems;
    private Context context;
    private ExoPlayer currentPlayer = null;
    private int currentPlayingPosition = -1;
    private RecyclerView recyclerView;

    public VideoAdapter(List<Video> videoItems, Context context, RecyclerView recyclerView) {
        this.videoItems = videoItems;
        this.context = context;
        this.recyclerView = recyclerView;
        setupScrollListener();
    }

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    playVisibleVideo();
                }
            }
        });
    }

    private void playVisibleVideo() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        int firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (firstVisibleItem == RecyclerView.NO_POSITION) return;

        playVideoAt(firstVisibleItem);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_home_screen, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video videoItem = videoItems.get(position);

        if (holder.player != null) {
            holder.player.release();
        }

        holder.player = new ExoPlayer.Builder(context).build();
        holder.playerView.setPlayer(holder.player);
        holder.playerView.setUseController(false);

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(videoItem.getVideoUri()))
                .setMimeType("video/mp4")
                .build();

        holder.player.setMediaItem(mediaItem);
        holder.player.prepare();

        holder.player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    holder.player.seekTo(0);
                    holder.player.play();
                }
            }
        });

        holder.playerView.setOnClickListener(v -> {
            if (holder.player.isPlaying()) {
                holder.player.pause();
            } else {
                holder.player.play();
            }
        });

        // Dừng video trước đó nếu có
        if (currentPlayingPosition == position) {
            holder.player.play();
            currentPlayer = holder.player;
        } else {
            holder.player.pause();
        }
    }


    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    public void playVideoAt(int position) {
        if (currentPlayingPosition == position) return;

        Log.d("VideoAdapter", "Switched video from position " + currentPlayingPosition + " to " + position);

        // Dừng video hiện tại nếu có
        if (currentPlayer != null) {
            currentPlayer.pause();
        }

        notifyItemChanged(currentPlayingPosition);
        currentPlayingPosition = position;
        notifyItemChanged(position);
    }


    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ExoPlayer player;

        public VideoViewHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.video);
        }
    }

}
