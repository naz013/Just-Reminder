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
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.birthdays.BirthdayHelper;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.calendar.CalendarItem;
import com.cray.software.justreminder.datas.models.ShoppingListItem;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "EventsFactory";
    private ArrayList<CalendarItem> data;
    private Map<Long, List<ShoppingListItem>> map;
    private Context mContext;
    private TimeCount mCount;
    private int widgetID;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    EventsFactory(Context ctx, Intent intent) {
        mContext = ctx;
        mCount = TimeCount.getInstance(ctx);
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
        boolean is24 = SharedPrefs.getInstance(mContext).getBoolean(Prefs.IS_24_TIME_FORMAT);
        List<ReminderItem> reminderItems = ReminderHelper.getInstance(mContext).getRemindersEnabled();
        for (ReminderItem item : reminderItems) {
            String mType = item.getType();
            String summary = item.getSummary();
            long eventTime = item.getDateTime();
            long id = item.getId();

            JsonModel jsonModel = item.getModel();
            JPlace jPlace = jsonModel.getPlace();

            List<Integer> weekdays = jsonModel.getRecurrence().getWeekdays();
            String exclusion = jsonModel.getExclusion().toString();

            String time = "";
            String date = "";
            int viewType = 1;
            if (mType.contains(Constants.TYPE_LOCATION)) {
                date = String.format(Locale.getDefault(), "%.5f", jPlace.getLatitude());
                time = String.format(Locale.getDefault(), "%.5f", jPlace.getLongitude());
            } else {
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
                    map.put(id, new ShoppingListDataProvider(jsonModel.getShoppings(), false).getData());
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
            data.add(new CalendarItem(summary, jsonModel.getAction().getTarget(),
                    id, time, date, eventTime, viewType));
        }

        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        if (prefs.getBoolean(Prefs.WIDGET_BIRTHDAYS)) {
            int mDay;
            int mMonth;
            int n = 0;
            Calendar calendar = Calendar.getInstance();
            int hour = prefs.getInt(Prefs.BIRTHDAY_REMINDER_HOUR);
            int minute = prefs.getInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mMonth = calendar.get(Calendar.MONTH);
                List<BirthdayItem> list = BirthdayHelper.getInstance(mContext).getBirthdays(mDay, mMonth);
                for (BirthdayItem item : list) {
                    String birthday = item.getDate();
                    String name = item.getName();
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
                    data.add(new CalendarItem(mContext.getString(R.string.birthday), name, i, birthday, "", eventTime, 1));
                }
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60 * 24));
                n++;
            } while (n <= 7);
        }
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
            CalendarItem item = data.get(i);
            if (item.getViewType() == 1) {
                rView = new RemoteViews(mContext.getPackageName(),
                        R.layout.list_item_current_widget);
                rView.setInt(R.id.itemBg, "setBackgroundResource", itemBackground);

                String task = item.getName();
                if (task == null || task.matches("")) task = com.cray.software.justreminder.contacts.Contacts.getNameFromNumber(
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
                List<ShoppingListItem> lists = map.get(item.getId());
                rView.removeAllViews(R.id.todoList);
                for (ShoppingListItem list : lists) {
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