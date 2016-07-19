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

package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.templates.TemplateItem;

import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private static final String DB_NAME = "just_database";
    private static final int DB_VERSION = 14;
    private static final String CURRENT_TABLE_NAME = "current_task_table";
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

    public class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
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
                    break;
                case 2:
                    db.execSQL(LOCATION_TABLE_CREATE);
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 3:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 4:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL("DELETE FROM " + NOTE_TABLE_NAME);
                    break;
                case 5:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 6:
                    db.execSQL(EVENTS_TABLE_CREATE);
                    db.execSQL(CALLS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
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

    @Deprecated
    public Cursor queryAllReminders() throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, null, null, null, null, null);
    }

    @Deprecated
    public boolean deleteReminder(long rowId) {
        openGuard();
        return db.delete(CURRENT_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    // Contacts birthdays database

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
        args.put(Constants.ContactConstants.COLUMN_CONTACT_VAR, "");
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

    //Working with SMS templates table

    public long saveTemplate (TemplateItem templateItem) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, templateItem.getTitle());
        cv.put(Constants.COLUMN_DATE_TIME, templateItem.getDate());
        if (templateItem.getId() == 0) {
            return db.insert(MESSAGES_TABLE_NAME, null, cv);
        } else {
            return db.update(MESSAGES_TABLE_NAME, cv, Constants.COLUMN_ID + "=" + templateItem.getId(), null);
        }
    }

    public TemplateItem getTemplate(long id) throws SQLException {
        openGuard();
        Cursor c = db.query(MESSAGES_TABLE_NAME, null, Constants.COLUMN_ID  +
                "=" + id, null, null, null, null, null);
        TemplateItem templateItem = null;
        if (c != null && c.moveToFirst()) {
            String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            long date = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
            templateItem = new TemplateItem(title, id, date);
        }
        if (c != null) c.close();
        return templateItem;
    }

    public List<TemplateItem> queryTemplates() throws SQLException {
        openGuard();
        List<TemplateItem> list = new ArrayList<>();
        Cursor c = db.query(MESSAGES_TABLE_NAME, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                long date = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                list.add(new TemplateItem(title, id, date));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public boolean deleteTemplate(long rowId) {
        openGuard();
        return db.delete(MESSAGES_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //Working with reminder categories table

    public long setGroup(GroupItem groupItem) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_TEXT, groupItem.getTitle());
        cv.put(Constants.COLUMN_DATE_TIME, groupItem.getDateTime());
        cv.put(Constants.COLUMN_TECH_VAR, groupItem.getUuId());
        cv.put(Constants.COLUMN_COLOR, groupItem.getColor());
        if (groupItem.getId() == 0) {
            return db.insert(CATEGORIES_TABLE_NAME, null, cv);
        } else {
            return db.update(CATEGORIES_TABLE_NAME, cv, Constants.COLUMN_ID + "=" + groupItem.getId(), null);
        }
    }

    public boolean changeGroupColor(long rowId, int color){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(Constants.COLUMN_COLOR, color);
        return db.update(CATEGORIES_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public GroupItem getGroup(long id) throws SQLException {
        openGuard();
        Cursor c = db.query(CATEGORIES_TABLE_NAME, null, Constants.COLUMN_ID  +
                "=" + id, null, null, null, null, null);
        GroupItem groupItem = null;
        if (c != null && c.moveToFirst()) {
            String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            long date = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
            groupItem = new GroupItem(text, uuId, color, id, date);
        }
        if (c != null) c.close();
        return groupItem;
    }

    public GroupItem getGroup(String uuId) throws SQLException {
        openGuard();
        Cursor c = db.query(CATEGORIES_TABLE_NAME, null,
                Constants.COLUMN_TECH_VAR  +
                        "='" + uuId + "'", null, null, null, null, null);
        GroupItem groupItem = null;
        if (c != null && c.moveToFirst()) {
            String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
            long date = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
            long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
            groupItem = new GroupItem(text, uuId, color, id, date);
        }
        if (c != null) c.close();
        return groupItem;
    }

    public List<GroupItem> getAllGroups() throws SQLException {
        openGuard();
        List<GroupItem> list = new ArrayList<>();
        Cursor c = db.query(CATEGORIES_TABLE_NAME, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                long date = c.getLong(c.getColumnIndex(Constants.COLUMN_DATE_TIME));
                list.add(new GroupItem(text, uuId, color, id, date));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public boolean deleteGroup(long rowId) {
        openGuard();
        return db.delete(CATEGORIES_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    @Deprecated
    public Cursor getShopItemsActive(long remId) throws SQLException {
        openGuard();
        return db.query(SHOPPING_TABLE_NAME, null, Constants.COLUMN_REMINDER_ID + "=" + remId +
                        " AND "+ Constants.COLUMN_EXTRA_1 + "=" + 1, null, null, null,
                Constants.COLUMN_ARCHIVED + " ASC, " + Constants.COLUMN_DATE_TIME + " ASC", null);
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        //Log.d(LOG_TAG, "open guard failed");
        throw new SQLiteException("Could not open database");
    }
}