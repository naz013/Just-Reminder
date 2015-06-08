package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.interfaces.Constants;

public class FilesDataBase {
    private static final String DB_NAME = "files_database";
    private static final int DB_VERSION = 1;
    private static final String ARCHIVE_TABLE_NAME = "files_table";
    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String CURRENT_TABLE_CREATE =
            "create table " + ARCHIVE_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.FilesConstants.COLUMN_FILE_NAME + " VARCHAR(255), " +
                    Constants.FilesConstants.COLUMN_FILE_TYPE + " VARCHAR(255), " +
                    Constants.FilesConstants.COLUMN_FILE_LOCATION + " VARCHAR(255), " +
                    Constants.FilesConstants.COLUMN_FILE_LAST_EDIT + " INTEGER, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_TYPE + " VARCHAR(255), " +
                    Constants.COLUMN_DAY + " INTEGER, " +
                    Constants.COLUMN_MONTH + " INTEGER, " +
                    Constants.COLUMN_YEAR + " INTEGER, " +
                    Constants.COLUMN_HOUR + " INTEGER, " +
                    Constants.COLUMN_MINUTE + " INTEGER, " +
                    Constants.COLUMN_SECONDS + " INTEGER, " +
                    Constants.COLUMN_NUMBER + " VARCHAR(255), " +
                    Constants.COLUMN_REMIND_TIME + " INTEGER, " +
                    Constants.COLUMN_REPEAT + " INTEGER, " +
                    Constants.COLUMN_REMINDERS_COUNT + " INTEGER, " +
                    Constants.COLUMN_LATITUDE + " REAL, " +
                    Constants.COLUMN_LONGITUDE + " REAL, " +
                    Constants.COLUMN_FEATURE_TIME + " INTEGER, " +
                    Constants.COLUMN_DELAY + " INTEGER, " +
                    Constants.COLUMN_TECH_VAR + " VARCHAR(255), " +
                    Constants.COLUMN_WEEKDAYS + " VARCHAR(255) " +
                    ");";

    public class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CURRENT_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }


    }

    public FilesDataBase(Context c) {
        mContext = c;
    }

    public FilesDataBase open() throws SQLiteException {
        dbHelper = new DBHelper(mContext);

        db = dbHelper.getWritableDatabase();

        System.gc();
        return this;
    }

    public boolean isOpen ()
    {
        if (db !=null)
            return db.isOpen();
        else
            return false;
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void close() {
        if( dbHelper != null )
            dbHelper.close();
    }

    public long insertTask (String fileName, String fileType, String fileLocation, long edit, String text, String type,
                            int day, int month, int year, int hour, int minute, int seconds,
                            String number, int repeatCode, long repMinute, long count, double latitude, double longitude, String uID,
                            String weekdays) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.FilesConstants.COLUMN_FILE_NAME, fileName);
        cv.put(Constants.FilesConstants.COLUMN_FILE_TYPE, fileType);
        cv.put(Constants.FilesConstants.COLUMN_FILE_LOCATION, fileLocation);
        cv.put(Constants.FilesConstants.COLUMN_FILE_LAST_EDIT, edit);
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
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(ARCHIVE_TABLE_NAME, null, cv);
    }

    public Cursor queryGroup() throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, new String[] {Constants.COLUMN_ID, Constants.FilesConstants.COLUMN_FILE_NAME,
                Constants.FilesConstants.COLUMN_FILE_TYPE, Constants.FilesConstants.COLUMN_FILE_LOCATION,
                        Constants.FilesConstants.COLUMN_FILE_LAST_EDIT,
                        Constants.COLUMN_TEXT, Constants.COLUMN_TYPE, Constants.COLUMN_DAY,
                Constants.COLUMN_MONTH, Constants.COLUMN_YEAR, Constants.COLUMN_HOUR, Constants.COLUMN_MINUTE, Constants.COLUMN_SECONDS,
                Constants.COLUMN_NUMBER, Constants.COLUMN_REPEAT, Constants.COLUMN_REMIND_TIME, Constants.COLUMN_REMINDERS_COUNT,
                Constants.COLUMN_LATITUDE, Constants.COLUMN_LONGITUDE, Constants.COLUMN_TECH_VAR, Constants.COLUMN_WEEKDAYS}, null,
                null, null, null, null);
    }

    public Cursor getTask(long rowId) throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, new String[] {Constants.COLUMN_ID, Constants.FilesConstants.COLUMN_FILE_NAME,
                        Constants.FilesConstants.COLUMN_FILE_TYPE, Constants.FilesConstants.COLUMN_FILE_LOCATION,
                        Constants.FilesConstants.COLUMN_FILE_LAST_EDIT,
                        Constants.COLUMN_TEXT, Constants.COLUMN_TYPE, Constants.COLUMN_DAY, Constants.COLUMN_MONTH,
                Constants.COLUMN_YEAR, Constants.COLUMN_HOUR, Constants.COLUMN_MINUTE, Constants.COLUMN_SECONDS, Constants.COLUMN_NUMBER,
                Constants.COLUMN_REPEAT, Constants.COLUMN_REMIND_TIME, Constants.COLUMN_REMINDERS_COUNT, Constants.COLUMN_LATITUDE,
                Constants.COLUMN_LONGITUDE, Constants.COLUMN_TECH_VAR, Constants.COLUMN_WEEKDAYS},
                Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor getTask(String type) throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, new String[] {Constants.COLUMN_ID, Constants.FilesConstants.COLUMN_FILE_NAME,
                        Constants.FilesConstants.COLUMN_FILE_TYPE, Constants.FilesConstants.COLUMN_FILE_LOCATION,
                        Constants.FilesConstants.COLUMN_FILE_LAST_EDIT,
                        Constants.COLUMN_TEXT, Constants.COLUMN_TYPE, Constants.COLUMN_DAY, Constants.COLUMN_MONTH,
                        Constants.COLUMN_YEAR, Constants.COLUMN_HOUR, Constants.COLUMN_MINUTE, Constants.COLUMN_SECONDS, Constants.COLUMN_NUMBER,
                        Constants.COLUMN_REPEAT, Constants.COLUMN_REMIND_TIME, Constants.COLUMN_REMINDERS_COUNT, Constants.COLUMN_LATITUDE,
                        Constants.COLUMN_LONGITUDE, Constants.COLUMN_TECH_VAR, Constants.COLUMN_WEEKDAYS},
                Constants.FilesConstants.COLUMN_FILE_TYPE +
                        "='" + type + "'", null, null, null, null, null);
    }

    public boolean deleteTask(long rowId) {
        openGuard();
        return db.delete(ARCHIVE_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_TYPE + " FROM " + ARCHIVE_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        //Log.d(LOG_TAG, "open guard failed");
        throw new SQLiteException("Could not open database");
    }
}