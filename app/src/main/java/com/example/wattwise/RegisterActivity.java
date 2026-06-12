package com.example.wattwise;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView txtBackLogin;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtBackLogin = findViewById(R.id.txtBackLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        txtBackLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullname = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fullname.isEmpty()) {
            showAutoDismissDialog(
                    "👤",
                    "Full Name Required",
                    "Please enter your full name."
            );
            return;
        }

        if (email.isEmpty()) {
            showAutoDismissDialog(
                    "📧",
                    "Email Required",
                    "Please enter your email address."
            );
            return;
        }

        if (password.isEmpty()) {
            showAutoDismissDialog(
                    "🔒",
                    "Password Required",
                    "Please enter your password."
            );
            return;
        }

        if (confirmPassword.isEmpty()) {
            showAutoDismissDialog(
                    "🔐",
                    "Confirm Password",
                    "Please confirm your password."
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAutoDismissDialog(
                    "⚠️",
                    "Password Mismatch",
                    "Password and Confirm Password do not match."
            );
            return;
        }

        if (databaseHelper.checkEmailExists(email)) {
            showAutoDismissDialog(
                    "❌",
                    "Email Already Registered",
                    "This email address has already been used."
            );
            return;
        }

        boolean inserted = databaseHelper.insertUser(fullname, email, password);

        if (inserted) {
            showInfoDialog(
                    "✅",
                    "Account Created",
                    "Your account has been created successfully.",
                    true
            );
        } else {
            showInfoDialog(
                    "❌",
                    "Registration Failed",
                    "Unable to create your account. Please try again.",
                    false
            );
        }
    }

    private void showAutoDismissDialog(String icon, String title, String message) {
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

        btnDialogOk.setVisibility(View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 1800);
    }

    private void showInfoDialog(
            String icon,
            String title,
            String message,
            boolean closeAfterOk
    ) {
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

        btnDialogOk.setOnClickListener(v -> {
            dialog.dismiss();

            if (closeAfterOk) {
                finish();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}