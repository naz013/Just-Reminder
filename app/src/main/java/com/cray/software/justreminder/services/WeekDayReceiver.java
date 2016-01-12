package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.activities.WeekDayDialog;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.json.JsonRecurrence;

import java.util.ArrayList;
import java.util.Calendar;

public class WeekDayReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra("alarmId", 0);
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);

        ArrayList<Integer> repeat = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            JsonRecurrence jsonRecurrence = new JsonParser(json).getRecurrence();
            repeat = jsonRecurrence.getWeekdays();
        }
        if (c != null) c.close();

        if (TimeCount.isDay(repeat)) {
            Intent service = new Intent(context, WeekDayReceiver.class);
            context.startService(service);
            Intent resultIntent = new Intent(context, WeekDayDialog.class);
            resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            context.startActivity(resultIntent);
            db.close();
        } else {
            Intent service = new Intent(context, WeekDayReceiver.class);
            context.startService(service);
            db.close();
        }
    }

    public void setAlarm(Context context, long id) {
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);

        Integer i = (int) (long) id;
        int hour = 0, minute = 0;
        if (c != null && c.moveToNext()) {
            hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
        }
        if (c != null) c.close();

        Intent intent = new Intent(context, WeekDayReceiver.class);
        intent.putExtra("alarmId", id);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long current = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long dbTime = calendar.getTimeInMillis();
        if (dbTime > current) alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        else alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, alarmIntent);
        db.close();
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, WeekDayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}