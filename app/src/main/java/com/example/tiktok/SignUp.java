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

public class SignUp extends AppCompatActivity {

    private EditText edtAccount, edtPassword;
    private Button btnContinue;
    private DatabaseReference databaseReference;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Ánh xạ View
        edtAccount = findViewById(R.id.account);
        edtPassword = findViewById(R.id.password);
        btnContinue = findViewById(R.id.btnContinue);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserToFirebase();
            }
        });

        // Xử lý sự kiện nhấn vào "Đăng ký" để chuyển đến màn hình đăng nhập
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
                finish(); // Đóng màn hình đăng ký
            }
        });
    }

    private void saveUserToFirebase() {
        String account = edtAccount.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu có đúng định dạng không
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ và số!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem tài khoản đã tồn tại
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                boolean accountExists = false;

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String existingAccount = userSnapshot.child("account").getValue(String.class);
                    if (existingAccount != null && existingAccount.equals(account)) {
                        accountExists = true;
                        break;
                    }
                }

                if (accountExists) {
                    Toast.makeText(SignUp.this, "Tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
                } else {
                    // Nếu tài khoản chưa tồn tại, thực hiện đăng ký
                    String userId = databaseReference.push().getKey();
                    User user = new User(account, password);
                    databaseReference.child(userId).setValue(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SignUp.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                                // Chuyển sang SignIn.java sau khi đăng ký thành công
                                Intent intent = new Intent(SignUp.this, SignIn.class);
                                startActivity(intent);
                                finish(); // Đóng màn hình SignUp
                            })
                            .addOnFailureListener(e -> Toast.makeText(SignUp.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(SignUp.this, "Lỗi khi kiểm tra tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
