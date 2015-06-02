package com.cray.software.justreminder.widgets;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.WidgetItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import hirondelle.date4j.DateTime;

public class CalendarMonthFactory implements RemoteViewsService.RemoteViewsFactory {

    ArrayList<DateTime> datetimeList;
    ArrayList<WidgetItem> pagerData = new ArrayList<>();
    Context context;
    int widgetID;
    int SUNDAY = 1;
    int startDayOfWeek = SUNDAY;
    int mDay, mMonth, mYear, prefsMonth;

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
        prefsMonth  = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetID, 0);

        int year = calendar.get(Calendar.YEAR);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = prefsMonth + 1;
        mYear = year;

        DateTime firstDateOfMonth = new DateTime(year, prefsMonth + 1, 1, 0, 0, 0, 0);
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
            if (prefs.loadInt(Constants.APP_UI_PREFERENCES_START_DAY) == 1){
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
        int hour = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR);
        int minute = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE);
        boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
        boolean isRemindersEnabled = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR);
        TimeCount mCount = new TimeCount(context);

        DataBase db = new DataBase(context);
        if (!db.isOpen()) db.open();

        pagerData.clear();

        int position = 0;
        boolean hasBirthdays;
        boolean hasReminders;
        do {
            hasBirthdays = false;
            hasReminders = false;
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            Cursor c = db.getEvents(currentDay, currentMonth);
            if (c != null && c.moveToFirst()){
                do {
                    String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                    Date date1 = null;
                    try {
                        date1 = format.parse(birthday);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (date1 != null) {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(date1);
                        int bDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int bMonth = calendar1.get(Calendar.MONTH);
                        calendar1.setTimeInMillis(System.currentTimeMillis());
                        calendar1.set(Calendar.MONTH, bMonth);
                        calendar1.set(Calendar.DAY_OF_MONTH, bDay);
                        calendar1.set(Calendar.HOUR_OF_DAY, hour);
                        calendar1.set(Calendar.MINUTE, minute);
                        if (bDay == currentDay && currentMonth == bMonth) {
                            hasBirthdays = true;
                        }
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();

            if (isRemindersEnabled) {
                Cursor s = db.queryGroup();
                if (s != null && s.moveToFirst()) {
                    do {
                        int myHour = s.getInt(s.getColumnIndex(Constants.COLUMN_HOUR));
                        int myMinute = s.getInt(s.getColumnIndex(Constants.COLUMN_MINUTE));
                        int myDay = s.getInt(s.getColumnIndex(Constants.COLUMN_DAY));
                        int myMonth = s.getInt(s.getColumnIndex(Constants.COLUMN_MONTH));
                        int myYear = s.getInt(s.getColumnIndex(Constants.COLUMN_YEAR));
                        int repCode = s.getInt(s.getColumnIndex(Constants.COLUMN_REPEAT));
                        int remCount = s.getInt(s.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                        int afterTime = s.getInt(s.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                        String mType = s.getString(s.getColumnIndex(Constants.COLUMN_TYPE));
                        String weekdays = s.getString(s.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                        int isDone = s.getInt(s.getColumnIndex(Constants.COLUMN_IS_DONE));
                        if ((mType.startsWith(Constants.TYPE_SKYPE) ||
                                mType.matches(Constants.TYPE_CALL) ||
                                mType.startsWith(Constants.TYPE_APPLICATION) ||
                                mType.matches(Constants.TYPE_MESSAGE) ||
                                mType.matches(Constants.TYPE_REMINDER) ||
                                mType.matches(Constants.TYPE_TIME)) && isDone == 0) {
                            long time = mCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                                    afterTime, repCode, remCount, 0);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                hasReminders = true;
                            }
                            if (!mType.matches(Constants.TYPE_TIME) && isFeature && repCode > 0) {
                                int days = 0;
                                do {
                                    calendar1.setTimeInMillis(time + (repCode * Intervals.MILLS_INTERVAL_DAY));
                                    time = calendar1.getTimeInMillis();
                                    mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                    mMonth = calendar1.get(Calendar.MONTH);
                                    mYear = calendar1.get(Calendar.YEAR);
                                    days = days + repCode;
                                    if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                        hasReminders = true;
                                    }
                                } while (days < 31);
                            }
                        } else if (mType.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0) {
                            long time = mCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(time);
                            int mDay = calendar1.get(Calendar.DAY_OF_MONTH);
                            int mMonth = calendar1.get(Calendar.MONTH);
                            int mYear = calendar1.get(Calendar.YEAR);
                            if (time > 0 && mDay == currentDay && mMonth == currentMonth && mYear == currentYear) {
                                hasReminders = true;
                            }
                            int days = 0;
                            if (isFeature) {
                                ArrayList<Integer> list = getRepeatArray(weekdays);
                                do {
                                    calendar1.setTimeInMillis(time + Intervals.MILLS_INTERVAL_DAY);
                                    time = calendar1.getTimeInMillis();
                                    int weekDay = calendar1.get(Calendar.DAY_OF_WEEK);
                                    days = days + 1;
                                    if (list.get(weekDay - 1) == 1) {
                                        int sDay = calendar1.get(Calendar.DAY_OF_MONTH);
                                        int sMonth = calendar1.get(Calendar.MONTH);
                                        int sYear = calendar1.get(Calendar.YEAR);
                                        if (time > 0 && sDay == currentDay && sMonth == currentMonth && sYear == currentYear) {
                                            hasReminders = true;
                                        }
                                    }
                                } while (days < 31);
                            }
                        }
                    } while (s.moveToNext());
                }
                if (s != null) s.close();
            }
            db.close();

            pagerData.add(new WidgetItem(currentDay, currentMonth, currentYear,
                    hasReminders, hasBirthdays));
            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        } while (position < 40);
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        return res;
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
        int itemTextColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_TITLE_COLOR + widgetID, 0);
        int widgetBgColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_COLOR + widgetID, 0);
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.month_view_grid);

        ColorSetter cs = new ColorSetter(context);

        int selDay = datetimeList.get(i).getDay();
        int selMonth = datetimeList.get(i).getMonth();
        int selYear = datetimeList.get(i).getYear();

        rView.setTextViewText(R.id.textView, String.valueOf(selDay));
        rView.setTextColor(R.id.textView, itemTextColor);
        rView.setInt(R.id.background, "setBackgroundColor", widgetBgColor);

        Calendar calendar = Calendar.getInstance();

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
                        rView.setInt(R.id.reminderMark, "setBackgroundColor",
                                context.getResources().getColor(cs.colorReminderCalendar()));
                    }
                    if (item.isHasBirthdays()){
                        rView.setInt(R.id.birthdayMark, "setBackgroundColor",
                                context.getResources().getColor(cs.colorBirthdayCalendar()));
                    }
                    break;
                }
            }
        }

        if (mDay == selDay && mMonth == selMonth && mYear == selYear){
            rView.setInt(R.id.currentMark, "setBackgroundColor",
                    context.getResources().getColor(cs.colorCurrentCalendar()));
            rView.setViewVisibility(R.id.currentMark, View.VISIBLE);
        } else {
            rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT);
            rView.setViewVisibility(R.id.currentMark, View.INVISIBLE);
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