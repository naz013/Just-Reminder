package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.content.Intent;

import com.cray.software.justreminder.helpers.Notifier;

public class BirthdayPermanentService extends IntentService {

    public BirthdayPermanentService(){super("BirthdayPermanentService");}

    @Override
    protected void onHandleIntent(Intent intent) {
        new BirthdayPermanentAlarm().cancelAlarm(getApplicationContext());
        new Notifier(getApplicationContext()).hideBirthdayPermanent();
        stopSelf();
    }
}
