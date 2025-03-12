package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    RelativeLayout search_button;
    RelativeLayout plus_button;
    RelativeLayout noti_button;
    RelativeLayout profile_button;


    private ViewPager2 viewPager;
    private VideoAdapter videoAdapter;
    private List<Video> videoItems;
//    private List<Video> videoItems1;



    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);


//         Khởi tạo danh sách video
//        videoItems1 = new ArrayList<>();
//        videoItems1.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//6d4d64f5-eb0d-4262-8e6d-2c30e9afa8da.mp4", "@john_doe", "120k", "30", "Smooth vibes", "đám dỗ bên cồn"));
//        videoItems1.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//63d56221-aec7-42e7-9653-9a373ba9fe58.mp4", "@karenne", "345k", "54", "Waiting for you", "#mangden #kontum #dance"));
//        videoItems1.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//d270f3e0-6427-46cf-8bcf-792d553cb9fd.mp4", "@tiktok_user", "900k", "150", "Let's dance", "#beoitutu"));


        videoItems = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");

//        for (Video video : videoItems1) {
//            String videoId = databaseReference.push().getKey(); // Tạo ID duy nhất
//            databaseReference.child(videoId).setValue(video);
//        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoItems.clear(); // Xóa danh sách cũ

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Video video = dataSnapshot.getValue(Video.class);
                    videoItems.add(video);
                }
                Collections.reverse(videoItems); // Reverse the list to show latest first
                videoAdapter.notifyDataSetChanged(); // Cập nhật giao diện
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load videos: " + error.getMessage());
            }
        });

        // Thêm các video khác vào danh sách videoItems

        // Set ViewPager2 adapter
        viewPager = findViewById(R.id.viewPager);
        videoAdapter = new VideoAdapter(videoItems, this); // Đảm bảo rằng context được truyền vào
        viewPager.setAdapter(videoAdapter);

        // Thiết lập ViewPager2 cuộn dọc
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL); // Chuyển thành cuộn dọc

        search_button = findViewById(R.id.search_page);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

                Intent intent = new Intent(HomeScreen.this, SearchScreen.class);
                startActivity(intent);

                HomeScreen.this.finish();
            }
        });

        plus_button = findViewById(R.id.upload_page);
        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

                Intent intent = new Intent(HomeScreen.this, CameraScreen.class);
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
                startActivity(intent);

                HomeScreen.this.finish();
            }
        });

        profile_button = findViewById(R.id.info_page);
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoAdapter.stopVideoAtPosition(videoAdapter.getCurrentPositionVideo());

                Intent intent = new Intent(HomeScreen.this, ProfileScreen.class);
                startActivity(intent);

                HomeScreen.this.finish();
            }
        });


        videoAdapter.playVideoAt(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                videoAdapter.playVideoAt(position); // Gọi phương thức phát video
            }
        });

        Log.d("VideoAdapter", "On Create play video at: " + videoAdapter.getCurrentPositionVideo());


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
        videoAdapter.playVideoAt(videoAdapter.getCurrentPositionVideo());
        Log.d(
                "VideoAdapter",
                "On Resumn play video at: " + videoAdapter.getCurrentPositionVideo()
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoAdapter.stopAllVideo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoAdapter.playVideoAt(videoAdapter.getCurrentPositionVideo());
        Log.d(
                "VideoAdapter",
                "On Start play video at: " + videoAdapter.getCurrentPositionVideo()
        );
    }




}
