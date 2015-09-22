package com.cray.software.justreminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.async.GetExchangeTasksAsync;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.StartHelp;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Language;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.hexrain.design.ScreenManager;

import java.io.File;
import java.util.Locale;

public class SplashScreen extends Activity{
    SharedPrefs sPrefs;
    TextView textView;
    LinearLayout splashBg;
    ColorSetter cs = new ColorSetter(SplashScreen.this);

    public static final String APP_UI_PREFERENCES = "ui_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.splash_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setRequestedOrientation(cs.getRequestOrientation());

        textView = (TextView) findViewById(R.id.textView);
        String name;
        if (Module.isPro()){
            name = getString(R.string.app_name_pro);
        } else name = getString(R.string.app_name);
        textView.setText(name.toUpperCase());
        textView.setTextColor(getResources().getColor(R.color.colorWhite));

        splashBg = (LinearLayout) findViewById(R.id.splashBg);
        splashBg.setBackgroundColor(cs.colorSetter());

        sPrefs = new SharedPrefs(SplashScreen.this);
        if (SyncHelper.isSdPresent()){
            sPrefs.loadPrefsFromFile();
        }
        initPrefs();
    }

    private void initPrefs() {
        File settingsUI = new File("/data/data/" + getPackageName() + "/shared_prefs/" + APP_UI_PREFERENCES + ".xml");
        if(!settingsUI.exists()){
            SharedPreferences appUISettings = getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putString(Prefs.THEME, "6");
            uiEd.putString(Prefs.CURRENT_COLOR, "1");
            uiEd.putString(Prefs.BIRTHDAY_COLOR, "3");
            uiEd.putString(Prefs.REMINDERS_COLOR, "5");
            uiEd.putString(Prefs.MAP_TYPE, Constants.MAP_TYPE_NORMAL);
            uiEd.putString(Prefs.SCREEN, Constants.SCREEN_AUTO);
            uiEd.putString(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
            uiEd.putString(Prefs.LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
            uiEd.putInt(Prefs.LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            uiEd.putInt(Prefs.BIRTHDAY_LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            uiEd.putInt(Prefs.LOCATION_RADIUS, 25);
            uiEd.putInt(Prefs.TRACK_DISTANCE, 1);
            uiEd.putInt(Prefs.TRACK_TIME, 1);
            uiEd.putInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
            uiEd.putInt(Prefs.TEXT_SIZE, 4);
            uiEd.putInt(Prefs.VOLUME, 25);
            uiEd.putInt(Prefs.LAST_CALENDAR_VIEW, 1);

            String localeCheck = Locale.getDefault().toString().toLowerCase();
            String url;
            if (localeCheck.startsWith("uk")) {
                url = Constants.LANGUAGE_UK;
            } else if (localeCheck.startsWith("ru")) {
                url = Constants.LANGUAGE_RU;
            } else url = Constants.LANGUAGE_EN;

            uiEd.putString(Prefs.VOICE_LANGUAGE, url);
            uiEd.putString(Prefs.TIME_MORNING, "7:0");
            uiEd.putString(Prefs.TIME_DAY, "12:0");
            uiEd.putString(Prefs.TIME_EVENING, "19:0");
            uiEd.putString(Prefs.TIME_NIGHT, "23:0");

            uiEd.putString(Prefs.TTS_LOCALE, Language.ENGLISH);

            uiEd.putInt(Prefs.START_DAY, 1);
            uiEd.putInt(Prefs.DAYS_TO_BIRTHDAY, 0);
            uiEd.putInt(Prefs.NOTIFICATION_REPEAT_INTERVAL, 15);
            uiEd.putInt(Prefs.APP_RUNS_COUNT, 0);
            uiEd.putInt(Prefs.DELAY_TIME, 5);
            uiEd.putInt(Prefs.EVENT_DURATION, 30);
            uiEd.putInt(Prefs.MISSED_CALL_TIME, 10);
            uiEd.putInt(Prefs.AUTO_BACKUP_INTERVAL, 6);
            uiEd.putBoolean(Prefs.TRACKING_NOTIFICATION, true);
            uiEd.putBoolean(Prefs.RATE_SHOW, false);
            uiEd.putBoolean(Prefs.IS_CREATE_SHOWN, false);
            uiEd.putBoolean(Prefs.IS_CALENDAR_SHOWN, false);
            uiEd.putBoolean(Prefs.IS_LIST_SHOWN, false);
            uiEd.putBoolean(Prefs.CONTACT_BIRTHDAYS, false);
            uiEd.putBoolean(Prefs.BIRTHDAY_REMINDER, true);
            uiEd.putBoolean(Prefs.CALENDAR_IMAGE, false);
            uiEd.putBoolean(Prefs.USE_DARK_THEME, false);
            uiEd.putBoolean(Prefs.EXPORT_TO_CALENDAR, false);
            uiEd.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
            uiEd.putBoolean(Prefs.INFINITE_VIBRATION, false);
            uiEd.putBoolean(Prefs.NOTIFICATION_REPEAT, false);
            uiEd.putBoolean(Prefs.WIDGET_BIRTHDAYS, false);
            uiEd.putBoolean(Prefs.QUICK_NOTE_REMINDER, false);
            uiEd.putBoolean(Prefs.NOTE_ENCRYPT, true);
            uiEd.putBoolean(Prefs.SYNC_NOTES, true);
            uiEd.putBoolean(Prefs.ANIMATIONS, true);
            uiEd.putBoolean(Prefs.AUTO_LANGUAGE, true);
            uiEd.putBoolean(Prefs.EXPORT_TO_STOCK, false);
            uiEd.putBoolean(Prefs.HIDE_TRANSLATION_MENU, false);
            uiEd.putBoolean(Prefs.REMINDERS_IN_CALENDAR, true);
            uiEd.putBoolean(Prefs.IS_24_TIME_FORMAT, true);
            uiEd.putBoolean(Prefs.UNLOCK_DEVICE, false);
            uiEd.putBoolean(Prefs.CALENDAR_FEATURE_TASKS, true);
            uiEd.putBoolean(Prefs.MISSED_CALL_REMINDER, false);
            uiEd.putBoolean(Prefs.QUICK_SMS, false);
            uiEd.putBoolean(Prefs.FOLLOW_REMINDER, false);
            uiEd.putBoolean(Prefs.TTS, false);
            uiEd.putBoolean(Prefs.EXTENDED_BUTTON, true);
            uiEd.putBoolean(Prefs.ITEM_PREVIEW, true);
            uiEd.putBoolean(Prefs.SYNC_BIRTHDAYS, true);

            if (Module.isPro()) {
                uiEd.putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
                uiEd.putBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
                uiEd.putBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
                uiEd.putBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            }
            uiEd.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPrefs prefs = new SharedPrefs(this);
        if (!prefs.loadBoolean("isGen")){
            DataBase db = new DataBase(this);
            db.open();
            Cursor c = db.queryGroup();
            if (c != null && c.moveToFirst()){
                do {
                    long time = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    if(time < 1000) db.updateReminderAfterTime(id, time * TimeCount.minute);
                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }
            db.close();
            prefs.saveBoolean("isGen", true);
        }

        if (!prefs.loadBoolean("isGenB")){
            DataBase db = new DataBase(this);
            db.open();
            Cursor c = db.getBirthdays();
            if (c != null && c.moveToFirst()){
                do {
                    String id = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_UUID));
                    if (id == null || id.matches("")){
                        String uuId = SyncHelper.generateID();
                        db.updateOtherInformationEvent(
                                c.getLong(c.getColumnIndex(Constants.ContactConstants.COLUMN_ID)),
                                uuId);
                    }
                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }
            db.close();
            prefs.saveBoolean("isGenB", true);
        }

        checkPrefs();

        sPrefs = new SharedPrefs(SplashScreen.this);
        if (isFirstTime() && !sPrefs.loadBoolean(Prefs.CONTACTS_IMPORT_DIALOG)) {
            startActivity(new Intent(SplashScreen.this, StartHelp.class));
        } else {
            startActivity(new Intent(SplashScreen.this, ScreenManager.class));
        }

        new GetExchangeTasksAsync(this, null).execute();

        finish();
    }

    private void checkPrefs(){
        sPrefs = new SharedPrefs(SplashScreen.this);
        if (!sPrefs.isString(Prefs.CURRENT_COLOR)){
            sPrefs.savePrefs(Prefs.CURRENT_COLOR, "5");
        }
        if (!sPrefs.isString(Prefs.LIST_ORDER)){
            sPrefs.savePrefs(Prefs.LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
        }
        if (!sPrefs.isString(Prefs.BIRTHDAY_COLOR)){
            sPrefs.savePrefs(Prefs.BIRTHDAY_COLOR, "2");
        }
        if (!sPrefs.isString(Prefs.REMINDERS_COLOR)){
            sPrefs.savePrefs(Prefs.REMINDERS_COLOR, "5");
        }
        if (!sPrefs.isString(Prefs.SCREEN)){
            sPrefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_AUTO);
        }
        if (!sPrefs.isString(Prefs.DRIVE_USER)){
            sPrefs.savePrefs(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
        }
        if (!sPrefs.isString(Prefs.TTS_LOCALE)){
            sPrefs.savePrefs(Prefs.TTS_LOCALE, Language.ENGLISH);
        }
        String localeCheck = Locale.getDefault().toString().toLowerCase();
        String url;
        if (localeCheck.startsWith("uk")) {
            url = Constants.LANGUAGE_UK;
        } else if (localeCheck.startsWith("ru")) {
            url = Constants.LANGUAGE_RU;
        } else url = Constants.LANGUAGE_EN;
        if (!sPrefs.isString(Prefs.VOICE_LANGUAGE)){
            sPrefs.savePrefs(Prefs.VOICE_LANGUAGE, url);
        }

        if (!sPrefs.isString(Prefs.TIME_MORNING)){
            sPrefs.savePrefs(Prefs.TIME_MORNING, "7:0");
        }

        if (!sPrefs.isString(Prefs.TIME_DAY)){
            sPrefs.savePrefs(Prefs.TIME_DAY, "12:0");
        }

        if (!sPrefs.isString(Prefs.TIME_EVENING)){
            sPrefs.savePrefs(Prefs.TIME_EVENING, "19:0");
        }

        if (!sPrefs.isString(Prefs.TIME_NIGHT)){
            sPrefs.savePrefs(Prefs.TIME_NIGHT, "23:0");
        }

        if (!sPrefs.isString(Prefs.DAYS_TO_BIRTHDAY)){
            sPrefs.saveInt(Prefs.DAYS_TO_BIRTHDAY, 0);
        }
        if (!sPrefs.isString(Prefs.QUICK_NOTE_REMINDER_TIME)){
            sPrefs.saveInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!sPrefs.isString(Prefs.TEXT_SIZE)){
            sPrefs.saveInt(Prefs.TEXT_SIZE, 4);
        }
        if (!sPrefs.isString(Prefs.START_DAY)){
            sPrefs.saveInt(Prefs.START_DAY, 1);
        }
        if (!sPrefs.isString(Prefs.BIRTHDAY_REMINDER_HOUR)){
            sPrefs.saveInt(Prefs.BIRTHDAY_REMINDER_HOUR, 12);
        }
        if (!sPrefs.isString(Prefs.BIRTHDAY_REMINDER_MINUTE)){
            sPrefs.saveInt(Prefs.BIRTHDAY_REMINDER_MINUTE, 0);
        }
        if (!sPrefs.isString(Prefs.TRACK_DISTANCE)){
            sPrefs.saveInt(Prefs.TRACK_DISTANCE, 1);
        }
        if (!sPrefs.isString(Prefs.AUTO_BACKUP_INTERVAL)){
            sPrefs.saveInt(Prefs.AUTO_BACKUP_INTERVAL, 6);
        }
        if (!sPrefs.isString(Prefs.TRACK_TIME)){
            sPrefs.saveInt(Prefs.TRACK_TIME, 1);
        }
        if (!sPrefs.isString(Prefs.APP_RUNS_COUNT)){
            sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
        }
        if (!sPrefs.isString(Prefs.LAST_CALENDAR_VIEW)){
            sPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 1);
        }
        if (!sPrefs.isString(Prefs.DELAY_TIME)){
            sPrefs.saveInt(Prefs.DELAY_TIME, 5);
        }
        if (!sPrefs.isString(Prefs.EVENT_DURATION)){
            sPrefs.saveInt(Prefs.EVENT_DURATION, 30);
        }
        if (!sPrefs.isString(Prefs.NOTIFICATION_REPEAT_INTERVAL)){
            sPrefs.saveInt(Prefs.NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!sPrefs.isString(Prefs.VOLUME)){
            sPrefs.saveInt(Prefs.VOLUME, 25);
        }
        if (!sPrefs.isString(Prefs.MISSED_CALL_TIME)){
            sPrefs.saveInt(Prefs.MISSED_CALL_TIME, 10);
        }
        if (!sPrefs.isString(Prefs.RATE_SHOW)){
            sPrefs.saveBoolean(Prefs.RATE_SHOW, false);
        }
        if (!sPrefs.isString(Prefs.AUTO_LANGUAGE)){
            sPrefs.saveBoolean(Prefs.AUTO_LANGUAGE, true);
        }
        if (!sPrefs.isString(Prefs.QUICK_NOTE_REMINDER)){
            sPrefs.saveBoolean(Prefs.QUICK_NOTE_REMINDER, false);
        }
        if (!sPrefs.isString(Prefs.SYNC_NOTES)){
            sPrefs.saveBoolean(Prefs.SYNC_NOTES, true);
        }
        if (!sPrefs.isString(Prefs.REMINDERS_IN_CALENDAR)){
            sPrefs.saveBoolean(Prefs.REMINDERS_IN_CALENDAR, false);
        }
        if (!sPrefs.isString(Prefs.TTS)){
            sPrefs.saveBoolean(Prefs.TTS, false);
        }
        if (!sPrefs.isString(Prefs.ANIMATIONS)){
            sPrefs.saveBoolean(Prefs.ANIMATIONS, true);
        }
        if (!sPrefs.isString(Prefs.SYNC_BIRTHDAYS)){
            sPrefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, true);
        }
        if (!sPrefs.isString(Prefs.NOTE_ENCRYPT)){
            sPrefs.saveBoolean(Prefs.NOTE_ENCRYPT, true);
        }
        if (!sPrefs.isString(Prefs.CONTACTS_IMPORT_DIALOG)){
            sPrefs.saveBoolean(Prefs.CONTACTS_IMPORT_DIALOG, false);
        }
        if (!sPrefs.isString(Prefs.CONTACT_BIRTHDAYS)){
            sPrefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, false);
        }
        if (!sPrefs.isString(Prefs.BIRTHDAY_REMINDER)){
            sPrefs.saveBoolean(Prefs.BIRTHDAY_REMINDER, true);
        }
        if (!sPrefs.isString(Prefs.CALENDAR_IMAGE)){
            sPrefs.saveBoolean(Prefs.CALENDAR_IMAGE, false);
        }
        if (!sPrefs.isString(Prefs.SILENT_SMS)){
            sPrefs.saveBoolean(Prefs.SILENT_SMS, false);
        }
        if (!sPrefs.isString(Prefs.EXTENDED_BUTTON)){
            sPrefs.saveBoolean(Prefs.EXTENDED_BUTTON, true);
        }
        if (!sPrefs.isString(Prefs.ITEM_PREVIEW)){
            sPrefs.saveBoolean(Prefs.ITEM_PREVIEW, true);
        }
        if (!sPrefs.isString(Prefs.WIDGET_BIRTHDAYS)){
            sPrefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, false);
        }
        if (!sPrefs.isString(Prefs.WEAR_NOTIFICATION)){
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false);
        }
        if (!sPrefs.isString(Prefs.EXPORT_TO_STOCK)){
            sPrefs.saveBoolean(Prefs.EXPORT_TO_STOCK, false);
        }
        if (!sPrefs.isString(Prefs.USE_DARK_THEME)){
            sPrefs.saveBoolean(Prefs.USE_DARK_THEME, false);
        }
        if (!sPrefs.isString(Prefs.EXPORT_TO_CALENDAR)){
            sPrefs.saveBoolean(Prefs.EXPORT_TO_CALENDAR, false);
        }
        if (!sPrefs.isString(Prefs.AUTO_CHECK_BIRTHDAYS)){
            sPrefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!sPrefs.isString(Prefs.INFINITE_VIBRATION)){
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, false);
        }
        if (!sPrefs.isString(Prefs.AUTO_BACKUP)){
            sPrefs.saveBoolean(Prefs.AUTO_BACKUP, false);
        }
        if (!sPrefs.isString(Prefs.SMART_FOLD)){
            sPrefs.saveBoolean(Prefs.SMART_FOLD, false);
        }
        if (!sPrefs.isString(Prefs.NOTIFICATION_REPEAT)){
            sPrefs.saveBoolean(Prefs.NOTIFICATION_REPEAT, false);
        }
        if (!sPrefs.isString(Prefs.HIDE_TRANSLATION_MENU)){
            sPrefs.saveBoolean(Prefs.HIDE_TRANSLATION_MENU, false);
        }
        if (!sPrefs.isString(Prefs.IS_24_TIME_FORMAT)){
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true);
        }
        if (!sPrefs.isString(Prefs.UNLOCK_DEVICE)){
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, false);
        }
        if (!sPrefs.isString(Prefs.CALENDAR_FEATURE_TASKS)){
            sPrefs.saveBoolean(Prefs.CALENDAR_FEATURE_TASKS, false);
        }
        if (!sPrefs.isString(Prefs.MISSED_CALL_REMINDER)){
            sPrefs.saveBoolean(Prefs.MISSED_CALL_REMINDER, false);
        }
        if (!sPrefs.isString(Prefs.QUICK_SMS)){
            sPrefs.saveBoolean(Prefs.QUICK_SMS, false);
        }
        if (!sPrefs.isString(Prefs.FOLLOW_REMINDER)){
            sPrefs.saveBoolean(Prefs.FOLLOW_REMINDER, false);
        }

        if (Module.isPro()) {
            if (!sPrefs.isString(Prefs.LED_STATUS)) {
                sPrefs.saveBoolean(Prefs.LED_STATUS, false);
            }
            if (!sPrefs.isString(Prefs.LED_COLOR)) {
                sPrefs.saveInt(Prefs.LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_LED_STATUS)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_LED_COLOR)) {
                sPrefs.saveInt(Prefs.BIRTHDAY_LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_VIBRATION_STATUS)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_SOUND_STATUS)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_SOUND_STATUS, false);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_WAKE_STATUS)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_INFINITE_SOUND)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, false);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_INFINITE_VIBRATION)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
            }
            if (!sPrefs.isString(Prefs.BIRTHDAY_USE_GLOBAL)) {
                sPrefs.saveBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
            }
        }
    }

    private boolean isFirstTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean(Prefs.TECH_ONE, false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Prefs.TECH_ONE, true);
            editor.commit();
        }
        return !ranBefore;
    }
}
