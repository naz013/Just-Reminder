package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.Calendar;

public class BirthdayAlarm extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BirthdayAlarm.class);
        context.startService(service);
        Intent check = new Intent(context, CheckBirthdays.class);
        context.startService(check);
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, BirthdayAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 210, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPrefs sharedPrefs = new SharedPrefs(context);
        int hour = sharedPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = sharedPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, BirthdayAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 210, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}