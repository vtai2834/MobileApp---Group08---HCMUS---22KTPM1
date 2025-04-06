package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeScreen extends AppCompatActivity {

    RelativeLayout search_button;
    RelativeLayout plus_button;
    RelativeLayout noti_button;
    RelativeLayout profile_button;


    private ViewPager2 viewPager;
    private VideoAdapter videoAdapter;
    private List<Video> videoItems;
//    private List<Video> videoItems1;
    private final List<String> videoIds = new ArrayList<>();;

    String userID = "";
    String userIdName = "";

    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        // Lấy userId từ Intent
//        userID = getIntent().getStringExtra("USER_ID");
//        userIdName = getIntent().getStringExtra("USER_ID_NAME");

        // Lấy thông tin người dùng từ UserManager
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUserID();
            userIdName = currentUser.getIdName();
        }

        videoItems = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoItems.clear();
                videoIds.clear();

                List<Pair<Video, String>> videoPairs = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Video video = dataSnapshot.getValue(Video.class);
                    String videoId = dataSnapshot.getKey();
                    videoPairs.add(new Pair<>(video, videoId));
                }

                sortVideo(videoPairs);


                videoAdapter.notifyDataSetChanged();


                Log.d("checkVideoIds_onDataChange", String.valueOf(videoIds));

                // After data is loaded, set the correct item position
                int pos = getIntent().getIntExtra("SEARCH_VIDEO_POSITION", 0);  // Default is 0 if not found
                Log.d("check pos from search | profile -> home", String.valueOf(pos));

                videoAdapter.currentPositionPlayingVideo = pos;

                // Ensure setCurrentItem is called after data is loaded
                viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(pos, false);  // Set the correct item position without animation
                        videoAdapter.playVideoAt(pos);
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load videos: " + error.getMessage());
            }

            private void sortVideo(List<Pair<Video, String>> videoPairs)
            {

//                Collections.shuffle(videoPairs);
                Collections.reverse(videoPairs);

                for (Pair<Video, String> pair : videoPairs) {
                    videoItems.add(pair.first);
                    videoIds.add(pair.second);
//                    Log.d("checkVideoIds_onCreate()", String.valueOf(videoIds));
                }
            }
        });

        // Thêm các video khác vào danh sách videoItems

        // Set ViewPager2 adapter
        viewPager = findViewById(R.id.viewPager);
        videoAdapter = new VideoAdapter(videoItems, videoIds, this, userID); // Đảm bảo rằng context được truyền vào
        viewPager.setAdapter(videoAdapter);

        // Thiết lập ViewPager2 cuộn dọc
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL); // Chuyển thành cuộn dọc

        search_button = findViewById(R.id.search_page);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

//                Gson gson = new Gson();
//                String videoItemsJson = gson.toJson(videoIds);

                Intent intent = new Intent(HomeScreen.this, SearchScreen.class);
                intent.putExtra("VIDEOS_ARRAY_JSON", new ArrayList<>(videoIds));
//                intent.putExtra("USER_ID", userID);
                startActivity(intent);
                finish();
            }
        });

        plus_button = findViewById(R.id.upload_page);
        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

                Intent intent = new Intent(HomeScreen.this, CameraScreen.class);
//                intent.putExtra("USER_ID", userID);
//                intent.putExtra("USER_ID_NAME", userIdName);
                startActivity(intent);

                HomeScreen.this.finish();
            }
        });

        noti_button = findViewById(R.id.notifi_page);
        noti_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

                Intent intent = new Intent(HomeScreen.this, NotificationScreen.class);
//                intent.putExtra("USER_ID", userID);
                startActivity(intent);

                HomeScreen.this.finish();
            }
        });

        profile_button = findViewById(R.id.info_page);
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

                // Không cần truyền USER_ID nữa, vì ProfileScreen đã lấy từ SharedPreferences
                Intent intent = new Intent(HomeScreen.this, ProfileScreen.class);
                intent.putExtra("VIDEOS_ARRAY_JSON", new ArrayList<>(videoIds));
                startActivity(intent);

                HomeScreen.this.finish();
            }
        });



        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("VideoAdapter - onPageSelected()", "On Page Selected play video at: " + position);
                videoAdapter.playVideoAt(position); // Gọi phương thức phát video
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoAdapter.stopAllVideo();
        videoAdapter.releasePlayers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoAdapter.releasePlayers();
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.d(
                "VideoAdapter - onResume()",
                "On Resumn play video at: " + videoAdapter.getCurrentPositionVideo()
        );
        videoAdapter.playVideoAt(videoAdapter.getCurrentPositionVideo());
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoAdapter.stopAllVideo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(
                "VideoAdapter - onStart()",
                "On Start play video at: " + videoAdapter.getCurrentPositionVideo()
        );
        videoAdapter.playVideoAt(videoAdapter.getCurrentPositionVideo());
    }




}
