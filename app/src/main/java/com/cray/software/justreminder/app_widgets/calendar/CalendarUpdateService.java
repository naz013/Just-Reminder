package com.cray.software.justreminder.app_widgets.calendar;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;

public class CalendarUpdateService extends IntentService {

    public CalendarUpdateService() {
        super("CalendarUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int action = intent.getIntExtra("actionPlus", 0);
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        SharedPreferences sp =
                getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE);
        int month  = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetId, 0);
        int year  = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetId, 0);
        if (action != 0){
            SharedPreferences.Editor editor = sp.edit();
            if (month < 11 && month >= 0) month += 1;
            else month = 0;
            editor.putInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + widgetId, month);
            if (month == 0) year += 1;
            editor.putInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + widgetId, year);
            editor.commit();
            new UpdatesHelper(CalendarUpdateService.this).updateCalendarWidget();
            stopSelf();
        } else stopSelf();
    }
}
