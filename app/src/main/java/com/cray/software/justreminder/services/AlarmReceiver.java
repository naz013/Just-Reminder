package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.cray.software.justreminder.activities.ReminderDialog;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.enums.NewMethod;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.Type;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

import java.util.ArrayList;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("----ON_RECEIVE-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        int code = intent.getIntExtra(Constants.ITEM_CODE_INTENT, 0);
        Intent service = new Intent(context, AlarmReceiver.class);
        context.startService(service);
        if (code == 2) {
            JModel reminder = new Type(context).getItem(id);
            String exclusion = reminder.getExclusion().toString();
            if (exclusion != null){
                Recurrence helper = new Recurrence(exclusion);
                if (!helper.isRange()) start(context, id);
                else {
                    Reminder.update(context, id);
                    new UpdatesHelper(context).updateWidget();
                }
            } else start(context, id);
        } else if (code == 1) {
            JRecurrence reminder = new Type(context).getItem(id).getRecurrence();
            ArrayList<Integer> weekdays = reminder.getWeekdays();
            if (weekdays != null && weekdays.size() > 0) {
                if (TimeCount.isDay(weekdays)) start(context, id);
            } else {
                int day = reminder.getMonthday();
                if (TimeCount.isDay(day)) start(context, id);
            }
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
        NextBase db = new NextBase(context);
        db.open();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ITEM_ID_INTENT, id);
        Cursor c = db.getReminder(id);
        Integer i = (int) (long) id;
        long due = 0;
        long repeat = 0;
        int code = 0;
        if (c != null && c.moveToNext()) {
            due = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            String type = c.getString(c.getColumnIndex(NextBase.TYPE));
            repeat = new JParser(json).getRecurrence().getRepeat();
            if (type.matches(Constants.TYPE_TIME))
                code = 2;

            if (type.contains(Constants.TYPE_WEEKDAY) || type.contains(Constants.TYPE_MONTHDAY))
                code = 1;
        }
        if (c != null) c.close();
        db.close();

        if (due == 0) return;
        intent.putExtra(Constants.ITEM_CODE_INTENT, code);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (code == 1) {
            if (Module.isMarshmallow()) {
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, due, AlarmManager.INTERVAL_DAY, alarmIntent);
            } else {
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, due, AlarmManager.INTERVAL_DAY, alarmIntent);
            }
        } else {
            if (repeat > 0) {
                if (Module.isMarshmallow()) {
                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, due, repeat, alarmIntent);
                } else {
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, due, repeat, alarmIntent);
                }
            } else {
                if (Module.isMarshmallow()) {
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, alarmIntent);
                } else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, due, alarmIntent);
                }
            }
        }
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}