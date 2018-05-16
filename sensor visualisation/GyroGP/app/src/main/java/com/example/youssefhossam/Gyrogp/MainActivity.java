package com.example.youssefhossam.Gyrogp;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mMagnetic;
    private Sensor mGyro;
    float[] accelValues;
    float[] correctedAccelValues;
    float[] gravityValues;
    float[] magnetValues;
    float[] rotationMatrix;
    float[] rotationMatrixTranspose;
    GraphView gyroGraph;
    GraphView gravityGraph;
    long gravityStartTime;
    long gyroStartTime;
    long lastReadingTime;
    ArrayList<DataPoint> gyroGraphZValues;
    int gravitySamplesCounter = 0;
    Button startButton;
    EditText timeBox;
    TextView avgText;
    boolean isRecording=false;
    LineGraphSeries<DataPoint> gravitySeries;
    boolean isAccelStarted=false;
    double avgSamplingPeriod=5; //hankhod datapoint kol kam reading.
    long defaultSamplingPeriod=100000000;
    long accelreadingsCount=0;
    double currentTotal=0;
    long lastAvgRefresh;
    double difference,val;
    float[] prevGravity;
    double cosSim;
    int counter=0;
    LinkedBlockingQueue<float[]> gravHistory=new LinkedBlockingQueue<>();
    LineGraphSeries<DataPoint> gyroSeries;
    private int gyroSamplesCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyro= mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,mGyro,SENSOR_DELAY_FASTEST);
        rotationMatrix = new float[16];
        rotationMatrixTranspose = new float[16];
        accelValues = new float[4];
        correctedAccelValues = new float[4];
        gravityValues = new float[3];
        magnetValues = new float[3];
        gyroGraph = (GraphView) findViewById(R.id.graph);
        gravityGraph=(GraphView)findViewById(R.id.graph1);
        gravityStartTime = SystemClock.elapsedRealtime();
        gyroStartTime=SystemClock.elapsedRealtime();
        gyroGraphZValues = new ArrayList<DataPoint>();
//        startButton = (Button) findViewById(R.id.button);
//        startButton.setBackgroundColor(Color.GREEN);
        timeBox=(EditText)findViewById(R.id.editText);
        timeBox.setText("0.99");
        gravitySeries=new LineGraphSeries<DataPoint>();
        gyroSeries=new LineGraphSeries<DataPoint>();
        gravityGraph.addSeries(gravitySeries);
        gyroGraph.addSeries(gyroSeries);
        gravityGraph.getViewport().setXAxisBoundsManual(true);
        gyroGraph.getViewport().setXAxisBoundsManual(true);
        gyroGraph.getViewport().setMinX(0);
        gyroGraph.getViewport().setMaxX(1000);
        gravityGraph.getViewport().setYAxisBoundsManual(true);
        gyroGraph.getViewport().setYAxisBoundsManual(true);
        gravityGraph.getViewport().setMinX(0);
        gravityGraph.getViewport().setMaxX(1000);
        gravityGraph.getViewport().setMinY(0.9);
        gravityGraph.getViewport().setMaxY(1);
        gyroGraph.getViewport().setMinY(-3);
        gyroGraph.getViewport().setMaxY(3);
        avgText=(TextView)findViewById(R.id.textView);

    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,mGyro , SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE: {
                accelValues[0] = event.values[0];
                accelValues[1] = event.values[1];
                accelValues[2] = event.values[2];
                accelValues[3] = 1;
                if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                    transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                    multiplyMV(correctedAccelValues, 0, rotationMatrixTranspose, 0, accelValues, 0);
                    //if(isRecording)
                    updateGyroGraph();

                }
                break;
            }
            case Sensor.TYPE_GRAVITY: {
                gravityValues = event.values.clone();
                updateGravityGraph();
                if(gravHistory.size()>10)
                    prevGravity=gravHistory.poll().clone();

                gravHistory.add(gravityValues.clone());
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                magnetValues = event.values.clone();
                break;
            }
        }


    }

    void updateGravityGraph() {
        try {
            if(prevGravity==null)
                return;
            double gravityMag=Math.sqrt(Math.pow(gravityValues[0],2)+Math.pow(gravityValues[1],2)+Math.pow(gravityValues[2],2));
            double prevGravityMag=Math.sqrt(Math.pow(prevGravity[0],2)+Math.pow(prevGravity[1],2)+Math.pow(prevGravity[2],2));
            float dot=0;

            for(int i=0;i<prevGravity.length;i++){
                dot+=(prevGravity[i]*gravityValues[i]);
            }

            cosSim=dot/(prevGravityMag*gravityMag);
            counter++;
            if(cosSim<Float.parseFloat(timeBox.getText().toString())&&counter>15){
                MediaPlayer ring = MediaPlayer.create(this, R.raw.ring);
                counter=0;
                ring.start();
            }
            
            if (gravitySamplesCounter == 0) //first call after button press
                gravityStartTime = SystemClock.elapsedRealtime();
            gravitySeries.appendData(new DataPoint(gravitySamplesCounter,cosSim),false,1000000);
            gravitySamplesCounter++;
            if (SystemClock.elapsedRealtime() - gravityStartTime >= 10 * 1000) {
                gravitySamplesCounter = 0;
                gravityGraph.getViewport().setMaxX(100 * 10);
                gravitySeries.resetData(new DataPoint[0]);
                gravityGraph.getViewport().setMaxX(150* 10);
            }
        }
        catch(Exception e)
        {
            displayExceptionMessage(e.getMessage());
        }


    }


    void updateGyroGraph() {
        try {

                if (gyroSamplesCounter == 0) //first call after button press
                    gyroStartTime = SystemClock.elapsedRealtime();
                gyroSeries.appendData(new DataPoint(gyroSamplesCounter,correctedAccelValues[1]),false,1000000);
                gyroSamplesCounter++;
                if (SystemClock.elapsedRealtime() - gyroStartTime >= 10* 1000) {
                    gyroSamplesCounter = 0;
    //                startButton.setBackgroundColor(Color.GREEN);
    //                startButton.setText("Start");
                    gyroGraph.getViewport().setMaxX(100 * 10);
                    gyroSeries.resetData(new DataPoint[0]);
                    gyroGraph.getViewport().setMaxX(150* 10);
                }
        }
        catch(Exception e)
        {
            displayExceptionMessage(e.getMessage());
        }


    }


    //    public void startRecording(View v)
//    {
//        isRecording=true;
//        startButton.setBackgroundColor(Color.RED);
//        startButton.setText("Recording");
//    }
public static String getHTML() throws Exception {
    String urlToRead="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix:32bd9c8968033ee6735bcb0919ed2f85f759c8bc8a0a51c03d6cd2647eeb7e66@ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/bumpsdb/Bump";
    StringBuilder result = new StringBuilder();
    URL url = new URL(urlToRead);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
        result.append(line);
    }
    rd.close();
    return result.toString();
}


    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        SingularValueDecomposition
    }

}
