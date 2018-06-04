
/**
 * Created by Youssef Hossam on 09/02/2018.
 */
package stackers.bumpsfinder.productionapplicaion;

import android.location.Location;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.MathArrays;

import java.util.ArrayList;
import java.util.Arrays;



public class Anamoly {

    double [] accelValues ;
    double [] accelTime ;
    double [] speedValues ;

    int interestStartIndex ;
    int interestEndIndex;

    public Reading[] accels;
    public Reading[] speeds;
    public Location loc;
    public int type;
    public String comment;
    public boolean MLpred;
    public boolean cosSimPred;

//    Anamoly(Reading[]readings,Reading[] speeds,Location loc)
//    {
//        this.readings=readings;
//        this.speeds=speeds;
//        this.loc=loc;
//    }

    Anamoly(Reading[] accels, Reading[] speeds, Location loc)
    {
        this.accels=accels;
        this.speeds=speeds;
        this.loc=loc;
        accelValues = new double[accels.length];
        accelTime = new double[accels.length];
        speedValues = new double[speeds.length];
        convertToRelativeTime();
        for(int i = 0 ; i < accels.length ; i++) {
            accelValues[i]= (double)accels[i].value;
            accelTime[i]= (double)accels[i].relativeTime ;
            // accelValues.add((double)accels[i].value) ;
            //accelTime.add((double)accels[i].relativeTime) ;
        }
        for(int i =0 ; i < speeds.length ; i++) {
            speedValues[i] = speeds[i].value ;
        }
    }
    public void printAccelValues(){
        System.out.print("AccelValues: [ ");
        for(int i = 0 ; i < accelValues.length ; i ++){
            System.out.print(accelValues[i]);
            if(i != accelValues.length -1 )
                System.out.print(" , " );
        }
        System.out.println("]");
    }
    public void printAccelTimes(){
        System.out.print("AccelTimes: [ ");
        for(int i = 0 ; i < accelTime.length ; i ++){
            System.out.print(accelTime[i] );
            if(i != accelTime.length -1 )
                System.out.print(" , " );
        }
        System.out.println("]");
    }
    public void printSpeedValues(){
        System.out.print("SpeedValues: [ ");
        for(int i = 0 ; i < speeds.length ; i ++){
            System.out.print(speeds[i].value );
            if(i != speeds.length -1 )
                System.out.print(" , " );
        }
        System.out.println("]");
    }
    public void printSpeedTimes(){
        System.out.print("SpeedTimes: [ ");
        for(int i = 0 ; i < speeds.length ; i ++){
            System.out.print(speeds[i].time );
            if(i != speeds.length -1 )
                System.out.print(" , " );
        }
        System.out.println("]");
    }
    public  String getTypeName() {
        String[ ]names ={"MATAB" , "HOFRA" ,"GALAT", "TAKSER" ,"UNKNWON"} ;
        String miss = "" ;
        int accessIndex = type ;
        if(accessIndex > 4) {
            miss = "miss labled";
            accessIndex -= 5 ;
        }
        return  miss+names[accessIndex] ;
    }
    public void convertToRelativeTime () {
        long ref = accels[0].time ;
        if(ref == 0 )
            return;
        for(int i= 0 ;i < accels.length ; i++ ) {
            accels[i].relativeTime = ((double)(accels[i].time - ref))/Math.pow(10,9) ;
        }
    }
    double getMax(double[] array ){
        if(array.length ==0 )
            return -1 ;
        double max = array[0];
        for(int i = 0 ; i < array.length ; i++) {
            if(array[i] > max){
                max = array[i];
            }
        }

        return max ;
    }
    double getAbsSum(double[] array ){
        if(array.length ==0 )
            return -1 ;
        double sum = 0;
        for(int i = 0 ; i < array.length ; i++) {
            sum += Math.abs((array[i] )) ;
        }

        return sum ;
    }
    public void applySmoothingFilter (double fSize){
        System.out.println("size before :" + accelValues.length);
        double maxBefore = getAbsSum(accelValues);
        double [] filter = new double[(int)fSize-1];
        for(int i = 0 ; i < fSize-1 ; i++ )
            filter[i]=  1.0/fSize ;
        double [] newValues = MathArrays.convolve(accelValues , filter);
        double maxAfter = getAbsSum(newValues) ;
        for ( int i = 0 ; i < accelValues.length ; i++){
            accelValues[i]= newValues[i+(int)Math.floor(fSize/2)]*maxBefore/maxAfter;
        }
        System.out.println("size after :" + accelValues.length);
    }
    /*
        gets the start and end indicies and stores them in the class
     */
    public void getAreaOfInterest (double periodOfInterest){
        int startIndex = 0 ;
        double endTime =  periodOfInterest;
        double [] accel = accelValues;
        double[] time = accelTime ;

        double maxSum = 0; //the maximum sum of abselutes
        int maxStart = 0 ;//the index of the start of the max Sum window
        int maxEnd = 0;
        int index = 0 ;//index of the acceleration/time value
        double tempTime = 0; //time start from 0

        while ((tempTime < endTime) && ( index < time.length )) {
            maxSum += Math.abs(accel[index]) ;
            tempTime = time[index] ;
            index += 1 ;
        }
        int endIndex = index ;// now start and end are start and end indices of the window
        maxEnd= endIndex ;
        double tempSum = maxSum ;// the summation of the current window abs

        while(endIndex<time.length-1) {
            endIndex += 1;
            tempSum += Math.abs(accel[endIndex]);
            tempSum -= Math.abs(accel[startIndex]);
            startIndex += 1;
            if (tempSum > maxSum) {
                maxSum = tempSum;
                maxStart = startIndex ;
                maxEnd = endIndex ;
            }
        }
        interestStartIndex = maxStart ;
        interestEndIndex = maxEnd ;
        System.out.println("Start of Interest: " + maxStart);
        System.out.println("end of Interest: " + maxEnd);
    }
    /*
    this function works on the stored start and end indicies
     */
    public double avgAbsRatio () {
        double totalAbsSum = 0;
        double interestAbsSum = 0;
        for (int i = 0; i < accelValues.length; i++) {
            totalAbsSum += Math.abs(accelValues[i]);
            if (i >= interestStartIndex && i < interestEndIndex)
                interestAbsSum += Math.abs(accelValues[i]);

        }
        double avgTotal = totalAbsSum/accelValues.length ;
        double avgInterest = interestAbsSum/(interestEndIndex-interestStartIndex);
        System.out.println("avg Abs Ratio: " +avgInterest/avgTotal );
        return avgInterest/avgTotal ;
    }
    public int interestPeakCount(){
        int peakCount  = 0 ;
        for(int i = interestStartIndex +1 ; i<interestEndIndex-1 ; i++){
            if((accelValues[i] < accelValues[i-1] && accelValues[i] < accelValues[i+1]) ||
                    (accelValues[i] > accelValues[i-1] && accelValues[i] > accelValues[i+1]))
                peakCount ++ ;
        }
        System.out.println("Peak count: " + peakCount);
        return peakCount ;
    }
    public int interestZeroCrossings(){
        int zeroCrossings = 0 ;
        for(int i = interestStartIndex+1 ; i < interestEndIndex ; i++){
            if(accelValues[i] * accelValues[i-1] < 0 )
                zeroCrossings++ ;
        }
        System.out.println("Zero Crossings " +zeroCrossings);
        return zeroCrossings;
    }
    public double getSpeedMean (){
        DescriptiveStatistics stats = new DescriptiveStatistics();

    // Add the data from the array
        for( int i = 0; i < speeds.length; i++) {
            stats.addValue(speeds[i].value);
        }

// Compute some statistics
        System.out.println("Mean : " + stats.getMean());
        return  stats.getMean();
    }
    public double getSpeedStd (){
        DescriptiveStatistics stats = new DescriptiveStatistics();

        // Add the data from the array
        for( int i = 0; i < speeds.length; i++) {
            stats.addValue(speeds[i].value);
        }

// Compute some statistics
        System.out.println("Standard Div : "+stats.getStandardDeviation());
        return  stats.getStandardDeviation();
    }
    /**
     * uses linear interpolation and extrapolation to sample accelerometer readings for a given session length.
     *
     * @param samplingRate number of samples per second
     * @param readings     array representing the  timeline in nanoseconds of accelerometer readings
     * @param time         required session length in seconds
     * @return array of sampled readings
     */
    double[] getSampledReadings(int samplingRate, ArrayList<Reading> readings, int time) {
        double xFirst, yFirst, xSecond, ySecond, xInter, yInter;
        double Ts = 1.0 /(double) samplingRate * Math.pow(10, 9); //in nanoseconds
        int sampledReadingsCount = time * samplingRate + 1;
        double[] sampledReadings = new double[sampledReadingsCount];
        sampledReadings[0] = readings.get(0).value; //reading at t=0
        int i = 1, j = 1;
        double currentTime = Ts+readings.get(0).time;
        while (true) {
            while (i < readings.size() && currentTime > readings.get(i).time)
                i++;

            if (i == readings.size()) //recorded session is over
                break;

            xFirst = readings.get(i - 1).time;
            yFirst = readings.get(i - 1).value;
            xSecond = readings.get(i).time;
            ySecond = readings.get(i).value;
            xInter = currentTime;

            yInter = yFirst + (xInter - xFirst) / (xSecond - xFirst) * (ySecond - yFirst); //linear Interpolation
            sampledReadings[j] = yInter;
            j++;
            if (j == sampledReadingsCount) //sampling session  is over
                break;
            currentTime += Ts;
        }

        while (j < sampledReadingsCount) //assign last recorded reading to all the remaining samples (approximate extrapolation)
        {
            sampledReadings[j] = readings.get(i - 1).value;
            j++;
        }

        accelValues = sampledReadings ;

        double [] newTime = new double[time * samplingRate] ;
        for(int k  = 0 ;  k <  time * samplingRate ; k++)
            newTime[k]= (double)k / (double) samplingRate ;
        accelTime = newTime ;
        return sampledReadings;

    }
    public double getInterestSpeed(double samplingRate) {
        long startOfAccelTime = accels[0].time;
        double absStartTime = ( interestStartIndex * 1/samplingRate* Math.pow(10,9) + startOfAccelTime) ;
        double absEndTime = interestEndIndex * 1 / samplingRate * Math.pow(10,9) + startOfAccelTime ;

        for( int i = 0 ; i < speeds.length ; i++) {
            double speedTime = speeds[i].time ;
            if (speedTime > absStartTime && speedTime<absEndTime)
                return speeds[i].value ;
        }
        return getSpeedMean();
    }
    public void printType(){
        System.out.println("Type : " + getTypeName());
    }
    public void printData(){
        printAccelValues();
        printAccelTimes();
        printSpeedValues();
        printSpeedTimes();
        printType();

    }
    public double [] getFeatureVector(){
        getSampledReadings(50 , new ArrayList<Reading>(Arrays.asList(accels)) , 10) ;
        printAccelValues();
        applySmoothingFilter(5);
        printAccelValues();
        getAreaOfInterest(2);
        double avgAbs = avgAbsRatio() ;
        double peakCount = interestPeakCount() ;
        double zeroCrossings = interestZeroCrossings() ;
        double speedInterest = getInterestSpeed(50) ;
        double speedMean = getSpeedMean() ;

        double []  features = {avgAbs , peakCount , zeroCrossings , speedInterest, speedMean } ;
        return  features ;
    }

}