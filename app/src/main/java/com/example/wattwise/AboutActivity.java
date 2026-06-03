package com.example.wattwise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    LinearLayout btnGithub, btnLogout;
    TextView txtGithub;

    String githubUrl = "https://github.com/Ict602/WattWise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btnGithub = findViewById(R.id.btnGithub);
        btnLogout = findViewById(R.id.btnLogout);
        txtGithub = findViewById(R.id.txtGithub);

        txtGithub.setText(githubUrl);

        btnGithub.setOnClickListener(v -> openGithub());
        txtGithub.setOnClickListener(v -> openGithub());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void openGithub() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open GitHub link", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AboutActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}