package com.cray.software.justreminder.app_widgets.calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.hexrain.flextcal.FlextHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hirondelle.date4j.DateTime;

public class CalendarWeekdayFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<String> mWeekdaysList;
    private Context mContext;
    private int mWidgetId;
    private int SUNDAY = 1;
    private int startDayOfWeek = SUNDAY;

    CalendarWeekdayFactory(Context ctx, Intent intent) {
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
        mWeekdaysList = new ArrayList<>();
        mWeekdaysList.clear();
        SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

        // 17 Feb 2013 is Sunday
        DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
        DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (prefs.loadInt(Prefs.START_DAY) == 1){
            nextDay = nextDay.plusDays(1);
        }

        for (int i = 0; i < 7; i++) {
            Date date = FlextHelper.convertDateTimeToDate(nextDay);
            mWeekdaysList.add(fmt.format(date).toUpperCase());
            nextDay = nextDay.plusDays(1);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mWeekdaysList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + mWidgetId, 0);
        CalendarTheme item = CalendarTheme.getThemes(mContext).get(theme);
        int itemTextColor = item.getItemTextColor();
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.weekday_grid);

        rView.setTextViewText(R.id.textView1, mWeekdaysList.get(i));
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