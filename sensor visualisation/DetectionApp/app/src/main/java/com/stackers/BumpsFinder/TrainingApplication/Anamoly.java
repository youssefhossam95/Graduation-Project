package com.stackers.BumpsFinder.TrainingApplication;

import android.location.Location;

/**
 * Created by Youssef Hossam on 09/02/2018.
 */

public class Anamoly {

    public Reading[] readings;
    public Reading[] speeds;
    public Location loc;
    public int type;
    public String comment;
    Anamoly(Reading[]readings,Reading[] speeds,Location loc)
    {
        this.readings=readings;
        this.speeds=speeds;
        this.loc=loc;
    }

}