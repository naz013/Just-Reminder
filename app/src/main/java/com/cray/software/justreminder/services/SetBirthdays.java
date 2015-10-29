package com.cray.software.justreminder.services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import com.cray.software.justreminder.databases.DataBase;

public class SetBirthdays extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DataBase db = new DataBase(getApplicationContext());
        db.open();
        Cursor c = db.getBirthdays();
        if (c != null && c.moveToFirst()) {
            new BirthdayAlarm().setBirthdaysAlarm(getApplicationContext());
            stopSelf();
        } else stopSelf();
        return START_STICKY;
    }
}
