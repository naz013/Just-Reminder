package com.cray.software.justreminder.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

public class SetBirthdays extends Service {

    private BirthdayAlarm alarmReceiver = new BirthdayAlarm();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarmReceiver.cancelAlarm(getApplicationContext(), 210);
        DataBase db = new DataBase(getApplicationContext());
        db.open();
        if (db.getCountBirthdays() > 0) {
            SharedPrefs sharedPrefs = new SharedPrefs(getApplicationContext());
            int hour = sharedPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
            int minute = sharedPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
            alarmReceiver.setBirthdaysAlarm(getApplicationContext(), hour, minute);
            stopSelf();
        } else stopSelf();
        return START_STICKY;
    }
}
