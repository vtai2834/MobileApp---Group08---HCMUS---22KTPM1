package com.example.tiktok;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoItems;
    private Context context;

    public VideoAdapter(List<Video> videoItems, Context context) {
        this.videoItems = videoItems;
        this.context = context; // Save context to use later for showing Toast/Dialogs
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

        // Set video URI
        holder.videoView.setVideoURI(Uri.parse(videoItem.getVideoUri()));
//        holder.videoView.start();

        // Set video to loop by setting a listener on the VideoView's MediaPlayer
        holder.videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);  // Set the video to loop
            holder.videoView.start(); // Start the video
        });

        // Set user info
        holder.username.setText(videoItem.getUsername());
        holder.likes.setText(videoItem.getLikes());
        holder.comments.setText(videoItem.getComments());
        holder.music.setText(videoItem.getMusic());
        holder.content.setText(videoItem.getTitle());

        // Handle comment button click
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CommentScreen.class);
                intent.putExtra("VIDEO_URI", videoItem.getVideoUri());
                view.getContext().startActivity(intent);
            }
        });

        // Handle share button click
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ShareScreen.class);
                intent.putExtra("VIDEO_URI", videoItem.getVideoUri());
                view.getContext().startActivity(intent);
            }
        });

        // Handle like button click
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle like action here
                Toast.makeText(view.getContext(), "Liked", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle download button click
        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDownloadDialog();
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        VideoView videoView;
        TextView username, likes, comments, music, content;
        RelativeLayout commentButton, shareButton, likeButton, downloadButton;

        public VideoViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.like);
            comments = itemView.findViewById(R.id.cmt_num);
            music = itemView.findViewById(R.id.music);
            content = itemView.findViewById(R.id.content);

            commentButton = itemView.findViewById(R.id.comment);
            shareButton = itemView.findViewById(R.id.share);
            likeButton = itemView.findViewById(R.id.heart);
            downloadButton = itemView.findViewById(R.id.download);
        }
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
}
