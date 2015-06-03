package com.cray.software.justreminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.dialogs.StartHelp;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Language;
import com.cray.software.justreminder.modules.ManageModule;
import com.hexrain.design.ScreenManager;

import java.io.File;
import java.util.Locale;

public class SplashScreen extends Activity{
    SharedPrefs sPrefs;
    TextView textView;
    LinearLayout splashBg;
    ColorSetter cs = new ColorSetter(SplashScreen.this);

    public static final String APP_UI_PREFERENCES = "ui_settings";
    SharedPreferences appUISettings;

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
        if (new ManageModule().isPro()){
            name = getString(R.string.app_name_pro);
        } else name = getString(R.string.app_name);
        textView.setText(name.toUpperCase());
        textView.setTextColor(getResources().getColor(R.color.colorWhite));

        splashBg = (LinearLayout) findViewById(R.id.splashBg);
        splashBg.setBackgroundColor(cs.colorSetter());

        sPrefs = new SharedPrefs(SplashScreen.this);
        File settingsUI = new File("/data/data/" + getPackageName() + "/shared_prefs/" + APP_UI_PREFERENCES + ".xml");
        if(!settingsUI.exists()){
            appUISettings = getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putString(Constants.APP_UI_PREFERENCES_THEME, "6");
            uiEd.putString(Constants.APP_UI_PREFERENCES_CURRENT_COLOR, "1");
            uiEd.putString(Constants.APP_UI_PREFERENCES_BIRTHDAY_COLOR, "3");
            uiEd.putString(Constants.APP_UI_PREFERENCES_REMINDERS_COLOR, "5");
            uiEd.putString(Constants.APP_UI_PREFERENCES_MAP_TYPE, Constants.MAP_TYPE_NORMAL);
            uiEd.putString(Constants.APP_UI_PREFERENCES_SCREEN, Constants.SCREEN_AUTO);
            uiEd.putString(Constants.APP_UI_PREFERENCES_DRIVE_USER, Constants.DRIVE_USER_NONE);
            uiEd.putString(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_LOCATION_RADIUS, 25);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE, 1);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_TRACK_TIME, 1);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER_TIME, 10);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_TEXT_SIZE, 4);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_VOLUME, 25);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);

            String localeCheck = Locale.getDefault().toString().toLowerCase();
            String url;
            if (localeCheck.startsWith("uk")) {
                url = Constants.LANGUAGE_UK;
            } else if (localeCheck.startsWith("ru")) {
                url = Constants.LANGUAGE_RU;
            } else url = Constants.LANGUAGE_EN;

            uiEd.putString(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, url);
            uiEd.putString(Constants.APP_UI_PREFERENCES_TIME_MORNING, "7:0");
            uiEd.putString(Constants.APP_UI_PREFERENCES_TIME_DAY, "12:0");
            uiEd.putString(Constants.APP_UI_PREFERENCES_TIME_EVENING, "19:0");
            uiEd.putString(Constants.APP_UI_PREFERENCES_TIME_NIGHT, "23:0");

            uiEd.putString(Constants.APP_UI_PREFERENCES_TTS_LOCALE, Language.ENGLISH);

            uiEd.putInt(Constants.APP_UI_PREFERENCES_START_DAY, 1);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_DAYS_TO_BIRTHDAY, 0);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL, 15);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, 0);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_DELAY_TIME, 5);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_EVENT_DURATION, 30);
            uiEd.putInt(Constants.APP_UI_PREFERENCES_MISSED_CALL_TIME, 10);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_TRACKING_NOTIFICATION, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_RATE_SHOW, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_IS_CREATE_SHOWN, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_IS_CALENDAR_SHOWN, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_IS_LIST_SHOWN, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_INFINITE_VIBRATION, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_SYNC_NOTES, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_UNLOCK_DEVICE, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_MISSED_CALL_REMINDER, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_FOLLOW_REMINDER, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_TTS, false);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON, true);
            uiEd.putBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW, true);

            if (new ManageModule().isPro()) {
                uiEd.putBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(Constants.APP_UI_PREFERENCES_USE_GLOBAL, true);
                uiEd.putBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_INFINITE_VIBRATION, false);
                uiEd.putBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_VIBRATION_STATUS, false);
                uiEd.putBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_WAKE_STATUS, false);
            }
            uiEd.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPrefs();

        sPrefs = new SharedPrefs(SplashScreen.this);
        if (isFirstTime() && !sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG)) {
            startActivity(new Intent(SplashScreen.this, StartHelp.class));
        } else {
            startActivity(new Intent(SplashScreen.this, ScreenManager.class));
        }
        finish();
    }

    private void checkPrefs(){
        sPrefs = new SharedPrefs(SplashScreen.this);
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_CURRENT_COLOR)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_CURRENT_COLOR, "5");
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_LIST_ORDER)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_COLOR)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_BIRTHDAY_COLOR, "2");
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_REMINDERS_COLOR)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_REMINDERS_COLOR, "5");
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_SCREEN)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_SCREEN, Constants.SCREEN_AUTO);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_DRIVE_USER)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, Constants.DRIVE_USER_NONE);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TTS_LOCALE)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_TTS_LOCALE, Language.ENGLISH);
        }
        String localeCheck = Locale.getDefault().toString().toLowerCase();
        String url;
        if (localeCheck.startsWith("uk")) {
            url = Constants.LANGUAGE_UK;
        } else if (localeCheck.startsWith("ru")) {
            url = Constants.LANGUAGE_RU;
        } else url = Constants.LANGUAGE_EN;
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE, url);
        }

        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TIME_MORNING)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_TIME_MORNING, "7:0");
        }

        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TIME_DAY)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_TIME_DAY, "12:0");
        }

        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TIME_EVENING)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_TIME_EVENING, "19:0");
        }

        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TIME_NIGHT)){
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_TIME_NIGHT, "23:0");
        }

        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_DAYS_TO_BIRTHDAY)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_DAYS_TO_BIRTHDAY, 0);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER_TIME)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TEXT_SIZE)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_TEXT_SIZE, 4);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_START_DAY)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_START_DAY, 1);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR, 12);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE, 0);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE, 1);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TRACK_TIME)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_TRACK_TIME, 1);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, 0);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_DELAY_TIME)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_DELAY_TIME, 5);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_EVENT_DURATION)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_EVENT_DURATION, 30);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_VOLUME)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_VOLUME, 25);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_MISSED_CALL_TIME)){
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_MISSED_CALL_TIME, 10);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_RATE_SHOW)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_RATE_SHOW, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_SYNC_NOTES)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_SYNC_NOTES, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_TTS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_TTS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_ANIMATIONS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_USE_CONTACTS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_SILENT_SMS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_SILENT_SMS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_WEAR_NOTIFICATION)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WEAR_NOTIFICATION, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_SILENT_SMS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_SILENT_SMS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_INFINITE_VIBRATION)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_INFINITE_VIBRATION, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_AUTO_BACKUP)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_AUTO_BACKUP, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_SMART_FOLD)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT, true);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_UNLOCK_DEVICE)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UNLOCK_DEVICE, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_MISSED_CALL_REMINDER)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_MISSED_CALL_REMINDER, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_QUICK_SMS)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS, false);
        }
        if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_FOLLOW_REMINDER)){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_FOLLOW_REMINDER, false);
        }

        if (new ManageModule().isPro()) {
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_LED_STATUS)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_LED_STATUS, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_LED_COLOR)) {
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_STATUS)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_STATUS, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_COLOR)) {
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_LED_COLOR, Constants.ColorConstants.COLOR_BLUE);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_VIBRATION_STATUS)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_VIBRATION_STATUS, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_SOUND_STATUS)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_SOUND_STATUS, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_WAKE_STATUS)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_WAKE_STATUS, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_INFINITE_SOUND)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_INFINITE_SOUND, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_BIRTHDAY_INFINITE_VIBRATION)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_BIRTHDAY_INFINITE_VIBRATION, false);
            }
            if (!sPrefs.isString(Constants.APP_UI_PREFERENCES_USE_GLOBAL)) {
                sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_GLOBAL, true);
            }
        }
    }

    private boolean isFirstTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanGuide", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanGuide", true);
            editor.commit();
        }
        return !ranBefore;
    }
}
