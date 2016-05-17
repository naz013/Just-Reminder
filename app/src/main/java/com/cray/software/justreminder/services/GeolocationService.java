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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;

public class GeolocationService extends Service {

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.LOG_TAG, "geo service started");
        mLocList = new MyLocation();
        updateListener();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocList);
        stopService(new Intent(getApplicationContext(), CheckPosition.class));
        Log.d(Constants.LOG_TAG, "geo service stop");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MyLocation implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            double currentLat = location.getLatitude();
            double currentLong = location.getLongitude();
            getApplicationContext().startService(new Intent(getApplicationContext(), CheckPosition.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("lat", currentLat)
                    .putExtra("lon", currentLong));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            updateListener();
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateListener();
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateListener();
        }
    }

    private void updateListener() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SharedPrefs prefs = new SharedPrefs(getApplicationContext());
        long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
        int distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
        }
    }
}
