package com.backdoor.moove;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CircularButton;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.backdoor.shared.SharedConst;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class ReminderActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private TextView mTextView;
    private static final String TAG = "WearReminder";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        Intent intent = getIntent();
        final String task = intent.getStringExtra(Const.INTENT_TEXT);
        final String type = intent.getStringExtra(Const.INTENT_TYPE);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

                CircularButton buttonOk = (CircularButton) findViewById(R.id.buttonOk);
                CircularButton buttonCall = (CircularButton) findViewById(R.id.buttonCall);
                CircularButton buttonNotification = (CircularButton) findViewById(R.id.buttonNotification);

                if (type != null) {
                    if (type.contains("call")) {
                        mTextView.setText(task);
                    } else if (type.contains("message")) {
                        mTextView.setText(task);
                        buttonCall.setVisibility(View.VISIBLE);
                        buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
                    } else {
                        mTextView.setText(task);
                        buttonCall.setVisibility(View.INVISIBLE);
                    }

                    buttonCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (type.contains("message")){
                                sendRequest(SharedConst.KEYCODE_MESSAGE);
                            } else {
                                sendRequest(SharedConst.KEYCODE_CALL);
                            }
                        }
                    });
                } else {
                    mTextView.setText(task);
                    buttonCall.setVisibility(View.INVISIBLE);
                }

                buttonNotification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendRequest(SharedConst.KEYCODE_FAVOURITE);
                    }
                });

                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendRequest(SharedConst.KEYCODE_OK);
                    }
                });
            }
        });

        Log.d(TAG, "On service create");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void sendRequest(int keyCode) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.PHONE_REMINDER);
        putDataMapReq.getDataMap().putInt(SharedConst.REQUEST_KEY, keyCode);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        Log.d(TAG, "Data sent");

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "On connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "On connection suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "On connection failed");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }
}
