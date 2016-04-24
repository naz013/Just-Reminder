package com.cray.software.justreminder.app_widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.app_widgets.configs.AddReminderWidgetConfig;

public class AddReminderWidget extends AppWidgetProvider{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences sp = context.getSharedPreferences(
                AddReminderWidgetConfig.ADD_REMINDER_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.add_reminder_widget_layout);

        int widgetColor = sp.getInt(AddReminderWidgetConfig.ADD_REMINDER_WIDGET_COLOR + widgetID, 0);

        rv.setInt(R.id.widgetBg, "setBackgroundResource", widgetColor);

        Intent configIntent = new Intent(context, ReminderManager.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        rv.setOnClickPendingIntent(R.id.imageView, configPendingIntent);
        appWidgetManager.updateAppWidget(widgetID, rv);
    }
}
