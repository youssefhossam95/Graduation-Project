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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BumpAnamoly extends Activity {



    //Var Declarations
    public ArrayList<LatLng> bumpsLocationData;  // This array contains all the bumps for the current path
    private ArrayList<LatLng>  bumpsData; // This array contains all offline bumps
    ApplicationContextHolder appContext;
    private FileHandler myFileHandler;
    private static final String TAG = "Anamoly";
    private int onlineBumpsNumber = 0;
    private int offlineBumpsNumber = 0;
    private static BumpAnamoly myAnamoly = null;
    private CountDownLatch latch = new CountDownLatch(1);
    private BumpAnamoly(){
    myFileHandler = FileHandler.getFileHandler();
    bumpsLocationData = new ArrayList<LatLng>();
    bumpsData = new ArrayList<LatLng>();
    }
    public static BumpAnamoly getAnamolyHandler(){
        if(myAnamoly==null){
            myAnamoly = new BumpAnamoly();
        }
        return myAnamoly;
    }
    public Thread getOnlineBumpsData() {
      return  new Thread() {
            public void run() {
                runOnUiThread(new Thread(new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    if(android.os.Debug.isDebuggerConnected())
                                        android.os.Debug.waitForDebugger();
                                    Log.d(TAG,"getBumpsData : getting Bumps Data");
                                    String newURL="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/_design/GetAllJsons/_view/getLocation";
                                    httpBackgroundConnection myConnection=new httpBackgroundConnection(appContext.getContext(),newURL,"GET");
                                    String longLatData=myConnection.execute("nothing").get(5, TimeUnit.SECONDS);
                                    JSONObject responseJson = new JSONObject(longLatData);
                                    JSONArray rowsData=responseJson.getJSONArray("rows");
                                    bumpsData= new ArrayList<LatLng>();
                                    String[] currentLocation;
                                    for(int i=0;i<rowsData.length();i++)
                                    {
                                        JSONObject currentRow=rowsData.getJSONObject(i);
                                        currentLocation=currentRow.getString("value").split("fused")[1].split("acc")[0].split(",");
                                        LatLng currentLatLng;
                                        if(currentLocation[0].contains("*")==false)
                                        {
                                            currentLatLng=new LatLng(Double.parseDouble(currentLocation[0]),Double.parseDouble(currentLocation[1]));
                                            bumpsData.add(currentLatLng);
                                        }
                                    }
                                    Log.d(TAG," getBumpsData : Bumps Number = "+String.valueOf(bumpsData.size()));
                                    latch.countDown();
                                }
                                catch (Exception e)
                                {
                                    Log.e(TAG,"getBumpsData : Exception : "+e.toString());
                                }
                            }
                        }
                        )
                );
            }
        };

    }
    private int clearRedundantData()
    {

        double distance=-1;
        ArrayList<LatLng> tempBumpsData = bumpsData;
        for(int i=0;i<tempBumpsData.size()-1;i++)
        {
        for(int j=1;j<tempBumpsData.size()-1;j++)
        {
            distance=calculateLatLongDistance(new LatLng(tempBumpsData.get(i).latitude,tempBumpsData.get(i).longitude),new LatLng(tempBumpsData.get(j).latitude,tempBumpsData.get(j).longitude));
            if(distance<=5)
            {
                tempBumpsData.remove(j);
            }
        }

        }
        bumpsLocationData = tempBumpsData;
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
                                        if(distance <= 8)
                                        {
                                            newBumps.add(bumpsLocationData.get(i));
                                            break;
                                        }
                                    }
                                }
                                    bumpsLocationData=newBumps;
                                    Log.d(TAG,"drawAnamoliesOnMap bumps avilable for road = "+String.valueOf(newBumps.size()));
                                    for(int i=0;i<newBumps.size();i++)
                                    {
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(newBumps.get(i));
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bump));
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
    public int getOnlineBumpsNumber() throws InterruptedException, ExecutionException, TimeoutException {
        Log.d(TAG,"getOnlineBumpsNumber : Loading Bumps Data");

        try{
           httpBackgroundConnection myConnection = new httpBackgroundConnection(ApplicationContextHolder.getContext(),"https://graduationprojectdbmanager.eu-de.mybluemix.net/bumpsNumber","GET");
           String bumpsNumber = myConnection.execute("nothing").get(5, TimeUnit.SECONDS);
           JSONObject responseJson = new JSONObject(bumpsNumber);
           return responseJson.getInt("totalRows");
       }
       catch(Exception e)
       {
           Log.e(TAG,"getOnlineBumpsNumber Exception : "+e.toString());
           return -1;
       }

    }
    public int getOfflineBumpsNumber(){
        ArrayList<String> retrievedData = myFileHandler.readFromFile("offlineBump");
        if(retrievedData == null || retrievedData.size()==0) return -1;
        return Integer.parseInt(retrievedData.get(0));

    }
    private void readBumpsOfflineData() throws InterruptedException {
        ArrayList<String> bumpsStringData = myFileHandler.readFromFile("bumpsData");
        for(int i=0; i < bumpsStringData.size();i++){
            bumpsData.add(convertStringToLatLng(bumpsStringData.get(i)));
        }
    }
    private LatLng convertStringToLatLng(String currentBump){
        String [] latLngString = currentBump.split("\\(");
        String [] temp = latLngString[1].split(",");
        double Lat = Double.parseDouble(temp[0]);
        double Long = Double.parseDouble(temp[1].replace(")",""));
        return new LatLng(Lat,Long);
    }
    public void loadBumpsData(final ArrayList<LatLng> currentPath,final GoogleMap mMap) throws InterruptedException, ExecutionException, TimeoutException {
        Log.d(TAG,"loadBumpsData : Loading Bumps Data");
        onlineBumpsNumber = getOnlineBumpsNumber();
        offlineBumpsNumber = getOfflineBumpsNumber();
        Log.d(TAG,"loadBumps Data : online Bumps Number = "+onlineBumpsNumber);
        Log.d(TAG,"loadBumps Data : offline Bumps Number = "+offlineBumpsNumber);
        if(onlineBumpsNumber != offlineBumpsNumber || (onlineBumpsNumber == -1 && offlineBumpsNumber == -1)){
           myFileHandler.clearFile("offlineBump");
           myFileHandler.writeToFile("offlineBump",String.valueOf(onlineBumpsNumber));
           Thread T = getOnlineBumpsData();
           T.start();
           T.join();
           latch.await();
           // Clear Bumps Data
           myFileHandler.clearFile("bumpsData");
           for(int i = 0 ; i<bumpsData.size();i++)
           {
               myFileHandler.writeToFile("bumpsData",bumpsData.get(i).toString()+"\n");
           }
       }
       else
       {
           readBumpsOfflineData(); // Should fill the bumpsData array
       }
       clearRedundantData();
       drawAnamoliesOnMap(currentPath,mMap);
       new Thread() {
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MapsActivity.showPopUpWindow(bumpsLocationData.size());
                        }
                    });
                }
                catch (Exception e){

                }
            }
        }.start();
    }
    public double calculateLatLongDistance(LatLng source,LatLng dest){
        Location sourceLocation = new Location("sourceLocation");
        sourceLocation.setLatitude(source.latitude);
        sourceLocation.setLongitude(source.longitude);
        Location destLocation = new Location("destLocation");
        destLocation.setLatitude(dest.latitude);
        destLocation.setLongitude(dest.longitude);
        return sourceLocation.distanceTo(destLocation);
    }
}
