/**
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.backdoor.shared.SharedConst;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Language;
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
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ShowBirthday extends Activity implements View.OnClickListener,
        TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {

    private long id;
    private int contactId;
    private String name, number, birthDate;
    private ColorSetter cs = new ColorSetter(ShowBirthday.this);
    private Notifier notifier = new Notifier(ShowBirthday.this);

    private int currVolume;
    private int streamVol;
    private int mVolume;
    private int mStream;
    private String wearMessage;

    private TextToSpeech tts;

    private static final int MY_DATA_CHECK_CODE = 111;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
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

        boolean isWake;
        if (Module.isPro()) {
            if (!prefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL)) {
                isWake = prefs.getBoolean(Prefs.BIRTHDAY_WAKE_STATUS);
            } else {
                isWake = prefs.getBoolean(Prefs.WAKE_STATUS);
            }
        } else {
            isWake = prefs.getBoolean(Prefs.WAKE_STATUS);
        }
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire();
            screenLock.release();
        }

        setContentView(R.layout.show_birthday_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        id = getIntent().getLongExtra("id", 0);

        findViewById(R.id.single_container).setVisibility(View.VISIBLE);

        CardView card = (CardView) findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());
        if (Module.isLollipop()) card.setCardElevation(Configs.CARD_ELEVATION_REMINDER);

        loadImage();

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        FloatingActionButton buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(this);
        FloatingActionButton buttonSend = (FloatingActionButton) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);
        colorify(buttonOk, buttonCall, buttonSend);
        buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        buttonSend.setImageResource(R.drawable.ic_send_black_24dp);

        DataBase db = new DataBase(ShowBirthday.this);
        db.open();
        Cursor c = db.getBirthday(id);
        if (c != null && c.moveToFirst()) {
            contactId = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
            name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
            number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
            birthDate = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
        }
        if (c != null) {
            c.close();
        }
        db.close();
        if (number == null || number.matches("")) {
            number = Contacts.getNumber(name, ShowBirthday.this);
        }
        CircleImageView contactPhoto = (CircleImageView) findViewById(R.id.contactPhoto);
        if (contactId == 0 && number != null) {
            contactId = Contacts.getIdFromNumber(number, ShowBirthday.this);
        }
        Uri photo = Contacts.getPhoto(contactId);
        if (photo != null) contactPhoto.setImageURI(photo);
        else contactPhoto.setVisibility(View.GONE);

        String years =  TimeUtil.getAgeFormatted(this, birthDate);

        RoboTextView userName = (RoboTextView) findViewById(R.id.userName);
        userName.setText(name);
        RoboTextView userNumber = (RoboTextView) findViewById(R.id.userNumber);

        RoboTextView userYears = (RoboTextView) findViewById(R.id.userYears);
        userYears.setText(years);

        wearMessage = name + "\n" + years;

        if (number == null || number.matches("noNumber")) {
            buttonCall.setVisibility(View.GONE);
            buttonSend.setVisibility(View.GONE);
            userNumber.setVisibility(View.GONE);
        } else {
            userNumber.setText(number);
        }

        notifier.showNotification(TimeUtil.getYears(birthDate), name);

        boolean isGlobal = prefs.getBoolean(Prefs.BIRTHDAY_USE_GLOBAL);
        if (!isGlobal && prefs.getBoolean(Prefs.BIRTHDAY_TTS)) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (prefs.getBoolean(Prefs.WEAR_SERVICE)) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
    }

    private void loadImage() {
        ImageView bgImage = (ImageView) findViewById(R.id.bgImage);
        bgImage.setVisibility(View.GONE);
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        String imagePrefs = prefs.getString(Prefs.REMINDER_IMAGE);
        boolean blur = prefs.getBoolean(Prefs.REMINDER_IMAGE_BLUR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = (int) (metrics.heightPixels * 0.75);
        if (imagePrefs.matches(Constants.DEFAULT)) {
            if (blur && Module.isPro()) {
                Picasso.with(this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        } else if (imagePrefs.matches(Constants.NONE)) {
            bgImage.setVisibility(View.GONE);
        } else {
            if (blur && Module.isPro()) {
                Picasso.with(this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        }
    }

    private void colorify(final FloatingActionButton... fab) {
        for (FloatingActionButton button : fab) {
            button.setBackgroundTintList(ViewUtils.getFabState(this, cs.colorAccent(), cs.colorPrimary()));
        }
    }

    public void removeFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (SharedPrefs.getInstance(this).getBoolean(Prefs.WEAR_SERVICE)) {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_STOP);
            DataMap map = putDataMapReq.getDataMap();
            map.putBoolean(SharedConst.KEY_STOP_B, true);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonOk:
                updateBirthday();
                break;
            case R.id.buttonCall:
                call();
                break;
            case R.id.buttonSend:
                sendSMS();
                break;
        }
    }

    private void sendSMS() {
        if (Permissions.checkPermission(ShowBirthday.this, Permissions.SEND_SMS)) {
            Telephony.sendSms(number, ShowBirthday.this);
            updateBirthday();
        } else {
            Permissions.requestPermission(ShowBirthday.this, 103, Permissions.SEND_SMS);
        }
    }

    private void call() {
        if (Permissions.checkPermission(ShowBirthday.this, Permissions.CALL_PHONE)) {
            Telephony.makeCall(number, ShowBirthday.this);
            updateBirthday();
        } else {
            Permissions.requestPermission(ShowBirthday.this, 104, Permissions.CALL_PHONE);
        }
    }

    private void updateBirthday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        DataBase db = new DataBase(ShowBirthday.this);
        db.open();
        db.setShown(id, String.valueOf(year));
        db.close();
        removeFlags();
        handler.removeCallbacks(increaseVolume);
        notifier.recreatePermanent();
        notifier.discardNotification();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Telephony.sendSms(number, ShowBirthday.this);
                    updateBirthday();
                } else {
                    Permissions.showInfo(ShowBirthday.this, Permissions.SEND_SMS);
                }
                break;
            case 104:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Telephony.makeCall(number, ShowBirthday.this);
                    updateBirthday();
                } else {
                    Permissions.showInfo(ShowBirthday.this, Permissions.CALL_PHONE);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        if (prefs.getBoolean(Prefs.SMART_FOLD)){
            moveTaskToBack(true);
            new RepeatNotificationReceiver().cancelAlarm(ShowBirthday.this, id);
            new RepeatNotificationReceiver().cancelAlarm(ShowBirthday.this, 0);
            removeFlags();
        } else {
            Messages.toast(getApplicationContext(), getString(R.string.select_one_of_item));
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
    protected void onDestroy() {
        super.onDestroy();
        removeFlags();
        if (!SharedPrefs.getInstance(this).getBoolean(Prefs.SYSTEM_VOLUME)) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(mStream, currVolume, 0);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                try {
                    startActivity(installTTSIntent);
                } catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Language().getLocale(ShowBirthday.this, true));
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                if (name != null && !name.matches("")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (Module.isLollipop())
                        tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);
                    else tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        } else Log.e("error", "Initialization Failed!");
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
                        updateBirthday();
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
