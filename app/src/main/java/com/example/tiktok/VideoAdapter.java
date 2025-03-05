package com.example.tiktok;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public int currentPositionPlayingVideo = -1;

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

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

    private void processClick(View view, int position_vid, Video videoItem) {
        RelativeLayout heart = view.findViewById(R.id.heart);
        heart.setOnClickListener(v -> {
            ImageView like_img = view.findViewById(R.id.like_img);
            like_img.setBackgroundColor(Color.parseColor("#FF0007"));
        });

        RelativeLayout comment = view.findViewById(R.id.comment);
        comment.setOnClickListener(v -> {
            stopVideoAtPosition(position_vid);
            Intent intent = new Intent(view.getContext(), CommentScreen.class);
            intent.putExtra("VIDEO_URI", videoItem.getVideoUri());
            view.getContext().startActivity(intent);
        });

        RelativeLayout share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> {
            stopVideoAtPosition(position_vid);
            Intent intent = new Intent(view.getContext(), ShareScreen.class);
            intent.putExtra("VIDEO_URI", videoItem.getVideoUri());
            view.getContext().startActivity(intent);
        });

        RelativeLayout download = view.findViewById(R.id.download);
        download.setOnClickListener(v -> {
            showDownloadDialog();
        });
    }

    // Method to show the download confirmation dialog
    private void showDownloadDialog() {
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context); // Use the context passed in constructor
        builder.setTitle("Tải xuống video này")
                .setMessage("Bạn có chắc chắn muốn tải video này xuống?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle "Đồng ý" button click (start download)
                        startDownload();
                    }
                })
                .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle "Hủy bỏ" button click (cancel download)
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    // Method to simulate video download
    private void startDownload() {
        // Simulate the video download process here
        // For now, you can just show a toast as a placeholder
        Toast.makeText(context, "Video đang được tải xuống...", Toast.LENGTH_SHORT).show();
    }

    public int getCurrentPositionVideo() {
        return currentPositionPlayingVideo;
    }

    public void stopVideoAtPosition(int position) {
        // Kiểm tra nếu player tại vị trí này tồn tại
        ExoPlayer player = playerMap.get(position);
        if (player != null) {
            player.pause();  // Dừng video tại vị trí đó
            player.stop();   // Dừng video
        }

        currentPositionPlayingVideo = -1;
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

                currentPositionPlayingVideo = holder.getAdapterPosition();

                TextView username = holder.itemView.findViewById(R.id.username);
                TextView like_cnt = holder.itemView.findViewById(R.id.like);
                TextView cmt_cnt = holder.itemView.findViewById(R.id.cmt_num);
                TextView music = holder.itemView.findViewById(R.id.music);
                TextView content = holder.itemView.findViewById(R.id.content);

                username.setText(videoItem.getUsername());
                like_cnt.setText(videoItem.getLikes());
                cmt_cnt.setText(videoItem.getComments());
                music.setText(videoItem.getMusic());
                content.setText(videoItem.getTitle());

                // Truyền position vào processClick
                processClick(holder.itemView, position, videoItem);
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
//        new android.os.Handler().postDelayed(() -> {
//            notifyItemChanged(position);
//        }, 50); // Delay ngắn để tránh conflict với RecyclerView
    }


    public void playVideoAt(int position) {
        for (ExoPlayer player : playerMap.values()) {
            if (player != null) player.pause();
        }
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
