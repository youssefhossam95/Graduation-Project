package com.example.youssefhossam.graphsvisualisationapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import at.markushi.ui.CircleButton;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

public class MainActivity extends AppCompatActivity {
    CircleButton VoiceModeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VoiceModeButton=(CircleButton)findViewById(R.id.VoiceButton);
        VoiceModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast V=Toast.makeText(MainActivity.this,"Simple Mode Button Clicked",Toast.LENGTH_SHORT);
                V.show();
                Intent myIntent = new Intent(getApplicationContext(), SimpleActivity.class);
                startActivity(myIntent);
            }
        });

    //    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
    //    setSupportActionBar(myToolbar);
    }


    protected void onResume() {
        super.onResume();
        Drawable tempImage = getResources().getDrawable(R.drawable.megaphone);
        VoiceModeButton.setImageDrawable(tempImage);
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }
    protected void onDestroy(){
        super.onDestroy();
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }






}




