package com.example.tiktok;

public class User {
    public String account;
    public String password;

    public User() {
        // Bắt buộc để Firebase sử dụng
    }

    public User(String account, String password) {
        this.account = account;
        this.password = password;
    }
}
