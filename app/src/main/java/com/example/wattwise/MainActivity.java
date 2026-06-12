package com.example.wattwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    LinearLayout btnGoogle;
    TextView txtRegister, txtForgotPassword;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("USERNAME", "Google User");
            intent.putExtra("EMAIL", "googleuser@gmail.com");
            startActivity(intent);
            finish();
        });

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            showWattWiseDialog(
                    "📧",
                    "Email Required",
                    "Please enter your email address."
            );
            return;
        }

        if (password.isEmpty()) {
            showWattWiseDialog(
                    "🔒",
                    "Password Required",
                    "Please enter your password."
            );
            return;
        }

        boolean loginSuccess = databaseHelper.checkLogin(email, password);

        if (loginSuccess) {
            String username = databaseHelper.getUserName(email);

            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();

        } else {
            showWattWiseDialog(
                    "❌",
                    "Login Failed",
                    "Invalid email or password.\nPlease try again."
            );
        }
    }

    private void showWattWiseDialog(String icon, String title, String message) {

        View view = getLayoutInflater().inflate(
                R.layout.dialog_wattwise,
                null
        );

        TextView txtDialogIcon = view.findViewById(R.id.txtDialogIcon);
        TextView txtDialogTitle = view.findViewById(R.id.txtDialogTitle);
        TextView txtDialogMessage = view.findViewById(R.id.txtDialogMessage);
        Button btnDialogOk = view.findViewById(R.id.btnDialogOk);

        txtDialogIcon.setText(icon);
        txtDialogTitle.setText(title);
        txtDialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnDialogOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}