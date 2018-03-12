package com.example.youssefhossam.graphsvisualisationapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class helpJoe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_joe);
    }

    public void nextButtonPressed(View v)
    {
        Intent myIntent = new Intent(getApplicationContext(), help.class);
        startActivity(myIntent);
    }
}
