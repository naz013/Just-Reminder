package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Constants;

public class TaskButlerService extends IntentService {
    AlarmReceiver alarm = new AlarmReceiver();
    WeekDayReceiver weekDayReceiver = new WeekDayReceiver();
    DataBase DB;

    public TaskButlerService() {
        super("TaskButlerService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //get all active reminders
        new Thread(new Runnable() {
            @Override
            public void run() {
                DB = new DataBase(getApplicationContext());
                DB.open();
                Cursor c = DB.queryGroup();
                if (c != null && c.moveToFirst()){
                    do {
                        long rowId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                        String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                        int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                        if (isDone != 1) {
                            if (type.startsWith(Constants.TYPE_WEEKDAY))
                                weekDayReceiver.setAlarm(getApplicationContext(), rowId);
                            else if (!type.startsWith(Constants.TYPE_LOCATION))
                                alarm.setAlarm(getApplicationContext(), rowId);
                        }
                    } while (c.moveToNext());
                } else {
                    getApplicationContext().stopService(new Intent(getApplicationContext(), TaskButlerService.class));
                }
                if (c != null) c.close();
                DB.close();
            }
        }).start();
    }
}