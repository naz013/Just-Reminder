package com.cray.software.justreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.roomorama.caldroid.CalendarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hirondelle.date4j.DateTime;

public class CalendarWeekdayFactory implements RemoteViewsService.RemoteViewsFactory {

    ArrayList<String> weekdays;
    Context context;
    int widgetID;
    int SUNDAY = 1;
    int startDayOfWeek = SUNDAY;

    CalendarWeekdayFactory(Context ctx, Intent intent) {
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
        weekdays = new ArrayList<>();
        weekdays.clear();
        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

        // 17 Feb 2013 is Sunday
        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
        SharedPrefs prefs = new SharedPrefs(context);
        if (prefs.loadInt(Constants.APP_UI_PREFERENCES_START_DAY) == 1){
            nextDay = nextDay.plusDays(1);
        }

        for (int i = 0; i < 7; i++) {
            Date date = CalendarHelper.convertDateTimeToDate(nextDay);
            weekdays.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return weekdays.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = context.getSharedPreferences(
                CalendarWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        int itemTextColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_TITLE_COLOR + widgetID, 0);
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.weekday_grid);

        rView.setTextViewText(R.id.textView1, weekdays.get(i));
        rView.setTextColor(R.id.textView1, itemTextColor);

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