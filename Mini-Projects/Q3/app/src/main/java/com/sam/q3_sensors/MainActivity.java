package com.sam.q3_sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // --- Sensor Manager to access device sensors ---
    private SensorManager sensorManager;

    // --- The three sensors we need ---
    private Sensor accelerometerSensor;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    // --- TextViews to display sensor values ---
    private TextView textAccX, textAccY, textAccZ;
    private TextView textLight;
    private TextView textProximity;
    private TextView textAccStatus, textLightStatus, textProxStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sensor App");
        }

        // Link TextViews to XML
        textAccX       = findViewById(R.id.textAccX);
        textAccY       = findViewById(R.id.textAccY);
        textAccZ       = findViewById(R.id.textAccZ);
        textLight      = findViewById(R.id.textLight);
        textProximity  = findViewById(R.id.textProximity);
        textAccStatus  = findViewById(R.id.textAccStatus);
        textLightStatus= findViewById(R.id.textLightStatus);
        textProxStatus = findViewById(R.id.textProxStatus);

        // Get the SensorManager system service
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // --- Get Accelerometer sensor ---
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor == null) {
            textAccStatus.setText("Accelerometer: NOT AVAILABLE on this device");
            Toast.makeText(this, "No Accelerometer found!", Toast.LENGTH_SHORT).show();
        } else {
            textAccStatus.setText("Accelerometer: Available ✓");
        }

        // --- Get Light sensor ---
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            textLightStatus.setText("Light Sensor: NOT AVAILABLE on this device");
            Toast.makeText(this, "No Light Sensor found!", Toast.LENGTH_SHORT).show();
        } else {
            textLightStatus.setText("Light Sensor: Available ✓");
        }

        // --- Get Proximity sensor ---
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            textProxStatus.setText("Proximity Sensor: NOT AVAILABLE on this device");
            Toast.makeText(this, "No Proximity Sensor found!", Toast.LENGTH_SHORT).show();
        } else {
            textProxStatus.setText("Proximity Sensor: Available ✓");
        }
    }

    /**
     * Register all sensor listeners when app comes to foreground.
     * SENSOR_DELAY_NORMAL = updates every ~200ms (saves battery).
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this,
                    accelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (lightSensor != null) {
            sensorManager.registerListener(this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (proximitySensor != null) {
            sensorManager.registerListener(this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Unregister listeners when app goes to background.
     * This saves battery — very important!
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Called automatically every time any sensor value changes.
     * We check which sensor fired and update the correct TextViews.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                // Accelerometer gives X, Y, Z values in m/s²
                // X = left/right tilt
                // Y = forward/backward tilt
                // Z = up/down (gravity ~9.8 when flat)
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                textAccX.setText(String.format("X (left/right):      %.4f m/s²", x));
                textAccY.setText(String.format("Y (forward/back):  %.4f m/s²", y));
                textAccZ.setText(String.format("Z (up/down):         %.4f m/s²", z));
                break;

            case Sensor.TYPE_LIGHT:
                // Light sensor gives a single value in lux
                // 0 lux = complete darkness
                // 40000+ lux = direct sunlight
                float lux = event.values[0];
                textLight.setText(String.format("%.2f lux", lux));

                // Show a human readable description of light level
                String lightDesc;
                if (lux < 10)        lightDesc = "Very Dark (night)";
                else if (lux < 100)  lightDesc = "Dim (indoor)";
                else if (lux < 1000) lightDesc = "Normal (office light)";
                else if (lux < 10000)lightDesc = "Bright (cloudy day)";
                else                 lightDesc = "Very Bright (sunlight)";

                textLightStatus.setText("Light Sensor: Available ✓  —  " + lightDesc);
                break;

            case Sensor.TYPE_PROXIMITY:
                // Proximity sensor gives distance in cm
                // Most phones only return 0 (near) or max value (far)
                float distance = event.values[0];
                float maxRange = proximitySensor.getMaximumRange();

                if (distance < maxRange) {
                    textProximity.setText(distance + " cm  —  NEAR (object detected)");
                } else {
                    textProximity.setText(distance + " cm  —  FAR (no object)");
                }
                break;
        }
    }

    /**
     * Called when sensor accuracy changes — required by SensorEventListener
     * but we don't need to do anything here for this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this app
    }
}
