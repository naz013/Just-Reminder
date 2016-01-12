package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.databases.NextBase;

public class TaskButlerService extends IntentService {

    public TaskButlerService() {
        super("TaskButlerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //get all active reminders
        AlarmReceiver alarm = new AlarmReceiver();
        NextBase db = new NextBase(getApplicationContext());
        db.open();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                long rowId = c.getLong(c.getColumnIndex(NextBase._ID));
                int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                if (isDone != 1) {
                    alarm.enableReminder(getApplicationContext(), rowId);
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        stopSelf();
    }
}