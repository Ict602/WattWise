package com.example.wattwise;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class EditBillActivity extends AppCompatActivity {

    Spinner spMonth, spRebate;
    EditText etUnit;
    Button btnUpdate, btnCancel;

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

    String[] rebates = {"0%", "1%", "2%", "3%", "4%", "5%"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);

        databaseHelper = new DatabaseHelper(this);

        spMonth = findViewById(R.id.spMonth);
        spRebate = findViewById(R.id.spRebate);
        etUnit = findViewById(R.id.etUnit);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        txtMonthBadge = findViewById(R.id.txtMonthBadge);
        txtCurrentRecord = findViewById(R.id.txtCurrentRecord);
        txtCurrentCost = findViewById(R.id.txtCurrentCost);
        txtNewCost = findViewById(R.id.txtNewCost);

        setupSpinners();

        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            Toast.makeText(this, "Invalid record", Toast.LENGTH_SHORT).show();
            finish();
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

        spMonth.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        spRebate.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnUpdate.setOnClickListener(v -> updateRecord());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
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
            txtCurrentCost.setText(String.format(Locale.US, "RM %.2f", oldFinalCost));

            etUnit.setText(String.valueOf(unit));

            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(month)) {
                    spMonth.setSelection(i);
                    break;
                }
            }

            spRebate.setSelection(rebate);

        } else {
            Toast.makeText(this, "Record not found", Toast.LENGTH_SHORT).show();
            finish();
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

        int previewRebate = spRebate.getSelectedItemPosition();

        double previewTotal = calculateTotalCharges(previewUnit);
        previewTotal = Math.round(previewTotal * 100.0) / 100.0;

        double previewFinal = previewTotal - (previewTotal * previewRebate / 100.0);
        previewFinal = Math.round(previewFinal * 100.0) / 100.0;

        txtNewCost.setText(String.format(Locale.US, "RM %.2f", previewFinal));
    }

    private void updateRecord() {
        String unitText = etUnit.getText().toString().trim();

        if (unitText.isEmpty()) {
            etUnit.setError("Please enter electricity unit used.");
            etUnit.requestFocus();
            return;
        }

        unit = Integer.parseInt(unitText);

        if (unit < 1 || unit > 1000) {
            etUnit.setError("Unit must be between 1 and 1000 kWh.");
            etUnit.requestFocus();
            return;
        }

        month = spMonth.getSelectedItem().toString();
        rebate = spRebate.getSelectedItemPosition();

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
            Toast.makeText(this, "Record updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update record", Toast.LENGTH_SHORT).show();
        }
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
}