package com.example.tiktok;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoItems;
    private Context context;
    private Map<Integer, ExoPlayer> playerMap = new HashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    public VideoAdapter(List<Video> videoItems, Context context) {
        this.videoItems = videoItems;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_home_screen, parent, false);
        processClick(view);
        return new VideoViewHolder(view);
    }

    private void processClick(View view) {
        RelativeLayout heart = view.findViewById(R.id.heart);
        heart.setOnClickListener(v -> showToast(view, "Click heart"));

        RelativeLayout comment = view.findViewById(R.id.comment);
        comment.setOnClickListener(v -> showToast(view, "Click comment"));

        RelativeLayout share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> showToast(view, "Click share"));

        RelativeLayout download = view.findViewById(R.id.download);
        download.setOnClickListener(v -> showToast(view, "Click download"));
    }

    private void showToast(View view, String message) {
        Log.d("ItemVideoHomeScreen", message);
        Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        if (position < 0 || position >= videoItems.size()) return; // Tránh truy cập vị trí không hợp lệ

        Video videoItem = videoItems.get(position);
        ExoPlayer player = playerMap.get(position);

        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            playerMap.put(position, player);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoItem.getVideoUri()));
            player.setMediaItem(mediaItem);
            player.prepare();
        }
        holder.playerView.setUseController(true);
        holder.playerView.setControllerShowTimeoutMs(0);
        ExoPlayer finalPlayer = player;
        holder.itemView.post(() -> {
            if (holder.getAdapterPosition() == position) { // Đảm bảo đúng ViewHolder
                holder.playerView.setPlayer(finalPlayer);
                finalPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                finalPlayer.play();

                TextView username = holder.itemView.findViewById(R.id.username);
                username.setText(videoItem.getUsername());
            }
        });
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.getAdapterPosition();

        if (position != RecyclerView.NO_POSITION && playerMap.containsKey(position)) {
            ExoPlayer player = playerMap.get(position);
            if (player != null) {
                player.stop();
                player.release();
            }
            playerMap.remove(position);
        }

        holder.playerView.setPlayer(null);

        // Gọi cập nhật lại Adapter để ép RecyclerView load lại video ngay lập tức
        new android.os.Handler().postDelayed(() -> {
            notifyItemChanged(position);
        }, 50); // Delay ngắn để tránh conflict với RecyclerView
    }

    public void stopAllVideo(){
        for (ExoPlayer player : playerMap.values()) {
            if (player != null) player.stop();
        }
    }

    public void playVideoAt(int position) {
        stopAllVideo();
        if (playerMap.get(position) != null) {
            playerMap.get(position).play();
        }
        Log.d("VideoAdapter", "Playing video at position: " + position);
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.playerView.getPlayer() != null) {
            holder.playerView.getPlayer().pause();
        }
    }

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
