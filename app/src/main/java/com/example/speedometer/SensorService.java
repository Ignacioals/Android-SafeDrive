package com.example.speedometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isMoving = false;
    private float mThreshold = 2.5f;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double rateOfRotation = Math.sqrt(Math.pow(event.values[0],2)
                + Math.pow(event.values[1], 2)
                + Math.pow(event.values[2], 2));
        if(rateOfRotation>mThreshold){
            if(!isMoving){
                isMoving = true;
            }
        }
        else{
            isMoving = false;
        }
        updateUI();


    }

    private void updateUI(){
        if (MainActivity.p == 0) {
            if (isMoving != false) {
                MainActivity.movementAlert.setText("El telefono esta en uso");
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void stopMovementUpdates() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onUnbind(Intent intent){
        stopMovementUpdates();
        return super.onUnbind(intent);
    }

}
