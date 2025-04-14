package com.example.tiktok;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewFollowingScreen extends AppCompatActivity {

    private RecyclerView followingRecyclerView;
    private FollowingAdapter followingAdapter;
    private List<User> followingList;
    private ImageButton backButton;
    private String userID;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_following_screen);
        backButton = findViewById(R.id.backButton);

        language = getIntent().getStringExtra("language");

        if (language != null) {
            if (language.equals("English") || language.equals("Tiếng Anh")) {
                LocaleHelper.setLocale(this, "en");
            } else {
                LocaleHelper.setLocale(this, "vi");
            }
        } else {
            LocaleHelper.setLocale(this, "vi");
        }

        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUserID();  // Lấy userID từ đối tượng người dùng
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();  // Kết thúc Activity nếu không tìm thấy userID
        }

        followingRecyclerView = findViewById(R.id.followingRecyclerView);
        followingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        followingList = new ArrayList<>();
        followingAdapter = new FollowingAdapter(this, followingList);
        followingRecyclerView.setAdapter(followingAdapter);

        loadFollowingList();

        backButton.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void loadFollowingList() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.child(userID).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot followingSnapshot) {
                followingList.clear();

                if (followingSnapshot.exists()) {
                    long count = followingSnapshot.getChildrenCount();

                    // Lấy toàn bộ danh sách Users để đối chiếu
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot usersSnapshot) {
                            for (DataSnapshot snapshot : followingSnapshot.getChildren()) {
                                String followingIdName = snapshot.getKey();

                                boolean found = false;
                                for (DataSnapshot userSnap : usersSnapshot.getChildren()) {
                                    User user = userSnap.getValue(User.class);
                                    if (user != null && user.getIdName().equals(followingIdName)) {
                                        followingList.add(user);
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                }
                            }

                            followingAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


}
