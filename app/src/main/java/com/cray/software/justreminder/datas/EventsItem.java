package com.cray.software.justreminder.datas;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class EventsItem implements Parcelable {

    /**
     * Strings.
     */
    private String type, name, number, time;

    /**
     * Date in mills and event identifier.
     */
    private long id, date;

    /**
     * Day, month, year and event color.
     */
    private int day, month, year, color;

    /**
     * Event type field.
     */
    private EventType inn;

    /**
     * Event item constructor.
     * @param type type.
     * @param name title.
     * @param number event phone number.
     * @param id event identifier.
     * @param date event date in milliseconds.
     * @param day event day of month.
     * @param month event month.
     * @param year event year.
     * @param inn event type enum.
     * @param color event color.
     */
    public EventsItem(final String type, final String name, final String number,
                      final long id, final long date, final int day,
                      final int month, final int year, final EventType inn,
                      final int color) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.date = date;
        this.number = number;
        this.day = day;
        this.month = month;
        this.year = year;
        this.inn = inn;
        this.color = color;
    }

    public final int getColor() {
        return color;
    }

    public final void setColor(final int color) {
        this.color = color;
    }

    public final EventType getInn(){
        return inn;
    }

    public final void setInn(final EventType inn){
        this.inn = inn;
    }

    public final int getYear(){
        return year;
    }

    public final void setYear(final int year){
        this.year = year;
    }

    public final int getMonth(){
        return month;
    }

    public final void setMonth(final int month){
        this.month = month;
    }

    public final int getDay(){
        return day;
    }

    public final void setDay(final int day){
        this.day = day;
    }

    public final String getTime(){
        return time;
    }

    public final void setTime(final String time){
        this.time = time;
    }

    public final long getId(){
        return id;
    }

    public final void setId(final long id){
        this.id = id;
    }

    public final long getDate(){
        return date;
    }

    public final void setDate(final long date){
        this.date = date;
    }

    public final String getType(){
        return type;
    }

    public final void setType(final String type){
        this.type = type;
    }

    public final String getName(){
        return name;
    }

    public final void setName(final String name){
        this.name = name;
    }

    public final String getNumber(){
        return number;
    }

    public final void setNumber(final String number){
        this.number = number;
    }

    public EventsItem(final Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<EventsItem> CREATOR = new Creator<EventsItem>() {
        public EventsItem createFromParcel(final Parcel in) {
            return new EventsItem(in);
        }

        public EventsItem[] newArray(final int size) {

            return new EventsItem[size];
        }

    };

    public void readFromParcel(final Parcel in) {
        type = in.readString();
        name = in.readString();
        number = in.readString();
        time = in.readString();
        id = in.readLong();
        date = in.readLong();
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
        color = in.readInt();
        inn = (EventType) in.readValue(EventType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(time);
        dest.writeLong(id);
        dest.writeLong(date);
        dest.writeInt(day);
        dest.writeInt(month);
        dest.writeInt(year);
        dest.writeInt(color);
        dest.writeValue(inn);
    }
}
