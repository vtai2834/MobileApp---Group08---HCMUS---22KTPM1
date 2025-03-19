package com.example.tiktok;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

public class AndroidDownloader implements Downloader {
    private final Context context;
    private final DownloadManager downloadManager;

    public AndroidDownloader(Context context) {
        this.context = context;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public long downloadFile(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                // Đổi MIME type phù hợp với video, ví dụ video/mp4
                .setMimeType("video/mp4")
                // Để tải qua mạng Wi-Fi
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                // Tùy chọn hiển thị thông báo khi tải hoàn thành
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // Tên video khi tải về
                .setTitle("video.mp4")
                // Cài đặt header nếu cần thiết (token hoặc thông tin xác thực khác)
                .addRequestHeader("Authorization", "Bearer <token>")
                // Chỉ định vị trí lưu video trong thư mục Download
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "video.mp4");

        return downloadManager.enqueue(request);
    }
}
