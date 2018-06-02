package stackers.bumpsfinder.productionapplicaion;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ahmed on 4/28/2018.
 */

public class TaskRequestDirections extends AsyncTask<String, Void, String> {
    private GoogleMap myMap;
    private Context myContext;
    MapsActivity myMapActivity;
    public TaskRequestDirections(GoogleMap newMap,Context newContext,MapsActivity mapsActivity)
    {
        myMap = newMap;
        myContext = newContext;
        myMapActivity = mapsActivity;
    }
    @Override
    protected String doInBackground(String... strings) {
        String responseString = "";
        try {
            responseString = requestDirection(strings[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        //Parse json here
        TaskParser taskParser = new TaskParser(myMap,myMapActivity);
        taskParser.execute(s);
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }
}

