package com.example.youssefhossam.graphsvisualisationapp;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ahmed on 10/19/2017.
 */

public class BackgroundWorker extends AsyncTask<String,Void,String>
{
    Context context;
    AlertDialog alertDialog;
    BackgroundWorker(Context ctx)
    {
    context=ctx;
    }




    @Override
    protected String doInBackground(String... voids) {
        // for debug worker thread
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

        String UrlSite="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/bumpsdb/Bump";
        try {
            URL url=new URL(UrlSite);
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            String authorization = "ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix:32bd9c8968033ee6735bcb0919ed2f85f759c8bc8a0a51c03d6cd2647eeb7e66";
            String basicAuth = "Basic " + new String(Base64.encodeToString(authorization.getBytes(),Base64.DEFAULT)).replace("\n", "");;
            Log.e("Opening Connection","Connection Opened");
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization",basicAuth);
            httpURLConnection.setDoInput(true);
            int Num=httpURLConnection.getResponseCode();
            Log.e("Code",Integer.toString(Num));
            InputStream inputStream=httpURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            String Result="";
            String line;
            while((line=bufferedReader.readLine())!=null)
            {
            Result+=line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return Result.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        alertDialog.setMessage(result);
        alertDialog.show();
    }

    @Override
    protected void onPreExecute() {
       alertDialog=new AlertDialog.Builder(context).create();
       alertDialog.setTitle("Getting DB");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
