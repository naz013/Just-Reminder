package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.modules.Module;

import java.util.Calendar;

public class AutoSyncAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AutoSyncAlarm.class);
        context.startService(service);
        new SyncTask(context, null, true).execute();
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, AutoSyncAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1101, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        SharedPrefs prefs = new SharedPrefs(context);
        int interval = prefs.loadInt(Prefs.AUTO_BACKUP_INTERVAL);
        calendar.setTimeInMillis(System.currentTimeMillis() + (AlarmManager.INTERVAL_HOUR * interval));
        if (Module.isMarshmallow())
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                TimeCount.HOUR * interval, alarmIntent);
        else alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                TimeCount.HOUR * interval, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        Integer i = (int) (long) 1101;
        Intent intent = new Intent(context, AutoSyncAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}