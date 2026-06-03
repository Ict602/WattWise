package com.example.wattwise;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    TextView txtGreeting, txtCurrentAmount, txtCurrentMonth;

    LinearLayout calculateCard, historyCard, aboutCard;
    LinearLayout bottomCalculator, bottomHistory, bottomAbout;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);

        txtGreeting = findViewById(R.id.txtGreeting);
        txtCurrentAmount = findViewById(R.id.txtCurrentAmount);
        txtCurrentMonth = findViewById(R.id.txtCurrentMonth);

        calculateCard = findViewById(R.id.calculateCard);
        historyCard = findViewById(R.id.historyCard);
        aboutCard = findViewById(R.id.aboutCard);

        bottomCalculator = findViewById(R.id.bottomCalculator);
        bottomHistory = findViewById(R.id.bottomHistory);
        bottomAbout = findViewById(R.id.bottomAbout);

        String username = getIntent().getStringExtra("USERNAME");

        if (username != null && !username.isEmpty()) {
            txtGreeting.setText("Hello, " + username + " 👋");
        } else {
            txtGreeting.setText("Hello, User 👋");
        }

        loadLatestBill();

        calculateCard.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, CalculatorActivity.class)));

        bottomCalculator.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, CalculatorActivity.class)));

        historyCard.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, HistoryActivity.class)));

        bottomHistory.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, HistoryActivity.class)));

        aboutCard.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, AboutActivity.class)));

        bottomAbout.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, AboutActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLatestBill();
    }

    private void loadLatestBill() {
        Cursor cursor = databaseHelper.getLatestBill();

        if (cursor != null && cursor.moveToFirst()) {
            String month = cursor.getString(0);
            int year = cursor.getInt(1);
            double finalCost = cursor.getDouble(2);

            txtCurrentAmount.setText(
                    String.format(Locale.US, "RM %.2f", finalCost)
            );

            txtCurrentMonth.setText(month + " " + year);
        } else {
            txtCurrentAmount.setText("No record yet");
            txtCurrentMonth.setText("Tap Calculate Bill to start");
        }

        if (cursor != null) {
            cursor.close();
        }
    }
}