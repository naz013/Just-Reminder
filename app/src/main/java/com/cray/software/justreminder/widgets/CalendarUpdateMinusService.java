package com.cray.software.justreminder.widgets;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.cray.software.justreminder.interfaces.Constants;

public class CalendarUpdateMinusService extends IntentService {

    public CalendarUpdateMinusService() {
        super("CalendarUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int action = intent.getIntExtra("actionMinus", 0);
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        SharedPreferences sp =
                getSharedPreferences(CalendarWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        int month  = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetId, 0);
        if (action != 0){
            SharedPreferences.Editor editor = sp.edit();
            if (month == 0) month = 11;
            else month = month - 1;
            editor.putInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetId, month);
            editor.commit();
            new UpdatesHelper(CalendarUpdateMinusService.this).updateCalendarWidget();
            stopSelf();
        } else stopSelf();
    }
}