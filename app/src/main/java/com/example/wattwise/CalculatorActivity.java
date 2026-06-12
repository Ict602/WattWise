package com.example.wattwise;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class CalculatorActivity extends AppCompatActivity {

    Spinner spMonth, spRebate;
    EditText etUnit;
    Button btnCalculate, btnReset, btnSave;
    TextView txtTotalCharges, txtFinalCost, txtFinalNote;

    DatabaseHelper databaseHelper;

    double totalCharges = 0.0;
    double finalCost = 0.0;
    int selectedRebate = 0;
    int unit = 0;
    int currentYear = 0;
    String selectedMonth = "";

    boolean isCalculated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        databaseHelper = new DatabaseHelper(this);

        spMonth = findViewById(R.id.spMonth);
        spRebate = findViewById(R.id.spRebate);
        etUnit = findViewById(R.id.etUnit);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);

        txtTotalCharges = findViewById(R.id.txtTotalCharges);
        txtFinalCost = findViewById(R.id.txtFinalCost);
        txtFinalNote = findViewById(R.id.txtFinalNote);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        setupSpinners();

        btnCalculate.setOnClickListener(v -> calculateBill());
        btnSave.setOnClickListener(v -> confirmSaveRecord());
        btnReset.setOnClickListener(v -> confirmResetForm());
    }

    private void setupSpinners() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        String[] rebates = {"0%", "1%", "2%", "3%", "4%", "5%"};

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected,
                months
        );
        monthAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spMonth.setAdapter(monthAdapter);

        ArrayAdapter<String> rebateAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected,
                rebates
        );
        rebateAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spRebate.setAdapter(rebateAdapter);
    }

    private void calculateBill() {
        String unitText = etUnit.getText().toString().trim();

        if (unitText.isEmpty()) {
            showAutoDismissDialog(
                    "⚠️",
                    "Input Required",
                    "Please enter electricity unit used."
            );
            etUnit.requestFocus();
            return;
        }

        try {
            unit = Integer.parseInt(unitText);
        } catch (Exception e) {
            showAutoDismissDialog(
                    "⚠️",
                    "Invalid Input",
                    "Please enter a valid number."
            );
            etUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            showAutoDismissDialog(
                    "⚠️",
                    "Invalid Range",
                    "Unit must be between 1 and 1000 kWh."
            );
            etUnit.requestFocus();
            return;
        }

        selectedMonth = spMonth.getSelectedItem().toString();
        selectedRebate = spRebate.getSelectedItemPosition();

        totalCharges = calculateTotalCharges(unit);
        totalCharges = Math.round(totalCharges * 100.0) / 100.0;

        finalCost = totalCharges - (totalCharges * selectedRebate / 100.0);
        finalCost = Math.round(finalCost * 100.0) / 100.0;

        txtTotalCharges.setText(String.format(Locale.US, "RM %.2f", totalCharges));
        txtFinalCost.setText(String.format(Locale.US, "RM %.2f", finalCost));
        txtFinalNote.setText("After " + selectedRebate + "% rebate");

        isCalculated = true;

        showAutoDismissDialog(
                "✅",
                "Calculation Complete",
                "Electricity bill calculated successfully."
        );
    }

    private double calculateTotalCharges(int unit) {
        if (unit <= 200) {
            return unit * 0.218;
        } else if (unit <= 300) {
            return (200 * 0.218) + ((unit - 200) * 0.334);
        } else if (unit <= 600) {
            return (200 * 0.218) + (100 * 0.334) + ((unit - 300) * 0.516);
        } else {
            return (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((unit - 600) * 0.546);
        }
    }

    private void confirmSaveRecord() {
        if (!isCalculated) {
            showAutoDismissDialog(
                    "⚠️",
                    "Calculation Required",
                    "Please calculate the bill before saving."
            );
            return;
        }

        showConfirmDialog(
                "💾",
                "Save Record",
                "Are you sure you want to save this bill record?",
                "Yes",
                "No",
                () -> saveRecord()
        );
    }

    private void saveRecord() {
        boolean inserted = databaseHelper.insertBill(
                selectedMonth,
                currentYear,
                unit,
                selectedRebate,
                totalCharges,
                finalCost
        );

        if (inserted) {
            showInfoDialog(
                    "✅",
                    "Record Saved",
                    "Your electricity bill record has been saved successfully.\n\n" +
                            "Month: " + selectedMonth + "\n" +
                            "Total Charges: RM " + String.format(Locale.US, "%.2f", totalCharges) + "\n" +
                            "Final Cost: RM " + String.format(Locale.US, "%.2f", finalCost),
                    true
            );
        } else {
            showInfoDialog(
                    "❌",
                    "Save Failed",
                    "Unable to save bill record.",
                    false
            );
        }
    }

    private void confirmResetForm() {
        showConfirmDialog(
                "🔄",
                "Reset Form",
                "Are you sure you want to clear all fields?",
                "Yes",
                "No",
                () -> resetForm()
        );
    }

    private void resetForm() {
        etUnit.setText("");
        spMonth.setSelection(0);
        spRebate.setSelection(0);

        txtTotalCharges.setText("RM 0.00");
        txtFinalCost.setText("RM 0.00");
        txtFinalNote.setText("After rebate");

        totalCharges = 0.0;
        finalCost = 0.0;
        selectedRebate = 0;
        unit = 0;
        selectedMonth = "";
        isCalculated = false;

        etUnit.clearFocus();

        showAutoDismissDialog(
                "✅",
                "Form Reset",
                "All fields have been cleared."
        );
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

    private void showConfirmDialog(
            String icon,
            String title,
            String message,
            String positiveText,
            String negativeText,
            Runnable positiveAction
    ) {
        View view = getLayoutInflater().inflate(
                R.layout.dialog_confirm,
                null
        );

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