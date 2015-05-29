package com.cray.software.justreminder.widgets;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.cray.software.justreminder.interfaces.Constants;

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
                getSharedPreferences(CalendarWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        int month  = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetId, 0);
        if (action != 0){
            Log.d(Constants.LOG_TAG, "Plus button pressed");
            SharedPreferences.Editor editor = sp.edit();
            if (month < 11 && month >= 0) month = month + 1;
            else month = 0;
            editor.putInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetId, month);
            editor.commit();
            new UpdatesHelper(CalendarUpdateService.this).updateCalendarWidget();
            stopSelf();
        } else stopSelf();
    }
}
