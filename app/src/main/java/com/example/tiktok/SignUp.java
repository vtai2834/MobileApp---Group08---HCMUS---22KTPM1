package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignUp extends AppCompatActivity {

    private EditText edtAccount, edtPassword;
    private Button btnContinue;
    private DatabaseReference databaseReference;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtAccount = findViewById(R.id.account);
        edtPassword = findViewById(R.id.password);
        btnContinue = findViewById(R.id.btnContinue);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserToFirebase();
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveUserToFirebase() {
        String account = edtAccount.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String bio = "";
        String name = account;
        String idName = account;
        String avatar = "android.resource://" + getPackageName() + "/" + R.drawable.default_profile;
        int followerCount = 0;
        int followingCount = 0;
        int likeCount = 0;
        Map<String, Boolean> followers = new HashMap<>();
        Map<String, Boolean> following = new HashMap<>();

        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ và số!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean accountExists = false;

                if (task.getResult() != null) {
                    for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                        String existingAccount = userSnapshot.child("account").getValue(String.class);
                        if (existingAccount != null && existingAccount.equals(account)) {
                            accountExists = true;
                            break;
                        }
                    }
                }

                if (accountExists) {
                    Toast.makeText(SignUp.this, "Tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
                } else {
                    String userId = databaseReference.push().getKey();

                    // Hash password trước khi lưu
                    String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                    User user = new User(account, hashedPassword, followerCount, followingCount, likeCount, avatar, name, idName, bio, followers, following);

                    databaseReference.child(userId).setValue(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SignUp.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUp.this, SignIn.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(SignUp.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(SignUp.this, "Lỗi khi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
