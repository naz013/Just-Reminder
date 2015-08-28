package com.cray.software.justreminder.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.cray.software.justreminder.interfaces.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class WearService extends Service implements DataApi.DataListener,
        MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    public WearService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.LOG_TAG, "---------------------onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
        Log.d(Constants.LOG_TAG, "on start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (!mResolvingError) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        Log.d(Constants.LOG_TAG, "on destroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(Constants.LOG_TAG, "data received");
        for (DataEvent event: dataEventBuffer) {

            Log.d("[DEBUG] DeviceService ", "Event received: " + event.getDataItem().getUri());

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/hearMe/voice")) {

                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                String data = dataItem.getDataMap().getString("key");

                Log.d(Constants.LOG_TAG, "received string " + data);
                Toast.makeText(getApplicationContext(), "Received " + data, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(Constants.LOG_TAG, "message received");
    }

    @Override
    public void onConnected(Bundle bundle) {
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Log.d(Constants.LOG_TAG, "connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(Constants.LOG_TAG, "Connection to Google API client has failed");
        mResolvingError = false;
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
    }
}
