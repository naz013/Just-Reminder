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
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

public class GeolocationService extends Service {

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.LOG_TAG, "geo service started");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocList = new MyLocation();
        SharedPrefs prefs = new SharedPrefs(getApplicationContext());
        long time;
        time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
        int distance;
        distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
        //Toast.makeText(getApplicationContext(), "service start ", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocList);
        Log.d(Constants.LOG_TAG, "geo service stop");
        //Toast.makeText(getApplicationContext(), "service stop ", Toast.LENGTH_SHORT).show();
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
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time;
            time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
            int distance;
            distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time;
            time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
            int distance;
            distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time;
            time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
            int distance;
            distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }
    }
}
