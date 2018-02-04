package com.example.youssefhossam.graphsvisualisationapp;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Youssef Hossam on 30/01/2018.
 */

public class FileHandler {
   public int NumberOfDefects=0;
    AppCompatActivity activity;
    FileHandler(AppCompatActivity activity){
        this.activity = activity ;
    }
    void saveData(float[] accelValues, int anamolyType, Location location, String comment) throws JSONException {
        JSONArray jsArray = new JSONArray(accelValues);
        JSONObject jsonFile= new JSONObject();
        try
        {
            jsonFile.put("accelVal",jsArray);
            jsonFile.put("anamolyType",anamolyType);
            jsonFile.put("Location",location.toString());
            jsonFile.put("Comment", comment);
            jsonFile.put("_id",String.valueOf(android.os.Build.MODEL)+ DateFormat.getDateTimeInstance().format(new Date()));
            ;
        }
        catch(Exception e){
            Log.e("log_tag", "Error in  JsonFIle "+e.toString());
            e.printStackTrace();

        }
        NumberOfDefects++;
        writeToFile("Object"+NumberOfDefects,jsonFile.toString());
        //    File F=new File(readFromFile());
        //   JSONObject jsonObj = new JSONObject(F.toString());
        //Log.e("doola",jsonObj.getString("_id"));//
    }
    public void uploadLocalData() {
        try
        {
            Log.e("Welcome","Uploading  NumberOfData= "+NumberOfDefects);
            String url="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/";
            int temp=NumberOfDefects;
            for(int i=0;i<temp;i++)
            {

                Log.e("Number of Defects",String.valueOf(NumberOfDefects));
                File F=new File(readFromFile("Object"+(i+1)));
                JSONObject jsonObj = new JSONObject(F.toString());
                BackgroundWorker BW=new BackgroundWorker(activity.getApplicationContext(),url,"POST");
                String Result=  BW.execute(jsonObj).get(2000, TimeUnit.MILLISECONDS);
                if(Integer.valueOf(Result)==201)
                {
                    Toast.makeText(activity.getApplicationContext(),"File "+(i+1)+" Uploaded Successfully",Toast.LENGTH_SHORT).show();
                    activity.deleteFile("Object"+(i+1)+".txt");
                    NumberOfDefects--;
                }
                else
                {
                    Toast.makeText(activity.getApplicationContext(),"File "+(i+1)+" Not Uploaded Successfully",Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception E)
        {
            Log.e("Error ya dola",E.toString());
        }


    }
    private void writeToFile(String FileName,String Data) {
        try {
            FileOutputStream fileOutputStream =  activity.openFileOutput(FileName+".txt", Context.MODE_PRIVATE);
            fileOutputStream.write(Data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public String readFromFile(String FileName) {

        String ret = "";
        try {
            InputStream inputStream = activity.openFileInput(FileName+".txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Filer Handler Class :", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Filer Handler Class :", "Can not read file: " + e.toString());
        }
        return ret;
    }

public String[] getAllFiles()
{
    if(NumberOfDefects!=0)
    {
        String[] allFiles=new String[NumberOfDefects];
        for (int i=0;i<NumberOfDefects;i++)
        {
            allFiles[i]=readFromFile("Object"+(i+1));
        }
        return allFiles;
    }
    else return null;
}


}
