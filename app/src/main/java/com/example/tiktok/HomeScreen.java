package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    RelativeLayout search_button;
    RelativeLayout plus_button;
    RelativeLayout noti_button;
    RelativeLayout profile_button;

    private ViewPager2 viewPager;
    private VideoAdapter videoAdapter;
    private List<Video> videoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        // Khởi tạo danh sách video
        videoItems = new ArrayList<>();
        videoItems.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.main_video, "@karenne", "345k", "54", "Waiting for you", "#mangden #kontum #dance"));
        videoItems.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.video_2, "@john_doe", "120k", "30", "Smooth vibes", "đám dỗ bên cồn"));
        videoItems.add(new Video("android.resource://" + getPackageName() + "/" + R.raw.video_3, "@tiktok_user", "900k", "150", "Let's dance", "#beoitutu"));

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
                Intent intent = new Intent(HomeScreen.this, SearchScreen.class);
                startActivity(intent);
            }
        });

        plus_button = findViewById(R.id.upload_page);
        plus_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, CameraScreen.class);
                startActivity(intent);
            }
        });

        noti_button = findViewById(R.id.notifi_page);
        noti_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, NotificationScreen.class);
                startActivity(intent);
            }
        });

        profile_button = findViewById(R.id.info_page);
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, ProfileScreen.class);
                startActivity(intent);
            }
        });
    }
}
