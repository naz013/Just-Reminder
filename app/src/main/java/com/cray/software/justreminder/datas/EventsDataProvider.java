package com.cray.software.justreminder.datas;

import android.app.AlarmManager;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.ReminderUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventsDataProvider {
    public enum Type {
        birthday,
        reminder
    }

    private ArrayList<EventsItem> data = new ArrayList<>();
    private Cursor s, c;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    int hour, minute;
    boolean isFeature;

    public EventsDataProvider(){
        data = new ArrayList<>();
    }

    public void setBirthdays(Cursor c){
        this.c = c;
    }

    public void setReminders(Cursor s){
        this.s = s;
    }

    public void setTime(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public void setFeature(boolean isFeature){
        this.isFeature = isFeature;
    }

    public ArrayList<EventsItem> getData(){
        return data;
    }

    public EventsItem getItem(int position){
        return data.get(position);
    }

    public void deleteItem(int position){
        data.remove(position);
    }

    public ArrayList<EventsItem> getMatches(int day, int month, int year){
        ArrayList<EventsItem> res = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (EventsItem item : data){
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            int mYear = item.getYear();
            Type type = item.getInn();
            if (type == Type.birthday && mDay == day && mMonth == month){
                res.add(item);
            } else {
                if (mDay == day && mMonth == month && mYear == year) res.add(item);
            }
        }
        long diff = System.currentTimeMillis() - start;
        Log.d(Constants.LOG_TAG, "Search time " + diff);
        return res;
    }

    public long getStartTime(){
        long time = System.currentTimeMillis();
        for (EventsItem item : data) {
            long value = item.getDate();
            if (value < time) time = value;
        }
        return time;
    }

    public void fillArray(){
        loadBirthdays();
        loadReminders();
    }

    public void loadBirthdays(){
        if (c != null && c.moveToFirst()){
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                String name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                long id = c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID));
                String number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
                Date date1 = null;
                try {
                    date1 = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date1 != null) {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTime(date1);
                    int bDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int bMonth = calendar1.get(Calendar.MONTH);
                    int bYear = calendar1.get(Calendar.YEAR);
                    calendar1.setTimeInMillis(System.currentTimeMillis());
                    calendar1.set(Calendar.MONTH, bMonth);
                    calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                    calendar1.set(Calendar.HOUR_OF_DAY, hour);
                    calendar1.set(Calendar.MINUTE, minute);
                    data.add(new EventsItem("birthday", name, number, id, calendar1.getTimeInMillis(),
                            bDay, bMonth, bYear, Type.birthday));
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
    }

    public void loadReminders(){
        long start = System.currentTimeMillis();
        if (s != null && s.moveToFirst()) {
            do {
                int myHour = s.getInt(s.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = s.getInt(s.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = s.getInt(s.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = s.getInt(s.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = s.getInt(s.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = s.getInt(s.getColumnIndex(Constants.COLUMN_REPEAT));
                int remCount = s.getInt(s.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int isDone = s.getInt(s.getColumnIndex(Constants.COLUMN_IS_DONE));
                long afterTime = s.getInt(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String mType = s.getString(s.getColumnIndex(Constants.COLUMN_TYPE));
                String name = s.getString(s.getColumnIndex(Constants.COLUMN_TEXT));
                String number = s.getString(s.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = s.getString(s.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                long id = s.getLong(s.getColumnIndex(Constants.COLUMN_ID));
                if ((mType.startsWith(Constants.TYPE_SKYPE) ||
                        mType.matches(Constants.TYPE_CALL) ||
                        mType.startsWith(Constants.TYPE_APPLICATION) ||
                        mType.matches(Constants.TYPE_MESSAGE) ||
                        mType.matches(Constants.TYPE_REMINDER) ||
                        mType.matches(Constants.TYPE_TIME)) && isDone == 0) {
                    long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                            afterTime, repCode, remCount, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(time);
                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int mMonth = calendar1.get(Calendar.MONTH);
                    int mYear = calendar1.get(Calendar.YEAR);
                    if (time > 0) {
                        if (number == null) number = "0";
                        data.add(new EventsItem("reminder", name, number, id, time, mDay,
                                mMonth, mYear, Type.reminder));
                    }
                    if (!mType.matches(Constants.TYPE_TIME) && isFeature && repCode > 0) {
                        int days = 0;
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + (repCode *
                                    AlarmManager.INTERVAL_DAY));
                            time = calendar1.getTimeInMillis();
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            mMonth = calendar1.get(Calendar.MONTH);
                            mYear = calendar1.get(Calendar.YEAR);
                            days = days + repCode;
                            if (time > 0) {
                                if (number == null) number = "0";
                                data.add(new EventsItem("reminder", name, number, id, time, mDay,
                                        mMonth, mYear, Type.reminder));
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                    long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(time);
                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int mMonth = calendar1.get(Calendar.MONTH);
                    int mYear = calendar1.get(Calendar.YEAR);
                    if (time > 0) {
                        if (number == null) number = "0";
                        data.add(new EventsItem("reminder", name, number, id, time, mDay,
                                mMonth, mYear, Type.reminder));
                    }
                    int days = 0;
                    if (isFeature) {
                        ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                            time = calendar1.getTimeInMillis();
                            int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                            days = days + 1;
                            if (list.get(weekDay - 1) == 1) {
                                int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int sMonth = calendar1.get(Calendar.MONTH);
                                int sYear = calendar1.get(Calendar.YEAR);
                                if (time > 0) {
                                    if (number == null) number = "0";
                                    data.add(new EventsItem("reminder", name, number, id, time, sDay,
                                            sMonth, sYear, Type.reminder));
                                }
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (mType.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0) {
                    long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    if (time > 0) {
                        calendar1.setTimeInMillis(time);
                        int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int mMonth = calendar1.get(Calendar.MONTH);
                        int mYear = calendar1.get(Calendar.YEAR);
                        if (time > 0) {
                            if (number == null) number = "0";
                            data.add(new EventsItem("reminder", name, number, id, time, mDay,
                                    mMonth, mYear, Type.reminder));
                        }
                    }
                    int days = 1;
                    if (isFeature) {
                        do {
                            time = TimeCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
                            days = days + 1;
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (time > 0) {
                                if (number == null) number = "0";
                                data.add(new EventsItem("reminder", name, number, id, time, mDay,
                                        mMonth, mYear, Type.reminder));
                            }
                        } while (days < Configs.MAX_MONTH_COUNT);
                    }
                }
            } while (s.moveToNext());
        }
        long diff = System.currentTimeMillis() - start;
        Log.d(Constants.LOG_TAG, "Calculate time " + diff);
    }

    public class EventsItem implements Parcelable{
        private String type, name, number, time;
        private long id, date;
        private int day, month, year;
        private Type inn;

        public EventsItem(String type, String name, String number, long id, long date, int day,
                          int month, int year, Type inn){
            this.type = type;
            this.name = name;
            this.id = id;
            this.date = date;
            this.number = number;
            this.day = day;
            this.month = month;
            this.year = year;
            this.inn = inn;
        }

        public Type getInn(){
            return inn;
        }

        public void setInn(Type inn){
            this.inn = inn;
        }

        public int getYear(){
            return year;
        }

        public void setYear(int year){
            this.year = year;
        }

        public int getMonth(){
            return month;
        }

        public void setMonth(int month){
            this.month = month;
        }

        public int getDay(){
            return day;
        }

        public void setDay(int day){
            this.day = day;
        }

        public String getTime(){
            return time;
        }

        public void setTime(String time){
            this.time = time;
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

        public EventsItem(Parcel in) {
            super();
            readFromParcel(in);
        }

        public final Parcelable.Creator<EventsItem> CREATOR = new Parcelable.Creator<EventsItem>() {
            public EventsItem createFromParcel(Parcel in) {
                return new EventsItem(in);
            }

            public EventsItem[] newArray(int size) {

                return new EventsItem[size];
            }

        };

        public void readFromParcel(Parcel in) {
            type = in.readString();
            name = in.readString();
            number = in.readString();
            id = in.readLong();
            date = in.readLong();
            day = in.readInt();
            month = in.readInt();
            year = in.readInt();
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
            dest.writeInt(day);
            dest.writeInt(month);
            dest.writeInt(year);
        }
    }
}
