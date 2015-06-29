package com.cray.software.justreminder.async;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class CacheAsync extends AsyncTask<Void, Void, Void> {
    Context ctx;
    String taskType;
    String uuId;
    long localId;

    public static final String DELETE_CACHES = "delete_caches";
    public static final String DELETE_ALL_CACHES = "delete_all_caches";
    public static final String RE_GENERATE = "re_generate";
    public static final String GENERATE = "generate";

    public CacheAsync(Context context, String taskType, String uuId, long localId){
        this.ctx = context;
        this.taskType = taskType;
        this.uuId = uuId;
        this.localId = localId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBase data = new DataBase(ctx);
        data.open();
        if (taskType.matches(DELETE_CACHES)){
            //delete caches
            Cursor c = data.getCache(uuId);
            if (c != null && c.moveToFirst()){
                do {
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    data.deleteCache(id);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }
        if (taskType.matches(DELETE_ALL_CACHES)){
            //delete caches
            Cursor c = data.queryAllCache();
            if (c != null && c.moveToFirst()){
                do {
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    data.deleteCache(id);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }
        if (taskType.matches(RE_GENERATE)){
            //update caches
            Cursor c = data.getCache(localId);
            if (c != null && c.moveToFirst()){
                do {
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    data.deleteCache(id);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
            c = data.getReminder(localId);
            if (c != null && c.moveToFirst()){
                int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                int remCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long afterTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String mType = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String name = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
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
                    if (time > 0) {
                        if (number == null) number = "0";
                        data.insertCache(id, name, mType, myDay, myMonth, myYear, number, uuId,
                                weekdays, categoryId, time);
                    }
                    if (!mType.matches(Constants.TYPE_TIME) && repCode > 0) {
                        int days = 0;
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + (repCode *
                                    AlarmManager.INTERVAL_DAY));
                            time = calendar1.getTimeInMillis();
                            days = days + repCode;
                            if (time > 0) {
                                if (number == null) number = "0";
                                data.insertCache(id, name, mType, myDay, myMonth, myYear, number, uuId,
                                        weekdays, categoryId, time);
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                    long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.setTimeInMillis(time);
                    if (time > 0) {
                        if (number == null) number = "0";
                        data.insertCache(id, name, mType, myDay, myMonth, myYear, number, uuId,
                                weekdays, categoryId, time);
                    }
                    int days = 0;
                    ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                    do {
                        calendar1.setTimeInMillis(calendar1.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                        time = calendar1.getTimeInMillis();
                        int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                        days = days + 1;
                        if (list.get(weekDay - 1) == 1) {
                            if (time > 0) {
                                if (number == null) number = "0";
                                data.insertCache(id, name, mType, myDay, myMonth, myYear, number, uuId,
                                        weekdays, categoryId, time);
                            }
                        }
                    } while (days < Configs.MAX_DAYS_COUNT);
                } else if (mType.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0){
                    long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                    Calendar calendar1 = Calendar.getInstance();
                    if (time > 0) {
                        calendar1.setTimeInMillis(time);
                        if (time > 0) {
                            if (number == null) number = "0";
                            data.insertCache(id, name, mType, myDay, myMonth, myYear, number, uuId,
                                    weekdays, categoryId, time);
                        }
                    }
                    int days = 1;
                    do {
                        time = TimeCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
                        days = days + 1;
                        calendar1.setTimeInMillis(time);
                        if (time > 0) {
                            if (number == null) number = "0";
                            data.insertCache(id, name, mType, myDay, myMonth, myYear, number, uuId,
                                    weekdays, categoryId, time);
                        }
                    } while (days < Configs.MAX_MONTH_COUNT);
                }
            }
        }
        if (taskType.matches(GENERATE)){
            //update caches
            Cursor c = data.queryAllReminders();
            if (c != null && c.moveToFirst()){
                do {
                    int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                    int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                    int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                    int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                    int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                    int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                    int remCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                    long afterTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                    String mType = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                    String name = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                    String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                    String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                    String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                    int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    if ((mType.startsWith(Constants.TYPE_SKYPE) ||
                            mType.matches(Constants.TYPE_CALL) ||
                            mType.startsWith(Constants.TYPE_APPLICATION) ||
                            mType.matches(Constants.TYPE_MESSAGE) ||
                            mType.matches(Constants.TYPE_REMINDER) ||
                            mType.matches(Constants.TYPE_TIME)) && isDone == 0) {
                        long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                                afterTime, repCode, remCount, 0);
                        Calendar calendar1 = Calendar.getInstance();
                        if (time > 0) {
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (number == null) number = "0";
                            data.insertCache(id, name, mType, mDay, mMonth, mYear, number, uuId,
                                    weekdays, categoryId, time);
                        }
                        if (!mType.matches(Constants.TYPE_TIME) && repCode > 0) {
                            int days = 0;
                            do {
                                calendar1.setTimeInMillis(calendar1.getTimeInMillis() + (repCode *
                                        AlarmManager.INTERVAL_DAY));
                                time = calendar1.getTimeInMillis();
                                days = days + repCode;
                                if (time > 0) {
                                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    int mMonth = calendar1.get(Calendar.MONTH);
                                    int mYear = calendar1.get(Calendar.YEAR);
                                    if (number == null) number = "0";
                                    data.insertCache(id, name, mType, mDay, mMonth, mYear, number, uuId,
                                            weekdays, categoryId, time);
                                }
                            } while (days < Configs.MAX_DAYS_COUNT);
                        }
                    } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                        long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                        Calendar calendar1 = Calendar.getInstance();
                        if (time > 0) {
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (number == null) number = "0";
                            data.insertCache(id, name, mType, mDay, mMonth, mYear, number, uuId,
                                    weekdays, categoryId, time);
                        }
                        int days = 0;
                        ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                        do {
                            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                            time = calendar1.getTimeInMillis();
                            int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                            days = days + 1;
                            if (list.get(weekDay - 1) == 1) {
                                if (time > 0) {
                                    int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    int mMonth = calendar1.get(Calendar.MONTH);
                                    int mYear = calendar1.get(Calendar.YEAR);
                                    if (number == null) number = "0";
                                    data.insertCache(id, name, mType, mDay, mMonth, mYear, number, uuId,
                                            weekdays, categoryId, time);
                                }
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
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
                                data.insertCache(id, name, mType, mDay, mMonth, mYear, number, uuId,
                                        weekdays, categoryId, time);
                            }
                        }
                        int days = 1;
                        do {
                            time = TimeCount.getNextMonthDayTime(myDay, calendar1.getTimeInMillis(), days);
                            days = days + 1;
                            calendar1.setTimeInMillis(time);
                            if (time > 0) {
                                int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                int mMonth = calendar1.get(Calendar.MONTH);
                                int mYear = calendar1.get(Calendar.YEAR);
                                if (number == null) number = "0";
                                data.insertCache(id, name, mType, mDay, mMonth, mYear, number, uuId,
                                        weekdays, categoryId, time);
                            }
                        } while (days < Configs.MAX_MONTH_COUNT);
                    }
                } while (c.moveToNext());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(ctx).updateCalendarWidget();
    }
}
