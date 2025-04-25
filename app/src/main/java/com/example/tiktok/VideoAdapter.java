package com.example.tiktok;

import static androidx.core.content.ContextCompat.startActivities;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoItems;
    private List<String> videoIds;
    private Context context;
    private Map<Integer, ExoPlayer> playerMap = new HashMap<>();

    private String userID = "";
    private NotificationManager notificationManager;

    public int currentPositionPlayingVideo = -1;

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    public VideoAdapter(List<Video> videoItems, List<String> videoIds, Context context, String userID) {
        this.videoItems = videoItems;
        this.videoIds = videoIds;
        this.context = context;
        this.userID = userID;
        this.notificationManager = NotificationManager.getInstance();
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
            TextView like_cnt = view.findViewById(R.id.like);
            DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes").child(videoId).child(userId);
            DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoId);
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User has already liked, so remove the like
                        likeRef.removeValue();
                        like_img.clearColorFilter(); // Reset color
                        videoRef.child("likes").setValue(String.valueOf(Integer.parseInt(videoItem.getLikes()) - 1));
                        like_cnt.setText(String.valueOf(Integer.parseInt(like_cnt.getText().toString()) - 1));
                        videoItem.setLikes(String.valueOf(Integer.parseInt(like_cnt.getText().toString())));
                    } else {
                        // User has not liked, so add the like
                        likeRef.setValue(true);
                        like_img.setColorFilter(Color.parseColor("#FF0007")); // Change to red
                        videoRef.child("likes").setValue(String.valueOf(Integer.parseInt(videoItem.getLikes()) + 1));
                        like_cnt.setText(String.valueOf(Integer.parseInt(like_cnt.getText().toString()) + 1));
                        videoItem.setLikes(String.valueOf(Integer.parseInt(like_cnt.getText().toString())));

                        // Create notification for like
                        if (!videoItem.getUsername().equals(userId)) {
                            notificationManager.createLikeNotification(
                                    userId,
                                    videoId,
                                    videoItem.getUsername()
                            );
                        }
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
            CommentBottomSheet commentBottomSheet = new CommentBottomSheet(videoIds.get(position_vid), userID, videoItem);
            commentBottomSheet.show(fragmentManager, commentBottomSheet.getTag());
        });

        RelativeLayout share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> {
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
        ImageView plusImageView = holder.itemView.findViewById(R.id.plus);
        ImageView playPauseOverlay = holder.itemView.findViewById(R.id.play_pause_overlay);

        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            playerMap.put(position, player);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoItem.getVideoUri()));
            player.setMediaItem(mediaItem);
            player.prepare();
        }
        //Loading bar
        holder.playerView.setUseController(true);
        holder.playerView.setControllerShowTimeoutMs(0);
        playPauseOverlay.setVisibility(View.GONE);

        ExoPlayer finalPlayer = player;

        holder.playerView.setOnClickListener(v -> {
            if (finalPlayer.isPlaying()) {
                finalPlayer.pause();
                playPauseOverlay.setVisibility(View.VISIBLE);
            } else {
                finalPlayer.play();
                playPauseOverlay.setVisibility(View.GONE);
            }
            holder.playerView.showController();

        });

        holder.itemView.post(() -> {
            if (holder.getAdapterPosition() == position) { // Đảm bảo đúng ViewHolder
                holder.playerView.setPlayer(finalPlayer);
                finalPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                holder.playerView.showController();
                finalPlayer.play();

                currentPositionPlayingVideo = holder.getAdapterPosition();

                TextView usernameView = holder.itemView.findViewById(R.id.username);
                TextView like_cnt = holder.itemView.findViewById(R.id.like);
                TextView cmt_cnt = holder.itemView.findViewById(R.id.cmt_num);
                TextView music = holder.itemView.findViewById(R.id.music);
                TextView content = holder.itemView.findViewById(R.id.content);
                ImageView like_img = holder.itemView.findViewById(R.id.like_img);
                CircleImageView avt = holder.itemView.findViewById(R.id.avt);

                // Lấy thông tin người dùng từ Firebase bằng cách kiểm tra nếu username là user ID
                String videoUsername = videoItem.getUsername();  // Tên người dùng từ videoItem
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

                if (videoUsername.substring(0, 3).equals("-OL")) {
                    // Đây là Firebase user ID, truy xuất idName từ Firebase của người đăng video
                    userRef.child(videoUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String videoOwnerIdName = snapshot.child("idName").getValue(String.class);
                                if (videoOwnerIdName != null && !videoOwnerIdName.isEmpty()) {
                                    // Hiển thị idName của người đăng video
                                    usernameView.setText(videoOwnerIdName);
                                    Toast.makeText(context, "Video Owner idName: " + videoOwnerIdName, Toast.LENGTH_SHORT).show();
                                } else {
                                    usernameView.setText(videoItem.getUsername());
                                }
                            } else {
                                usernameView.setText(videoItem.getUsername());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("Firebase", "Error fetching user data: " + error.getMessage());
                            usernameView.setText(videoItem.getUsername());
                        }
                    });
                } else {
                    // Nếu không phải user ID Firebase, tìm user có idName = videoUsername
                    userRef.orderByChild("idName").equalTo(videoUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Lặp qua kết quả (thường chỉ có 1 kết quả)
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String user_avt = snapshot.child("avatar").getValue(String.class);

                                    // Load profile image
                                    if (user_avt != null && !user_avt.isEmpty()) {
                                        Glide.with(content)
                                                .load(user_avt)
                                                .placeholder(R.drawable.default_profile)
                                                .error(R.drawable.default_profile)
                                                .into(avt);
                                    } else {
                                        avt.setImageResource(R.drawable.default_profile);
                                    }

                                    // Đã tìm thấy user, thoát khỏi vòng lặp
                                    break;
                                }

                                // Hiển thị username
                                usernameView.setText(videoUsername);
                            } else {
                                // Không tìm thấy user với idName = videoUsername
                                Log.d("Firebase", "No user found with idName: " + videoUsername);
                                usernameView.setText(videoUsername);
                                avt.setImageResource(R.drawable.default_profile);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("Firebase", "Error fetching user data: " + error.getMessage());
                            usernameView.setText(videoUsername);
                            avt.setImageResource(R.drawable.default_profile);
                        }
                    });
                }

                like_cnt.setText(videoItem.getLikes());
                cmt_cnt.setText(videoItem.getComments());
                music.setText(videoItem.getMusic());
                content.setText(videoItem.getTitle());

                // Lấy userKey từ SharedPreferences (đã lưu từ LogIn.java)
                SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String userKey = sharedPreferences.getString("userKey", null);

                if (userKey != null) {
                    userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String currentUserIdName = snapshot.child("idName").getValue(String.class);
                                if (currentUserIdName != null && !currentUserIdName.isEmpty()) {
                                    String videoUsername = videoItem.getUsername();
//                                    Toast.makeText(context,
//                                            "currentUserIdName: " + currentUserIdName + "\nvideoUsername: " + videoUsername,
//                                            Toast.LENGTH_SHORT).show();

                                    if (videoUsername.startsWith("-OL")) {

                                        // Nếu là user ID, lấy idName của người đăng video
                                        userRef.child(videoUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot ownerSnapshot) {
                                                if (ownerSnapshot.exists()) {
                                                    String videoOwnerIdName = ownerSnapshot.child("idName").getValue(String.class);

                                                    if (videoOwnerIdName != null) {
                                                        // Kiểm tra xem người dùng hiện tại có phải là người đăng video không
                                                        if (currentUserIdName.equals(videoOwnerIdName)) {
                                                            // Nếu người đăng video là chính mình, không hiển thị icon
                                                            plusImageView.setVisibility(View.INVISIBLE);  // Ẩn icon
                                                        } else {
                                                            // Nếu không phải, kiểm tra trạng thái follow
                                                            checkFollowStatusAndUpdateIcon(currentUserIdName, videoOwnerIdName, plusImageView);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) { }
                                        });
                                    } else {
                                        // Nếu video username không phải là Firebase userId mà đã là idName
                                        if (currentUserIdName.equals(videoUsername)) {
                                            // Nếu người đăng video là chính mình, không hiển thị icon
                                            plusImageView.setVisibility(View.INVISIBLE);  // Ẩn icon
                                        } else {
                                            // Nếu không phải, kiểm tra trạng thái follow
                                            checkFollowStatusAndUpdateIcon(currentUserIdName, videoUsername, plusImageView);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }

                if (userKey != null) {
                    // Lấy thông tin "like" của người dùng từ Firebase
                    DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("likes")
                            .child(videoIds.get(position)).child(userKey);
                    likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                like_img.setColorFilter(Color.parseColor("#FF0007")); // Đổi màu thành đỏ
                            } else {
                                like_img.clearColorFilter(); // Reset màu
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d("Firebase", "Error: " + error.getMessage());
                        }
                    });
                }

                // Lấy idName của người dùng hiện tại (người xem)
                if (userKey != null) {
                    userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String currentUserIdName = snapshot.child("idName").getValue(String.class);
//                                if (currentUserIdName != null && !currentUserIdName.isEmpty()) {
//                                    Toast.makeText(context, "Your idName: " + currentUserIdName, Toast.LENGTH_SHORT).show();
//                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi khi lấy idName của bạn", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Thiết lập sự kiện nhấn vào ImageView để thông báo userKey
                plusImageView.setOnClickListener(v -> {
                    userRef.orderByChild("account").equalTo(videoItem.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String videoOwnerUserKey = userSnapshot.getKey();  // userKey của người đăng video
                                    String videoOwnerIdName = userSnapshot.child("idName").getValue(String.class);

                                    if (videoOwnerIdName != null && !videoOwnerIdName.isEmpty()) {
                                        String currentUserKey = sharedPreferences.getString("userKey", null);

                                        if (currentUserKey != null) {
                                            userRef.child(currentUserKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        String currentUserIdName = snapshot.child("idName").getValue(String.class);

                                                        if (currentUserIdName != null && !currentUserIdName.isEmpty()) {
                                                            // ======= Kiểm tra nếu người dùng không thể follow chính mình =======
                                                            if (currentUserIdName.equals(videoOwnerIdName)) {
                                                                Toast.makeText(context, "Bạn không thể follow chính mình", Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }

                                                            // ======= Kiểm tra xem đã follow người đó chưa =======
                                                            DatabaseReference followerRef = userRef.child(videoOwnerUserKey).child("follower");
                                                            followerRef.child(currentUserIdName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot followerSnapshot) {
                                                                    if (followerSnapshot.exists()) {
                                                                        // Đã follow rồi, thực hiện unfollow
                                                                        followerRef.child(currentUserIdName).removeValue().addOnCompleteListener(task -> {
                                                                            if (task.isSuccessful()) {
                                                                                // Giảm followerCount của người đăng video
                                                                                DatabaseReference followerCountRef = userRef.child(videoOwnerUserKey).child("followerCount");
                                                                                followerCountRef.setValue(ServerValue.increment(-1));

                                                                                // Xóa người đăng video khỏi list following của người dùng hiện tại
                                                                                DatabaseReference followingRef = userRef.child(currentUserKey).child("following");
                                                                                followingRef.child(videoOwnerIdName).removeValue().addOnCompleteListener(task1 -> {
                                                                                    if (task1.isSuccessful()) {
                                                                                        // Giảm followingCount của người dùng hiện tại
                                                                                        DatabaseReference followingCountRef = userRef.child(currentUserKey).child("followingCount");
                                                                                        followingCountRef.setValue(ServerValue.increment(-1));

                                                                                        // Cập nhật lại icon và thông báo
                                                                                        plusImageView.setImageResource(R.drawable.plus_icon); // Đổi icon thành plus
                                                                                        Toast.makeText(context, "Đã hủy follow", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    } else {
                                                                        // Chưa follow, thực hiện follow
                                                                        followerRef.child(currentUserIdName).setValue(true).addOnCompleteListener(task -> {
                                                                            if (task.isSuccessful()) {
                                                                                // Tăng followerCount của người đăng video
                                                                                DatabaseReference followerCountRef = userRef.child(videoOwnerUserKey).child("followerCount");
                                                                                followerCountRef.setValue(ServerValue.increment(1));

                                                                                // Thêm người đăng video vào list following của người dùng hiện tại
                                                                                DatabaseReference followingRef = userRef.child(currentUserKey).child("following");
                                                                                followingRef.child(videoOwnerIdName).setValue(true).addOnCompleteListener(task1 -> {
                                                                                    if (task1.isSuccessful()) {
                                                                                        // Tăng followingCount của người dùng hiện tại
                                                                                        DatabaseReference followingCountRef = userRef.child(currentUserKey).child("followingCount");
                                                                                        followingCountRef.setValue(ServerValue.increment(1));

                                                                                        // Cập nhật lại icon và thông báo
                                                                                        plusImageView.setImageResource(R.drawable.minus_icon); // Đổi icon thành minus
                                                                                        Toast.makeText(context, "Follow thành công", Toast.LENGTH_SHORT).show();

                                                                                        notificationManager.createFollowNotification(
                                                                                                currentUserKey,
                                                                                                videoOwnerUserKey
                                                                                        );
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Toast.makeText(context, "Lỗi khi kiểm tra trạng thái follow", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(context, "Lỗi khi lấy idName người dùng hiện tại", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Không tìm thấy người đăng video", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi khi truy vấn người đăng video", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                avt.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ProfileScreen.class);
                    intent.putExtra("viewUsername", videoUsername);
                    intent.putExtra("VIDEOS_ARRAY_JSON", new ArrayList<>(videoIds));
                    context.startActivity(intent);
                });

                // Truyền position vào processClick
                processClick(holder.itemView, position, videoItem);
            }
        });
    }

    private void checkFollowStatusAndUpdateIcon(String currentUserIdName, String videoOwnerIdName, ImageView plusImageView) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("idName").equalTo(videoOwnerIdName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String videoOwnerUserKey = userSnapshot.getKey();

                        usersRef.child(videoOwnerUserKey).child("follower").child(currentUserIdName)
                                .addListenerForSingleValueEvent(new ValueEventListener() {


                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot followerSnapshot) {
                                        if (plusImageView.getVisibility() != View.VISIBLE) {
                                            plusImageView.setVisibility(View.VISIBLE); // Đảm bảo plusImageView hiển thị
                                        }
                                        if (followerSnapshot.exists()) {
                                            plusImageView.setImageResource(R.drawable.minus_icon); // Đã follow -> icon minus
                                        } else {
                                            plusImageView.setImageResource(R.drawable.plus_icon); // Chưa follow -> icon plus
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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

