package com.cray.software.justreminder.datas;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.enums.WidgetType;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.json.JsonRecurrence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WidgetDataProvider {

    private ArrayList<Item> data;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int hour, minute;
    private boolean isFeature;
    private Context mContext;

    public WidgetDataProvider(Context context){
        this.mContext = context;
        data = new ArrayList<>();
    }

    public void setTime(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public void setFeature(boolean isFeature){
        this.isFeature = isFeature;
    }

    public ArrayList<Item> getData(){
        return data;
    }

    public Item getItem(int position){
        return data.get(position);
    }

    public boolean hasReminder(int day, int month, int year){
        boolean res = false;
        for (Item item : data){
            if (res) break;
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            int mYear = item.getYear();
            WidgetType type = item.getType();
            res = mDay == day && mMonth == month && mYear == year && type == WidgetType.REMINDER;
        }
        return res;
    }

    public boolean hasBirthday(int day, int month){
        boolean res = false;
        for (Item item : data){
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            WidgetType type = item.getType();
            if (mDay == day && mMonth == month && type == WidgetType.BIRTHDAY) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void fillArray(){
        data.clear();
        loadBirthdays();
        loadReminders();
    }

    public void loadReminders(){
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getActiveReminders();
        if (c != null && c.moveToFirst()) {
            do {
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                String mType = c.getString(c.getColumnIndex(NextBase.TYPE));
                long eventTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));

                if (!mType.contains(Constants.TYPE_LOCATION)) {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(eventTime);
                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int mMonth = calendar1.get(Calendar.MONTH);
                    int mYear = calendar1.get(Calendar.YEAR);
                    if (eventTime > 0) {
                        data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                    }

                    JsonModel jsonModel = new JsonParser(json).parse();
                    JsonRecurrence jsonRecurrence = jsonModel.getRecurrence();
                    long repeatTime = jsonRecurrence.getRepeat();
                    long limit = jsonRecurrence.getLimit();
                    long count = jsonModel.getCount();
                    int myDay = jsonRecurrence.getMonthday();
                    boolean isLimited = limit > 0;

                    if (isFeature) {
                        if (mType.startsWith(Constants.TYPE_WEEKDAY)) {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            ArrayList<Integer> list = jsonRecurrence.getWeekdays();
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() +
                                        AlarmManager.INTERVAL_DAY);
                                eventTime = calendar1.getTimeInMillis();
                                int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                if (list.get(weekDay - 1) == 1 && eventTime > 0) {
                                    int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    int sMonth = calendar1.get(Calendar.MONTH);
                                    int sYear = calendar1.get(Calendar.YEAR);
                                    days++;
                                    data.add(new Item(sDay, sMonth, sYear, WidgetType.REMINDER));
                                }
                            } while (days < max);
                        } else if (mType.startsWith(Constants.TYPE_MONTHDAY)) {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            do {
                                eventTime = TimeCount.getNextMonthDayTime(myDay,
                                        calendar1.getTimeInMillis() + TimeCount.DAY);
                                calendar1.setTimeInMillis(eventTime);
                                int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int sMonth = calendar1.get(Calendar.MONTH);
                                int sYear = calendar1.get(Calendar.YEAR);
                                if (eventTime > 0) {
                                    days++;
                                    data.add(new Item(sDay, sMonth, sYear, WidgetType.REMINDER));
                                }
                            } while (days < max);
                        } else {
                            long days = 0;
                            long max = Configs.MAX_DAYS_COUNT;
                            if (isLimited) max = limit - count;
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() + repeatTime);
                                eventTime = calendar1.getTimeInMillis();
                                mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                mMonth = calendar1.get(Calendar.MONTH);
                                mYear = calendar1.get(Calendar.YEAR);
                                if (eventTime > 0) {
                                    days++;
                                    data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                                }
                            } while (days < max);
                        }
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    public void loadBirthdays(){
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getBirthdays();
        if (c != null && c.moveToFirst()){
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
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
                    calendar1.setTimeInMillis(System.currentTimeMillis());
                    calendar1.set(Calendar.MONTH, bMonth);
                    calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                    calendar1.set(Calendar.HOUR_OF_DAY, hour);
                    calendar1.set(Calendar.MINUTE, minute);
                    data.add(new Item(bDay, bMonth, 0, WidgetType.BIRTHDAY));
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    public class Item {
        int day, month, year;
        WidgetType type;

        public Item(int day, int month, int year, WidgetType type){
            this.day = day;
            this.month = month;
            this.year = year;
            this.type = type;
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

        public WidgetType getType(){
            return type;
        }
    }
}
