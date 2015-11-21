package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.constants.Constants;

public class FilesDataBase {
    private static final String DB_NAME = "files_database";
    private static final int DB_VERSION = 2;
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
                    Constants.COLUMN_VIBRATION + " INTEGER, " +
                    Constants.COLUMN_VOICE + " INTEGER, " +
                    Constants.COLUMN_AUTO_ACTION + " INTEGER, " +
                    Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER, " +
                    Constants.COLUMN_WAKE_SCREEN + " INTEGER, " +
                    Constants.COLUMN_UNLOCK_DEVICE + " INTEGER, " +
                    Constants.COLUMN_REPEAT_LIMIT + " INTEGER, " +
                    Constants.COLUMN_EXTRA_1 + " INTEGER, " +
                    Constants.COLUMN_EXTRA_2 + " INTEGER, " +
                    Constants.COLUMN_EXTRA_3 + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_4 + " VARCHAR(255), " +
                    Constants.COLUMN_EXTRA_5 + " VARCHAR(255), " +
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
            switch (oldVersion){
                case 1:
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VOICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_VIBRATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_AUTO_ACTION + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_WAKE_SCREEN + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_UNLOCK_DEVICE + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_NOTIFICATION_REPEAT + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_REPEAT_LIMIT + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_1 + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_2 + " INTEGER");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_3 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_4 + " VARCHAR(255)");
                    db.execSQL("ALTER TABLE " + ARCHIVE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_EXTRA_5 + " VARCHAR(255)");
                    break;
            }
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

    public long insertFile(String fileName, String fileType, String fileLocation, long edit,
                           String text, String type, int day, int month, int year, int hour,
                           int minute, int seconds, String number, int repeatCode, long repMinute,
                           long count, double latitude, double longitude, String uID,
                           String weekdays, String exclusion) {
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
        cv.put(Constants.COLUMN_EXTRA_3, exclusion);
        //Log.d(LOG_TAG, "data is inserted " + cv);
        return db.insert(ARCHIVE_TABLE_NAME, null, cv);
    }

    public boolean updateFileExtra(long rowId, int vibro, int voice, int repeat, int wake,
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
        return db.update(ARCHIVE_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getFiles() throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor getFile(long rowId) throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, null, Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor getFile(String type) throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, null, Constants.FilesConstants.COLUMN_FILE_TYPE +
                        "='" + type + "'", null, null, null, null, null);
    }

    public boolean deleteFile(long rowId) {
        openGuard();
        return db.delete(ARCHIVE_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int countFiles() throws SQLException {
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
