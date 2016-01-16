package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.ReminderDialog;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.modules.Module;

import java.io.File;
import java.util.Calendar;

public class RepeatNotificationReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        if (id != 0) {
            NextBase db = new NextBase(context);
            db.open();
            Cursor c = db.getReminder(id);
            if (c!= null && c.moveToFirst()) {
                String task = c.getString(c.getColumnIndex(NextBase.SUMMARY));
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                JsonModel jsonModel = new JsonParser(json).parse();
                String melody = jsonModel.getMelody().getMelodyPath();
                int color = jsonModel.getLed().getColor();
                showNotification(context, task, type, id, color, melody);
            }
            if (c != null) c.close();
            db.close();
        }
    }

    public void setAlarm(Context context, long id) {
        int repeat = new SharedPrefs(context).loadInt(Prefs.NOTIFICATION_REPEAT_INTERVAL);
        int minutes = repeat * 1000 * 60;
        Intent intent = new Intent(context, RepeatNotificationReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        Integer i = (int) (long) id;
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + minutes, minutes, alarmIntent);
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, RepeatNotificationReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    private void showNotification(Context ctx, String task, String type, long itemId, int color, String melody){
        SharedPrefs sPrefs = new SharedPrefs(ctx);
        Uri soundUri;
        if (melody != null && !melody.matches("")){
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)) {
                String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
                if (path != null) {
                    File sound = new File(path);
                    soundUri = Uri.fromFile(sound);
                } else {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setContentTitle(task);
        builder.setAutoCancel(false);
        builder.setPriority(5);
        sPrefs = new SharedPrefs(ctx);
        if (sPrefs.loadBoolean(Prefs.SMART_FOLD)) {
            if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                Intent notificationIntent = new Intent(ctx, ReminderDialog.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent intent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(intent);
            }
        }
        if (Module.isPro()){
            builder.setContentText(ctx.getString(R.string.app_name_pro));
        } else builder.setContentText(ctx.getString(R.string.app_name));

        if (type != null) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL)) {
                builder.setSmallIcon(R.drawable.ic_call_white_24dp);
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                builder.setSmallIcon(R.drawable.ic_textsms_white_24dp);
            } else if (type.matches(Constants.TYPE_LOCATION)) {
                builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
            } else if (type.matches(Constants.TYPE_TIME)) {
                builder.setSmallIcon(R.drawable.ic_alarm_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_event_white_24dp);
            }
        } else {
            builder.setSmallIcon(R.drawable.ic_event_white_24dp);
        }

        builder.setSound(soundUri);

        if (sPrefs.loadBoolean(Prefs.VIBRATION_STATUS)){
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(ctx);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());
    }
}