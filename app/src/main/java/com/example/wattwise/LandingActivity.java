package com.example.wattwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {
            startActivity(new Intent(
                    LandingActivity.this,
                    MainActivity.class
            ));
        });
    }
}