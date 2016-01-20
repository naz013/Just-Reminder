package com.cray.software.justreminder.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.services.MissedCallAlarm;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.RoundImageView;

import java.util.Calendar;

public class MissedCallDialog extends Activity {

    private MissedCallAlarm alarm = new MissedCallAlarm();
    private long id;
    private ColorSetter cs = new ColorSetter(MissedCallDialog.this);
    private Notifier notifier = new Notifier(MissedCallDialog.this);

    private String number;

    private int currVolume;
    private int streamVol;
    private int mVolume;
    private int mStream;

    private Handler handler = new Handler();

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
        SharedPrefs prefs = new SharedPrefs(MissedCallDialog.this);
        boolean systemVol = prefs.loadBoolean(Prefs.SYSTEM_VOLUME);
        boolean increasing = prefs.loadBoolean(Prefs.INCREASING_VOLUME);
        if (systemVol) {
            mStream = prefs.loadInt(Prefs.SOUND_STREAM);
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
            int prefsVol = prefs.loadInt(Prefs.VOLUME);
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

        boolean isFull = prefs.loadBoolean(Prefs.UNLOCK_DEVICE);
        if (isFull) {
            runOnUiThread(new Runnable() {
                public void run() {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            });
        }

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        Intent res = getIntent();
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        number = res.getStringExtra("number");
        long time = res.getLongExtra("time", 0);

        CardView card = (CardView) findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());
        if (Module.isLollipop()) card.setCardElevation(Configs.CARD_ELEVATION_REMINDER);

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        LinearLayout subjectContainer = (LinearLayout) findViewById(R.id.subjectContainer);
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
        RoundImageView contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor, buttonNotification);
        buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);

        TextView remText = (TextView) findViewById(R.id.remText);
        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        TextView actionDirect = (TextView) findViewById(R.id.actionDirect);
        TextView messageView = (TextView) findViewById(R.id.messageView);
        TextView someView = (TextView) findViewById(R.id.someView);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String formattedTime = TimeUtil.getTime(calendar.getTime(),
                prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));

        String name = Contacts.getNameFromNumber(number, MissedCallDialog.this);

        if (number != null) {
            long conID = Contacts.getIdFromNumber(number, MissedCallDialog.this);
            Bitmap photo = Contacts.getPhoto(this, conID);
            if (photo != null) {
                contactPhoto.setImageBitmap(photo);
            } else {
                contactPhoto.setVisibility(View.GONE);
            }
            if (name == null) name = "";
            remText.setText(R.string.missed_call);
            contactInfo.setText(name + "\n" + number);
            actionDirect.setText(R.string.from);
            someView.setText(R.string.last_called);
            messageView.setText(formattedTime);
        }

        buttonCancel.setImageResource(R.drawable.ic_send_black_24dp);

        contactPhoto.setVisibility(View.VISIBLE);
        Bitmap photo = Contacts.getPhoto(this, id);
        if (photo != null) contactPhoto.setImageBitmap(photo);
        else contactPhoto.setVisibility(View.GONE);

        wakeScreen();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setType("vnd.android-dir/mms-sms");
                sendIntent.putExtra("address", number);
                startActivity(Intent.createChooser(sendIntent, "SMS:"));
                removeMissed();
                removeFlags();
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMissed();
                removeFlags();
                finish();
            }
        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMissed();
                if (Permissions.checkPermission(MissedCallDialog.this, Permissions.CALL_PHONE)) {
                    Telephony.makeCall(number, MissedCallDialog.this);
                    removeFlags();
                    finish();
                } else {
                    Permissions.requestPermission(MissedCallDialog.this, 104, Permissions.CALL_PHONE);
                }
            }
        });

        notifier.showMissedReminder(name == null || name.matches("") ? number : name, id);
    }

    private void removeMissed() {
        alarm.cancelAlarm(getApplicationContext(), id);
        notifier.discardNotification(id);
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
        SharedPrefs prefs = new SharedPrefs(MissedCallDialog.this);
        if (prefs.loadBoolean(Prefs.WAKE_STATUS)) {
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
        SharedPrefs prefs = new SharedPrefs(MissedCallDialog.this);
        boolean systemVol = prefs.loadBoolean(Prefs.SYSTEM_VOLUME);
        if (!systemVol) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(mStream, currVolume, 0);
        }
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        if (new SharedPrefs(MissedCallDialog.this).loadBoolean(Prefs.SMART_FOLD)){
            moveTaskToBack(true);
            removeFlags();
        } else Messages.toast(getApplicationContext(), getString(R.string.select_one_of_item));
    }
}