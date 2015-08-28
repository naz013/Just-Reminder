package com.cray.software.justreminder.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

public class NotesBase {
    private static final String DB_NAME = "notes_base";
    private static final int DB_VERSION = 2;
    private static final String NOTE_TABLE_NAME = "notes_table";
    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String NOTE_TABLE_CREATE =
            "create table " + NOTE_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_NOTE + " VARCHAR(255), " +
                    Constants.COLUMN_DATE + " VARCHAR(255), " +
                    Constants.COLUMN_IMAGE + " BLOB, " +
                    Constants.COLUMN_UUID + " VARCHAR(255), " +
                    Constants.COLUMN_FONT_STYLE + " INTEGER, " +
                    Constants.COLUMN_FONT_SIZE + " INTEGER, " +
                    Constants.COLUMN_FONT_COLOR + " INTEGER, " +
                    Constants.COLUMN_FONT_UNDERLINED + " INTEGER, " +
                    Constants.COLUMN_LINK_ID + " INTEGER, " +
                    Constants.COLUMN_COLOR + " INTEGER " +
                    ");";

    public class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(NOTE_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion){
                case 1:
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_STYLE + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_SIZE + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_UNDERLINED + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LINK_ID + " INTEGER");
                    break;
            }
        }


    }

    public NotesBase(Context c) {
        mContext = c;
    }

    public NotesBase open() throws SQLiteException {
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
        return db.insert(NOTE_TABLE_NAME, null, cv);
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
        return db.update(NOTE_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateNote(long rowId, String note) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_NOTE, note);
        return db.update(NOTE_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean linkToReminder(long rowId, long linkId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_LINK_ID, linkId);
        return db.update(NOTE_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
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
        return db.query(NOTE_TABLE_NAME, null, null, null, null, null, order);
    }

    public Cursor getNote(long rowId) throws SQLException {
        openGuard();
        return db.query(NOTE_TABLE_NAME, null,
                Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor getNoteByReminder(long remId) throws SQLException {
        openGuard();
        return db.query(NOTE_TABLE_NAME, null,
                Constants.COLUMN_LINK_ID  +
                        "='" + remId + "'", null, null, null, null, null);
    }

    public boolean deleteNote(long rowId) {
        openGuard();
        return db.delete(NOTE_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_ID + " FROM " + NOTE_TABLE_NAME;
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