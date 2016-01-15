package com.cray.software.justreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.activities.ReminderDialog;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.enums.NewMethod;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonParser;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.Type;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class AlarmReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        Intent service = new Intent(context, AlarmReceiver.class);
        context.startService(service);
        JsonModel reminder = new Type(context).getItem(id);
        if (reminder.getType().matches(Constants.TYPE_TIME)){
            String exclusion = reminder.getExclusion().toString();
            if (exclusion != null){
                Recurrence helper = new Recurrence(exclusion);
                if (!helper.isRange()){
                    start(context, id);
                } else {
                    Reminder.update(context, id);
                    new UpdatesHelper(context).updateWidget();
                }
            } else {
                start(context, id);
            }
        } else {
            start(context, id);
        }
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
        if (c != null && c.moveToNext()) {
            due = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
            String json = c.getString(c.getColumnIndex(NextBase.JSON));
            repeat = new JsonParser(json).getRecurrence().getRepeat();
        }
        if (c != null) c.close();
        db.close();

        if (due == 0) return;

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (repeat > 0) {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, due, repeat, alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, due, alarmIntent);
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