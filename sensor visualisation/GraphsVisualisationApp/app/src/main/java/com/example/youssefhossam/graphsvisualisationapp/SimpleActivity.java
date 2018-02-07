package com.example.youssefhossam.graphsvisualisationapp;
import android.content.pm.PackageManager;
import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
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
public class SimpleActivity extends AppCompatActivity implements Serializable {

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
    private Context context;
    //NEW
    EditText commentBoxText;
    static final int REQUEST_LOCATION=1;
    private double longitude;
    private double latitude;
    private Location myLocation;
    private String address;
    private FusedLocationProviderClient mFusedLocationClient;
    public final static int SAMPLINGRATE=120; // number of samples per second (Fs)
    private SeekBar sensitivityThreshold;
    private Double senstivThreshold;
    private SensorHandler mySensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        ContextHolder contextHolder=new ContextHolder();
        contextHolder.setContext(getApplicationContext());
        mySensor=new SensorHandler(this,senstivThreshold);
        sessionStartTime=0;
        fileHandler =new FileHandler();
        startButton=(CircleButton) findViewById(R.id.StartRecordingButton);
        uploadButton=(CircleButton)findViewById(R.id.uploadButton);
        currentSessionAccelReading=new ArrayList<Reading>();
        commentTextBox=(TextView) findViewById(R.id.textView1);
        typeTextBox=(TextView) findViewById(R.id.textView2);
        graphZValues=new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        this.context=getApplicationContext();
        sensitivityThreshold=findViewById(R.id.sensitivityThreshold);
        sensitivityThreshold.setMax(100);
        String Result=fileHandler.readFromFile("Defects");
        Log.e("Data = ",Result);
        if(Result!="")
        {
            fileHandler.NumberOfDefects=Integer.valueOf(Result);
        }
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                if(!fileHandler.uploadLocalData())
                {
                    displayExceptionMessage("No Files To Be Uploaded ");
                }

            }
        });
        sensitivityThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue =  ((float)i / 10.0);
                senstivThreshold=progressChangedValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SimpleActivity.this, "Sensitivity Threshold  :" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }
        });
        //NEW
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    protected void onResume() {
        super.onResume();
        Log.e("Simple Activity of Def",String.valueOf(fileHandler.getNumberOfDefects()));
    }
    protected void onPause() {
        super.onPause();

    }
    protected void onStop() {
        Log.e("On Stop","thank you");
        try{
            FileOutputStream fileOutputStream =  openFileOutput("Defects.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(String.valueOf(fileHandler.NumberOfDefects).getBytes());
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
            fileOutputStream.write(String.valueOf(fileHandler.NumberOfDefects).getBytes());
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
        mySensor=new SensorHandler(this,senstivThreshold);
        currentSessionLocation=getLocation();
    }
    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2)
        {
            if(resultCode == RESULT_OK) {
             fileHandler=data.getExtras().getParcelable("fileHandler");
            }
        }
        else
        {
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


    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.viewFileButton:
                //your code
                // EX : call intent if you want to swich to other activity
                displayExceptionMessage("View Files Counts = "+fileHandler.NumberOfDefects);

                return true;
            case R.id.aboutButton:
                Intent myIntent = new Intent(getApplicationContext(), viewFiles.class);
                myIntent.putExtra("myFile",fileHandler);
                startActivityForResult(myIntent,2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    Location getLocation()
    {
        Location location= new Location("");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {


                        if (location != null) {


                            longitude=location.getLongitude();
                            latitude=location.getLatitude();
                            currentSessionLocation=location;
                            ((TextView)findViewById(R.id.longitudeText)).setText(""+longitude);
                            ((TextView)findViewById(R.id.latitudeText)).setText(""+latitude);
                            //((TextView)findViewById(R.id.addressText)).setText(""+getAddress(latitude,longitude));
                        }
                        else
                        {
                            ((TextView)findViewById(R.id.longitudeText)).setText("Can't find the location");
                            ((TextView)findViewById(R.id.latitudeText)).setText("Can't find the location");
                            //  ((TextView)findViewById(R.id.addressText)).setText("Can't find the location");

                        }

                    }
                });


        return location;

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
        if(readings.size()==0)
        {
            return new double[10];
        }
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



