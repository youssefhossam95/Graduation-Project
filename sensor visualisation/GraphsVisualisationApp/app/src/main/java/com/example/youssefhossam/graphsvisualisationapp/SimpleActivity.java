package com.example.youssefhossam.graphsvisualisationapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
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
    TextView longitudeText;
    TextView latitudeText;
    public final static int MATAB = 0, HOFRA = 1,GHLAT = 2, HARAKA = 3, TAKSER = 4, UNKNOWN=5;
    int currentSessionAnamolyType = UNKNOWN;
    ArrayList<Reading> currentSessionAccelReading;
    String userComment;
    GraphView graph;
    ArrayList<DataPoint> graphZValues;
    TextView commentTextBox;
    TextView commentTextView;
    TextView typeTextBox;
    TextView fileNumbersText;
    private Context context;
    int type = 0;
    String comment = "";
    //NEW
    CircleMenu circleMenu;
    public final static int SAMPLINGRATE = 120; // number of samples per second (Fs)
    private SeekBar sensitivityThreshold;
    private SensorHandler mySensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        statusGPSCheck();
        ContextHolder contextHolder = new ContextHolder();
        contextHolder.setContext(getApplicationContext());
        circleMenu = (CircleMenu) findViewById(R.id.circle_menu);
        circleMenu.setMainMenu(Color.parseColor("#53aaa8"), R.mipmap.icon_menu, R.mipmap.icon_cancel)
                .addSubMenu(Color.parseColor("#fddd00"), R.mipmap.icon_bump)
                .addSubMenu(Color.parseColor("#FFFFFF"), R.mipmap.icon_potholes)
                .addSubMenu(Color.parseColor("#d00e0e"), R.mipmap.icon_wrong)
                .addSubMenu(Color.parseColor("#000000"), R.mipmap.icon_vibration)
                .addSubMenu(Color.parseColor("#FFFFFF"), R.mipmap.icon_cracks)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {

                    @Override
                    public void onMenuSelected(int index) {

                        if (mySensor.isVoiceMode)
                            return;

                        if (mySensor.lastAnamoly == null && !mySensor.isStillProcessing.get()) {
                            displayExceptionMessage("Sorry No Data To Classify You Missed It");
                            return;
                        }

                        type=index;

                        switch (index) {
                            case MATAB: {
                                displayExceptionMessage("You have chosen مطب");
                                comment = "مطب";
                                break;
                            }
                            case HOFRA: {
                                displayExceptionMessage("You have chosen نقرة");
                                comment = "نقرة";
                                break;
                            }
                            case GHLAT: {
                                displayExceptionMessage("You have chosen غلط ");
                                comment = "غلط";
                                break;
                            }
                            case HARAKA: {
                                displayExceptionMessage("You have chosen حركة");
                                comment = "حركة";
                                break;
                            }

                            case TAKSER: {
                                displayExceptionMessage("You have chosen تكسير ");
                                comment = "تكسير";
                                break;
                            }
                        }

                        typeTextBox.setText("Type  = " + comment);
                        if (mySensor.lastAnamolyLoc != null) {
                            longitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLongitude()));
                            latitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLatitude()));
                            displayExceptionMessage("bdan 1");
                        }
                        else
                        {
                            displayExceptionMessage("Location not available! file not saved");
                            return;
                        }

                        Thread t = new Thread() {
                            public void run() {
                                displayExceptionMessage("zeeby 2");
                                while (mySensor.isStillProcessing.get()) ;
                                try {
                                    displayExceptionMessage("zeeby 3");
                                    mySensor.lastAnamoly.type = type;
                                    mySensor.lastAnamoly.comment = comment;
                                    displayExceptionMessage("khwal 4");
                                    fileHandler.saveData(mySensor.lastAnamoly);
                                    saveDefectsValues();
                                    displayExceptionMessage("3rs 5");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            displayExceptionMessage("mtnak 6");
                                            drawGraphData();
                                            displayExceptionMessage("shrmot 7");
                                        }
                                    });

                                } catch (Exception e) {
                                   displayExceptionMessage(e.getMessage());
                                }
                            }
                        };
                        t.start();

                    }

                }).setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {

            @Override
            public void onMenuOpened() {
            }

            @Override
            public void onMenuClosed() {
            }

        });

        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        mySensor = new SensorHandler(this, circleMenu);
        if (!isNetworkAvailable()) {
            toggleButton.setChecked(false);
            mySensor.isVoiceMode = false;

        } else {
            toggleButton.setChecked(true);
            mySensor.isVoiceMode = true;
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isNetworkAvailable()) {
                        toggleButton.setTextOn("Voice Mode");
                        mySensor.isVoiceMode=true;
                        commentTextBox.setVisibility(View.VISIBLE);
                        commentTextView.setVisibility(View.VISIBLE);
                    } else {
                        displayExceptionMessage("To Enable Voice Mode Please Check Your Internet Connection");
                        toggleButton.setChecked(false);
                    }

                } else {

                    toggleButton.setTextOff("Buttons Mode");
                    commentTextBox.setVisibility(View.INVISIBLE);
                    commentTextView.setVisibility(View.INVISIBLE);
                    mySensor.isVoiceMode=false;
                }
            }
        });
        sessionStartTime = 0;
        fileNumbersText = (TextView) findViewById(R.id.fileNumbersText);
        ;
        fileHandler = new FileHandler();
        uploadButton = (CircleButton) findViewById(R.id.uploadButton);
        currentSessionAccelReading = new ArrayList<Reading>();
        commentTextBox = (TextView) findViewById(R.id.commentTextBox);
        typeTextBox = (TextView) findViewById(R.id.typeTextBox);
        longitudeText = (TextView) findViewById(R.id.longitudeText);
        latitudeText = (TextView) findViewById(R.id.latitudeText);
        graphZValues = new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        this.context = getApplicationContext();
        sensitivityThreshold = findViewById(R.id.sensitivityThreshold);
        commentTextView=findViewById(R.id.CommentLayOut);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable()) {
                    if (!fileHandler.uploadLocalData()) {
                        displayExceptionMessage("No Files To Be Uploaded ");
                    }
                    updateFileNumber();
                } else {
                    displayExceptionMessage("Check Internet Connection");
                }


            }
        });
        sensitivityThreshold.setProgress(100);
        sensitivityThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mySensor.threshold = (100.0 - (double) i) / 50.0 + mySensor.INITIAL_THRESHOLD;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SimpleActivity.this, "Sensitivity:" + sensitivityThreshold.getProgress(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        updateFileNumber();
    }

    protected void onResume() {
        mySensor.startListening();
        updateFileNumber();
        mySensor.isActivityAwake=true;
        super.onResume();
    }

    protected void onPause() {
        super.onPause();

    }

    protected void onStop() {
        Log.e("On Stop", "Simple Activity Stopped");
        mySensor.stopListening();
        mySensor.isActivityAwake=false;
        mySensor.lastAnamoly=null;
        saveDefectsValues();
        super.onStop();
    }

    protected void onDestroy() {
        Log.e("On Destroy ", "Simple Activity Destory");
        saveDefectsValues();
        super.onDestroy();


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void displayExceptionMessage(String msg) {
        final String text=msg;
        final AppCompatActivity me=this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(me, text, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                fileHandler = data.getExtras().getParcelable("fileHandler");
            }
        } else {
            currentSessionAnamolyType = UNKNOWN;
            if (resultCode == RESULT_OK && requestCode == 100 && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                userComment = result.get(0);
                for (String s : result) //search for keywords
                {
                    if (s.contains("مطب")) {
                        currentSessionAnamolyType = MATAB;
                        break;
                    } else if (s.contains("حفره") || s.contains("حفرة")) {
                        currentSessionAnamolyType = HOFRA;
                        break;
                    } else if (s.contains("تكسير")) {
                        currentSessionAnamolyType = TAKSER;
                        break;
                    } else if (s.contains("غلط")) {
                        currentSessionAnamolyType = GHLAT;
                        break;
                    } else if (s.contains("حركة") || s.contains("حركه")) {
                        currentSessionAnamolyType = HARAKA;
                        break;
                    }
                }
                commentTextBox.setText(userComment);
                if (mySensor.lastAnamolyLoc!=null) {
                    longitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLongitude()));
                    latitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLatitude()));
                }
                else
                {
                    displayExceptionMessage("Location not available! file not saved");
                    return;
                }

                String s = "";
                switch (currentSessionAnamolyType) {
                    case UNKNOWN:
                        s = "UNKNOWN";
                        break;
                    case MATAB:
                        s = "MATAB";
                        break;
                    case HOFRA:
                        s = "HOFRA";
                        break;
                    case TAKSER:
                        s = "TAKSER";
                        break;
                    case GHLAT:
                        s = "GHLAT";
                        break;
                    case HARAKA:
                        s = "HARAKA";
                        break;
                }
                typeTextBox.setText(s);

                Thread t = new Thread() {
                    public void run() {
                        while (mySensor.isStillProcessing.get()) ;
                        try {
                            mySensor.lastAnamoly.comment = userComment;
                            mySensor.lastAnamoly.type = currentSessionAnamolyType;
                            fileHandler.saveData(mySensor.lastAnamoly);
                            saveDefectsValues();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    drawGraphData();
                                }
                            });
                            
                        } catch (Exception e) {
                            displayExceptionMessage(e.getMessage());
                        }
                    }
                };
                t.start();


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
                displayExceptionMessage("View Files Counts = " + fileHandler.NumberOfDefects);
                Intent myIntent = new Intent(getApplicationContext(), viewFiles.class);
                myIntent.putExtra("myFile", fileHandler);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(myIntent, 2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * uses linear interpolation and extrapolation to sample accelerometer readings for a given session length.
     *
     * @param readings array representing the  timeline in nanoseconds of accelerometer readings
     * @param time     required session length in seconds
     * @return array of sampled readings
     */
    double[] getSampledReadings(ArrayList<Reading> readings, int time) {
        double xFirst, yFirst, xSecond, ySecond, xInter, yInter;
        double Ts = 1.0 / (double) SAMPLINGRATE * Math.pow(10, 9); //in nanoseconds
        int sampledReadingsCount = time * SAMPLINGRATE + 1;
        double[] sampledReadings = new double[sampledReadingsCount];
        if (readings.size() == 0) {
            return new double[10];
        }
        sampledReadings[0] = readings.get(0).value; //reading at t=0
        int i = 1, j = 1;
        double currentTime = Ts + readings.get(0).time;
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

    public void saveDefectsValues() {
        try {
            FileOutputStream fileOutputStream = openFileOutput("Defects.txt", Context.MODE_PRIVATE);
            if (fileHandler.getNumberOfDefects() != 0) {
                boolean[] temp = fileHandler.getAvailableFiles();
                for (int i = 0; i < 100; i++) {
                    if (temp[i] == true) {
                        fileOutputStream.write(String.valueOf(i).getBytes());
                        fileOutputStream.write(String.valueOf("\n").getBytes());
                    }
                }
            }
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("Saving Objects", e.toString());
        }
    }

    public void drawGraphData() {

        graphZValues.clear();
        double relativeTime = 10;
        int counter = -1;
        for (Reading reading : mySensor.lastAnamoly.readings) {
            counter++;
            if (counter % 2 == 1)
                continue;
            relativeTime = (reading.time - mySensor.lastAnamoly.readings[0].time) / Math.pow(10, 9);
            graphZValues.add(new DataPoint(relativeTime, reading.value));

        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(graphZValues.toArray(new DataPoint[0]));
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX((int) relativeTime);
        graph.removeAllSeries();
        graph.addSeries(series);
        updateFileNumber();
    }

    public void statusGPSCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, Enable it ")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    void updateFileNumber() {
        fileNumbersText.setText(String.valueOf(fileHandler.getNumberOfDefects()));
    }

}



