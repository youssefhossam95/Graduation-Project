package com.stackers.BumpsFinder.TrainingApplication;

/**
 * represents a captured accelerometer reading
 * Created by Youssef Hossam on 06/10/2017.
 */
public class Reading {

    public long time;
    public float value;
    public double relativeTime ;
    public Reading() {}

    public Reading(long time,float val)
    {
        this.time=time;
        this.value=val;
    }

}

