package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileScreen extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    RelativeLayout home_button;
    RelativeLayout edit_profile_button;

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

        home_button = findViewById(R.id.home_page);
        home_button.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, HomeScreen.class);
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
}