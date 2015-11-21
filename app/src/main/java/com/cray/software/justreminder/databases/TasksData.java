package com.cray.software.justreminder.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.ExchangeConstants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;

public class TasksData {
    private static final String DB_NAME = "tasks_base";
    private static final int DB_VERSION = 1;
    private static final String GOOGLE_TASKS_LISTS_TABLE_NAME = "google_task_lists";
    private static final String GOOGLE_TASKS_TABLE_NAME = "google_tasks";
    private static final String MS_TASKS_TABLE_NAME = "exchange_tasks";
    private static final String ACCOUNTS_TABlE_NAME = "accounts";
    private static final String MS_CONTACTS_TABLE_NAME = "exchange_contacts";
    private static final String MS_COMPANIES_TABLE_NAME = "exchange_companies";
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

    private static final String ACCOUNTS_TABLE_CREATE =
            "create table " + ACCOUNTS_TABlE_NAME + "(" +
                    ExchangeConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    ExchangeConstants.COLUMN_USER + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_PASSWORD + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_DOMAIN + "  VARCHAR(255), " +
                    ExchangeConstants.COLUMN_VERSION + "  VARCHAR(255), " +
                    ExchangeConstants.COLUMN_TECH + "  VARCHAR(255) " +
                    ");";

    private static final String MS_CONTACTS_TABLE_CREATE =
            "create table " + MS_CONTACTS_TABLE_NAME + "(" +
                    ExchangeConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    ExchangeConstants.COLUMN_TASK_ID + " INTEGER, " +
                    ExchangeConstants.COLUMN_VALUE + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_TECH + "  VARCHAR(255) " +
                    ");";

    private static final String MS_COMPANIES_TABLE_CREATE =
            "create table " + MS_COMPANIES_TABLE_NAME + "(" +
                    ExchangeConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    ExchangeConstants.COLUMN_TASK_ID + " INTEGER, " +
                    ExchangeConstants.COLUMN_VALUE + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_TECH + "  VARCHAR(255) " +
                    ");";

    private static final String MS_TASKS_TABLE_CREATE =
            "create table " + MS_TASKS_TABLE_NAME + "(" +
                    ExchangeConstants.COLUMN_ID + " integer primary key autoincrement, " +
                    ExchangeConstants.COLUMN_ACTUAL_WORK + " INTEGER, " +
                    ExchangeConstants.COLUMN_ASSIGNED_TIME + " INTEGER, " +
                    ExchangeConstants.COLUMN_CHANGES_COUNT + " INTEGER, " +
                    ExchangeConstants.COLUMN_COMPLETE_DATE + " INTEGER, " +
                    ExchangeConstants.COLUMN_DUE + " INTEGER, " +
                    ExchangeConstants.COLUMN_IS_COMPLETED + " INTEGER, " +
                    ExchangeConstants.COLUMN_IS_RECURRING + " INTEGER, " +
                    ExchangeConstants.COLUMN_IS_TEAM_TASK + " INTEGER, " +
                    ExchangeConstants.COLUMN_PERCENT_COMPLETE + " REAL, " +
                    ExchangeConstants.COLUMN_START_DATE + " INTEGER, " +
                    ExchangeConstants.COLUMN_TOTAL_WORK + " INTEGER, " +
                    ExchangeConstants.COLUMN_MESSAGE_BODY + " TEXT, " +
                    ExchangeConstants.COLUMN_MILEAGE + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_RECURRENCE + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_STATUS + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_STATUS_DESCRIPTION + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_OWNER + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_TASK_ID + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_TECH_3 + " VARCHAR(255), " +
                    ExchangeConstants.COLUMN_TECH_4 + " INTEGER, " +
                    ExchangeConstants.COLUMN_TECH_5 + " INTEGER, " +
                    ExchangeConstants.COLUMN_TECH_6 + " INTEGER, " +
                    ExchangeConstants.COLUMN_TECH + "  VARCHAR(255) " +
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
            sqLiteDatabase.execSQL(ACCOUNTS_TABLE_CREATE);
            sqLiteDatabase.execSQL(MS_CONTACTS_TABLE_CREATE);
            sqLiteDatabase.execSQL(MS_COMPANIES_TABLE_CREATE);
            sqLiteDatabase.execSQL(MS_TASKS_TABLE_CREATE);
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
        String order = null;
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
        String order = null;
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

    //MS Exchange accounts

    public long addAccount(String login, String password, String domain) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(ExchangeConstants.COLUMN_USER, login);
        cv.put(ExchangeConstants.COLUMN_PASSWORD, password);
        cv.put(ExchangeConstants.COLUMN_DOMAIN, domain);
        return db.insert(ACCOUNTS_TABlE_NAME, null, cv);
    }

    public Cursor getAccount() throws SQLException {
        openGuard();
        return db.query(ACCOUNTS_TABlE_NAME, null, null, null, null, null, null);
    }

    public boolean deleteAccount(long rowId) {
        openGuard();
        return db.delete(ACCOUNTS_TABlE_NAME, ExchangeConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    // MS Exchange tasks

    public long addExchangeTask(String message, int actualWork, long assignedTime, int count,
                                long complete, long due, int completed, int recurring, int team,
                                String mileage, double percent, String recurrence, long start,
                                String status, int total, String description, String owner,
                                String mode, String taskId) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(ExchangeConstants.COLUMN_MESSAGE_BODY, message);
        cv.put(ExchangeConstants.COLUMN_ACTUAL_WORK, actualWork);
        cv.put(ExchangeConstants.COLUMN_ASSIGNED_TIME, assignedTime);
        cv.put(ExchangeConstants.COLUMN_CHANGES_COUNT, count);
        cv.put(ExchangeConstants.COLUMN_COMPLETE_DATE, complete);
        cv.put(ExchangeConstants.COLUMN_DUE, due);
        cv.put(ExchangeConstants.COLUMN_IS_COMPLETED, completed);
        cv.put(ExchangeConstants.COLUMN_IS_RECURRING, recurring);
        cv.put(ExchangeConstants.COLUMN_IS_TEAM_TASK, team);
        cv.put(ExchangeConstants.COLUMN_MILEAGE, mileage);
        cv.put(ExchangeConstants.COLUMN_PERCENT_COMPLETE, percent);
        cv.put(ExchangeConstants.COLUMN_RECURRENCE, recurrence);
        cv.put(ExchangeConstants.COLUMN_START_DATE, start);
        cv.put(ExchangeConstants.COLUMN_STATUS, status);
        cv.put(ExchangeConstants.COLUMN_TOTAL_WORK, total);
        cv.put(ExchangeConstants.COLUMN_STATUS_DESCRIPTION, description);
        cv.put(ExchangeConstants.COLUMN_OWNER, owner);
        cv.put(ExchangeConstants.COLUMN_MODE, mode);
        cv.put(ExchangeConstants.COLUMN_TASK_ID, taskId);
        return db.insert(MS_TASKS_TABLE_NAME, null, cv);
    }

    public boolean updateExchangeTask(long rowId, String message, int actualWork, long assignedTime, int count,
                                      long complete, long due, int completed, int recurring, int team,
                                      String mileage, double percent, String recurrence, long start,
                                      String status, int total, String description, String owner, String mode) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(ExchangeConstants.COLUMN_MESSAGE_BODY, message);
        args.put(ExchangeConstants.COLUMN_ACTUAL_WORK, actualWork);
        args.put(ExchangeConstants.COLUMN_ASSIGNED_TIME, assignedTime);
        args.put(ExchangeConstants.COLUMN_CHANGES_COUNT, count);
        args.put(ExchangeConstants.COLUMN_COMPLETE_DATE, complete);
        args.put(ExchangeConstants.COLUMN_DUE, due);
        args.put(ExchangeConstants.COLUMN_IS_COMPLETED, completed);
        args.put(ExchangeConstants.COLUMN_IS_RECURRING, recurring);
        args.put(ExchangeConstants.COLUMN_IS_TEAM_TASK, team);
        args.put(ExchangeConstants.COLUMN_MILEAGE, mileage);
        args.put(ExchangeConstants.COLUMN_PERCENT_COMPLETE, percent);
        args.put(ExchangeConstants.COLUMN_RECURRENCE, recurrence);
        args.put(ExchangeConstants.COLUMN_START_DATE, start);
        args.put(ExchangeConstants.COLUMN_STATUS, status);
        args.put(ExchangeConstants.COLUMN_TOTAL_WORK, total);
        args.put(ExchangeConstants.COLUMN_STATUS_DESCRIPTION, description);
        args.put(ExchangeConstants.COLUMN_OWNER, owner);
        args.put(ExchangeConstants.COLUMN_MODE, mode);
        return db.update(MS_TASKS_TABLE_NAME, args, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public Cursor getExchangeTasks() throws SQLException {
        openGuard();
        return db.query(MS_TASKS_TABLE_NAME, null, null, null, null, null, null);
    }

    public boolean deleteExchangeTask(long rowId) {
        openGuard();
        return db.delete(MS_TASKS_TABLE_NAME, ExchangeConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    // MS Contacts

    public long addContact(long taskId, String value) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(ExchangeConstants.COLUMN_TASK_ID, taskId);
        cv.put(ExchangeConstants.COLUMN_VALUE, value);
        return db.insert(MS_CONTACTS_TABLE_NAME, null, cv);
    }

    public Cursor getContacts(long id) throws SQLException {
        openGuard();
        return db.query(MS_CONTACTS_TABLE_NAME, null,
                ExchangeConstants.COLUMN_TASK_ID  +
                        "='" + id + "'", null, null, null, null, null);
    }

    public boolean deleteContact(long rowId) {
        openGuard();
        return db.delete(MS_CONTACTS_TABLE_NAME, ExchangeConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    //MS Companies

    public long addCompany(long taskId, String value) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(ExchangeConstants.COLUMN_TASK_ID, taskId);
        cv.put(ExchangeConstants.COLUMN_VALUE, value);
        return db.insert(MS_COMPANIES_TABLE_NAME, null, cv);
    }

    public Cursor getCompanies(long id) throws SQLException {
        openGuard();
        return db.query(MS_COMPANIES_TABLE_NAME, null,
                ExchangeConstants.COLUMN_TASK_ID  +
                        "='" + id + "'", null, null, null, null, null);
    }

    public boolean deleteCompany(long rowId) {
        openGuard();
        return db.delete(MS_COMPANIES_TABLE_NAME, ExchangeConstants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}