package stackers.bumpsfinder.productionapplicaion;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationService {

    public Location currentLocation = null;
    private static final String TAG = "LocationService";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static LocationService mLocationServiceHandler;
    private LocationRequest mLocationRequest;
    private  LocationCallback mLocationCallback;
    private LocationService(){
        //Location Request And Call Back
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;

                }
            };
        };
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ApplicationContextHolder.getContext());
        try
        {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
        }
        catch (SecurityException e)
        {
            Log.e(TAG,"Security Exception in Location Service : "+e.toString());
        }

    }
    public static LocationService getLocationServiceInstance(){
        if (mLocationServiceHandler == null)
        {
            mLocationServiceHandler = new LocationService();
        }
        return mLocationServiceHandler;
    }
    public  Location getDeviceLocation()
    {
        return currentLocation;
    }
    public boolean isLocationAvailable(){ return (currentLocation == null)?false:true; }
    public Task<Location> waitForLocationToInit(){
        Task location = null;
        try
        {
            location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful() && task.getResult()!=null){
                        Log.d(TAG,"onComplete : Found Location");
                        currentLocation = (Location)task.getResult();
                    }
                    else
                    {
                        Log.d(TAG,"onComplete : current Location is null");
                    }
                }
            });

    }
        catch (SecurityException e)
    {

    }
       return  location;
    }
}
