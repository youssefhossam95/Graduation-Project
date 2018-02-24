package com.example.youssefhossam.graphsvisualisationapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hitomi.cmlibrary.CircleMenu;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

/**
 * Created by Youssef Hossam on 30/01/2018.
 */

public class SensorHandler implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mMagnetic;
    float[] accelValues;
    float[] correctedAccelValues;
    float[] gravityValues;
    float[] magnetValues;
    float[] rotationMatrix;
    float[] rotationMatrixTranspose;
    AppCompatActivity activity;
    boolean check = true;
    static final double INITIAL_THRESHOLD = 3.0;
    public double threshold;
    LinkedBlockingQueue<Reading> readingsQ = new LinkedBlockingQueue<Reading>();
    Long lastAnamolyTime;
    SensorHandler me = this;
    public Anamoly lastAnamoly;
    static final int REQUEST_LOCATION = 1;
    public Location mLocation;
    Location lastAnamolyLoc;
    CircleMenu circleMenu;
    private FusedLocationProviderClient mFusedLocationClient;
    boolean isVoiceMode;
    LocationManager locationManager;
    LocationListener locationListener;
    LinkedBlockingQueue<Reading> speedsQ = new LinkedBlockingQueue<Reading>();
    AtomicBoolean isStillProcessing;
    boolean isActivityAwake = true;

    SensorHandler(AppCompatActivity activity, CircleMenu circMenu) {
        isStillProcessing=new AtomicBoolean(false);
        threshold = INITIAL_THRESHOLD;
        this.activity = activity;
        circleMenu = circMenu;
        mSensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationMatrix = new float[16];
        rotationMatrixTranspose = new float[16];
        accelValues = new float[4];
        correctedAccelValues = new float[4];
        gravityValues = new float[3];
        magnetValues = new float[3];
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        initializeLocation();
        lastAnamolyLoc = mLocation;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
// Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (speedsQ.size() < 3000)
                    speedsQ.add(new Reading(location.getTime(), location.getSpeed()));
                else {
                    speedsQ.poll();
                    speedsQ.add(new Reading(location.getTime(), location.getSpeed()));
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        checkForSensors();
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mGravity);
        mSensorManager.unregisterListener(this, mMagnetic);
    }


    public void startListening() {
        mSensorManager.registerListener(me, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(me, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(me, mMagnetic, SENSOR_DELAY_FASTEST);
    }


    private void extractReadings(long endTime) {
        Log.e("Extracted Readings", "E7na Hena");

        while (endTime - readingsQ.peek().time > 10 * Math.pow(10, 9))
            readingsQ.poll();

        Reading[] tempAccelArray = new Reading[readingsQ.size()];
        Reading[] tempSpeedsArray = new Reading[speedsQ.size()];
        LinkedBlockingQueue<Reading> tempSpeedsQ = new LinkedBlockingQueue<Reading>(speedsQ);

        for (int i = 0; i < tempSpeedsArray.length; i++) {
            tempSpeedsArray[i] = tempSpeedsQ.poll();
        }

        for (int i = 0; i < tempAccelArray.length; i++) {
            tempAccelArray[i] = readingsQ.poll();
        }

        lastAnamoly = new Anamoly(tempAccelArray, tempSpeedsArray, lastAnamolyLoc); //han7ot el array of speeds hena
    }


    public void onSensorChanged(SensorEvent event) {




        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                accelValues[0] = event.values[0];
                accelValues[1] = event.values[1];
                accelValues[2] = event.values[2];
                accelValues[3] = 1;

                if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                    transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                    multiplyMV(correctedAccelValues, 0, rotationMatrixTranspose, 0, accelValues, 0);
                    if (readingsQ.size() < 2000)
                        readingsQ.add(new Reading(event.timestamp, correctedAccelValues[2]));
                    else {
                        readingsQ.poll();
                        readingsQ.add(new Reading(event.timestamp, correctedAccelValues[2]));
                    }
                    if (correctedAccelValues[2] > threshold) {
                        if (lastAnamolyTime == null) {
                            MediaPlayer ring = MediaPlayer.create(activity, R.raw.ring);
                            ring.start();
                            if (mLocation != null) {
                                if (isVoiceMode)
                                    promptSpeechInput();
                                else
                                    circleMenu.openMenu();
                            } else {
                                displayExceptionMessage("Location is not available yet ");
                                lastAnamoly = null;
                            }
                            initializeLocation();
                            lastAnamolyLoc = mLocation;
                            lastAnamolyTime = event.timestamp;
                            isStillProcessing.set(true);
                        }


                    }

                    if (lastAnamolyTime != null && event.timestamp - lastAnamolyTime > 5 * Math.pow(10, 9)) {
                        stopListening();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if(isActivityAwake)
                                    startListening();
                                if (!isVoiceMode)
                                    lastAnamoly = null;
                            }
                        }, 3000);

                        extractReadings(event.timestamp);
                        lastAnamolyTime = null;
                        isStillProcessing.set(false);
                    }


                }
                break;
            }
            case Sensor.TYPE_GRAVITY: {
                gravityValues = event.values.clone();
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                magnetValues = event.values.clone();
                break;
            }
        }


    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void displayExceptionMessage(String msg) {

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        try {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar");
            activity.startActivityForResult(intent, 100);
        } catch (Exception e) {
            displayExceptionMessage(e.getMessage());
        }
    }

    private void initializeLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    displayExceptionMessage("Please Turn ON GPS");
                }
                mLocation = location;

            }
        });


    }

    private void checkForSensors()
    {
        if (mGravity == null) {
            displayExceptionMessage("Your device doesn't have a gyroscope, the application will be closed now!");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    activity.finish();
                }
            }, 2000);

        } else if (mMagnetic == null) {
            displayExceptionMessage("Your device doesn't have a magnetometer, the application will be closed now!");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    activity.finish();
                }
            }, 2000);
        }
    }

}
