package com.example.youssefhossam.graphsvisualisationapp;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
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
    ArrayList<Reading>readingsTimeLine;
    double alertThreshold=1.5;
    boolean isSpeechMode=true;
    Button switchModeButton,matabButton,hofraButton,takserButton,ghlatButton,harakaButton;
    boolean isMatabPressed=false,isHofraPressed=false,isTakserPressed=false,isGhlatPressed=false,isHarakaPressed=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        readingsTimeLine=new ArrayList<Reading>();
        switchModeButton=(Button)findViewById(R.id.buttonMode);
        matabButton=(Button)findViewById(R.id.buttonMatab);
        hofraButton=(Button)findViewById(R.id.buttonHofra);
        takserButton=(Button)findViewById(R.id.buttonTakser);
        ghlatButton=(Button)findViewById(R.id.buttonGhlat);
        harakaButton=(Button)findViewById(R.id.buttonHaraka);

    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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
                    //if(isRecording)
                    processData();
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

    void processData() {
        try {
            boolean isNormal = true;
            for (float val : correctedAccelValues) {
                if (Math.abs(val) > alertThreshold)
                    isNormal = false;
            }
            if (isNormal)
                return;

            if (!isSpeechMode) //buttons Mode
            {
                MediaPlayer mp = MediaPlayer.create(this, R.raw.ring);
                mp.start();
                while (!(isMatabPressed || isHofraPressed || isGhlatPressed || isTakserPressed || isHarakaPressed))
                    ;

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
            }
            else //Speech Mode
            {
                promptSpeechInput();
            }
            if (samplesCounter == 0) //first reading in recording session.
            {
                startTime = SystemClock.elapsedRealtime();
                readingsTimeLine.add(new Reading((long) 0, correctedAccelValues[2]));
            } else {
                readingsTimeLine.add(new Reading(lastReadingTime - startTime, correctedAccelValues[2]));
            }

            samplesCounter++;
            if (SystemClock.elapsedRealtime() - startTime >= 10000) {
                samplesCounter = 0;

            }
        }
        catch(Exception e)
        {}


    }

    //    public void startRecording(View v)
//    {
//        isRecording=true;
//        startButton.setBackgroundColor(Color.RED);
//        startButton.setText("Recording");
//    }
    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * uses linear interpolation and extrapolation to sample accelerometer readings for a given session length.
     * @param samplingRate number of samples per second
     * @param readings array representing the  timeline in nanoseconds of accelerometer readings
     * @param time required session length in seconds
     * @return array of sampled readings
     */
    double[] getSampledReadings(int samplingRate,ArrayList<Reading>readings,int time)
    {
        double stepSize=1/samplingRate * Math.pow(10,9);
        int sampledReadingsCount=time*samplingRate+1;
        double[] sampledReadings=new double[sampledReadingsCount];
        sampledReadings[0]=readings.get(0).value; //reading at t=0
        int i=1,j=1;
        double currentTime=stepSize;
        while(true)
        {
            while(i<readings.size() && currentTime>readings.get(i).time)
                i++;

            if(i==readings.size())
                break;

            sampledReadings[j]=readings.get(i-1).value + (currentTime-readings.get(i-1).time)/(readings.get(i).time-readings.get(i-1).time) * (readings.get(i).value-readings.get(i-1).value); //linear interpolation
            j++;
            if(j==sampledReadingsCount) //session time is over
                break;
            currentTime+=stepSize;
        }

        while(j<sampledReadingsCount)
        {
            sampledReadings[j]=readings.get(i-1).value;
            j++;
        }

        return sampledReadings;

    }

    private void promptSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        try {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar");
            startActivityForResult(intent,100);
        } catch (Exception e) {
            displayExceptionMessage(e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


                }
                break;
            }

        }
    }
    public void switchModeClick(View v)
    {
        isSpeechMode=!isSpeechMode;
        if(isSpeechMode)
        {
            matabButton.setVisibility(View.INVISIBLE);
            hofraButton.setVisibility(View.INVISIBLE);
            takserButton.setVisibility(View.INVISIBLE);
            ghlatButton.setVisibility(View.INVISIBLE);
            harakaButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            matabButton.setVisibility(View.VISIBLE);
            hofraButton.setVisibility(View.VISIBLE);
            takserButton.setVisibility(View.VISIBLE);
            ghlatButton.setVisibility(View.VISIBLE);
            harakaButton.setVisibility(View.VISIBLE);
        }

    }
    public void matabClick(View v) {
        isMatabPressed=true;
    }


    public void hofraClick(View v) {
        isHofraPressed=true;
    }

    public void takserClick(View v) {
        isTakserPressed=true;
    }

    public void ghlatClick(View v) {
        isGhlatPressed=true;
    }

    public void harakaClick(View v) {
        isHarakaPressed=true;
    }

}




