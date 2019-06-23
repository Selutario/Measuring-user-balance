package com.example.hellobalance;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;

/*
* The objective of this algorithm is to check if the user changes his original position.
*
* To do this, the user is asked to remain still in the original position, and the accelerometer
* samples are used to calculate the average position of the three axes during that time.
* Once calibrated, if any of the axes measures a significantly different value from the average,
* it indicates that the user has lost balance.
*
* Simple algorithm still in development.
* Made by José Luis López Sánchez.*/
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Number of samples to calculate the average.
    private final int MAX_INDEX = 20;

    // Accelerometer variables
    private SensorManager sensorManager;
    private Sensor sensorAcc;

    // Layout variables
    private TextView tv_x_axis;
    private TextView tv_y_axis;
    private TextView tv_z_axis;
    private TextView tv_mean;
    private TextView tv_change;
    private TextView tv_balance;

    // Calibration variables
    private boolean calibrated = false;
    private int sample_index = 0;
    private float measured_x[] = new float [MAX_INDEX];
    private float measured_y[] = new float [MAX_INDEX];
    private float measured_z[] = new float [MAX_INDEX];

    // Average of each axis.
    private float mean_x = 0;
    private float mean_y = 0;
    private float mean_z = 0;
    private float move_allowed = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keeps screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Sensor declaration. We use 1Hz frequency to get smoother measurements.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAcc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        sensorManager.registerListener(this, sensorAcc, 1000000);

        tv_x_axis = (TextView) findViewById(R.id.x_axis);
        tv_y_axis = (TextView) findViewById(R.id.y_axis);
        tv_z_axis = (TextView) findViewById(R.id.z_axis);
        tv_mean = (TextView) findViewById(R.id.tv_mean);
        tv_change = (TextView) findViewById(R.id.tv_change);
        tv_balance = (TextView) findViewById(R.id.tv_balance);
    }

    @Override
    public void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    // Pause sensor listener to save battery and memory.
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Resume sensor listener
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAcc, 1000000);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Values measured on each axis.
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calibration process
        if(!calibrated){
            // If there are enough samples, calculate average.
            if(sample_index >= MAX_INDEX){
                for(int i=0; i<MAX_INDEX; i++){
                    mean_x += measured_x[i];
                    mean_y += measured_y[i];
                    mean_z += measured_z[i];
                }
                mean_x = mean_x/MAX_INDEX;
                mean_y = mean_y/MAX_INDEX;
                mean_z = mean_z/MAX_INDEX;

                calibrated = true;
            } else {
                // If there are not, save the sample value.
                measured_x[sample_index%MAX_INDEX] = x;
                measured_y[sample_index%MAX_INDEX] = y;
                measured_z[sample_index%MAX_INDEX] = z;
                sample_index++;
            }
        }

        // How much the current value changes with respect to the average or
        // normal position.
        float change_x, change_y, change_z;
        change_x = Math.abs(x - mean_x);
        change_y = Math.abs(y - mean_y);
        change_z = Math.abs(z - mean_z);

        // Show registered values
        tv_x_axis.setText("\nX AXIS: " + x);
        tv_y_axis.setText("\nY AXIS: " + y);
        tv_z_axis.setText("\nZ AXIS: " + z);
        tv_mean.setText("\nMEAN_X: " + mean_x + " | Y: " + mean_y + " | Z: " + mean_z);
        tv_change.setText("CHANGE_X: " + change_x + " | Y: " + change_y + " | Z: " + change_z);

        // Check if the current value is sufficiently different from the average or normal position.
        // In that case, the user is unbalanced.
        if(!calibrated){
            tv_balance.setTextColor(Color.BLUE);
            tv_balance.setText("CALIBRATING");
        }
        else if(change_x > move_allowed) {
            tv_balance.setTextColor(Color.RED);
            tv_balance.setText("X__UNBALANCED");
        } else if (change_y > move_allowed/2 ) {
            tv_balance.setTextColor(Color.RED);
            tv_balance.setText("Y__UNBALANCED");
        } else if (change_z > move_allowed ) {
            tv_balance.setTextColor(Color.RED);
            tv_balance.setText("Z__UNBALANCED");
        } else {
            tv_balance.setTextColor(Color.GREEN);
            tv_balance.setText("BALANCED");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
