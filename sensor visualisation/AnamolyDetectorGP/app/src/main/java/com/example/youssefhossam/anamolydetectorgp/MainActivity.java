package com.example.youssefhossam.anamolydetectorgp;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Sensor mMagnetic;
    float[] accelValues;
    float[] correctedAccelValues;
    float[] gravityValues;
    float[] magnetValues;
    float[] rotationMatrix;
    float[] rotationMatrixTranspose;
    File statsFile;
    String fileName="StatsFile";
    double mean,variance;
    long readingsCount;
    double readingsSum,varianceTotal;
    double prevZ,prevSlope;
    boolean isPrevZAnamolous;
    boolean ignoreAnamoly; //remains true after an anamoly until a non-anamolous peak occurs.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_NORMAL);
            rotationMatrix = new float[16];
            rotationMatrixTranspose = new float[16];
            accelValues = new float[4];
            correctedAccelValues = new float[4];
            gravityValues = new float[3];
            magnetValues = new float[3];
            statsFile=new File(getApplicationContext().getFilesDir(),fileName);
            if(statsFile.createNewFile()){
                mean=variance=0;
                readingsCount=0;
            }
            else{
                BufferedReader bf = new BufferedReader(new FileReader(statsFile));
                String s=bf.readLine();
                readingsCount=Long.parseLong(s);
                s=bf.readLine();
                mean=Double.parseDouble(s);
                s=bf.readLine();
                variance=Double.parseDouble(s);
            }
            readingsSum=readingsCount*mean;
            varianceTotal=readingsCount*variance;
            prevZ=0;
            prevSlope=1;
            isPrevZAnamolous=false;
            ignoreAnamoly=false;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            displayExceptionMessage(e.getMessage());
        }
    }


    protected void onResume() {
        super.onResume();
        try {
            mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGravity, SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mMagnetic, SENSOR_DELAY_NORMAL);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            displayExceptionMessage(e.getMessage());
        }
    }

    protected void onPause() {
        super.onPause();

        //mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void onSensorChanged(SensorEvent event) {
        try{
            switch (event.sensor.getType()) {
                case Sensor.TYPE_LINEAR_ACCELERATION: {
                    accelValues[0] = event.values[0];
                    accelValues[1] = event.values[1];
                    accelValues[2] = event.values[2];
                    accelValues[3] = 1;
                    if (getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)) {
                        transposeM(rotationMatrixTranspose, 0, rotationMatrix, 0);
                        multiplyMV(correctedAccelValues, 0, rotationMatrixTranspose, 0, accelValues, 0);
                        if(!isPrevZAnamolous && isPrevZPeak()) //a non-anamolous peak occured
                           ignoreAnamoly=false;
                        processZValue();
                    }

                    break;
                }
                case Sensor.TYPE_GRAVITY: {
                    gravityValues = event.values.clone();
                    break;
                }
                case Sensor.TYPE_MAGNETIC_FIELD: {
                    magnetValues = event.values.clone();
                    break;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            displayExceptionMessage(e.getMessage());
        }

    }


    /**
     * adds the current z value to the mean and variance and takes action if current z is anamolous.
     */
    void processZValue() {
        double absZValue=Math.abs(correctedAccelValues[2]);
        readingsSum+=absZValue;
        readingsCount++;
        mean=readingsSum/readingsCount;
        varianceTotal+=Math.pow(absZValue-mean,2);
        variance=varianceTotal/readingsCount;
        boolean isZanamolous=isCurrentZAnamolous();
        if(ignoreAnamoly) //ignore anamoly if a recent anamoly happened.
            return;
        // hena han3ml hwar eno ys2l el user w keda.

    }

    /**
     *
     * @return returns wether the previous z value was a peak or not.
     */
    boolean isPrevZPeak()
    {
        boolean isTurningPoint;
        double currentSlope=correctedAccelValues[2]-prevZ;
        isTurningPoint=((currentSlope/prevSlope)<0);//opposite slope signs
        prevSlope=currentSlope;
        prevZ=correctedAccelValues[2];
        return isTurningPoint;
    }

    /**
     * checks if current z value is anamolous
     * @return
     */
    boolean isCurrentZAnamolous()
    {
        double value=Math.abs(correctedAccelValues[2]);
        double e=0.92,cumProbability;
        NormalDistribution d=new NormalDistribution(mean,Math.sqrt(variance));
        cumProbability=d.cumulativeProbability(value);
        isPrevZAnamolous=(cumProbability>0.99);
        return isPrevZAnamolous;

    }
    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}

