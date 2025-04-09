package com.example.tiktok;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeBio extends AppCompatActivity {

    private EditText etBio;
    private TextView saveButton;
    private TextView cancelButton;
    private DatabaseReference databaseReference;
    private String originalBio;
    private String userKey;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_bio);

        etBio = findViewById(R.id.etBio);
        saveButton = findViewById(R.id.save);
        cancelButton = findViewById(R.id.cancel);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        originalBio = getIntent().getStringExtra("currentBio");
        userKey = getIntent().getStringExtra("userKey");
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

        if (originalBio != null) {
            etBio.setText(originalBio);
        }

        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String newBio = etBio.getText().toString().trim();

            if (newBio.length() == 0) {
                Toast.makeText(this, "Tiểu sử không được để trống.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newBio.equals(originalBio)) {
                Toast.makeText(this, "Tiểu sử không thay đổi.", Toast.LENGTH_SHORT).show();
                return;
            }

            updateBioInFirebase(newBio);
        });
    }

    private void updateBioInFirebase(String newBio) {
        if (userKey != null) {
            databaseReference.child(userKey).child("bio").setValue(newBio)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi cập nhật tiểu sử: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
        }
    }
}
