package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cray.software.justreminder.dialogs.MissedCallDialog;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

public class MissedCallAlarm extends BroadcastReceiver {

    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        String number = intent.getStringExtra("number");
        long time = intent.getLongExtra("time", 0);
        Intent service = new Intent(context, MissedCallAlarm.class);
        context.startService(service);
        Intent resultIntent = new Intent(context, MissedCallDialog.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.putExtra("number", number);
        resultIntent.putExtra("time", time);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(resultIntent);
    }

    public void setAlarm(Context context, long id, String number, long timeStamp) {
        Intent intent = new Intent(context, MissedCallAlarm.class);
        intent.putExtra("number", number);
        intent.putExtra("time", timeStamp);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        SharedPrefs prefs = new SharedPrefs(context);
        int time = prefs.loadInt(Prefs.MISSED_CALL_TIME);
        long mills = 1000 * 60;
        Integer i = (int) (long) id;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (time * mills), time * mills, alarmIntent);
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, MissedCallAlarm.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}