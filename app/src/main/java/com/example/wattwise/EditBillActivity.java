package com.example.wattwise;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class EditBillActivity extends AppCompatActivity {

    Spinner spMonth;
    EditText etUnit;

    Button btnUpdate, btnCancel;
    Button btnRebate0, btnRebate1, btnRebate2, btnRebate3, btnRebate4, btnRebate5;

    TextView txtMonthBadge, txtCurrentRecord, txtCurrentCost, txtNewCost;

    DatabaseHelper databaseHelper;

    int billId = -1;
    int currentYear = 0;
    int unit = 0;
    int rebate = 0;
    String month = "";

    double totalCharges = 0.0;
    double finalCost = 0.0;

    String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);

        databaseHelper = new DatabaseHelper(this);

        spMonth = findViewById(R.id.spMonth);
        etUnit = findViewById(R.id.etUnit);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        btnRebate0 = findViewById(R.id.btnRebate0);
        btnRebate1 = findViewById(R.id.btnRebate1);
        btnRebate2 = findViewById(R.id.btnRebate2);
        btnRebate3 = findViewById(R.id.btnRebate3);
        btnRebate4 = findViewById(R.id.btnRebate4);
        btnRebate5 = findViewById(R.id.btnRebate5);

        txtMonthBadge = findViewById(R.id.txtMonthBadge);
        txtCurrentRecord = findViewById(R.id.txtCurrentRecord);
        txtCurrentCost = findViewById(R.id.txtCurrentCost);
        txtNewCost = findViewById(R.id.txtNewCost);

        setupMonthSpinner();
        setupRebateButtons();

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

        spMonth.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        updatePreview();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );

        btnUpdate.setOnClickListener(v -> confirmUpdate());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupMonthSpinner() {
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected,
                months
        );

        monthAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spMonth.setAdapter(monthAdapter);
    }

    private void setupRebateButtons() {
        btnRebate0.setOnClickListener(v -> selectRebate(0));
        btnRebate1.setOnClickListener(v -> selectRebate(1));
        btnRebate2.setOnClickListener(v -> selectRebate(2));
        btnRebate3.setOnClickListener(v -> selectRebate(3));
        btnRebate4.setOnClickListener(v -> selectRebate(4));
        btnRebate5.setOnClickListener(v -> selectRebate(5));

        selectRebate(0);
    }

    private void selectRebate(int value) {
        rebate = value;

        Button[] buttons = {
                btnRebate0,
                btnRebate1,
                btnRebate2,
                btnRebate3,
                btnRebate4,
                btnRebate5
        };

        for (Button button : buttons) {
            button.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            );
            button.setTextColor(Color.parseColor("#020B1A"));
        }

        buttons[value].setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#FFC107"))
        );
        buttons[value].setTextColor(Color.BLACK);

        updatePreview();
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

            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(month)) {
                    spMonth.setSelection(i);
                    break;
                }
            }

            selectRebate(rebate);

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
                "Yes",
                "No",
                () -> updateRecord()
        );
    }

    private void updateRecord() {
        String unitText = etUnit.getText().toString().trim();

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
                    "Please enter a valid number.",
                    false
            );
            etUnit.requestFocus();
            return;
        }

        if (unit < 1 || unit > 1000) {
            showInfoDialog(
                    "⚠️",
                    "Invalid Range",
                    "Unit must be between 1 and 1000 kWh.",
                    false
            );
            etUnit.requestFocus();
            return;
        }

        month = spMonth.getSelectedItem().toString();

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