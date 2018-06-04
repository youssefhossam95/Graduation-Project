package stackers.bumpsfinder.productionapplicaion;

import android.app.Application;
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
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;



public class FileHandler {

    private static final String TAG = "FileHandler";
    private static FileHandler myFileHandler;
    public  int NumberOfDefects = 0;
    private static boolean [] AvailableFiles = new boolean[500];
    public static boolean isCurrentlyUploading = false;

    private   FileHandler(){

    }
    public static  FileHandler getFileHandler() {
        if(myFileHandler==null)
        {
            myFileHandler = new FileHandler();
        }
        return myFileHandler;
    }
    public void writeToFile(String FileName,String Data) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ApplicationContextHolder.getContext().openFileOutput(FileName+".txt", Context.MODE_APPEND));
            outputStreamWriter.write(Data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public void clearFile(String FileName){
        try {
            FileOutputStream fileOutputStream =  ApplicationContextHolder.getContext().openFileOutput(FileName+".txt", Context.MODE_PRIVATE);
            fileOutputStream.write("".getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public ArrayList<String> readFromFile(String FileName) {
        Log.d(TAG,"readFromFile : reading data from file "+FileName);
        ArrayList<String> result = new ArrayList<String>();
        try {
            InputStream inputStream = ApplicationContextHolder.getContext().openFileInput(FileName+".txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    result.add(receiveString);
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
              Log.e(TAG, "readFromFile FileNotFoundException : " + e.toString());
              return null;
        } catch (IOException e) {
            Log.e(TAG, "readFromFile IOException : " + e.toString());
            return null;
        }
        return result;
    }
    public String readSingleFile(String FileName) {
        String ret ="";
        try {
            InputStream inputStream = ApplicationContextHolder.getContext().openFileInput(FileName+".txt");
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
            String[] allFiles= new String[NumberOfDefects];
            int j=0;
            JSONObject obj;
            for (int i=0;i < 500; i++)
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
            String [] result = new String[j];
            for (int k=0;k<j;k++)
            {
                result[k]=allFiles[k];
            }
            return result;
        }
        else return null;
    }
    public int getNumberOfDefects() {
        int temp = 0;
        for (int i = 0; i < 500; i++)
        {
            String temp1=readSingleFile("File"+(i));
            if( temp1 != null)
            {
                AvailableFiles[i]=true;
                temp++;
            }
            else
            {
                AvailableFiles[i]=false;
            }
        }
        NumberOfDefects = temp;
        return NumberOfDefects;
    }
    public boolean deleteFile(String fileName) {
        if(NumberOfDefects!=0)
        {
            Log.e("Delete Func App Context",String.valueOf(ApplicationContextHolder.getContext()));
            boolean fs=ApplicationContextHolder.getContext().deleteFile(fileName);
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
    void saveData(Anamoly lastAnamoly,String userName) throws JSONException {
        int anamolyType=lastAnamoly.type;
        Location location=lastAnamoly.loc;
        String comment=lastAnamoly.comment;
        Reading[] accelValues=lastAnamoly.accels;
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
        int temp = 0;
        for (int i=0; i<500; i++)
        {
            String temp1 = readSingleFile("File"+(i));
            if(temp1 == null)
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
        isCurrentlyUploading = true;
        Thread t=new Thread(){
            public void run(){
                upload();
                isCurrentlyUploading = false;
            }
        };
        t.start();
        return true;
    }

    public boolean upload() {
        try
        {

            Log.e("Welcome","Uploading  NumberOfData= "+NumberOfDefects);
            String url="https://ac89aed5-3fa3-48cf-b18d-dcda366b5b3f-bluemix.cloudant.com/simpledb/"; // To Be Edited testdb
            int temp=NumberOfDefects;
            boolean result=false;
            for(int i=0;i<500;i++)
            {
                if(AvailableFiles[i]==true)
                {
                    Log.e("Number of Defects",String.valueOf(NumberOfDefects));
                    File F=new File(readSingleFile("File"+(i)));
                    httpBackgroundConnection BW=new httpBackgroundConnection(ApplicationContextHolder.getContext(),url,"POST");
                    String Result =  BW.execute(F.toString()).get(5, TimeUnit.SECONDS);
                    if(Integer.valueOf(Result) == 201)
                    {
                        Log.e("App Context",String.valueOf(ApplicationContextHolder.getContext()));
                        deleteFile("File"+(i)+".txt");
                        NumberOfDefects--;
                        result=true;

                    }
                    else if(Integer.valueOf(Result) == 409)
                    {
                        Log.e("Dola","i = "+String.valueOf(i));
                        deleteFile("File"+(i)+".txt");

                    }
                    else
                    {
                        Log.e(TAG,"upload : File "+(i+1)+" Not uploaded");
                    }
                }

            }
            getNumberOfDefects();
            return result;
        }
        catch (Exception E)
        {
          //  displayExceptionMessage("Server is Busy or Uploading the File please try again after 1 minute or close and open the application");
            getNumberOfDefects();
            E.printStackTrace();
            return false;
        }

    }
}
