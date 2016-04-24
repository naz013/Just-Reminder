package com.cray.software.justreminder.app_widgets.calendar;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.WidgetDataProvider;
import com.cray.software.justreminder.datas.models.WidgetItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.ArrayList;
import java.util.Calendar;

import hirondelle.date4j.DateTime;

public class CalendarMonthFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<DateTime> mDateTimeList;
    private ArrayList<WidgetItem> mPagerData = new ArrayList<>();
    private Context mContext;
    private int mWidgetId;
    private int mDay;
    private int mMonth;
    private int mYear;

    CalendarMonthFactory(Context ctx, Intent intent) {
        mContext = ctx;
        mWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        mDateTimeList = new ArrayList<>();
        mDateTimeList.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        SharedPreferences sp =
                mContext.getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int prefsMonth = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + mWidgetId, 0);

        mYear = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + mWidgetId, 0);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = prefsMonth + 1;

        DateTime firstDateOfMonth = new DateTime(mYear, prefsMonth + 1, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth
                .getNumDaysInMonth() - 1);

        // Add dates of first week from previous month
        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();

        SharedPrefs prefs = new SharedPrefs(mContext);
        int startDayOfWeek = prefs.loadInt(Prefs.START_DAY) + 1;

        // If weekdayOfFirstDate smaller than startDayOfWeek
        // For e.g: weekdayFirstDate is Monday, startDayOfWeek is Tuesday
        // increase the weekday of FirstDate because it's in the future
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }

        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                    - startDayOfWeek);
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }

            mDateTimeList.add(dateTime);
            weekdayOfFirstDate--;
        }

        // Add dates of current month
        for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
            mDateTimeList.add(firstDateOfMonth.plusDays(i));
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
                mDateTimeList.add(nextDay);
                i++;
                if (nextDay.getWeekDay() == endDayOfWeek) {
                    break;
                }
            }
        }

        // Add more weeks to fill remaining rows
        int size = mDateTimeList.size();
        int numOfDays = 42 - size;
        DateTime lastDateTime = mDateTimeList.get(size - 1);
        for (int i = 1; i <= numOfDays; i++) {
            DateTime nextDateTime = lastDateTime.plusDays(i);
            mDateTimeList.add(nextDateTime);
        }

        showEvents();
    }

    private void showEvents() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentDay;
        int currentMonth;
        int currentYear;

        SharedPrefs sPrefs = new SharedPrefs(mContext);
        int hour = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR);

        WidgetDataProvider provider = new WidgetDataProvider(mContext);
        provider.setTime(hour, minute);
        if (isRemindersEnabled) {
            provider.setFeature(isFeature);
        }
        provider.fillArray();
        mPagerData.clear();

        int position = 0;
        do {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            boolean hasReminders = provider.hasReminder(currentDay, currentMonth, currentYear);
            boolean hasBirthdays = provider.hasBirthday(currentDay, currentMonth);
            mPagerData.add(new WidgetItem(currentDay, currentMonth, currentYear,
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
        return mDateTimeList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + mWidgetId, 0);
        CalendarTheme calendarTheme = CalendarTheme.getThemes(mContext).get(theme);
        int itemTextColor = calendarTheme.getItemTextColor();
        int rowColor = calendarTheme.getRowColor();
        int reminderM = calendarTheme.getReminderMark();
        int birthdayM = calendarTheme.getBirthdayMark();
        int currentM = calendarTheme.getCurrentMark();
        int prefsMonth = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + mWidgetId, 0);
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.month_view_grid);

        ColorSetter cs = new ColorSetter(mContext);

        int selDay = mDateTimeList.get(i).getDay();
        int selMonth = mDateTimeList.get(i).getMonth();
        int selYear = mDateTimeList.get(i).getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int realMonth = calendar.get(Calendar.MONTH);
        int realYear = calendar.get(Calendar.YEAR);

        rView.setTextViewText(R.id.textView, String.valueOf(selDay));
        if (selMonth == prefsMonth + 1) rView.setTextColor(R.id.textView, itemTextColor);
        else rView.setTextColor(R.id.textView, mContext.getResources().getColor(R.color.material_grey));
        rView.setInt(R.id.background, "setBackgroundResource", rowColor);

        rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT);
        rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT);
        rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT);

        if (mPagerData.size() > 0){
            for (WidgetItem item : mPagerData){
                int day = item.getDay();
                int month = item.getMonth() + 1;
                int year = item.getYear();
                if (day == selDay && month == selMonth){
                    if (item.isHasReminders() && year == selYear){
                        if (reminderM != 0){
                            rView.setInt(R.id.reminderMark, "setBackgroundResource", reminderM);
                        } else {
                            rView.setInt(R.id.reminderMark, "setBackgroundColor",
                                    mContext.getResources().getColor(cs.colorReminderCalendar()));
                        }
                    } else rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT);
                    if (item.isHasBirthdays()){
                        if (birthdayM != 0){
                            rView.setInt(R.id.birthdayMark, "setBackgroundResource", birthdayM);
                        } else {
                            rView.setInt(R.id.birthdayMark, "setBackgroundColor",
                                    mContext.getResources().getColor(cs.colorBirthdayCalendar()));
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
                        mContext.getResources().getColor(cs.colorCurrentCalendar()));
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