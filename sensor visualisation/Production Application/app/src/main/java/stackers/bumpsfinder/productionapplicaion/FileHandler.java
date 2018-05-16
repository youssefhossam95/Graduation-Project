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
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;



public class FileHandler {

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
            FileOutputStream fileOutputStream =  ApplicationContextHolder.getContext().openFileOutput(FileName+".txt", Context.MODE_PRIVATE);
            fileOutputStream.write(Data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public String[] readFromFile(String FileName) {

        String [] ret =new String[500];
        int i=0;
        try {
            InputStream inputStream = ApplicationContextHolder.getContext().openFileInput(FileName+".txt");
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
