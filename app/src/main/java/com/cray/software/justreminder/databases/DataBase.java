package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.constants.Constants;

public class DataBase {
    private static final String DB_NAME = "just_database";
    private static final int DB_VERSION = 13;
    private static final String CURRENT_TABLE_NAME = "current_task_table";
    private static final String CURRENT_CACHE_TABLE_NAME = "current_cache_task_table";
    private static final String CONTACTS_TABLE_NAME = "contacts_task_table";
    private static final String LOCATION_TABLE_NAME = "locations_table";
    private static final String NOTE_TABLE_NAME = "notes_table";
    private static final String EVENTS_TABLE_NAME = "events_table";
    private static final String CALLS_TABLE_NAME = "calls_table";
    private static final String MESSAGES_TABLE_NAME = "messages_table";
    private static final String CATEGORIES_TABLE_NAME = "categories_table";
    private static final String SHOPPING_TABLE_NAME = "shopping_table";

    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String CURRENT_TABLE_CREATE =
            "create table " + CURRENT_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_TYPE + " VARCHAR(255), " +
                    Constants.COLUMN_DAY + " INTEGER, " +
                    Constants.COLUMN_IS_DONE + " INTEGER, " +
                    Constants.COLUMN_CUSTOM_RADIUS + " INTEGER, " +
                    Constants.COLUMN_ARCHIVED + " INTEGER, " +
                    Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER, " +
                    Constants.COLUMN_MONTH + " INTEGER, " +
                    Constants.COLUMN_YEAR + " INTEGER, " +
                    Constants.COLUMN_HOUR + " INTEGER, " +
                    Constants.COLUMN_MINUTE + " INTEGER, " +
                    Constants.COLUMN_SECONDS + " INTEGER, " +
                    Constants.COLUMN_NUMBER + " VARCHAR(255), " +
                    Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255), " +
                    Constants.COLUMN_REMIND_TIME + " INTEGER, " +
                    Constants.COLUMN_REPEAT + " INTEGER, " +
                    Constants.COLUMN_REMINDERS_COUNT + " INTEGER, " +
                    Constants.COLUMN_LED_COLOR + " INTEGER, " +
                    Constants.COLUMN_SYNC_CODE + " INTEGER, " +
                    Constants.COLUMN_LATITUDE + " REAL, " +
                    Constants.COLUMN_LONGITUDE + " REAL, " +
                    Constants.COLUMN_FEATURE_TIME + " INTEGER, " +
                    Constants.COLUMN_DELAY + " INTEGER, " +
                    Constants.COLUMN_VIBRATION + " INTEGER, " +
                    Constants.COLUMN_VOICE + " INTEGER, " +
                    Constants.COLUMN_AUTO_ACTION + " INTEGER, " +
                    Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER, " +
                    Constants.COLUMN_WAKE_SCREEN + " INTEGER, " +
                    Constants.COLUMN_UNLOCK_DEVICE + " INTEGER, " +
                    Constants.COLUMN_REPEAT_LIMIT + " INTEGER, " +
                    Constants.COLUMN_EXTRA_1 + " INTEGER, " +
                    Constants.COLUMN_EXTRA_2 + " INTEGER, " +
                    Constants.COLUMN_TECH_VAR + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_3 + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_4 + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_5 + " VARCHAR(255), " +
                    Constants.COLUMN_DATE_TIME + " VARCHAR(255), " +
                    Constants.COLUMN_CATEGORY + " VARCHAR(255), " +
                    Constants.COLUMN_WEEKDAYS + " VARCHAR(255) " +
                    ");";

    private static final String CONTACTS_TABLE_CREATE =
            "create table " + CONTACTS_TABLE_NAME + "(" +
                    Constants.ContactConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.ContactConstants.COLUMN_CONTACT_NAME + " VARCHAR(255), " +
                    Constants.ContactConstants.COLUMN_CONTACT_ID + " INTEGER, " +
                    Constants.ContactConstants.COLUMN_CONTACT_NUMBER + " VARCHAR(255), " +
                    Constants.ContactConstants.COLUMN_CONTACT_MAIL + " VARCHAR(255), " +
                    Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY + " VARCHAR(255), " +
                    Constants.ContactConstants.COLUMN_CONTACT_DAY + " INTEGER, " +
                    Constants.ContactConstants.COLUMN_CONTACT_MONTH + " INTEGER, " +
                    Constants.ContactConstants.COLUMN_CONTACT_UUID + " VARCHAR(255), " +
                    Constants.ContactConstants.COLUMN_CONTACT_VAR + " VARCHAR(255) " +
                    ");";

    private static final String LOCATION_TABLE_CREATE =
            "create table " + LOCATION_TABLE_NAME + "(" +
                    Constants.LocationConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.LocationConstants.COLUMN_LOCATION_NAME + " VARCHAR(255), " +
                    Constants.LocationConstants.COLUMN_LOCATION_LATITUDE + " REAL, " +
                    Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE + " REAL, " +
                    Constants.LocationConstants.COLUMN_LOCATION_TECH + " VARCHAR(255), " +
                    Constants.LocationConstants.COLUMN_LOCATION_TECH1+ " VARCHAR(255), " +
                    Constants.LocationConstants.COLUMN_LOCATION_TECH2 + " INTEGER, " +
                    Constants.LocationConstants.COLUMN_LOCATION_VAR + " INTEGER, " +
                    Constants.LocationConstants.COLUMN_LOCATION_VAR1 + " REAL, " +
                    Constants.LocationConstants.COLUMN_LOCATION_VAR2 + " REAL " +
                    ");";

    private static final String EVENTS_TABLE_CREATE =
            "create table " + EVENTS_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_DELETE_URI + " VARCHAR(255), " +
                    Constants.COLUMN_REMINDER_ID + " INTEGER, " +
                    Constants.COLUMN_EVENT_VAR + " VARCHAR(255), " +
                    Constants.COLUMN_EVENT_ID + " INTEGER, " +
                    Constants.COLUMN_EVENT_TECH + " VARCHAR(255) " +
                    ");";

    private static final String CALLS_TABLE_CREATE =
            "create table " + CALLS_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_NUMBER + " VARCHAR(255), " +
                    Constants.COLUMN_DATE_TIME + " INTEGER " +
                    ");";

    private static final String MESSAGES_TABLE_CREATE =
            "create table " + MESSAGES_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_DATE_TIME + " INTEGER " +
                    ");";

    private static final String CATEGORIES_TABLE_CREATE =
            "create table " + CATEGORIES_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_COLOR + " INTEGER, " +
                    Constants.COLUMN_TECH_VAR + " VARCHAR(255), " +
                    Constants.COLUMN_CATEGORY + " VARCHAR(255), " +
                    Constants.COLUMN_FEATURE_TIME + " INTEGER, " +
                    Constants.COLUMN_DELAY + " INTEGER, " +
                    Constants.COLUMN_DATE_TIME + " INTEGER " +
                    ");";

    private static final String SHOPPING_TABLE_CREATE =
            "create table " + SHOPPING_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_COLOR + " INTEGER, " +
                    Constants.COLUMN_TECH_VAR + " VARCHAR(255), " +
                    Constants.COLUMN_CATEGORY + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_3 + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_4 + " VARCHAR(255), " +
                    Constants.COLUMN_REMINDER_ID + " INTEGER, " +
                    Constants.COLUMN_ARCHIVED + " INTEGER, " +
                    Constants.COLUMN_EXTRA_1 + " INTEGER, " +
                    Constants.COLUMN_EXTRA_2 + " INTEGER, " +
                    Constants.COLUMN_DATE_TIME + " INTEGER " +
                    ");";

    public class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CURRENT_TABLE_CREATE);
            sqLiteDatabase.execSQL(CONTACTS_TABLE_CREATE);
            sqLiteDatabase.execSQL(LOCATION_TABLE_CREATE);
            sqLiteDatabase.execSQL(EVENTS_TABLE_CREATE);
            sqLiteDatabase.execSQL(CALLS_TABLE_CREATE);
            sqLiteDatabase.execSQL(MESSAGES_TABLE_CREATE);
            sqLiteDatabase.execSQL(CATEGORIES_TABLE_CREATE);
            sqLiteDatabase.execSQL(SHOPPING_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion){
                case 1:
                    db.execSQL(CONTACTS_TABLE_CREATE);
                    db.execSQL(LOCATION_TABLE_CREATE);
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 2:
                    db.execSQL(LOCATION_TABLE_CREATE);
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 3:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 4:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    db.execSQL("DELETE FROM " + NOTE_TABLE_NAME);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 5:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 6:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 7:
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 8:
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 10:
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    db.execSQL("DELETE FROM " + CURRENT_CACHE_TABLE_NAME);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 11:
                    db.execSQL(SHOPPING_TABLE_CREATE);
                    //
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
                case 12:
                    //Add new fields to shopping table
                    db.execSQL("ALTER TABLE " + SHOPPING_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + SHOPPING_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + SHOPPING_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + SHOPPING_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    break;
            }
        }
    }

    public DataBase(Context c) {
        mContext = c;
    }

    public DataBase open() throws SQLiteException {
        dbHelper = new DBHelper(mContext);

        db = dbHelper.getWritableDatabase();

        System.gc();
        return this;
    }

    public boolean isOpen () {
        return db != null && db.isOpen();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void close() {
        if( dbHelper != null )
            dbHelper.close();
    }

    // Reminders database

    @Deprecated
    public long insertReminder(String text, String type, int day, int month, int year, int hour,
                               int minute, int seconds, String number, int repeatCode, long repMinute,
                               long count, double latitude, double longitude, String uID, String weekdays,
                               int export, String melody, int radius, int color, int code,
                               String categoryId, String exclusion) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, text);
        cv.put(Constants.COLUMN_TYPE, type);
        cv.put(Constants.COLUMN_DAY, day);
        cv.put(Constants.COLUMN_MONTH, month);
        cv.put(Constants.COLUMN_YEAR, year);
        cv.put(Constants.COLUMN_HOUR, hour);
        cv.put(Constants.COLUMN_MINUTE, minute);
        cv.put(Constants.COLUMN_SECONDS, seconds);
        cv.put(Constants.COLUMN_NUMBER, number);
        cv.put(Constants.COLUMN_REPEAT, repeatCode);
        cv.put(Constants.COLUMN_REMIND_TIME, repMinute);
        cv.put(Constants.COLUMN_REMINDERS_COUNT, count);
        cv.put(Constants.COLUMN_LATITUDE, latitude);
        cv.put(Constants.COLUMN_LONGITUDE, longitude);
        cv.put(Constants.COLUMN_TECH_VAR, uID);
        cv.put(Constants.COLUMN_WEEKDAYS, weekdays);
        cv.put(Constants.COLUMN_IS_DONE, 0);
        cv.put(Constants.COLUMN_ARCHIVED, 0);
        cv.put(Constants.COLUMN_CUSTOM_RADIUS, radius);
        cv.put(Constants.COLUMN_CUSTOM_MELODY, melody);
        cv.put(Constants.COLUMN_EXPORT_TO_CALENDAR, export);
        cv.put(Constants.COLUMN_LED_COLOR, color);
        cv.put(Constants.COLUMN_SYNC_CODE, code);
        cv.put(Constants.COLUMN_CATEGORY, categoryId);
        cv.put(Constants.COLUMN_VIBRATION, -1);
        cv.put(Constants.COLUMN_VOICE, -1);
        cv.put(Constants.COLUMN_NOTIFICATION_REPEAT, -1);
        cv.put(Constants.COLUMN_WAKE_SCREEN, -1);
        cv.put(Constants.COLUMN_UNLOCK_DEVICE, -1);
        cv.put(Constants.COLUMN_AUTO_ACTION, -1);
        cv.put(Constants.COLUMN_REPEAT_LIMIT, -1);
        cv.put(Constants.COLUMN_EXTRA_3, exclusion);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(CURRENT_TABLE_NAME, null, cv);
    }

    @Deprecated
    public boolean updateReminder(long rowId, String text, String type, int day, int month, int year,
                                  int hour, int minute, int seconds, String number, int repeatCode,
                                  long repMinute, long count, double latitude, double longitude,
                                  String weekdays, int export, String melody, int radius, int color,
                                  int code, String categoryId, String exclusion) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_TEXT, text);
        args.put(Constants.COLUMN_TYPE, type);
        args.put(Constants.COLUMN_DAY, day);
        args.put(Constants.COLUMN_MONTH, month);
        args.put(Constants.COLUMN_YEAR, year);
        args.put(Constants.COLUMN_HOUR, hour);
        args.put(Constants.COLUMN_MINUTE, minute);
        args.put(Constants.COLUMN_SECONDS, seconds);
        args.put(Constants.COLUMN_NUMBER, number);
        args.put(Constants.COLUMN_REPEAT, repeatCode);
        args.put(Constants.COLUMN_REMIND_TIME, repMinute);
        args.put(Constants.COLUMN_REMINDERS_COUNT, count);
        args.put(Constants.COLUMN_LATITUDE, latitude);
        args.put(Constants.COLUMN_LONGITUDE, longitude);
        args.put(Constants.COLUMN_IS_DONE, 0);
        args.put(Constants.COLUMN_ARCHIVED, 0);
        args.put(Constants.COLUMN_CUSTOM_RADIUS, radius);
        args.put(Constants.COLUMN_CUSTOM_MELODY, melody);
        args.put(Constants.COLUMN_WEEKDAYS, weekdays);
        args.put(Constants.COLUMN_EXPORT_TO_CALENDAR, export);
        args.put(Constants.COLUMN_LED_COLOR, color);
        args.put(Constants.COLUMN_SYNC_CODE, code);
        args.put(Constants.COLUMN_CATEGORY, categoryId);
        args.put(Constants.COLUMN_EXTRA_3, exclusion);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean updateReminderStartTime(long rowId, int day, int month, int year, int hour, int minute, int seconds) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_DAY, day);
        args.put(Constants.COLUMN_MONTH, month);
        args.put(Constants.COLUMN_YEAR, year);
        args.put(Constants.COLUMN_HOUR, hour);
        args.put(Constants.COLUMN_MINUTE, minute);
        args.put(Constants.COLUMN_SECONDS, seconds);
        args.put(Constants.COLUMN_REMINDERS_COUNT, 0);
        args.put(Constants.COLUMN_IS_DONE, 0);
        args.put(Constants.COLUMN_ARCHIVED, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean updateReminderExtra(long rowId, int vibro, int voice, int repeat, int wake,
                                       int unlock, int auto, long limit) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_VIBRATION, vibro);
        args.put(Constants.COLUMN_VOICE, voice);
        args.put(Constants.COLUMN_NOTIFICATION_REPEAT, repeat);
        args.put(Constants.COLUMN_WAKE_SCREEN, wake);
        args.put(Constants.COLUMN_UNLOCK_DEVICE, unlock);
        args.put(Constants.COLUMN_AUTO_ACTION, auto);
        args.put(Constants.COLUMN_REPEAT_LIMIT, limit);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public long updateReminderDateTime(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        long time = new TimeCount(mContext).generateDateTime(rowId);
        args.put(Constants.COLUMN_FEATURE_TIME, time);
        db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null);
        return time;
    }

    @Deprecated
    public boolean updateReminderAfterTime(long rowId, long time) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REMIND_TIME, time);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean updateReminderGroup(long rowId, String group) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_CATEGORY, group);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean updateReminderCount(long rowId, long count) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REMINDERS_COUNT, count);
        args.put(Constants.COLUMN_DELAY, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setDelay(long rowId, int delay) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_DELAY, delay);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setUniqueId(long rowId, String id) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_TECH_VAR, id);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_IS_DONE, 1);
        args.put(Constants.COLUMN_DELAY, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean toArchive(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_ARCHIVED, 1);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setUnDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_IS_DONE, 0);
        args.put(Constants.COLUMN_REMINDERS_COUNT, 0);
        args.put(Constants.COLUMN_DELAY, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setLocationStatus(long rowId, int status) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REMINDERS_COUNT, status);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setNotificationShown(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REPEAT, 1);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public boolean setGroup(long rowId, String groupId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_CATEGORY, groupId);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public Cursor queryAllReminders() throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, null, null, null, null, null);
    }

    @Deprecated
    public Cursor queryGroup(String category) throws SQLException {
        openGuard();
        String order = Constants.COLUMN_IS_DONE + " ASC, " +
                Constants.COLUMN_FEATURE_TIME + " ASC";
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_CATEGORY  + "='" + category + "'"
                + " AND "+ Constants.COLUMN_ARCHIVED + "='"
                + 0 + "'", null, null, null, order);
    }

    @Deprecated
    public Cursor queryGroup() throws SQLException {
        openGuard();
        String order = Constants.COLUMN_IS_DONE + " ASC, " +
                Constants.COLUMN_FEATURE_TIME + " ASC";
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_ARCHIVED  + "='" + 0 + "'", null, null, null, order);
    }

    @Deprecated
    public Cursor getArchivedReminders() throws SQLException {
        openGuard();
        String order = Constants.COLUMN_FEATURE_TIME + " ASC";
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_ARCHIVED  + "='" + 1 + "'", null, null, null, order);
    }

    @Deprecated
    public Cursor getActiveReminders() throws SQLException {
        openGuard();
        String order = Constants.COLUMN_IS_DONE + " ASC, " +
                Constants.COLUMN_FEATURE_TIME + " ASC";
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_IS_DONE  + "='" + 0 + "'"
                + " AND "+ Constants.COLUMN_ARCHIVED + "='"
                + 0 + "'", null, null, null, order);
    }

    @Deprecated
    public Cursor getReminder(long rowId) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_ID  + "=" + rowId, null, null, null,
                null, null);
    }

    @Deprecated
    public Cursor getReminder(String uuID) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_TECH_VAR  + "='" + uuID + "'", null, null, null,
                null, null);
    }

    @Deprecated
    public boolean deleteReminder(long rowId) {
        openGuard();
        return db.delete(CURRENT_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_TYPE + " FROM " + CURRENT_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    @Deprecated
    public int getCountActive() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_TYPE + " FROM " + CURRENT_TABLE_NAME +
                " WHERE " + Constants.COLUMN_ARCHIVED + " = '" + 0 + "' AND " + Constants.COLUMN_IS_DONE +
                " = '" + 0 + "'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    // Contacts birthdays database

    public int getCountBirthdays() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.ContactConstants.COLUMN_CONTACT_ID + " FROM " + CONTACTS_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public boolean deleteBirthday(long rowId) {
        openGuard();
        return db.delete(CONTACTS_TABLE_NAME, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getBirthdays(int day, int month) throws SQLException {
        openGuard();
        return db.query(CONTACTS_TABLE_NAME, null,
                Constants.ContactConstants.COLUMN_CONTACT_DAY  + "='" + day + "'" +
                        " AND "+ Constants.ContactConstants.COLUMN_CONTACT_MONTH + "='"
                + month + "'", null, null, null, null, null);
    }

    public Cursor getBirthday(long rowId) throws SQLException {
        openGuard();
        return db.query(CONTACTS_TABLE_NAME, null, Constants.ContactConstants.COLUMN_ID  +
                "=" + rowId, null, null, null, null, null);
    }

    public Cursor getBirthdays() throws SQLException {
        openGuard();
        return db.query(CONTACTS_TABLE_NAME, null, null, null, null, null, null);
    }

    public long addBirthday(String name, int contact_id, String birthday, int day, int month,
                            String number, String uuId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_NAME, name);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_ID, contact_id);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY, birthday);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_DAY, day);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_MONTH, month);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_NUMBER, number);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_UUID, uuId);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(CONTACTS_TABLE_NAME, null, cv);
    }

    public boolean updateFullEvent(long rowId, String name, int contact_id, String birthday, int day,
                                   int month, String number){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.ContactConstants.COLUMN_CONTACT_NAME, name);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_ID, contact_id);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_NUMBER, number);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY, birthday);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_DAY, day);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_MONTH, month);
        return db.update(CONTACTS_TABLE_NAME, args, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setShown(long rowId, String year){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.ContactConstants.COLUMN_CONTACT_VAR, year);
        return db.update(CONTACTS_TABLE_NAME, args, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateOtherInformationEvent(long rowId, String uuId){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.ContactConstants.COLUMN_CONTACT_UUID, uuId);
        return db.update(CONTACTS_TABLE_NAME, args, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //Frequently used places database

    public int getCountPlaces() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.LocationConstants.COLUMN_LOCATION_NAME + " FROM " + LOCATION_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public boolean deletePlace(long rowId) {
        openGuard();
        return db.delete(LOCATION_TABLE_NAME, Constants.LocationConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getPlace(String name) throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, null, Constants.LocationConstants.COLUMN_LOCATION_NAME  +
                        "='" + name + "'", null, null, null, null, null);
    }

    public Cursor getPlace(long id) throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, null, Constants.LocationConstants.COLUMN_ID  +
                        "=" + id, null, null, null, null, null);
    }

    public Cursor queryPlaces() throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, null, null, null, null, null, null);
    }

    public long insertPlace (String name, double latitude, double longitude) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.LocationConstants.COLUMN_LOCATION_NAME, name);
        cv.put(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE, latitude);
        cv.put(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE, longitude);
        return db.insert(LOCATION_TABLE_NAME, null, cv);
    }

    public boolean updatePlace(long rowId, String name, double latitude, double longitude){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.LocationConstants.COLUMN_LOCATION_NAME, name);
        args.put(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE, latitude);
        args.put(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE, longitude);
        return db.update(LOCATION_TABLE_NAME, args, Constants.LocationConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //Events table

    public long addCalendarEvent (String uri, long reminderId, long eventId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_DELETE_URI, uri);
        cv.put(Constants.COLUMN_REMINDER_ID, reminderId);
        cv.put(Constants.COLUMN_EVENT_ID, eventId);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(EVENTS_TABLE_NAME, null, cv);
    }

    public Cursor getCalendarEvent(long id) throws SQLException {
        openGuard();
        return db.query(EVENTS_TABLE_NAME, null, Constants.COLUMN_ID +
                "=" + id, null, null, null, null, null);
    }

    public Cursor getCalendarEvents(long id) throws SQLException {
        openGuard();
        return db.query(EVENTS_TABLE_NAME, null,
                Constants.COLUMN_REMINDER_ID  +
                        "='" + id + "'", null, null, null, null, null);
    }

    public Cursor getCalendarEvents() throws SQLException {
        openGuard();
        return db.query(EVENTS_TABLE_NAME, null, null, null, null, null, null, null);
    }

    public boolean deleteCalendarEvent(long rowId) {
        openGuard();
        return db.delete(EVENTS_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //Working with missed calls table

    public long addMissedCall (String number, long dateTime) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_NUMBER, number);
        cv.put(Constants.COLUMN_DATE_TIME, dateTime);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(CALLS_TABLE_NAME, null, cv);
    }

    public Cursor getMissedCall(long id) throws SQLException {
        openGuard();
        return db.query(CALLS_TABLE_NAME, null, Constants.COLUMN_ID +
                "=" + id, null, null, null, null, null);
    }

    public Cursor getMissedCall(String number) throws SQLException {
        openGuard();
        return db.query(CALLS_TABLE_NAME, null,
                Constants.COLUMN_NUMBER  +
                        "='" + number + "'", null, null, null, null, null);
    }

    public boolean deleteMissedCall(long rowId) {
        openGuard();
        return db.delete(CALLS_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int getCountMissed(String number) throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_TYPE + " FROM " + CURRENT_TABLE_NAME +
                " WHERE " + Constants.COLUMN_NUMBER + " = '" + number + "'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    //Working with SMS templates table

    public long addTemplate (String text, long dateTime) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, text);
        cv.put(Constants.COLUMN_DATE_TIME, dateTime);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(MESSAGES_TABLE_NAME, null, cv);
    }

    public boolean updateTemplate(long rowId, String text, long dateTime){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_TEXT, text);
        args.put(Constants.COLUMN_DATE_TIME, dateTime);
        return db.update(MESSAGES_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getTemplate(long id) throws SQLException {
        openGuard();
        return db.query(MESSAGES_TABLE_NAME, null, Constants.COLUMN_ID  +
                "=" + id, null, null, null, null, null);
    }

    public Cursor queryTemplates() throws SQLException {
        openGuard();
        return db.query(MESSAGES_TABLE_NAME, null, null, null, null, null, null);
    }

    public boolean deleteTemplate(long rowId) {
        openGuard();
        return db.delete(MESSAGES_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //Working with reminder categories table

    public long addCategory (String name, long dateTime, String uuID, int color) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, name);
        cv.put(Constants.COLUMN_DATE_TIME, dateTime);
        cv.put(Constants.COLUMN_TECH_VAR, uuID);
        cv.put(Constants.COLUMN_COLOR, color);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(CATEGORIES_TABLE_NAME, null, cv);
    }

    public boolean updateCategory(long rowId, String name, long dateTime, int color){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_TEXT, name);
        args.put(Constants.COLUMN_DATE_TIME, dateTime);
        args.put(Constants.COLUMN_COLOR, color);
        return db.update(CATEGORIES_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateCategoryColor(long rowId, int color){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_COLOR, color);
        return db.update(CATEGORIES_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getCategory(long id) throws SQLException {
        openGuard();
        return db.query(CATEGORIES_TABLE_NAME, null, Constants.COLUMN_ID  +
                "=" + id, null, null, null, null, null);
    }

    public Cursor getCategory(String uuId) throws SQLException {
        openGuard();
        return db.query(CATEGORIES_TABLE_NAME, null,
                Constants.COLUMN_TECH_VAR  +
                        "='" + uuId + "'", null, null, null, null, null);
    }

    public Cursor queryCategories() throws SQLException {
        openGuard();
        return db.query(CATEGORIES_TABLE_NAME, null, null, null, null, null, null);
    }

    public boolean deleteCategory(long rowId) {
        openGuard();
        return db.delete(CATEGORIES_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //Working with reminder shopping list type table

    public long addShopItem (String task, String uuID, long remId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, task);
        cv.put(Constants.COLUMN_DATE_TIME, System.currentTimeMillis());
        cv.put(Constants.COLUMN_TECH_VAR, uuID);
        cv.put(Constants.COLUMN_REMINDER_ID, remId);
        cv.put(Constants.COLUMN_ARCHIVED, 0);
        cv.put(Constants.COLUMN_EXTRA_1, ShoppingList.ACTIVE);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(SHOPPING_TABLE_NAME, null, cv);
    }

    public long addShopItem (String task, String uuID, long remId, int checked, long dateTime) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, task);
        cv.put(Constants.COLUMN_DATE_TIME, dateTime);
        cv.put(Constants.COLUMN_EXTRA_1, ShoppingList.ACTIVE);
        cv.put(Constants.COLUMN_TECH_VAR, uuID);
        cv.put(Constants.COLUMN_REMINDER_ID, remId);
        cv.put(Constants.COLUMN_ARCHIVED, checked);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(SHOPPING_TABLE_NAME, null, cv);
    }

    public boolean updateShopItem(long rowId, String task, int checked){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_TEXT, task);
        args.put(Constants.COLUMN_ARCHIVED, checked);
        return db.update(SHOPPING_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateShopItem(long rowId, int checked){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_ARCHIVED, checked);
        return db.update(SHOPPING_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateShopItemStatus(long rowId, int status){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_EXTRA_1, status);
        return db.update(SHOPPING_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getShopItem(long id) throws SQLException {
        openGuard();
        return db.query(SHOPPING_TABLE_NAME, null, Constants.COLUMN_ID +
                "=" + id, null, null, null, null, null);
    }

    public Cursor getShopItem(String uuId) throws SQLException {
        openGuard();
        return db.query(SHOPPING_TABLE_NAME, null,
                Constants.COLUMN_TECH_VAR  +
                        "='" + uuId + "'", null, null, null, null, null);
    }

    public Cursor getShopItems() throws SQLException {
        openGuard();
        return db.query(SHOPPING_TABLE_NAME, null, null, null, null,
                Constants.COLUMN_ARCHIVED + " ASC, " + Constants.COLUMN_DATE_TIME + " ASC", null);
    }

    public Cursor getShopItems(long remId) throws SQLException {
        openGuard();
        return db.query(SHOPPING_TABLE_NAME, null, Constants.COLUMN_REMINDER_ID + "=" + remId,
                null, null, null, Constants.COLUMN_ARCHIVED + " ASC, " + Constants.COLUMN_DATE_TIME + " ASC", null);
    }

    public Cursor getShopItemsActive(long remId) throws SQLException {
        openGuard();
        return db.query(SHOPPING_TABLE_NAME, null, Constants.COLUMN_REMINDER_ID + "=" + remId +
                " AND "+ Constants.COLUMN_EXTRA_1 + "=" + 1, null, null, null,
                Constants.COLUMN_ARCHIVED + " ASC, " + Constants.COLUMN_DATE_TIME + " ASC", null);
    }

    public boolean deleteShopItem(long rowId) {
        openGuard();
        return db.delete(SHOPPING_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean deleteShopItems(long rowId) {
        openGuard();
        return db.delete(SHOPPING_TABLE_NAME, Constants.COLUMN_REMINDER_ID + "=" + rowId, null) > 0;
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        //Log.d(LOG_TAG, "open guard failed");
        throw new SQLiteException("Could not open database");
    }
}