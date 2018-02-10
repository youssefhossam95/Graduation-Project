package com.example.youssefhossam.graphsvisualisationapp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.hitomi.cmlibrary.OnMenuStatusChangeListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import at.markushi.ui.CircleButton;

public class SimpleActivity extends AppCompatActivity implements Serializable {
    long sessionStartTime;
    FileHandler fileHandler;
    CircleButton uploadButton;
    public final static int UNKNOWN=0,MATAB=1,HOFRA=2,TAKSER=3,GHLAT=4,HARAKA=5;
    int currentSessionAnamolyType=UNKNOWN;
    ArrayList<Reading> currentSessionAccelReading;
    boolean isVoiceActivityDone=true, isRecording=false,ignoreTimeOver=true;
    String userComment;
    GraphView graph;
    ArrayList<DataPoint> graphZValues;
    TextView commentTextBox;
    TextView typeTextBox;
    private Context context;
    //NEW
    CircleMenu circleMenu;
    EditText commentBoxText;
    public final static int SAMPLINGRATE=120; // number of samples per second (Fs)
    private SeekBar sensitivityThreshold;
    private Double senstivThreshold=2.0;
    private SensorHandler mySensor;
    Anamoly lastAnamoly;
    static Boolean isVoiceMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        ContextHolder contextHolder=new ContextHolder();
        contextHolder.setContext(getApplicationContext());
        final ToggleButton toggleButton=(ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setChecked(true);
        isVoiceMode=true;
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    isVoiceMode=true;
                    toggleButton.setTextOn("Voice Mode");
                } else {
                    isVoiceMode=false;
                    toggleButton.setTextOff("Buttons Mode");
                }
            }
        });
        sessionStartTime=0;
        fileHandler =new FileHandler();
        uploadButton=(CircleButton)findViewById(R.id.uploadButton);
        currentSessionAccelReading=new ArrayList<Reading>();
        commentTextBox=(TextView) findViewById(R.id.commentTextBox);
        typeTextBox=(TextView) findViewById(R.id.typeTextBox);
        graphZValues=new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        this.context=getApplicationContext();
        sensitivityThreshold=findViewById(R.id.sensitivityThreshold);
        sensitivityThreshold.setMax(30);
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
            double progressChangedValue = 2.00;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue =  ((double)i / 10.0);
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
        circleMenu = (CircleMenu) findViewById(R.id.circle_menu);
        circleMenu.setMainMenu(Color.parseColor("#03986f"), R.mipmap.icon_menu, R.mipmap.icon_cancel)
                .addSubMenu(Color.parseColor("#fddd00"), R.mipmap.icon_bump)
                .addSubMenu(Color.parseColor("#FFFFFF"), R.mipmap.icon_potholes)
                .addSubMenu(Color.parseColor("#d00e0e"), R.mipmap.icon_wrong)
                .addSubMenu(Color.parseColor("#000000"), R.mipmap.icon_vibration)
                .addSubMenu(Color.parseColor("#FFFFFF"), R.mipmap.icon_cracks)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {

                    @Override
                    public void onMenuSelected(int index) {
                        switch (index)
                        {
                            case 0 :
                                displayExceptionMessage("You have chosen مطب");
                                return;
                            case 1 :
                                displayExceptionMessage("You have chosen نقرة");
                                return;
                            case 2 :
                                displayExceptionMessage("You have chosen غلط ");
                                return;
                            case 3 :
                                displayExceptionMessage("You have chosen حركة");
                                return;
                            case 4 :
                                displayExceptionMessage("You have chosen تكسير ");
                                return;
                        }



                    }

                }).setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {

            @Override
            public void onMenuOpened() {}

            @Override
            public void onMenuClosed() {}

        });
        mySensor=new SensorHandler(this,senstivThreshold,lastAnamoly,isVoiceMode,circleMenu);
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
    public void displayExceptionMessage(String msg) {
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
            if (resultCode == RESULT_OK && requestCode==100&& null != data) {
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


       /*        try {
                    fileHandler.saveData(lastAnamoly); //dola ezbot el ada2(khod object mn anamoly)
               }
               catch (org.json.JSONException exception)
                {
                    displayExceptionMessage(exception.getMessage());
                }*/

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
                long relativeTime=10;
                for(Reading reading:lastAnamoly.readings) {
                    relativeTime=(reading.time-lastAnamoly.readings[0].time)/(long)Math.pow(10,9);
                    graphZValues.add(new DataPoint(relativeTime, reading.value));
                }
                typeTextBox.setText("Type  = "+s);
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX((int)relativeTime);
                graph.removeAllSeries();
                graph.addSeries(series);
                lastAnamoly=null;
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
                displayExceptionMessage("View Files Counts = "+fileHandler.NumberOfDefects);
                Intent myIntent = new Intent(getApplicationContext(), viewFiles.class);
                myIntent.putExtra("myFile",fileHandler);
                startActivityForResult(myIntent,2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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



