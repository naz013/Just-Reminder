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
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.cray.software.justreminder.BuildConfig;
import com.cray.software.justreminder.LogInActivity;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.json.JAction;
import com.cray.software.justreminder.reminder.json.JExclusion;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JLed;
import com.cray.software.justreminder.reminder.json.JMelody;
import com.cray.software.justreminder.reminder.json.JParser;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JShopping;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.tests.TestActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Application splash screen for checking preferences.
 */
public class SplashScreen extends AppCompatActivity {

    public static final String PREFERENCES_NAME = "ui_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
    }

    /**
     * Save initial argument on first application run.
     */
    private void initPrefs() {
        File settingsUI = new File("/data/data/" + getPackageName() + "/shared_prefs/" + PREFERENCES_NAME + ".xml");
        if(!settingsUI.exists()){
            SharedPreferences appUISettings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
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
            uiEd.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        if (prefs.getBoolean(Prefs.EXPORT_SETTINGS)){
            prefs.loadPrefsFromFile();
        }
        initPrefs();

        if (!prefs.getBoolean("isGenB")){
            for (BirthdayItem item : BirthdayHelper.getInstance(this).getAll()) {
                if (TextUtils.isEmpty(item.getUuId())) {
                    BirthdayHelper.getInstance(this).setUuid(item.getId(), SyncHelper.generateID());
                }
            }
            prefs.putBoolean("isGenB", true);
        }
        if (!prefs.getBoolean(Prefs.IS_MIGRATION)) {
            try {
                migrateToNewDb();
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
            checkGroups();
            prefs.putBoolean(Prefs.IS_MIGRATION, true);
        }
        checkPrefs();
        if (BuildConfig.DEBUG) {
            startActivity(new Intent(SplashScreen.this, TestActivity.class));
        } else if (Module.isCloud()) {
            startActivity(new Intent(SplashScreen.this, ScreenManager.class));
        } else {
            if (isFirstTime() && !prefs.getBoolean(Prefs.CONTACTS_IMPORT_DIALOG)) {
                startActivity(new Intent(SplashScreen.this, LogInActivity.class));
            } else {
                startActivity(new Intent(SplashScreen.this, ScreenManager.class));
            }
        }
        finish();
    }

    private void checkGroups() {
        GroupHelper helper = GroupHelper.getInstance(this);
        if (helper.getAll().size() == 0) {
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            helper.saveGroup(new GroupItem("General", defUiID, 5, 0, time));
            helper.saveGroup(new GroupItem("Work", SyncHelper.generateID(), 3, 0, time));
            helper.saveGroup(new GroupItem("Personal", SyncHelper.generateID(), 0, 0, time));
            for (ReminderItem item : ReminderHelper.getInstance(this).getAll()) {
                item.setGroupId(defUiID);
                ReminderHelper.getInstance(this).saveReminder(item);
            }
        }
    }

    private void migrateToNewDb() throws SQLiteException{
        stopService(new Intent(SplashScreen.this, GeolocationService.class));
        stopService(new Intent(SplashScreen.this, CheckPosition.class));
        DataBase db = new DataBase(this);
        db.open();
        Cursor c = db.queryAllReminders();
        if (c != null && c.moveToFirst()){
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
                due = new TimeCount(SplashScreen.this).generateDateTime(type, myDay, due, repCode * TimeCount.DAY, listW, count, 0);
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
                ReminderItem item = new ReminderItem(text, json, type, uuId, catId, null, 0, 0, 0, 0, 0, due, 0, 0);
                long mId = ReminderHelper.getInstance(this).saveReminder(item);
                db.deleteReminder(id);
                receiver.enableReminder(SplashScreen.this, mId);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Check if preference exist. If no save default.
     */
    private void checkPrefs(){
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        if (!prefs.hasKey(Prefs.TODAY_COLOR)){
            prefs.putInt(Prefs.TODAY_COLOR, 4);
        }
        if (!prefs.hasKey(Prefs.BIRTH_COLOR)){
            prefs.putInt(Prefs.BIRTH_COLOR, 1);
        }
        if (!prefs.hasKey(Prefs.REMINDER_COLOR)){
            prefs.putInt(Prefs.REMINDER_COLOR, 6);
        }
        if (!prefs.hasKey(Prefs.APP_THEME)){
            prefs.putInt(Prefs.APP_THEME, Configs.DEFAULT_THEME);
        }
        if (!prefs.hasKey(Prefs.SCREEN)){
            prefs.putString(Prefs.SCREEN, Constants.SCREEN_AUTO);
        }
        if (!prefs.hasKey(Prefs.DRIVE_USER)){
            prefs.putString(Prefs.DRIVE_USER, Constants.DRIVE_USER_NONE);
        }
        if (!prefs.hasKey(Prefs.TTS_LOCALE)){
            prefs.putString(Prefs.TTS_LOCALE, Language.ENGLISH);
        }
        if (!prefs.hasKey(Prefs.REMINDER_IMAGE)){
            prefs.putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
        }

        if (!prefs.hasKey(Prefs.VOICE_LOCALE)){
            prefs.putInt(Prefs.VOICE_LOCALE, 0);
        }
        if (!prefs.hasKey(Prefs.TIME_MORNING)){
            prefs.putString(Prefs.TIME_MORNING, "7:0");
        }
        if (!prefs.hasKey(Prefs.TIME_DAY)){
            prefs.putString(Prefs.TIME_DAY, "12:0");
        }
        if (!prefs.hasKey(Prefs.TIME_EVENING)){
            prefs.putString(Prefs.TIME_EVENING, "19:0");
        }
        if (!prefs.hasKey(Prefs.TIME_NIGHT)){
            prefs.putString(Prefs.TIME_NIGHT, "23:0");
        }
        if (!prefs.hasKey(Prefs.DAYS_TO_BIRTHDAY)){
            prefs.putInt(Prefs.DAYS_TO_BIRTHDAY, 0);
        }
        if (!prefs.hasKey(Prefs.QUICK_NOTE_REMINDER_TIME)){
            prefs.putInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!prefs.hasKey(Prefs.TEXT_SIZE)){
            prefs.putInt(Prefs.TEXT_SIZE, 4);
        }
        if (!prefs.hasKey(Prefs.START_DAY)){
            prefs.putInt(Prefs.START_DAY, 1);
        }
        if (!prefs.hasKey(Prefs.BIRTHDAY_REMINDER_HOUR)){
            prefs.putInt(Prefs.BIRTHDAY_REMINDER_HOUR, 12);
        }
        if (!prefs.hasKey(Prefs.BIRTHDAY_REMINDER_MINUTE)){
            prefs.putInt(Prefs.BIRTHDAY_REMINDER_MINUTE, 0);
        }
        if (!prefs.hasKey(Prefs.TRACK_DISTANCE)){
            prefs.putInt(Prefs.TRACK_DISTANCE, 1);
        }
        if (!prefs.hasKey(Prefs.AUTO_BACKUP_INTERVAL)){
            prefs.putInt(Prefs.AUTO_BACKUP_INTERVAL, 6);
        }
        if (!prefs.hasKey(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL)){
            prefs.putInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
        }
        if (!prefs.hasKey(Prefs.TRACK_TIME)){
            prefs.putInt(Prefs.TRACK_TIME, 1);
        }
        if (!prefs.hasKey(Prefs.APP_RUNS_COUNT)){
            prefs.putInt(Prefs.APP_RUNS_COUNT, 0);
        }
        if (!prefs.hasKey(Prefs.LAST_CALENDAR_VIEW)){
            prefs.putInt(Prefs.LAST_CALENDAR_VIEW, 1);
        }
        if (!prefs.hasKey(Prefs.DELAY_TIME)){
            prefs.putInt(Prefs.DELAY_TIME, 5);
        }
        if (!prefs.hasKey(Prefs.EVENT_DURATION)){
            prefs.putInt(Prefs.EVENT_DURATION, 30);
        }
        if (!prefs.hasKey(Prefs.NOTIFICATION_REPEAT_INTERVAL)){
            prefs.putInt(Prefs.NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!prefs.hasKey(Prefs.VOLUME)){
            prefs.putInt(Prefs.VOLUME, 25);
        }
        if (!prefs.hasKey(Prefs.MAP_TYPE)){
            prefs.putInt(Prefs.MAP_TYPE, Constants.MAP_NORMAL);
        }
        if (!prefs.hasKey(Prefs.MISSED_CALL_TIME)){
            prefs.putInt(Prefs.MISSED_CALL_TIME, 10);
        }
        if (!prefs.hasKey(Prefs.SOUND_STREAM)){
            prefs.putInt(Prefs.SOUND_STREAM, 5);
        }

        if (!prefs.hasKey(Prefs.DAY_NIGHT)){
            prefs.putBoolean(Prefs.DAY_NIGHT, false);
        }
        if (!prefs.hasKey(Prefs.RATE_SHOW)){
            prefs.putBoolean(Prefs.RATE_SHOW, false);
        }
        if (!prefs.hasKey(Prefs.REMINDER_IMAGE_BLUR)){
            prefs.putBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
        }
        if (!prefs.hasKey(Prefs.QUICK_NOTE_REMINDER)){
            prefs.putBoolean(Prefs.QUICK_NOTE_REMINDER, false);
        }
        if (!prefs.hasKey(Prefs.SYNC_NOTES)){
            prefs.putBoolean(Prefs.SYNC_NOTES, true);
        }
        if (!prefs.hasKey(Prefs.REMINDERS_IN_CALENDAR)){
            prefs.putBoolean(Prefs.REMINDERS_IN_CALENDAR, false);
        }
        if (!prefs.hasKey(Prefs.TTS)){
            prefs.putBoolean(Prefs.TTS, false);
        }
        if (!prefs.hasKey(Prefs.SYNC_BIRTHDAYS)){
            prefs.putBoolean(Prefs.SYNC_BIRTHDAYS, true);
        }
        if (!prefs.hasKey(Prefs.NOTE_ENCRYPT)){
            prefs.putBoolean(Prefs.NOTE_ENCRYPT, true);
        }
        if (!prefs.hasKey(Prefs.CONTACTS_IMPORT_DIALOG)){
            prefs.putBoolean(Prefs.CONTACTS_IMPORT_DIALOG, false);
        }
        if (!prefs.hasKey(Prefs.CONTACT_BIRTHDAYS)){
            prefs.putBoolean(Prefs.CONTACT_BIRTHDAYS, false);
        }
        if (!prefs.hasKey(Prefs.BIRTHDAY_REMINDER)){
            prefs.putBoolean(Prefs.BIRTHDAY_REMINDER, true);
        }
        if (!prefs.hasKey(Prefs.CALENDAR_IMAGE)){
            prefs.putBoolean(Prefs.CALENDAR_IMAGE, false);
        }
        if (!prefs.hasKey(Prefs.SILENT_SMS)){
            prefs.putBoolean(Prefs.SILENT_SMS, false);
        }
        if (!prefs.hasKey(Prefs.ITEM_PREVIEW)){
            prefs.putBoolean(Prefs.ITEM_PREVIEW, true);
        }
        if (!prefs.hasKey(Prefs.WIDGET_BIRTHDAYS)){
            prefs.putBoolean(Prefs.WIDGET_BIRTHDAYS, false);
        }
        if (!prefs.hasKey(Prefs.WEAR_NOTIFICATION)){
            prefs.putBoolean(Prefs.WEAR_NOTIFICATION, false);
        }
        if (!prefs.hasKey(Prefs.EXPORT_TO_STOCK)){
            prefs.putBoolean(Prefs.EXPORT_TO_STOCK, false);
        }
        if (!prefs.hasKey(Prefs.USE_DARK_THEME)){
            prefs.putBoolean(Prefs.USE_DARK_THEME, false);
        }
        if (!prefs.hasKey(Prefs.EXPORT_TO_CALENDAR)){
            prefs.putBoolean(Prefs.EXPORT_TO_CALENDAR, false);
        }
        if (!prefs.hasKey(Prefs.AUTO_CHECK_BIRTHDAYS)){
            prefs.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!prefs.hasKey(Prefs.INFINITE_VIBRATION)){
            prefs.putBoolean(Prefs.INFINITE_VIBRATION, false);
        }
        if (!prefs.hasKey(Prefs.AUTO_BACKUP)){
            prefs.putBoolean(Prefs.AUTO_BACKUP, false);
        }
        if (!prefs.hasKey(Prefs.SMART_FOLD)){
            prefs.putBoolean(Prefs.SMART_FOLD, false);
        }
        if (!prefs.hasKey(Prefs.NOTIFICATION_REPEAT)){
            prefs.putBoolean(Prefs.NOTIFICATION_REPEAT, false);
        }
        if (!prefs.hasKey(Prefs.IS_24_TIME_FORMAT)){
            prefs.putBoolean(Prefs.IS_24_TIME_FORMAT, true);
        }
        if (!prefs.hasKey(Prefs.UNLOCK_DEVICE)){
            prefs.putBoolean(Prefs.UNLOCK_DEVICE, false);
        }
        if (!prefs.hasKey(Prefs.CALENDAR_FEATURE_TASKS)){
            prefs.putBoolean(Prefs.CALENDAR_FEATURE_TASKS, false);
        }
        if (!prefs.hasKey(Prefs.MISSED_CALL_REMINDER)){
            prefs.putBoolean(Prefs.MISSED_CALL_REMINDER, false);
        }
        if (!prefs.hasKey(Prefs.QUICK_SMS)){
            prefs.putBoolean(Prefs.QUICK_SMS, false);
        }
        if (!prefs.hasKey(Prefs.FOLLOW_REMINDER)){
            prefs.putBoolean(Prefs.FOLLOW_REMINDER, false);
        }
        if (!prefs.hasKey(Prefs.BIRTHDAY_PERMANENT)){
            prefs.putBoolean(Prefs.BIRTHDAY_PERMANENT, false);
        }
        if (!prefs.hasKey(Prefs.REMINDER_CHANGED)){
            prefs.putBoolean(Prefs.REMINDER_CHANGED, false);
        }
        if (!prefs.hasKey(Prefs.SYSTEM_VOLUME)){
            prefs.putBoolean(Prefs.SYSTEM_VOLUME, false);
        }
        if (!prefs.hasKey(Prefs.INCREASING_VOLUME)){
            prefs.putBoolean(Prefs.INCREASING_VOLUME, false);
        }

        if (Module.isPro()) {
            if (!prefs.hasKey(Prefs.LED_STATUS)) {
                prefs.putBoolean(Prefs.LED_STATUS, true);
            }
            if (!prefs.hasKey(Prefs.LED_COLOR)) {
                prefs.putInt(Prefs.LED_COLOR, 11);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_LED_STATUS)) {
                prefs.putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_LED_COLOR)) {
                prefs.putInt(Prefs.BIRTHDAY_LED_COLOR, 6);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_VIBRATION_STATUS)) {
                prefs.putBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_SOUND_STATUS)) {
                prefs.putBoolean(Prefs.BIRTHDAY_SOUND_STATUS, false);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_WAKE_STATUS)) {
                prefs.putBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_INFINITE_SOUND)) {
                prefs.putBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, false);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_INFINITE_VIBRATION)) {
                prefs.putBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
            }
            if (!prefs.hasKey(Prefs.BIRTHDAY_USE_GLOBAL)) {
                prefs.putBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
            }
        } else {
            prefs.putInt(Prefs.MARKER_STYLE, 5);
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
            editor.apply();
        }
        return !ranBefore;
    }
}
