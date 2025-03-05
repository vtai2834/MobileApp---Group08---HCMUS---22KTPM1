package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
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

    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        // Khởi tạo danh sách video
        videoItems = new ArrayList<>();
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//6d4d64f5-eb0d-4262-8e6d-2c30e9afa8da.mp4", "@john_doe", "120k", "30", "Smooth vibes", "đám dỗ bên cồn"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//63d56221-aec7-42e7-9653-9a373ba9fe58.mp4", "@karenne", "345k", "54", "Waiting for you", "#mangden #kontum #dance"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//d270f3e0-6427-46cf-8bcf-792d553cb9fd.mp4", "@tiktok_user", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//d270f3e0-6427-46cf-8bcf-792d553cb9fd.mp4", "@tiktok_user", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));
        videoItems.add(new Video("https://xtjgitlcyzphaailqpdj.supabase.co/storage/v1/object/public/testvideo//91bf1782-1b22-41ef-874a-6e06d64f66a5.mp4", "@tiktok_user222", "900k", "150", "Let's dance", "#beoitutu"));

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


//        videoAdapter.playVideoAt(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                videoAdapter.playVideoAt(position); // Gọi phương thức phát video
            }
        });


    }


}
