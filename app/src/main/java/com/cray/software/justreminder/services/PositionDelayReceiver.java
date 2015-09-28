package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.Calendar;

public class PositionDelayReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GeolocationService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void setDelay(Context context, long id) {
        DataBase DB = new DataBase(context);
        DB.open();
        Cursor c = DB.getReminder(id);

        Integer i = (int) (long) id;
        int day = 0, month = 0, year = 0, hour = 0, minute = 0, seconds = 0;
        if (c != null && c.moveToNext()) {
            day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
        }
        if (c != null) c.close();
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        DB.close();
    }

    public void cancelDelay(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}