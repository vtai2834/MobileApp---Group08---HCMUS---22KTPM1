package com.example.tiktok;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity {

    private ImageView back_to_profile_button, editName, editUsername;
    private EditText etUsername, etUserId, etBio;
    private TextView tvLinkID, btnSave;
    private DatabaseReference databaseReference;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ View
        etUsername = findViewById(R.id.etUsername);
        etUserId = findViewById(R.id.etUserId);
        etBio = findViewById(R.id.Bio);
        tvLinkID = findViewById(R.id.linkID);
        back_to_profile_button = findViewById(R.id.back_to_profilescreen);
        editName = findViewById(R.id.editName);
        editUsername = findViewById(R.id.editUsername);
        btnSave = findViewById(R.id.btn_save);

        // Lấy username từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Lấy dữ liệu user từ Firebase
        getUserDataFromFirebase();

        // Xử lý sự kiện nút quay lại ProfileScreen
        back_to_profile_button.setOnClickListener(view -> finish());

        // Xử lý sự kiện click vào icon chỉnh sửa username
        editName.setOnClickListener(view -> enableEditing(etUsername));

        // Xử lý sự kiện click vào icon chỉnh sửa user_id
        editUsername.setOnClickListener(view -> enableEditing(etUserId));

        ImageView editBio = findViewById(R.id.etBio);
        editBio.setOnClickListener(view -> enableEditing(etBio));

        // Xử lý sự kiện bấm "Lưu"
        btnSave.setOnClickListener(view -> updateUserData());
    }

    private void enableEditing(EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.requestFocus();

        // Hiển thị bàn phím
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void getUserDataFromFirebase() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    if (storedUsername != null && storedUsername.equals(username)) {
                        String name = userSnapshot.child("name").getValue(String.class);
                        String idName = userSnapshot.child("idName").getValue(String.class);
                        String bio = userSnapshot.child("bio").getValue(String.class); // Lấy Bio

                        // Cập nhật giao diện
                        etUsername.setText(name != null ? name : "Người dùng");
                        etUserId.setText(idName != null ? idName : username);
                        etBio.setText(bio != null ? bio : ""); // Hiển thị Bio
                        tvLinkID.setText("tiktok.com@" + (idName != null ? idName : username));

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

                    if (storedIdName != null && storedIdName.equals(newIdName) && !userSnapshot.getKey().equals(userKey[0])) {
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

                // Cập nhật dữ liệu
                databaseReference.child(userKey[0]).child("name").setValue(newName);
                databaseReference.child(userKey[0]).child("idName").setValue(newIdName);
                databaseReference.child(userKey[0]).child("bio").setValue(newBio);
                Toast.makeText(EditProfile.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfile.this, "Lỗi khi tìm kiếm tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }



}