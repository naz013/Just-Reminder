/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.reminder.ReminderDialogActivity;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.utils.ViewUtils;

import java.io.File;
import java.util.Calendar;

public class RepeatNotificationReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        if (item != null) {
            String task = item.getSummary();
            String type = item.getType();
            JsonModel jsonModel = item.getModel();
            String melody = jsonModel.getMelody().getMelodyPath();
            int color = jsonModel.getLed().getColor();
            showNotification(context, task, type, id, color, melody);
        }
    }

    public void setAlarm(Context context, long id) {
        int repeat = SharedPrefs.getInstance(context).getInt(Prefs.NOTIFICATION_REPEAT_INTERVAL);
        int minutes = repeat * 1000 * 60;
        Intent intent = new Intent(context, RepeatNotificationReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        Integer i = (int) (long) id;
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (Module.isMarshmallow()) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + minutes, minutes, alarmIntent);
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + minutes, minutes, alarmIntent);
        }
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

    private void showNotification(Context context, String task, String type, long itemId, int color, String melody){
        Uri soundUri;
        if (melody != null && !melody.matches("")){
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (SharedPrefs.getInstance(context).getBoolean(Prefs.CUSTOM_SOUND)) {
                String path = SharedPrefs.getInstance(context).getString(Prefs.CUSTOM_SOUND_FILE);
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(task);
        builder.setAutoCancel(false);
        builder.setPriority(5);
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.SMART_FOLD)) {
            if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                Intent notificationIntent = new Intent(context, ReminderDialogActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(intent);
            }
        }
        if (Module.isPro()){
            builder.setContentText(context.getString(R.string.app_name_pro));
        } else builder.setContentText(context.getString(R.string.app_name));

        if (type != null) {
            builder.setSmallIcon(ViewUtils.getIcon(type));
        } else {
            builder.setSmallIcon(R.drawable.ic_event_white_24dp);
        }

        builder.setSound(soundUri);

        if (SharedPrefs.getInstance(context).getBoolean(Prefs.VIBRATION_STATUS)){
            long[] pattern;
            if (SharedPrefs.getInstance(context).getBoolean(Prefs.INFINITE_VIBRATION)){
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            if (SharedPrefs.getInstance(context).getBoolean(Prefs.LED_STATUS)){
                if (color != 0) {
                    builder.setLights(color, 500, 1000);
                } else {
                    builder.setLights(SharedPrefs.getInstance(context).getInt(Prefs.LED_COLOR), 500, 1000);
                }
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());
    }
}