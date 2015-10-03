package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;
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
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Language;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.RoundImageView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class ShowBirthday extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private DataBase DB;
    private long id;
    private SharedPrefs sPrefs;
    private int contactId;
    private String name, number, birthDate;
    private ColorSetter cs = new ColorSetter(ShowBirthday.this);
    private Notifier notifier = new Notifier(ShowBirthday.this);
    private TextToSpeech tts;
    private boolean isDark = false;

    private static final int MY_DATA_CHECK_CODE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        sPrefs = new SharedPrefs(ShowBirthday.this);
        boolean isFull = sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE);
        if (isFull) {
            runOnUiThread(new Runnable() {
                public void run() {
                    getWindow().addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            });
        }

        setContentView(R.layout.show_birthday_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        id = i.getLongExtra("id", 0);

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        Typeface typeface = AssetsUtil.getLightTypeface(this);

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);

        FloatingActionButton buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(this);

        FloatingActionButton buttonSend = (FloatingActionButton) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);

        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        colorify(buttonOk, buttonCall, buttonSend);
        if (isDark){
            buttonOk.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_done_grey600_24dp));
            buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_call_grey600_24dp));
            buttonSend.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_send_grey600_24dp));
        } else {
            buttonOk.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_done_white_24dp));
            buttonCall.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_call_white_24dp));
            buttonSend.setIconDrawable(ViewUtils.getDrawable(this, R.drawable.ic_send_white_24dp));
        }

        DB = new DataBase(ShowBirthday.this);
        sPrefs = new SharedPrefs(ShowBirthday.this);
        Contacts contacts = new Contacts(ShowBirthday.this);

        DB.open();
        Cursor c = DB.getBirthday(id);
        if (c != null && c.moveToFirst()){
            contactId = c.getInt(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_ID));
            name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
            number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
            birthDate = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
        }
        if (c != null) c.close();
        if (number == null || number.matches("")) {
            number = Contacts.getNumber(name, ShowBirthday.this);
        }
        RoundImageView contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        Bitmap photo = Contacts.getPhoto(this, contactId);
        if (photo != null) {
            contactPhoto.setImageBitmap(photo);
        } else {
            contactPhoto.setVisibility(View.GONE);
        }

        String years = TimeUtil.getYears(birthDate) + " " + getString(R.string.years_string);

        TextView userName = (TextView) findViewById(R.id.userName);
        userName.setTypeface(typeface);
        userName.setText(name);
        TextView userNumber = (TextView) findViewById(R.id.userNumber);
        userNumber.setTypeface(typeface);

        TextView userYears = (TextView) findViewById(R.id.userYears);
        userYears.setTypeface(typeface);
        userYears.setText(years);

        if (number == null || number.matches("noNumber")) {
            buttonCall.setVisibility(View.GONE);
            buttonSend.setVisibility(View.GONE);
            userNumber.setVisibility(View.GONE);
        } else {
            userNumber.setText(number);
        }

        wakeScreen();

        notifier.showNotification(TimeUtil.getYears(birthDate), name);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        boolean isGlobal = sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL);
        if (!isGlobal && sPrefs.loadBoolean(Prefs.BIRTHDAY_TTS)) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button : fab){
            if (isDark){
                button.setColorNormal(getResources().getColor(R.color.colorWhite));
                button.setColorPressed(getResources().getColor(R.color.colorGrayDark));
            } else {
                button.setColorNormal(getResources().getColor(R.color.colorGrayDark));
                button.setColorPressed(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    private void wakeScreen() {
        boolean wake;
        sPrefs = new SharedPrefs(ShowBirthday.this);
        if (Module.isPro()){
            if (!sPrefs.loadBoolean(Prefs.BIRTHDAY_USE_GLOBAL)){
                wake = sPrefs.loadBoolean(Prefs.BIRTHDAY_WAKE_STATUS);
            } else wake = sPrefs.loadBoolean(Prefs.WAKE_STATUS);
        } else wake = sPrefs.loadBoolean(Prefs.WAKE_STATUS);

        if (wake) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            if (!isScreenOn) {
                PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.FULL_WAKE_LOCK, "Just");
                screenLock.acquire(2000);
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
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        switch (v.getId()){
            case R.id.buttonOk:
                notifier.discardNotification();
                DB.open();
                DB.setShown(id, String.valueOf(year));
                removeFlags();
                notifier.recreatePermanent();
                finish();
                break;
            case R.id.buttonCall:
                notifier.discardNotification();
                if (new Permissions(ShowBirthday.this).checkPermission(Permissions.CALL_PHONE)) {
                    Telephony.makeCall(number, ShowBirthday.this);
                    DB.open();
                    DB.setShown(id, String.valueOf(year));
                    removeFlags();
                    notifier.recreatePermanent();
                    finish();
                } else {
                    new Permissions(ShowBirthday.this).requestPermission(ShowBirthday.this,
                            new String[]{Permissions.CALL_PHONE}, 104);
                }
                break;
            case R.id.buttonSend:
                notifier.discardNotification();
                if (new Permissions(ShowBirthday.this).checkPermission(Permissions.SEND_SMS)) {
                    Telephony.sendSms(number, ShowBirthday.this);
                    DB.open();
                    DB.setShown(id, String.valueOf(year));
                    removeFlags();
                    notifier.recreatePermanent();
                    finish();
                } else {
                    new Permissions(ShowBirthday.this).requestPermission(ShowBirthday.this,
                            new String[]{Permissions.SEND_SMS}, 103);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        switch (requestCode){
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Telephony.sendSms(number, ShowBirthday.this);
                    DB.open();
                    DB.setShown(id, String.valueOf(year));
                    removeFlags();
                    notifier.recreatePermanent();
                    finish();
                } else {
                    new Permissions(ShowBirthday.this).showInfo(ShowBirthday.this, Permissions.SEND_SMS);
                }
                break;
            case 104:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Telephony.makeCall(number, ShowBirthday.this);
                    DB.open();
                    DB.setShown(id, String.valueOf(year));
                    removeFlags();
                    notifier.recreatePermanent();
                    finish();
                } else {
                    new Permissions(ShowBirthday.this).showInfo(ShowBirthday.this, Permissions.CALL_PHONE);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        sPrefs = new SharedPrefs(ShowBirthday.this);
        if (sPrefs.loadBoolean(Prefs.SMART_FOLD)){
            moveTaskToBack(true);
            new RepeatNotificationReceiver().cancelAlarm(ShowBirthday.this, id);
            new RepeatNotificationReceiver().cancelAlarm(ShowBirthday.this, 0);
            removeFlags();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.must_click_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        sPrefs = new SharedPrefs(ShowBirthday.this);
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(new Language().getLocale(ShowBirthday.this, true));
            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("error", "This Language is not supported");
            } else{
                if (name != null && !name.matches("")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        } else
            Log.e("error", "Initialization Failed!");
    }
}
