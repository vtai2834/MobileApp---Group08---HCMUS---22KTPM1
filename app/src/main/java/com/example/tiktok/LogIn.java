package com.example.tiktok;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LogIn extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Ánh xạ View
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Xử lý sự kiện khi nhấn nút đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserInFirebase();
            }
        });

        // Xử lý sự kiện nhấn vào "Đăng ký" để chuyển đến màn hình SignUp
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    private void checkUserInFirebase() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean userFound = false;
                String userID = "";

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    String storedPassword = userSnapshot.child("password").getValue(String.class);

                    if (storedUsername != null && storedUsername.equals(username)) {
                        userFound = true;
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Lấy thông tin user từ Firebase
                            userID = userSnapshot.getKey();
                            String userIdName = userSnapshot.child("idName").getValue(String.class);
                            String name = userSnapshot.child("name").getValue(String.class);
                            String avatar = userSnapshot.child("avatar").getValue(String.class);
                            String bio = userSnapshot.child("bio").getValue(String.class);
                            int followerCount = 0;
                            int followingCount = 0;
                            int likeCount = 0;

                            if (userSnapshot.child("followerCount").exists()) {
                                followerCount = userSnapshot.child("followerCount").getValue(Integer.class);
                            }
                            if (userSnapshot.child("followingCount").exists()) {
                                followingCount = userSnapshot.child("followingCount").getValue(Integer.class);
                            }
                            if (userSnapshot.child("likeCount").exists()) {
                                likeCount = userSnapshot.child("likeCount").getValue(Integer.class);
                            }

                            // Tạo đối tượng User và lưu vào UserManager
                            User currentUser = new User(userID ,username, password, followerCount, followingCount,
                                    likeCount, avatar, name, userIdName, bio);
                            UserManager.getInstance().setCurrentUser(currentUser);

                            // Lưu username vào SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.putString("userID", userID);
                            editor.apply();

                            Toast.makeText(LogIn.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // Chuyển sang HomeScreen
                            Intent intent = new Intent(LogIn.this, HomeScreen.class);
//                            intent.putExtra("USER_ID", userID);
//                            intent.putExtra("USER_ID_NAME", userIdName);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LogIn.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
                if (!userFound) {
                    Toast.makeText(LogIn.this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LogIn.this, "Lỗi khi kiểm tra tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

