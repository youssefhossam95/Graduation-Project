package com.stackers.BumpsFinder.TrainingApplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hitomi.cmlibrary.CircleMenu;

import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
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

public class SensorHandler implements SensorEventListener,TextToSpeech.OnInitListener {


    private static final double COSSIMTHRESHOLD = 0.99;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mMagnetic;
    private Sensor mGyro;
    float[] accelValues;
    float[] correctedAccelValues;
    float[] gravityValues;
    float[] magnetValues;
    float[] gyroValues;
    float[] correctedGyroValues;
    float[] rotationMatrix;
    float[] rotationMatrixTranspose;
    AppCompatActivity activity;
    boolean check = true;
    static final double INITIAL_THRESHOLD = 2.5;
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
    public LocationManager locationManager;
    public LocationListener locationListener;
    LinkedBlockingQueue<Reading> speedsQ = new LinkedBlockingQueue<Reading>();
    AtomicBoolean isStillProcessing;
    boolean isActivityAwake = true;
    float[] prevGravity;
    LinkedBlockingQueue<float[]> gravHistory=new LinkedBlockingQueue<>();
    LinkedBlockingQueue<float[]> gyroHistory=new LinkedBlockingQueue<>();
    static final double GYROTHRESHOLD=0.5;
    private long lastSmallCosSimTime;
    TextToSpeech tts ;
    double[][] layer1Weights;
    double[][] layer2Weights;


    SensorHandler(AppCompatActivity activity, CircleMenu circMenu) {

        //nu.pattern.OpenCV.loadLibrary();
        isStillProcessing=new AtomicBoolean(false);
        threshold = INITIAL_THRESHOLD;
        this.activity = activity;
        circleMenu = circMenu;
        mSensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyro=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationMatrix = new float[16];
        rotationMatrixTranspose = new float[16];
        accelValues = new float[4];
        correctedAccelValues = new float[4];
        gravityValues = new float[3];
        magnetValues = new float[3];
        gyroValues=new float[4];
        correctedGyroValues=new float[4];
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,mGyro,SENSOR_DELAY_FASTEST);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        lastAnamolyLoc = mLocation;
        tts= new TextToSpeech(activity, this);
        tts.setLanguage(Locale.US);
        try {
            loadNNWeights();
        }catch(IOException e){
            e.printStackTrace();
        }
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (speedsQ.size() < 3000)
                    speedsQ.add(new Reading(location.getElapsedRealtimeNanos(), location.getSpeed()));
                else {
                    speedsQ.poll();
                    speedsQ.add(new Reading(location.getElapsedRealtimeNanos(), location.getSpeed()));
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        initializeLocation();

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

        while (!speedsQ.isEmpty() && endTime - speedsQ.peek().time > 15 * Math.pow(10, 9))
            speedsQ.poll();



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
                    if (correctedAccelValues[2] > threshold && !FileHandler.isCurrentlyUploading ) {


                        if (lastAnamolyTime == null) {

                            MediaPlayer ring = MediaPlayer.create(activity, R.raw.ring);
                            ring.start();
                            Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 300 milliseconds
                            if (v != null)
                                v.vibrate(300);

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
                    if (lastAnamolyTime != null && event.timestamp - lastAnamolyTime > 3 * Math.pow(10, 9))
                    {
                        if(!isDeviceStable(4)){
                            tts.speak("Rotation ignored", TextToSpeech.QUEUE_ADD, null, null);
                            lastAnamolyTime=null;
                        }
                    }

                    if (lastAnamolyTime != null && event.timestamp - lastAnamolyTime > 5 * Math.pow(10, 9)) {
                        stopListening();
                        if (circleMenu.isOpened() && !isVoiceMode) {
                            circleMenu.closeMenu();
                            displayExceptionMessage("5 Seconds has passed! Anamoly will be ignored.");
                        }
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (isActivityAwake)
                                    startListening();
                                if (!isVoiceMode)
                                    lastAnamoly = null;
                            }
                        }, 1500);

                        extractReadings(event.timestamp);
                        lastAnamolyTime = null;
                        isStillProcessing.set(false);
                    }


                }
                break;
            }
            case Sensor.TYPE_GRAVITY: {
                gravityValues = event.values.clone();
                if (gravHistory.size() > 10)
                    prevGravity = gravHistory.poll().clone();

                gravHistory.add(gravityValues.clone());
                updateCosSim();
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                magnetValues = event.values.clone();
                break;
            }
            case Sensor.TYPE_GYROSCOPE: {

                gyroValues[0] = event.values[0];
                gyroValues[1] = event.values[1];
                gyroValues[2] = event.values[2];
                gyroValues[3] = 1;
                if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                    transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                    multiplyMV(correctedGyroValues, 0, rotationMatrixTranspose, 0, gyroValues, 0);
                    //if(isRecording)

                }

                if (gyroHistory.size() > 1100)
                    gyroHistory.poll();

                gyroHistory.add(correctedGyroValues.clone());

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
            return;
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
//    private boolean isDeviceRotating() {
//
//        ArrayList<float[]> history=new ArrayList<float[]>(gyroHistory);
//        for ( float [] gyroArray : history ){
//            if(gyroArray[1]>GYROTHRESHOLD)
//                return true;
//        }
//        return false;
//
//    }

    private void updateCosSim(){
        if(prevGravity==null)
            return;
        double cosSim;
        double gravityMag=Math.sqrt(Math.pow(gravityValues[0],2)+Math.pow(gravityValues[1],2)+Math.pow(gravityValues[2],2));
        double prevGravityMag=Math.sqrt(Math.pow(prevGravity[0],2)+Math.pow(prevGravity[1],2)+Math.pow(prevGravity[2],2));
        float dot=0;

        for(int i=0;i<prevGravity.length;i++){
            dot+=(prevGravity[i]*gravityValues[i]);
        }

        cosSim=dot/(prevGravityMag*gravityMag);
        if(cosSim<COSSIMTHRESHOLD)
            lastSmallCosSimTime=SystemClock.elapsedRealtime();
    }

    private boolean isDeviceStable(int stabilityPeriod){
        return SystemClock.elapsedRealtime()-lastSmallCosSimTime>stabilityPeriod*1000;
    }
    @Override
    public void onInit(int status) {

    }
    public void loadNNWeights() throws IOException{
        InputStream is=null;
        try{
         is = activity.getResources().openRawResource(R.raw.nnweights1);}
        catch(Exception e){
            e.printStackTrace();
        }
        BufferedReader input =  new BufferedReader(new InputStreamReader(is), 1024*8);
        layer1Weights=loadCsvToMatrix(input);
        is=activity.getResources().openRawResource(R.raw.nnweights2);
        input=new BufferedReader(new InputStreamReader(is), 1024*8);
        layer2Weights=loadCsvToMatrix(input);
    }


    public double [][] loadCsvToMatrix(BufferedReader input) throws IOException {
        String line = null;
        ArrayList<String[]>stringVals=new ArrayList<String[]>();

        while (( line = input.readLine()) != null)
            stringVals.add(line.split(","));

        double[][]weights=new double[stringVals.size()][stringVals.get(0).length];
        for(int i=0;i<stringVals.size();i++){
            for(int j=0;j<stringVals.get(i).length;j++)
              weights[i][j]=Double.parseDouble(stringVals.get(i)[j]);
        }
        return weights;

    }
    public double forwardProp(double [] features){

        Array2DRowRealMatrix layer1W=new Array2DRowRealMatrix(layer1Weights);
        Array2DRowRealMatrix layer2W=new Array2DRowRealMatrix(layer2Weights);
        layer1W=(Array2DRowRealMatrix)layer1W.transpose();
        layer2W=(Array2DRowRealMatrix)layer2W.transpose();
        double [] a1=layer1W.preMultiply(features);

        for (double x : a1)
            x=Math.max(x,0);

        double [] a2=layer2W.preMultiply(a1);
        Sigmoid s=new Sigmoid();
        return s.value(a2[0]);

    }


}


