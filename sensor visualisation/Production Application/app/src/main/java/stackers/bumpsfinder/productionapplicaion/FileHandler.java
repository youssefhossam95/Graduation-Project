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




}
