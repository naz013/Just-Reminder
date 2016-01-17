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
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.cray.software.justreminder.services.MissedCallAlarm;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.RoundImageView;

import java.util.Calendar;

public class MissedCallDialog extends Activity {
    private MissedCallAlarm alarm = new MissedCallAlarm();
    private long id;
    private SharedPrefs sPrefs;
    private ColorSetter cs = new ColorSetter(MissedCallDialog.this);
    private Notifier notifier = new Notifier(MissedCallDialog.this);
    private String number;
    private int currVolume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPrefs = new SharedPrefs(MissedCallDialog.this);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int prefsVol = sPrefs.loadInt(Prefs.VOLUME);
        float volPercent = (float) prefsVol / Configs.MAX_VOLUME;
        int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVol = (int) (maxVol * volPercent);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0);

        boolean isFull = sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE);
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

        String name = Contacts.getContactNameFromNumber(number, MissedCallDialog.this);

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String formattedTime = TimeUtil.getTime(calendar.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
        if (name != null && !name.matches("")) {
            remText.setText(SuperUtil.appendString(name, "\n", number, "\n\n\n\n", getString(R.string.last_called),
                    "\n", formattedTime));
        } else {
            remText.setText(number + "\n\n\n" + getString(R.string.last_called) + "\n" + formattedTime);
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
        sPrefs = new SharedPrefs(MissedCallDialog.this);
        if (sPrefs.loadBoolean(Prefs.WAKE_STATUS)) {
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
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);
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