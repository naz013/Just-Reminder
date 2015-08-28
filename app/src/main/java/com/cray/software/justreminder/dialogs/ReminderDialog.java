package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
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
import com.cray.software.justreminder.interfaces.Language;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.Telephony;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.utils.Utils;
import com.cray.software.justreminder.views.RoundImageView;
import com.cray.software.justreminder.views.TextDrawable;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class ReminderDialog extends Activity implements TextToSpeech.OnInitListener {
    private static final int MY_DATA_CHECK_CODE = 111;
    FloatingActionButton buttonOk, buttonCancel, buttonCall, buttonDelay, buttonDelayFor,
            buttonNotification, buttonEdit;
    TextView remText;
    RoundImageView contactPhoto;
    LinearLayout single_container;
    AlarmReceiver alarm = new AlarmReceiver();
    DelayReceiver delay = new DelayReceiver();
    RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    long id, remCount;
    DataBase DB;
    String task;
    SharedPrefs sPrefs;
    int repCode, color = -1;
    BroadcastReceiver deliveredReceiver, sentReceiver;
    ColorSetter cs = new ColorSetter(ReminderDialog.this);
    String melody, num, name;
    Notifier notifier = new Notifier(ReminderDialog.this);
    TextToSpeech tts;
    boolean isDark = false;
    int currVolume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPrefs = new SharedPrefs(ReminderDialog.this);
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
        single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        buttonEdit = (FloatingActionButton) findViewById(R.id.buttonEdit);
        buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, buttonEdit);
        int mins = sPrefs.loadInt(Prefs.DELAY_TIME);
        setTextDrawable(buttonDelay, String.valueOf(mins));
        setTextDrawable(buttonDelayFor, "...");
        if (isDark){
            buttonOk.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_done_grey600_24dp));
            buttonEdit.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_create_grey600_24dp));
            buttonCancel.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_clear_grey600_24dp));
            buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_call_grey600_24dp));
            buttonNotification.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_favorite_grey600_24dp));
        } else {
            buttonOk.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_done_white_24dp));
            buttonEdit.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_create_white_24dp));
            buttonCancel.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_clear_white_24dp));
            buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_call_white_24dp));
            buttonNotification.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_favorite_white_24dp));
        }

        contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        DB = new DataBase(ReminderDialog.this);
        DB.open();
        Cursor r = DB.getReminder(id);
        String type = "";
        if (r != null && r.moveToFirst()) {
            task = r.getString(r.getColumnIndex(Constants.COLUMN_TEXT));
            type = r.getString(r.getColumnIndex(Constants.COLUMN_TYPE));
            num = r.getString(r.getColumnIndex(Constants.COLUMN_NUMBER));
            melody = r.getString(r.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            repCode = r.getInt(r.getColumnIndex(Constants.COLUMN_REPEAT));
            remCount = r.getInt(r.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            color = r.getInt(r.getColumnIndex(Constants.COLUMN_LED_COLOR));
        }
        sPrefs.savePrefs(Prefs.REMINDER_TYPE, type);
        sPrefs.savePrefs(Prefs.REMINDER_TEXT, task);
        sPrefs.savePrefs(Prefs.REMINDER_NUMBER, num);

        remText = (TextView) findViewById(R.id.remText);
        remText.setText("");
        if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                type.matches(Constants.TYPE_SKYPE) || type.matches(Constants.TYPE_SKYPE_VIDEO) ||
                type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
            String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER);
            if (!type.startsWith(Constants.TYPE_SKYPE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getContactIDFromNumber(number, ReminderDialog.this);
                Bitmap photo = Contacts.getPhoto(this, conID);
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo);
                } else {
                    contactPhoto.setVisibility(View.GONE);
                }
            }
            name = Contacts.getContactNameFromNumber(number, ReminderDialog.this);
            remText.setText(task + "\n" + name + "\n" + number);
        } else if (type.matches(Constants.TYPE_LOCATION_MESSAGE) || type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE) ||
                type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_SKYPE_CHAT)){
            if (!sPrefs.loadBoolean(Prefs.SILENT_SMS)) {
                String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER);
                remText.setText(task + "\n" + number);
                buttonCall.setVisibility(View.VISIBLE);
                if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_send_grey600_24dp));
                else buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_send_white_24dp));
            } else {
                String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER);
                remText.setText(task + "\n" + number);
                buttonCall.setVisibility(View.GONE);
                buttonDelay.setVisibility(View.GONE);
                buttonDelayFor.setVisibility(View.GONE);
            }
        } else if (type.matches(Constants.TYPE_APPLICATION)){
            String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER);
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0);
            } catch (final PackageManager.NameNotFoundException ignored) {}
            final String nameA = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            remText.setText(nameA);
            buttonCall.setVisibility(View.VISIBLE);
            if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_open_in_browser_grey600_24dp));
            else buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_open_in_browser_white_24dp));
        } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
            String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER);
            remText.setText(number);
            buttonCall.setVisibility(View.VISIBLE);
            if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_open_in_browser_grey600_24dp));
            else buttonCall.setIconDrawable(Utils.getDrawable(this, R.drawable.ic_open_in_browser_white_24dp));
        } else {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
        }

        if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
            buttonDelay.setVisibility(View.GONE);
            buttonDelayFor.setVisibility(View.GONE);
        }

        wakeScreen();

        if (repCode == 0) {
            buttonCancel.setVisibility(View.GONE);
        } else {
            buttonCancel.setVisibility(View.VISIBLE);
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancelAlarm(getApplicationContext(), id);
                notifier.discardNotification(id);
                makeArchive(id);
                Reminder.backup(ReminderDialog.this);
                removeFlags();
                if (repCode > 0) DB.updateReminderCount(id, remCount + 1);
                repeater.cancelAlarm(ReminderDialog.this, id);
                finish();
            }
        });

        buttonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                make();
                removeFlags();
                if (repCode > 0) DB.updateReminderCount(id, remCount + 1);
                repeater.cancelAlarm(ReminderDialog.this, id);
                if ((task == null || task.trim().matches("")) &&
                        (num != null && !num.trim().matches(""))) {
                    notifier.showReminderNotification(name + " " + num, id);
                } else {
                    notifier.showReminderNotification(task, id);
                }
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                make();
                if (repCode > 0) DB.updateReminderCount(id, remCount + 1);
                removeFlags();
                repeater.cancelAlarm(ReminderDialog.this, id);
                finish();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                make();
                repeater.cancelAlarm(ReminderDialog.this, id);
                if (repCode > 0) DB.updateReminderCount(id, remCount + 1);
                removeFlags();
                Reminder.edit(id, ReminderDialog.this);
                finish();
            }
        });

        buttonDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                delay.setAlarm(ReminderDialog.this, 1, id);
                int inTime = sPrefs.loadInt(Prefs.DELAY_TIME);
                DB.setDelay(id, inTime);
                Reminder.backup(ReminderDialog.this);
                repeater.cancelAlarm(ReminderDialog.this, id);
                removeFlags();
                finish();
            }
        });
        buttonDelayFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                showDialog();
                Reminder.backup(ReminderDialog.this);
                repeater.cancelAlarm(ReminderDialog.this, id);
            }
        });
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER).trim();
                String typePrefs = sPrefs.loadPrefs(Prefs.REMINDER_TYPE);
                String task = sPrefs.loadPrefs(Prefs.REMINDER_TEXT);
                if (typePrefs.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                        typePrefs.matches(Constants.TYPE_MESSAGE) || typePrefs.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                    sendSMS(number, task, melody);
                } else if (typePrefs.matches(Constants.TYPE_SKYPE)){
                    Telephony.skypeCall(number, ReminderDialog.this);
                } else if (typePrefs.matches(Constants.TYPE_SKYPE_VIDEO)){
                    Telephony.skypeVideoCall(number, ReminderDialog.this);
                } else if (typePrefs.matches(Constants.TYPE_SKYPE_CHAT)){
                    Telephony.skypeChat(number, ReminderDialog.this);
                } else if (typePrefs.matches(Constants.TYPE_APPLICATION)){
                    openApplication(number);
                } else if (typePrefs.matches(Constants.TYPE_APPLICATION_BROWSER)){
                    openLink(number);
                } else {
                    Telephony.makeCall(number, ReminderDialog.this);
                }
                make();
                if (repCode > 0) DB.updateReminderCount(id, remCount + 1);
                removeFlags();
                repeater.cancelAlarm(ReminderDialog.this, id);
                if (!typePrefs.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                        !typePrefs.matches(Constants.TYPE_MESSAGE) ||
                        !typePrefs.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                    finish();
                }
            }
        });
        String number = sPrefs.loadPrefs(Prefs.REMINDER_NUMBER);
        String typePrefs = sPrefs.loadPrefs(Prefs.REMINDER_TYPE);
        boolean autoLaunch = sPrefs.loadBoolean(Prefs.APPLICATION_AUTO_LAUNCH);
        if (typePrefs.matches(Constants.TYPE_MESSAGE) || typePrefs.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                typePrefs.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
            if (sPrefs.loadBoolean(Prefs.SILENT_SMS)) {
                sendSMS(number, task, melody);
            } else {
                if (task == null || task.matches("")) showReminder(num, 1, id, melody, color);
                else showReminder(task, 1, id, melody, color);
            }
        } else if (typePrefs.matches(Constants.TYPE_APPLICATION)){
            if (autoLaunch) openApplication(number);

            if (task == null || task.matches("")) showReminder(num, 1, id, melody, color);
            else showReminder(task, 1, id, melody, color);
        } else if (typePrefs.matches(Constants.TYPE_APPLICATION_BROWSER)){
            if (autoLaunch) openLink(number);

            if (task == null || task.matches("")) showReminder(num, 1, id, melody, color);
            else showReminder(task, 1, id, melody, color);
        } else {
            if (task == null || task.matches("")) showReminder(num, 1, id, melody, color);
            else showReminder(task, 1, id, melody, color);
        }

        if (sPrefs.loadBoolean(Prefs.NOTIFICATION_REPEAT)) {
            repeater.setAlarm(ReminderDialog.this, id);
        }

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (sPrefs.loadBoolean(Prefs.TTS)) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    private void make(){
        if (repCode == 0){
            makeArchive(id);
        } else Reminder.generate(id, this);
        Reminder.backup(this);
    }

    private void setTextDrawable(FloatingActionButton button, String text){
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(isDark ? Color.DKGRAY : Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .fontSize(30) /* size in px */
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, Color.TRANSPARENT);
        button.setIconDrawable(drawable);
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

    private void showReminder(final String taskN, int i, long itemId, String melodyN, int colorN){
        sPrefs = new SharedPrefs(ReminderDialog.this);
        if (!sPrefs.loadBoolean(Prefs.TTS)) {
            notifier.showReminder(taskN, i, itemId, melodyN, colorN);
        } else {
            notifier.showTTSNotification(taskN, itemId, colorN);
        }
    }

    public void wakeScreen() {
        sPrefs = new SharedPrefs(ReminderDialog.this);
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

    public void openLink(String number) {
        Telephony.openLink(number, this);
        notifier.discardNotification(id);
        make();
        repeater.cancelAlarm(ReminderDialog.this, id);
        finish();
    }

    public void openApplication(String number) {
        Telephony.openApp(number, this);
        notifier.discardNotification(id);
        make();
        repeater.cancelAlarm(ReminderDialog.this, id);
        finish();
    }

    public void showDialog(){
        final CharSequence[] items = {getString(R.string.repeat_5_min),
                getString(R.string.repeat_10_min),
                getString(R.string.repeat_15_min),
                getString(R.string.repeat_30_min),
                getString(R.string.repeat_45_min),
                getString(R.string.repeat_60_min),
                getString(R.string.repeat_90_min),
                getString(R.string.repeat_120_min),
                getString(R.string.string_5_hours),
                getString(R.string.string_1_day),
                getString(R.string.string_2_days),
                getString(R.string.string_1_week)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.time_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                int x = 0;
                if (item == 0){
                    x = 5;
                } else if (item == 1){
                    x = 10;
                } else if (item == 2){
                    x = 15;
                } else if (item == 3){
                    x = 30;
                } else if (item == 4){
                    x = 45;
                } else if (item == 5){
                    x = 60;
                } else if (item == 6){
                    x = 90;
                } else if (item == 7){
                    x = 120;
                } else if (item == 8){
                    x = 60 * 5;
                } else if (item == 9){
                    x = 60 * 24;
                } else if (item == 10){
                    x = 60 * 24 * 2;
                } else if (item == 11){
                    x = 60 * 24 * 7;
                }

                delay.setAlarm(ReminderDialog.this, 1, id, x);
                if (!DB.isOpen()) DB.open();
                DB.setDelay(id, x);

                if (x < 100) {
                    Toast.makeText(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            x + " " + getString(R.string.repeat_toast_end), Toast.LENGTH_SHORT).show();
                }
                if (x > 120 && x < 60 * 24) {
                    Toast.makeText(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            (x / 60) + " " + getString(R.string.string_hours), Toast.LENGTH_SHORT).show();
                }
                if (x >= 60 * 24 && x < 60 * 24 * 7) {
                    Toast.makeText(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            (x / (60 * 24)) + " " + getString(R.string.string_days), Toast.LENGTH_SHORT).show();
                }
                if (x == 60 * 24 * 7) {
                    Toast.makeText(ReminderDialog.this, getString(R.string.repeat_toast_start) + " " +
                            1 + " " + getString(R.string.simple_week), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                removeFlags();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSMS(String phoneNumber, String message, final String melody) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(ReminderDialog.this, 0,
                new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(ReminderDialog.this,
                0, new Intent(DELIVERED), 0);

        registerReceiver(sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                sPrefs = new SharedPrefs(ReminderDialog.this);
                String text = sPrefs.loadPrefs(Prefs.REMINDER_TEXT);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        remText.setText(getString(R.string.dialog_message_sent));
                        notifier.showReminder(text, 1, id, melody, color);
                        if (buttonCall.getVisibility() == View.VISIBLE) {
                            buttonCall.setVisibility(View.GONE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        notifier.showReminder(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        notifier.showReminder(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        notifier.showReminder(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        notifier.showReminder(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(Utils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // ---when the SMS has been delivered---
        registerReceiver( deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(ReminderDialog.this, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(ReminderDialog.this, "SMS not delivered",
                        Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        new UpdatesHelper(ReminderDialog.this).updateWidget();
        DB = new DataBase(ReminderDialog.this);
        DB.open();
        DB.updateReminderDateTime(id);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()){
            notifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    public void makeArchive(long id){
        Reminder.disable(id, this);
    }

    @Override
    protected void onDestroy() {
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
        if (deliveredReceiver != null) {
            unregisterReceiver(deliveredReceiver);
        }
        notifier.recreatePermanent();
        removeFlags();
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);
        if (DB != null) DB.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        if (new SharedPrefs(ReminderDialog.this)
                .loadBoolean(Prefs.SMART_FOLD)){
            moveTaskToBack(true);
            repeater.cancelAlarm(ReminderDialog.this, id);
            removeFlags();
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.must_click_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        sPrefs = new SharedPrefs(ReminderDialog.this);
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(new Language().getLocale(ReminderDialog.this));
            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("error", "This Language is not supported");
            } else{
                if (task != null && !task.matches("")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(task, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        tts.speak(task, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        } else
            Log.e("error", "Initilization Failed!");
    }
}