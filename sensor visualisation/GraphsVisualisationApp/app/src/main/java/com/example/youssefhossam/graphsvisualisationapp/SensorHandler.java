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
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

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
    boolean check=true;
    Double threshold;
    LinkedBlockingQueue<Reading> readingsQ=new LinkedBlockingQueue<Reading>();
    Long lastAnamolyTime;
    SensorHandler me=this;
    Anamoly lastAnamoly;
    static final int REQUEST_LOCATION=1;
    private double longitude;
    private double latitude;
    private FusedLocationProviderClient mFusedLocationClient;
    Boolean isVoiceMode;



    SensorHandler(AppCompatActivity activity,Double Threshold,Anamoly lastAnamoly,Boolean isVoiceMode)
    {
        this.activity=activity;
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
        this.threshold=Threshold;
        this.lastAnamoly=lastAnamoly;
        this.isVoiceMode=isVoiceMode;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }
    private void extractReadings(long endTime)
    {
        while(endTime-readingsQ.peek().time>10*Math.pow(10,9))
            readingsQ.poll();
        Reading[]tempArray=new Reading[readingsQ.size()];
        for(int i=0;i<tempArray.length;i++)
        {
            tempArray[i]=readingsQ.poll();
        }
        lastAnamoly=new Anamoly(tempArray,null,getLocation()); //han7ot el array of speeds hena
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
                    if(readingsQ.size()<2000)
                        readingsQ.add(new Reading(event.timestamp,correctedAccelValues[2]));
                    else
                    {
                        readingsQ.poll();
                        readingsQ.add(new Reading(event.timestamp,correctedAccelValues[2]));
                    }
                    if(correctedAccelValues[2]>threshold)
                    {
                        if(lastAnamolyTime==null)
                        {
                            MediaPlayer ring= MediaPlayer.create(activity,R.raw.ring);
                            ring.start();
                        }
                        lastAnamolyTime=event.timestamp;


                    }
                    else
                    {
                        if(lastAnamolyTime!=null && event.timestamp-lastAnamolyTime>2*Math.pow(10,9))
                        {
                            mSensorManager.unregisterListener(this, mAccelerometer);
                            mSensorManager.unregisterListener(this, mGravity);
                            mSensorManager.unregisterListener(this, mMagnetic);
                            new Timer().schedule(new TimerTask() {
                                @Override public void run() {
                                    mSensorManager.registerListener(me, mAccelerometer, SENSOR_DELAY_FASTEST);
                                    mSensorManager.registerListener(me, mGravity, SENSOR_DELAY_FASTEST);
                                    mSensorManager.registerListener(me, mMagnetic, SENSOR_DELAY_FASTEST);
                                    if(!isVoiceMode)
                                        lastAnamoly=null;
                                } } , 5000);

                            extractReadings(event.timestamp);
                            lastAnamolyTime=null;

                            if(isVoiceMode)
                                promptSpeechInput();

                        }
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
    public void displayExceptionMessage(String msg)
    {

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
    private void promptSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        try {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar");
            activity.startActivityForResult(intent,100);
        } catch (Exception e) {
            displayExceptionMessage(e.getMessage());
        }
    }

    public Location getLocation() {
        Location location= new Location("");
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {


                        if (location != null) {

                            longitude=location.getLongitude();
                            latitude=location.getLatitude();
                        }
                        else
                        {
                            displayExceptionMessage("Please Turn ON GPS");
                        }

                    }
                });


        return location;

    }

}
