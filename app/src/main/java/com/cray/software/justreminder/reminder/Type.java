package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class Type {

    private Context mContext;
    private int view;
    private String type;

    public Type(Context context){
        this.mContext = context;
        this.type = "";
    }

    /**
     * Inflate layout file for reminder.
     * @param view layout resource identifier.
     */
    public void inflateView(int view){
        this.view = view;
    }

    /**
     * Get reminder object.
     * @param id reminder identifier.
     * @return reminder object
     */
    public Reminder getItem(long id){
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
            int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            int mySeconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            long afterTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            long due = c.getLong(c.getColumnIndex(Constants.COLUMN_FEATURE_TIME));
            long count = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            int exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            int expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
            int ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
            int voice = c.getInt(c.getColumnIndex(Constants.COLUMN_VOICE));
            int vibration = c.getInt(c.getColumnIndex(Constants.COLUMN_VIBRATION));
            int notificationRepeat = c.getInt(c.getColumnIndex(Constants.COLUMN_NOTIFICATION_REPEAT));
            int wake = c.getInt(c.getColumnIndex(Constants.COLUMN_WAKE_SCREEN));
            int unlock = c.getInt(c.getColumnIndex(Constants.COLUMN_UNLOCK_DEVICE));
            int auto = c.getInt(c.getColumnIndex(Constants.COLUMN_AUTO_ACTION));
            long limit = c.getLong(c.getColumnIndex(Constants.COLUMN_REPEAT_LIMIT));
            String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
            String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

            c.close();
            db.close();

            return new Reminder(id, text, type, weekdays, melody, catId, uuId,
                    new double[]{latitude, longitude}, number, myDay, myMonth, myYear, myHour, myMinute,
                    mySeconds, repCode, exp, radius, ledColor, expTasks, afterTime, due, count, vibration,
                    voice, notificationRepeat, wake, unlock, auto, limit);
        } else return null;
    }

    /**
     * Get reminder object.
     * @param uuId reminder unique identifier.
     * @return reminder object
     */
    public Reminder getItem(String uuId){
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminder(uuId);
        if (c != null && c.moveToFirst()){
            String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
            int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
            int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
            int mySeconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
            int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            long afterTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
            long due = c.getLong(c.getColumnIndex(Constants.COLUMN_FEATURE_TIME));
            long count = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
            long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
            int exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            int expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
            int ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
            int voice = c.getInt(c.getColumnIndex(Constants.COLUMN_VOICE));
            int vibration = c.getInt(c.getColumnIndex(Constants.COLUMN_VIBRATION));
            int notificationRepeat = c.getInt(c.getColumnIndex(Constants.COLUMN_NOTIFICATION_REPEAT));
            int wake = c.getInt(c.getColumnIndex(Constants.COLUMN_WAKE_SCREEN));
            int unlock = c.getInt(c.getColumnIndex(Constants.COLUMN_UNLOCK_DEVICE));
            int auto = c.getInt(c.getColumnIndex(Constants.COLUMN_AUTO_ACTION));
            long limit = c.getLong(c.getColumnIndex(Constants.COLUMN_REPEAT_LIMIT));
            String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
            double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

            c.close();
            db.close();

            return new Reminder(id, text, type, weekdays, melody, catId, uuId,
                    new double[]{latitude, longitude}, number, myDay, myMonth, myYear, myHour, myMinute,
                    mySeconds, repCode, exp, radius, ledColor, expTasks, afterTime, due, count, vibration,
                    voice, notificationRepeat, wake, unlock, auto, limit);
        } else return null;
    }

    /**
     * Get reminder layout resource identifier.
     * @return
     */
    public int getView(){
        return view;
    }

    /**
     * Set reminder type.
     * @param type
     */
    public void setType(String type){
        this.type = type;
    }

    /**
     * Get reminder type.
     * @return
     */
    public String getType(){
        return type;
    }

    /**
     * Save new reminder to database.
     * @param item reminder object.
     * @return
     */
    public long save(Reminder item){
        DataBase db = new DataBase(mContext);
        db.open();
        long id = db.insertReminder(item.getTitle(), item.getType(), item.getDay(), item.getMonth(),
                item.getYear(), item.getHour(), item.getMinute(), item.getSeconds(),
                item.getNumber(), item.getRepCode(), item.getRepMinute(), 0, item.getPlace()[0],
                item.getPlace()[1], item.getUuId(), item.getWeekdays(), item.getExport(),
                item.getMelody(), item.getRadius(), item.getColor(), item.getCode(),
                item.getCategoryId());
        db.updateReminderDateTime(id);
        db.updateReminderExtra(id, item.getVibration(), item.getVoice(), item.getNotificationRepeat(),
                item.getWake(), item.getUnlock(), item.getAuto(), item.getLimit());
        db.close();
        updateViews();
        return id;
    }

    /**
     * Update reminder in database.
     * @param id reminder identifier.
     * @param item reminder object.
     */
    public void save(long id, Reminder item){
        DataBase db = new DataBase(mContext);
        db.open();
        db.updateReminder(id, item.getTitle(), item.getType(), item.getDay(), item.getMonth(),
                item.getYear(), item.getHour(), item.getMinute(), item.getSeconds(),
                item.getNumber(), item.getRepCode(), item.getRepMinute(), 0, item.getPlace()[0],
                item.getPlace()[1], item.getWeekdays(), item.getExport(), item.getMelody(),
                item.getRadius(), item.getColor(), item.getCode(), item.getCategoryId());
        db.updateReminderDateTime(id);
        db.updateReminderExtra(id, item.getVibration(), item.getVoice(), item.getNotificationRepeat(),
                item.getWake(), item.getUnlock(), item.getAuto(), item.getLimit());
        db.close();
        updateViews();
    }

    /**
     * Add reminder to Google, Stock Calendar and/or Google Tasks.
     * @param item reminder object.
     * @param id reminder identifier.
     */
    protected void exportToServices(Reminder item, long id){
        SharedPrefs prefs = new SharedPrefs(mContext);
        boolean stock = prefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        boolean calendar = prefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        if (item.getExport() == 1) ReminderUtils.exportToCalendar(mContext, item.getTitle(), item.getDue(), id, calendar, stock);
        if (item.getCode() == Constants.SYNC_GTASKS_ONLY) ReminderUtils.exportToTasks(mContext, item.getTitle(), item.getDue(), id);
    }

    /**
     * Update all application widgets and permanent notification in Status Bar.
     */
    private void updateViews(){
        new Notifier(mContext).recreatePermanent();
        new UpdatesHelper(mContext).updateWidget();
    }
}
