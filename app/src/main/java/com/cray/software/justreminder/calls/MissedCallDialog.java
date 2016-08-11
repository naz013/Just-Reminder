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

package com.cray.software.justreminder.calls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backdoor.shared.SharedConst;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
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

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MissedCallDialog extends Activity implements GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {

    private MissedCallAlarm alarm = new MissedCallAlarm();
    private long id;
    private ColorSetter cs = ColorSetter.getInstance(MissedCallDialog.this);
    private Notifier notifier = new Notifier(MissedCallDialog.this);

    private String number;

    private int currVolume;
    private int streamVol;
    private int mVolume;
    private int mStream;
    private String wearMessage;

    private Handler handler = new Handler();

    private GoogleApiClient mGoogleApiClient;

    /**
     * Runnable for increasing volume in stream.
     */
    private Runnable increaseVolume = new Runnable() {
        @Override
        public void run() {
            if (mVolume < streamVol) {
                mVolume++;
                handler.postDelayed(increaseVolume, 750);
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(mStream, mVolume, 0);
            } else handler.removeCallbacks(increaseVolume);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        boolean systemVol = prefs.getBoolean(Prefs.SYSTEM_VOLUME);
        boolean increasing = prefs.getBoolean(Prefs.INCREASING_VOLUME);
        if (systemVol) {
            mStream = prefs.getInt(Prefs.SOUND_STREAM);
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            currVolume = am.getStreamVolume(mStream);
            streamVol = currVolume;
            mVolume = currVolume;
            if (increasing) {
                mVolume = 0;
                handler.postDelayed(increaseVolume, 750);
            }
            am.setStreamVolume(mStream, mVolume, 0);
        } else {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mStream = 3;
            currVolume = am.getStreamVolume(mStream);
            int prefsVol = prefs.getInt(Prefs.VOLUME);
            float volPercent = (float) prefsVol / Configs.MAX_VOLUME;
            int maxVol = am.getStreamMaxVolume(mStream);
            streamVol = (int) (maxVol * volPercent);
            mVolume = streamVol;
            if (increasing) {
                mVolume = 0;
                handler.postDelayed(increaseVolume, 750);
            }
            am.setStreamVolume(mStream, mVolume, 0);
        }

        boolean isFull = prefs.getBoolean(Prefs.UNLOCK_DEVICE);
        if (isFull) {
            runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        }

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        Intent res = getIntent();
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        number = res.getStringExtra("number");
        long time = res.getLongExtra("time", System.currentTimeMillis());

        CardView card = (CardView) findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());
        if (Module.isLollipop()) card.setCardElevation(Configs.CARD_ELEVATION_REMINDER);

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        RelativeLayout subjectContainer = (RelativeLayout) findViewById(R.id.subjectContainer);
        single_container.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        subjectContainer.setVisibility(View.GONE);

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        FloatingActionButton buttonEdit = (FloatingActionButton) findViewById(R.id.buttonEdit);
        FloatingActionButton buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        FloatingActionButton buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        FloatingActionButton buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        buttonDelay.setVisibility(View.GONE);
        FloatingActionButton buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        buttonDelayFor.setVisibility(View.GONE);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        buttonNotification.setVisibility(View.GONE);
        buttonEdit.setVisibility(View.GONE);

        CircleImageView contactPhoto = (CircleImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor, buttonNotification);
        buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        buttonCancel.setImageResource(R.drawable.ic_clear_black_vector);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);

        TextView remText = (TextView) findViewById(R.id.remText);
        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        TextView actionDirect = (TextView) findViewById(R.id.actionDirect);
        TextView messageView = (TextView) findViewById(R.id.messageView);
        TextView someView = (TextView) findViewById(R.id.someView);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String formattedTime = TimeUtil.getTime(calendar.getTime(),
                prefs.getBoolean(Prefs.IS_24_TIME_FORMAT));

        String name = Contacts.getNameFromNumber(number, MissedCallDialog.this);

        wearMessage = name + "\n" + number;

        if (number != null) {
            long conID = Contacts.getIdFromNumber(number, MissedCallDialog.this);
            Uri photo = Contacts.getPhoto(conID);
            if (photo != null)
                contactPhoto.setImageURI(photo);
            else contactPhoto.setVisibility(View.GONE);

            if (name == null) name = "";
            remText.setText(R.string.missed_call);
            contactInfo.setText(name + "\n" + number);
            actionDirect.setText(R.string.from);
            someView.setText(R.string.last_called);
            messageView.setText(formattedTime);
        }

        buttonCancel.setImageResource(R.drawable.ic_send_black_24dp);
        contactPhoto.setVisibility(View.VISIBLE);
        wakeScreen();

        buttonCancel.setOnClickListener(v -> sendSMS());

        buttonOk.setOnClickListener(v -> ok());

        buttonCall.setOnClickListener(v -> call());

        notifier.showMissedReminder(name == null || name.matches("") ? number : name, id);

        if (prefs.getBoolean(Prefs.WEAR_SERVICE)) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
    }

    private void call() {
        removeMissed();
        if (Permissions.checkPermission(MissedCallDialog.this, Permissions.CALL_PHONE)) {
            Telephony.makeCall(number, MissedCallDialog.this);
            removeFlags();
            finish();
        } else {
            Permissions.requestPermission(MissedCallDialog.this, 104, Permissions.CALL_PHONE);
        }
    }

    private void ok() {
        removeMissed();
        removeFlags();
        finish();
    }

    private void sendSMS() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("vnd.android-dir/mms-sms");
        sendIntent.putExtra("address", number);
        startActivity(Intent.createChooser(sendIntent, "SMS:"));
        removeMissed();
        removeFlags();
        finish();
    }

    private void removeMissed() {
        alarm.cancelAlarm(getApplicationContext(), id);
        notifier.discardNotification(id);
        handler.removeCallbacks(increaseVolume);
        DataBase db = new DataBase(MissedCallDialog.this);
        db.open();
        db.deleteMissedCall(id);
        db.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 104:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Telephony.makeCall(number, MissedCallDialog.this);
                    removeFlags();
                    finish();
                } else {
                    Permissions.showInfo(MissedCallDialog.this, Permissions.CALL_PHONE);
                }
                break;
        }
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button:fab){
            button.setBackgroundTintList(ViewUtils.getFabState(this, cs.colorAccent(), cs.colorPrimary()));
        }
    }

    public void wakeScreen() {
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WAKE_STATUS)) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (!isScreenOn) {
                PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.FULL_WAKE_LOCK, "Just");
                screenLock.acquire();
                screenLock.release();
            }
        }
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE)) {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_STOP);
            DataMap map = putDataMapReq.getDataMap();
            map.putBoolean(SharedConst.KEY_STOP_B, true);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()){
            notifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifier.recreatePermanent();
        removeFlags();
        boolean systemVol = SharedPrefs.getInstance(this).getBoolean(Prefs.SYSTEM_VOLUME);
        if (!systemVol) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(mStream, currVolume, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE))
            mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE)) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.SMART_FOLD)){
            moveTaskToBack(true);
            removeFlags();
        } else Messages.toast(getApplicationContext(), getString(R.string.select_one_of_item));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d(Constants.LOG_TAG, "Connected");

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_BIRTHDAY);
        DataMap map = putDataMapReq.getDataMap();
        map.putString(SharedConst.KEY_TASK, wearMessage);
        map.putInt(SharedConst.KEY_COLOR, cs.colorAccent());
        map.putBoolean(SharedConst.KEY_THEME, cs.isDark());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(Constants.LOG_TAG, "Data received");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(SharedConst.PHONE_BIRTHDAY) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    int keyCode = dataMap.getInt(SharedConst.REQUEST_KEY);
                    if (keyCode == SharedConst.KEYCODE_OK) {
                        ok();
                    } else if (keyCode == SharedConst.KEYCODE_MESSAGE) {
                        sendSMS();
                    } else {
                        call();
                    }
                }
            }
        }
    }
}