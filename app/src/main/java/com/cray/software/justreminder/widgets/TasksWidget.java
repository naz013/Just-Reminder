package com.cray.software.justreminder.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.interfaces.TasksConstants;

public class TasksWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        SharedPreferences sp = context.getSharedPreferences(
                TasksWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.tasks_widget_layout);

        int widgetColor = sp.getInt(TasksWidgetConfig.CURRENT_WIDGET_HEADER_COLOR + widgetID, 0);
        int widgetBgColor = sp.getInt(TasksWidgetConfig.CURRENT_WIDGET_COLOR + widgetID, 0);
        int widgetTitleColor = sp.getInt(TasksWidgetConfig.CURRENT_WIDGET_TITLE_COLOR + widgetID, 0);
        int widgetButton = sp.getInt(TasksWidgetConfig.CURRENT_WIDGET_BUTTON_COLOR + widgetID, 0);
        int widgetButtonSettings = sp.getInt(TasksWidgetConfig.CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, 0);

        rv.setInt(R.id.headerBg, "setBackgroundColor", widgetColor);
        rv.setInt(R.id.widgetBg, "setBackgroundColor", widgetBgColor);
        rv.setTextColor(R.id.widgetTitle, widgetTitleColor);
        rv.setInt(R.id.tasksCount, "setImageResource", widgetButton);

        Intent configIntent = new Intent(context, TaskManager.class);
        configIntent.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent);

        configIntent = new Intent(context, TasksWidgetConfig.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent);
        rv.setInt(R.id.settingsButton, "setImageResource", widgetButtonSettings);

        Intent startActivityIntent = new Intent(context, TaskManager.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent);

        Intent adapter = new Intent(context, TasksService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        rv.setRemoteAdapter(android.R.id.list, adapter);
        appWidgetManager.updateAppWidget(widgetID, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID,
                android.R.id.list);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                TasksWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(TasksWidgetConfig.CURRENT_WIDGET_COLOR + widgetID);
            editor.remove(TasksWidgetConfig.CURRENT_WIDGET_HEADER_COLOR + widgetID);
            editor.remove(TasksWidgetConfig.CURRENT_WIDGET_BUTTON_COLOR + widgetID);
            editor.remove(TasksWidgetConfig.CURRENT_WIDGET_TITLE_COLOR + widgetID);
            editor.remove(TasksWidgetConfig.CURRENT_WIDGET_ITEM_COLOR + widgetID);
        }
        editor.commit();
    }
}
