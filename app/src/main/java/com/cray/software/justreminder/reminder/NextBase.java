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

package com.cray.software.justreminder.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.places.PlaceItem;
import com.cray.software.justreminder.reminder.json.JParser;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class NextBase {
    private static final String DB_NAME = "reminder_base";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "reminders_table";

    public static final String _ID = "_id";
    public static final String SUMMARY = "summary";
    public static final String TYPE = "type";
    public static final String EVENT_TIME = "event_time";
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
                    EVENT_TIME + " INTEGER, " +
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

    public boolean isOpen() {
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

    public long saveReminder(ReminderItem item) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(SUMMARY, item.getSummary());
        cv.put(TYPE, item.getType());
        cv.put(EVENT_TIME, item.getDateTime());
        cv.put(UUID, item.getUuId());
        cv.put(CATEGORY, item.getGroupUuId());
        cv.put(JSON, item.getJson());
        cv.put(DB_LIST, item.getList());
        cv.put(DB_STATUS, item.getStatus());
        cv.put(REMINDER_STATUS, item.getReminder());
        cv.put(NOTIFICATION_STATUS, item.getNotification());
        cv.put(LOCATION_STATUS, item.getLocation());
        cv.put(DELAY, item.getDelay());
        cv.put(TAGS, item.getTags());
        if (item.getId() == 0) {
            return db.insert(TABLE_NAME, null, cv);
        } else {
            return db.update(TABLE_NAME, cv, _ID + "=" + item.getId(), null);
        }
    }

    public boolean setGroup(long rowId, String group) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(CATEGORY, group);
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

    public boolean setUnDone(long rowId) {
        openGuard();
        ContentValues cv = new ContentValues();
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

    private ReminderItem reminderFromCursor(Cursor c) {
        String summary = c.getString(c.getColumnIndex(SUMMARY));
        String json = c.getString(c.getColumnIndex(JSON));
        String type = c.getString(c.getColumnIndex(TYPE));
        String categoryId = c.getString(c.getColumnIndex(CATEGORY));
        String tags = c.getString(c.getColumnIndex(TAGS));
        String uuId = c.getString(c.getColumnIndex(UUID));
        int list = c.getInt(c.getColumnIndex(DB_LIST));
        int status = c.getInt(c.getColumnIndex(DB_STATUS));
        int location = c.getInt(c.getColumnIndex(LOCATION_STATUS));
        int reminder = c.getInt(c.getColumnIndex(REMINDER_STATUS));
        int notification = c.getInt(c.getColumnIndex(NOTIFICATION_STATUS));
        long id = c.getLong(c.getColumnIndex(_ID));
        long dateTime = c.getLong(c.getColumnIndex(EVENT_TIME));
        long delay = c.getLong(c.getColumnIndex(DELAY));
        return new ReminderItem(summary, json, type, uuId, categoryId, tags, list, status, location, reminder, notification, dateTime, delay, id);
    }

    public List<ReminderItem> queryAllReminders() throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getAllLocations() throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, TYPE + " LIKE ?", new String[] {"%"+ Constants.TYPE_LOCATION + "%" }, null, null, null);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<PlaceItem> queryAllLocations() throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, TYPE + " LIKE ?", new String[] {"%"+ Constants.TYPE_LOCATION + "%" }, null, null, null);
        List<PlaceItem> list = new ArrayList<>();
        int mRadius = SharedPrefs.getInstance(mContext).getInt(Prefs.LOCATION_RADIUS);
        if (c != null && c.moveToFirst()) {
            do {
                String text = c.getString(c.getColumnIndex(SUMMARY));
                long id = c.getLong(c.getColumnIndex(_ID));
                String json = c.getString(c.getColumnIndex(JSON));
                int isDone = c.getInt(c.getColumnIndex(DB_STATUS));
                int isArch = c.getInt(c.getColumnIndex(DB_LIST));
                if (isArch == 0 && isDone == 0) {
                    JPlace jPlace = new JParser(json).getPlace();
                    double latitude = jPlace.getLatitude();
                    double longitude = jPlace.getLongitude();
                    int style = jPlace.getMarker();
                    int radius = jPlace.getRadius();
                    if (radius == -1) {
                        radius = mRadius;
                    }
                    list.add(new PlaceItem(text, new LatLng(latitude, longitude), style, id, radius));
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getByKey(String key) throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, JSON + " LIKE ?", new String[] {"%"+ key + "%" }, null, null, null);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getReminders(String category) throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + EVENT_TIME + " ASC";
        Cursor c = db.query(TABLE_NAME, null, CATEGORY  + "='" + category + "'" + " AND "+ DB_LIST + "='" + 0 + "'", null, null, null, order);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getReminders(long to) throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + EVENT_TIME + " ASC";
        Cursor c = db.query(TABLE_NAME, null, EVENT_TIME + "<='" + to + "'" + " AND "+ DB_LIST + "='" + 0 + "'", null, null, null, order);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getReminders() throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + EVENT_TIME + " ASC";
        Cursor c = db.query(TABLE_NAME, null, DB_LIST  + "='" + 0 + "'", null, null, null, order);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getArchivedReminders() throws SQLException {
        openGuard();
        String order = EVENT_TIME + " ASC";
        Cursor c = db.query(TABLE_NAME, null, DB_LIST  + "='" + 1 + "'", null, null, null, order);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<ReminderItem> getActiveReminders() throws SQLException {
        openGuard();
        String order = DB_STATUS + " ASC, " + EVENT_TIME + " ASC";
        Cursor c = db.query(TABLE_NAME, null, DB_STATUS + "=" + 0 + " AND "+ DB_LIST + "=" + 0 + "", null, null, null, order);
        List<ReminderItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public ReminderItem getReminder(long rowId) throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, _ID  + "=" + rowId, null, null, null, null, null);
        ReminderItem item = null;
        if (c != null && c.moveToFirst()) {
            item = reminderFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public ReminderItem getReminder(String uuID) throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, UUID + "='" + uuID + "'", null, null, null, null, null);
        ReminderItem item = null;
        if (c != null && c.moveToFirst()) {
            item = reminderFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public boolean deleteReminder(long rowId) {
        openGuard();
        return db.delete(TABLE_NAME, _ID + "=" + rowId, null) > 0;
    }

    public void openGuard() throws SQLiteException {
        if (isOpen()) return;
        open();
        if (isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}