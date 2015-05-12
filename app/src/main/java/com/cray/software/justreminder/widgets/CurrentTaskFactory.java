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
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CurrentTaskFactory implements RemoteViewsService.RemoteViewsFactory {

    ArrayList<String> text, numberList, date, time;
    ArrayList<Long> ids;
    Context context;
    DataBase db;
    int widgetID;
    TimeCount tC;

    CurrentTaskFactory(Context ctx, Intent intent) {
        context = ctx;
        widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        text = new ArrayList<>();
        numberList = new ArrayList<>();
        date = new ArrayList<>();
        time = new ArrayList<>();
        ids = new ArrayList<>();
        db = new DataBase(context);
    }

    @Override
    public void onDataSetChanged() {
        text.clear();
        numberList.clear();
        time.clear();
        date.clear();
        ids.clear();
        db = new DataBase(context);
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
        int repTime;
        int done;
        double lat;
        double longi;
        int repCount;
        long id;
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()) {
            do {
                id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
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
                    ids.add(id);

                    text.add(title);
                    if (number != null) {
                        numberList.add(number);
                    } else numberList.add(null);
                    tC = new TimeCount(context);

                    String[] dT = tC.getNextDateTime(year, month, day, hour, minute, seconds,
                            repTime, repCode, repCount, 0);
                    if (lat != 0.0 || longi != 0.0) {
                        date.add(String.format("%.5f", lat));
                        time.add(String.format("%.5f", longi));
                    } else {
                        if (type.matches(Constants.TYPE_WEEKDAY) ||
                                type.matches(Constants.TYPE_WEEKDAY_CALL) ||
                                type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);

                            String formattedTime;
                            if (new SharedPrefs(context).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                formattedTime = sdf.format(calendar.getTime());
                            } else {
                                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                                formattedTime = sdf.format(calendar.getTime());
                            }

                            date.add(getRepeatString(weekdays));
                            time.add(formattedTime);
                        } else {
                            date.add(dT[0]);
                            time.add(dT[1]);
                        }
                    }
                }
            } while (c.moveToNext());
        }

        if(c != null) c.close();

        SharedPrefs prefs = new SharedPrefs(context);
        if (prefs.loadBoolean(Constants.APP_UI_PREFERENCES_WIDGET_BIRTHDAYS)) {
            int mDay;
            int mMonth;
            int n = 0;
            Calendar calendar = Calendar.getInstance();
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mMonth = calendar.get(Calendar.MONTH);
                Cursor cursor = db.getEvents(mDay, mMonth);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String birthday = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                        String name = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                        time.add(birthday);
                        date.add("");
                        long i = 0;
                        ids.add(i);
                        text.add(context.getString(R.string.birthday_text));
                        numberList.add(name);
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

    private String getRepeatString(String repCode) {
        String res;
        StringBuilder sb = new StringBuilder();
        if (Character.toString(repCode.charAt(0)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_monday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(1)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_tuesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(2)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_wednesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(3)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_thursday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(4)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_friday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(5)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_saturday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(6)).matches(Constants.DAY_CHECKED)) {
            sb.append(context.getString(R.string.weekday_sunday));
        }
        if (repCode.matches(Constants.ALL_CHECKED)) {
            res = context.getString(R.string.interval_day);
        } else res = sb.toString();
        return res;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return text.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = context.getSharedPreferences(
                CurrentTaskWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.list_item_current_widget);
        int itemColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_ITEM_COLOR + widgetID, 0);
        int itemTextColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, 0);
        float itemTextSize = sp.getFloat(CurrentTaskWidgetConfig.CURRENT_WIDGET_TEXT_SIZE + widgetID, 0);
        rView.setInt(R.id.itemBg, "setBackgroundColor", itemColor);

        String task = text.get(i);
        Contacts contacts = new Contacts(context);
        if (task == null || task.matches("")) task = contacts
                .getContactNameFromNumber(numberList.get(i), context);
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

        if (numberList.get(i) != null && !numberList.get(i).matches("")) {
            rView.setTextViewText(R.id.taskNumber, numberList.get(i));
            rView.setTextColor(R.id.taskNumber, itemTextColor);
        } else {
            rView.setViewVisibility(R.id.taskNumber, View.GONE);
        }
        rView.setTextViewText(R.id.taskDate, date.get(i));
        rView.setTextColor(R.id.taskDate, itemTextColor);
        rView.setTextViewText(R.id.taskTime, time.get(i));
        rView.setTextColor(R.id.taskTime, itemTextColor);

        long id = ids.get(i);
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