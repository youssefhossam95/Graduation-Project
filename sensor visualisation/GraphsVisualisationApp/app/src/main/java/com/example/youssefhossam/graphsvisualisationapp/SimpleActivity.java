package com.example.youssefhossam.graphsvisualisationapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import at.markushi.ui.CircleButton;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;
import android.location.Location;

import org.w3c.dom.Text;

public class SimpleActivity extends AppCompatActivity implements SensorEventListener {


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
    Button startButton;
    public final static int UNKNOWN=0,MATAB=1,HOFRA=2,TAKSER=3,GHLAT=4,HARAKA=5;
    Integer currentSessionAnamolyType=UNKNOWN;
    ArrayList<Double> currentSessionAccelReading;
    boolean isVoiceActivityDone=true, isRecording=false,ignoreTimeOver=true;
    String userComment;
    Location currentSessionLocation;
    GraphView graph;
    ArrayList<DataPoint> graphZValues;
    TextView commentTextBox;
    TextView typeTextBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
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
        sessionStartTime=0;
        startButton=(Button) findViewById(R.id.startButton);
        currentSessionAccelReading=new ArrayList<Double>();
        commentTextBox=(TextView) findViewById(R.id.textView1);
        typeTextBox=(TextView) findViewById(R.id.textView2);
        graphZValues=new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
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
                    
                    if(SystemClock.elapsedRealtime()-sessionStartTime <10000)
                    {
                        currentSessionAccelReading.add((double)correctedAccelValues[2]);
                        ignoreTimeOver=false;
                    }
                    else if(!ignoreTimeOver) 
                    {
                        ignoreTimeOver=true;
                        startButton.setBackgroundColor(Color.GRAY);
                        startButton.setText("Start Recording");
                        promptSpeechInput();
                        
                        
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

        public void startRecording(View v)
    {
        startButton.setBackgroundColor(Color.GREEN);
        startButton.setText("Recording");
        sessionStartTime=SystemClock.elapsedRealtime();
        currentSessionAccelReading.clear();
        currentSessionLocation=getLocation();
    }


    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
        currentSessionAnamolyType=UNKNOWN;
        if (resultCode == RESULT_OK && null != data) {

            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
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

            saveData(currentSessionAccelReading,currentSessionAnamolyType,currentSessionLocation,userComment);

            for(int i=0;i<currentSessionAccelReading.size();i++) {
                graphZValues.add(new DataPoint(i, currentSessionAccelReading.get(i)));
            }
            commentTextBox.setText(userComment);
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
            typeTextBox.setText(s);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
            graph.addSeries(series);
        }


        }


    Location getLocation()
    {
        return new Location("dummy"); //
    }

    void saveData(ArrayList<Double> accelValues, int anamolyType,Location location,String comment)
    {

    }

    void uploadLocalData()
    {

    }




}




