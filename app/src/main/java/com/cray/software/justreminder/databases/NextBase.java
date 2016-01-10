package com.cray.software.justreminder.databases;

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
    public static final String CATEGORY = "category";
    public static final String JSON = "_json";
    public static final String DB_STATUS = "db_status";
    public static final String LOCATION_STATUS = "l_status";
    public static final String REMINDER_STATUS = "r_status";
    public static final String NOTIFICATION_STATUS = "n_status";
    public static final String UUID = "uuid";

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
                    START_TIME + " INTEGER, " +
                    DB_STATUS + " INTEGER, " +
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

    public long saveNote (String note, String date, int color, String uuId, byte[] image, int style) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_NOTE, note);
        cv.put(Constants.COLUMN_DATE, date);
        cv.put(Constants.COLUMN_COLOR, color);
        cv.put(Constants.COLUMN_UUID, uuId);
        cv.put(Constants.COLUMN_IMAGE, image);
        cv.put(Constants.COLUMN_FONT_STYLE, style);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(TABLE_NAME, null, cv);
    }

    public boolean updateNote(long rowId, String note, String date, int color, String uuId, byte[] image,
                              int style) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_NOTE, note);
        args.put(Constants.COLUMN_DATE, date);
        args.put(Constants.COLUMN_COLOR, color);
        args.put(Constants.COLUMN_UUID, uuId);
        args.put(Constants.COLUMN_IMAGE, image);
        args.put(Constants.COLUMN_FONT_STYLE, style);
        return db.update(TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateNote(long rowId, String note) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_NOTE, note);
        return db.update(TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateNoteColor(long rowId, int color) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_COLOR, color);
        return db.update(TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean linkToReminder(long rowId, long linkId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_LINK_ID, linkId);
        return db.update(TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getNotes() throws SQLException {
        openGuard();
        SharedPrefs prefs = new SharedPrefs(mContext);
        String orderPrefs = prefs.loadPrefs(Prefs.NOTES_ORDER);
        String order = null;
        if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = Constants.COLUMN_DATE + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = Constants.COLUMN_DATE + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_NAME_A_Z)){
            order = Constants.COLUMN_NOTE + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_NAME_Z_A)){
            order = Constants.COLUMN_NOTE + " DESC";
        }
        return db.query(TABLE_NAME, null, null, null, null, null, order);
    }

    public Cursor getNote(long rowId) throws SQLException {
        openGuard();
        return db.query(TABLE_NAME, null,
                Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor getNoteByReminder(long remId) throws SQLException {
        openGuard();
        return db.query(TABLE_NAME, null,
                Constants.COLUMN_LINK_ID  +
                        "='" + remId + "'", null, null, null, null, null);
    }

    public boolean deleteNote(long rowId) {
        openGuard();
        return db.delete(TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + _ID + " FROM " + TABLE_NAME;
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