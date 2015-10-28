package com.cray.software.justreminder.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

public class JustBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TaskButlerService.class));
        context.startService(new Intent(context, SetBirthdays.class));
        if (new SharedPrefs(context).loadBoolean(Prefs.STATUS_BAR_NOTIFICATION)){
            new Notifier(context).showPermanent();
        }
        if (new SharedPrefs(context).loadBoolean(Prefs.AUTO_CHECK_BIRTHDAYS)){
            new BirthdayCheckAlarm().setAlarm(context);
        }
        if (new SharedPrefs(context).loadBoolean(Prefs.AUTO_CHECK_FOR_EVENTS)){
            new EventsCheckAlarm().setAlarm(context);
        }
        if (new SharedPrefs(context).loadBoolean(Prefs.AUTO_BACKUP)){
            new AutoSyncAlarm().setAlarm(context);
        }
        if (new SharedPrefs(context).loadBoolean(Prefs.BIRTHDAY_PERMANENT)){
            new BirthdayPermanentAlarm().setAlarm(context);
            new Notifier(context).showBirthdayPermanent();
        }

        new DisableAsync(context).execute();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            context.startService(new Intent(context, WearService.class));
        }*/
    }
}
