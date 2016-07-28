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
import android.util.Log;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.enums.NewMethod;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDialog;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.utils.TimeUtil;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("----ON_RECEIVE-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        Intent service = new Intent(context, AlarmReceiver.class);
        context.startService(service);
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        String exclusion = item.getModel().getExclusion().toString();
        String type = item.getType();
        if (type.matches(Constants.TYPE_TIME)) {
            if (exclusion != null) {
                Recurrence helper = new Recurrence(exclusion);
                if (!helper.isRange()) start(context, id);
                else {
                    Reminder.update(context, id);
                    UpdatesHelper.getInstance(context).updateWidget();
                }
            } else start(context, id);
        } else start(context, id);
    }

    private void start(Context context, long id) {
        Intent resultIntent = new Intent(context, ReminderDialog.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(resultIntent);
    }

    @NewMethod
    public void enableReminder(Context context, long id) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        ReminderItem item = ReminderHelper.getInstance(context).getReminder(id);
        Integer i = (int) (long) id;
        long due = 0;
        if (item != null) {
            due = item.getDateTime();
            /*if (due < System.currentTimeMillis()) {
                if (type != null) {
                    if (type.startsWith(Constants.TYPE_WEEKDAY) ||
                            type.startsWith(Constants.TYPE_MONTHDAY)) {
                        due = new TimeCount(context).generateDateTime(type,
                                jRecurrence.getMonthday(), System.currentTimeMillis(), repeat,
                                jRecurrence.getWeekdays(), jModel.getCount(), 0);
                        jModel.setEventTime(due);
                        db.updateReminderTime(id, due);
                        db.updateCount(id, jModel.toJsonString());
                    }
                }
            }*/
        }
        if (due == 0) return;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Module.isMarshmallow()) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, due, alarmIntent);
        }
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) alarmMgr.cancel(alarmIntent);
    }
}