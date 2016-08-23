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
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cray.software.justreminder.reminder.ReminderDialogActivity;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Module;

import java.util.Calendar;

public class DelayReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        Intent service = new Intent(context, DelayReceiver.class);
        context.startService(service);
        Intent resultIntent = new Intent(context, ReminderDialogActivity.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(resultIntent);
    }

    public void setAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        int min = 60 * 1000;
        Intent intent = new Intent(context, DelayReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        int inTime = SharedPrefs.getInstance(context).getInt(Prefs.DELAY_TIME);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (Module.isMarshmallow())
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis() + (min * inTime), alarmIntent);
        else alarmMgr.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis() + (min * inTime), alarmIntent);
    }

    public void setAlarm(Context context, long id, int time) {
        Integer i = (int) (long) id;
        int min = 60 * 1000;
        Intent intent = new Intent(context, DelayReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (Module.isMarshmallow())
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis() + (min * time), alarmIntent);
        else alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + (min * time), alarmIntent);
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, DelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}