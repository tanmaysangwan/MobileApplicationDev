package com.example.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    private TextView textX;
    private TextView textY;
    private TextView textZ;
    private TextView textLight;
    private TextView textProximity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textX = findViewById(R.id.textX);
        textY = findViewById(R.id.textY);
        textZ = findViewById(R.id.textZ);
        textLight = findViewById(R.id.textLight);
        textProximity = findViewById(R.id.textProximity);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (accelerometer == null) {
            textX.setText("X: N/A");
            textY.setText("Y: N/A");
            textZ.setText("Z: N/A");
        }
        if (lightSensor == null) {
            textLight.setText("N/A");
        }
        if (proximitySensor == null) {
            textProximity.setText("Status: N/A");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager == null) return;

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor == null) return;

        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            textX.setText(String.format(Locale.US, "X: %.2f", x));
            textY.setText(String.format(Locale.US, "Y: %.2f", y));
            textZ.setText(String.format(Locale.US, "Z: %.2f", z));
            return;
        }

        if (type == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            textLight.setText(String.format(Locale.US, "%.2f lx", lux));
            return;
        }

        if (type == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            float max = event.sensor.getMaximumRange();
            boolean isNear = distance < max;
            String status = isNear ? "Near" : "Far";
            textProximity.setText(String.format(Locale.US, "Status: %s (%.1f)", status, distance));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // no-op
    }
}