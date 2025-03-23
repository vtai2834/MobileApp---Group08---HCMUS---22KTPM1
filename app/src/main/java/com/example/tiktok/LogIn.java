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

public class LogIn extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private DatabaseReference databaseReference;

    private TextView tvSignUp;
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

        // Kiểm tra tài khoản trong Firebase
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                boolean loginSuccess = false;
                String userID = ""; // lưu userID để truy cập dữ liệu ng dùng trong ứng dụng

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String existingUsername = userSnapshot.child("account").getValue(String.class);
                    String existingPassword = userSnapshot.child("password").getValue(String.class);

                    if (existingUsername != null && existingPassword != null
                            && existingUsername.equals(username) && existingPassword.equals(password)) {
                        loginSuccess = true;
                        userID = userSnapshot.getKey();
                        break;
                    }
                }

                if (loginSuccess) {
                    Toast.makeText(LogIn.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình HomeScreen
                    Intent intent = new Intent(LogIn.this, HomeScreen.class);
                    intent.putExtra("USER_ID", userID);
                    startActivity(intent);
                    finish(); // Đóng màn hình đăng nhập
                } else {
                    Toast.makeText(LogIn.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LogIn.this, "Lỗi khi kiểm tra tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
