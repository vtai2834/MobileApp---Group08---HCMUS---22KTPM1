package com.example.tiktok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private ImageView back_to_profilescreen;
    private CircleImageView profileImage;
    private FrameLayout profilePhotoContainer;
    private TextView linkID, etUsername, etUserId, etBio;
    private RelativeLayout instagramContainer, youtubeContainer;
    private RelativeLayout tiktokStudioContainer, ordersContainer;

    private DatabaseReference databaseReference;
    private String username;
    private String originalName, originalIdName, originalBio;
    private boolean hasChanges = false;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupClickListeners();

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

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Load user data
        getUserDataFromFirebase();
    }

    private void initializeViews() {
        back_to_profilescreen = findViewById(R.id.back_to_profilescreen);
        profileImage = findViewById(R.id.profileImage);
        profilePhotoContainer = findViewById(R.id.profilePhotoContainer);
        etUsername = findViewById(R.id.etUsername);
        etUserId = findViewById(R.id.etUserId);
        etBio = findViewById(R.id.etBio);
        linkID = findViewById(R.id.linkID);

        // New ImageView for editing name
        ImageView editName = findViewById(R.id.editUsername);

        // Containers for click handling
        instagramContainer = findViewById(R.id.instagramContainer);
        youtubeContainer = findViewById(R.id.youtubeContainer);
        tiktokStudioContainer = findViewById(R.id.tiktokStudioContainer);
        ordersContainer = findViewById(R.id.ordersContainer);
    }

    private void setupClickListeners() {
        back_to_profilescreen.setOnClickListener(v -> {
            if (hasChanges) {
                showDiscardChangesDialog();
            } else {
                finish();
            }
        });

        profilePhotoContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thay đổi ảnh chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        instagramContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng liên kết Instagram chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        youtubeContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng liên kết YouTube chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        tiktokStudioContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng TikTok Studio chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        ordersContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Đơn hàng chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        // Khi nhấn vào EditUsername, truyền idName vào ChangeTikTokID
        ImageView editUserId = findViewById(R.id.editUserId);
        editUserId.setOnClickListener(v -> {
            String idName = etUserId.getText().toString().trim();

            FirebaseDatabase.getInstance().getReference("Users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                                String storedIdName = userSnapshot.child("idName").getValue(String.class);
                                if (storedIdName != null && storedIdName.equals(idName)) {
                                    String userKey = userSnapshot.getKey();

                                    Intent intent = new Intent(EditProfile.this, ChangeTikTokID.class);
                                    intent.putExtra("idName", idName);
                                    intent.putExtra("userKey", userKey);
                                    intent.putExtra("language", language);
                                    startActivity(intent);
                                    return;
                                }
                            }

                            Toast.makeText(EditProfile.this, "Không tìm thấy userKey tương ứng với ID!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfile.this, "Lỗi khi tìm userKey từ Firebase!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Khi nhấn vào EditName, mở ChangeUsername và truyền currentUsername
        ImageView editName = findViewById(R.id.editUsername);
        editName.setOnClickListener(v -> {
            String currentUsername = etUsername.getText().toString().trim();
            String idName = etUserId.getText().toString().trim();

            FirebaseDatabase.getInstance().getReference("Users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                                String storedIdName = userSnapshot.child("idName").getValue(String.class);
                                if (storedIdName != null && storedIdName.equals(idName)) {
                                    String userKey = userSnapshot.getKey();

                                    Intent intent = new Intent(EditProfile.this, ChangeUsername.class);
                                    intent.putExtra("currentUsername", currentUsername);
                                    intent.putExtra("userKey", userKey);
                                    intent.putExtra("language", language);
                                    startActivity(intent);
                                    return;
                                }
                            }

                            Toast.makeText(EditProfile.this, "Không tìm thấy userKey tương ứng với ID!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfile.this, "Lỗi khi tìm userKey từ Firebase!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Khi nhấn vào EditBio, mở ChangeBio và truyền currentBio
        ImageView editBio = findViewById(R.id.editBio);
        editBio.setOnClickListener(v -> {
            String currentBio = etBio.getText().toString().trim(); // Lấy bio hiện tại
            String idName = etUserId.getText().toString().trim(); // lấy idName hiện tại

            FirebaseDatabase.getInstance().getReference("Users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                                String storedIdName = userSnapshot.child("idName").getValue(String.class);
                                if (storedIdName != null && storedIdName.equals(idName)) {
                                    String userKey = userSnapshot.getKey();

                                    Intent intent = new Intent(EditProfile.this, ChangeBio.class);
                                    intent.putExtra("currentBio", currentBio);  // Truyền bio vào ChangeBio
                                    intent.putExtra("userKey", userKey);  // Truyền userKey vào ChangeBio
                                    intent.putExtra("language", language);
                                    startActivity(intent);
                                    return;
                                }
                            }

                            Toast.makeText(EditProfile.this, "Không tìm thấy userKey tương ứng với ID!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfile.this, "Lỗi khi tìm userKey từ Firebase!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void enableEditing(EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.requestFocus();

        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateLinkID() {
        String idName = etUserId.getText().toString().trim();
        if (!idName.isEmpty()) {
            linkID.setText("tiktok.com/@" + idName);
        }
    }

    private void checkForChanges() {
        String newName = etUsername.getText().toString().trim();
        String newIdName = etUserId.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();

        hasChanges = !newName.equals(originalName) ||
                !newIdName.equals(originalIdName) ||
                !newBio.equals(originalBio);
    }

    private void showDiscardChangesDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Hủy thay đổi?")
                .setMessage("Bạn có muốn hủy các thay đổi đã thực hiện không?")
                .setPositiveButton("Hủy thay đổi", (dialog, which) -> finish())
                .setNegativeButton("Tiếp tục chỉnh sửa", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void getUserDataFromFirebase() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    if (storedUsername != null && storedUsername.equals(username)) {
                        String avt = userSnapshot.child("avatar").getValue(String.class);
                        String name = userSnapshot.child("name").getValue(String.class);
                        String idName = userSnapshot.child("idName").getValue(String.class);
                        String bio = userSnapshot.child("bio").getValue(String.class);

                        // Save original values
                        originalName = name != null ? name : "";
                        originalIdName = idName != null ? idName : "";
                        originalBio = bio != null ? bio : "";

                        // Update UI
                        etUsername.setText(originalName);
                        etUserId.setText(originalIdName);
                        etBio.setText(originalBio);
                        linkID.setText("tiktok.com/@" + originalIdName);

                        // Load profile image
                        if (avt != null && !avt.isEmpty()) {
                            Glide.with(this)
                                    .load(avt)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.default_profile);
                        }

                        return;
                    }
                }
                Toast.makeText(EditProfile.this, "Không tìm thấy dữ liệu tài khoản!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfile.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        String newName = etUsername.getText().toString().trim();
        String newIdName = etUserId.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();

        if (newName.isEmpty() || newIdName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                final String[] userKey = new String[1];
                final boolean[] isIdNameTaken = {false};

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    String storedIdName = userSnapshot.child("idName").getValue(String.class);

                    if (storedUsername != null && storedUsername.equals(username)) {
                        userKey[0] = userSnapshot.getKey();
                    }

                    if (storedIdName != null && storedIdName.equals(newIdName) &&
                            !newIdName.equals(originalIdName)) {
                        isIdNameTaken[0] = true;
                    }
                }

                if (userKey[0] == null) {
                    Toast.makeText(EditProfile.this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isIdNameTaken[0]) {
                    Toast.makeText(EditProfile.this, "ID đã tồn tại! Vui lòng chọn ID khác.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update data
                databaseReference.child(userKey[0]).child("name").setValue(newName);
                databaseReference.child(userKey[0]).child("idName").setValue(newIdName);
                databaseReference.child(userKey[0]).child("bio").setValue(newBio)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditProfile.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            hasChanges = false;
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(EditProfile.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(EditProfile.this, "Lỗi khi tìm kiếm tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDataFromFirebase();
    }

}
