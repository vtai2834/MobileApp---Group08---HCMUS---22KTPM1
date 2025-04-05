package com.example.tiktok;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class ChangeTikTokID extends AppCompatActivity {

    private EditText etUserId;
    private TextView saveButton;
    private TextView cancelButton;
    private DatabaseReference databaseReference;
    private String originalIdName;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_tik_tok_id);

        etUserId = findViewById(R.id.etUserId);
        saveButton = findViewById(R.id.save);
        cancelButton = findViewById(R.id.cancel);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        originalIdName = getIntent().getStringExtra("idName");
        userKey = getIntent().getStringExtra("userKey");

        if (originalIdName != null) {
            etUserId.setText(originalIdName);
        }

        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String newIdName = etUserId.getText().toString().trim();

            if (!isValidIdName(newIdName)) {
                Toast.makeText(this, "ID không hợp lệ. Chỉ dùng chữ, số, dấu chấm, gạch dưới.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newIdName.equals(originalIdName)) {
                Toast.makeText(this, "ID không thay đổi.", Toast.LENGTH_SHORT).show();
                return;
            }

            checkIdNameUniqueness(newIdName);
        });
    }

    private boolean isValidIdName(String idName) {
        String regex = "^[a-zA-Z0-9._]+$";
        return Pattern.matches(regex, idName);
    }

    private void checkIdNameUniqueness(String newIdName) {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedIdName = userSnapshot.child("idName").getValue(String.class);
                    if (storedIdName != null && storedIdName.equals(newIdName)) {
                        Toast.makeText(this, "ID đã tồn tại. Vui lòng chọn ID khác.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                updateIdNameInFirebase(newIdName);
            } else {
                Toast.makeText(this, "Lỗi khi kiểm tra ID trong Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateIdNameInFirebase(String newIdName) {
        if (userKey != null) {
            databaseReference.child(userKey).child("idName").setValue(newIdName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật ID thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi cập nhật ID: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
        }
    }
}
