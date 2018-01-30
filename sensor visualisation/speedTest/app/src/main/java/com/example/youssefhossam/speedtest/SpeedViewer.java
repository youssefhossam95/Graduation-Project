package com.example.youssefhossam.speedtest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

/**
 * Created by Youssef Hossam on 17/01/2018.
 */

public class SpeedViewer extends Thread {

    TextView output;
    LocationManager locMan;
    Location loc;
    String locProv;
    Context activity;
    SpeedViewer(Context activity,TextView output, LocationManager locMan)
    {
        this.output=output;
        this.locMan=locMan;
        locProv= LocationManager.GPS_PROVIDER;
        this.activity=activity;
    }

    public void run()
    {
        while(!currentThread().isInterrupted())
        {
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                loc=locMan.getLastKnownLocation(locProv);
            try {
                if (loc != null)
                    output.setText(String.valueOf(loc.getSpeed()));
                else
                    output.setText("GPS OFF");
            }
            catch (Exception e)
            {
                int x;
            }
        }
    }


}
