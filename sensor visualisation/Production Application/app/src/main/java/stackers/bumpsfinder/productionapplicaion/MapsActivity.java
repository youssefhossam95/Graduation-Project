package stackers.bumpsfinder.productionapplicaion;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;


import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //Maps And Location Variables
    private GoogleMap mMap;
    private Boolean mLocationPermssionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));
    private static final float DEFAULT_ZOOM = 15f;

    //Widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGPS;
    ArrayList<LatLng> listPoints;
    MarkerOptions markerOptions;
    Anamoly anaomly;
    FileHandler myFileHandler;
    ApplicationContextHolder appContext;

    // This part of variables are for permissions and check Google maps availablity on the device
    private static final String TAG = "MapsActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int LOCATION_REQUEST = 500;
    HashMap<String,String> myAppPermissions = new HashMap<String,String>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSearchText = (AutoCompleteTextView)findViewById(R.id.inputSearch);
        mGPS = (ImageView)findViewById(R.id.ic_gps);
        init();
        isServicesOK();
        getLocationPermission();
        initMap();

        listPoints = new ArrayList<>();
        myFileHandler = FileHandler.getFileHandler();
        anaomly=new Anamoly();
        appContext.setContext(getApplicationContext());
    }

    private void init(){
        Log.d(TAG,"init : initializing App Data");
        // Google Place API adapter init
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, (GoogleApiClient.OnConnectionFailedListener) this)
                .build();
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        //Initialinzing Permissions Map
        myAppPermissions.put("fineLocation",Manifest.permission.ACCESS_FINE_LOCATION);
        myAppPermissions.put("coarseLocation",Manifest.permission.ACCESS_COARSE_LOCATION);

        //Search Text Field
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                //Executing method of searching
                    Thread mThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            geolocate();
                        }
                    });
                    mThread.start();
                    return true;
                }
                return  false;
            }
        });
        mGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

    }
    private void initMap() {
        Log.d(TAG,"initMap : Initilaizing Map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    public boolean isServicesOK() {
        Log.d(TAG,"isServicesOk : Checking google service version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);
        if(available == ConnectionResult.SUCCESS)
        { // Everything is fine and user now can make maps request
            Log.d(TAG,"isServicesOK : Google Play services is ok ");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            // An error occured and we can resolve it
            Log.d(TAG,"isServicesOk : Error occuered but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            Toast.makeText(this,"You can't make Maps Request due to incompatible  device .. Sorry",Toast.LENGTH_SHORT).show();

        }
        return false;
    }
    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission : Getting location permissions");
        if(ContextCompat.checkSelfPermission(this,myAppPermissions.get("fineLocation")) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this,myAppPermissions.get("coarseLocation")) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermssionsGranted = true;
                Log.d(TAG,"getLocationPermission : All Permissions are granted (Y)");
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{myAppPermissions.get("fineLocation"), myAppPermissions.get("coarseLocaion")},LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{myAppPermissions.get("fineLocation"), myAppPermissions.get("coarseLocaion")},LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation : getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try
        {
            if(mLocationPermssionsGranted)
            {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                             Log.d(TAG,"onComplete : Found Location");
                             Location currentLocation = (Location)task.getResult();
                             listPoints.add(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                             moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
                        }
                        else
                        {
                            Log.d(TAG,"onComplete : current Location is null");
                            Toast.makeText(getApplicationContext(),"Unable to get current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e)
        {

        }
    }
    private void geolocate(){

            new Thread(){
                public void run(){
                    try{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listPoints.size() == 2) {
                                listPoints.remove(1);
                                mMap.clear();
                            }
                            Log.d(TAG,"gelocate : gelocating");
                            String searchString = mSearchText.getText().toString();
                            Geocoder geocoder = new Geocoder(MapsActivity.this);
                            List<Address> list = new ArrayList<Address>();
                            try{
                                list = geocoder.getFromLocationName(searchString,1);
                            }
                            catch (IOException e)
                            {
                                Log.e(TAG,"gelocate : IOException : "+e.getMessage());
                            }
                            if (list.size() > 0) {
/*                                Address address = list.get(0);
                                Log.d(TAG,"gelocate : Address is located At "+address.toString());
                                try {
                                    list = geocoder.getFromLocationName(searchString, 1);
                                } catch (IOException e) {
                                    Log.e(TAG, "geolocate : IOException:" + e.getMessage());
                                }
                                listPoints.add(new LatLng(address.getLatitude(),address.getLongitude()));
                                String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                                TaskRequestDirections taskRequestDirections = new TaskRequestDirections(mMap,getApplicationContext());
                                taskRequestDirections.execute(url);
                                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));*/
                            }
                            else
                            {
                                Log.d(TAG,"geolocate : Couldn't find given location ");
                            }
                        }
                    });
                    }
                    catch (Exception e){
                    Log.e(TAG,"gelocate : ExceptionMessage: "+e.getMessage());
                    } }}.start();
    }
    private void moveCamera(LatLng latlng,float zoom , String title){
        Log.d(TAG,"moveCamera : Moving the camera to Lat = "+ latlng.latitude +" Long = "+latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,zoom));
        if(!title.equals("My Location"))
        {
            MarkerOptions options = new MarkerOptions().position(latlng).title(title);
            mMap.addMarker(options);
        }

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map is Ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG,"onMapReady : Map is ready here");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if(mLocationPermssionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }



    /*     mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
               if (listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }
                //Save first point select
                listPoints.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                if (listPoints.size() == 2) {
                    //Create the URL to get request from first marker to second marker
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections(mMap,getApplicationContext());
                    taskRequestDirections.execute(url);
                //    anaomly.getBumpsData(listPoints,mMap);

                }
            }
        });*/

    }


    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }



}
