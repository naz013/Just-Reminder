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

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cray.software.justreminder.BuildConfig;
import com.cray.software.justreminder.LogInActivity;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JAction;
import com.cray.software.justreminder.json.JExclusion;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JLed;
import com.cray.software.justreminder.json.JMelody;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.json.JShopping;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.tests.TestActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Application splash screen for checking preferences.
 */
public class SplashScreen extends AppCompatActivity {

    public static final String APP_UI_PREFERENCES = "ui_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Save initial argument on first application run.
     */
    private void initPrefs() {
        File settingsUI = new File("/data/data/" + getPackageName() + "/shared_prefs/" + APP_UI_PREFERENCES + ".xml");
        if(!settingsUI.exists()){
            SharedPreferences appUISettings = getSharedPreferences(APP_UI_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putInt(Prefs.APP_THEME, Configs.DEFAULT_THEME);
            uiEd.putInt(Prefs.TODAY_COLOR, 0);
            uiEd.putInt(Prefs.BIRTH_COLOR, 2);
            uiEd.putInt(Prefs.REMINDER_COLOR, 4);
            uiEd.putInt(Prefs.MAP_TYPE, Constants.MAP_NORMAL);
            uiEd.putString(Prefs.SCREEN, Constants.SCREEN_AUTO);
            uiEd.putString(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
            uiEd.putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
            uiEd.putInt(Prefs.LED_COLOR, LED.BLUE);
            uiEd.putInt(Prefs.BIRTHDAY_LED_COLOR, LED.BLUE);
            uiEd.putInt(Prefs.LOCATION_RADIUS, 25);
            uiEd.putInt(Prefs.MARKER_STYLE, 5);
            uiEd.putInt(Prefs.TRACK_DISTANCE, 1);
            uiEd.putInt(Prefs.TRACK_TIME, 1);
            uiEd.putInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
            uiEd.putInt(Prefs.TEXT_SIZE, 4);
            uiEd.putInt(Prefs.VOLUME, 25);
            uiEd.putInt(Prefs.LAST_CALENDAR_VIEW, 1);

            String localeCheck = Locale.getDefault().toString().toLowerCase();
            int locale;
            if (localeCheck.startsWith("uk")) {
                locale = 2;
            } else if (localeCheck.startsWith("ru")) {
                locale = 1;
            } else locale = 0;

            uiEd.putInt(Prefs.VOICE_LOCALE, locale);
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
            uiEd.putInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
            uiEd.putInt(Prefs.SOUND_STREAM, 5);
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
            uiEd.putBoolean(Prefs.EXPORT_TO_STOCK, false);
            uiEd.putBoolean(Prefs.REMINDERS_IN_CALENDAR, true);
            uiEd.putBoolean(Prefs.IS_24_TIME_FORMAT, true);
            uiEd.putBoolean(Prefs.UNLOCK_DEVICE, false);
            uiEd.putBoolean(Prefs.CALENDAR_FEATURE_TASKS, true);
            uiEd.putBoolean(Prefs.MISSED_CALL_REMINDER, false);
            uiEd.putBoolean(Prefs.QUICK_SMS, false);
            uiEd.putBoolean(Prefs.FOLLOW_REMINDER, false);
            uiEd.putBoolean(Prefs.TTS, false);
            uiEd.putBoolean(Prefs.ITEM_PREVIEW, true);
            uiEd.putBoolean(Prefs.SYNC_BIRTHDAYS, true);
            uiEd.putBoolean(Prefs.BIRTHDAY_PERMANENT, false);
            uiEd.putBoolean(Prefs.REMINDER_CHANGED, false);
            uiEd.putBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
            uiEd.putBoolean(Prefs.SYSTEM_VOLUME, false);
            uiEd.putBoolean(Prefs.INCREASING_VOLUME, false);
            uiEd.putBoolean(Prefs.DAY_NIGHT, false);

            if (Module.isPro()) {
                uiEd.putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(Prefs.LED_STATUS, true);
                uiEd.putInt(Prefs.BIRTHDAY_LED_COLOR, 6);
                uiEd.putInt(Prefs.LED_COLOR, 11);
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
        SharedPrefs prefs = new SharedPrefs(SplashScreen.this);
        if (prefs.loadBoolean(Prefs.EXPORT_SETTINGS)){
            prefs.loadPrefsFromFile();
        }
        initPrefs();

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
            if (c != null) c.close();
            db.close();
            prefs.saveBoolean("isGenB", true);
        }

        if (!prefs.loadBoolean(Prefs.IS_MIGRATION)) {
            try {
                migrateToNewDb();
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
            checkGroups();
            prefs.saveBoolean(Prefs.IS_MIGRATION, true);
        }

        checkPrefs();

        if (BuildConfig.DEBUG) {
            startActivity(new Intent(SplashScreen.this, TestActivity.class));
        } else if (Module.isCloud()) {
            startActivity(new Intent(SplashScreen.this, ScreenManager.class));
        } else {
            if (isFirstTime() && !prefs.loadBoolean(Prefs.CONTACTS_IMPORT_DIALOG)) {
                startActivity(new Intent(SplashScreen.this, LogInActivity.class));
            } else {
                startActivity(new Intent(SplashScreen.this, ScreenManager.class));
            }
        }

        finish();
    }

    private void checkGroups() {
        DataBase DB = new DataBase(this);
        DB.open();
        Cursor cat = DB.queryCategories();
        if (cat == null || cat.getCount() == 0){
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            DB.addCategory("General", time, defUiID, 5);
            DB.addCategory("Work", time, SyncHelper.generateID(), 3);
            DB.addCategory("Personal", time, SyncHelper.generateID(), 0);

            NextBase db = new NextBase(this);
            db.open();
            Cursor c = db.getReminders();
            if (c != null && c.moveToFirst()){
                do {
                    db.setGroup(c.getLong(c.getColumnIndex(NextBase._ID)), defUiID);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
            db.close();
        }
        if (cat != null) cat.close();
        DB.close();
    }

    private void migrateToNewDb() throws SQLiteException{
        stopService(new Intent(SplashScreen.this, GeolocationService.class));
        stopService(new Intent(SplashScreen.this, CheckPosition.class));
        DataBase db = new DataBase(this);
        db.open();
        Cursor c = db.queryAllReminders();
        if (c != null && c.moveToFirst()){
            NextBase nextBase = new NextBase(this);
            nextBase.open();
            AlarmReceiver receiver = new AlarmReceiver();
            do {
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long due = c.getLong(c.getColumnIndex(Constants.COLUMN_FEATURE_TIME));
                long count = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                int expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
                int ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
                int voice = c.getInt(c.getColumnIndex(Constants.COLUMN_VOICE));
                int vibration = c.getInt(c.getColumnIndex(Constants.COLUMN_VIBRATION));
                int notificationRepeat = c.getInt(c.getColumnIndex(Constants.COLUMN_NOTIFICATION_REPEAT));
                int wake = c.getInt(c.getColumnIndex(Constants.COLUMN_WAKE_SCREEN));
                int unlock = c.getInt(c.getColumnIndex(Constants.COLUMN_UNLOCK_DEVICE));
                int auto = c.getInt(c.getColumnIndex(Constants.COLUMN_AUTO_ACTION));
                long limit = c.getLong(c.getColumnIndex(Constants.COLUMN_REPEAT_LIMIT));
                String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                String exclusion = c.getString(c.getColumnIndex(Constants.COLUMN_EXTRA_3));
                double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

                receiver.cancelAlarm(SplashScreen.this, id);

                ArrayList<Integer> listW = new ArrayList<>();
                if (weekdays != null) {
                    listW = ReminderUtils.getRepeatArray(weekdays);
                }

                due = new TimeCount(SplashScreen.this)
                        .generateDateTime(type, myDay, due, repCode * TimeCount.DAY, listW, count, 0);
                if (due < System.currentTimeMillis()) {
                    continue;
                }

                JParser parser = new JParser();
                parser.setCategory(catId);
                parser.setCount(count);
                parser.setAwakeScreen(wake);
                parser.setUnlockScreen(unlock);
                parser.setNotificationRepeat(notificationRepeat);
                parser.setVibration(vibration);
                parser.setVoiceNotification(voice);
                parser.setSummary(text);
                parser.setType(type);
                parser.setEventTime(due);
                parser.setStartTime(due);
                parser.setUuid(uuId);

                if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                    ArrayList<JShopping> list = new ArrayList<>();
                    ArrayList<ShoppingList> shoppingLists =
                            ShoppingListDataProvider.load(SplashScreen.this, id);
                    for (ShoppingList item : shoppingLists){
                        JShopping jShopping = new JShopping(item.getTitle(),
                                item.getIsChecked(), item.getUuId(), item.getTime(), item.getStatus());
                        list.add(jShopping);
                    }
                    parser.setShopping(list);
                }

                JAction jAction = new JAction(type, number, auto, null, null);
                parser.setAction(jAction);

                JExport jExport = new JExport(expTasks, exp, null);
                parser.setExport(jExport);

                JMelody jMelody = new JMelody(melody, -1);
                parser.setMelody(jMelody);

                int status = ledColor != -1 ? 1 : 0;
                JLed jLed = new JLed(ledColor, status);
                parser.setLed(jLed);

                JPlace jPlace = new JPlace(latitude, longitude, radius, -1);
                parser.setPlace(jPlace);

                JExclusion jExclusion = new JExclusion(exclusion);
                parser.setExclusion(jExclusion);

                JRecurrence jRecurrence = new JRecurrence();
                jRecurrence.setWeekdays(listW);
                jRecurrence.setLimit(limit);
                jRecurrence.setMonthday(myDay);
                jRecurrence.setRepeat(repCode * AlarmManager.INTERVAL_DAY);
                parser.setRecurrence(jRecurrence);

                String json = parser.toJsonString();

                long mId = nextBase.insertReminder(text, type, due, uuId, catId, json);
                db.deleteReminder(id);

                receiver.enableReminder(SplashScreen.this, mId);
            } while (c.moveToNext());

            nextBase.close();
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Check if preference exist. If no save default.
     */
    private void checkPrefs(){
        SharedPrefs prefs = new SharedPrefs(SplashScreen.this);
        if (!prefs.isString(Prefs.TODAY_COLOR)){
            prefs.saveInt(Prefs.TODAY_COLOR, 4);
        }
        if (!prefs.isString(Prefs.BIRTH_COLOR)){
            prefs.saveInt(Prefs.BIRTH_COLOR, 1);
        }
        if (!prefs.isString(Prefs.REMINDER_COLOR)){
            prefs.saveInt(Prefs.REMINDER_COLOR, 6);
        }
        if (!prefs.isString(Prefs.APP_THEME)){
            prefs.saveInt(Prefs.APP_THEME, Configs.DEFAULT_THEME);
        }
        if (!prefs.isString(Prefs.SCREEN)){
            prefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_AUTO);
        }
        if (!prefs.isString(Prefs.DRIVE_USER)){
            prefs.savePrefs(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
        }
        if (!prefs.isString(Prefs.TTS_LOCALE)){
            prefs.savePrefs(Prefs.TTS_LOCALE, Language.ENGLISH);
        }
        if (!prefs.isString(Prefs.REMINDER_IMAGE)){
            prefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
        }

        if (!prefs.isString(Prefs.VOICE_LOCALE)){
            prefs.saveInt(Prefs.VOICE_LOCALE, 0);
        }
        if (!prefs.isString(Prefs.TIME_MORNING)){
            prefs.savePrefs(Prefs.TIME_MORNING, "7:0");
        }
        if (!prefs.isString(Prefs.TIME_DAY)){
            prefs.savePrefs(Prefs.TIME_DAY, "12:0");
        }
        if (!prefs.isString(Prefs.TIME_EVENING)){
            prefs.savePrefs(Prefs.TIME_EVENING, "19:0");
        }
        if (!prefs.isString(Prefs.TIME_NIGHT)){
            prefs.savePrefs(Prefs.TIME_NIGHT, "23:0");
        }
        if (!prefs.isString(Prefs.DAYS_TO_BIRTHDAY)){
            prefs.saveInt(Prefs.DAYS_TO_BIRTHDAY, 0);
        }
        if (!prefs.isString(Prefs.QUICK_NOTE_REMINDER_TIME)){
            prefs.saveInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!prefs.isString(Prefs.TEXT_SIZE)){
            prefs.saveInt(Prefs.TEXT_SIZE, 4);
        }
        if (!prefs.isString(Prefs.START_DAY)){
            prefs.saveInt(Prefs.START_DAY, 1);
        }
        if (!prefs.isString(Prefs.BIRTHDAY_REMINDER_HOUR)){
            prefs.saveInt(Prefs.BIRTHDAY_REMINDER_HOUR, 12);
        }
        if (!prefs.isString(Prefs.BIRTHDAY_REMINDER_MINUTE)){
            prefs.saveInt(Prefs.BIRTHDAY_REMINDER_MINUTE, 0);
        }
        if (!prefs.isString(Prefs.TRACK_DISTANCE)){
            prefs.saveInt(Prefs.TRACK_DISTANCE, 1);
        }
        if (!prefs.isString(Prefs.AUTO_BACKUP_INTERVAL)){
            prefs.saveInt(Prefs.AUTO_BACKUP_INTERVAL, 6);
        }
        if (!prefs.isString(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL)){
            prefs.saveInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
        }
        if (!prefs.isString(Prefs.TRACK_TIME)){
            prefs.saveInt(Prefs.TRACK_TIME, 1);
        }
        if (!prefs.isString(Prefs.APP_RUNS_COUNT)){
            prefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
        }
        if (!prefs.isString(Prefs.LAST_CALENDAR_VIEW)){
            prefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 1);
        }
        if (!prefs.isString(Prefs.DELAY_TIME)){
            prefs.saveInt(Prefs.DELAY_TIME, 5);
        }
        if (!prefs.isString(Prefs.EVENT_DURATION)){
            prefs.saveInt(Prefs.EVENT_DURATION, 30);
        }
        if (!prefs.isString(Prefs.NOTIFICATION_REPEAT_INTERVAL)){
            prefs.saveInt(Prefs.NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!prefs.isString(Prefs.VOLUME)){
            prefs.saveInt(Prefs.VOLUME, 25);
        }
        if (!prefs.isString(Prefs.MAP_TYPE)){
            prefs.saveInt(Prefs.MAP_TYPE, Constants.MAP_NORMAL);
        }
        if (!prefs.isString(Prefs.MISSED_CALL_TIME)){
            prefs.saveInt(Prefs.MISSED_CALL_TIME, 10);
        }
        if (!prefs.isString(Prefs.SOUND_STREAM)){
            prefs.saveInt(Prefs.SOUND_STREAM, 5);
        }

        if (!prefs.isString(Prefs.DAY_NIGHT)){
            prefs.saveBoolean(Prefs.DAY_NIGHT, false);
        }
        if (!prefs.isString(Prefs.RATE_SHOW)){
            prefs.saveBoolean(Prefs.RATE_SHOW, false);
        }
        if (!prefs.isString(Prefs.REMINDER_IMAGE_BLUR)){
            prefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
        }
        if (!prefs.isString(Prefs.QUICK_NOTE_REMINDER)){
            prefs.saveBoolean(Prefs.QUICK_NOTE_REMINDER, false);
        }
        if (!prefs.isString(Prefs.SYNC_NOTES)){
            prefs.saveBoolean(Prefs.SYNC_NOTES, true);
        }
        if (!prefs.isString(Prefs.REMINDERS_IN_CALENDAR)){
            prefs.saveBoolean(Prefs.REMINDERS_IN_CALENDAR, false);
        }
        if (!prefs.isString(Prefs.TTS)){
            prefs.saveBoolean(Prefs.TTS, false);
        }
        if (!prefs.isString(Prefs.SYNC_BIRTHDAYS)){
            prefs.saveBoolean(Prefs.SYNC_BIRTHDAYS, true);
        }
        if (!prefs.isString(Prefs.NOTE_ENCRYPT)){
            prefs.saveBoolean(Prefs.NOTE_ENCRYPT, true);
        }
        if (!prefs.isString(Prefs.CONTACTS_IMPORT_DIALOG)){
            prefs.saveBoolean(Prefs.CONTACTS_IMPORT_DIALOG, false);
        }
        if (!prefs.isString(Prefs.CONTACT_BIRTHDAYS)){
            prefs.saveBoolean(Prefs.CONTACT_BIRTHDAYS, false);
        }
        if (!prefs.isString(Prefs.BIRTHDAY_REMINDER)){
            prefs.saveBoolean(Prefs.BIRTHDAY_REMINDER, true);
        }
        if (!prefs.isString(Prefs.CALENDAR_IMAGE)){
            prefs.saveBoolean(Prefs.CALENDAR_IMAGE, false);
        }
        if (!prefs.isString(Prefs.SILENT_SMS)){
            prefs.saveBoolean(Prefs.SILENT_SMS, false);
        }
        if (!prefs.isString(Prefs.ITEM_PREVIEW)){
            prefs.saveBoolean(Prefs.ITEM_PREVIEW, true);
        }
        if (!prefs.isString(Prefs.WIDGET_BIRTHDAYS)){
            prefs.saveBoolean(Prefs.WIDGET_BIRTHDAYS, false);
        }
        if (!prefs.isString(Prefs.WEAR_NOTIFICATION)){
            prefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false);
        }
        if (!prefs.isString(Prefs.EXPORT_TO_STOCK)){
            prefs.saveBoolean(Prefs.EXPORT_TO_STOCK, false);
        }
        if (!prefs.isString(Prefs.USE_DARK_THEME)){
            prefs.saveBoolean(Prefs.USE_DARK_THEME, false);
        }
        if (!prefs.isString(Prefs.EXPORT_TO_CALENDAR)){
            prefs.saveBoolean(Prefs.EXPORT_TO_CALENDAR, false);
        }
        if (!prefs.isString(Prefs.AUTO_CHECK_BIRTHDAYS)){
            prefs.saveBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!prefs.isString(Prefs.INFINITE_VIBRATION)){
            prefs.saveBoolean(Prefs.INFINITE_VIBRATION, false);
        }
        if (!prefs.isString(Prefs.AUTO_BACKUP)){
            prefs.saveBoolean(Prefs.AUTO_BACKUP, false);
        }
        if (!prefs.isString(Prefs.SMART_FOLD)){
            prefs.saveBoolean(Prefs.SMART_FOLD, false);
        }
        if (!prefs.isString(Prefs.NOTIFICATION_REPEAT)){
            prefs.saveBoolean(Prefs.NOTIFICATION_REPEAT, false);
        }
        if (!prefs.isString(Prefs.IS_24_TIME_FORMAT)){
            prefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true);
        }
        if (!prefs.isString(Prefs.UNLOCK_DEVICE)){
            prefs.saveBoolean(Prefs.UNLOCK_DEVICE, false);
        }
        if (!prefs.isString(Prefs.CALENDAR_FEATURE_TASKS)){
            prefs.saveBoolean(Prefs.CALENDAR_FEATURE_TASKS, false);
        }
        if (!prefs.isString(Prefs.MISSED_CALL_REMINDER)){
            prefs.saveBoolean(Prefs.MISSED_CALL_REMINDER, false);
        }
        if (!prefs.isString(Prefs.QUICK_SMS)){
            prefs.saveBoolean(Prefs.QUICK_SMS, false);
        }
        if (!prefs.isString(Prefs.FOLLOW_REMINDER)){
            prefs.saveBoolean(Prefs.FOLLOW_REMINDER, false);
        }
        if (!prefs.isString(Prefs.BIRTHDAY_PERMANENT)){
            prefs.saveBoolean(Prefs.BIRTHDAY_PERMANENT, false);
        }
        if (!prefs.isString(Prefs.REMINDER_CHANGED)){
            prefs.saveBoolean(Prefs.REMINDER_CHANGED, false);
        }
        if (!prefs.isString(Prefs.SYSTEM_VOLUME)){
            prefs.saveBoolean(Prefs.SYSTEM_VOLUME, false);
        }
        if (!prefs.isString(Prefs.INCREASING_VOLUME)){
            prefs.saveBoolean(Prefs.INCREASING_VOLUME, false);
        }

        if (Module.isPro()) {
            if (!prefs.isString(Prefs.LED_STATUS)) {
                prefs.saveBoolean(Prefs.LED_STATUS, true);
            }
            if (!prefs.isString(Prefs.LED_COLOR)) {
                prefs.saveInt(Prefs.LED_COLOR, 11);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_LED_STATUS)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_LED_COLOR)) {
                prefs.saveInt(Prefs.BIRTHDAY_LED_COLOR, 6);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_VIBRATION_STATUS)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_SOUND_STATUS)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_SOUND_STATUS, false);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_WAKE_STATUS)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_INFINITE_SOUND)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, false);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_INFINITE_VIBRATION)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
            }
            if (!prefs.isString(Prefs.BIRTHDAY_USE_GLOBAL)) {
                prefs.saveBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
            }
        } else {
            prefs.saveInt(Prefs.MARKER_STYLE, 5);
        }
    }

    /**
     * Check if application runs first time.
     * @return Boolean
     */
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
