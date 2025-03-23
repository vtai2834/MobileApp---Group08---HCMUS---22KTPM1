package com.example.tiktok;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

public class ProfileScreen extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    RelativeLayout home_button;
    RelativeLayout edit_profile_button;

    //thông tin của User:
    private String userID;
    private String account;
    private String avatar;
    private String bio;
    private int followerCount;
    private int followingCount;
    private String idName;
    private int likeCount;
    private String name;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_screen);

        // Lấy userId từ Intent
        userID = getIntent().getStringExtra("USER_ID");
        Log.d("HomeScreen", "Received userId: " + userID);

        // Nếu có userId, lấy thông tin user từ Firebase
        if (userID != null && !userID.isEmpty()) {
            fetchUserData(userID);
        } else {
            Toast.makeText(this, "Không nhận được ID người dùng!", Toast.LENGTH_SHORT).show();
        }



        home_button = findViewById(R.id.home_page);
        home_button.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, HomeScreen.class);
            intent.putExtra("USER_ID", userID);
            startActivity(intent);
        });

        edit_profile_button = findViewById(R.id.edit_profile_btn);
        edit_profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileScreen.this, EditProfile.class);
                startActivity(intent);
            }
        });
    }


    private void fetchUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy thông tin user từ Firebase
                    account = snapshot.child("account").getValue(String.class);
                    avatar = snapshot.child("avatar").getValue(String.class);
                    bio = snapshot.child("bio").getValue(String.class);

                    // Lấy các giá trị số, đảm bảo kiểm tra null
                    if (snapshot.child("followerCount").exists()) {
                        followerCount = snapshot.child("followerCount").getValue(Integer.class);
                    }
                    if (snapshot.child("followingCount").exists()) {
                        followingCount = snapshot.child("followingCount").getValue(Integer.class);
                    }

                    idName = snapshot.child("idName").getValue(String.class);

                    if (snapshot.child("likeCount").exists()) {
                        likeCount = snapshot.child("likeCount").getValue(Integer.class);
                    }

                    name = snapshot.child("name").getValue(String.class);
                    password = snapshot.child("password").getValue(String.class);

                    // DI CHUYỂN TẤT CẢ VIỆC CẬP NHẬT UI VÀO ĐÂY
                    // Vì đây là lúc dữ liệu đã sẵn sàng
                    updateUI();

                } else {
                    Toast.makeText(ProfileScreen.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi khi lấy thông tin người dùng: " + error.getMessage());
                Toast.makeText(ProfileScreen.this, "Lỗi khi lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo phương thức mới để cập nhật UI
    private void updateUI() {
        // Cập nhật tất cả UI ở đây
        TextView userNameProfile = findViewById(R.id.userName_profile);
        userNameProfile.setText(account);

        TextView userIdProfile = findViewById(R.id.user_id);
        userIdProfile.setText(idName);

        CircleImageView avtProfile = findViewById(R.id.avt_profile);
        Log.d("CHECK_AVATAR_URL", avatar);
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(avtProfile);
        } else {
            avtProfile.setImageResource(R.drawable.default_profile);
        }

        TextView numFollowing = findViewById(R.id.num_following);
        numFollowing.setText(String.valueOf(followingCount));

        TextView numFollowers = findViewById(R.id.num_follower);
        numFollowers.setText(String.valueOf(followerCount));

        TextView numLikes = findViewById(R.id.num_like);
        numLikes.setText(String.valueOf(likeCount));
    }
}