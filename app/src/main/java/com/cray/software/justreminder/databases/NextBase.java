package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.json.JsonParser;

/**
 * Copyright 2016 Nazar Suhovich
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
public class NextBase {
    private static final String DB_NAME = "reminder_base";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "reminders_table";

    public static final String _ID = "_id";
    public static final String SUMMARY = "summary";
    public static final String TYPE = "type";
    public static final String START_TIME = "start_time";
    public static final String DELAY = "delay";
    public static final String CATEGORY = "category";
    public static final String JSON = "_json";
    public static final String DB_STATUS = "db_status";
    public static final String DB_LIST = "db_list";
    public static final String LOCATION_STATUS = "l_status";
    public static final String REMINDER_STATUS = "r_status";
    public static final String NOTIFICATION_STATUS = "n_status";
    public static final String UUID = "uuid";
    public static final String TAGS = "_tags_";

    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String TABLE_CREATE =
            "create table " + TABLE_NAME + "(" +
                    _ID + " integer primary key autoincrement, " +
                    SUMMARY + " VARCHAR(255), " +
                    TYPE + " VARCHAR(255), " +
                    CATEGORY + " VARCHAR(255), " +
                    UUID + " VARCHAR(255), " +
                    TAGS + " VARCHAR(255), " +
                    START_TIME + " INTEGER, " +
                    DELAY + " INTEGER, " +
                    DB_STATUS + " INTEGER, " +
                    DB_LIST + " INTEGER, " +
                    LOCATION_STATUS + " INTEGER, " +
                    REMINDER_STATUS + " INTEGER, " +
                    NOTIFICATION_STATUS + " INTEGER, " +
                    JSON + " TEXT " +
                    ");";

    public class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion){

            }
        }
    }

    public NextBase(Context c) {
        mContext = c;
    }

    public NextBase open() throws SQLiteException {
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
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // Reminders database

    public long insertReminder(String summary, String type, long eventTime,
                               String uID, String categoryId, String json) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(SUMMARY, summary);
        cv.put(TYPE, type);
        cv.put(START_TIME, eventTime);
        cv.put(UUID, uID);
        cv.put(CATEGORY, categoryId);
        cv.put(JSON, json);
        cv.put(DB_LIST, 0);
        cv.put(DB_STATUS, 0);
        cv.put(REMINDER_STATUS, 0);
        cv.put(NOTIFICATION_STATUS, 0);
        cv.put(DELAY, 0);
        return db.insert(TABLE_NAME, null, cv);
    }

    public boolean updateReminder(long rowId, String summary, String type, long eventTime,
                                  String uID, String categoryId, String json) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(SUMMARY, summary);
        cv.put(TYPE, type);
        cv.put(START_TIME, eventTime);
        cv.put(UUID, uID);
        cv.put(CATEGORY, categoryId);
        cv.put(JSON, json);
        cv.put(DB_LIST, 0);
        cv.put(DB_STATUS, 0);
        cv.put(REMINDER_STATUS, 0);
        cv.put(NOTIFICATION_STATUS, 0);
        cv.put(DELAY, 0);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean updateReminderStartTime(long rowId, long eventTime) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(START_TIME, eventTime);
        cv.put(DELAY, 0);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setGroup(long rowId, String group) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(CATEGORY, group);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean updateCount(long rowId, String json) {
        openGuard();
        JsonParser parser = new JsonParser(json);
        long count = parser.getCount();
        parser.setCount(count + 1);
        ContentValues cv = new ContentValues();
        cv.put(JSON, parser.getJSON());
        cv.put(DELAY, 0);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setDelay(long rowId, long delay) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(DELAY, delay);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setDone(long rowId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(DB_STATUS, 1);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean toArchive(long rowId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(DB_LIST, 1);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setUnDone(long rowId, String json) {
        openGuard();
        JsonParser parser = new JsonParser(json);
        parser.setCount(0);
        ContentValues cv = new ContentValues();
        cv.put(JSON, parser.getJSON());
        cv.put(DB_STATUS, 0);
        cv.put(DELAY, 0);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setLocationStatus(long rowId, int status) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(LOCATION_STATUS, status);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setNotificationShown(long rowId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(NOTIFICATION_STATUS, 1);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public boolean setReminderShown(long rowId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(REMINDER_STATUS, 1);
        return db.update(TABLE_NAME, cv, _ID + "=" + rowId, null) > 0;
    }

    public Cursor queryAllReminders() throws SQLException {
        openGuard();
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor queryGroup(String category) throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + START_TIME + " ASC";
        return db.query(TABLE_NAME, null, CATEGORY  + "='" + category + "'"
                + " AND "+ DB_LIST + "='" + 0 + "'", null, null, null, order);
    }

    public Cursor queryGroup() throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + START_TIME + " ASC";
        return db.query(TABLE_NAME, null, DB_LIST  + "='" + 0 + "'", null, null, null, order);
    }

    public Cursor getArchivedReminders() throws SQLException {
        openGuard();
        String order = START_TIME + " ASC";
        return db.query(TABLE_NAME, null, DB_LIST  + "='" + 1 + "'", null, null, null, order);
    }

    public Cursor getActiveReminders() throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + START_TIME + " ASC";
        return db.query(TABLE_NAME, null, DB_STATUS + "='" + 0 + "'" + " AND "+ DB_LIST + "='"
                + 0 + "'", null, null, null, order);
    }

    public Cursor getReminder(long rowId) throws SQLException {
        openGuard();
        return db.query(TABLE_NAME, null, _ID  + "=" + rowId, null, null, null,
                null, null);
    }

    public Cursor getReminder(String uuID) throws SQLException {
        openGuard();
        return db.query(TABLE_NAME, null, UUID + "='" + uuID + "'", null, null, null,
                null, null);
    }

    public boolean deleteReminder(long rowId) {
        openGuard();
        return db.delete(TABLE_NAME, _ID + "=" + rowId, null) > 0;
    }

    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + _ID + " FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getCountActive() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + _ID + " FROM " + TABLE_NAME +
                " WHERE " + DB_STATUS + " = '" + 0 + "' AND " + DB_LIST +
                " = '" + 0 + "'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public void openGuard() throws SQLiteException {
        if (isOpen()) {
            return;
        }

        open();

        if (isOpen()) {
            return;
        }
        throw new SQLiteException("Could not open database");
    }
}