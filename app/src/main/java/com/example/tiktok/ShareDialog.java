package com.example.tiktok;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ShareDialog extends BottomSheetDialog {
    private Context context;
    private String videoUrl;
    private OnReportClickListener reportListener;

    public interface OnReportClickListener {
        void onReportClick(String videoUrl);
    }

    public ShareDialog(@NonNull Context context, String videoUrl, OnReportClickListener listener) {
        super(context);
        this.context = context;
        this.videoUrl = videoUrl;
        this.reportListener = listener;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_share_screen, null);
        setContentView(view);

        // Cập nhật kích thước của BottomSheetDialog
        setOnShowListener(dialog -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(dpToPx(200));
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // Thiết lập chiều cao cố định
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = dpToPx(200);
                bottomSheet.setLayoutParams(layoutParams);
            }
        });

        // Thiết lập sự kiện cho Report
        view.findViewById(R.id.reportButton).setOnClickListener(v -> {
            if (reportListener != null) {
                reportListener.onReportClick(videoUrl);
            }
            dismiss();
        });

        // Thiết lập sự kiện cho Facebook
        RelativeLayout fb_btn = view.findViewById(R.id.facebookButton);
        fb_btn.setOnClickListener(v -> {

            shareToApp("com.facebook.katana", "facebook.com");
//            Toast.makeText(context, "Đăng bài hoặc chia sẻ qua Facebook thành công !", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho SMS
        view.findViewById(R.id.smsButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, videoUrl);
            intent.setPackage("com.android.mms");
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Fallback nếu không tìm thấy ứng dụng SMS
                intent.setPackage(null);
                context.startActivity(Intent.createChooser(intent, "Share via SMS"));
            }
            dismiss();

//            Toast.makeText(context, "Chia sẻ qua SMS thành công !", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho Messenger
        view.findViewById(R.id.messengerButton).setOnClickListener(v -> {
            shareToApp("com.facebook.orca", "fb-messenger://share");
//            Toast.makeText(context, "Chia sẻ qua Messenger thành công !", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho Instagram
        view.findViewById(R.id.instagramButton).setOnClickListener(v -> {
            shareToApp("com.instagram.android", "instagram://");
//            Toast.makeText(context, "Chia sẻ qua Instagram thành công !", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho Cancel
        view.findViewById(R.id.cancelButton).setOnClickListener(v -> dismiss());
    }

    // Chuyển đổi dp sang pixels
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void shareToApp(String packageName, String fallbackUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, videoUrl);

        boolean appFound = false;

        // Thử sử dụng ứng dụng cụ thể
        if (packageName != null) {
            intent.setPackage(packageName);
            try {
                context.startActivity(intent);
                appFound = true;
                Log.d("ShareDialog", "App found and opened: " + packageName);
            } catch (ActivityNotFoundException e) {
                appFound = false;
                Log.e("ShareDialog", "App not found: " + packageName, e);
            }
        }

        // Nếu không tìm thấy ứng dụng, thử mở bằng URL
        if (!appFound && fallbackUrl != null) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                context.startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                // Không tìm thấy trình duyệt
                Toast.makeText(context, "Không tìm thấy ứng dụng phù hợp", Toast.LENGTH_SHORT).show();
            }
        }

        dismiss();
    }
}
