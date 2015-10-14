package com.cray.software.justreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.CalendarData;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class CurrentTaskFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<CalendarData> data;
    private Context mContext;
    private DataBase db;
    private int widgetID;

    CurrentTaskFactory(Context ctx, Intent intent) {
        mContext = ctx;
        widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        data = new ArrayList<>();
        db = new DataBase(mContext);
    }

    @Override
    public void onDataSetChanged() {
        data.clear();
        db = new DataBase(mContext);
        db.open();
        String title;
        String type;
        String number;
        String weekdays;
        int hour;
        int minute;
        int seconds;
        int day;
        int month;
        int year;
        int repCode;
        long repTime;
        int done;
        double lat;
        double longi;
        long repCount;
        long id;
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()) {
            do {
                id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                repCount = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                longi = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                done = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));

                if (done != 1) {
                    TimeCount tC = new TimeCount(mContext);

                    String[] dT = tC.getNextDateTime(year, month, day, hour, minute, seconds,
                            repTime, repCode, repCount, 0);
                    String time = "";
                    String date = "";
                    if (lat != 0.0 || longi != 0.0) {
                        date = String.format("%.5f", lat);
                        time = String.format("%.5f", longi);
                    } else {
                        boolean is24 = new SharedPrefs(mContext)
                                .loadBoolean(Prefs.IS_24_TIME_FORMAT);
                        if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);

                            date = ReminderUtils.getRepeatString(mContext, weekdays);
                            time = TimeUtil.getTime(calendar.getTime(), is24);
                        } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                            long timeL = TimeCount.getNextMonthDayTime(hour, minute, day, 0);
                            Calendar calendar1 = Calendar.getInstance();
                            if (timeL > 0) {
                                calendar1.setTimeInMillis(timeL);

                                date = TimeUtil.dateFormat.format(calendar1.getTime());
                                time = TimeUtil.getTime(calendar1.getTime(), is24);
                            }
                        } else {
                            date = dT[0];
                            time = dT[1];
                        }
                    }

                    data.add(new CalendarData(title, number, id, time, date));
                }
            } while (c.moveToNext());
        }

        if(c != null) c.close();

        SharedPrefs prefs = new SharedPrefs(mContext);
        if (prefs.loadBoolean(Prefs.WIDGET_BIRTHDAYS)) {
            int mDay;
            int mMonth;
            int n = 0;
            Calendar calendar = Calendar.getInstance();
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mMonth = calendar.get(Calendar.MONTH);
                Cursor cursor = db.getBirthdays(mDay, mMonth);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String birthday = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                        String name = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                        long i = 0;
                        data.add(new CalendarData(mContext.getString(R.string.birthday_text), name, i, birthday, ""));
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                }
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60 * 24));
                n++;
            } while (n <= 7);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                CurrentTaskWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_current_widget);
        int itemColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_ITEM_COLOR + widgetID, 0);
        int itemTextColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, 0);
        float itemTextSize = sp.getFloat(CurrentTaskWidgetConfig.CURRENT_WIDGET_TEXT_SIZE + widgetID, 0);
        rView.setInt(R.id.itemBg, "setBackgroundColor", itemColor);

        CalendarData item = data.get(i);

        String task = item.getName();
        Contacts contacts = new Contacts(mContext);
        if (task == null || task.matches("")) task = Contacts.getContactNameFromNumber(
                item.getNumber(), mContext);
        rView.setTextViewText(R.id.taskText, task);
        rView.setTextColor(R.id.taskText, itemTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rView.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
            rView.setTextViewTextSize(R.id.taskNumber, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
            rView.setTextViewTextSize(R.id.taskDate, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
            rView.setTextViewTextSize(R.id.taskTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
        } else {
            rView.setFloat(R.id.taskTime, "setTextSize", itemTextSize);
            rView.setFloat(R.id.taskDate, "setTextSize", itemTextSize);
            rView.setFloat(R.id.taskNumber, "setTextSize", itemTextSize);
            rView.setFloat(R.id.taskText, "setTextSize", itemTextSize);
        }

        String number = item.getNumber();
        if (number != null && !number.matches("")) {
            rView.setTextViewText(R.id.taskNumber, number);
            rView.setTextColor(R.id.taskNumber, itemTextColor);
        } else {
            rView.setViewVisibility(R.id.taskNumber, View.GONE);
        }
        rView.setTextViewText(R.id.taskDate, item.getDayDate());
        rView.setTextColor(R.id.taskDate, itemTextColor);
        rView.setTextViewText(R.id.taskTime, item.getTime());
        rView.setTextColor(R.id.taskTime, itemTextColor);

        long id = item.getId();
        if (id != 0) {
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Constants.EDIT_ID, id);
            fillInIntent.putExtra(Constants.EDIT_WIDGET, 2);
            rView.setOnClickFillInIntent(R.id.taskDate, fillInIntent);
            rView.setOnClickFillInIntent(R.id.taskTime, fillInIntent);
            rView.setOnClickFillInIntent(R.id.taskNumber, fillInIntent);
            rView.setOnClickFillInIntent(R.id.taskText, fillInIntent);
            rView.setOnClickFillInIntent(R.id.itemBg, fillInIntent);
        }
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}