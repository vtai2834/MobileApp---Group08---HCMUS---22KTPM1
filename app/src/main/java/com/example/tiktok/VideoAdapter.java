package com.example.tiktok;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoItems;
    private List<String> videoIds;
    private Context context;
    private Map<Integer, ExoPlayer> playerMap = new HashMap<>();

    private String userID = "";


    public int currentPositionPlayingVideo = -1;

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    public VideoAdapter(List<Video> videoItems, List<String> videoIds, Context context, String userID) {
        this.videoItems = videoItems;
        this.videoIds = videoIds;
        this.context = context;
        this.userID = userID;
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
            String userId = userID;
            String videoId = videoIds.get(position_vid);
            ImageView like_img = view.findViewById(R.id.like_img);
            DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes").child(videoId).child(userId);

            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User has already liked, so remove the like
                        likeRef.removeValue();
                        like_img.clearColorFilter(); // Reset color
                    } else {
                        // User has not liked, so add the like
                        likeRef.setValue(true);
                        like_img.setColorFilter(Color.parseColor("#FF0007")); // Change to red
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.d(
                            "Firebase",
                            "Error: " + error.getMessage()
                    );
                }
            });
        });


        RelativeLayout comment = view.findViewById(R.id.comment);
        comment.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            CommentBottomSheet commentBottomSheet = new CommentBottomSheet(videoIds.get(position_vid), userID);
            commentBottomSheet.show(fragmentManager, commentBottomSheet.getTag());
            //            stopVideoAtPosition(position_vid);
//            Intent intent = new Intent(view.getContext(), CommentScreen.class);
//            intent.putExtra("VIDEO_URI", videoItem.getVideoUri());
//            view.getContext().startActivity(intent);
        });

        RelativeLayout share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> {
//            stopVideoAtPosition(position_vid);

//            shareVideo(view, videoItem.getVideoUri());
            ShareDialog dialog = new ShareDialog(context, videoItem.getVideoUri(), url -> {
                showReportDialog(videoItem.getVideoUri());
            });
            dialog.show();

        });

        RelativeLayout download = view.findViewById(R.id.download);
        download.setOnClickListener(v -> {
            showDownloadDialog(videoItem);
        });
    }

    public void showReportDialog(String videoUrl) {
        String[] reasons = {
                "Bạo lực, làm dụng và bóc lột dạng phạm tội",
                "Thủ ghét và quấy rối",
                "Tự tử và tự làm hại bản thân",
                "Cách ăn uống không lành mạnh và hình ảnh cơ thể ốm yếu",
                "Hoạt động và thử thách nguy hiểm",
                "Hình ảnh khoả thân hoặc nội dung tình dục",
                "Nội dung gây sốc và phản cảm",
                "Thông tin sai lệch",
                "Hành vi lừa đảo và gửi nội dung thu rác",
                "Hàng hóa và hoạt động được kiểm soát",
                "Gian lận và lừa đảo",
                "Chia sẻ thông tin cá nhân",
                "Sản phẩm nhái và vi phạm quyền sở hữu trí tuệ",
                "Nội dung định hướng thương hiệu không được chi tiết lộ",
                "Khác"
        };

        // Đảm bảo context là một Activity
        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.report_dialog_layout, null);

            // Tùy chỉnh phần tiêu đề và phần tử
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            dialogTitle.setText("Chọn lý do của bạn");

            // Lấy ListView và set các lựa chọn
            ListView listView = dialogView.findViewById(R.id.dialogListView);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, reasons);
            listView.setAdapter(adapter);

            // Tạo AlertDialog và hiển thị
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView); // Gán view tùy chỉnh cho dialog
            builder.setCancelable(true);  // Cho phép đóng dialog khi nhấn ra ngoài

            // Tạo AlertDialog
            AlertDialog alertDialog = builder.create();

            // Xử lý sự kiện khi người dùng chọn một mục trong ListView
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedReason = reasons[position];
                Toast.makeText(context, "Lý do bạn chọn: " + selectedReason, Toast.LENGTH_SHORT).show();


                alertDialog.dismiss();
            });

            // Lấy ImageButton (nút đóng) và thêm sự kiện đóng dialog
            ImageButton closeButton = dialogView.findViewById(R.id.closeButton_report);
            closeButton.setOnClickListener(v1 -> {
                // Đảm bảo rằng dialog được đóng khi người dùng nhấn vào nút "X"
                alertDialog.dismiss();
            });

            // Hiển thị dialog
            alertDialog.show();
        }
    }

    public void shareVideo(View view, String videoUrl) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ video từ ứng dụng");

            view.getContext().startActivity(Intent.createChooser(shareIntent, "Chia sẻ video qua"));
        } catch (Exception e) {
            Log.e("VideoAdapter", "Error sharing video: " + e.getMessage());
            Toast.makeText(context, "Lỗi khi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show the download confirmation dialog
    private void showDownloadDialog(Video videoItem) {
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context); // Use the context passed in constructor
        AlertDialog dialog = builder.setTitle("Tải xuống video này")
                .setMessage("Bạn có chắc chắn muốn tải video này xuống?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle "Đồng ý" button click (start download)
//                        startDownload();
                        AndroidDownloader downloader = new AndroidDownloader(context);
                        downloader.downloadFile(videoItem.getVideoUri());
                    }
                })
                .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle "Hủy bỏ" button click (cancel download)
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
        // After showing the dialog, you can modify button colors
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);


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
                ImageView like_img = holder.itemView.findViewById(R.id.like_img);

                if (videoItem.getUsername().substring(0, 3).equals("-OL")) {
                    // This is a Firebase user ID, fetch the actual username from Firebase
                    String userId = videoItem.getUsername(); // The full ID like "-OLm4Zc1z1YSUyV7zHDm"
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Get the idName field from the user data
                                String idName = snapshot.child("idName").getValue(String.class);
                                if (idName != null && !idName.isEmpty()) {
                                    username.setText(idName);
                                } else {
                                    // Fallback in case idName is not available
                                    username.setText(videoItem.getUsername());
                                }
                            } else {
                                // User not found in database, use original username
                                username.setText(videoItem.getUsername());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("Firebase", "Error fetching user data: " + error.getMessage());
                            username.setText(videoItem.getUsername());
                        }
                    });
                } else {
                    username.setText(videoItem.getUsername());
                }

                like_cnt.setText(videoItem.getLikes());
                cmt_cnt.setText(videoItem.getComments());
                music.setText(videoItem.getMusic());
                content.setText(videoItem.getTitle());

                Log.d(
                        "Firebase",
                        "Video: " + videoItem.getTitle() + "Id: " + videoIds.get(position) + "Position: " + position + ""
                );

                String userId = "-OL4poKAL6huFI4pgHbb";
                DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes").child(videoIds.get(position)).child(userId);
                likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            like_img.setColorFilter(Color.parseColor("#FF0007")); // Change to red
                        } else {
                            like_img.clearColorFilter(); // Reset color
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.d(
                                "Firebase",
                                "Error: " + error.getMessage()
                        );
                    }
                });



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

//         Gọi cập nhật lại Adapter để ép RecyclerView load lại video ngay lập tức
        new android.os.Handler().postDelayed(() -> {
            notifyItemChanged(position);
        }, 50); // Delay ngắn để tránh conflict với RecyclerView
    }

    public void stopAllVideo(){
        for (ExoPlayer player : playerMap.values()) {
            if (player != null)
            {
                player.pause();
                player.stop();
            }
        }
    }

    public void playVideoAt(int position) {
        stopAllVideo();
        if (playerMap.get(position) != null) {
            playerMap.get(position).prepare();
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
