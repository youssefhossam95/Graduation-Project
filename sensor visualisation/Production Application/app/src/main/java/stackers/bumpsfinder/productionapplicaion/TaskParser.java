package stackers.bumpsfinder.productionapplicaion;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Ahmed on 4/28/2018.
 */

public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

    private static final String TAG = "TaskParser";
    private GoogleMap mMap;
    private ArrayList<LatLng> polylineList = null;
    MapsActivity myMainActivity;
    public TaskParser(GoogleMap maps , MapsActivity myMainActiv)
    {
        mMap=maps;
        myMainActivity = myMainActiv;
        polylineList= new ArrayList<>();

    }
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
        Log.d(TAG,"doInBackground : loading Data");
        JSONObject jsonObject = null;
        List<List<HashMap<String, String>>> routes = null;
        try {
            jsonObject = new JSONObject(strings[0]);
            DirectionsParser directionsParser = new DirectionsParser();
            routes = directionsParser.parse(jsonObject);
            if(routes.size()!=0)
            {
                Log.d(TAG,"doInBackground : Routes 0 = "+routes.get(0).toString());
            }
        } catch (JSONException e) {
            Log.e(TAG,"doInBackground JSONException : "+e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
        //Get list route and display it into the map
        Log.d(TAG,"OnPostExecute : Getting polylines ");
        polylineList = null;;

        for (List<HashMap<String, String>> path : lists) {
            polylineList = new ArrayList();
            for (HashMap<String, String> point : path) {
                double lat = Double.parseDouble(point.get("lat"));
                double lon = Double.parseDouble(point.get("lon"));
                polylineList.add(new LatLng(lat,lon));
            }

            final Anamoly  anaomly=Anamoly.getAnamolyHandler();
            try {
              new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            anaomly.loadBumpsData(polylineList,mMap);
                        }
                        catch (Exception e){
                        Log.e(TAG,"onPostExecute : Anamoly Thread Exception "+e.toString());
                        }
                    }
                }).start();
            }
            catch (Exception e)
            {
                Log.e(TAG,"OnPostExecute : Error : "+e.toString());
            }
        }

        if (polylineList !=null) {
            myMainActivity.setPathList(polylineList);
            myMainActivity.drawPathOnMap();

        } else {
            Toast.makeText(ApplicationContextHolder.getContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            myMainActivity.showKeyboard();
        }

    }


}