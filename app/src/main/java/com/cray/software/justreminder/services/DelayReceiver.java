package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.dialogs.ReminderDialog;
import com.cray.software.justreminder.dialogs.WeekDayDialog;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.Calendar;

public class DelayReceiver extends BroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    SharedPrefs sPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        int window = intent.getIntExtra(Constants.WINDOW_INTENT, 0);
        Intent service = new Intent(context, DelayReceiver.class);
        context.startService(service);
        Intent resultIntent;
        if (window == 1) resultIntent = new Intent(context, ReminderDialog.class);
        else resultIntent = new Intent(context, WeekDayDialog.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(resultIntent);
    }

    public void setAlarm(Context context, int window, long id) {
        Integer i = (int) (long) id;
        int min = 60 * 1000;
        Intent intent = new Intent(context, DelayReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        intent.putExtra(Constants.WINDOW_INTENT, window);
        sPrefs = new SharedPrefs(context);
        int inTime = sPrefs.loadInt(Prefs.DELAY_TIME);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + (min * inTime), alarmIntent);
    }

    public void setAlarm(Context context, int window, long id, int time) {
        Integer i = (int) (long) id;
        int min = 60 * 1000;
        Intent intent = new Intent(context, DelayReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        intent.putExtra(Constants.WINDOW_INTENT, window);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + (min * time), alarmIntent);
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