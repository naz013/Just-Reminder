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

package com.cray.software.justreminder.notes;

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

import java.util.ArrayList;
import java.util.List;

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

    public boolean isOpen() {
        return db != null && db.isOpen();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public long saveNote(NoteItem item) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_NOTE, item.getNote());
        cv.put(Constants.COLUMN_DATE, item.getDate());
        cv.put(Constants.COLUMN_COLOR, item.getColor());
        cv.put(Constants.COLUMN_UUID, item.getUuId());
        cv.put(Constants.COLUMN_IMAGE, item.getImage());
        cv.put(Constants.COLUMN_FONT_STYLE, item.getStyle());
        cv.put(Constants.COLUMN_LINK_ID, item.getLinkId());
        if (item.getId() == 0) {
            return db.insert(NOTE_TABLE_NAME, null, cv);
        } else {
            return db.update(NOTE_TABLE_NAME, cv, Constants.COLUMN_ID + "=" + item.getId(), null);
        }
    }

    public boolean updateNoteColor(long rowId, int color) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_COLOR, color);
        return db.update(NOTE_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean linkToReminder(long rowId, long linkId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_LINK_ID, linkId);
        return db.update(NOTE_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public List<NoteItem> getNotes() throws SQLException {
        openGuard();
        String orderPrefs = SharedPrefs.getInstance(mContext).getString(Prefs.NOTES_ORDER);
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
        Cursor c = db.query(NOTE_TABLE_NAME, null, null, null, null, null, order);
        List<NoteItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                int color  = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                int style  = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
                String date = c.getString(c.getColumnIndex(Constants.COLUMN_DATE));
                String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long linkId = c.getLong(c.getColumnIndex(Constants.COLUMN_LINK_ID));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                list.add(new NoteItem(note, uuID, date, color, style, image, id, linkId));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public NoteItem getNote(long id) throws SQLException {
        openGuard();
        Cursor c = db.query(NOTE_TABLE_NAME, null, Constants.COLUMN_ID  + "=" + id, null, null, null, null, null);
        NoteItem item = null;
        if (c != null && c.moveToFirst()) {
            String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
            int color  = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            int style  = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
            String date = c.getString(c.getColumnIndex(Constants.COLUMN_DATE));
            String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
            byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
            long linkId = c.getLong(c.getColumnIndex(Constants.COLUMN_LINK_ID));
            item = new NoteItem(note, uuID, date, color, style, image, id, linkId);
        }
        if (c != null) c.close();
        return item;
    }

    public NoteItem getNoteByReminder(long remId) throws SQLException {
        openGuard();
        Cursor c = db.query(NOTE_TABLE_NAME, null, Constants.COLUMN_LINK_ID  + "='" + remId + "'", null, null, null, null, null);
        NoteItem item = null;
        if (c != null && c.moveToFirst()) {
            String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
            int color  = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            int style  = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
            String date = c.getString(c.getColumnIndex(Constants.COLUMN_DATE));
            String uuID = c.getString(c.getColumnIndex(Constants.COLUMN_UUID));
            byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
            long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
            item = new NoteItem(note, uuID, date, color, style, image, id, remId);
        }
        if (c != null) c.close();
        return item;
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
        throw new SQLiteException("Could not open database");
    }
}