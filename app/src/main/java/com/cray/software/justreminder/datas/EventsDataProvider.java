package com.cray.software.justreminder.datas;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.reminder.ReminderUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventsDataProvider {

    private ArrayList<EventsItem> data = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int hour, minute;
    private boolean isFeature;
    private boolean isBirthdays;
    private boolean isReminders;
    private Context mContext;

    public EventsDataProvider(Context mContext){
        this.mContext = mContext;
        data = new ArrayList<>();
    }

    public void setBirthdays(boolean isBirthdays){
        this.isBirthdays = isBirthdays;
    }

    public void setReminders(boolean isReminders){
        this.isReminders = isReminders;
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

    public ArrayList<EventsItem> getMatches(int day, int month, int year){
        ArrayList<EventsItem> res = new ArrayList<>();
        for (EventsItem item : data){
            int mDay = item.getDay();
            int mMonth = item.getMonth();
            int mYear = item.getYear();
            EventType type = item.getInn();
            if (type == EventType.birthday && mDay == day && mMonth == month){
                res.add(item);
            } else {
                if (mDay == day && mMonth == month && mYear == year) res.add(item);
            }
        }
        return res;
    }

    public void fillArray(){
        if (isBirthdays) loadBirthdays();
        if (isReminders) loadReminders();
    }

    public void loadBirthdays(){
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getBirthdays();
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
                            bDay, bMonth, bYear, EventType.birthday, 0));
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    public void loadReminders(){
        long start = System.currentTimeMillis();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor cat = db.queryCategories();
        Map<String, Integer> map = new HashMap<>();
        if (cat != null && cat.moveToFirst()){
            do {
                String uuid = cat.getString(cat.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int color = cat.getInt(cat.getColumnIndex(Constants.COLUMN_COLOR));
                map.put(uuid, color);
            } while (cat.moveToNext());
        }
        if (cat != null) cat.close();
        Cursor s = db.getActiveReminders();
        if (s != null && s.moveToFirst()) {
            do {
                int myHour = s.getInt(s.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = s.getInt(s.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = s.getInt(s.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = s.getInt(s.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = s.getInt(s.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = s.getInt(s.getColumnIndex(Constants.COLUMN_REPEAT));
                String category = s.getString(s.getColumnIndex(Constants.COLUMN_CATEGORY));
                long remCount = s.getLong(s.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int isDone = s.getInt(s.getColumnIndex(Constants.COLUMN_IS_DONE));
                long afterTime = s.getInt(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String mType = s.getString(s.getColumnIndex(Constants.COLUMN_TYPE));
                String name = s.getString(s.getColumnIndex(Constants.COLUMN_TEXT));
                String number = s.getString(s.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = s.getString(s.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                long id = s.getLong(s.getColumnIndex(Constants.COLUMN_ID));

                int color = 0;
                if (map.containsKey(category)) color = map.get(category);

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
                                mMonth, mYear, EventType.reminder, color));
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
                                        mMonth, mYear, EventType.reminder, color));
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
                                mMonth, mYear, EventType.reminder, color));
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
                                            sMonth, sYear, EventType.reminder, color));
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
                                    mMonth, mYear, EventType.reminder, color));
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
                                        mMonth, mYear, EventType.reminder, color));
                            }
                        } while (days < Configs.MAX_MONTH_COUNT);
                    }
                }
            } while (s.moveToNext());
        }
        if (s != null) s.close();
        db.close();
        long diff = System.currentTimeMillis() - start;
        Log.d(Constants.LOG_TAG, "Calculate time " + diff);
    }
}
