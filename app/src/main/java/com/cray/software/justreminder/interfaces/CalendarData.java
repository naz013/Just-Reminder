package com.cray.software.justreminder.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

public class CalendarData implements Parcelable {
    private String type, name, number;
    private long id, date;

    public CalendarData(String type, String name, String number, long id, long date){
        this.type = type;
        this.name = name;
        this.id = id;
        this.date = date;
        this.number = number;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getDate(){
        return date;
    }

    public void setDate(long date){
        this.date = date;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public CalendarData(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<CalendarData> CREATOR = new Parcelable.Creator<CalendarData>() {
        public CalendarData createFromParcel(Parcel in) {
            return new CalendarData(in);
        }

        public CalendarData[] newArray(int size) {

            return new CalendarData[size];
        }

    };

    public void readFromParcel(Parcel in) {
        type = in.readString();
        name = in.readString();
        number = in.readString();
        id = in.readLong();
        date = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeLong(id);
        dest.writeLong(date);
    }
}
