package stackers.bumpsfinder.productionapplicaion;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Youssef Hossam on 16/05/2018.
 */

public class MachineLearning {

    static double[][] layer1Weights;
    static double[][] layer2Weights;
    static double [][]layer1bias;
    static double[][]layer2bias;
    static double[] mean;
    static double[] var;

    public static MapsActivity activity;

    private static final String TAG = "MachineLearning";
    public static void loadNNParams() throws IOException {

        InputStream is=null;
        is = activity.getResources().openRawResource(R.raw.nnweights1);
        BufferedReader input =  new BufferedReader(new InputStreamReader(is), 1024*8);
        layer1Weights=loadCsvToMatrix(input);

        is=activity.getResources().openRawResource(R.raw.nnweights2);
        input=new BufferedReader(new InputStreamReader(is), 1024*8);
        layer2Weights=loadCsvToMatrix(input);



        is=activity.getResources().openRawResource(R.raw.nnbias1);
        input=new BufferedReader(new InputStreamReader(is), 1024*8);
        layer1bias=loadCsvToMatrix(input);

        is=activity.getResources().openRawResource(R.raw.nnbias2);
        input=new BufferedReader(new InputStreamReader(is), 1024*8);
        layer2bias=loadCsvToMatrix(input);



        is=activity.getResources().openRawResource(R.raw.mean);
        input=new BufferedReader(new InputStreamReader(is), 1024*8);
        mean=loadCsvToArray(input) ;


        is=activity.getResources().openRawResource(R.raw.var);
        input=new BufferedReader(new InputStreamReader(is), 1024*8);
        var=loadCsvToArray(input) ;


    }


    public static double [][] loadCsvToMatrix(BufferedReader input) throws IOException {
        String line = null;
        ArrayList<String[]> stringVals=new ArrayList<String[]>();

        while (( line = input.readLine()) != null)
            stringVals.add(line.split(","));

        double[][]weights=new double[stringVals.size()][stringVals.get(0).length];
        for(int i=0;i<stringVals.size();i++){
            for(int j=0;j<stringVals.get(i).length;j++)
                weights[i][j]=Double.parseDouble(stringVals.get(i)[j]);
        }
        return weights;
    }
    public static double forwardProp(double [] features){

        Array2DRowRealMatrix featuresM=new Array2DRowRealMatrix(features);
        Array2DRowRealMatrix layer1W=new Array2DRowRealMatrix(layer1Weights);
        Array2DRowRealMatrix layer2W=new Array2DRowRealMatrix(layer2Weights);
        Array2DRowRealMatrix layer1B=new Array2DRowRealMatrix(layer1bias);
        Array2DRowRealMatrix layer2B=new Array2DRowRealMatrix(layer2bias);

        layer1W=(Array2DRowRealMatrix)layer1W.transpose();
        layer2W=(Array2DRowRealMatrix)layer2W.transpose();
        //bias no transpose

        Array2DRowRealMatrix z1=layer1W.multiply(featuresM);
        z1=z1.add(layer1B);
        double[][] a1Arr=z1.getData();

        for (double[] arr : a1Arr) { //relu
            for (double x : arr)
                x = Math.max(x, 0);
        }

        Array2DRowRealMatrix z2=layer2W.multiply(new Array2DRowRealMatrix(a1Arr));
        z2=z2.add(layer2B);
        Sigmoid s=new Sigmoid();
        return s.value(z2.getData()[0][0]);

    }

    public static double [] loadCsvToArray( BufferedReader bufferedReader) throws IOException {
        String line = bufferedReader.readLine() ;
        String[] valuesStr = line.split(",");
        double[] doubles = new double[valuesStr.length] ;
        for(int i = 0 ; i < valuesStr.length ; i ++ )
        {
            float value = Float.parseFloat(valuesStr[i]) ;
            doubles[i]= value;
        }

        return doubles ;
    }

    public static boolean setAnamolyPrediction(Anamoly anamoly){
        Log.d(TAG,"setAnamolyPrediction for anamoly type = "+anamoly.type);
        return anamoly.MLpred = forwardProp(scaleFeatures(anamoly.getFeatureVector()))>0.5;
    }

    public static double[] scaleFeatures (double[] features){
        double [] scaledFeatures = new double[features.length] ;
        for( int i = 0 ;i  < features.length ; i ++ ) {
            scaledFeatures[i] = (features[i]-mean[i])/Math.sqrt(var[i]) ;
        }
        return  scaledFeatures ;
    }



}
