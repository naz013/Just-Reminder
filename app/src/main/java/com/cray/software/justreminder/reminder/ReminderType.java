package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class ReminderType {

    private Context context;
    private int view;
    private String type;

    public ReminderType(Context context){
        this.context = context;
        this.type = "";
    }

    public void inflateView(int view){
        this.view = view;
    }

    public DataItem getItem(long id){
        DataBase db = new DataBase(context);
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
            int exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            int expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
            int ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
            String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
            String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));

            c.close();
            db.close();

            return new DataItem(text, type, weekdays, melody, catId, uuId,
                    new double[]{latitude, longitude}, number, myDay, myMonth, myYear, myHour, myMinute,
                    mySeconds, repCode, exp, radius, ledColor, expTasks, afterTime, due);
        } else return null;
    }

    public int getView(){
        return view;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public long save(DataItem item){
        DataBase db = new DataBase(context);
        db.open();
        long id = db.insertReminder(item.getTitle(), item.getType(), item.getDay(), item.getMonth(),
                item.getYear(), item.getHour(), item.getMinute(), item.getSeconds(),
                item.getNumber(), item.getRepCode(), item.getRepMinute(), 0, item.getPlace()[0],
                item.getPlace()[1], item.getUuId(), item.getWeekdays(), item.getExport(),
                item.getMelody(), item.getRadius(), item.getColor(), item.getCode(),
                item.getCategoryId());
        db.updateReminderDateTime(id);
        db.close();
        updateViews();
        return id;
    }

    public void save(long id, DataItem item){
        DataBase db = new DataBase(context);
        db.open();
        db.updateReminder(id, item.getTitle(), item.getType(), item.getDay(), item.getMonth(),
                item.getYear(), item.getHour(), item.getMinute(), item.getSeconds(),
                item.getNumber(), item.getRepCode(), item.getRepMinute(), 0, item.getPlace()[0],
                item.getPlace()[1], item.getWeekdays(), item.getExport(), item.getMelody(),
                item.getRadius(), item.getColor(), item.getCode(), item.getCategoryId());
        db.updateReminderDateTime(id);
        db.close();
        updateViews();
    }

    protected void exportToServices(DataItem item, long id){
        SharedPrefs prefs = new SharedPrefs(context);
        boolean stock = prefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        boolean calendar = prefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        if (item.getExport() == 1) ReminderUtils.exportToCalendar(context, item.getTitle(), item.getDue(), id, calendar, stock);
        if (item.getCode() == Constants.SYNC_GTASKS_ONLY) ReminderUtils.exportToTasks(context, item.getTitle(), item.getDue(), id);
    }

    private void updateViews(){
        new Notifier(context).recreatePermanent();
        new UpdatesHelper(context).updateWidget();
    }
}
