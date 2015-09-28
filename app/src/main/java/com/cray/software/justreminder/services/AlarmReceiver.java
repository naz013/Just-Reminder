package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ReminderDialog;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        Intent service = new Intent(context, AlarmReceiver.class);
        context.startService(service);
        Intent resultIntent = new Intent(context, ReminderDialog.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(resultIntent);
    }

    public void setAlarm(Context context, long id) {
        DataBase DB = new DataBase(context);
        DB.open();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        Cursor c = DB.getReminder(id);
        Integer i = (int) (long) id;
        int min = 60 * 1000;
        int day = 0, month = 0, year = 0, hour = 0, minute = 0, seconds = 0, repeatTime = 0;
        long inTime = 0;
        long remCount = 0;
        if (c != null && c.moveToNext()) {
            day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            inTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            repeatTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            remCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
        }
        if (c != null) c.close();
        //Log.d(Constants.LOG_TAG, "----------alarm send values " + id + " : " + task + " : " + phoneNumber);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        if (inTime != 0) {
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);
        if (inTime == 0) {
            calendar.set(Calendar.MILLISECOND, 0);
        }
        if (inTime > 0){
            if (repeatTime > 0){
                if (remCount > 0){
                            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + inTime +
                            (min * remCount * repeatTime), min * repeatTime, alarmIntent);
                } else {
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() +
                            inTime, min * repeatTime, alarmIntent);
                }
            } else {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + inTime, alarmIntent);
            }
        } else {
            if (repeatTime > 0){
                if (remCount > 0){
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() +
                            (AlarmManager.INTERVAL_DAY * remCount * repeatTime), AlarmManager.INTERVAL_DAY * repeatTime, alarmIntent);
                } else {
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY * repeatTime, alarmIntent);
                }
            } else {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            }
        }
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}