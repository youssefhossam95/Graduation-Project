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
    CircleButton startButton;
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
        startButton=(CircleButton) findViewById(R.id.StartRecordingButton);
        uploadButton=(CircleButton)findViewById(R.id.uploadButton);
        currentSessionAccelReading=new ArrayList<Reading>();
        commentTextBox=(TextView) findViewById(R.id.textView1);
        typeTextBox=(TextView) findViewById(R.id.textView2);
        graphZValues=new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        this.context=getApplicationContext();
        String Result=readFromFile("Defects");
        Log.e("Data = ",Result);
        if(Result!="")
        {
            NumberOfDefects=Integer.valueOf(Result);
        }

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                uploadLocalData();

            }
        });
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
                        currentSessionAccelReading.add(new Reading(event.timestamp,correctedAccelValues[2]));
                        ignoreTimeOver=false;
                    }
                    else if(!ignoreTimeOver)
                    {
                        ignoreTimeOver=true;
                        Drawable tempImage = getResources().getDrawable(R.drawable.rec);
                        startButton.setImageDrawable(tempImage);
                        if(isNetworkAvailable())
                        {
                            promptSpeechInput();
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

            double [] tempSampledVals=getSampledReadings(1000,currentSessionAccelReading,10);
            float [] currentSampledAccelVals=new float[tempSampledVals.length];

            for(int i=0;i<tempSampledVals.length;i++) //casting double array to float.
            {
                currentSampledAccelVals[i]=(float)tempSampledVals[i];
            }
            try {
                saveData(currentSampledAccelVals, currentSessionAnamolyType, currentSessionLocation, userComment);
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
            for(int i=0;i<currentSessionAccelReading.size();i++) {
                graphZValues.add(new DataPoint(i, currentSampledAccelVals[i]));
            }
            typeTextBox.setText("Type  = "+s);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
            graph.removeAllSeries();
            graph.addSeries(series);
        }


    }



    Location getLocation()
    {
        return new Location("dummy"); //
    }

    void saveData(float[] accelValues, int anamolyType,Location location,String comment) throws JSONException {
        JSONArray jsArray = new JSONArray(accelValues);
        JSONObject jsonFile= new JSONObject();
        try
        {
            jsonFile.put("accelVal",jsArray);
            jsonFile.put("anamolyType",anamolyType);
            jsonFile.put("Location",location.toString());
            jsonFile.put("Comment", comment);
            jsonFile.put("_id",String.valueOf(android.os.Build.MODEL)+DateFormat.getDateTimeInstance().format(new Date()));
            ;
        }
        catch(Exception e){
            Log.e("log_tag", "Error in  JsonFIle "+e.toString());
            e.printStackTrace();

        }
        NumberOfDefects++;
        writeToFile("Object"+NumberOfDefects,jsonFile.toString());
        //    File F=new File(readFromFile());
        //   JSONObject jsonObj = new JSONObject(F.toString());
        //Log.e("doola",jsonObj.getString("_id"));//
    }

    public void uploadLocalData() {
        try
        {
            Log.e("Welcome","Uploading  NumberOfData= "+NumberOfDefects);
            String url="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/";
            int temp=NumberOfDefects;
            for(int i=0;i<temp;i++)
            {

                Log.e("Number of Defects",String.valueOf(NumberOfDefects));
                File F=new File(readFromFile("Object"+(i+1)));
                JSONObject jsonObj = new JSONObject(F.toString());
                BackgroundWorker BW=new BackgroundWorker(this.context,url,"POST");
                String Result=  BW.execute(jsonObj).get(2000, TimeUnit.MILLISECONDS);
                if(Integer.valueOf(Result)==201)
                {
                    Toast.makeText(this.context,"File "+(i+1)+" Uploaded Successfully",Toast.LENGTH_SHORT).show();
                    deleteFile("Object"+(i+1)+".txt");
                    NumberOfDefects--;
                }
                else
                {
                    Toast.makeText(this.context,"File "+(i+1)+" Not Uploaded Successfully",Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception E)
        {
            Log.e("Error ya dola",E.toString());
        }


    }

    private void writeToFile(String FileName,String Data) {
        try {
            FileOutputStream fileOutputStream =  openFileOutput(FileName+".txt", Context.MODE_PRIVATE);
            fileOutputStream.write(Data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public String readFromFile(String FileName) {

        String ret = "";
        try {
            InputStream inputStream = openFileInput(FileName+".txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
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
        double xFirst,yFirst,xSecond,ySecond,xInter,yInter;
        double Ts=1/samplingRate * Math.pow(10,9); //in nanoseconds
        int sampledReadingsCount=time*samplingRate+1;
        double[] sampledReadings=new double[sampledReadingsCount];
        sampledReadings[0]=readings.get(0).value; //reading at t=0
        int i=1,j=1;
        double currentTime=Ts;
        while(true)
        {
            while(i<readings.size() && currentTime>readings.get(i).time)
                i++;

            if(i==readings.size()) //recorded session is over
                break;

            xFirst=readings.get(i-1).time;
            yFirst=readings.get(i-1).value;
            xSecond=readings.get(i).time;
            ySecond=readings.get(i).value;
            xInter=currentTime;

            yInter=yFirst+(xInter-xFirst)/(xSecond-xFirst)*(ySecond-yFirst); //linear Interpolation
            sampledReadings[j]=yInter;
            j++;
            if(j==sampledReadingsCount) //sampling session  is over
                break;
            currentTime+=Ts;
        }

        while(j<sampledReadingsCount) //assign last recorded reading to all the remaining samples (approximate extrapolation)
        {
            sampledReadings[j]=readings.get(i-1).value;
            j++;
        }

        return sampledReadings;

    }
}




