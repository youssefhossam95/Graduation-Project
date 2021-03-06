package com.example.youssefhossam.samplingtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminInfo;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import at.markushi.ui.CircleButton;

import org.json.JSONArray;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.lang.Object;

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
    long sessionStartTime;
    CircleButton startButton;
    public final static int UNKNOWN = 0, MATAB = 1, HOFRA = 2, TAKSER = 3, GHLAT = 4, HARAKA = 5;
    int currentSessionAnamolyType = UNKNOWN;
    ArrayList<Reading> currentSessionAccelReading;
    boolean isVoiceActivityDone = true, isRecording = false, ignoreTimeOver = true;
    String userComment;
    Location currentSessionLocation;
    GraphView graph, sampledGraph;
    ArrayList<DataPoint> graphZValues, sampledGraphZValues;
    int NumberOfDefects = 0;
    private Context context;
    EditText samplingRateTextBox;

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
        sessionStartTime = 0;
        startButton = (CircleButton) findViewById(R.id.StartRecordingButton);
        samplingRateTextBox = (EditText) findViewById(R.id.text);
        currentSessionAccelReading = new ArrayList<Reading>();
        graphZValues = new ArrayList<DataPoint>();
        sampledGraphZValues = new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        sampledGraph = (GraphView) findViewById(R.id.graph2);
        this.context = getApplicationContext();
        graph.getViewport().setXAxisBoundsManual(true);
        sampledGraph.getViewport().setXAxisBoundsManual(true);
        samplingRateTextBox.setText("100");
        graph.getViewport().setScalable(true);
        sampledGraph.getViewport().setScalable(true);

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

    protected void onStop() {
        Log.e("On Stop", "thank you");
        try {
            FileOutputStream fileOutputStream = openFileOutput("Defects.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(String.valueOf(NumberOfDefects).getBytes());
            fileOutputStream.close();
        } catch (Exception e) {

        }
        super.onStop();
        //mSensorManager.unregisterListener(this);
    }

    protected void onDestroy() {
        Log.e("On Destroy ", "thank you");
        try {
            FileOutputStream fileOutputStream = openFileOutput("Defects.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(String.valueOf(NumberOfDefects).getBytes());
            fileOutputStream.close();
        } catch (Exception e) {

        }
        super.onDestroy();
        mSensorManager.unregisterListener(this);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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

                    if (SystemClock.elapsedRealtime() - sessionStartTime < 10000) {
                        currentSessionAccelReading.add(new Reading(event.timestamp, correctedAccelValues[2]));
                        ignoreTimeOver = false;
                    } else if (!ignoreTimeOver) {
                        ignoreTimeOver = true;
                        Drawable tempImage = getResources().getDrawable(R.drawable.rec);
                        startButton.setImageDrawable(tempImage);
                        plotNewData();
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


    public void startRecording(View v) {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.ring);
        mp.start();
        Drawable tempImage = getResources().getDrawable(R.drawable.temprec);
        startButton.setImageDrawable(tempImage);
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
            }
        }.start();
        sessionStartTime = SystemClock.elapsedRealtime();
        currentSessionAccelReading.clear();
    }


    public void displayExceptionMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public void plotNewData() {


        double[] tempSampledVals = getSampledReadings(Integer.parseInt(samplingRateTextBox.getText().toString()), currentSessionAccelReading, 10);
        float[] currentSampledAccelVals = new float[tempSampledVals.length];
        float[] currentAccelVals = new float[currentSessionAccelReading.size()];
        for (int i = 0; i < tempSampledVals.length; i++) //casting double to float.
        {
            currentSampledAccelVals[i] = (float) tempSampledVals[i];

        }

        for (int i = 0; i < currentSessionAccelReading.size(); i++) {
            currentAccelVals[i] = currentSessionAccelReading.get(i).value;
        }


        graphZValues.clear();
        sampledGraphZValues.clear();
        for (int i = 0; i < currentSessionAccelReading.size(); i++) {
            graphZValues.add(new DataPoint(i, currentAccelVals[i]));

        }

        for (int i = 0; i < currentSampledAccelVals.length; i++) {
            sampledGraphZValues.add(new DataPoint(i, currentSampledAccelVals[i]));
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
        LineGraphSeries<DataPoint> sampledSeries = new LineGraphSeries<DataPoint>(sampledGraphZValues.toArray(new DataPoint[0]));
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(currentAccelVals.length);
        sampledGraph.getViewport().setMinX(0);
        sampledGraph.getViewport().setMaxX(currentSampledAccelVals.length);
        graph.removeAllSeries();
        graph.addSeries(series);
        sampledGraph.removeAllSeries();
        sampledGraph.addSeries(sampledSeries);
    }


    /**
     * uses linear interpolation and extrapolation to sample accelerometer readings for a given session length.
     *
     * @param samplingRate number of samples per second
     * @param readings     array representing the  timeline in nanoseconds of accelerometer readings
     * @param time         required session length in seconds
     * @return array of sampled readings
     */
    double[] getSampledReadings(int samplingRate, ArrayList<Reading> readings, int time) {
        double xFirst, yFirst, xSecond, ySecond, xInter, yInter;
        double Ts = 1.0 /(double) samplingRate * Math.pow(10, 9); //in nanoseconds
        int sampledReadingsCount = time * samplingRate + 1;
        double[] sampledReadings = new double[sampledReadingsCount];
        sampledReadings[0] = readings.get(0).value; //reading at t=0
        int i = 1, j = 1;
        double currentTime = Ts+readings.get(0).time;
        while (true) {
            while (i < readings.size() && currentTime > readings.get(i).time)
                i++;

            if (i == readings.size()) //recorded session is over
                break;

            xFirst = readings.get(i - 1).time;
            yFirst = readings.get(i - 1).value;
            xSecond = readings.get(i).time;
            ySecond = readings.get(i).value;
            xInter = currentTime;

            yInter = yFirst + (xInter - xFirst) / (xSecond - xFirst) * (ySecond - yFirst); //linear Interpolation
            sampledReadings[j] = yInter;
            j++;
            if (j == sampledReadingsCount) //sampling session  is over
                break;
            currentTime += Ts;
        }

        while (j < sampledReadingsCount) //assign last recorded reading to all the remaining samples (approximate extrapolation)
        {
            sampledReadings[j] = readings.get(i - 1).value;
            j++;
        }

        return sampledReadings;

    }
}





