package com.stackers.BumpsFinder.TrainingApplication;

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
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Youssef Hossam on 30/01/2018.
 */

public class FileHandler implements Parcelable {
    public  int NumberOfDefects=0;
    static FileHandler myFile=null;
    private static boolean []AvailableFiles=new boolean[100];
    public static boolean isCurrentlyUploading=false;
    ContextHolder contextHolder;
 private   FileHandler(){
        for(int i=0;i<100;i++)
        {
            AvailableFiles[i]=false;
        }
        ContextHolder contextHolder=new ContextHolder();
        String[] Result=readFromFile("Defects");
        if(Result[0]!=null)
        {
            int i=0;
                while(i<100 && Result[i]!=null)
                {
                    AvailableFiles[Integer.valueOf(Result[i])]=true;
                    NumberOfDefects++;
                    i++;
                }
        }

    }

    public static FileHandler getFileHandlerObject()
    {
        if(myFile==null) {
            myFile = new FileHandler();
        }
            return myFile;
    }


    public int getNumberOfDefects()
    {
        int temp=0;
        for (int i=0;i<100;i++)
        {
                String temp1=readSingleFile("File"+(i));
                if(temp1!=null)
                {
                    AvailableFiles[i]=true;
                    temp++;
                }
                else
                {
                    AvailableFiles[i]=false;
                }
        }
        NumberOfDefects=temp;
        return NumberOfDefects;
    }
    void saveData(Anamoly lastAnamoly,String userName) throws JSONException {
        int anamolyType=lastAnamoly.type;
        Location location=lastAnamoly.loc;
        String comment=lastAnamoly.comment;
        Reading[] accelValues=lastAnamoly.readings;
        float[] accelValue=new float[accelValues.length];
        long [] accelTime=new long[accelValues.length];
        for(int i=0;i<accelValues.length;i++)
        {
            accelValue[i]=accelValues[i].value;
            accelTime[i]=accelValues[i].time;
        }
        Reading [] speedValues=lastAnamoly.speeds;
        float[] speedValue=new float[speedValues.length];
        long [] speedTime=new long[speedValues.length];
        for(int i=0;i<speedValues.length;i++)
        {
            speedValue[i]=speedValues[i].value;
            speedTime[i]=speedValues[i].time;
        }
        JSONArray accelValueArray=new JSONArray(accelValue);
        JSONArray accelTimeArray=new JSONArray(accelTime);
        JSONArray speedValueArray=new JSONArray(speedValue);
        JSONArray speedTimeArray=new JSONArray(speedTime);
        JSONObject jsonFile= new JSONObject();
        try
        {
            jsonFile.put("accelValues",accelValueArray);
            jsonFile.put("accelTime",accelTimeArray);
            jsonFile.put("speedValues",speedValueArray);
            jsonFile.put("speedTime",speedTimeArray);
            jsonFile.put("anamolyType",anamolyType);
            jsonFile.put("Location",location.toString());
            jsonFile.put("Comment", comment);
            jsonFile.put("_id",userName+" "+String.valueOf(android.os.Build.MODEL)+ DateFormat.getDateTimeInstance().format(new Date()));
            ;
        }
        catch(Exception e){
            Log.e("log_tag", "Error in  JsonFIle "+e.toString());
            e.printStackTrace();

        }
        int temp=0;
        for (int i=0;i<100;i++)
        {
            String temp1=readSingleFile("File"+(i));
            if(temp1==null)
            {
                AvailableFiles[i]=true;
                writeToFile("File"+String.valueOf(i),jsonFile.toString());
                NumberOfDefects++;
                Log.e("file is Here",String.valueOf(i));
                return;
            }
        }

    }

    public boolean uploadLocalData()
    {
        if(NumberOfDefects==0)
            return false;

        isCurrentlyUploading=true;
        displayExceptionMessage("Uploading files...");
        Thread t=new Thread(){
          public void run(){
              upload();
              isCurrentlyUploading=false;
          }
        };
        t.start();
       return true;
    }

    public boolean upload() {
        try
        {

            Log.e("Welcome","Uploading  NumberOfData= "+NumberOfDefects);
            String url="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/";
            int temp=NumberOfDefects;
            boolean result=false;
            for(int i=0;i<100;i++)
            {
                if(AvailableFiles[i]==true)
                {
                    Log.e("Number of Defects",String.valueOf(NumberOfDefects));
                    File F=new File(readSingleFile("File"+(i)));
                    JSONObject jsonObj = new JSONObject(F.toString());
                    BackgroundWorker BW=new BackgroundWorker(contextHolder.getContext(),url,"POST",this);
                    String Result=  BW.execute(jsonObj).get(5, TimeUnit.SECONDS);
                    if(Integer.valueOf(Result)==201)
                    {
                        Log.e("App Context",String.valueOf(contextHolder.getContext()));
                        deleteFile("File"+(i)+".txt");
                        NumberOfDefects--;
                        result=true;
                    }
                    else if(Integer.valueOf(Result)==409)
                    {
                        Log.e("Dola","i = "+String.valueOf(i));
                        deleteFile("File"+(i)+".txt");
                        displayExceptionMessage("File "+(i+1)+" Already uploaded to the server");
                    }
                    else
                    {
                       displayExceptionMessage("File "+(i+1)+" Not Uploaded");
                    }
                }

            }
            displayExceptionMessage("files uploaded!");
            getNumberOfDefects();
            return result;
        }
        catch (Exception E)
        {
            displayExceptionMessage("Server is Busy or Uploading the File please try again after 1 minute or close and open the application");
            getNumberOfDefects();
            return false;
        }

    }
    public void writeToFile(String FileName,String Data) {
        try {
            FileOutputStream fileOutputStream =  contextHolder.getContext().openFileOutput(FileName+".txt", Context.MODE_PRIVATE);
            fileOutputStream.write(Data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public String[] readFromFile(String FileName) {

        String [] ret =new String[100];
        int i=0;
        try {
            InputStream inputStream = contextHolder.getContext().openFileInput(FileName+".txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                  //  stringBuilder.append(receiveString);
                    ret[i]=receiveString;
                    i++;
                }

                inputStream.close();
              //  ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
          //  Log.e("File Handler Class :", "File not found Error: " + e.toString());
        } catch (IOException e) {
            Log.e("File Handler Class :", "Can not read file: " + e.toString());
        }
        return ret;
    }
    public String readSingleFile(String FileName) {

        String ret ="";
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
          //  Log.e("Filer Handler Class :", "File not found: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.e("Filer Handler Class :", "Can not read file: " + e.toString());
        }
        return ret;
    }
    public String[] getAllFiles() {
        getNumberOfDefects();
        if(NumberOfDefects!=0)
        {
            String[] allFiles=new String[NumberOfDefects];
            int j=0;
            JSONObject obj;
            for (int i=0;i<100;i++)
            {
                if(AvailableFiles[i]==true )
                {
                    String temp=readSingleFile("File"+(i));
                    if(temp!=null)
                    {
                    try
                    {   obj = new JSONObject(temp);
                        allFiles[j]="File"+(i+1)+"\n"+"id : "+obj.getString("_id")+"\n"+"Location"+obj.getString("Location")+"\n"+"anamoly Type : "+obj.getString("anamolyType")+"\n"+"Comment :"+obj.getString("Comment");
                        j++;
                    }
                    catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + temp + "\"");
                    }
                    }
                    else
                    {
                        AvailableFiles[i]=false;
                        NumberOfDefects--;
                    }
                }
            }
            String [] result=new String[j];
            for (int k=0;k<j;k++)
            {
                result[k]=allFiles[k];
            }
            return result;
        }
        else return null;
    }
    public boolean[] getAvailableFiles() {
        return AvailableFiles;
    }
    public boolean deleteFile(String fileName) {
        if(NumberOfDefects!=0)
        {
            Log.e("Delete Func App Context",String.valueOf(contextHolder.getContext()));
            boolean fs=contextHolder.getContext().deleteFile(fileName);
           if(fs)
           {
               String []temp=fileName.split("File");
               String []val=temp[1].split(".txt");
               AvailableFiles[Integer.valueOf(val[0])]=false;
               NumberOfDefects--;
               Log.e("filerHandler Context","after Deleting File Defects="+String.valueOf(NumberOfDefects));
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

    public void displayExceptionMessage(String msg) {
        final String text = msg;
        final AppCompatActivity activity = (AppCompatActivity) contextHolder.getContext();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
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