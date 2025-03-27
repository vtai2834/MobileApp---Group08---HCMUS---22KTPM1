package com.example.tiktok;

import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private RelativeLayout home_button, edit_profile_button;
    private EditText etBio;
    private TextView tvUsername, tvUserId, tvLike, tvFollower, tvFollowing;
    private String username;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_screen);

//        recyclerView = findViewById(R.id.recyclerViewProfile);
//
//        // Sử dụng GridLayoutManager với 3 cột và hướng cuộn ngang
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, true);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // Khởi tạo danh sách video
//        videoList = new ArrayList<>();
//        videoList.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "", "", "", "", ""));
//        videoList.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "", "", "", "", ""));
//        videoList.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "", "", "", "", ""));
//        videoList.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "", "", "", "", ""));
//        videoList.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "", "", "", "", ""));
//        videoList.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "", "", "", "", ""));
//
//        // Thiết lập Adapter
//        videoAdapter = new VideoAdapter(videoList, this); // Truyền context vào VideoAdapter
//        recyclerView.setAdapter(videoAdapter);

// Ánh xạ View
        tvUsername = findViewById(R.id.tvUsername);
        tvUserId = findViewById(R.id.user_id);
        tvFollower = findViewById(R.id.follower);
        tvFollowing = findViewById(R.id.following);
        tvLike = findViewById(R.id.like);
        etBio = findViewById(R.id.etBio); // EditText nhập Bio
        home_button = findViewById(R.id.home_page);
        edit_profile_button = findViewById(R.id.edit_profile_btn);

        // Lấy username từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Truy cập Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Lấy dữ liệu user từ Firebase
        getUserDataFromFirebase();

        // Xử lý sự kiện click nút Home
        home_button.setOnClickListener(v -> {
            Intent homeIntent = new Intent(ProfileScreen.this, HomeScreen.class);
            startActivity(homeIntent);
        });

        // Xử lý sự kiện click nút Chỉnh sửa hồ sơ
        edit_profile_button.setOnClickListener(view -> {
            Intent editIntent = new Intent(ProfileScreen.this, EditProfile.class);
            startActivity(editIntent);
        });

        // Lắng nghe khi EditText mất focus
        etBio.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                updateBioToFirebase();
            }
        });

        // Bắt sự kiện click vào profile_layout, mid hoặc middle để ẩn bàn phím
        View profileLayout = findViewById(R.id.main);
//        View midLayout = findViewById(R.id.mid);
        View middleLayout = findViewById(R.id.middle);

        View.OnClickListener hideKeyboardListener = v -> {
            hideKeyboard();
            etBio.clearFocus();
            updateBioToFirebase(); // Cập nhật Bio nếu có thay đổi
        };

        profileLayout.setOnClickListener(hideKeyboardListener);
//        midLayout.setOnClickListener(hideKeyboardListener);
        middleLayout.setOnClickListener(hideKeyboardListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDataFromFirebase(); // Gọi lại để cập nhật thông tin
    }

    private void getUserDataFromFirebase() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    if (storedUsername != null && storedUsername.equals(username)) {
                        String name = userSnapshot.child("name").getValue(String.class);
                        String idName = userSnapshot.child("idName").getValue(String.class);
                        Integer followerCount = userSnapshot.child("followerCount").getValue(Integer.class);
                        Integer followingCount = userSnapshot.child("followingCount").getValue(Integer.class);
                        Integer likeCount = userSnapshot.child("likeCount").getValue(Integer.class);
                        String bio = userSnapshot.child("bio").getValue(String.class); // Lấy bio từ Firebase

                        // Set dữ liệu vào View
                        tvUsername.setText(name != null ? name : "Người dùng");
                        tvUserId.setText(idName != null ? idName : username);
                        tvFollower.setText(followerCount != null ? String.valueOf(followerCount) : "0");
                        tvFollowing.setText(followingCount != null ? String.valueOf(followingCount) : "0");
                        tvLike.setText(likeCount != null ? String.valueOf(likeCount) : "0");
                        etBio.setText(bio != null ? bio : ""); // Hiển thị bio

                        return;
                    }
                }
                Toast.makeText(ProfileScreen.this, "Không tìm thấy dữ liệu tài khoản!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileScreen.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBioToFirebase() {
        String newBio = etBio.getText().toString().trim();
        if (newBio.isEmpty()) return;

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    if (storedUsername != null && storedUsername.equals(username)) {
                        // Cập nhật bio trên Firebase
                        userSnapshot.getRef().child("bio").setValue(newBio)
                                .addOnSuccessListener(unused -> Toast.makeText(this, "Cập nhật Bio thành công!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật Bio!", Toast.LENGTH_SHORT).show());
                        return;
                    }
                }
            }
        });
    }

    // Hàm ẩn bàn phím
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && !(view instanceof EditText)) {
                hideKeyboard();
                etBio.clearFocus();
                updateBioToFirebase(); // Cập nhật bio khi chạm ra ngoài
            }
        }
        return super.dispatchTouchEvent(event);
    }

}