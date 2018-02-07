package com.example.youssefhossam.samplingtest;

/**
 * Created by Youssef Hossam on 04/02/2018.
 */

import android.content.Intent;
import android.widget.Toast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
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
    GraphView graph, anamolyGraph;
    ArrayList<DataPoint> graphZValues, anamolyZValues;
    long lastResetTime=0;
    boolean isQueueBlocked=false;
    SensorHandler(AppCompatActivity activity,Double Threshold,GraphView graph,GraphView anamolyGraph)
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
        this.anamolyGraph=anamolyGraph;
        this.graph=graph;
        graphZValues=new ArrayList<DataPoint>();
        anamolyZValues=new ArrayList<DataPoint>();
    }
    private void extractReadings(long endTime)
    {
        while(endTime-readingsQ.peek().time>10*Math.pow(10,9))
            readingsQ.poll();
        Reading[]tempArray=new Reading[readingsQ.size()];
        LinkedBlockingQueue<Reading>tempQ=new LinkedBlockingQueue<Reading>(readingsQ);
        for(int i=0;i<tempArray.length;i++)
        {
            tempArray[i]=tempQ.poll();
        }
        anamolyZValues.clear();
        for (int i = 0; i < tempArray.length; i++) {
            anamolyZValues.add(new DataPoint(i, tempArray[i].value));
        }
        LineGraphSeries<DataPoint> anamolySeries = new LineGraphSeries<DataPoint>(anamolyZValues.toArray(new DataPoint[0]));
        anamolyGraph.getViewport().setMaxX(anamolyZValues.size());
        anamolyGraph.removeAllSeries();
        anamolyGraph.addSeries(anamolySeries);

    }

    public void onSensorChanged(SensorEvent event) {
        if(isQueueBlocked)
            return;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                accelValues[0] = event.values[0];
                accelValues[1] = event.values[1];
                accelValues[2] = event.values[2];
                accelValues[3] = 1;

                if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                    transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                    multiplyMV(correctedAccelValues, 0, rotationMatrixTranspose, 0, accelValues, 0);
                    graphZValues.add(new DataPoint(graphZValues.size(), correctedAccelValues[2]));
                    LineGraphSeries<DataPoint> series= new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
                    graph.removeAllSeries();
                    graph.addSeries(series);
                    if(event.timestamp-lastResetTime>20*Math.pow(10,9)) {
                        graphZValues.clear();
                        lastResetTime = event.timestamp;
                    }
                    if(readingsQ.size()<2000)
                        readingsQ.add(new Reading(event.timestamp,correctedAccelValues[2]));
                    else
                    {
                        readingsQ.poll();
                        readingsQ.add(new Reading(event.timestamp,correctedAccelValues[2]));
                    }
                    if(correctedAccelValues[2]>threshold)
                    {
                        lastAnamolyTime=event.timestamp;
                    }
                    else
                    {
                        if(lastAnamolyTime!=null && event.timestamp-lastAnamolyTime>2*Math.pow(10,9))
                        {

                            try{
                            extractReadings(event.timestamp);}
                            catch(Exception e)
                            {
                                displayExceptionMessage(e.getMessage());
                            }
                            lastAnamolyTime=null;
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

}
