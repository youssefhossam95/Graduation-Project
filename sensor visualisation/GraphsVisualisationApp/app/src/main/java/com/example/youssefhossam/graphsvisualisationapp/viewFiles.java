package com.example.youssefhossam.graphsvisualisationapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class viewFiles extends AppCompatActivity {
String [] bumpsFiles;
    viewFiles(String [] files)
    {
        bumpsFiles=files;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);
    }
}
