package com.sam.q1_currencyconverterapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.sam.q1_currencyconverterapp.R;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private EditText inputAmount;
    private Spinner spinnerFrom, spinnerTo;
    private TextView textResult;

    // Currency names shown in spinners
    private final String[] currencies = {"INR", "USD", "JPY", "EUR"};

    /*
     * Exchange rates relative to INR (Indian Rupee as base).
     * Row = FROM currency, Column = TO currency.
     * Index: 0=INR, 1=USD, 2=JPY, 3=EUR
     *
     * Example: rates[1][0] means USD -> INR = 83.5
     */
    private final double[][] rates = {
            // TO:  INR      USD       JPY       EUR
            {  1.0,    0.012,   1.78,    0.011  },  // FROM: INR
            {  83.5,   1.0,     148.5,   0.92   },  // FROM: USD
            {  0.56,   0.0067,  1.0,     0.0062 },  // FROM: JPY
            {  90.5,   1.085,   161.0,   1.0    }   // FROM: EUR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the saved theme (Dark/Light) before setContentView
        applySavedTheme();

        setContentView(R.layout.activity_main);

        // Set the toolbar as the action bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Currency Converter");
        }

        // Link Java variables to XML views
        inputAmount = findViewById(R.id.editTextAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo   = findViewById(R.id.spinnerTo);
        textResult  = findViewById(R.id.textViewResult);
        Button btnConvert = findViewById(R.id.btnConvert);
        Button btnSwap = findViewById(R.id.btnSwap);

        // Populate both spinners with the currency list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Default: FROM = INR (index 0), TO = USD (index 1)
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);

        // Convert button click
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });

        // Swap button: swaps the FROM and TO spinner selections
        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fromPos = spinnerFrom.getSelectedItemPosition();
                int toPos   = spinnerTo.getSelectedItemPosition();
                spinnerFrom.setSelection(toPos);
                spinnerTo.setSelection(fromPos);

                // Re-convert automatically after swap if amount exists
                if (!inputAmount.getText().toString().isEmpty()) {
                    convertCurrency();
                }
            }
        });

        // Auto-convert when user changes the spinner selection
        AdapterView.OnItemSelectedListener autoConvert = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!inputAmount.getText().toString().isEmpty()) {
                    convertCurrency();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        };

        spinnerFrom.setOnItemSelectedListener(autoConvert);
        spinnerTo.setOnItemSelectedListener(autoConvert);
    }

    /**
     * Reads the input amount, fetches the correct exchange rate,
     * performs the conversion, and displays the result.
     */
    private void convertCurrency() {
        String amountStr = inputAmount.getText().toString().trim();

        // Validate: make sure the user entered something
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected currency indices
        int fromIndex = spinnerFrom.getSelectedItemPosition();
        int toIndex   = spinnerTo.getSelectedItemPosition();

        // If both currencies are the same, result equals input
        if (fromIndex == toIndex) {
            textResult.setText(String.format("%.2f %s", amount, currencies[toIndex]));
            return;
        }

        // Multiply input by the rate from the 2D matrix
        double convertedAmount = amount * rates[fromIndex][toIndex];

        // Show the result
        @SuppressLint("DefaultLocale") String resultText = String.format(
                "%.2f %s  =  %.4f %s",
                amount, currencies[fromIndex],
                convertedAmount, currencies[toIndex]
        );
        textResult.setText(resultText);
    }

    /**
     * Inflates the options menu (gear icon → Settings).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles toolbar menu item clicks.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Open the Settings screen
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Reads the saved theme preference from SharedPreferences
     * and applies Dark or Light mode before the UI is drawn.
     */
    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
