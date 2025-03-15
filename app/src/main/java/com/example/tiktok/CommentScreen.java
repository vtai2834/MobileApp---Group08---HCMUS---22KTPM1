package com.example.tiktok;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentScreen extends AppCompatActivity {
    // Model Comment
    public static class Comment {
        private String username;
        private String avatarUrl;
        private String commentText;
        private String timestamp;

        // 🔹 Constructor mặc định (bắt buộc để Firebase có thể deserialize)
        public Comment() {}

        // 🔹 Constructor đầy đủ
        public Comment(String username, String avatarUrl, String commentText, String timestamp) {
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.commentText = commentText;
            this.timestamp = timestamp;
        }

        // 🔹 Getters
        public String getUsername() { return username; }
        public String getAvatarUrl() { return avatarUrl; }
        public String getCommentText() { return commentText; }
        public String getTimestamp() { return timestamp; }
    }


    // Adapter Comment
    public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
        private List<Comment> commentList;

        public CommentAdapter(List<Comment> commentList) {
            this.commentList = commentList;
        }

        // ViewHolder
        public class CommentViewHolder extends RecyclerView.ViewHolder {
            CircleImageView ivProfile;
            TextView tvUsername;
            TextView tvComment;
            TextView tvTimestamp;

            public CommentViewHolder(View itemView) {
                super(itemView);
                ivProfile = itemView.findViewById(R.id.iv_profile);
                tvUsername = itemView.findViewById(R.id.tv_username);
                tvComment = itemView.findViewById(R.id.tv_comment);
                tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            }
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = commentList.get(position);

            // Sử dụng Glide để load ảnh đại diện
            Glide.with(holder.itemView.getContext())
                    .load(comment.getAvatarUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.ivProfile);

            holder.tvUsername.setText(comment.getUsername());
            holder.tvComment.setText(comment.getCommentText());
            holder.tvTimestamp.setText(comment.getTimestamp());
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }
    }

    // Khai báo các view và adapter
    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private ImageButton buttonSendComment;
    private TextView textCommentCount;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private ImageButton btn_close;

    VideoView mainVideo;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_screen);

        //lấy dữ liệu từ intent của homeScreen:
        String videoUriString = getIntent().getStringExtra("VIDEO_URI");
        if (videoUriString != null) {
            Uri videoUri = Uri.parse(videoUriString);

            mainVideo = findViewById(R.id.video_cmt_screen);


            // Thiết lập video
            mainVideo.setVideoURI(videoUri);
            mainVideo.start();
        }

        // Ánh xạ các view từ layout
        recyclerViewComments = findViewById(R.id.rv_comments);
        editTextComment = findViewById(R.id.et_comment_input);
        buttonSendComment = findViewById(R.id.btn_send_comment);
        textCommentCount = findViewById(R.id.tv_comment_count);

        // Khởi tạo danh sách bình luận mẫu
        commentList = new ArrayList<>();
        commentList.add(new Comment("User1", "data:image/jpeg;base64,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", "Bình luận đầu tiên", "2 giờ trước"));
        commentList.add(new Comment("User2", "data:image/jpeg;base64,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", "Bình luận thứ hai", "1 giờ trước"));

        String videoID = "-OL9wbuLI-SBUrDbt1RD";
        String userID = "-OL4kcCENjXWFelvZmvE";
        DatabaseReference cmtRef = FirebaseDatabase.getInstance().getReference("comments").child(videoID).child(userID);

//        commentList.add(cmtRef.child(videoID).child(userID).getValue(Comment.class));
        cmtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear(); // Xóa danh sách cũ để tránh bị lặp lại khi load lại

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                        Log.d("Firebase", "Comment: " + comment.getCommentText() + " | Total: " + commentList.size());
                    }
                }

                // Cập nhật RecyclerView sau khi dữ liệu đã tải xong
                commentAdapter.notifyDataSetChanged();

                // Cập nhật số lượng bình luận (phải làm sau khi danh sách được cập nhật)
                textCommentCount.setText(String.format("%d Comments", commentList.size()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });



        // Cập nhật số lượng bình luận
        textCommentCount.setText(String.format("%d Comments", commentList.size()));

        // Thiết lập RecyclerView
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        // Xử lý sự kiện gửi bình luận
        buttonSendComment.setOnClickListener(v -> {
            String commentText = editTextComment.getText().toString().trim();

            if (!commentText.isEmpty()) {
                // Tạo bình luận mới
                Comment newComment = new Comment(
                        "Bạn",
                        "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxISEhUSEBIVFRUXFRUVFRUXFRUVFRUXFhUWFxUVFRUYHSggGBolHRUVITEhJSkrLi4uFx8zODMsNygtLisBCgoKDg0OGhAQFy0dHR0tLS0tLS0tLSsrLS0tLS0tKy0tLSsrLSstLS0tLSstLS0tKystLS0tLSs3Ny0rLS0rK//AABEIAOAA4QMBIgACEQEDEQH/xAAbAAABBQEBAAAAAAAAAAAAAAAFAAIDBAYBB//EADsQAAEDAgQDBgUDAgUFAQAAAAEAAhEDIQQFEjFBUXEGImGBkaETMrHB0UJS8CPxB3KCouEUJGKSshX/xAAZAQACAwEAAAAAAAAAAAAAAAACAwABBAX/xAAgEQEBAAIDAAIDAQAAAAAAAAAAAQIRAyExEkEEIjJR/9oADAMBAAIRAxEAPwA4HBPAQJmOINwiuExGpJyxsNxyl8WQEoTwEoSxmLqfC45RDChub5rTotl5jgBzPgrGZ4xtGm6o/Zon8AeK8mzzNHV3lzjvsOAHJFjNhyuhzNu2jnt00mlt/mm8TwHigGIzes8Qajo48Cep3Q5xU1dmkCOSdqQq1GSn08U9plrnN8yowVx11YR7L+0hBDa237h9x91p6VQOAIMg8eBXmhKKZLnLqB0nvUzuP2+LfwimQbG6BXQoKNcOAc0yDsVKCmF2JAV2VGCngqIdKcmBOUR2UgVyUlEOlKU0JSqFD5XJTUiqWdKWpMlKVE2k1JKOVxRBTOcKAA9trwfylkh+6m7SP00vP6AqDs8Zg85Wfe8GjWsx4BKE8JELPs2QyFyE+FWx1YMaXGwAJJ5ACT7Aq5V1gf8AEPNtThQYbNu/rwHlv5+Cw1RXMfXL3ue7dxLj57D0VJ60YzTPlTWSjH/QvLRIsociwnxKoH85LeYllKkwNqOAsLceUwEvPksuobx8Us3WCr5e5t9xwKoPYQtrUqUCILx6H8IDjsICZYQR5BFhnb7A54SeUFmd9+B/KYrz8vdw0/8As0fUqpVpkGHCD/ITCbBjs5mppuFNx7rjbwK2LXLzNbTs/mHxGAO+YW9PomY0Fg0CnBRgp7UQUgKcmLqijklyV2VaECuFdSVLjkpJFcUW7K4kuKkOSXElaCfbR8U2DmT9vyVzssO61Qdt395jfD7n8K12ZbAb0WLesGyf00UJQnAJJJhhCzfbbE6MNU5uhg8z3vYFaUrDf4i1jFJk7lzvSB9z6osfVZePPcQblVd/orNYXKhptv5rUzNV2XoaagPgPv8Aha1mRsq4kPdDw5pcWEDS2HaQSZuLREblZHKcYGuaYJtw5rTY7PK9N9EUIPxKTXBhbN3VKnKDIsN+Cy5b+W42Y/H46oZnORxiqdOk0AvBJbHdYQTJuNo5crKy/s4wETUJ1OaS4iJAd3gxuoSHC0mY4Si2Fwdf4nx8Q4Gp8I02U2CGU2zxd4XnqbrHZ9XFTEPc0gtGljSNiGNDZHgSCfNHjlcuy8pjjOlF9IuJOq0nhw4SNkLzNux6joJmPVx9UapNJIaASSYAAkknYADdDMZT7xabGXiOREWPI/hNlIsByESyTEFr9P7hb/MLj7jzVB7YsuNdBBG4M+iOUFj0TDVZAVgFCMmxAc23WOU7j1DkVCcXpKCngqNqcFFHSuyuJKKdXElyVFnSuSuJKI7K4kkot1JJJUh3bWpNeOQA9v8AlGezzbDos72kfqxL/wDMR7wtPkTfosWf8t2P9DUJEJ4CRCSZpDUXm3bzETiAP2sA8yTK9IxGy8k7WVdWJqXnTDZ8Yk+5KZx+l5+AFS5XKLLg+MKUtulSbz4lPpM9aDA6WMvaOK1mX0hUr0KoMacO0gHclz6oFuQvJ6IDluGDwDuIb9AitfGuw+Iomm3V/R0aBu4GtU7ojjIELJe9tetaq32lfSxFH4NJz/isdIYQ5ofPzAzAvEzzHIrN4vCmk34VUBtRpD2uGkggwACRvsfSOu4zjJBWhzQWuieR6WssVUZUw9RzqxnSe6496Bw7p3IMRy3V8Oe5pOfj13IP5Plnwg7FYo6XaZ2+QERsP1uB0gcJ52GRzLDVcVVq4hlM6Q5wjciNIZ1MNEolic1NeGvdppi4a3Yk/qJMybm5+5UvY7MSzEfAMGnVdVb0cwFwcOokeiO7x3QY4zKyfTAY1vemInhxB8Qqq0PbPC/DrOtxP5/nRZ2Voxu5tlzmrpo+y9Q3HDh53+o91qGFZTIWQJ6fU/grT0nJ2JVWGp4TGp6JR0pSuBdUUSSSSiEkkkohJJLira3Ukl1RFDFu1Vyeb/vK2uSCyxNITV/1LdZKFh5fG7D0XaEiE4BKEg4Pxhhp8JXjWY1NVaof/N31M/zxXs+MYNDp249BcrxHFHS+pO+t5PWSn8RPIhY6QTzNvJKs4ANBtxPnsoW1Nv54qLEP1SnaJ23vZt0ANPDY8xwj+cFpMsFIYr4lRwBZQ7gNrurVg5w8QBH+orD/AOHWKw4qVG4qrpOlgo63kM3OsAkwD8nvC9Nbg2kTSquEX7j7bu6j9TvUrFyfrbK3YfvjNCd4E/zwQTtHlwrU3aQNQCoZjiq9MmDri0mx9QhuXdp3NqNbUY/ROkucCO6Yl1xw+yTjhfcabcvrKM3Twx1Op2bGwJNzNx90W7E4IOxLi6P6Je+Qban9wR5CorHa/Lix5e0CDvI2niquQ4ptGm9oN3XJ25+y13L5YdM8xmGfbP8AbnGipXdH7reUj7rMtF1azGtqqOPiVXp7rRhNSRj5LvK0dyyzo6fQ/krQ0XLNYU3nojuDenQqijCngplNPCINOSSAShWp1JdAXQFW1uJJ+lcIU2mjUk6EiFENSXYXFSKeXtmoPVbnJhZYrKx3/JbnKRZYeVu4/RMBdhdC7CScrV6c+/8AZeHdqKWnE1m8nx6AX89/Ne7VQvHu1uCL6tSs0fO52nhIbafb2TeKk8vjJbJr2w3zUr6JkyCOtlFXdPktLOiDJIABJJAAAkkmwAHEr1Psg19CrSp1qJaRhmsL3Aj4ZBJgHYyTcCT7rz/s3mowuIZXLNYbMjjBES08CJ/svWcn7RsxdKpVLNDKYDnOJBsdRdIAsRoMi/us/wCRctak6afxpjvdvabMsRT1NYJOvVBA1NlonSSLgwCRI4FB6+Eczvxqpz3mnYf8I7/+TQrgVKZF4Icx244XBuFdqaWDS4C42ix4bLDMteOhdVUxj2YimRzaZHkvLMRVIJA4SFrMTVNGs8T3DdvTksjmA758ST6lavx5pk/IrOOF05jbhT16KaOBWyMFEMEEWwr7oZghZX6JgpkUPUDZWAFSw1SRYbb+CsNejgEye1qja9TNeFVqSHNYuhic0p0qtj0bpXC1PJTHOUUaWppXS5ROcrUcko7811WEsqZc+S2mViyyeVN36rXZaLLncvrfxiLU6U1qeSAJKSaG5qSe4CLiSNrX3M7G/ost2kwh+GZHdaJNwAI2iGha+lTnvcXGfLgPIQs520qywNbGkOhxt3nN/QOYFp8bcCEzC9gznTyvMnE3AgE7cfNDKwiyP5xQa1s+Kzz1rjLkjCnoVHsuxzmzvpcWk9YUKI5dR+I9rY3Kuqx9bTsLmVQCS9zjP6iTPMGd1vMaz4jQ8clistyX4cFlj9Vq8nxLr03jxHKeIHl9FzOTVy3HU4tzHVAc7w2ps8Qsdjh3l6dnWFGkuC85zOlDk/gpPPFKjhQ6ZT6uRFwPwyAeR/KI5JQLyQ0SRv5/2R6nlTjf5fRNueqVjxzKMhhsprNsW+YIRzKckJAfWgA/K2Y1RuSRsP510GGyszLiXBveI4bjfmn4x5nmY8LeSZx53IrkwmKhWogwAIAmA2w84UFTD/t9Fac4BQPrBaNkaV2qRrlFUPHmmh6tS0Hpzaiq/EXQ9TS9rmtNLlCHLoKvQdnFybK4uKKdlJJJWi/lQ+q1eX7LM5WLDqtLhG2XM5PXQwXxUA3I9VUx2OZp0tOomx0y6B1bsp20G8RPW/smY5h0OI4CUswgx77RobEG41nwEWb1mem6Dds8MG4VgaAAx/DYD+5WgwjtTGnmAhPbIE4aoBvqB8hBP2RT0FeMZ1U1PgXi35Q17UUrs0lxO5/KGVgVtx8ZckULR9kcPNTUfL1QBjFsexGX1HnU0SB9yq5P5Fxf022EbYIgG7HiNlAxhFiCDyKuMFlzMnSxTloe2DxWI7R5cGzbxW2ogygnauhLC4clfHdZJyTeLDZJivhVmu4Huu6Hj5GD5LdU2ucYAJNz5DdebvaZuvVuzpBwzK5Jl1MT1Eh46SCtPJj9svHlrcT0mNawx+3veNp6hZmvW3J3KMY/Hd0gWMEW2g2jw3+qzzhJTuGahPNluow2VYo4aSn4ekr1KnCaUEZwI0gcLqgCiWbwZg3tPgJhCwmQGR66CmpIgpWFSBV2FTgqKrq4uritRSkuSkogxlQsFp8Jss1luw6LSYQ2XLz9dLBc1QFJhxMzx+iiDJVvDshDF53pVwfdPwuXy+IlQZszVTrNj9Jj/Uz/AIKu43DlwBaYc24P5VahUFUztLS144hw29pVgleM9osKKdRrZ3YHO8JJgdYAPms9iN/p0Wn7cSzFVwd9Zj/LHd/2lvssqATcrZj4Rl6lpjkJ4L2rsplHwKTGEd6AXdV5X2UwnxcTSbwDg4/6bj6L23CyHQeSVy5fRnFPtbfSDhDgD1ULstFoJHurlNqmhI+IryWVUqsAbA2AQbO6OqkeiNYwwChmMvTjwSs5qytPFenluLw1zbitb2Uxv/ZmnxZUcI8Hd4H1LvRB81w8EqHIa2mo5vBwnzbt7ErXP2jPlPjR7HG8c7qvpA3Tq7iXQutoRutOE1GXK9rOGbKZjMQKYjiphUDG3Qiu8ucSeKH7FCpUy5lVx5D6g/ZCwtRg8L/Tc3i5p+n/AAs1XbDiPEpmNBk5K4uSuowOyp2FQBTtUQ5cSSUUSSS4rQby47dFocK6yzWAdt0WgwZsuXl66OIxh7tHmpmuhVcIbK01t0KVI+qGi/OwQrHUyx3xWCJ+YHiPyr1amS8cgPdQ4gOe1zXWJmPDpzVwE8ec9sMm/wCqruqio1moNsRJJ0gEiD4LK4rs5WYQ2AZ2OponxAJlbLNMxpUTLzpdFm8DAHzfuOw8L9VSxGbPrMAoQHub8zSDABGq52G/jstkIvdLsRlLhXa7TAae8vRyf6seELA5RmL2v0scCC5rJ46iTqE8bQYW6yxji/U4WE38eXuk8k7OwvQsxPlME+A9SnSlwF7qri2yVSr07EeCv1ngC6HmoTcpfJOmviv0yGcU91mS/S8Hy9bfdbDN2brF43cjxTeK7hfNGodX7x08dik43Bcb8lRwlQaBxJ9lapYfWYufZbcfGO+nVKhcVcweXmNRbtfor2Cy8NEw0cyblNxObNYCxpkmxPJLuX+Dk/0qtZlNutxgD3WPxOJ+I9zoiST+Faz+o4loMxEx/OiC/Ego8A5rq6omPlSAphR7VO1VwVMwq0OlJIrgVo6kuriiCeAKP4C9gs7giijKlRveY9wgbDY9efmubW+NRSpFo3E8lDWzhrC1r2GXD9N45mLWVDB5yYnEgN4BwBI8xuD0VenWDqlSru0d1k27o3N9r80rK6MxxmV7F8TnGHaNXxALiWwdR8A0ieCzeY9smF0OAYwAkOJJdYcQBfbYLJdpO1T6uplJjWAWJ3cfOFi3VSbmSTzn7p/Hx2zdJzyxnUEs4zH41VzxIaT3Z3j7KLLMc6m6WgOkFpaRqDgYtHkEP1Ir2cpg4inIkapI6cFoI+3oHZPLnmq2pUaxrWNcWMbYBxEDuhoA+Yr0GkzSAOXuTc+6F5PRAbPjPqjDAs2V3TfIeFHVqQpSqOYut5hUrH1VxVWSU9zIpt6/lViZV2u3+l0ugynR+F1lGazJtysRmjIcVusyCyObUplThouadKeCr2DeU/WUcoV4MgrNYL545rUYLLnOaXSABxJt0kwt0vTFZ2I5pji1jQ39QQ7LKHxKgHDcqvmOIMhrhccZseiuYHGNw7C83e4d1vLe5/nBBpe1PtO+axAFgAB9/dZ6ubq7ja5cS925Q6s+Sjgalp1YVunUlDQrFJ6ZA2CLApwFVoVArIKKAdSSSUQl1chJWgjgwjGEQbCIzhFza3RLjsMXhumLGYNpMHj1KjZTcKZbI1bTwmd+iuh0BV2PAZJScz8HlGZ0X0Xva5tnOJmeEmIQZ9ytf2qc6o4mAGjYWk+JjbfZZam0T6+wW3jy3ix8mOskARPJq2io13I/hVzhd+g9/wCBPw7bjp7j+yMt7l2bqCpT1A2OkhGw08x6H8rzz/DnMi1jmOdI12ng3S2PutvVxrONVjRx7wB9ys2U1TvYvOdZUMYdTTY+n3UzqzXMBa4EGLzuFE+sOCpeM0oxCJESyP8AxP0QvEEyiGCdLL+P0UynQpe2fx9wszjmzK0+JFiFnccN0nC6p+XcZSs7Q/oZU2IzKpViTDWmWsBsDzjifEpmZDvKvQYtsvTHZ2OYev8AEpEkQWuAHK4JMegXHNfpLi0m8c1Uy7EANfTJAOtrrkCRpI3PIgeqNYfGUog1GnmGnX/8yEzEvJn6taVWJV/H6DUdonSdpABmL2H8uh5EIlbOCeCo2lSNCiLWGeiLHIZSICu03ooGrMpSmArqIJySakptBXChF8MhWERbDrm1uixVPdPSPWy58LU2EzEO7vmPqlTqwk8jRxzpmO1WEa2n4kgDlz9fwvPqvcfby6Hf+eK9I7WsJovcTJAhoHCYE9eHqvOnNc6xE8RzWrhv6s/POzmYmT1t5Bd1RHPfoqdGzrq5Rg+6czxfy+SJF+aN5TmYpu+S/MOj6grN0i5hkTB5cOqPZY+bvph3+0+26q2DkrUM7QuPzav9p6bAIvhs6pugTHC6C4R+GtqaRtuJHmSCAEZq5LQLdb4bMEaLNv8AKGgb/dJuWJkxyXg8Ou0yFewR7pCDZbhKbrs1McCRxAMcY2RNupjXauVjwJVJoJrcVn8wG6PVjdBMwSZ1k0e4spmPBU2VYKuZghL3XWueMmXp1cyVawNQMN+SpB11aFIo4XV04tomACeZVV7yTJXW0D4Lj2wYRSq0TU8OTAuhEiVrlaw7lTaVaoK4Gr7CnhQtKlCIByS4kosawyKUkLwoRNiwVthYudBjhf039kJGZt5o2Ch2OySjVk6dDj+ph0nzGx8wguGx48mlKpig8QTKAZngGtOtgAvcD3V+t2fxFMzTe2oOR7jvex9QqLsRUktexwOxsSrmNniXOZehWOwIf3mDvXnp+d1Dg8G4GCFoKNIHYH0KmODJ4GeiP566B8IZgcCIuEWw+CY0WCr0KbhwKuMceR9ErK2nYyROykBwXatEOADp7vy3IjpGy41/NSB45peh9E0vaQWvJjaT5b7nzV4Yx1QDWduAgDqqYeOYT2Pi4RzKyhyxliatwQfMAijq7TbiEKxpuVdn7Bn8stmTd0FqbrQZqOQPoUDqU3TZrvQrTj4zZ+pcPhgYM3uSI4Wi/kVLXMbFWMBhHWkEc7FV8dScHHQ0kdD7IgqteuRxKdQJNyk1pP6XebSpaNN1+6fQq4qnhdCcKLv2n0Kd8J37T6FMC41WqKgZSdOx9CrLGHkfRRVWGFTBQMaeR9FO1p5FHCySXYPI+iStH//Z",
                        commentText,
                        "Vừa xong"
                );

                // Thêm bình luận vào danh sách
                commentList.add(newComment);

                // Cập nhật số lượng bình luận
                textCommentCount.setText(String.format("%d Comments", commentList.size()));

                // Thông báo adapter về sự thay đổi
                commentAdapter.notifyItemInserted(commentList.size() - 1);

                // Cuộn đến bình luận mới nhất
                recyclerViewComments.scrollToPosition(commentList.size() - 1);

                // Xóa nội dung input
                editTextComment.setText("");

                cmtRef.push().setValue(newComment);
            }
        });

        //khi đóng màn hình:
        btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CommentScreen.this, HomeScreen.class);
                startActivity(intent);

                CommentScreen.this.finish();
            }
        });
    }
}