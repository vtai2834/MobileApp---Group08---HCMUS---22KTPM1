package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import android.util.Log;


public class SendCode extends AppCompatActivity {

    private EditText[] otpInputs;
    private TextView txtResendCode;
    private Button btnVerify;
    private String verificationId;
    private String phoneNumber;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_code);

        mAuth = FirebaseAuth.getInstance();

        otpInputs = new EditText[]{
                findViewById(R.id.otp1),
                findViewById(R.id.otp2),
                findViewById(R.id.otp3),
                findViewById(R.id.otp4),
                findViewById(R.id.otp5),
                findViewById(R.id.otp6)
        };
        txtResendCode = findViewById(R.id.txt_resend_code);
        btnVerify = findViewById(R.id.btn_verify);

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        btnVerify.setOnClickListener(v -> verifyCode());

        txtResendCode.setOnClickListener(v -> resendVerificationCode());
    }

    private void verifyCode() {
        StringBuilder code = new StringBuilder();
        for (EditText otpInput : otpInputs) {
            if (TextUtils.isEmpty(otpInput.getText().toString().trim())) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            code.append(otpInput.getText().toString().trim());
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code.toString());
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        Log.d("OTP_DEBUG", "OTP Entered: " + credential.getSmsCode()); // Log mã OTP
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SendCode.this, "Xác minh thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SendCode.this, HomeScreen.class));
                        finish();
                    } else {
                        Toast.makeText(SendCode.this, "Mã OTP không đúng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void resendVerificationCode() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(SendCode.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        Toast.makeText(SendCode.this, "Mã OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
