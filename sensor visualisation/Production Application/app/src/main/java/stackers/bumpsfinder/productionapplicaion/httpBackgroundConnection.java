package stackers.bumpsfinder.productionapplicaion;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.FileHandler;

/**
 * Created by Ahmed on 10/19/2017.
 */

public class httpBackgroundConnection extends AsyncTask<String,Void,String>
{

    Context context;
    String urlSite;
    String methodType;
    FileHandler myFile;
    httpBackgroundConnection(Context ctx,String url,String Method)
    {   if(android.os.Debug.isDebuggerConnected())
        android.os.Debug.waitForDebugger();
        urlSite=url;
        this.context=ctx;
        methodType=Method;
    }

    @Override
    protected String doInBackground(String ... Data){
        // for debug worker thread
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

    //    JSONObject jsonFile=Data[0];
        try {
            URL url=new URL(urlSite);
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            String authorization = "ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix:32bd9c8968033ee6735bcb0919ed2f85f759c8bc8a0a51c03d6cd2647eeb7e66";
            String basicAuth = "Basic " + new String(Base64.encodeToString(authorization.getBytes(),Base64.DEFAULT)).replace("\n", "");;
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setRequestMethod(methodType);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Authorization",basicAuth);
            httpURLConnection.setDoInput(true);
   //         httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();

            int Num=httpURLConnection.getResponseCode();
            if(Num==200)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                return sb.toString();
            }
            Log.e("Code",Integer.toString(Num));
            return Integer.toString(Num);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
