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

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail, etNewPassword, etConfirmPassword;
    Button btnResetPassword;
    TextView txtBackLogin;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        txtBackLogin = findViewById(R.id.txtBackLogin);

        btnResetPassword.setOnClickListener(v -> resetPassword());
        txtBackLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            showAutoDismissDialog(
                    "📧",
                    "Email Required",
                    "Please enter your registered email."
            );
            return;
        }

        if (!databaseHelper.checkEmailExists(email)) {
            showAutoDismissDialog(
                    "❌",
                    "Email Not Found",
                    "This email is not registered."
            );
            return;
        }

        if (newPassword.isEmpty()) {
            showAutoDismissDialog(
                    "🔒",
                    "Password Required",
                    "Please enter your new password."
            );
            return;
        }

        if (confirmPassword.isEmpty()) {
            showAutoDismissDialog(
                    "🔐",
                    "Confirm Password",
                    "Please confirm your new password."
            );
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAutoDismissDialog(
                    "⚠️",
                    "Password Mismatch",
                    "New password and confirm password do not match."
            );
            return;
        }

        boolean updated = databaseHelper.updatePassword(email, newPassword);

        if (updated) {
            showInfoDialog(
                    "✅",
                    "Password Updated",
                    "Your password has been reset successfully.",
                    true
            );
        } else {
            showInfoDialog(
                    "❌",
                    "Reset Failed",
                    "Failed to update password.",
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