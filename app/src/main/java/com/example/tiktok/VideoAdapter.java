package com.example.tiktok;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoItems;
    private Context context;
    private Map<Integer, ExoPlayer> playerMap = new HashMap<>();
    private int currentPlayingPosition = -1;

    public VideoAdapter(List<Video> videoItems, Context context) {
        this.videoItems = videoItems;
        this.context = context;
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

        // Kiểm tra nếu đã có ExoPlayer cho vị trí này, thì dùng lại
        ExoPlayer player = playerMap.get(position);
        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            playerMap.put(position, player);
        }

        holder.playerView.setPlayer(player);

        // Tránh load lại nếu video đã thiết lập trước đó
        if (player.getMediaItemCount() == 0) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoItem.getVideoUri()));
            player.setMediaItem(mediaItem);
            player.prepare();
        }

        // Tự động lặp lại video
        player.setRepeatMode(Player.REPEAT_MODE_ONE);

        // Hiển thị vĩnh viễn thanh điều khiển
        holder.playerView.setControllerShowTimeoutMs(0);
    }

    public void playVideoAt(int position) {
        for (int i = 0; i < playerMap.size(); i++)
            if(playerMap.get(i) != null)
                playerMap.get(i).pause();
        if (playerMap.get(position) != null)
            playerMap.get(position).play();

        Log.d(
                "VideoAdapter",
                "Playing video at position: " + position
        );
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    /**
     * Giữ nguyên player khi ViewHolder bị tái sử dụng, tránh reset video.
     */
    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.playerView.getPlayer() != null) {
            holder.playerView.getPlayer().pause();
        }
    }

    /**
     * Giải phóng bộ nhớ khi Adapter bị hủy
     */
    public void releasePlayers() {
        for (ExoPlayer player : playerMap.values()) {
            player.release();
        }
        playerMap.clear();
    }


    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.video);
        }
    }
}
