package com.example.youssefhossam.speedtest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Location loc;
    LocationManager locationManager;
    TextView output;
    SpeedViewer sv;
    String locProv;
    Context activity=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        output=(TextView)findViewById(R.id.text);
        sv=new SpeedViewer(this,output,locationManager);
        locProv= LocationManager.GPS_PROVIDER;

        output.post(new Runnable() {
            @Override
            public void run() {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    loc = locationManager.getLastKnownLocation(locProv);
                try {
                    if (loc != null)
                        output.setText(String.valueOf(loc.getSpeed()*3.6));
                    else
                        output.setText("GPS OFF");
                } catch (Exception e) {
                }
                output.postDelayed(this, 50);

            }
        });
    }
    protected void onDestroy()
    {
        super.onDestroy();
        sv.interrupt();
    }


}
