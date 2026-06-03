package com.example.wattwise;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
        btnReset.setOnClickListener(v -> resetForm());
        btnSave.setOnClickListener(v -> saveRecord());
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

        Toast.makeText(this, "Calculation completed", Toast.LENGTH_SHORT).show();
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

    private void saveRecord() {
        if (!isCalculated) {
            Toast.makeText(this, "Please calculate the bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = databaseHelper.insertBill(
                selectedMonth,
                currentYear,
                unit,
                selectedRebate,
                totalCharges,
                finalCost
        );

        if (inserted) {
            Toast.makeText(this, "Record saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
        }
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

        Toast.makeText(this, "Form reset", Toast.LENGTH_SHORT).show();
    }
}