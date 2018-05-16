package stackers.bumpsfinder.productionapplicaion;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ahmed on 4/28/2018.
 */

public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

    private GoogleMap mMap;


    public TaskParser(GoogleMap maps)
    {
        mMap=maps;
    }
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
        JSONObject jsonObject = null;
        List<List<HashMap<String, String>>> routes = null;
        try {
            jsonObject = new JSONObject(strings[0]);
            DirectionsParser directionsParser = new DirectionsParser();
            routes = directionsParser.parse(jsonObject);
            if(routes.size()!=0)
            {
                Log.e("Routes 0",routes.get(0).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
        //Get list route and display it into the map

        ArrayList points = null;

        PolylineOptions polylineOptions = null;

        for (List<HashMap<String, String>> path : lists) {
            points = new ArrayList();
            polylineOptions = new PolylineOptions();

            for (HashMap<String, String> point : path) {
                double lat = Double.parseDouble(point.get("lat"));
                double lon = Double.parseDouble(point.get("lon"));
                points.add(new LatLng(lat,lon));
            }

            polylineOptions.addAll(points);
            polylineOptions.width(15);
            polylineOptions.color(Color.BLACK);
            polylineOptions.geodesic(true);
            Anamoly anaomly=new Anamoly();
            anaomly.getBumpsData(points,mMap);
        }

        if (polylineOptions!=null) {
            mMap.addPolyline(polylineOptions);
        } else {
            Toast.makeText(ApplicationContextHolder.getContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
        }

    }

}