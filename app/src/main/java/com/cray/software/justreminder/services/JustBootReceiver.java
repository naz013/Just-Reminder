package com.cray.software.justreminder.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;

public class JustBootReceiver extends BroadcastReceiver {
    DataBase DB;

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TaskButlerService.class));
        context.startService(new Intent(context, SetBirthdays.class));
        if (new SharedPrefs(context).loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)){
            new Notifier(context).showPermanent();
        }
        if (new SharedPrefs(context).loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS)){
            new BirthdayCheckAlarm().setAlarm(context);
        }
        DB = new DataBase(context);
        DB.open();
        if (DB.getCount() != 0){
            Cursor c = DB.queryGroup();
            if (c != null && c.moveToFirst()){
                ArrayList<String> types = new ArrayList<>();
                do{
                    String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                    types.add(type);
                } while (c.moveToNext());
                if (types.contains(Constants.TYPE_LOCATION) ||
                        types.contains(Constants.TYPE_LOCATION_CALL) ||
                        types.contains(Constants.TYPE_LOCATION_MESSAGE)){
                    context.startService(new Intent(context, GeolocationService.class));
                }
            }
            if (c != null) c.close();
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            context.startService(new Intent(context, WearService.class));
        }*/
    }
}
