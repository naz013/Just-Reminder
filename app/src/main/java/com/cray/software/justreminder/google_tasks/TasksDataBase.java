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

package com.cray.software.justreminder.google_tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class TasksDataBase {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title_column";
    public static final String COLUMN_LIST_ID = "list_id_column";
    public static final String COLUMN_TASK_ID = "task_id_column";
    public static final String COLUMN_DEFAULT = "default_column";
    public static final String COLUMN_E_TAG = "e_tag_column";
    public static final String COLUMN_KIND = "kind_column";
    public static final String COLUMN_SELF_LINK = "self_link_column";
    public static final String COLUMN_UPDATED = "updated_column";
    public static final String COLUMN_COLOR = "color_column";
    public static final String COLUMN_COMPLETED = "completed_column";
    public static final String COLUMN_DELETED = "deleted_column";
    public static final String COLUMN_DUE = "due_column";
    public static final String COLUMN_LINKS = "links_column";
    public static final String COLUMN_NOTES = "notes_column";
    public static final String COLUMN_PARENT = "parent_column";
    public static final String COLUMN_POSITION = "position_column";
    public static final String COLUMN_REMINDER_ID = "reminder_id_column";
    public static final String SYSTEM_DEFAULT = "tech_column";
    public static final String REMINDER_ID = "tech_ii_column";
    public static final String COLUMN_TECH_3 = "tech_iii_column";
    public static final String COLUMN_TECH_4 = "tech_iv_column";
    public static final String COLUMN_TECH_5 = "tech_v_column";
    public static final String COLUMN_TECH_6 = "tech_vi_column";
    public static final String COLUMN_STATUS = "status_column";
    public static final String COLUMN_HIDDEN = "hidden_column";

    public static final String COLUMN_CODE = "code_column";
    public static final String COLUMN_LOCAL_ID = "local_id_column";

    private static final String DB_NAME = "tasks_base";
    private static final int DB_VERSION = 1;
    private static final String GOOGLE_TASKS_LISTS_TABLE_NAME = "google_task_lists";
    private static final String GOOGLE_TASKS_TABLE_NAME = "google_tasks";
    private static final String GOOGLE_DELAYED_TABLE_NAME = "delayed_tasks";
    private static final String TAG = "TasksDataBase";
    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String GOOGLE_TASKS_LISTS_TABLE_CREATE =
            "create table " + GOOGLE_TASKS_LISTS_TABLE_NAME + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TITLE + " VARCHAR(255), " +
                    COLUMN_LIST_ID + " VARCHAR(255), " +
                    COLUMN_DEFAULT + " INTEGER, " +
                    COLUMN_E_TAG + " VARCHAR(255), " +
                    COLUMN_KIND + " VARCHAR(255), " +
                    COLUMN_SELF_LINK + " VARCHAR(255), " +
                    COLUMN_UPDATED + " INTEGER, " +
                    COLUMN_COLOR + " INTEGER, " +
                    SYSTEM_DEFAULT + " INTEGER, " +
                    REMINDER_ID + " INTEGER, " +
                    COLUMN_TECH_3 + " INTEGER, " +
                    COLUMN_TECH_4 + "  VARCHAR(255), " +
                    COLUMN_TECH_5 + "  VARCHAR(255), " +
                    COLUMN_TECH_6 + "  VARCHAR(255) " +
                    ");";

    private static final String GOOGLE_TASKS_TABLE_CREATE =
            "create table " + GOOGLE_TASKS_TABLE_NAME + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TITLE + " VARCHAR(255), " +
                    COLUMN_TASK_ID + " VARCHAR(255), " +
                    COLUMN_COMPLETED + " INTEGER, " +
                    COLUMN_DELETED + " INTEGER, " +
                    COLUMN_DUE + " INTEGER, " +
                    COLUMN_E_TAG + " VARCHAR(255), " +
                    COLUMN_KIND + " VARCHAR(255), " +
                    COLUMN_LINKS + " VARCHAR(255), " +
                    COLUMN_NOTES + " VARCHAR(255), " +
                    COLUMN_PARENT + " VARCHAR(255), " +
                    COLUMN_POSITION + " VARCHAR(255), " +
                    COLUMN_SELF_LINK + " VARCHAR(255), " +
                    COLUMN_UPDATED + " INTEGER, " +
                    COLUMN_REMINDER_ID + " VARCHAR(255), " +
                    COLUMN_LIST_ID + " VARCHAR(255), " +
                    COLUMN_STATUS + " VARCHAR(255), " +
                    COLUMN_HIDDEN + " INTEGER, " +
                    SYSTEM_DEFAULT + " INTEGER, " +
                    REMINDER_ID + " INTEGER, " +
                    COLUMN_TECH_3 + " INTEGER, " +
                    COLUMN_TECH_4 + "  VARCHAR(255), " +
                    COLUMN_TECH_5 + "  VARCHAR(255), " +
                    COLUMN_TECH_6 + "  VARCHAR(255) " +
                    ");";

    private static final String DELAYED_TASKS_TABLE_CREATE =
            "create table " + GOOGLE_DELAYED_TABLE_NAME + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TITLE + " VARCHAR(255), " +
                    COLUMN_LIST_ID + " VARCHAR(255), " +
                    COLUMN_COLOR + " INTEGER, " +
                    COLUMN_LOCAL_ID + " INTEGER, " +
                    COLUMN_CODE + " INTEGER, " +
                    COLUMN_DUE + " INTEGER, " +
                    COLUMN_TASK_ID + "  VARCHAR(255), " +
                    COLUMN_NOTES + "  VARCHAR(255), " +
                    COLUMN_STATUS + "  VARCHAR(255) " +
                    ");";

    public class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(GOOGLE_TASKS_LISTS_TABLE_CREATE);
            sqLiteDatabase.execSQL(GOOGLE_TASKS_TABLE_CREATE);
            sqLiteDatabase.execSQL(DELAYED_TASKS_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public TasksDataBase(Context c) {
        mContext = c;
    }

    public TasksDataBase open() throws SQLiteException {
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

    public long saveTaskList(TaskListItem item) {
        openGuard();
        Log.d(TAG, "saveTaskList: id " + item.getId() + ", color " + item.getColor());
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, item.getTitle());
        cv.put(COLUMN_LIST_ID, item.getListId());
        cv.put(COLUMN_DEFAULT, item.getDef());
        cv.put(COLUMN_E_TAG, item.geteTag());
        cv.put(COLUMN_KIND, item.getKind());
        cv.put(COLUMN_SELF_LINK, item.getSelfLink());
        cv.put(COLUMN_UPDATED, item.getUpdated());
        cv.put(COLUMN_COLOR, item.getColor());
        if (item.getId() == 0) {
            return db.insert(GOOGLE_TASKS_LISTS_TABLE_NAME, null, cv);
        } else {
            db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, cv, COLUMN_ID + "=" + item.getId(), null);
            return item.getId();
        }
    }

    public List<TaskListItem> getTaskLists() throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null, null, null, null, null, null);
        List<TaskListItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(taskListFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    private TaskListItem taskListFromCursor(Cursor c) {
        long id = c.getLong(c.getColumnIndex(COLUMN_ID));
        long updated = c.getLong(c.getColumnIndex(COLUMN_UPDATED));
        int def = c.getInt(c.getColumnIndex(COLUMN_DEFAULT));
        int color = c.getInt(c.getColumnIndex(COLUMN_COLOR));
        int sys = c.getInt(c.getColumnIndex(SYSTEM_DEFAULT));
        String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
        String listId = c.getString(c.getColumnIndex(COLUMN_LIST_ID));
        String eTag = c.getString(c.getColumnIndex(COLUMN_E_TAG));
        String kind = c.getString(c.getColumnIndex(COLUMN_KIND));
        String selfLink = c.getString(c.getColumnIndex(COLUMN_SELF_LINK));
        return new TaskListItem(title, listId, def, eTag, kind, selfLink, updated, color, id, sys);
    }

    public TaskListItem getTasksList(long id) throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null, COLUMN_ID  + "=" + id, null, null, null, null, null);
        TaskListItem item = null;
        if (c != null && c.moveToFirst()) {
            item = taskListFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public TaskListItem getTasksList(String listId) throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null, COLUMN_LIST_ID  + "='" + listId + "'", null, null, null, null, null);
        TaskListItem item = null;
        if (c != null && c.moveToFirst()) {
            item = taskListFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public TaskListItem getDefaultTasksList() throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null, COLUMN_DEFAULT  + "='" + 1 + "'", null, null, null, null, null);
        TaskListItem item = null;
        if (c != null && c.moveToFirst()) {
            item = taskListFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public boolean setDefault(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(COLUMN_DEFAULT, 1);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setSystemDefault(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(SYSTEM_DEFAULT, 1);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setSimple(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(COLUMN_DEFAULT, 0);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public long saveTask(TaskItem item) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, item.getTitle());
        cv.put(COLUMN_TASK_ID, item.getTaskId());
        cv.put(COLUMN_COMPLETED, item.getCompleteDate());
        cv.put(COLUMN_DELETED, item.getDel());
        cv.put(COLUMN_DUE, item.getDueDate());
        cv.put(COLUMN_E_TAG, item.geteTag());
        cv.put(COLUMN_KIND, item.getKind());
        cv.put(COLUMN_NOTES, item.getNotes());
        cv.put(COLUMN_PARENT, item.getParent());
        cv.put(COLUMN_POSITION, item.getPosition());
        cv.put(COLUMN_SELF_LINK, item.getSelfLink());
        cv.put(COLUMN_UPDATED, item.getUpdateDate());
        cv.put(COLUMN_REMINDER_ID, item.getReminderId());
        cv.put(COLUMN_LIST_ID, item.getListId());
        cv.put(COLUMN_STATUS, item.getStatus());
        cv.put(COLUMN_HIDDEN, item.getHidden());
        if (item.getId() == 0) {
            return db.insert(GOOGLE_TASKS_TABLE_NAME, null, cv);
        } else {
            return db.update(GOOGLE_TASKS_TABLE_NAME, cv, COLUMN_ID + "=" + item.getId(), null);
        }
    }

    public List<TaskItem> getTasks() throws SQLException {
        openGuard();
        String orderPrefs = SharedPrefs.getInstance(mContext).getString(Prefs.TASKS_ORDER);
        String order;
        if (orderPrefs.matches(Constants.ORDER_DEFAULT)){
            order = COLUMN_POSITION + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = COLUMN_DUE + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = COLUMN_DUE + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_Z_A)){
            order = COLUMN_STATUS + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_A_Z)){
            order = COLUMN_STATUS + " ASC";
        } else {
            order = COLUMN_POSITION + " ASC";
        }
        Cursor c = db.query(GOOGLE_TASKS_TABLE_NAME, null, null, null, null, null, order);
        List<TaskItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(taskFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    private TaskItem taskFromCursor(Cursor c) {
        long id = c.getLong(c.getColumnIndex(COLUMN_ID));
        long updated = c.getLong(c.getColumnIndex(COLUMN_UPDATED));
        long completed = c.getLong(c.getColumnIndex(COLUMN_COMPLETED));
        long due = c.getLong(c.getColumnIndex(COLUMN_DUE));
        long reminderId = c.getLong(c.getColumnIndex(COLUMN_REMINDER_ID));
        int del = c.getInt(c.getColumnIndex(COLUMN_DELETED));
        int hidden = c.getInt(c.getColumnIndex(COLUMN_HIDDEN));
        String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
        String listId = c.getString(c.getColumnIndex(COLUMN_LIST_ID));
        String taskId = c.getString(c.getColumnIndex(COLUMN_TASK_ID));
        String eTag = c.getString(c.getColumnIndex(COLUMN_E_TAG));
        String note = c.getString(c.getColumnIndex(COLUMN_NOTES));
        String kind = c.getString(c.getColumnIndex(COLUMN_KIND));
        String selfLink = c.getString(c.getColumnIndex(COLUMN_SELF_LINK));
        String parent = c.getString(c.getColumnIndex(COLUMN_PARENT));
        String position = c.getString(c.getColumnIndex(COLUMN_POSITION));
        String status = c.getString(c.getColumnIndex(COLUMN_STATUS));
        return new TaskItem(title, taskId, completed, del, due, eTag, kind, note, parent,
                position, selfLink, updated, reminderId, listId, status, hidden, id);
    }

    public TaskItem getTask(long id) throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_TABLE_NAME, null, COLUMN_ID  + "=" + id, null, null, null, null, null);
        TaskItem item = null;
        if (c != null && c.moveToFirst()) {
            item = taskFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public TaskItem getTask(String taskId) throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_TABLE_NAME, null, COLUMN_TASK_ID  + "='" + taskId + "'", null, null, null, null, null);
        TaskItem item = null;
        if (c != null && c.moveToFirst()) {
            item = taskFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public TaskItem getTaskByReminder(long reminderId) throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_TABLE_NAME, null, COLUMN_REMINDER_ID  + "='" + reminderId + "'", null, null, null, null, null);
        TaskItem item = null;
        if (c != null && c.moveToFirst()) {
            item = taskFromCursor(c);
        }
        if (c != null) c.close();
        return item;
    }

    public List<TaskItem> getTasks(String listId) throws SQLException {
        openGuard();
        String orderPrefs = SharedPrefs.getInstance(mContext).getString(Prefs.TASKS_ORDER);
        String order;
        if (orderPrefs.matches(Constants.ORDER_DEFAULT)){
            order = COLUMN_POSITION + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = COLUMN_DUE + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = COLUMN_DUE + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_Z_A)){
            order = COLUMN_STATUS + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_A_Z)){
            order = COLUMN_STATUS + " ASC";
        } else {
            order = COLUMN_POSITION + " ASC";
        }
        Cursor c = db.query(GOOGLE_TASKS_TABLE_NAME, null, COLUMN_LIST_ID  + "='" + listId + "'", null, null, null, order, null);
        List<TaskItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(taskFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public List<TaskItem> getCompletedTasks(String listId) throws SQLException {
        openGuard();
        Cursor c = db.query(GOOGLE_TASKS_TABLE_NAME, null, COLUMN_LIST_ID  + "='" + listId + "' AND " +
                COLUMN_STATUS + "='" + GTasksHelper.TASKS_COMPLETE + "'", null, null, null, null, null);
        List<TaskItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(taskFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    public boolean setTaskDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(COLUMN_STATUS, GTasksHelper.TASKS_COMPLETE);
        args.put(COLUMN_COMPLETED, System.currentTimeMillis());
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setTaskUnDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(COLUMN_STATUS, GTasksHelper.TASKS_NEED_ACTION);
        args.put(COLUMN_COMPLETED, 0);
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean deleteTasksList(long rowId) {
        openGuard();
        return db.delete(GOOGLE_TASKS_LISTS_TABLE_NAME, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean deleteTask(long rowId) {
        openGuard();
        return db.delete(GOOGLE_TASKS_TABLE_NAME, COLUMN_ID + "=" + rowId, null) > 0;
    }

    //work with delayed tasks
    public boolean delete(long rowId) {
        openGuard();
        return db.delete(GOOGLE_DELAYED_TABLE_NAME, COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor get(long rowId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_DELAYED_TABLE_NAME, null, COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor get() throws SQLException {
        openGuard();
        return db.query(GOOGLE_DELAYED_TABLE_NAME, null, null, null, null, null, null);
    }

    public long add(String title, String listId, int code, int color, String taskId, String note,
                              long id, long time, String status) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_LIST_ID, listId);
        cv.put(COLUMN_COLOR, color);
        cv.put(COLUMN_LOCAL_ID, id);
        cv.put(COLUMN_CODE, code);
        cv.put(COLUMN_TASK_ID, taskId);
        cv.put(COLUMN_DUE, time);
        cv.put(COLUMN_NOTES, note);
        cv.put(COLUMN_STATUS, status);
        return db.insert(GOOGLE_DELAYED_TABLE_NAME, null, cv);
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}