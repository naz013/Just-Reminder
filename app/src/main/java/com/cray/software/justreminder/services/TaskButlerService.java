package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.constants.Constants;

public class TaskButlerService extends IntentService {

    public TaskButlerService() {
        super("TaskButlerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //get all active reminders
        AlarmReceiver alarm = new AlarmReceiver();
        WeekDayReceiver weekDayReceiver = new WeekDayReceiver();
        DataBase db = new DataBase(getApplicationContext());
        db.open();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                if (isDone != 1) {
                    if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                        weekDayReceiver.setAlarm(getApplicationContext(), rowId);
                    } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                        new MonthDayReceiver().setAlarm(getApplicationContext(), rowId);
                    } else if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
                        if (day > 0){
                            alarm.setAlarm(getApplicationContext(), rowId);
                        }
                    } else if (!type.matches(Constants.TYPE_SHOPPING_LIST)) {
                        alarm.setAlarm(getApplicationContext(), rowId);
                    } else {
                        alarm.setAlarm(getApplicationContext(), rowId);
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        stopSelf();
    }
}