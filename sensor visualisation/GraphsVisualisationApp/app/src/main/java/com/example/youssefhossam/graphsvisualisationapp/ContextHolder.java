package com.example.youssefhossam.graphsvisualisationapp;

/**
 * Created by Ahmed Adel on 2/5/2018.
 */

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class ContextHolder implements Parcelable {
    public static Context context = null;

    public ContextHolder() {

    }

    public static void setContext(Context context){
        ContextHolder.context = context;
    }

    public static Context getContext(){
        return context;
    }

    protected ContextHolder(Parcel in) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ContextHolder> CREATOR = new Parcelable.Creator<ContextHolder>() {
        @Override
        public ContextHolder createFromParcel(Parcel in) {
            return new ContextHolder(in);
        }

        @Override
        public ContextHolder[] newArray(int size) {
            return new ContextHolder[size];
        }
    };
}