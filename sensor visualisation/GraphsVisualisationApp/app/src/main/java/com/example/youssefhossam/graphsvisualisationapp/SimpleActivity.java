package com.example.youssefhossam.graphsvisualisationapp;

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
import 	org.json.JSONArray;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.lang.Object;
public class SimpleActivity extends AppCompatActivity  {

    long sessionStartTime;
    CircleButton startButton;
    FileHandler fileHandler;
    CircleButton uploadButton;
    public final static int UNKNOWN=0,MATAB=1,HOFRA=2,TAKSER=3,GHLAT=4,HARAKA=5;
    int currentSessionAnamolyType=UNKNOWN;
    ArrayList<Reading> currentSessionAccelReading;
    boolean isVoiceActivityDone=true, isRecording=false,ignoreTimeOver=true;
    String userComment;
    Location currentSessionLocation;
    GraphView graph;
    ArrayList<DataPoint> graphZValues;
    TextView commentTextBox;
    TextView typeTextBox;
    int NumberOfDefects=0;
    private Context context;
    public final static int SAMPLINGRATE=120; // number of samples per second (Fs)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        SensorHandler mySensor=new SensorHandler(this);
        sessionStartTime=0;
        fileHandler =new FileHandler(this);
        startButton=(CircleButton) findViewById(R.id.StartRecordingButton);
        uploadButton=(CircleButton)findViewById(R.id.uploadButton);
        currentSessionAccelReading=new ArrayList<Reading>();
        commentTextBox=(TextView) findViewById(R.id.textView1);
        typeTextBox=(TextView) findViewById(R.id.textView2);
        graphZValues=new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        this.context=getApplicationContext();
        String Result=fileHandler.readFromFile("Defects");
        Log.e("Data = ",Result);
        if(Result!="")
        {
            NumberOfDefects=Integer.valueOf(Result);
        }

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                fileHandler.uploadLocalData();

            }
        });
    }
    protected void onResume() {
        super.onResume();
    }
    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }
    protected void onStop() {
        Log.e("On Stop","thank you");
        try{
            FileOutputStream fileOutputStream =  openFileOutput("Defects.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(String.valueOf(NumberOfDefects).getBytes());
            fileOutputStream.close();
        }
        catch(Exception e)
        {

        }
        super.onStop();
        //mSensorManager.unregisterListener(this);
    }
    protected void onDestroy(){
        Log.e("On Destroy ","thank you");
        try{
            FileOutputStream fileOutputStream =  openFileOutput("Defects.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(String.valueOf(NumberOfDefects).getBytes());
            fileOutputStream.close();
        }
        catch(Exception e)
        {

        }
        super.onDestroy();


    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void startRecording(View v)
    {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.ring);
        mp.start();
        Drawable tempImage = getResources().getDrawable(R.drawable.temprec);
        startButton.setImageDrawable(tempImage);
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                commentTextBox.setText("Seconds Remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                commentTextBox.setText("Done!");
            }
        }.start();
        sessionStartTime=SystemClock.elapsedRealtime();
        currentSessionAccelReading.clear();
        currentSessionLocation=getLocation();
    }
    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        currentSessionAnamolyType=UNKNOWN;
        if (resultCode == RESULT_OK && null != data) {

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            userComment=result.get(0);

            for(String s:result) //search for keywords
            {
                if(s.contains("مطب"))
                {
                    currentSessionAnamolyType=MATAB;
                    break;
                }
                else if(s.contains("حفره") || s.contains("حفرة"))
                {
                    currentSessionAnamolyType=HOFRA;
                    break;
                }
                else if(s.contains("تكسير"))
                {
                    currentSessionAnamolyType=TAKSER;
                    break;
                }
                else if(s.contains("غلط"))
                {
                    currentSessionAnamolyType=GHLAT;
                    break;
                }
                else if(s.contains("حركة") || s.contains("حركه"))
                {
                    currentSessionAnamolyType=HARAKA;
                    break;
                }
            }

            double [] tempSampledVals=getSampledReadings(currentSessionAccelReading,10);
            float [] currentSampledAccelVals=new float[tempSampledVals.length];

            for(int i=0;i<tempSampledVals.length;i++) //casting double array to float.
            {
                currentSampledAccelVals[i]=(float)tempSampledVals[i];
            }
            try {
                fileHandler.saveData(currentSampledAccelVals, currentSessionAnamolyType, currentSessionLocation, userComment);
            }
            catch (org.json.JSONException exception)
            {
                displayExceptionMessage(exception.getMessage());
            }

            commentTextBox.setText("Your Comment = "+userComment);
            String s="";
            switch(currentSessionAnamolyType) {
                case UNKNOWN:
                    s="UNKNOWN";
                    break;
                case MATAB:
                    s="MATAB";
                    break;
                case HOFRA:
                    s="HOFRA";
                    break;
                case TAKSER:
                    s="TAKSER";
                    break;
                case GHLAT:
                    s="GHLAT";
                    break;
                case HARAKA:
                    s="HARAKA";
                    break;
            }

            graphZValues.clear();
            for(int i=0;i<currentSampledAccelVals.length;i++) {
                graphZValues.add(new DataPoint(i, currentSampledAccelVals[i]));
            }
            typeTextBox.setText("Type  = "+s);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(currentSampledAccelVals.length);
            graph.removeAllSeries();
            graph.addSeries(series);
        }


    }



    Location getLocation()
    {
        return new Location("dummy"); //
    }





    /**
     * uses linear interpolation and extrapolation to sample accelerometer readings for a given session length.
     * @param readings array representing the  timeline in nanoseconds of accelerometer readings
     * @param time required session length in seconds
     * @return array of sampled readings
     */
    double[] getSampledReadings(ArrayList<Reading> readings, int time) {
        double xFirst, yFirst, xSecond, ySecond, xInter, yInter;
        double Ts = 1.0 /(double) SAMPLINGRATE* Math.pow(10, 9); //in nanoseconds
        int sampledReadingsCount = time * SAMPLINGRATE + 1;
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



