package com.cray.software.justreminder.datas;

import android.app.AlarmManager;
import android.database.Cursor;

import com.cray.software.justreminder.enums.WidgetType;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.reminder.ReminderUtils;

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
    private Cursor c, s;

    public WidgetDataProvider(){
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

    public void setBirthdays(Cursor c){
        this.c = c;
    }

    public void setReminders(Cursor s){
        this.s = s;
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
        if (s != null && s.moveToFirst()) {
            do {
                int myHour = s.getInt(s.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = s.getInt(s.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = s.getInt(s.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = s.getInt(s.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = s.getInt(s.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = s.getInt(s.getColumnIndex(Constants.COLUMN_REPEAT));
                int remCount = s.getInt(s.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long afterTime = s.getLong(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String mType = s.getString(s.getColumnIndex(Constants.COLUMN_TYPE));
                String weekdays = s.getString(s.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                if ((mType.startsWith(Constants.TYPE_SKYPE) ||
                        mType.matches(Constants.TYPE_CALL) ||
                        mType.startsWith(Constants.TYPE_APPLICATION) ||
                        mType.matches(Constants.TYPE_MESSAGE) ||
                        mType.matches(Constants.TYPE_REMINDER) ||
                        mType.matches(Constants.TYPE_TIME))) {
                    long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                            afterTime, repCode, remCount, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(time);
                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int mMonth = calendar1.get(Calendar.MONTH);
                    int mYear = calendar1.get(Calendar.YEAR);
                    if (time > 0) {
                        data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
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
                            days += repCode;
                            if (time > 0) {
                                data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (mType.startsWith(Constants.TYPE_WEEKDAY)) {
                    long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(time);
                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                    int mMonth = calendar1.get(Calendar.MONTH);
                    int mYear = calendar1.get(Calendar.YEAR);
                    if (time > 0) {
                        data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                    }
                    int days = 0;
                    if (isFeature) {
                        ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() +
                                    AlarmManager.INTERVAL_DAY);
                            time = calendar1.getTimeInMillis();
                            int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                            days += 1;
                            if (list.get(weekDay - 1) == 1) {
                                int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int sMonth = calendar1.get(Calendar.MONTH);
                                int sYear = calendar1.get(Calendar.YEAR);
                                if (time > 0) {
                                    data.add(new Item(sDay, sMonth, sYear, WidgetType.REMINDER));
                                }
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (mType.startsWith(Constants.TYPE_MONTHDAY)){
                    long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    if (time > 0) {
                        calendar1.setTimeInMillis(time);
                        int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int mMonth = calendar1.get(Calendar.MONTH);
                        int mYear = calendar1.get(Calendar.YEAR);
                        if (time > 0) {
                            data.add(new Item(mDay, mMonth, mYear, WidgetType.REMINDER));
                        }
                    }
                    int days = 1;
                    if (isFeature){
                        do {
                            time = TimeCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
                            days += 1;
                            calendar1.setTimeInMillis(time);
                            int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int sMonth = calendar1.get(Calendar.MONTH);
                            int sYear = calendar1.get(Calendar.YEAR);
                            if (time > 0) {
                                data.add(new Item(sDay, sMonth, sYear, WidgetType.REMINDER));
                            }
                        } while (days < Configs.MAX_MONTH_COUNT);
                    }
                }
            } while (s.moveToNext());
        }
        if (s != null) s.close();
    }

    public void loadBirthdays(){
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
