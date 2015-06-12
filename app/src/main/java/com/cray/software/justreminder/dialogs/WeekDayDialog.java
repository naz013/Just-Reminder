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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.async.BackupTask;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Language;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.views.RoundImageView;
import com.cray.software.justreminder.views.TextDrawable;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class WeekDayDialog extends Activity implements TextToSpeech.OnInitListener {
    private static final int MY_DATA_CHECK_CODE = 111;
    FloatingActionButton buttonOk, buttonCancel, buttonCall, buttonDelay, buttonDelayFor,
            buttonNotification, buttonEdit;
    TextView remText;
    RoundImageView contactPhoto;
    LinearLayout single_container;
    WeekDayReceiver alarm = new WeekDayReceiver();
    DelayReceiver delay = new DelayReceiver();
    RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    long id, remCount;
    DataBase DB = new DataBase(WeekDayDialog.this);
    SharedPrefs sPrefs;
    String task;
    int repCode, color = -1;
    BroadcastReceiver deliveredReceiver, sentReceiver;
    Contacts contacts;
    ColorSetter cs = new ColorSetter(WeekDayDialog.this);
    UpdatesHelper updatesHelper;

    Notifier notifier = new Notifier(WeekDayDialog.this);
    String melody, num;
    TextToSpeech tts;
    int currVolume;
    boolean isDark = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPrefs = new SharedPrefs(WeekDayDialog.this);
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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
        contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        sPrefs = new SharedPrefs(WeekDayDialog.this);
        contacts = new Contacts(WeekDayDialog.this);

        isDark = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, buttonEdit);
        int mins = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_DELAY_TIME);
        setTextDrawable(buttonDelay, String.valueOf(mins));
        setTextDrawable(buttonDelayFor, "...");
        if (isDark){
            buttonOk.setIconDrawable(getResources().getDrawable(R.drawable.ic_done_grey600_24dp));
            buttonEdit.setIconDrawable(getResources().getDrawable(R.drawable.ic_create_grey600_24dp));
            buttonCancel.setIconDrawable(getResources().getDrawable(R.drawable.ic_clear_grey600_24dp));
            buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_call_grey600_24dp));
            buttonNotification.setIconDrawable(getResources().getDrawable(R.drawable.ic_favorite_grey600_24dp));
        } else {
            buttonOk.setIconDrawable(getResources().getDrawable(R.drawable.ic_done_white_24dp));
            buttonEdit.setIconDrawable(getResources().getDrawable(R.drawable.ic_create_white_24dp));
            buttonCancel.setIconDrawable(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
            buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_call_white_24dp));
            buttonNotification.setIconDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        }

        DB.open();
        Cursor r = DB.getTask(id);
        String type = "";
        if (r != null && r.moveToFirst()) {
            task = r.getString(r.getColumnIndex(Constants.COLUMN_TEXT));
            type = r.getString(r.getColumnIndex(Constants.COLUMN_TYPE));
            num = r.getString(r.getColumnIndex(Constants.COLUMN_NUMBER));
            melody = r.getString(r.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            repCode = r.getInt(r.getColumnIndex(Constants.COLUMN_REPEAT));
            remCount = r.getLong(r.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            color = r.getInt(r.getColumnIndex(Constants.COLUMN_LED_COLOR));
        } else Log.d(Constants.LOG_TAG, "--------------- nullable cursor ");

        sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_REMINDER_TYPE, type);
        sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_REMINDER_TEXT, task);
        sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_REMINDER_NUMBER, num);

        remText = (TextView) findViewById(R.id.remText);
        remText.setText("");
        if (type.matches(Constants.TYPE_WEEKDAY_CALL) || type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
            String number = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_NUMBER);
            contactPhoto.setVisibility(View.VISIBLE);
            long conID = contacts.getContactIDFromNumber(number, WeekDayDialog.this);
            Bitmap photo = contacts.openPhoto(conID);
            if (photo != null) {
                contactPhoto.setImageBitmap(photo);
            } else {
                contactPhoto.setVisibility(View.GONE);
            }
            remText.setText(task + "\n" + number);
        } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE) || type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SILENT_SMS)) {
                String number = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_NUMBER);
                remText.setText(task + "\n" + number);
                buttonCall.setVisibility(View.VISIBLE);
                if (isDark) buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_send_grey600_24dp));
                else buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_send_white_24dp));
            } else {
                String number = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_NUMBER);
                remText.setText(task + "\n" + number);
                buttonCall.setVisibility(View.GONE);
                buttonDelay.setVisibility(View.GONE);
                buttonDelayFor.setVisibility(View.GONE);
            }
        } else if (type.matches(Constants.TYPE_WEEKDAY)) {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
        } else {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
        }

        wakeScreen();
        DB.updateDateTime(id);

        buttonCancel.setVisibility(View.VISIBLE);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancelAlarm(getApplicationContext(), id);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                notifier.discardNotification(id);
                new moveToArchive().execute(id);
                updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                updatesHelper.updateWidget();
                makeBackup();
                DB.setDelay(id, 0);
                removeFlags();
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                generateEvent(id);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                sPrefs = new SharedPrefs(WeekDayDialog.this);
                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION)) {
                    notifier.discardNotification(id);
                }
                updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                updatesHelper.updateWidget();
                makeBackup();
                removeFlags();
                DB.setDelay(id, 0);
                finish();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                generateEvent(id);
                DB.setDelay(id, 0);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                sPrefs = new SharedPrefs(WeekDayDialog.this);
                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION)) {
                    notifier.discardNotification(id);
                }
                updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                updatesHelper.updateWidget();
                makeBackup();
                removeFlags();
                editReminder(id);
                finish();
            }
        });

        buttonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                generateEvent(id);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                sPrefs = new SharedPrefs(WeekDayDialog.this);
                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION)) {
                    notifier.discardNotification(id);
                }
                updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                updatesHelper.updateWidget();
                makeBackup();
                DB.setDelay(id, 0);
                removeFlags();
                if ((task == null || task.trim().matches("")) &&
                        (num != null && !num.trim().matches(""))) notifier.showReminderNotification(num, id);
                else notifier.showReminderNotification(task, id);
                finish();
            }
        });

        buttonDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                generateEvent(id);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                sPrefs = new SharedPrefs(WeekDayDialog.this);
                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION)) {
                    notifier.discardNotification(id);
                }
                delay.setAlarm(WeekDayDialog.this, 2, id);
                int inTime = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_DELAY_TIME);
                DB.setDelay(id, inTime);
                updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                updatesHelper.updateWidget();
                removeFlags();
                makeBackup();
                finish();
            }
        });
        buttonDelayFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                generateEvent(id);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                sPrefs = new SharedPrefs(WeekDayDialog.this);
                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION)) {
                    notifier.discardNotification(id);
                }
                showDialog();
                updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                updatesHelper.updateWidget();

                makeBackup();
            }
        });
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifier.discardNotification(id);
                repeater.cancelAlarm(WeekDayDialog.this, id);
                generateEvent(id);
                String number = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_NUMBER);
                String typePrefs = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_TYPE);
                String task = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_TEXT);
                if (typePrefs.matches(Constants.TYPE_LOCATION_MESSAGE) || typePrefs.matches(Constants.TYPE_MESSAGE)) {
                    sendSMS(number, task, melody);
                } else {
                    makeCall(number);
                }
                makeBackup();
                removeFlags();
                DB.setDelay(id, 0);
                if (!typePrefs.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                    updatesHelper = new UpdatesHelper(WeekDayDialog.this);
                    updatesHelper.updateWidget();
                    finish();
                }
            }
        });
        String number = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_NUMBER);
        String typePrefs = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_TYPE);
        String text = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_TEXT);
        if (typePrefs.matches(Constants.TYPE_MESSAGE) || typePrefs.matches(Constants.TYPE_LOCATION_MESSAGE)) {
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SILENT_SMS)) {
                sendSMS(number, text, melody);
            } else {
                showNotification(text, 1, id, melody, color);
            }
        } else {
            showNotification(text, 1, id, melody, color);
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT)) {
            repeater.setAlarm(WeekDayDialog.this, id);
        }

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TTS)) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    private void editReminder(long id){
        Intent intentId = new Intent(this, ReminderManager.class);
        if (id != 0) {
            intentId.putExtra(Constants.EDIT_ID, id);
            new AlarmReceiver().cancelAlarm(this, id);
            new WeekDayReceiver().cancelAlarm(this, id);
            new MonthDayReceiver().cancelAlarm(this, id);
            new DelayReceiver().cancelAlarm(this, id);
            new PositionDelayReceiver().cancelDelay(this, id);
            startActivity(intentId);
        }
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

    private void showNotification(final String taskN, int i, long itemId, String melodyN, int colorN){
        sPrefs = new SharedPrefs(WeekDayDialog.this);
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_TTS)) {
            notifier.showNotification(taskN, i, itemId, melodyN, colorN);
        } else {
            notifier.showTTSNotification(taskN, itemId, colorN);
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

    private void generateEvent(long id){
        DB = new DataBase(WeekDayDialog.this);
        DB.open();
        Cursor c = DB.getTask(id);
        if (c != null && c.moveToFirst()){
            String text = "";
            String type = "";
            String weekdays = "";
            int hour = 0;
            int minute = 0;
            int day = 0;
            int exp = 0;
            Cursor t = DB.getTask(id);
            if (t != null && t.moveToNext()) {
                text = t.getString(t.getColumnIndex(Constants.COLUMN_TEXT));
                type = t.getString(t.getColumnIndex(Constants.COLUMN_TYPE));
                weekdays = t.getString(t.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                hour = t.getInt(t.getColumnIndex(Constants.COLUMN_HOUR));
                day = t.getInt(t.getColumnIndex(Constants.COLUMN_DAY));
                minute = t.getInt(t.getColumnIndex(Constants.COLUMN_MINUTE));
                exp = t.getInt(t.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            }
            long nextDate = TimeCount.getNextWeekdayTime(hour, minute, weekdays, 0);
            if (type.startsWith(Constants.TYPE_MONTHDAY)) nextDate = TimeCount.getNextMonthDayTime(hour, minute, day, 0);
            sPrefs = new SharedPrefs(WeekDayDialog.this);
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && exp == 1) {
                exportToCalendar(text, nextDate, id);
            }
        }
    }

    private void exportToCalendar(String summary, long startTime, long id){
        sPrefs = new SharedPrefs(WeekDayDialog.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            new CalendarManager(WeekDayDialog.this).addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            new CalendarManager(WeekDayDialog.this).addEventToStock(summary, startTime);
        }
    }

    public void wakeScreen() {
        sPrefs = new SharedPrefs(WeekDayDialog.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WAKE_STATUS)) {
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

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    private void showDialog(){
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
                delay.setAlarm(WeekDayDialog.this, 2, id, x);
                if (!DB.isOpen()) DB.open();
                DB.setDelay(id, x);

                if (x < 100) {
                    Toast.makeText(WeekDayDialog.this, getString(R.string.repeat_toast_start) + " " +
                            x + " " + getString(R.string.repeat_toast_end), Toast.LENGTH_SHORT).show();
                }
                if (x > 120 && x < 60 * 24) {
                    Toast.makeText(WeekDayDialog.this, getString(R.string.repeat_toast_start) + " " +
                            (x / 60) + " " + getString(R.string.string_hours), Toast.LENGTH_SHORT).show();
                }
                if (x >= 60 * 24 && x < 60 * 24 * 7) {
                    Toast.makeText(WeekDayDialog.this, getString(R.string.repeat_toast_start) + " " +
                            (x / (60 * 24)) + " " + getString(R.string.string_days), Toast.LENGTH_SHORT).show();
                }
                if (x == 60 * 24 * 7) {
                    Toast.makeText(WeekDayDialog.this, getString(R.string.repeat_toast_start) + " " +
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

    private void makeBackup(){
        sPrefs = new SharedPrefs(WeekDayDialog.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_BACKUP)){
            new BackupTask(WeekDayDialog.this).execute();
        }
    }

    private void sendSMS(String phoneNumber, String message, final String melody) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(WeekDayDialog.this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(WeekDayDialog.this,
                0, new Intent(DELIVERED), 0);

        registerReceiver(sentReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                sPrefs = new SharedPrefs(WeekDayDialog.this);
                String text = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDER_TEXT);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        remText.setText(getString(R.string.dialog_message_sent));
                        notifier.showNotification(text, 1, id, melody, color);
                        if (buttonCall.getVisibility() == View.VISIBLE) {
                            buttonCall.setVisibility(View.GONE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        notifier.showNotification(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        notifier.showNotification(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        notifier.showNotification(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_white_24dp));
                        if (buttonCall.getVisibility() == View.GONE) {
                            buttonCall.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        notifier.showNotification(text, 0, id, melody, color);
                        remText.setText(getString(R.string.message_send_error));
                        if (isDark) buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_grey600_24dp));
                        else buttonCall.setIconDrawable(getResources().getDrawable(R.drawable.ic_cached_white_24dp));
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
                        Toast.makeText(WeekDayDialog.this, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(WeekDayDialog.this, "SMS not delivered",
                        Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

    }

    private void makeCall(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()){
            notifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    private void makeArchive(long id){
        DB = new DataBase(WeekDayDialog.this);
        DB.open();
        DB.setDone(id);
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
        sPrefs = new SharedPrefs(WeekDayDialog.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD)){
            updatesHelper = new UpdatesHelper(WeekDayDialog.this);
            updatesHelper.updateWidget();
            moveTaskToBack(true);
            repeater.cancelAlarm(WeekDayDialog.this, id);
            removeFlags();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.must_click_message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        sPrefs = new SharedPrefs(WeekDayDialog.this);
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(new Language().getLocale(WeekDayDialog.this));
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
            Log.e("error", "Initialization Failed!");
    }

    class moveToArchive extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {
            long i = 0;
            if (params.length > 0){
                i = params[0];
            }
            makeArchive(i);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updatesHelper = new UpdatesHelper(WeekDayDialog.this);
            updatesHelper.updateWidget();
            notifier.recreatePermanent();
        }
    }
}