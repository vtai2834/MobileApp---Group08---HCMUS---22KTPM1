package com.example.tiktok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LogIn extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private DatabaseReference databaseReference;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkUserInFirebase();

            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                intent.putExtra("language", language);
                startActivity(intent);
                finish();
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
            if (!task.isSuccessful() || task.getResult() == null) {
                Toast.makeText(this, "Lỗi khi kiểm tra tài khoản!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean userFound = false;

            for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                String storedUsername = userSnapshot.child("account").getValue(String.class);
                String storedPassword = userSnapshot.child("password").getValue(String.class);

                if (storedUsername != null && storedUsername.equals(username)) {
                    userFound = true;

                    if (storedPassword != null) {
                        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedPassword);
                        if (result.verified) {
                            String userKey = userSnapshot.getKey();
                            String userIdName = userSnapshot.child("idName").getValue(String.class);
                            String name = userSnapshot.child("name").getValue(String.class);
                            String avatar = userSnapshot.child("avatar").getValue(String.class);
                            String bio = userSnapshot.child("bio").getValue(String.class);

                            int followerCount = userSnapshot.child("followerCount").getValue(Integer.class) != null
                                    ? userSnapshot.child("followerCount").getValue(Integer.class) : 0;
                            int followingCount = userSnapshot.child("followingCount").getValue(Integer.class) != null
                                    ? userSnapshot.child("followingCount").getValue(Integer.class) : 0;
                            int likeCount = userSnapshot.child("likeCount").getValue(Integer.class) != null
                                    ? userSnapshot.child("likeCount").getValue(Integer.class) : 0;

                            User currentUser = new User(userKey, username, password, followerCount, followingCount,
                                    likeCount, avatar, name, userIdName, bio);
                            UserManager.getInstance().setCurrentUser(currentUser);

                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.putString("userKey", userKey);
                            editor.putString("userID", userKey);
                            editor.apply();

                            Intent intent = new Intent(this, HomeScreen.class);
                            intent.putExtra("USER_ID", userKey);
                            intent.putExtra("USER_ID_NAME", userIdName);
                            intent.putExtra("language", language);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }

            if (!userFound) {
                Toast.makeText(this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
