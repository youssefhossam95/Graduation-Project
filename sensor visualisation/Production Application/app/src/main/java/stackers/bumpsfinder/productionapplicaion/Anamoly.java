package stackers.bumpsfinder.productionapplicaion;

import android.app.Activity;
import android.app.Application;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Anamoly extends Activity {

    public ArrayList<LatLng> bumpsLocationData;
    ApplicationContextHolder appContext;
    FileHandler myFileHandler;
    Anamoly(){
    myFileHandler = FileHandler.getFileHandler();

    }
    public void getBumpsData(final ArrayList<LatLng> currentPath,final GoogleMap mMap) {
        new Thread() {
            public void run() {
                runOnUiThread(new Thread(new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    String newURL="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/getLocation";
                                    httpBackgroundConnection myConnection=new httpBackgroundConnection(appContext.getContext(),newURL,"GET");
                                    String longLatData=myConnection.execute("nothing").get(5, TimeUnit.SECONDS);
                                    JSONObject responseJson = new JSONObject(longLatData);
                                    JSONArray rowsData=responseJson.getJSONArray("rows");
                                    bumpsLocationData= new ArrayList<LatLng>();
                                    String[] currentLocation;
                                    for(int i=0;i<rowsData.length();i++)
                                    {
                                        JSONObject currentRow=rowsData.getJSONObject(i);
                                        currentLocation=currentRow.getString("value").split("fused")[1].split("acc")[0].split(",");
                                        LatLng currentLatLng;
                                        if(currentLocation[0].contains("*")==false)
                                        {
                                            currentLatLng=new LatLng(Double.parseDouble(currentLocation[0]),Double.parseDouble(currentLocation[1]));
                                            bumpsLocationData.add(currentLatLng);
                                        }
                                    }

                                    Log.e("Bumps Number","= "+String.valueOf(bumpsLocationData.size()));
                                    clearRedundantData();
                                    drawAnamoliesOnMap(currentPath,mMap);

                                   /* for(int i=0;i<bumpsLocationData.size();i++)
                                    {
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(bumpsLocationData.get(i));
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                        mMap.addMarker(markerOptions);
                                    }*/
                                }
                                catch (Exception e)
                                {
                                    Log.e("Exception in http",e.toString());
                                }
                            }
                        }
                        )
                );
            }
        }.start();

    }
    private int clearRedundantData()
    {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        Location currentLocation=new Location("currentLocation");
        Location newLocation=new Location("newLocation");
        double distance=-1;
        for(int i=0;i<bumpsLocationData.size()-1;i++)
        {
        currentLocation.setLatitude(bumpsLocationData.get(i).latitude);
        currentLocation.setLongitude(bumpsLocationData.get(i).longitude);
        for(int j=1;j<bumpsLocationData.size();j++)
        {
            newLocation.setLatitude(bumpsLocationData.get(j).latitude);
            newLocation.setLongitude(bumpsLocationData.get(j).longitude);
            distance=currentLocation.distanceTo(newLocation);
            if(distance<=5)
            {
                bumpsLocationData.remove(j);
            }
        }

        }
       return bumpsLocationData.size();
    }
    public void drawAnamoliesOnMap(final ArrayList<LatLng> currentPath,final GoogleMap mMap)
    {
        new Thread() {
            public void run() {
                runOnUiThread(new Thread(new Runnable()
                        {
                            public void run()
                            {
                                if(android.os.Debug.isDebuggerConnected())
                                    android.os.Debug.waitForDebugger();
                                ArrayList<LatLng>newBumps=new ArrayList<LatLng>();
                                Location currentLocation=new Location("currentLocation");
                                Location pathLocation=new Location("pathLocation");
                                double distance=-1;
                                try
                                {
                                for(int i=0;i<bumpsLocationData.size();i++) {
                                    currentLocation.setLatitude(bumpsLocationData.get(i).latitude);
                                    currentLocation.setLongitude(bumpsLocationData.get(i).longitude);
                                    for(int j=0;j<currentPath.size();j++)
                                    {
                                        pathLocation.setLatitude(currentPath.get(j).latitude);
                                        pathLocation.setLongitude(currentPath.get(j).longitude);
                                        distance=currentLocation.distanceTo(pathLocation);
                                        if(distance<=8)
                                        {
                                            newBumps.add(bumpsLocationData.get(i));
                                        }
                                    }
                                }
                                    Log.e("new Bumps Number","= "+String.valueOf(newBumps.size()));
                                    for(int i=0;i<newBumps.size();i++)
                                    {
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(newBumps.get(i));
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                        mMap.addMarker(markerOptions);
                                    }

                                }
                                catch (Exception e)
                                {

                                }
                            }
                        }
                        )
                );
            }
        }.start();

    }



}
