package com.example.wattwise;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class EditBillActivity extends AppCompatActivity {

    RadioButton rbJan, rbFeb, rbMar, rbApr, rbMay, rbJun;
    RadioButton rbJul, rbAug, rbSep, rbOct, rbNov, rbDec;

    EditText etUnit;
    SeekBar seekRebate;

    Button btnUpdate, btnCancel;

    TextView txtMonthBadge, txtCurrentRecord, txtCurrentCost;
    TextView txtNewCost, txtRebateLabel;

    DatabaseHelper databaseHelper;

    int billId = -1;
    int currentYear = 0;
    int unit = 0;
    int rebate = 0;

    String month = "";

    double totalCharges = 0.0;
    double finalCost = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);

        databaseHelper = new DatabaseHelper(this);

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

        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        txtMonthBadge = findViewById(R.id.txtMonthBadge);
        txtCurrentRecord = findViewById(R.id.txtCurrentRecord);
        txtCurrentCost = findViewById(R.id.txtCurrentCost);
        txtNewCost = findViewById(R.id.txtNewCost);
        txtRebateLabel = findViewById(R.id.txtRebateLabel);

        setupMonthSelection();
        setupRebateSlider();

        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            showInfoDialog("❌", "Invalid Record", "This bill record is not valid.", true);
            return;
        }

        loadExistingData();
        updatePreview();

        etUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnUpdate.setOnClickListener(v -> confirmUpdate());

        btnCancel.setOnClickListener(v -> {
            showConfirmDialog(
                    "↩️",
                    "Cancel Edit",
                    "Are you sure you want to cancel editing this record?",
                    "Yes",
                    "No",
                    () -> finish()
            );
        });
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
                month = monthNames[index];

                txtMonthBadge.setText(month.substring(0, 3).toUpperCase());
                txtCurrentRecord.setText(month + " " + currentYear);

                updatePreview();
            });
        }
    }

    private void setupRebateSlider() {
        seekRebate.setMax(5);
        seekRebate.setProgress(0);
        txtRebateLabel.setText("Selected Rebate: 0%");

        seekRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebate = progress;
                txtRebateLabel.setText("Selected Rebate: " + rebate + "%");
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void loadExistingData() {
        Cursor cursor = databaseHelper.getBillById(billId);

        if (cursor != null && cursor.moveToFirst()) {
            month = cursor.getString(1);
            currentYear = cursor.getInt(2);
            unit = cursor.getInt(3);
            rebate = cursor.getInt(4);
            double oldFinalCost = cursor.getDouble(6);

            txtMonthBadge.setText(month.substring(0, 3).toUpperCase());
            txtCurrentRecord.setText(month + " " + currentYear);
            txtCurrentCost.setText(
                    String.format(Locale.US, "RM %.2f", oldFinalCost)
            );

            etUnit.setText(String.valueOf(unit));
            checkSelectedMonth(month);

            seekRebate.setProgress(rebate);
            txtRebateLabel.setText("Selected Rebate: " + rebate + "%");

        } else {
            showInfoDialog(
                    "❌",
                    "Record Not Found",
                    "The selected bill record could not be found.",
                    true
            );
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void checkSelectedMonth(String selectedMonth) {
        rbJan.setChecked(selectedMonth.equals("January"));
        rbFeb.setChecked(selectedMonth.equals("February"));
        rbMar.setChecked(selectedMonth.equals("March"));
        rbApr.setChecked(selectedMonth.equals("April"));
        rbMay.setChecked(selectedMonth.equals("May"));
        rbJun.setChecked(selectedMonth.equals("June"));
        rbJul.setChecked(selectedMonth.equals("July"));
        rbAug.setChecked(selectedMonth.equals("August"));
        rbSep.setChecked(selectedMonth.equals("September"));
        rbOct.setChecked(selectedMonth.equals("October"));
        rbNov.setChecked(selectedMonth.equals("November"));
        rbDec.setChecked(selectedMonth.equals("December"));
    }

    private void updatePreview() {
        String unitText = etUnit.getText().toString().trim();

        if (unitText.isEmpty()) {
            txtNewCost.setText("RM 0.00");
            return;
        }

        int previewUnit;

        try {
            previewUnit = Integer.parseInt(unitText);
        } catch (Exception e) {
            txtNewCost.setText("RM 0.00");
            return;
        }

        if (previewUnit < 1 || previewUnit > 1000) {
            txtNewCost.setText("RM 0.00");
            return;
        }

        double previewTotal = calculateTotalCharges(previewUnit);
        previewTotal = Math.round(previewTotal * 100.0) / 100.0;

        double previewFinal = previewTotal - (previewTotal * rebate / 100.0);
        previewFinal = Math.round(previewFinal * 100.0) / 100.0;

        txtNewCost.setText(
                String.format(Locale.US, "RM %.2f", previewFinal)
        );
    }

    private void confirmUpdate() {
        showConfirmDialog(
                "📝",
                "Update Record",
                "Are you sure you want to update this bill record?",
                "Update",
                "Cancel",
                this::updateRecord
        );
    }

    private void updateRecord() {
        String unitText = etUnit.getText().toString().trim();

        if (month.isEmpty()) {
            showInfoDialog(
                    "⚠️",
                    "Month Required",
                    "Please select a month.",
                    false
            );
            return;
        }

        if (unitText.isEmpty()) {
            showInfoDialog(
                    "⚠️",
                    "Unit Required",
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
                    "Invalid Unit",
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
                    "Electricity consumption must be between 1 and 1000 kWh only.",
                    false
            );
            etUnit.setText("0");
            etUnit.requestFocus();
            return;
        }

        totalCharges = calculateTotalCharges(unit);
        totalCharges = Math.round(totalCharges * 100.0) / 100.0;

        finalCost = totalCharges - (totalCharges * rebate / 100.0);
        finalCost = Math.round(finalCost * 100.0) / 100.0;

        boolean updated = databaseHelper.updateBill(
                billId,
                month,
                currentYear,
                unit,
                rebate,
                totalCharges,
                finalCost
        );

        if (updated) {
            showInfoDialog(
                    "✅",
                    "Update Successful",
                    "Bill record has been updated successfully.",
                    true
            );
        } else {
            showInfoDialog(
                    "❌",
                    "Update Failed",
                    "Failed to update bill record.",
                    false
            );
        }
    }

    private double calculateTotalCharges(int unit) {
        if (unit <= 200) {
            return unit * 0.218;
        } else if (unit <= 300) {
            return (200 * 0.218)
                    + ((unit - 200) * 0.334);
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
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }
    }
}