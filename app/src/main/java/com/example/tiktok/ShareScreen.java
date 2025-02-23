package com.example.tiktok;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShareScreen extends AppCompatActivity {

    private RelativeLayout reportButton;
    private RelativeLayout facebookButton;
    private RelativeLayout smsButton;
    private RelativeLayout messengerButton;
    private RelativeLayout instagramButton;
    private Button cancelButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_screen);

        //lấy dữ liệu từ intent của homeScreen:
        VideoView mainVideo;
        String videoUriString = getIntent().getStringExtra("VIDEO_URI");
        if (videoUriString != null) {
            Uri videoUri = Uri.parse(videoUriString);
            mainVideo = findViewById(R.id.share_video);
            mainVideo.setVideoURI(videoUri);
            mainVideo.start();
        } else {
            // Log or show an error message, indicating that the VIDEO_URI was not passed properly
            Log.e("ShareScreen", "VIDEO_URI is null");
        }

        reportButton = findViewById(R.id.reportButton);
        facebookButton = findViewById(R.id.facebookButton);
        smsButton = findViewById(R.id.smsButton);
        messengerButton = findViewById(R.id.messengerButton);
        instagramButton = findViewById(R.id.instagramButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Set click listeners for each button
        reportButton.setOnClickListener(v -> {
            String[] reasons = {
                    "Bạo lực, làm dụng và bóc lột dạng phạm tội",
                    "Thủ ghét và quấy rối",
                    "Tự tử và tự làm hại bản thân",
                    "Cách ăn uống không lành mạnh và hình ảnh cơ thể ốm yếu",
                    "Hoạt động và thử thách nguy hiểm",
                    "Hình ảnh khoả thân hoặc nội dung tình dục",
                    "Nội dung gây sốc và phản cảm",
                    "Thông tin sai lệch",
                    "Hành vi lừa đảo và gửi nội dung thu rác",
                    "Hàng hóa và hoạt động được kiểm soát",
                    "Gian lận và lừa đảo",
                    "Chia sẻ thông tin cá nhân",
                    "Sản phẩm nhái và vi phạm quyền sở hữu trí tuệ",
                    "Nội dung định hướng thương hiệu không được chi tiết lộ",
                    "Khác"
            };

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.report_dialog_layout, null);

            // Tùy chỉnh phần tiêu đề và phần tử
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            dialogTitle.setText("Chọn lý do của bạn");

            // Lấy ListView và set các lựa chọn
            ListView listView = dialogView.findViewById(R.id.dialogListView);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reasons);
            listView.setAdapter(adapter);

            // Tạo AlertDialog và hiển thị
            AlertDialog.Builder builder = new AlertDialog.Builder(ShareScreen.this);
            builder.setView(dialogView); // Gán view tùy chỉnh cho dialog
            builder.setCancelable(true);  // Cho phép đóng dialog khi nhấn ra ngoài

            // Tạo AlertDialog
            AlertDialog alertDialog = builder.create();

            // Xử lý sự kiện khi người dùng chọn một mục trong ListView
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedReason = reasons[position];
                Toast.makeText(ShareScreen.this, "Lý do bạn chọn: " + selectedReason, Toast.LENGTH_SHORT).show();

                // Bạn có thể thực hiện thêm các hành động khác tại đây khi người dùng chọn một lý do
            });

            // Lấy ImageButton (nút đóng) và thêm sự kiện đóng dialog
            ImageButton closeButton = dialogView.findViewById(R.id.closeButton_report);
            closeButton.setOnClickListener(v1 -> {
                // Đảm bảo rằng dialog được đóng khi người dùng nhấn vào nút "X"
                alertDialog.dismiss();  // Gọi dismiss() trên đối tượng alertDialog
            });

            // Hiển thị dialog
            alertDialog.show();
        });
//
//        facebookButton.setOnClickListener(v -> {
//            // Handle Facebook share action
//        });
//
//        smsButton.setOnClickListener(v -> {
//            // Handle SMS share action
//        });
//
//        messengerButton.setOnClickListener(v -> {
//            // Handle Messenger share action
//        });
//
//        instagramButton.setOnClickListener(v -> {
//            // Handle Instagram share action
//        });
//
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShareScreen.this, HomeScreen.class);
            startActivity(intent);
        });
    }
}