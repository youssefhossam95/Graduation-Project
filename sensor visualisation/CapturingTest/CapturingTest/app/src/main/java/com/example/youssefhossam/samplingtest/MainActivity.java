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

public class MainActivity extends AppCompatActivity  {

    float[] accelValues;
    float[] correctedAccelValues;
    float[] gravityValues;
    float[] magnetValues;
    float[] rotationMatrix;
    float[] rotationMatrixTranspose;
    long sessionStartTime;
    public final static int UNKNOWN = 0, MATAB = 1, HOFRA = 2, TAKSER = 3, GHLAT = 4, HARAKA = 5;
    GraphView graph, anamolyGraph;
    ArrayList<DataPoint> graphZValues, anamolyZValues;
    SensorHandler sensor;
    Double threshold=2.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotationMatrix = new float[16];
        rotationMatrixTranspose = new float[16];
        accelValues = new float[4];
        correctedAccelValues = new float[4];
        gravityValues = new float[3];
        magnetValues = new float[3];
        sessionStartTime = 0;
        graphZValues = new ArrayList<DataPoint>();
        anamolyZValues = new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        anamolyGraph = (GraphView) findViewById(R.id.graph2);
        graph.getViewport().setXAxisBoundsManual(true);
        anamolyGraph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(3000);
        graph.getViewport().setScalable(true);
        sensor=new SensorHandler(this,threshold,graph,anamolyGraph);
    }


    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }

    protected void onStop() {
        Log.e("On Stop", "thank you");
        try {
            FileOutputStream fileOutputStream = openFileOutput("Defects.txt", Context.MODE_PRIVATE);
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
            fileOutputStream.close();
        } catch (Exception e) {

        }
        super.onDestroy();

    }

    public void displayExceptionMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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





