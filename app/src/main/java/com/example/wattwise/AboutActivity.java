package com.example.wattwise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    LinearLayout btnGithub;
    TextView txtGithub;

    String githubUrl = "https://github.com/Ict602/WattWise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btnGithub = findViewById(R.id.btnGithub);
        txtGithub = findViewById(R.id.txtGithub);

        txtGithub.setText(githubUrl);

        btnGithub.setOnClickListener(v -> openGithub());
        txtGithub.setOnClickListener(v -> openGithub());
    }

    private void openGithub() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                    AboutActivity.this,
                    "Unable to open GitHub link",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}