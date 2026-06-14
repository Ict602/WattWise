package com.example.wattwise;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class CalculatorActivity extends AppCompatActivity {

    RadioButton rbJan, rbFeb, rbMar, rbApr, rbMay, rbJun;
    RadioButton rbJul, rbAug, rbSep, rbOct, rbNov, rbDec;

    EditText etUnit;
    SeekBar seekRebate;

    TextView txtRebateLabel, txtTotalCharges, txtFinalCost, txtFinalNote;

    Button btnCalculate, btnReset, btnSave, btnHistory, btnAbout, btnLogout;

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
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        rbJan = findViewById(R.id.rbJan);
        rbFeb = findViewById(R.id.rbFeb);
        rbMar = findViewById(R.id.rbMar);
        rbApr = findViewById(R.id.rbApr);
        rbMay = findViewById(R.id.rbMay);
        rbJun = findViewById(R.id.rbJun);
        rbJul = findViewById(R.id.rbJul);
        rbAug = findViewById(R.id.rbAug);
        rbSep = findViewById(R.id.rbSep);
        rbOct = findViewById(R.id.rbOct);
        rbNov = findViewById(R.id.rbNov);
        rbDec = findViewById(R.id.rbDec);

        etUnit = findViewById(R.id.etUnit);
        seekRebate = findViewById(R.id.seekRebate);

        txtRebateLabel = findViewById(R.id.txtRebateLabel);
        txtTotalCharges = findViewById(R.id.txtTotalCharges);
        txtFinalCost = findViewById(R.id.txtFinalCost);
        txtFinalNote = findViewById(R.id.txtFinalNote);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);
        btnHistory = findViewById(R.id.btnHistory);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);

        rbJan.setChecked(true);
        selectedMonth = "January";

        setupMonthSelection();
        setupRebateSlider();

        btnCalculate.setOnClickListener(v -> calculateBill());
        btnSave.setOnClickListener(v -> confirmSaveRecord());
        btnReset.setOnClickListener(v -> confirmResetForm());

        btnHistory.setOnClickListener(v -> {
            showAutoDismissDialog("📋", "Opening History", "Loading saved bill records...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(CalculatorActivity.this, HistoryActivity.class));
            }, 900);
        });

        btnAbout.setOnClickListener(v -> {
            showAutoDismissDialog("ℹ️", "Opening About", "Loading application information...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(CalculatorActivity.this, AboutActivity.class));
            }, 900);
        });

        btnLogout.setOnClickListener(v -> confirmLogout());
    }
    private void setupMonthSelection() {
        RadioButton[] monthButtons = {
                rbJan, rbFeb, rbMar, rbApr, rbMay, rbJun,
                rbJul, rbAug, rbSep, rbOct, rbNov, rbDec
        };

        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        for (int i = 0; i < monthButtons.length; i++) {
            int index = i;

            monthButtons[i].setOnClickListener(v -> {
                for (RadioButton rb : monthButtons) {
                    rb.setChecked(false);
                }

                monthButtons[index].setChecked(true);
                selectedMonth = monthNames[index];
                isCalculated = false;

            });
        }
    }

    private void setupRebateSlider() {
        seekRebate.setMax(5);
        seekRebate.setProgress(0);
        txtRebateLabel.setText("Rebate: 0%");

        seekRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRebate = progress;
                txtRebateLabel.setText("Selected Rebate: " + selectedRebate + "%");
                isCalculated = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    private void calculateBill() {

        String unitText = etUnit.getText().toString().trim();

        if (selectedMonth.isEmpty()) {

            showInfoDialog(
                    "⚠️",
                    "Month Required",
                    "Please select a month first.",
                    false
            );

            return;
        }

        if (unitText.isEmpty()) {

            showInfoDialog(
                    "⚠️",
                    "Input Required",
                    "Please enter electricity unit used.",
                    false
            );

            etUnit.requestFocus();
            return;
        }

        try {

            unit = Integer.parseInt(unitText);

        } catch (Exception e) {

            showInfoDialog(
                    "⚠️",
                    "Invalid Input",
                    "Please enter numbers only.",
                    false
            );

            etUnit.setText("0");
            etUnit.requestFocus();

            return;
        }

        if (unit < 1 || unit > 1000) {

            showInfoDialog(
                    "⚠️",
                    "Invalid Consumption",
                    "Electricity consumption must be between 1 and 1000 kWh.",
                    false
            );

            etUnit.setText("0");
            etUnit.requestFocus();

            return;
        }

        totalCharges = calculateTotalCharges(unit);
        totalCharges = Math.round(totalCharges * 100.0) / 100.0;

        finalCost = totalCharges - (totalCharges * selectedRebate / 100.0);
        finalCost = Math.round(finalCost * 100.0) / 100.0;

        txtTotalCharges.setText(
                String.format(Locale.US, "RM %.2f", totalCharges)
        );

        txtFinalCost.setText(
                String.format(Locale.US, "RM %.2f", finalCost)
        );

        txtFinalNote.setText(
                "After " + selectedRebate + "% rebate"
        );

        isCalculated = true;

        showInfoDialog(
                "✅",
                "Calculation Complete",
                "Total Charges: RM "
                        + String.format(Locale.US, "%.2f", totalCharges)
                        + "\nFinal Cost: RM "
                        + String.format(Locale.US, "%.2f", finalCost),
                false
        );
    }

    private double calculateTotalCharges(int unit) {
        if (unit <= 200) {
            return unit * 0.218;
        } else if (unit <= 300) {
            return (200 * 0.218) + ((unit - 200) * 0.334);
        } else if (unit <= 600) {
            return (200 * 0.218)
                    + (100 * 0.334)
                    + ((unit - 300) * 0.516);
        } else {
            return (200 * 0.218)
                    + (100 * 0.334)
                    + (300 * 0.516)
                    + ((unit - 600) * 0.546);
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
                "Save",
                "Cancel",
                this::saveRecord
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
                    "Your electricity bill record has been saved successfully.\n\n"
                            + "Month: " + selectedMonth + "\n"
                            + "Final Cost: RM " + String.format(Locale.US, "%.2f", finalCost),
                    false
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
                "Reset",
                "Cancel",
                this::resetForm
        );
    }

    private void resetForm() {
        rbJan.setChecked(true);
        selectedMonth = "January";

        RadioButton[] monthButtons = {
                rbFeb, rbMar, rbApr, rbMay, rbJun,
                rbJul, rbAug, rbSep, rbOct, rbNov, rbDec
        };

        for (RadioButton rb : monthButtons) {
            rb.setChecked(false);
        }

        etUnit.setText("");
        seekRebate.setProgress(0);
        selectedRebate = 0;
        txtRebateLabel.setText("Rebate: 0%");

        txtTotalCharges.setText("RM 0.00");
        txtFinalCost.setText("RM 0.00");
        txtFinalNote.setText("After rebate");

        totalCharges = 0.0;
        finalCost = 0.0;
        unit = 0;
        isCalculated = false;

        etUnit.clearFocus();

        showAutoDismissDialog(
                "✅",
                "Form Reset",
                "All fields have been cleared."
        );
    }

    private void confirmLogout() {
        showConfirmDialog(
                "🚪",
                "Logout WattWise",
                "Are you sure you want to logout from WattWise?",
                "Logout",
                "Cancel",
                () -> {
                    Intent intent = new Intent(CalculatorActivity.this, LandingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
        );
    }
    private void showAutoDismissDialog(String icon, String title, String message) {
        View view = getLayoutInflater().inflate(R.layout.dialog_wattwise, null);

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
        }, 1300);
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
        btnDialogOk.setVisibility(View.VISIBLE);

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
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
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