package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.helpers.SharedPrefs;

public class TasksData {
    private static final String DB_NAME = "tasks_base";
    private static final int DB_VERSION = 1;
    private static final String GOOGLE_TASKS_LISTS_TABLE_NAME = "google_task_lists";
    private static final String GOOGLE_TASKS_TABLE_NAME = "google_tasks";
    private static final String GOOGLE_DELAYED_TABLE_NAME = "delayed_tasks";
    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String GOOGLE_TASKS_LISTS_TABLE_CREATE =
            "create table " + GOOGLE_TASKS_LISTS_TABLE_NAME + "(" +
                    TasksConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    TasksConstants.COLUMN_TITLE + " VARCHAR(255), " +
                    TasksConstants.COLUMN_LIST_ID + " VARCHAR(255), " +
                    TasksConstants.COLUMN_DEFAULT + " INTEGER, " +
                    TasksConstants.COLUMN_E_TAG + " VARCHAR(255), " +
                    TasksConstants.COLUMN_KIND + " VARCHAR(255), " +
                    TasksConstants.COLUMN_SELF_LINK + " VARCHAR(255), " +
                    TasksConstants.COLUMN_UPDATED + " INTEGER, " +
                    TasksConstants.COLUMN_COLOR + " INTEGER, " +
                    TasksConstants.SYSTEM_DEFAULT + " INTEGER, " +
                    TasksConstants.REMINDER_ID + " INTEGER, " +
                    TasksConstants.COLUMN_TECH_3 + " INTEGER, " +
                    TasksConstants.COLUMN_TECH_4 + "  VARCHAR(255), " +
                    TasksConstants.COLUMN_TECH_5 + "  VARCHAR(255), " +
                    TasksConstants.COLUMN_TECH_6 + "  VARCHAR(255) " +
                    ");";

    private static final String GOOGLE_TASKS_TABLE_CREATE =
            "create table " + GOOGLE_TASKS_TABLE_NAME + "(" +
                    TasksConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    TasksConstants.COLUMN_TITLE + " VARCHAR(255), " +
                    TasksConstants.COLUMN_TASK_ID + " VARCHAR(255), " +
                    TasksConstants.COLUMN_COMPLETED + " INTEGER, " +
                    TasksConstants.COLUMN_DELETED + " INTEGER, " +
                    TasksConstants.COLUMN_DUE + " INTEGER, " +
                    TasksConstants.COLUMN_E_TAG + " VARCHAR(255), " +
                    TasksConstants.COLUMN_KIND + " VARCHAR(255), " +
                    TasksConstants.COLUMN_LINKS + " VARCHAR(255), " +
                    TasksConstants.COLUMN_NOTES + " VARCHAR(255), " +
                    TasksConstants.COLUMN_PARENT + " VARCHAR(255), " +
                    TasksConstants.COLUMN_POSITION + " VARCHAR(255), " +
                    TasksConstants.COLUMN_SELF_LINK + " VARCHAR(255), " +
                    TasksConstants.COLUMN_UPDATED + " INTEGER, " +
                    TasksConstants.COLUMN_REMINDER_ID + " VARCHAR(255), " +
                    TasksConstants.COLUMN_LIST_ID + " VARCHAR(255), " +
                    TasksConstants.COLUMN_STATUS + " VARCHAR(255), " +
                    TasksConstants.COLUMN_HIDDEN + " INTEGER, " +
                    TasksConstants.SYSTEM_DEFAULT + " INTEGER, " +
                    TasksConstants.REMINDER_ID + " INTEGER, " +
                    TasksConstants.COLUMN_TECH_3 + " INTEGER, " +
                    TasksConstants.COLUMN_TECH_4 + "  VARCHAR(255), " +
                    TasksConstants.COLUMN_TECH_5 + "  VARCHAR(255), " +
                    TasksConstants.COLUMN_TECH_6 + "  VARCHAR(255) " +
                    ");";

    private static final String DELAYED_TASKS_TABLE_CREATE =
            "create table " + GOOGLE_DELAYED_TABLE_NAME + "(" +
                    TasksConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    TasksConstants.COLUMN_TITLE + " VARCHAR(255), " +
                    TasksConstants.COLUMN_LIST_ID + " VARCHAR(255), " +
                    TasksConstants.COLUMN_COLOR + " INTEGER, " +
                    TasksConstants.COLUMN_LOCAL_ID + " INTEGER, " +
                    TasksConstants.COLUMN_CODE + " INTEGER, " +
                    TasksConstants.COLUMN_DUE + " INTEGER, " +
                    TasksConstants.COLUMN_TASK_ID + "  VARCHAR(255), " +
                    TasksConstants.COLUMN_NOTES + "  VARCHAR(255), " +
                    TasksConstants.COLUMN_STATUS + "  VARCHAR(255) " +
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

    public TasksData(Context c) {
        mContext = c;
    }

    public TasksData open() throws SQLiteException {
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

    public long addTasksList (String title, String listId, int def, String eTag, String kind,
                              String selfLink, long updated, int color) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(TasksConstants.COLUMN_TITLE, title);
        cv.put(TasksConstants.COLUMN_LIST_ID, listId);
        cv.put(TasksConstants.COLUMN_DEFAULT, def);
        cv.put(TasksConstants.COLUMN_E_TAG, eTag);
        cv.put(TasksConstants.COLUMN_KIND, kind);
        cv.put(TasksConstants.COLUMN_SELF_LINK, selfLink);
        cv.put(TasksConstants.COLUMN_UPDATED, updated);
        cv.put(TasksConstants.COLUMN_COLOR, color);
        return db.insert(GOOGLE_TASKS_LISTS_TABLE_NAME, null, cv);
    }

    public long addTask (String title, String taskId, long completeDate, boolean del, long dueDate,
                         String eTag, String kind, String notes, String parent,
                         String position, String selfLink, long updateDate, long reminderId,
                         String listId, String status, boolean hidden) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(TasksConstants.COLUMN_TITLE, title);
        cv.put(TasksConstants.COLUMN_TASK_ID, taskId);
        cv.put(TasksConstants.COLUMN_COMPLETED, completeDate);
        cv.put(TasksConstants.COLUMN_DELETED, del ? 1 : 0);
        cv.put(TasksConstants.COLUMN_DUE, dueDate);
        cv.put(TasksConstants.COLUMN_E_TAG, eTag);
        cv.put(TasksConstants.COLUMN_KIND, kind);
        cv.put(TasksConstants.COLUMN_NOTES, notes);
        cv.put(TasksConstants.COLUMN_PARENT, parent);
        cv.put(TasksConstants.COLUMN_POSITION, position);
        cv.put(TasksConstants.COLUMN_SELF_LINK, selfLink);
        cv.put(TasksConstants.COLUMN_UPDATED, updateDate);
        cv.put(TasksConstants.COLUMN_REMINDER_ID, reminderId);
        cv.put(TasksConstants.COLUMN_LIST_ID, listId);
        cv.put(TasksConstants.COLUMN_STATUS, status);
        cv.put(TasksConstants.COLUMN_HIDDEN, hidden ? 1 : 0);
        return db.insert(GOOGLE_TASKS_TABLE_NAME, null, cv);
    }

    public boolean updateTasksList(long rowId, String title, String listId, int def, String eTag, String kind,
                                   String selfLink, long updated, int color) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_TITLE, title);
        args.put(TasksConstants.COLUMN_LIST_ID, listId);
        args.put(TasksConstants.COLUMN_DEFAULT, def);
        args.put(TasksConstants.COLUMN_E_TAG, eTag);
        args.put(TasksConstants.COLUMN_KIND, kind);
        args.put(TasksConstants.COLUMN_SELF_LINK, selfLink);
        args.put(TasksConstants.COLUMN_UPDATED, updated);
        args.put(TasksConstants.COLUMN_COLOR, color);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setDefault(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_DEFAULT, 1);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setSystemDefault(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.SYSTEM_DEFAULT, 1);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setSimple(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_DEFAULT, 0);
        return db.update(GOOGLE_TASKS_LISTS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateFullTask(long rowId, String title, String taskId, long completeDate, boolean del, long dueDate,
                              String eTag, String kind, String notes, String parent,
                              String position, String selfLink, long updateDate, long reminderId,
                              String listId, String status, boolean hidden) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_TITLE, title);
        args.put(TasksConstants.COLUMN_TASK_ID, taskId);
        args.put(TasksConstants.COLUMN_COMPLETED, completeDate);
        args.put(TasksConstants.COLUMN_DELETED, del ? 1 : 0);
        args.put(TasksConstants.COLUMN_DUE, dueDate);
        args.put(TasksConstants.COLUMN_E_TAG, eTag);
        args.put(TasksConstants.COLUMN_KIND, kind);
        args.put(TasksConstants.COLUMN_NOTES, notes);
        args.put(TasksConstants.COLUMN_PARENT, parent);
        args.put(TasksConstants.COLUMN_POSITION, position);
        args.put(TasksConstants.COLUMN_SELF_LINK, selfLink);
        args.put(TasksConstants.COLUMN_UPDATED, updateDate);
        args.put(TasksConstants.COLUMN_REMINDER_ID, reminderId);
        args.put(TasksConstants.COLUMN_LIST_ID, listId);
        args.put(TasksConstants.COLUMN_STATUS, status);
        args.put(TasksConstants.COLUMN_HIDDEN, hidden ? 1 : 0);
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateTask(long rowId, String title, long dueDate,
                                  String notes, String status, long remId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_TITLE, title);
        args.put(TasksConstants.COLUMN_DUE, dueDate);
        args.put(TasksConstants.COLUMN_NOTES, notes);
        args.put(TasksConstants.COLUMN_STATUS, status);
        args.put(TasksConstants.COLUMN_COMPLETED, 0);
        args.put(TasksConstants.COLUMN_REMINDER_ID, remId);
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateTask(long rowId, String title, long dueDate,
                              String notes, String status, long remId, String listId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_TITLE, title);
        args.put(TasksConstants.COLUMN_DUE, dueDate);
        args.put(TasksConstants.COLUMN_NOTES, notes);
        args.put(TasksConstants.COLUMN_STATUS, status);
        args.put(TasksConstants.COLUMN_LIST_ID, listId);
        args.put(TasksConstants.COLUMN_COMPLETED, 0);
        args.put(TasksConstants.COLUMN_REMINDER_ID, remId);
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean updateTask(long rowId, String listId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_STATUS, GTasksHelper.TASKS_NEED_ACTION);
        args.put(TasksConstants.COLUMN_LIST_ID, listId);
        args.put(TasksConstants.COLUMN_COMPLETED, 0);
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setTaskDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_STATUS, GTasksHelper.TASKS_COMPLETE);
        args.put(TasksConstants.COLUMN_COMPLETED, System.currentTimeMillis());
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean setTaskUnDone(long rowId) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(TasksConstants.COLUMN_STATUS, GTasksHelper.TASKS_NEED_ACTION);
        args.put(TasksConstants.COLUMN_COMPLETED, 0);
        return db.update(GOOGLE_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getTasksLists() throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor getTasks() throws SQLException {
        openGuard();
        SharedPrefs prefs = new SharedPrefs(mContext);
        String orderPrefs = prefs.loadPrefs(Prefs.TASKS_ORDER);
        String order;
        if (orderPrefs.matches(Constants.ORDER_DEFAULT)){
            order = TasksConstants.COLUMN_POSITION + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = TasksConstants.COLUMN_DUE + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = TasksConstants.COLUMN_DUE + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_Z_A)){
            order = TasksConstants.COLUMN_STATUS + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_A_Z)){
            order = TasksConstants.COLUMN_STATUS + " ASC";
        } else {
            order = TasksConstants.COLUMN_POSITION + " ASC";
        }
        return db.query(GOOGLE_TASKS_TABLE_NAME, null, null, null, null, null, order);
    }

    public Cursor getTasksList(long rowId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null,
                Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor getTasksList(String listId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null,
                TasksConstants.COLUMN_LIST_ID  +
                        "='" + listId + "'", null, null, null, null, null);
    }

    public Cursor getDefaultTasksList() throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_LISTS_TABLE_NAME, null,
                TasksConstants.COLUMN_DEFAULT  +
                        "='" + 1 + "'", null, null, null, null, null);
    }

    public Cursor getTask(long rowId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_TABLE_NAME, null,
                Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor getTask(String taskId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_TABLE_NAME, null,
                TasksConstants.COLUMN_TASK_ID  +
                        "='" + taskId + "'", null, null, null, null, null);
    }

    public Cursor getTaskByReminder (long remId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_TASKS_TABLE_NAME, null,
                TasksConstants.COLUMN_REMINDER_ID  +
                        "='" + remId + "'", null, null, null, null, null);
    }

    public Cursor getTasks(String listId) throws SQLException {
        openGuard();
        SharedPrefs prefs = new SharedPrefs(mContext);
        String orderPrefs = prefs.loadPrefs(Prefs.TASKS_ORDER);
        String order;
        if (orderPrefs.matches(Constants.ORDER_DEFAULT)){
            order = TasksConstants.COLUMN_POSITION + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)){
            order = TasksConstants.COLUMN_DUE + " ASC";
        } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)){
            order = TasksConstants.COLUMN_DUE + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_Z_A)){
            order = TasksConstants.COLUMN_STATUS + " DESC";
        } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_A_Z)){
            order = TasksConstants.COLUMN_STATUS + " ASC";
        } else {
            order = TasksConstants.COLUMN_POSITION + " ASC";
        }
        return db.query(GOOGLE_TASKS_TABLE_NAME, null,
                TasksConstants.COLUMN_LIST_ID  +
                        "='" + listId + "'", null, null, null, order, null);
    }

    public boolean deleteTasksList(long rowId) {
        openGuard();
        return db.delete(GOOGLE_TASKS_LISTS_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public boolean deleteTask(long rowId) {
        openGuard();
        return db.delete(GOOGLE_TASKS_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public int getCount() throws SQLException {
        openGuard();
        String countQuery = "SELECT " + Constants.COLUMN_ID + " FROM " + GOOGLE_TASKS_LISTS_TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    //work with delayed tasks

    public boolean delete(long rowId) {
        openGuard();
        return db.delete(GOOGLE_DELAYED_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor get(long rowId) throws SQLException {
        openGuard();
        return db.query(GOOGLE_DELAYED_TABLE_NAME, null,
                Constants.COLUMN_ID  + "=" + rowId, null, null, null, null, null);
    }

    public Cursor get() throws SQLException {
        openGuard();
        return db.query(GOOGLE_DELAYED_TABLE_NAME, null, null, null, null, null, null);
    }

    public long add(String title, String listId, int code, int color, String taskId, String note,
                              long id, long time, String status) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(TasksConstants.COLUMN_TITLE, title);
        cv.put(TasksConstants.COLUMN_LIST_ID, listId);
        cv.put(TasksConstants.COLUMN_COLOR, color);
        cv.put(TasksConstants.COLUMN_LOCAL_ID, id);
        cv.put(TasksConstants.COLUMN_CODE, code);
        cv.put(TasksConstants.COLUMN_TASK_ID, taskId);
        cv.put(TasksConstants.COLUMN_DUE, time);
        cv.put(TasksConstants.COLUMN_NOTES, note);
        cv.put(TasksConstants.COLUMN_STATUS, status);
        return db.insert(GOOGLE_DELAYED_TABLE_NAME, null, cv);
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}