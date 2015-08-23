package com.hexrain.flextcal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import hirondelle.date4j.DateTime;

/**
 * Created by nazar on 18.08.15.
 */
public class FlextData implements Parcelable {

    private String task;
    private int hasEvent1, hasEvent2, resource;
    private DateTime key;

    public FlextData(Date key, String task, int hasEvent1, int hasEvent2, int resource){
        this.key = FlextHelper.convertDateToDateTime(key);
        this.task = task;
        this.hasEvent1 = hasEvent1;
        this.hasEvent2 = hasEvent2;
        this.resource = resource;
    }

    public FlextData(DateTime key, String task, int hasEvent1, int hasEvent2, int resource){
        this.key = key;
        this.task = task;
        this.hasEvent1 = hasEvent1;
        this.hasEvent2 = hasEvent2;
        this.resource = resource;
    }

    public DateTime getKey(){
        return key;
    }

    public String getTask(){
        return task;
    }

    public void setKey(DateTime key){
        this.key = key;
    }

    public void setKey(Date key){
        this.key = FlextHelper.convertDateToDateTime(key);
    }

    public void setTask(String task){
        this.task = task;
    }

    public int getResource(){
        return resource;
    }

    public void setResource(int resource){
        this.resource = resource;
    }

    public int hasEvent1(){
        return hasEvent1;
    }

    public void setHasEvent1(int hasEvent1){
        this.hasEvent1 = hasEvent1;
    }

    public int hasEvent2(){
        return hasEvent2;
    }

    public void setHasEvent2(int hasEvent2){
        this.hasEvent2 = hasEvent2;
    }

    public FlextData(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<FlextData> CREATOR = new Parcelable.Creator<FlextData>() {
        public FlextData createFromParcel(Parcel in) {
            return new FlextData(in);
        }

        public FlextData[] newArray(int size) {

            return new FlextData[size];
        }

    };

    public void readFromParcel(Parcel in) {
        hasEvent1 = in.readInt();
        hasEvent2 = in.readInt();
        resource = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hasEvent1);
        dest.writeInt(hasEvent2);
        dest.writeInt(resource);
    }
}
