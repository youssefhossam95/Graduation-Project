package com.stackers.BumpsFinder.TrainingApplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import at.markushi.ui.CircleButton;

public class aboutUs extends AppCompatActivity {
    CircleButton ahmedButton;
    CircleButton youssefButton;
    CircleButton waleedButton;
    CircleButton samaButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ahmedButton=(CircleButton)findViewById(R.id.ahmedAdelFb);
        ahmedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.facebook.com/AhmedAdelZakaria95");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        youssefButton=(CircleButton)findViewById(R.id.youssefHossamFb);
        youssefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.facebook.com/youssef.hossam.5");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        samaButton=(CircleButton)findViewById(R.id.samaIhabFB);
        samaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.facebook.com/sama.ihab.5");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        waleedButton=(CircleButton)findViewById(R.id.waleedHazemFB);
        waleedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.facebook.com/profile.php?id=647373663&ref=br_rs"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
}
