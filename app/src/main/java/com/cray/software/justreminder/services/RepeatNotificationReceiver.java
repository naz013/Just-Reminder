package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ReminderDialog;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class RepeatNotificationReceiver extends BroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    NotificationManager mNotifyMgr;
    Notification.Builder builder;
    SharedPrefs sPrefs;
    MediaPlayer mMediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        if (id != 0) {
            DataBase db = new DataBase(context);
            db.open();
            Cursor c = db.getTask(id);
            if (c!= null && c.moveToFirst()) {
                String task = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
                int color = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
                showNotification(context, task, type, 1, id, color, melody);
            }
            if (c != null) c.close();
        }
    }

    public void setAlarm(Context context, long id) {
        int repeat = new SharedPrefs(context).loadInt(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL);
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

    private void showNotification(Context ctx, String task, String type, int i, long itemId, int color, String melody){
        sPrefs = new SharedPrefs(ctx);
        Uri soundUri;
        if (melody != null && !melody.matches("")){
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (sPrefs.loadBoolean(Constants.CUSTOM_SOUND)) {
                String path = sPrefs.loadPrefs(Constants.CUSTOM_SOUND_FILE);
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
        builder = new Notification.Builder(ctx);
        builder.setContentTitle(task);
        builder.setAutoCancel(false);
        builder.setPriority(5);
        sPrefs = new SharedPrefs(ctx);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD)) {
            if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                Intent notificationIntent = new Intent(ctx, ReminderDialog.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent intent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(intent);
            }
        }
        if (new ManageModule().isPro()){
            builder.setContentText(ctx.getString(R.string.app_name_pro));
        } else builder.setContentText(ctx.getString(R.string.app_name));

        if (type != null) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL)) {
                builder.setSmallIcon(R.drawable.ic_call_white_24dp);
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                builder.setSmallIcon(R.drawable.ic_message_white_24dp);
            } else if (type.matches(Constants.TYPE_LOCATION)) {
                builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
            } else if (type.matches(Constants.TYPE_TIME)) {
                builder.setSmallIcon(R.drawable.ic_access_time_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_event_white_24dp);
            }
        } else {
            builder.setSmallIcon(R.drawable.ic_event_white_24dp);
        }

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_VOLUME);
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

        if (i == 1) {
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SOUND_STATUS)) {
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_INFINITE_SOUND)) {
                    mMediaPlayer = new MediaPlayer();
                    try {
                        mMediaPlayer.setDataSource(ctx, soundUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_INFINITE_SOUND)) mMediaPlayer.setLooping(true);
                    else mMediaPlayer.setLooping(false);

                    mMediaPlayer.setVolume(1-log1, 1-log1);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    try {
                        mMediaPlayer.prepareAsync();
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                } else {
                    mMediaPlayer = new MediaPlayer();
                    try {
                        mMediaPlayer.setDataSource(ctx, soundUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_INFINITE_SOUND)) mMediaPlayer.setLooping(true);
                    else mMediaPlayer.setLooping(false);

                    mMediaPlayer.setVolume(1-log1, 1-log1);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    try {
                        mMediaPlayer.prepareAsync();
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            } else {
                builder.setSound(soundUri);
            }
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_VIBRATION_STATUS)){
            long[] pattern;
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (new ManageModule().isPro()){
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LED_COLOR), 500, 1000);
                }
            }
        }
        mNotifyMgr =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());
    }
}