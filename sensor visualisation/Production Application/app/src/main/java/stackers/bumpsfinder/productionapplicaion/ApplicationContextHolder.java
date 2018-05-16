package stackers.bumpsfinder.productionapplicaion;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class ApplicationContextHolder implements Parcelable {
    public static Context mContext = null;

    public static void setContext(Context context){
        ApplicationContextHolder.mContext = context;
    }

    public static Context getContext(){
        return mContext;
    }

    protected ApplicationContextHolder(Parcel in) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ApplicationContextHolder> CREATOR = new Parcelable.Creator<ApplicationContextHolder>() {
        @Override
        public ApplicationContextHolder createFromParcel(Parcel in) {
            return new ApplicationContextHolder(in);
        }

        @Override
        public ApplicationContextHolder[] newArray(int size) {
            return new ApplicationContextHolder[size];
        }
    };
}
