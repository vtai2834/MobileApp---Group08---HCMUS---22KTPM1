package com.example.tiktok;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class ChangeUsername extends AppCompatActivity {

    private EditText etUsername;
    private TextView saveButton;
    private TextView cancelButton;

    private String currentUsername;
    private String userKey;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        etUsername = findViewById(R.id.etUsername);
        saveButton = findViewById(R.id.save);
        cancelButton = findViewById(R.id.cancel);

        Intent intent = getIntent();

        language = intent.getStringExtra("language");

        if (language != null) {
            if (language.equals("English") || language.equals("Tiếng Anh")) {
                LocaleHelper.setLocale(this, "en");
            } else {
                LocaleHelper.setLocale(this, "vi");
            }
        } else {
            LocaleHelper.setLocale(this, "vi");
        }

        if (intent != null) {
            currentUsername = intent.getStringExtra("currentUsername");
            userKey = intent.getStringExtra("userKey");
            etUsername.setText(currentUsername);
        }

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        saveButton.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUsernameInFirebase(newUsername);
            } else {
                Toast.makeText(this, "Tên người dùng không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void updateUsernameInFirebase(String newUsername) {
        if (userKey == null) {
            Toast.makeText(this, "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("Users")
                .child(userKey)
                .child("name")
                .setValue(newUsername)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật tên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
