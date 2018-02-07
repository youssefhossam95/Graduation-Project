package com.example.youssefhossam.graphsvisualisationapp;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
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
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Youssef Hossam on 30/01/2018.
 */

public class FileHandler implements Parcelable {
    public  int NumberOfDefects=0;
    ContextHolder contextHolder;
    FileHandler(){
        ContextHolder contextHolder=new ContextHolder();
    }
    public int getNumberOfDefects()
    {
        return NumberOfDefects;
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
    public boolean uploadLocalData() {
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
                BackgroundWorker BW=new BackgroundWorker(contextHolder.getContext(),url,"POST");
                String Result=  BW.execute(jsonObj).get(2000, TimeUnit.MILLISECONDS);
                if(Integer.valueOf(Result)==201)
                {
                    Toast.makeText(contextHolder.getContext(),"File "+(i+1)+" Uploaded Successfully",Toast.LENGTH_SHORT).show();
                    Log.e("App Context",String.valueOf(contextHolder.getContext()));
                    contextHolder.getContext().deleteFile("Object"+(i+1)+".txt");
                    NumberOfDefects--;
                    return true;
                }
                else
                {
                    Toast.makeText(contextHolder.getContext(),"File "+(i+1)+" Not Uploaded Successfully",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        catch (Exception E)
        {
            Toast.makeText(contextHolder.getContext(),"Error in files Exception",Toast.LENGTH_SHORT).show();
            return false;
        }

        return false;
    }
    private void writeToFile(String FileName,String Data) {
        try {
            FileOutputStream fileOutputStream =  contextHolder.getContext().openFileOutput(FileName+".txt", Context.MODE_PRIVATE);
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
            InputStream inputStream = contextHolder.getContext().openFileInput(FileName+".txt");
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
                allFiles[i]="Object"+(i+1)+"\n"+readFromFile("Object"+(i+1));
            }
            return allFiles;
        }
        else return null;
    }
    public boolean deleteFile(String fileName)
    {
        if(NumberOfDefects!=0)
        {
            Log.e("Delete Func App Context",String.valueOf(contextHolder.getContext()));
            boolean fs=contextHolder.getContext().deleteFile(fileName);
           if(fs)
           {
               NumberOfDefects--;
               Log.e("DOLA",String.valueOf(NumberOfDefects));
               return  true;
           }
           else
           {
               return false;
           }

        }
        else
        {
            return false;
        }

    }

    protected FileHandler(Parcel in) {
        NumberOfDefects = in.readInt();
        contextHolder = (ContextHolder) in.readValue(ContextHolder.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(NumberOfDefects);
        dest.writeValue(contextHolder);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FileHandler> CREATOR = new Parcelable.Creator<FileHandler>() {
        @Override
        public FileHandler createFromParcel(Parcel in) {
            return new FileHandler(in);
        }

        @Override
        public FileHandler[] newArray(int size) {
            return new FileHandler[size];
        }
    };
}