package com.stackers.BumpsFinder.TrainingApplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
    public final static int MATAB = 0, HOFRA = 1, GHLAT = 2, TAKSER = 3, UNKNOWN = 4;
    int currentSessionAnamolyType = UNKNOWN;
    ArrayList<Reading> currentSessionAccelReading;
    String userComment;
    GraphView graph;
    ArrayList<DataPoint> graphZValues;
    TextView typeTextBox;
    TextView speedTextBox;
    TextView fileNumbersText;
    TextView applicationModeText;
    private Context context;
    int type = 0;
    String comment = "";
    //NEW
    CircleMenu circleMenu;
    public final static int SAMPLINGRATE = 120; // number of samples per second (Fs)
    private SeekBar sensitivityThreshold;
    private SensorHandler mySensor;
    String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        statusGPSCheck();
        ContextHolder contextHolder = new ContextHolder();
        contextHolder.setContext(getApplicationContext());
        circleMenu = (CircleMenu) findViewById(R.id.circle_menu);
        applicationModeText=(TextView)findViewById(R.id.applicationModeText);
        circleMenu.setMainMenu(Color.parseColor("#53aaa8"), R.mipmap.icon_menu, R.mipmap.icon_cancel)
                .addSubMenu(Color.parseColor("#fddd00"), R.mipmap.icon_bump)
                .addSubMenu(Color.parseColor("#FFFFFF"), R.mipmap.icon_pothole)
                .addSubMenu(Color.parseColor("#d00e0e"), R.mipmap.icon_wrong)
                .addSubMenu(Color.parseColor("#FFFFFF"),R.mipmap.icon_cracks)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {
                    @Override
                    public void onMenuSelected(int index) {
                        if (mySensor.isVoiceMode)
                            return;
                        if (mySensor.lastAnamoly == null && !mySensor.isVoiceMode && !mySensor.isStillProcessing.get()) {
                            displayExceptionMessage("Sorry No Data To Classify You Missed It");
                            return;
                        }
                        if(fileHandler.getNumberOfDefects()==100) {
                            displayExceptionMessage("Anamoly Ignored as maximum number of files reached! Please upload Data to record new anamolies");
                            return;
                        }
                        type = index;
                        switch (index) {
                            case MATAB: {
                                displayExceptionMessage("You have chosen مطب");
                                comment = "مطب";
                                break;
                            }
                            case HOFRA: {
                                displayExceptionMessage("You have chosen حفرة");
                                comment = "حفرة";
                                break;
                            }
                            case GHLAT: {
                                displayExceptionMessage("You have chosen غلط ");
                                comment = "غلط";
                                break;
                            }
                            case TAKSER: {
                                displayExceptionMessage("You have chosen تكسير ");
                                comment = "تكسير";
                                break;
                            }
                        }

                        typeTextBox.setText(comment);
                        if (mySensor.lastAnamolyLoc != null) {
                            longitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLongitude()));
                            latitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLatitude()));
                        } else {
                            displayExceptionMessage("Location not available! file not saved");
                            return;
                        }

                        Thread t = new Thread() {
                            public void run() {
                                while (mySensor.isStillProcessing.get()) ;
                                try {
                                    mySensor.lastAnamoly.type = type;
                                    mySensor.lastAnamoly.comment = comment;
                                    fileHandler.saveData(mySensor.lastAnamoly,userName);
                                    saveDefectsValues();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String speedValueString =getSpeedAverage();
                                            speedTextBox.setText(String.valueOf(speedValueString));
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
            applicationModeText.setText("Buttons Mode");
            mySensor.isVoiceMode = false;

        } else {
            applicationModeText.setText("Voice Mode");
            toggleButton.setChecked(true);
            mySensor.isVoiceMode = true;
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isNetworkAvailable()) {
                        applicationModeText.setText("Voice Mode");
                        mySensor.isVoiceMode = true;
                    } else {
                        displayExceptionMessage("To Enable Voice Mode Please Check Your Internet Connection");
                        toggleButton.setChecked(false);
                        applicationModeText.setText("Buttons Mode");
                        mySensor.isVoiceMode = false;
                    }

                } else {
                    applicationModeText.setText("Buttons Mode");
                    mySensor.isVoiceMode = false;
                }
            }
        });
        sessionStartTime = 0;
        fileNumbersText = (TextView) findViewById(R.id.fileNumbersText);
        fileHandler = FileHandler.getFileHandlerObject();
        uploadButton = (CircleButton) findViewById(R.id.uploadButton);
        currentSessionAccelReading = new ArrayList<Reading>();
        typeTextBox = (TextView) findViewById(R.id.typeTextBox);
        longitudeText = (TextView) findViewById(R.id.longitudeText);
        latitudeText = (TextView) findViewById(R.id.latitudeText);
        speedTextBox = (TextView) findViewById(R.id.averageSpeedTextBox);
        graphZValues = new ArrayList<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        this.context = getApplicationContext();

        sensitivityThreshold = findViewById(R.id.sensitivityThreshold);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable()) {
                    displayExceptionMessage("Uploading Data");
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
        userName=getIntent().getExtras().getString("username");
        fileHandler.writeToFile("userInfo",userName);
        updateFileNumber();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    @Override
    protected void onStart() {
        initializeLocationListener();
        if(fileHandler.NumberOfDefects!=0)
        {
            displayExceptionMessage("Reminder : Please don't forget to upload the files!");
        }
        super.onStart();
    }
    protected void onResume() {
        mySensor.startListening();
        updateFileNumber();
        mySensor.isActivityAwake = true;
        super.onResume();
    }
    protected void onPause() {
        super.onPause();
        mySensor.isActivityAwake = false;

    }
    protected void onStop() {
        Log.e("On Stop", "Simple Activity Stopped");
        mySensor.lastAnamoly = null;
        mySensor.locationManager.removeUpdates(mySensor.locationListener);
        saveDefectsValues();
        mySensor.stopListening();
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
        final String text = msg;
        final AppCompatActivity me = this;
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
        if(requestCode==3)
        {
            if(resultCode==RESULT_OK)
            {
                userName=data.getExtras().getString("userName");
                fileHandler.writeToFile("userInfo",userName);
            }
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                fileHandler = data.getExtras().getParcelable("fileHandler");
            }
        }
        else {
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
                    }
                }

                if(fileHandler.getNumberOfDefects()==100) {
                    displayExceptionMessage("Anamoly Ignored as maximum number of files reached! Please upload data to record new anamolies");
                    return;
                }

                if (mySensor.lastAnamolyLoc != null) {
                    longitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLongitude()));
                    latitudeText.setText(String.valueOf(mySensor.lastAnamolyLoc.getLatitude()));
                } else {
                    displayExceptionMessage("Location not available! file not saved");
                    return;
                }
                String s = "";
                switch (currentSessionAnamolyType) {
                    case UNKNOWN:
                        s = "UNKNOWN";
                        break;
                    case MATAB:
                        s = "مطب";
                        break;
                    case HOFRA:
                        s = "حفرة";
                        break;
                    case TAKSER:
                        s = "تكسير";
                        break;
                    case GHLAT:
                        s = "غلط";
                        break;
                }
                typeTextBox.setText(s);
                Thread t = new Thread() {
                    public void run() {
                        while (mySensor.isStillProcessing.get()) ;
                        try {
                            mySensor.lastAnamoly.comment = userComment;
                            mySensor.lastAnamoly.type = currentSessionAnamolyType;
                            fileHandler.saveData(mySensor.lastAnamoly,userName);
                            saveDefectsValues();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String speedValueString =getSpeedAverage();
                                    speedTextBox.setText(String.valueOf(speedValueString));
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
        Intent myIntent;
        switch (item.getItemId()) {
            case R.id.viewFileButton:
                displayExceptionMessage("View Files Counts = " + fileHandler.NumberOfDefects);
                 myIntent = new Intent(getApplicationContext(), viewFiles.class);
                myIntent.putExtra("myFile", fileHandler);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(myIntent, 2);
                return true;
            case R.id.editName:
                 myIntent = new Intent(getApplicationContext(), editUserName.class);
                 myIntent.putExtra("userName",userName);
                 startActivityForResult(myIntent, 3);
                return true;
            case R.id.aboutUs:
                myIntent = new Intent(getApplicationContext(), aboutUs.class);
                startActivity(myIntent);
                return true;
            case R.id.Help:
                myIntent = new Intent(getApplicationContext(), helpJoe.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    String getSpeedAverage()
    {
        Reading[] speedValues = mySensor.lastAnamoly.speeds;
        float Summation = 0;

        if(speedValues.length==0)
        {
            return "0.0";
        }


        for (int i = 0; i < speedValues.length; i++) {
            Summation += speedValues[i].value;
        }
        float speedAverage = (Summation / speedValues.length) * 3.6f;
        if (Float.isNaN(speedAverage)) {
            speedAverage = 0;
        }
        String speedValueString = String.format("%.02f", speedAverage);
        Log.e("Summation = ", String.valueOf(speedValueString));
        Log.e("speed Average = ", String.valueOf(speedValueString));
        return speedValueString;
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //used in the first run for the program

        if(requestCode!=SensorHandler.REQUEST_LOCATION)
            return;

        int permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            mySensor.locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
            mySensor.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mySensor.locationListener);

        }
        else //permission refused -> request again
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, SensorHandler.REQUEST_LOCATION);
        }
    }
    void initializeLocationListener()
    {
        int permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            mySensor.locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
            mySensor.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mySensor.locationListener);

        }
        else //first time to run the program -> ask for permission
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, SensorHandler.REQUEST_LOCATION);
        }
    }
//    @Override
//    public void onBackPressed() {
//        displayExceptionMessage("You are already logged in ");
//        moveTaskToBack(false);
//    }
}



