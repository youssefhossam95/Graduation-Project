package stackers.bumpsfinder.productionapplicaion;


import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;


import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
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
    private Location currentLocation;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));
    private static final float DEFAULT_ZOOM = 15f;
    private  MediaPlayer mp ;
    //Widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGPS;
    private ImageView mPlaySimulation;
    private ImageView mStopSimulation;
    private static LinearLayout popUpWindow;
    private static TextView bumpsInfoTextView;
    ArrayList<LatLng> currentPathList;
    MarkerOptions markerOptions;
    Anamoly mAnamoly;
    FileHandler myFileHandler;
    ApplicationContextHolder appContext;

    //Simulation Variables
    private Timer timer;
    private int mIterator = 0;
    Marker marker =null;

    //Draw On Map Variables
    private Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition;
    private int index = 0,next;
    private PolylineOptions polylineOptions,blackPolyLineOptions;
    private Polyline blackPolyline,greyPolyLine;
    private ArrayList<LatLng> sourceDestinationLocations; // It's an array size of 2 normally 0 contains source location and 1 contains the destination

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
        // Map Variable To Layout
        mSearchText = (AutoCompleteTextView)findViewById(R.id.inputSearch);
        mGPS = (ImageView)findViewById(R.id.ic_gps);
        mPlaySimulation = (ImageView)findViewById(R.id.ic_startSimulation);
        mStopSimulation = (ImageView)findViewById(R.id.ic_stopSimulate);
        mp = MediaPlayer.create(this, R.raw.matab);
        popUpWindow = (LinearLayout)findViewById(R.id.popUpLayoutWindow);
        bumpsInfoTextView = (TextView)findViewById(R.id.bumpsInfoTextView);
        hidePopUpWindow();
        //Init data and check
        init();
        initData();
        isServicesOK();
        getLocationPermission();
        initMap();
        currentPathList = new ArrayList<>();
        sourceDestinationLocations = new ArrayList<>();
        myFileHandler = FileHandler.getFileHandler();

        mAnamoly=Anamoly.getAnamolyHandler();
        appContext.setContext(getApplicationContext());
    }
    public static void showPopUpWindow(int bumpsNumber){
        String bumpsInfo = "Number of bumps : "+bumpsNumber;
        bumpsInfoTextView.setText(bumpsInfo);
        popUpWindow.setVisibility(View.VISIBLE);
    }
    public static void hidePopUpWindow(){
        popUpWindow.setVisibility(View.GONE);
    }
    private void initData(){

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
                moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
            }
        });
        mPlaySimulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startSimulation();
            }
        });
        mStopSimulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSimulation();
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
    private Task<Location> getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation : getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Task location = null;
        try
        {
            if(mLocationPermssionsGranted)
            {
                location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && task.getResult()!=null){
                             Log.d(TAG,"onComplete : Found Location");
                             currentLocation = (Location)task.getResult();
                             sourceDestinationLocations.add(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())); //current Path list

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
        return location;
    }
    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    public void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }
    private void geolocate(){
        hideKeyboard();
            new Thread(){
                public void run(){
                    try{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(android.os.Debug.isDebuggerConnected())
                                android.os.Debug.waitForDebugger();
/*                            if (sourceDestinationLocations.size() == 2) {
                                sourceDestinationLocations.remove(1);
                                mMap.clear();
                            }*/
                            mMap.clear();
                         Thread T =   new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getDeviceLocation();
                                }
                            });
                         T.start();
                            try {
                                T.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
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
                                Address address = list.get(0);
                                Log.d(TAG,"gelocate : Address is located At "+address.toString());

                                sourceDestinationLocations.add(1,new LatLng(address.getLatitude(),address.getLongitude()));
                                String url = getRequestUrl(sourceDestinationLocations.get(0), sourceDestinationLocations.get(1));
                                TaskRequestDirections taskRequestDirections = new TaskRequestDirections(mMap,getApplicationContext(),MapsActivity.this);
                                taskRequestDirections.execute(url);
                                sourceDestinationLocations.clear();
                                //            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
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
    public void drawPathOnMap(){
        polylineOptions = new PolylineOptions();
        blackPolyLineOptions = new PolylineOptions();
        //Adjusting Bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng latlng:currentPathList){
            builder.include(latlng);
        }
/*        LatLngBounds bounds =builder.build();
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
        mMap.animateCamera(mCameraUpdate);*/
        polylineOptions.color(Color.GRAY);
        polylineOptions.width(5);
        polylineOptions.startCap(new SquareCap());
        polylineOptions.endCap(new SquareCap());
        polylineOptions.jointType(JointType.ROUND);
        polylineOptions.geodesic(true);
        polylineOptions.addAll(currentPathList);
        greyPolyLine = mMap.addPolyline(polylineOptions);

        blackPolyLineOptions.color(Color.BLACK);
        blackPolyLineOptions.width(5);
        blackPolyLineOptions.startCap(new SquareCap());
        blackPolyLineOptions.endCap(new SquareCap());
        blackPolyLineOptions.jointType(JointType.ROUND);
        blackPolyLineOptions.geodesic(true);
        blackPolyLineOptions.addAll(currentPathList);
        blackPolyline = mMap.addPolyline(blackPolyLineOptions);

        mMap.addMarker(new MarkerOptions().position(currentPathList.get(currentPathList.size()-1)));



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
        Log.d(TAG,"onMapReady : Map is ready here");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if(mLocationPermssionsGranted) {
            Task<Location> currentLocationTask = getDeviceLocation();
            currentLocationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            });

        }



    /*     mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
               if (currentPathList.size() == 2) {
                    currentPathList.clear();
                    mMap.clear();
                }
                //Save first point select
                currentPathList.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (currentPathList.size() == 1) {
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                if (currentPathList.size() == 2) {
                    //Create the URL to get request from first marker to second marker
                    String url = getRequestUrl(currentPathList.get(0), currentPathList.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections(mMap,getApplicationContext());
                    taskRequestDirections.execute(url);
                //    mAnamoly.getBumpsData(currentPathList,mMap);

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
    public void setPathList(ArrayList<LatLng> result){
        currentPathList = result;
    }
    private float getBearing(LatLng startPosition, LatLng newPos) //This function adjust the car movement
    {
        double lat = Math.abs(startPosition.latitude - newPos.latitude);
        double lng = Math.abs(startPosition.longitude - newPos.longitude);
        if(startPosition.latitude < newPos.latitude && startPosition.longitude < newPos.longitude) return (float)(Math.toDegrees(Math.atan(lng/lat)));
        else if (startPosition.latitude >= newPos.latitude && startPosition.longitude < newPos.longitude) return (float)((90 - Math.toDegrees(Math.atan(lng/lat))+90));
        else if (startPosition.latitude >= newPos.latitude && startPosition.longitude >= newPos.longitude) return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        else if(startPosition.latitude < newPos.latitude && startPosition.longitude >= newPos.longitude) return (float)((90 - Math.toDegrees(Math.atan(lng/lat))+270));
        return -1;
    }
    private boolean simulationBoolean = false;
    public void startSimulation() {
       // startActivity(new Intent(MapsActivity.this,popUpData.class));
        Log.d(TAG,"startSimulation : Started Simulating");
        final ArrayList<LatLng> tempbumpsLocationData = new ArrayList<LatLng>();
        for(int i=0;i<mAnamoly.bumpsLocationData.size();i++){
            tempbumpsLocationData.add(mAnamoly.bumpsLocationData.get(i));
        }
        simulationBoolean = true;
        //Animator
        final ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
        polyLineAnimator.setDuration(2000); // 2 Seconds
        polyLineAnimator.setInterpolator(new LinearInterpolator());
        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                List<LatLng> points = greyPolyLine.getPoints();
                int precentValue = (int)valueAnimator.getAnimatedValue();
                int size = points.size();
                int newPoints = (int)(size*(precentValue/100.0f));
                List <LatLng> p = points.subList(0,newPoints);
                blackPolyline.setPoints(p);
            }
        });
        polyLineAnimator.start();
        // Add Car Marker
        marker = mMap.addMarker(new MarkerOptions().position(currentPathList.get(index))
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        //Car moving
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (simulationBoolean) {
                    if (index < currentPathList.size() - 1) {
                        index++;
                        next = index + 1;
                    }
                    if (index < currentPathList.size() - 1) {
                        startPosition = currentPathList.get(index);
                        endPosition = currentPathList.get(next);
                    }

                    final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.setDuration(3000); // 3 Seconds
                    valueAnimator.setInterpolator(new LinearInterpolator());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            v = valueAnimator.getAnimatedFraction();
                            lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                            lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                            LatLng newPos = new LatLng(lat, lng);
                            marker.setPosition(newPos);
                            marker.setAnchor(0.5f, 0.5f);
                            marker.setRotation(getBearing(startPosition, newPos));
                            //  mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(newPos).zoom(15.5f).build()));
                        }
                    });
                    valueAnimator.start();
                    if(index <currentPathList.size()){
                        LatLng currentLatLng = new LatLng(currentPathList.get(index).latitude,currentPathList.get(index).longitude);
                        for(int i=0;i<tempbumpsLocationData.size();i++){

                            double dist =  mAnamoly.calculateLatLongDistance(new LatLng(currentLatLng.latitude,currentLatLng.longitude),new LatLng(tempbumpsLocationData.get(i).latitude,tempbumpsLocationData.get(i).longitude));
                            if (dist <= 30){
                                if(!mp.isPlaying())
                                {
                                    mp.start();
                                }
                                tempbumpsLocationData.remove(i);
                                break;
                            }
                        }
                        handler.postDelayed(this, 3000);
                    }


                }
            }
        },300);


        /*
        Log.d(TAG,"startSimulation : Started Simulating");
        final ArrayList<LatLng> tempbumpsLocationData = mAnamoly.bumpsLocationData;
        Toast.makeText(getApplicationContext(),"Started Simulation",Toast.LENGTH_SHORT).show();
        if(timer != null) {
            return;
        }
        else
        {
                timer = new Timer();
                 TimerTask simulationTask = new TimerTask() {
                @Override
                public void run() {
                    new Thread(){
                        public void run(){
                            try{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if(mIterator <= currentPathList.size()-1)
                                        {
                                            if(marker!=null)
                                            {
                                                marker.remove();
                                            }
                                            LatLng currentLatLng = new LatLng(currentPathList.get(mIterator).latitude,currentPathList.get(mIterator).longitude);
                                            MarkerOptions newMarker =new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).position(currentLatLng).title("Moving Car");
                                            for(int i=0;i<mAnamoly.bumpsLocationData.size();i++){
                                                double dist =  mAnamoly.calculateLatLongDistance(new LatLng(currentLatLng.latitude,currentLatLng.longitude),new LatLng(tempbumpsLocationData.get(i).latitude,tempbumpsLocationData.get(i).longitude));
                                              if (dist <= 30){
                                                  if(!mp.isPlaying())
                                                  {
                                                      mp.start();
                                                  }
                                                  tempbumpsLocationData.remove(i);
                                                  break;
                                              }
                                            }
                                            marker= mMap.addMarker(newMarker);
                                            mIterator++;
                                            //moveCamera(currentLatLng,25f,"My Location");
                                        }
                                    }
                                });
                            }
                            catch (Exception e){}

                        }
                    }.start();
                }
            };
                timer.scheduleAtFixedRate(simulationTask, 0, 1000);

        }
*/

    }
    public void stopSimulation() {
        Log.d(TAG,"stopSimulation : Stopping Simulating");
        simulationBoolean = false;
        index = 0;
/*        if(timer !=null)
        {
            timer.cancel();
            timer.purge();
            timer = null;
            mIterator=0;
        }*/
        if(marker!=null)
        {
            marker.remove();
        }
        Toast.makeText(getApplicationContext(),"Stopped Simulation",Toast.LENGTH_SHORT).show();
    }
    public void startTrip(View V){
        hidePopUpWindow();
        startSimulation();
    }
    public void cancelTrip(View V){
        hidePopUpWindow();
        mMap.clear();
        if(currentLocation!=null)
        moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
    }
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private void startLocationUpdate() {
        try {
            mLocationRequest = new LocationRequest();
            mLocationCallback = new LocationCallback();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(5);
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        catch (SecurityException e){
            Log.e(TAG,"startLocation Update : Error : "+e.toString());
        }
    }
}
