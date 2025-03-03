package com.example.tiktok;

import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ItemVideoHomeScreen extends AppCompatActivity {
    RelativeLayout control_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_video_home_screen);

        control_layout = findViewById(R.id.control_layout);
        control_layout.bringToFront();
    }
}