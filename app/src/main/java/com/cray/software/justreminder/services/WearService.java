package com.cray.software.justreminder.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.backdoor.shared.SharedConst;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Recognize;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class WearService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private GoogleApiClient mGoogleApiClient;

    public WearService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        Log.d(Constants.LOG_TAG, "Create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Log.d(Constants.LOG_TAG, "Destroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(Constants.LOG_TAG, "Data received");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(SharedConst.PHONE_VOICE) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    if (dataMap.getInt(SharedConst.KEY_LANGUAGE, 0) == 1) {
                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_VOICE);
                        putDataMapReq.getDataMap().putString(SharedConst.KEY_LANGUAGE,
                                Language.getLanguage(new SharedPrefs(getApplicationContext()).loadInt(Prefs.VOICE_LOCALE)));
                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
                    }
                }
                if (item.getUri().getPath().compareTo(SharedConst.PHONE_VOICE_RES) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    ArrayList<String> res = dataMap.getStringArrayList(SharedConst.KEY_VOICE_RES);
                    if (res != null && res.size() > 0) {
                        new Recognize(getApplicationContext()).parseResults(res, false, true);
                    }
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(Constants.LOG_TAG, "connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(Constants.LOG_TAG, "connection failed \n" + connectionResult.getErrorMessage());
    }
}
