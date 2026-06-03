package com.example.wattwise;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

    TextView txtMonthYear, txtUnit, txtRebate, txtTotalCharges, txtFinalCost, txtRebateAmount;
    Button btnEdit, btnDelete;

    DatabaseHelper databaseHelper;
    int billId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        databaseHelper = new DatabaseHelper(this);

        txtMonthYear = findViewById(R.id.txtMonthYear);
        txtUnit = findViewById(R.id.txtUnit);
        txtRebate = findViewById(R.id.txtRebate);
        txtTotalCharges = findViewById(R.id.txtTotalCharges);
        txtFinalCost = findViewById(R.id.txtFinalCost);
        txtRebateAmount = findViewById(R.id.txtRebateAmount);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            Toast.makeText(this, "Invalid record", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBillDetail();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(BillDetailActivity.this, EditBillActivity.class);
            intent.putExtra("BILL_ID", billId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBillDetail();
    }

    private void loadBillDetail() {
        Cursor cursor = databaseHelper.getBillById(billId);

        if (cursor != null && cursor.moveToFirst()) {
            String month = cursor.getString(1);
            int year = cursor.getInt(2);
            int unit = cursor.getInt(3);
            int rebate = cursor.getInt(4);
            double totalCharges = cursor.getDouble(5);
            double finalCost = cursor.getDouble(6);

            double rebateAmount = totalCharges - finalCost;
            rebateAmount = Math.round(rebateAmount * 100.0) / 100.0;

            txtMonthYear.setText(month + " " + year);
            txtUnit.setText(unit + " kWh");
            txtRebate.setText(rebate + "%");
            txtTotalCharges.setText(String.format(Locale.US, "RM %.2f", totalCharges));
            txtRebateAmount.setText(String.format(Locale.US, "-RM %.2f", rebateAmount));
            txtFinalCost.setText(String.format(Locale.US, "RM %.2f", finalCost));
        } else {
            Toast.makeText(this, "Record not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this bill record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteBill(billId);

                    if (deleted) {
                        Toast.makeText(this, "Record deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete record", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}