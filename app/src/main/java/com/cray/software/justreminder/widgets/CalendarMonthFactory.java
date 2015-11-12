package com.cray.software.justreminder.widgets;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.WidgetDataProvider;
import com.cray.software.justreminder.datas.WidgetItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.ArrayList;
import java.util.Calendar;

import hirondelle.date4j.DateTime;

public class CalendarMonthFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<DateTime> datetimeList;
    private ArrayList<WidgetItem> pagerData = new ArrayList<>();
    private Context context;
    private int widgetID;
    private int SUNDAY = 1;
    private int startDayOfWeek = SUNDAY;
    private int mDay;
    private int mMonth;
    private int mYear;

    CalendarMonthFactory(Context ctx, Intent intent) {
        context = ctx;
        widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        datetimeList = new ArrayList<>();
        datetimeList.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        SharedPreferences sp =
                context.getSharedPreferences(CalendarWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        int prefsMonth = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetID, 0);

        mYear = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_YEAR + widgetID, 0);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = prefsMonth + 1;

        DateTime firstDateOfMonth = new DateTime(mYear, prefsMonth + 1, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth
                .getNumDaysInMonth() - 1);

        // Add dates of first week from previous month
        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();

        // If weekdayOfFirstDate smaller than startDayOfWeek
        // For e.g: weekdayFirstDate is Monday, startDayOfWeek is Tuesday
        // increase the weekday of FirstDate because it's in the future
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }

        while (weekdayOfFirstDate > 0) {
            SharedPrefs prefs = new SharedPrefs(context);
            int temp = startDayOfWeek;
            if (prefs.loadInt(Prefs.START_DAY) == 1){
                temp = startDayOfWeek + 1;
            }

            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                    - temp);
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }

            datetimeList.add(dateTime);
            weekdayOfFirstDate--;
        }

        // Add dates of current month
        for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
            datetimeList.add(firstDateOfMonth.plusDays(i));
        }

        // Add dates of last week from next month
        int endDayOfWeek = startDayOfWeek - 1;

        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }

        if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
            int i = 1;
            while (true) {
                DateTime nextDay = lastDateOfMonth.plusDays(i);
                datetimeList.add(nextDay);
                i++;
                if (nextDay.getWeekDay() == endDayOfWeek) {
                    break;
                }
            }
        }

        // Add more weeks to fill remaining rows
        int size = datetimeList.size();
        int numOfDays = 42 - size;
        DateTime lastDateTime = datetimeList.get(size - 1);
        for (int i = 1; i <= numOfDays; i++) {
            DateTime nextDateTime = lastDateTime.plusDays(i);
            datetimeList.add(nextDateTime);
        }

        showEvents();
    }

    private void showEvents() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentDay;
        int currentMonth;
        int currentYear;

        SharedPrefs sPrefs = new SharedPrefs(context);
        int hour = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR);

        DataBase db = new DataBase(context);
        if (!db.isOpen()) db.open();
        WidgetDataProvider provider = new WidgetDataProvider();
        Cursor c = db.getBirthdays();
        provider.setBirthdays(c);
        provider.setTime(hour, minute);
        if (isRemindersEnabled) {
            Cursor s = db.getActiveReminders();
            provider.setReminders(s);
            provider.setFeature(isFeature);
        }
        provider.fillArray();

        pagerData.clear();

        int position = 0;
        do {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            boolean hasReminders = provider.hasReminder(currentDay, currentMonth, currentYear);
            boolean hasBirthdays = provider.hasBirthday(currentDay, currentMonth);
            pagerData.add(new WidgetItem(currentDay, currentMonth, currentYear,
                    hasReminders, hasBirthdays));
            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        } while (position < Configs.MAX_DAYS_COUNT);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return datetimeList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = context.getSharedPreferences(
                CalendarWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        int itemTextColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, 0);
        int rowColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_ROW_COLOR + widgetID, 0);
        int reminderM = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_REMINDER_COLOR + widgetID, 0);
        int birthdayM = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_BIRTHDAY_COLOR + widgetID, 0);
        int currentM = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_CURRENT_COLOR + widgetID, 0);
        int prefsMonth = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetID, 0);
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.month_view_grid);

        ColorSetter cs = new ColorSetter(context);

        int selDay = datetimeList.get(i).getDay();
        int selMonth = datetimeList.get(i).getMonth();
        int selYear = datetimeList.get(i).getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int realMonth = calendar.get(Calendar.MONTH);
        int realYear = calendar.get(Calendar.YEAR);

        rView.setTextViewText(R.id.textView, String.valueOf(selDay));
        if (selMonth == prefsMonth + 1) rView.setTextColor(R.id.textView, itemTextColor);
        else rView.setTextColor(R.id.textView, context.getResources().getColor(R.color.material_grey));
        rView.setInt(R.id.background, "setBackgroundResource", rowColor);

        rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT);
        rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT);
        rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT);

        if (pagerData.size() > 0){
            for (WidgetItem item : pagerData){
                int day = item.getDay();
                int month = item.getMonth() + 1;
                int year = item.getYear();
                if (day == selDay && month == selMonth){
                    if (item.isHasReminders() && year == selYear){
                        if (reminderM != 0){
                            rView.setInt(R.id.reminderMark, "setBackgroundResource", reminderM);
                        } else {
                            rView.setInt(R.id.reminderMark, "setBackgroundColor",
                                    context.getResources().getColor(cs.colorReminderCalendar()));
                        }
                    } else rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT);
                    if (item.isHasBirthdays()){
                        if (birthdayM != 0){
                            rView.setInt(R.id.birthdayMark, "setBackgroundResource", birthdayM);
                        } else {
                            rView.setInt(R.id.birthdayMark, "setBackgroundColor",
                                    context.getResources().getColor(cs.colorBirthdayCalendar()));
                        }
                    } else rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT);
                    break;
                }
            }
        }

        if (mDay == selDay && mMonth == selMonth && mYear == realYear && mMonth == realMonth + 1
                && mYear == selYear){
            if (currentM != 0){
                rView.setInt(R.id.currentMark, "setBackgroundResource", currentM);
            } else {
                rView.setInt(R.id.currentMark, "setBackgroundColor",
                        context.getResources().getColor(cs.colorCurrentCalendar()));
            }
        } else {
            rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT);
        }

        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MONTH, selMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, selDay);
        calendar.set(Calendar.YEAR, selYear);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        long dateMills = calendar.getTimeInMillis();

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("date", dateMills);
        rView.setOnClickFillInIntent(R.id.textView, fillInIntent);
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