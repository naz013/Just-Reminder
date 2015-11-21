package com.cray.software.justreminder.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.AddBirthday;
import com.cray.software.justreminder.widgets.configs.AddBirthdayWidgetConfig;

public class AddBirthdayWidget extends AppWidgetProvider{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences sp = context.getSharedPreferences(
                AddBirthdayWidgetConfig.ADD_BIRTHDAY_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.add_birthday_widget_layout);

        int widgetColor = sp.getInt(AddBirthdayWidgetConfig.ADD_BIRTHDAY_WIDGET_COLOR + widgetID, 0);

        rv.setInt(R.id.widgetBg, "setBackgroundResource", widgetColor);

        Intent configIntent = new Intent(context, AddBirthday.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        rv.setOnClickPendingIntent(R.id.imageView, configPendingIntent);
        appWidgetManager.updateAppWidget(widgetID, rv);
    }
}
