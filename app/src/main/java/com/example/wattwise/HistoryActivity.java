package com.example.wattwise;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ListView listHistory;
    TextView txtEmpty, txtTotalBills, txtAverageBill;

    DatabaseHelper databaseHelper;

    ArrayList<Integer> billIds;
    ArrayList<String> months;
    ArrayList<Integer> years;
    ArrayList<Integer> units;
    ArrayList<Integer> rebates;
    ArrayList<Double> costs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listHistory = findViewById(R.id.listHistory);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtTotalBills = findViewById(R.id.txtTotalBills);
        txtAverageBill = findViewById(R.id.txtAverageBill);

        databaseHelper = new DatabaseHelper(this);

        billIds = new ArrayList<>();
        months = new ArrayList<>();
        years = new ArrayList<>();
        units = new ArrayList<>();
        rebates = new ArrayList<>();
        costs = new ArrayList<>();

        loadSummary();
        loadHistory();

        listHistory.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HistoryActivity.this, BillDetailActivity.class);
            intent.putExtra("BILL_ID", billIds.get(position));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
        loadHistory();
    }

    private void loadSummary() {
        Cursor cursor = databaseHelper.getHistorySummary();

        if (cursor != null && cursor.moveToFirst()) {
            int totalBills = cursor.getInt(0);
            double averageBill = cursor.getDouble(1);

            txtTotalBills.setText(String.valueOf(totalBills));

            if (totalBills > 0) {
                txtAverageBill.setText(String.format(Locale.US, "RM %.2f", averageBill));
            } else {
                txtAverageBill.setText("RM 0.00");
            }
        } else {
            txtTotalBills.setText("0");
            txtAverageBill.setText("RM 0.00");
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void loadHistory() {
        billIds.clear();
        months.clear();
        years.clear();
        units.clear();
        rebates.clear();
        costs.clear();

        Cursor cursor = databaseHelper.getAllBills();

        if (cursor != null && cursor.getCount() > 0) {
            txtEmpty.setText("");

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String month = cursor.getString(1);
                int year = cursor.getInt(2);
                int unit = cursor.getInt(3);
                int rebate = cursor.getInt(4);
                double finalCost = cursor.getDouble(5);

                billIds.add(id);
                months.add(month);
                years.add(year);
                units.add(unit);
                rebates.add(rebate);
                costs.add(finalCost);
            }

            cursor.close();

        } else {
            txtEmpty.setText("No saved record yet.");
        }

        HistoryAdapter adapter = new HistoryAdapter(
                this,
                billIds,
                months,
                years,
                units,
                rebates,
                costs
        );

        listHistory.setAdapter(adapter);
    }
}