package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;

public class DataBase {
    private static final String DB_NAME = "just_database";
    private static final int DB_VERSION = 9;
    private static final String CURRENT_TABLE_NAME = "current_task_table";
    private static final String CONTACTS_TABLE_NAME = "contacts_task_table";
    private static final String LOCATION_TABLE_NAME = "locations_table";
    private static final String NOTE_TABLE_NAME = "notes_table";
    private static final String EVENTS_TABLE_NAME = "events_table";
    private static final String CALLS_TABLE_NAME = "calls_table";
    private static final String MESSAGES_TABLE_NAME = "messages_table";
    private static final String CATEGORIES_TABLE_NAME = "categories_table";

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
                    Constants.COLUMN_TECH_VAR + " VARCHAR(255), " +
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
                    Constants.ContactConstants.COLUMN_CONTACT_PHOTO_ID + " VARCHAR(255), " +
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
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    break;
                case 2:
                    db.execSQL(LOCATION_TABLE_CREATE);
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    break;
                case 3:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    break;
                case 4:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL("DELETE FROM "+ NOTE_TABLE_NAME);
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    break;
                case 5:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_IS_DONE + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXPORT_TO_CALENDAR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_RADIUS + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CUSTOM_MELODY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_ARCHIVED + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_DATE_TIME + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_CATEGORY + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LED_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + CURRENT_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_SYNC_CODE + " INTEGER");
                    break;
                case 6:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
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
                    break;
                case 7:
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 8:
                    db.execSQL(CATEGORIES_TABLE_CREATE);
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

    public long insertTask (String text, String type, int day, int month, int year, int hour,
                            int minute, int seconds, String number, int repeatCode, int repMinute,
                            long count, double latitude, double longitude, String uID, String weekdays,
                            int export, String melody, int radius, int color, int code, String categoryId) {
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
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(CURRENT_TABLE_NAME, null, cv);
    }

    public boolean updateTask(long rowId, String text, String type, int day, int month, int year,
                              int hour, int minute, int seconds, String number, int repeatCode,
                              int repMinute, long count, double latitude, double longitude,
                              String weekdays, int export, String melody, int radius, int color,
                              int code, String categoryId) {
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
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateStartTime(long rowId, int day, int month, int year, int hour, int minute, int seconds) {
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

    public boolean updateDateTime(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        TimeCount mCount = new TimeCount(mContext);
        args.put(Constants.COLUMN_FEATURE_TIME, mCount.generateDateTime(rowId));
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateCount(long rowId, long count) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REMINDERS_COUNT, count);
        args.put(Constants.COLUMN_DELAY, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setDelay(long rowId, int delay) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_DELAY, delay);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setUniqueId(long rowId, String id) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_TECH_VAR, id);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_IS_DONE, 1);
        args.put(Constants.COLUMN_DELAY, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean toArchive(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_ARCHIVED, 1);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setUnDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_IS_DONE, 0);
        args.put(Constants.COLUMN_REMINDERS_COUNT, 0);
        args.put(Constants.COLUMN_DELAY, 0);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setLocationShown(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REMINDERS_COUNT, 1);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setNotificationShown(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_REPEAT, 1);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setGroup(long rowId, String groupId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_CATEGORY, groupId);
        return db.update(CURRENT_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor queryAll() throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor queryGroup(String categoty) throws SQLException {
        openGuard();
        SharedPrefs prefs = new SharedPrefs(mContext);
        String orderPrefs = prefs.loadPrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER);
        String order = null;
        if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = Constants.COLUMN_FEATURE_TIME + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = Constants.COLUMN_FEATURE_TIME + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z)){
            order = Constants.COLUMN_IS_DONE + " ASC, " +
                    Constants.COLUMN_FEATURE_TIME + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A)){
            order = Constants.COLUMN_IS_DONE + " ASC, " +
                    Constants.COLUMN_FEATURE_TIME + " DESC";
        }
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_CATEGORY  + "='" + categoty + "'", null, null, null, order);
    }

    public Cursor queryGroup() throws SQLException {
        openGuard();
        SharedPrefs prefs = new SharedPrefs(mContext);
        String orderPrefs = prefs.loadPrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER);
        String order;
        if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = Constants.COLUMN_FEATURE_TIME + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = Constants.COLUMN_FEATURE_TIME + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z)){
            order = Constants.COLUMN_IS_DONE + " ASC, " +
                    Constants.COLUMN_FEATURE_TIME + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A)){
            order = Constants.COLUMN_IS_DONE + " ASC, " +
                    Constants.COLUMN_FEATURE_TIME + " DESC";
        } else {
            order = Constants.COLUMN_IS_DONE + " ASC, " +
                    Constants.COLUMN_FEATURE_TIME + " ASC";
        }
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_ARCHIVED  + "='" + 0 + "'", null, null, null, order);
    }

    public Cursor queryArchived() throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_ARCHIVED  + "='" + 1 + "'", null, null, null, null);
    }

    public Cursor getTask(long rowId) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_ID  + "=" + rowId, null, null, null,
                null, null);
    }

    public Cursor getTasks(int hour, int minute) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null,
                Constants.COLUMN_HOUR  + "='" + hour + "'" + " AND "+ Constants.COLUMN_MINUTE + "='"
                        + minute + "'", null, null, null, null, null);
    }

    public Cursor getMarkers(String type, String type2, String type3) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, Constants.COLUMN_TYPE  + "='" + type + "'" + " OR "+ Constants.COLUMN_TYPE + "='"
                + type2 + "'" + " OR "+ Constants.COLUMN_TYPE + "='"
                + type3 + "'" + " AND "+ Constants.COLUMN_ARCHIVED + "='"
                + 0 + "'" + " AND "+ Constants.COLUMN_IS_DONE + "='"
                + 0 + "'", null, null, null, null, null);
    }

    public boolean deleteTask(long rowId) {
        openGuard();
        return db.delete(CURRENT_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_TYPE + " FROM " + CURRENT_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

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

    public int getCountEvents() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.ContactConstants.COLUMN_CONTACT_ID + " FROM " + CONTACTS_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public boolean deleteEvent(long rowId) {
        openGuard();
        return db.delete(CONTACTS_TABLE_NAME, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getEvents(int day, int month) throws SQLException {
        openGuard();
        return db.query(CONTACTS_TABLE_NAME, null,
                Constants.ContactConstants.COLUMN_CONTACT_DAY  + "='" + day + "'" +
                        " AND "+ Constants.ContactConstants.COLUMN_CONTACT_MONTH + "='"
                + month + "'", null, null, null, null, null);
    }

    public Cursor getEvent(long rowId) throws SQLException {
        openGuard();
        return db.query(CONTACTS_TABLE_NAME, null, Constants.ContactConstants.COLUMN_ID  +
                "=" + rowId, null, null, null, null, null);
    }

    public Cursor queryEvents() throws SQLException {
        openGuard();
        return db.query(CONTACTS_TABLE_NAME, null, null, null, null, null, null);
    }

    public long insertEvent (String name, int contact_id, String birthday, int day, int month, String number, String email) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_NAME, name);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_ID, contact_id);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY, birthday);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_DAY, day);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_MONTH, month);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_NUMBER, number);
        cv.put(Constants.ContactConstants.COLUMN_CONTACT_MAIL, email);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(CONTACTS_TABLE_NAME, null, cv);
    }

    public boolean updateFullEvent(long rowId, String name, int contact_id, String birthday, int day, int month, String number, String mail, int photo_id){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.ContactConstants.COLUMN_CONTACT_NAME, name);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_ID, contact_id);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_NUMBER, number);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_MAIL, mail);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY, birthday);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_PHOTO_ID, photo_id);
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

    public boolean updateOtherInformationEvent(long rowId, String number, String mail, int photo_id){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.ContactConstants.COLUMN_CONTACT_NUMBER, number);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_MAIL, mail);
        args.put(Constants.ContactConstants.COLUMN_CONTACT_PHOTO_ID, photo_id);
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
        return db.query(LOCATION_TABLE_NAME, new String[] {Constants.LocationConstants.COLUMN_ID, Constants.LocationConstants.COLUMN_LOCATION_NAME,
                Constants.LocationConstants.COLUMN_LOCATION_LATITUDE, Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE,
                Constants.LocationConstants.COLUMN_LOCATION_TECH,
                Constants.LocationConstants.COLUMN_LOCATION_TECH1, Constants.LocationConstants.COLUMN_LOCATION_TECH2,
                Constants.LocationConstants.COLUMN_LOCATION_VAR, Constants.LocationConstants.COLUMN_LOCATION_VAR1,
                        Constants.LocationConstants.COLUMN_LOCATION_VAR2},
                Constants.LocationConstants.COLUMN_LOCATION_NAME  +
                        "='" + name + "'", null, null, null, null, null);
    }

    public Cursor getPlace(long id) throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, new String[] {Constants.LocationConstants.COLUMN_ID, Constants.LocationConstants.COLUMN_LOCATION_NAME,
                        Constants.LocationConstants.COLUMN_LOCATION_LATITUDE, Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE,
                        Constants.LocationConstants.COLUMN_LOCATION_TECH,
                        Constants.LocationConstants.COLUMN_LOCATION_TECH1, Constants.LocationConstants.COLUMN_LOCATION_TECH2,
                        Constants.LocationConstants.COLUMN_LOCATION_VAR, Constants.LocationConstants.COLUMN_LOCATION_VAR1,
                        Constants.LocationConstants.COLUMN_LOCATION_VAR2},
                Constants.LocationConstants.COLUMN_ID  +
                        "=" + id, null, null, null, null, null);
    }

    public Cursor queryPlaces() throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, new String[] {Constants.LocationConstants.COLUMN_ID,
                Constants.LocationConstants.COLUMN_LOCATION_NAME,
                Constants.LocationConstants.COLUMN_LOCATION_LATITUDE, Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE,
                Constants.LocationConstants.COLUMN_LOCATION_TECH,
                Constants.LocationConstants.COLUMN_LOCATION_TECH1, Constants.LocationConstants.COLUMN_LOCATION_TECH2,
                Constants.LocationConstants.COLUMN_LOCATION_VAR, Constants.LocationConstants.COLUMN_LOCATION_VAR1,
                Constants.LocationConstants.COLUMN_LOCATION_VAR2}, null, null, null, null, null);
    }

    public long insertPlace (String name, double latitude, double longitude) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.LocationConstants.COLUMN_LOCATION_NAME, name);
        cv.put(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE, latitude);
        cv.put(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE, longitude);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(LOCATION_TABLE_NAME, null, cv);
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
        return db.query(EVENTS_TABLE_NAME, null, Constants.COLUMN_ID  +
                        "=" + id, null, null, null, null, null);
    }

    public Cursor getCalendarEvents(long id) throws SQLException {
        openGuard();
        return db.query(EVENTS_TABLE_NAME, null,
                Constants.COLUMN_REMINDER_ID  +
                        "='" + id + "'", null, null, null, null, null);
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
        return db.query(CALLS_TABLE_NAME, null, Constants.COLUMN_ID  +
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
        return db.update(MESSAGES_TABLE_NAME, args, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
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
        return db.update(CATEGORIES_TABLE_NAME, args, Constants.ContactConstants.COLUMN_ID + "=" + rowId, null) > 0;
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

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        //Log.d(LOG_TAG, "open guard failed");
        throw new SQLiteException("Could not open database");
    }
}