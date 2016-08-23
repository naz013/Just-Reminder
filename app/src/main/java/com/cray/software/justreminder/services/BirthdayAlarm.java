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

import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.helpers.Module;

import java.util.Calendar;

public class BirthdayAlarm extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BirthdayAlarm.class);
        context.startService(service);
        cancelAlarm(context);
        setAlarm(context);
        Intent check = new Intent(context, CheckBirthdays.class);
        context.startService(check);
    }

    public void setAlarm(Context context){
        Intent intent1 = new Intent(context, BirthdayAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 210, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int hour = SharedPrefs.getInstance(context).getInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = SharedPrefs.getInstance(context).getInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis())
            calendar.setTimeInMillis(calendar.getTimeInMillis() + TimeCount.DAY);
        if (Module.isMarshmallow())
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        else alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, BirthdayAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(context, 210, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) alarmMgr.cancel(alarmIntent);
    }
}