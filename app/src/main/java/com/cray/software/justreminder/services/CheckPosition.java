package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ReminderDialog;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.LocationUtil;

import java.util.ArrayList;

public class CheckPosition extends IntentService {
    DataBase DB;
    NotificationManager mNotifyMgr;
    NotificationCompat.Builder builder;
    TimeCount tc;


    public CheckPosition() {
        super("CheckPosition");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    protected void onHandleIntent(final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                double currentLat = intent.getDoubleExtra("lat", 0);
                double currentLong = intent.getDoubleExtra("lon", 0);
                Location locationA = new Location("point A");
                locationA.setLatitude(currentLat);
                locationA.setLongitude(currentLong);
                DB = new DataBase(getApplicationContext());
                tc = new TimeCount(getApplicationContext());
                SharedPrefs sPrefs = new SharedPrefs(getApplicationContext());
                boolean isEnabled = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION);
                DB.open();
                Cursor c = DB.queryGroup();
                if (c != null && c.moveToFirst()) {
                    ArrayList<String> types = new ArrayList<>();
                    do{
                        String tp = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                        types.add(tp);
                    } while (c.moveToNext());
                    if (types.contains(Constants.TYPE_LOCATION) || types.contains(Constants.TYPE_LOCATION_CALL) ||
                            types.contains(Constants.TYPE_LOCATION_MESSAGE) ||
                            types.contains(Constants.TYPE_LOCATION_OUT) || types.contains(Constants.TYPE_LOCATION_OUT_CALL) ||
                            types.contains(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                        c.moveToFirst();
                        do {
                            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                            if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                                double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                                double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                                String task = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                                int status = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                                int shown = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                                int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
                                int stockRadius = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LOCATION_RADIUS);
                                if (radius == -1) radius = stockRadius;
                                if (isDone != 1) {
                                    if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0) {
                                        Location locationB = new Location("point B");
                                        locationB.setLatitude(lat);
                                        locationB.setLongitude(lon);
                                        float distance = locationA.distanceTo(locationB);
                                        int roundedDistance = Math.round(distance);
                                        if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
                                            if (status == LocationUtil.ACTIVE){
                                                if (roundedDistance <= radius) {
                                                    DB.setLocationStatus(id, LocationUtil.LOCKED);
                                                }
                                            }
                                            if (status == LocationUtil.LOCKED){
                                                if (roundedDistance > radius) {
                                                    showReminder(id, task);
                                                } else {
                                                    if (isEnabled) {
                                                        showNotification(id, roundedDistance, shown, task);
                                                    }
                                                }
                                            }
                                        } else {
                                            if (roundedDistance <= radius) {
                                                if (status != LocationUtil.SHOWN) {
                                                    showReminder(id, task);
                                                }
                                            } else {
                                                if (isEnabled) {
                                                    showNotification(id, roundedDistance, shown, task);
                                                }
                                            }
                                        }
                                    } else {
                                        if (tc.isCurrent(year, month, day, hour, minute, 0)) {
                                            Location locationB = new Location("point B");
                                            locationB.setLatitude(lat);
                                            locationB.setLongitude(lon);
                                            float distance = locationA.distanceTo(locationB);
                                            int roundedDistance = Math.round(distance);
                                            if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
                                                if (status == LocationUtil.ACTIVE){
                                                    if (roundedDistance <= radius) {
                                                        DB.setLocationStatus(id, LocationUtil.LOCKED);
                                                    }
                                                }
                                                if (status == LocationUtil.LOCKED){
                                                    if (roundedDistance > radius) {
                                                        showReminder(id, task);
                                                    } else {
                                                        if (isEnabled) {
                                                            showNotification(id, roundedDistance, shown, task);
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (roundedDistance <= radius) {
                                                    if (status != LocationUtil.SHOWN) {
                                                        showReminder(id, task);
                                                    }
                                                } else {
                                                    if (isEnabled) {
                                                        showNotification(id, roundedDistance, shown, task);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        while (c.moveToNext());
                    } else {
                        getApplication().stopService(new Intent(getApplicationContext(), GeolocationService.class));
                        stopSelf();
                    }
                } else {
                    getApplication().stopService(new Intent(getApplicationContext(), GeolocationService.class));
                    stopSelf();
                }

                if (c != null) c.close();
            }
        }).start();
    }

    private void showReminder(long id, String task){
        DataBase DB = new DataBase(getApplicationContext());
        DB.open().setLocationStatus(id, LocationUtil.SHOWN);
        Intent resultIntent = new Intent(getApplicationContext(), ReminderDialog.class);
        resultIntent.putExtra("taskDialog", task);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(resultIntent);
        stopIt();
    }

    private void showNotification(long id, int roundedDistance, int shown, String task){
        Integer i = (int) (long) id;
        builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentText(String.valueOf(roundedDistance));
        if (shown != 1) {
            builder.setContentTitle(task);
            builder.setContentText(String.valueOf(roundedDistance));
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        }
        mNotifyMgr =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify(i, builder.build());
    }

    private void stopIt(){
        new DisableAsync(getApplicationContext()).execute();
    }
}