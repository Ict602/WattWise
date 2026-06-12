package com.example.wattwise;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

    TextView txtMonthYear, txtUnit, txtRebate, txtTotalCharges, txtFinalCost, txtRebateAmount;
    Button btnEdit, btnDelete, btnBackDashboard;

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
        btnBackDashboard = findViewById(R.id.btnBackDashboard);

        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            showInfoDialog("❌", "Invalid Record", "This bill record is not valid.", true);
            return;
        }

        loadBillDetail();

        btnEdit.setOnClickListener(v -> confirmEdit());
        btnBackDashboard.setOnClickListener(v -> backToDashboard());
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
            txtRebate.setText(rebate + "% Rebate");
            txtTotalCharges.setText(String.format(Locale.US, "RM %.2f", totalCharges));
            txtRebateAmount.setText(String.format(Locale.US, "-RM %.2f", rebateAmount));
            txtFinalCost.setText(String.format(Locale.US, "RM %.2f", finalCost));

        } else {
            showInfoDialog("❌", "Record Not Found", "The selected bill record could not be found.", true);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void confirmEdit() {
        showConfirmDialog(
                "✏️",
                "Edit Record",
                "Do you want to edit this bill record?",
                "Yes",
                "No",
                () -> {
                    Intent intent = new Intent(BillDetailActivity.this, EditBillActivity.class);
                    intent.putExtra("BILL_ID", billId);
                    startActivity(intent);
                }
        );
    }

    private void backToDashboard() {
        Intent intent = new Intent(BillDetailActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmDelete() {
        showConfirmDialog(
                "🗑️",
                "Delete Record",
                "Are you sure you want to delete this bill record?\n\nThis action cannot be undone.",
                "Delete",
                "Cancel",
                () -> deleteRecord()
        );
    }

    private void deleteRecord() {
        boolean deleted = databaseHelper.deleteBill(billId);

        if (deleted) {
            showInfoDialog(
                    "✅",
                    "Record Deleted",
                    "Bill record has been deleted successfully.",
                    true
            );
        } else {
            showInfoDialog(
                    "❌",
                    "Delete Failed",
                    "Failed to delete bill record.",
                    false
            );
        }
    }

    private void showInfoDialog(String icon, String title, String message, boolean closeAfterOk) {
        View view = getLayoutInflater().inflate(R.layout.dialog_wattwise, null);

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

    private void showConfirmDialog(
            String icon,
            String title,
            String message,
            String positiveText,
            String negativeText,
            Runnable positiveAction
    ) {
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm, null);

        TextView txtDialogIcon = view.findViewById(R.id.txtDialogIcon);
        TextView txtDialogTitle = view.findViewById(R.id.txtDialogTitle);
        TextView txtDialogMessage = view.findViewById(R.id.txtDialogMessage);
        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);

        txtDialogIcon.setText(icon);
        txtDialogTitle.setText(title);
        txtDialogMessage.setText(message);
        btnYes.setText(positiveText);
        btnNo.setText(negativeText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            positiveAction.run();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}