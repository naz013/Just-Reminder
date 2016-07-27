/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.reminder.NextBase;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.reminder.json.JParser;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.reminder.ReminderDialog;
import com.cray.software.justreminder.utils.LocationUtil;

import java.util.ArrayList;

public class CheckPosition extends IntentService {

    public CheckPosition() {
        super("CheckPosition");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        double currentLat = intent.getDoubleExtra("lat", 0);
        double currentLong = intent.getDoubleExtra("lon", 0);
        Location locationA = new Location("point A");
        locationA.setLatitude(currentLat);
        locationA.setLongitude(currentLong);
        NextBase db = new NextBase(getApplicationContext());
        TimeCount timeCount = new TimeCount(getApplicationContext());
        boolean isEnabled = SharedPrefs.getInstance(getApplicationContext()).getBoolean(Prefs.TRACKING_NOTIFICATION);
        db.open();
        Cursor c = db.getAllLocations();
        if (c != null && c.moveToFirst()) {
            do {
                long id = c.getLong(c.getColumnIndex(NextBase._ID));
                long startTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                String task = c.getString(c.getColumnIndex(NextBase.SUMMARY));
                int status = c.getInt(c.getColumnIndex(NextBase.LOCATION_STATUS));
                int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                int shown = c.getInt(c.getColumnIndex(NextBase.NOTIFICATION_STATUS));
                String json = c.getString(c.getColumnIndex(NextBase.JSON));

                int stockRadius = SharedPrefs.getInstance(getApplicationContext()).getInt(Prefs.LOCATION_RADIUS);
                if (isDone != 1) {
                    if (type.matches(Constants.TYPE_PLACES)) {
                        checkPlace(json, locationA, stockRadius, id);
                    } else {
                        JPlace jPlace = new JParser(json).getPlace();
                        double lat = jPlace.getLatitude();
                        double lon = jPlace.getLongitude();
                        int radius = jPlace.getRadius();
                        if (radius == -1) radius = stockRadius;
                        if (startTime == 0) {
                            Location locationB = new Location("point B");
                            locationB.setLatitude(lat);
                            locationB.setLongitude(lon);
                            float distance = locationA.distanceTo(locationB);
                            int roundedDistance = Math.round(distance);
                            if (type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                                if (status == LocationUtil.ACTIVE) {
                                    if (roundedDistance < radius) {
                                        db.setLocationStatus(id, LocationUtil.LOCKED);
                                    }
                                }
                                if (status == LocationUtil.LOCKED) {
                                    if (roundedDistance > radius) {
                                        showReminder(id);
                                    } else {
                                        if (isEnabled) {
                                            showNotification(id, roundedDistance, shown, task);
                                        }
                                    }
                                }
                            } else {
                                if (roundedDistance <= radius) {
                                    if (status != LocationUtil.SHOWN) {
                                        showReminder(id);
                                    }
                                } else {
                                    if (isEnabled) {
                                        showNotification(id, roundedDistance, shown, task);
                                    }
                                }
                            }
                        } else {
                            if (timeCount.isCurrent(startTime)) {
                                Location locationB = new Location("point B");
                                locationB.setLatitude(lat);
                                locationB.setLongitude(lon);
                                float distance = locationA.distanceTo(locationB);
                                int roundedDistance = Math.round(distance);
                                if (type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                                    if (status == LocationUtil.ACTIVE) {
                                        if (roundedDistance <= radius) {
                                            db.setLocationStatus(id, LocationUtil.LOCKED);
                                        }
                                    }
                                    if (status == LocationUtil.LOCKED) {
                                        if (roundedDistance > radius) {
                                            showReminder(id);
                                        } else {
                                            if (isEnabled) {
                                                showNotification(id, roundedDistance, shown, task);
                                            }
                                        }
                                    }
                                } else {
                                    if (roundedDistance <= radius) {
                                        if (status != LocationUtil.SHOWN) {
                                            showReminder(id);
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
            } while (c.moveToNext());
        } else {
            getApplication().stopService(new Intent(getApplicationContext(), GeolocationService.class));
            stopSelf();
        }

        if (c != null) c.close();
        db.close();
    }

    private void checkPlace(String json, Location locationA, int stockRadius, long id) {
        ArrayList<JPlace> list = new JParser(json).getPlaces();
        if (list != null) {
            for (JPlace jPlace : list) {
                Location locationB = new Location("point B");
                locationB.setLatitude(jPlace.getLatitude());
                locationB.setLongitude(jPlace.getLongitude());
                float distance = locationA.distanceTo(locationB);
                int roundedDistance = Math.round(distance);
                int radius = jPlace.getRadius();
                if (radius == -1) radius = stockRadius;
                if (roundedDistance <= radius) {
                    showReminder(id);
                }
            }
        }
    }

    private void showReminder(long id){
        NextBase db = new NextBase(getApplicationContext());
        db.open().setLocationStatus(id, LocationUtil.SHOWN);
        db.close();
        Intent resultIntent = new Intent(getApplicationContext(), ReminderDialog.class);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(resultIntent);
    }

    private void showNotification(long id, int roundedDistance, int shown, String task){
        Integer i = (int) (long) id;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentText(String.valueOf(roundedDistance));
        if (shown != 1) {
            builder.setContentTitle(task);
            builder.setContentText(String.valueOf(roundedDistance));
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        }
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(i, builder.build());
    }
}