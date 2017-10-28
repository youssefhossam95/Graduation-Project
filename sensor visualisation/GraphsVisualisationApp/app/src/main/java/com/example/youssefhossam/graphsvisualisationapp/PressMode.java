package com.example.youssefhossam.graphsvisualisationapp;

import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

public class PressMode extends AppCompatActivity implements SensorEventListener {
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
    long startTime;
    long lastReadingTime;
    ArrayList<DataPoint> graphZValues;
    int samplesCounter = 0;
    ArrayList<Reading> readingsTimeLine;
    double alertThreshold = 1.5;
    boolean isSpeechMode = true;
    Button switchModeButton, matabButton, hofraButton, takserButton, ghlatButton, harakaButton;
    Boolean isMatabPressed = false, isHofraPressed = false, isTakserPressed = false, isGhlatPressed = false, isHarakaPressed = false;
    public final static int NOKEYWORDS = 0, MATAB = 1, HOFRA = 2, TAKSER = 3, GHLAT = 4, HARAKA = 5;
    Integer userVoiceReply = NOKEYWORDS;
    AtomicBoolean isDetectionON;
    ArrayList<Reading> currentSessionAccelReading;
    TextView accelValuesText;
    long lastAnamolyTime;
    boolean isVoiceActivityDone = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_press_mode);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
        rotationMatrix = new float[16];
        rotationMatrixTranspose = new float[16];
        accelValues = new float[4];
        correctedAccelValues = new float[4];
        gravityValues = new float[3];
        magnetValues = new float[3];
        startTime = SystemClock.elapsedRealtime();
        readingsTimeLine = new ArrayList<Reading>();
        switchModeButton = (Button) findViewById(R.id.buttonMode);
        matabButton = (Button) findViewById(R.id.buttonMatab);
        hofraButton = (Button) findViewById(R.id.buttonHofra);
        takserButton = (Button) findViewById(R.id.buttonTakser);
        ghlatButton = (Button) findViewById(R.id.buttonGhlat);
        harakaButton = (Button) findViewById(R.id.buttonHaraka);
        currentSessionAccelReading = new ArrayList<Reading>();
        isDetectionON = new AtomicBoolean(true);
        accelValuesText = (TextView) findViewById(R.id.textView);
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }
    protected void onDestroy(){
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
    public void matabClick(View v) {
        isMatabPressed = true;
    }

    public void hofraClick(View v) {
        isHofraPressed = true;
    }

    public void takserClick(View v) {
        isTakserPressed = true;
    }

    public void ghlatClick(View v) {
        isGhlatPressed = true;
    }

    public void harakaClick(View v) {
        isHarakaPressed = true;
    }
    void processReading() {
        try {

            if (Math.abs(correctedAccelValues[2]) > alertThreshold) //anamolous reading
            {
                if(SystemClock.elapsedRealtime()-lastAnamolyTime <3000 || !isVoiceActivityDone) //anamoly detected during switch off after previous anamoly
                    return;
                lastAnamolyTime=SystemClock.elapsedRealtime();
                isDetectionON.set(false);
            }
            else
            {
                isDetectionON.set(true);
                return;
            }

                MediaPlayer mp = MediaPlayer.create(this, R.raw.ring);
                mp.start();
                //while (!(isMatabPressed || isHofraPressed || isGhlatPressed || isTakserPressed || isHarakaPressed)) ;

                if (isMatabPressed) {
                    //matab functionality
                } else if (isHofraPressed) {
                    //hofra functionality
                } else if (isTakserPressed) {
                    //takser functionailty
                } else if (isGhlatPressed) {
                    //ghlat functionality
                } else {
                    //haraka functionality
                }
                isMatabPressed=isHarakaPressed=isTakserPressed=isGhlatPressed=isHofraPressed=false;


        }
        catch(Exception e)
        {}


    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                lastReadingTime=SystemClock.elapsedRealtimeNanos();
                accelValues[0] = event.values[0];
                accelValues[1] = event.values[1];
                accelValues[2] = event.values[2];
                accelValues[3] = 1;
                if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                    transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                    multiplyMV(correctedAccelValues, 0, rotationMatrixTranspose, 0, accelValues, 0);
                    currentSessionAccelReading.add(new Reading(SystemClock.elapsedRealtimeNanos(),correctedAccelValues[2]));
                    if(!isDetectionON.get()) {
                        //displayExceptionMessage("Sensor Working");
                        //break;
                    }
                    processReading();
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
}
