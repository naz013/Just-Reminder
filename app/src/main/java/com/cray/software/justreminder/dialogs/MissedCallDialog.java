package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.MissedCallAlarm;
import com.cray.software.justreminder.views.RoundImageView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MissedCallDialog extends Activity {
    FloatingActionButton buttonOk, buttonCancel, buttonCall, buttonDelay, buttonDelayFor, buttonNotification;
    TextView remText;
    RoundImageView contactPhoto;
    LinearLayout single_container;
    MissedCallAlarm alarm = new MissedCallAlarm();
    long id;
    SharedPrefs sPrefs;
    Contacts contacts;
    ColorSetter cs = new ColorSetter(MissedCallDialog.this);
    Notifier notifier = new Notifier(MissedCallDialog.this);
    DataBase db = new DataBase(MissedCallDialog.this);

    boolean isDark = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPrefs = new SharedPrefs(MissedCallDialog.this);
        boolean isFull = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_UNLOCK_DEVICE);
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
        final String number = res.getStringExtra("number");
        long time = res.getLongExtra("time", 0);

        contacts = new Contacts(MissedCallDialog.this);
        String name = contacts.getContactNameFromNumber(number, MissedCallDialog.this);

        single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        buttonDelay.setVisibility(View.GONE);
        buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        buttonDelayFor.setVisibility(View.GONE);
        buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        buttonNotification.setVisibility(View.GONE);
        contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        isDark = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor, buttonNotification);
        if (isDark){
            buttonOk.setIconDrawable(getResources().getDrawable(R.drawable.ic_done_grey600_24dp));
            buttonCancel.setIconDrawable(getResources().getDrawable(R.drawable.ic_clear_grey600_24dp));
            buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_call_grey600_24dp));
            buttonNotification.setIconDrawable(getResources().getDrawable(R.drawable.ic_favorite_grey600_24dp));
        } else {
            buttonOk.setIconDrawable(getResources().getDrawable(R.drawable.ic_done_white_24dp));
            buttonCancel.setIconDrawable(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
            buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_call_white_24dp));
            buttonNotification.setIconDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        }

        remText = (TextView) findViewById(R.id.remText);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMMM yyyy, HH:mm");
            formattedTime = sdf.format(calendar.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMMM yyyy, K:mm a");
            formattedTime = sdf.format(calendar.getTime());
        }
        if (name != null && !name.matches("")) {
            remText.setText(name + "\n" + number + "\n\n" +
                    "\n" +
                    "\n" + getString(R.string.string_last_called) + "\n" + formattedTime);
        } else {
            remText.setText(number + "\n" +
                    "\n" +
                    "\n" + getString(R.string.string_last_called) + "\n" + formattedTime);
        }
        if (isDark) buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_send_grey600_24dp));
        else buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_send_white_24dp));

        contactPhoto.setVisibility(View.VISIBLE);
        Bitmap photo = contacts.openPhoto(id);
        if (photo != null) {
            contactPhoto.setImageBitmap(photo);
        } else {
            contactPhoto.setVisibility(View.GONE);
        }

        wakeScreen();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancelAlarm(getApplicationContext(), id);
                notifier.discardNotification(id);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setType("vnd.android-dir/mms-sms");
                sendIntent.putExtra("address", number);
                startActivity(Intent.createChooser(sendIntent, "SMS:"));
                db.open();
                db.deleteMissedCall(id);
                removeFlags();
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancelAlarm(getApplicationContext(), id);
                notifier.discardNotification(id);
                db.open();
                db.deleteMissedCall(id);
                removeFlags();
                finish();
            }
        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancelAlarm(getApplicationContext(), id);
                notifier.discardNotification(id);
                db.open();
                db.deleteMissedCall(id);
                makeCall(number);
                removeFlags();
                finish();
            }
        });

        notifier.showMissedReminder(name == null || name.matches("") ? number : name, id);
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button:fab){
            if (isDark){
                button.setColorNormal(getResources().getColor(R.color.colorWhite));
                button.setColorPressed(getResources().getColor(R.color.colorGrayDark));
            } else {
                button.setColorNormal(getResources().getColor(R.color.colorGrayDark));
                button.setColorPressed(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    public void wakeScreen() {
        sPrefs = new SharedPrefs(MissedCallDialog.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WAKE_STATUS)) {
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

    public void makeCall(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
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
        notifier.recreatePermanent();
        removeFlags();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        sPrefs = new SharedPrefs(MissedCallDialog.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD)){
            moveTaskToBack(true);
            removeFlags();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.must_click_message), Toast.LENGTH_SHORT).show();
        }
    }
}