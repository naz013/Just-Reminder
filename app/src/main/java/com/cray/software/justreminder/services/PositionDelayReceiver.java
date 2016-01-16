package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.NextBase;

public class PositionDelayReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GeolocationService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void setDelay(Context context, long id) {
        NextBase db = new NextBase(context);
        db.open();
        Cursor c = db.getReminder(id);

        Integer i = (int) (long) id;
        long startTime = 0;
        if (c != null && c.moveToNext()) {
            startTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
        }
        if (c != null) c.close();
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmMgr.set(AlarmManager.RTC_WAKEUP, startTime, alarmIntent);
        db.close();
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