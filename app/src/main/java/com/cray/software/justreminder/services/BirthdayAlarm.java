package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;

import java.util.Calendar;

public class BirthdayAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BirthdayAlarm.class);
        context.startService(service);
        setAlarm(context);
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
        if (Module.isMarshmallow()) alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), alarmIntent);
        else alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
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