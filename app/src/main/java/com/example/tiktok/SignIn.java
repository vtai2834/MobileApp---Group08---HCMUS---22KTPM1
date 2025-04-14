package com.example.tiktok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SignIn extends AppCompatActivity {

    private TextView tvLoginUser, tvSignUp;
    private Spinner spinnerLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        tvLoginUser = findViewById(R.id.tvLoginUser);
        tvSignUp = findViewById(R.id.tvSignUp);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        int savedPosition = preferences.getInt("lang_position", 0);
        spinnerLanguage.setSelection(savedPosition);

        spinnerLanguage.post(() -> {
            String selected = spinnerLanguage.getSelectedItem().toString();
            if (selected.equals("Tiếng Anh")) {
                setLocale("en");
            }
        });

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelect = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelect) {
                    isFirstSelect = false;
                    return;
                }

                if (position == 0) {
                    setLocale("vi");
                } else if (position == 1) {
                    setLocale("en");
                }

                SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("lang_position", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tvLoginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, LogIn.class);
                Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
                String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                intent.putExtra("language", selectedLanguage);
                startActivity(intent);
                finish();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
                String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                intent.putExtra("language", selectedLanguage);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
