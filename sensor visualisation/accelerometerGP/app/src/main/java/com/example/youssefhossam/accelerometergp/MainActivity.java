package com.example.youssefhossam.accelerometergp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
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
    GraphView graph;
    GraphView realTimeGraph;
    long startTime;
    long lastReadingTime;
    ArrayList<DataPoint> graphZValues;
    int samplesCounter = 0;
    Button startButton;
    EditText timeBox;
    TextView avgText;
    boolean isRecording=false;
    LineGraphSeries<DataPoint> realTimeSeries;
    boolean isAccelStarted=false;
    double avgSamplingPeriod=5; //hankhod datapoint kol kam reading.
    long defaultSamplingPeriod=100000000;
    long accelreadingsCount=0;
    double currentTotal=0;
    long lastAvgRefresh;
    double difference,val;

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
        graph = (GraphView) findViewById(R.id.graph);
        realTimeGraph=(GraphView)findViewById(R.id.graph1);
        startTime = SystemClock.elapsedRealtime();
        graphZValues = new ArrayList<DataPoint>();
//        startButton = (Button) findViewById(R.id.button);
//        startButton.setBackgroundColor(Color.GREEN);
        timeBox=(EditText)findViewById(R.id.editText);
        timeBox.setText("10");
        realTimeSeries=new LineGraphSeries<DataPoint>();
        realTimeGraph.addSeries(realTimeSeries);
        realTimeGraph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1000);
        realTimeGraph.getViewport().setMinX(0);
        realTimeGraph.getViewport().setMaxX(1000);
        avgText=(TextView)findViewById(R.id.textView);

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
                accelValues[0] = event.values[0];
                accelValues[1] = event.values[1];
                accelValues[2] = event.values[2];
                accelValues[3] = 1;
                if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                    transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                    multiplyMV(correctedAccelValues, 0, rotationMatrixTranspose, 0, accelValues, 0);
                    //if(isRecording)
                        updateGraph();
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

    void updateGraph() {
        try {
            graphZValues.add(new DataPoint(samplesCounter, correctedAccelValues[2]));
            if (samplesCounter == 0) //first call after button press
                startTime = SystemClock.elapsedRealtime();
            realTimeSeries.appendData(new DataPoint(samplesCounter,correctedAccelValues[2]),false,1000000);

            samplesCounter++;
            if (SystemClock.elapsedRealtime() - startTime >= Integer.parseInt(timeBox.getText().toString()) * 1000) {
                samplesCounter = 0;
                isRecording = false;
//                startButton.setBackgroundColor(Color.GREEN);
//                startButton.setText("Start");
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
                realTimeGraph.getViewport().setMaxX(100 * Integer.parseInt(timeBox.getText().toString()));
                graph.getViewport().setMaxX(100 * Integer.parseInt(timeBox.getText().toString()));
                graph.removeAllSeries();
                graph.addSeries(series);
                graphZValues.clear();
                realTimeSeries.resetData(new DataPoint[0]);
                realTimeGraph.getViewport().setMaxX(150* Integer.parseInt(timeBox.getText().toString()));
                graph.getViewport().setMaxX(150 * Integer.parseInt(timeBox.getText().toString()));
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
    }

}
