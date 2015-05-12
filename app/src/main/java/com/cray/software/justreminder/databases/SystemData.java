package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.interfaces.Constants;

public class SystemData {
    private static final String DB_NAME = "system_data";
    private static final int DB_VERSION = 1;
    private static final String ARCHIVE_TABLE_NAME = "security_table";
    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "column_name";
    public static final String COLUMN_TYPE = "column_type";
    public static final String COLUMN_VAR = "column_var";
    public static final String COLUMN_VAR1 = "column_var_";
    public static final String COLUMN_VAR2 = "column__var";
    public static final String COLUMN_INT = "column_int";
    public static final String COLUMN_INT2 = "column_int_";

    private static final String CURRENT_TABLE_CREATE =
            "create table " + ARCHIVE_TABLE_NAME + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " VARCHAR(255), " +
                    COLUMN_TYPE + " VARCHAR(255), " +
                    COLUMN_VAR + " VARCHAR(255), " +
                    COLUMN_VAR1 + " VARCHAR(255), " +
                    COLUMN_VAR2 + " VARCHAR(255), " +
                    COLUMN_INT + " INTEGER, " +
                    COLUMN_INT2 + " INTEGER " +
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

    public SystemData(Context c) {
        mContext = c;
    }

    public SystemData open() throws SQLiteException {
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

    public long insertKey (String text, String type) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, text);
        cv.put(COLUMN_TYPE, type);
        return db.insert(ARCHIVE_TABLE_NAME, null, cv);
    }

    public Cursor queryKeys() throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, new String[] {COLUMN_ID, COLUMN_NAME, COLUMN_TYPE}, null, null, null, null, null);
    }

    public Cursor getKey(String type) throws SQLException {
        openGuard();
        return db.query(ARCHIVE_TABLE_NAME, new String[] {COLUMN_ID, COLUMN_NAME, COLUMN_TYPE},
                COLUMN_TYPE  +
                        "='" + type + "'", null, null, null, null, null);
    }

    public boolean deleteKey(long rowId) {
        openGuard();
        return db.delete(ARCHIVE_TABLE_NAME, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int countKeys() throws SQLException {
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