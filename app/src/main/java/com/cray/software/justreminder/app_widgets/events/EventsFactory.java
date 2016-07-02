/**
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.app_widgets.events;

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
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.CalendarModel;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "EventsFactory";
    private ArrayList<CalendarModel> data;
    private Map<Long, ArrayList<ShoppingList>> map;
    private Context mContext;
    private TimeCount mCount;
    private int widgetID;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    EventsFactory(Context ctx, Intent intent) {
        mContext = ctx;
        mCount = new TimeCount(ctx);
        widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        data = new ArrayList<>();
        map = new HashMap<>();
    }

    @Override
    public void onDataSetChanged() {
        data.clear();
        map.clear();
        NextBase db = new NextBase(mContext);
        db.open();
        Cursor c = db.getActiveReminders();
        if (c != null && c.moveToFirst()) {
            do {
                String json = c.getString(c.getColumnIndex(NextBase.JSON));
                String mType = c.getString(c.getColumnIndex(NextBase.TYPE));
                String summary = c.getString(c.getColumnIndex(NextBase.SUMMARY));
                long eventTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
                long id = c.getLong(c.getColumnIndex(NextBase._ID));

                JModel jModel = new JParser(json).parse();
                JPlace jPlace = jModel.getPlace();

                ArrayList<Integer> weekdays = jModel.getRecurrence().getWeekdays();
                String exclusion = jModel.getExclusion().toString();

                String time = "";
                String date = "";
                int viewType = 1;
                if (mType.contains(Constants.TYPE_LOCATION)) {
                    date = String.format("%.5f", jPlace.getLatitude());
                    time = String.format("%.5f", jPlace.getLongitude());
                } else {
                    boolean is24 = new SharedPrefs(mContext)
                            .loadBoolean(Prefs.IS_24_TIME_FORMAT);
                    if (mType.startsWith(Constants.TYPE_WEEKDAY)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(eventTime);

                        date = ReminderUtils.getRepeatString(mContext, weekdays);
                        time = TimeUtil.getTime(calendar.getTime(), is24);
                    } else if (mType.startsWith(Constants.TYPE_MONTHDAY)) {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTimeInMillis(eventTime);
                        date = TimeUtil.dateFormat.format(calendar1.getTime());
                        time = TimeUtil.getTime(calendar1.getTime(), is24);
                    } else if (mType.matches(Constants.TYPE_SHOPPING_LIST)) {
                        viewType = 2;
                        map.put(id, new ShoppingListDataProvider(jModel.getShoppings(), false).getData());
                    } else {
                        String[] dT = mCount.getNextDateTime(eventTime);
                        date = dT[0];
                        time = dT[1];
                        if (mType.matches(Constants.TYPE_TIME) && exclusion != null){
                            if (new Recurrence(exclusion).isRange()){
                                date = mContext.getString(R.string.paused);
                            }
                            time = "";
                        }
                    }
                }

                data.add(new CalendarModel(summary, jModel.getAction().getTarget(),
                        id, time, date, eventTime, viewType));
            } while (c.moveToNext());
        }
        if(c != null) c.close();
        db.close();

        DataBase DB = new DataBase(mContext);
        DB.open();
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (prefs.loadBoolean(Prefs.WIDGET_BIRTHDAYS)) {
            int mDay;
            int mMonth;
            int n = 0;
            Calendar calendar = Calendar.getInstance();
            int hour = prefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
            int minute = prefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mMonth = calendar.get(Calendar.MONTH);
                Cursor cursor = DB.getBirthdays(mDay, mMonth);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String birthday = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                        String name = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                        long i = 0;
                        long eventTime = 0;
                        try {
                            Date date = format.parse(birthday);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(System.currentTimeMillis());
                            int year = calendar1.get(Calendar.YEAR);
                            if (date != null) {
                                calendar1.setTime(date);
                                calendar1.set(Calendar.YEAR, year);
                                calendar1.set(Calendar.HOUR_OF_DAY, hour);
                                calendar1.set(Calendar.MINUTE, minute);
                                eventTime = calendar1.getTimeInMillis();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        data.add(new CalendarModel(mContext.getString(R.string.birthday), name, i, birthday, "", eventTime, 1));
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                }
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60 * 24));
                n++;
            } while (n <= 7);
        }
        DB.close();
    }

    @Override
    public void onDestroy() {
        map.clear();
        data.clear();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                EventsWidgetConfig.EVENTS_WIDGET_PREF, Context.MODE_PRIVATE);
        int theme = sp.getInt(EventsWidgetConfig.EVENTS_WIDGET_THEME + widgetID, 0);
        EventsTheme eventsTheme = EventsTheme.getThemes(mContext).get(theme);
        int itemBackground = eventsTheme.getItemBackground();
        int itemTextColor = eventsTheme.getItemTextColor();
        float itemTextSize = sp.getFloat(EventsWidgetConfig.EVENTS_WIDGET_TEXT_SIZE + widgetID, 0);
        int checkboxColor = eventsTheme.getCheckboxColor();

        RemoteViews rView = null;
        if (i < getCount()) {
            CalendarModel item = data.get(i);
            if (item.getViewType() == 1) {
                rView = new RemoteViews(mContext.getPackageName(),
                        R.layout.list_item_current_widget);
                rView.setInt(R.id.itemBg, "setBackgroundResource", itemBackground);

                String task = item.getName();
                if (task == null || task.matches("")) task = Contacts.getNameFromNumber(
                        item.getNumber(), mContext);
                rView.setTextViewText(R.id.taskText, task);
                rView.setTextColor(R.id.taskText, itemTextColor);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rView.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                    rView.setTextViewTextSize(R.id.taskNumber, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                    rView.setTextViewTextSize(R.id.taskDate, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                    rView.setTextViewTextSize(R.id.taskTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                    rView.setTextViewTextSize(R.id.leftTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                } else {
                    rView.setFloat(R.id.taskTime, "setTextSize", itemTextSize);
                    rView.setFloat(R.id.taskDate, "setTextSize", itemTextSize);
                    rView.setFloat(R.id.taskNumber, "setTextSize", itemTextSize);
                    rView.setFloat(R.id.taskText, "setTextSize", itemTextSize);
                    rView.setFloat(R.id.leftTime, "setTextSize", itemTextSize);
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

                rView.setTextViewText(R.id.leftTime, mCount.getRemaining(item.getDate()));
                rView.setTextColor(R.id.leftTime, itemTextColor);

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
            }
            if (item.getViewType() == 2) {
                rView = new RemoteViews(mContext.getPackageName(),
                        R.layout.list_item_current_widget_with_list);
                rView.setInt(R.id.itemBg, "setBackgroundResource", itemBackground);
                String task = item.getName();
                rView.setTextViewText(R.id.taskText, task);
                rView.setTextColor(R.id.taskText, itemTextColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rView.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                } else {
                    rView.setFloat(R.id.taskText, "setTextSize", itemTextSize);
                }

                int count = 0;
                ArrayList<ShoppingList> lists = map.get(item.getId());
                rView.removeAllViews(R.id.todoList);
                for (ShoppingList list : lists) {
                    RemoteViews view = new RemoteViews(mContext.getPackageName(),
                            R.layout.list_item_task_item_widget);

                    boolean isBlack = checkboxColor == 0;
                    if (list.isChecked() == 1) {
                        if (isBlack)
                            view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_black_24dp);
                        else
                            view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_white_24dp);
                    } else {
                        if (isBlack)
                            view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_outline_blank_black_24dp);
                        else
                            view.setInt(R.id.checkView, "setBackgroundResource", R.drawable.ic_check_box_outline_blank_white_24dp);
                    }

                    view.setTextColor(R.id.shopText, itemTextColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        view.setTextViewTextSize(R.id.shopText, TypedValue.COMPLEX_UNIT_SP, itemTextSize);
                    } else {
                        view.setFloat(R.id.shopText, "setTextSize", itemTextSize);
                    }

                    count++;
                    if (count == 9) {
                        view.setViewVisibility(R.id.checkView, View.INVISIBLE);
                        view.setTextViewText(R.id.shopText, "...");
                        rView.addView(R.id.todoList, view);
                        break;
                    } else {
                        view.setViewVisibility(R.id.checkView, View.VISIBLE);
                        view.setTextViewText(R.id.shopText, list.getTitle());
                        rView.addView(R.id.todoList, view);
                    }
                }

                long id = item.getId();
                if (id != 0) {
                    Intent fillInIntent = new Intent();
                    fillInIntent.putExtra(Constants.EDIT_ID, id);
                    fillInIntent.putExtra(Constants.EDIT_WIDGET, 2);
                    rView.setOnClickFillInIntent(R.id.taskText, fillInIntent);
                    rView.setOnClickFillInIntent(R.id.itemBg, fillInIntent);
                    rView.setOnClickFillInIntent(R.id.todoList, fillInIntent);
                }
            }
        }
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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