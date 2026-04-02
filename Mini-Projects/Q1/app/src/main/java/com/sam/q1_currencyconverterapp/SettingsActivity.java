package com.sam.q1_currencyconverterapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    // SharedPreferences key constants
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_DARK   = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar with back arrow
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // back arrow
        }

        // Safety check — if toolbar not found, just return
        if (toolbar == null) return;

        // Toggle switch for Dark/Light theme
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchDarkMode = findViewById(R.id.switchDarkMode);

        // Load the currently saved preference and reflect it on the switch
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        switchDarkMode.setChecked(isDark);

        // When the user toggles the switch
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {

                // Save the new preference
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_DARK, isChecked);
                editor.apply();

                // Apply the theme change immediately across the whole app
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }

    /**
     * Handles the back arrow press in the toolbar.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // go back to MainActivity
        return true;
    }
}