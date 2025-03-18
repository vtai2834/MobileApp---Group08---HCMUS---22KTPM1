package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignIn extends AppCompatActivity {

    private TextView tvLoginUser, tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        tvLoginUser = findViewById(R.id.tvLoginUser);
        tvSignUp = findViewById(R.id.tvSignUp);

        // Xử lý sự kiện nhấn vào "Sử dụng tên người dùng" để chuyển đến màn hình Login
        tvLoginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, LogIn.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện nhấn vào "Đăng ký" để chuyển đến màn hình SignUp
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}
