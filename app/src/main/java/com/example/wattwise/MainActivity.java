package com.example.wattwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
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
            Toast.makeText(
                    MainActivity.this,
                    "Invalid email or password",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}