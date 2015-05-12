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
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.dialogs.VoiceWidgetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CurrentTaskWidget extends AppWidgetProvider {

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
                CurrentTaskWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){
        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM yyyy");
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime());

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.current_tasks_widget_layout);
        rv.setTextViewText(R.id.widgetDate, date);

        int widgetColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_HEADER_COLOR + widgetID, 0);
        int widgetBgColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_COLOR + widgetID, 0);
        int widgetTextColor = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_TEXT_COLOR + widgetID, 0);
        int widgetButton = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_BUTTON_COLOR + widgetID, 0);
        int widgetButtonVoice = sp.getInt(CurrentTaskWidgetConfig.CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, 0);

        rv.setTextColor(R.id.widgetDate, widgetTextColor);
        rv.setInt(R.id.headerBg, "setBackgroundColor", widgetColor);
        rv.setInt(R.id.widgetBg, "setBackgroundColor", widgetBgColor);

        Intent configIntent = new Intent(context, QuickAddReminder.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent);
        rv.setInt(R.id.tasksCount, "setImageResource", widgetButton);

        configIntent = new Intent(context, VoiceWidgetDialog.class);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.voiceButton, configPendingIntent);
        rv.setInt(R.id.voiceButton, "setImageResource", widgetButtonVoice);

        Intent startActivityIntent = new Intent(context, ReminderManager.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent);

        Intent adapter = new Intent(context, CurrentTaskService.class);
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
                CurrentTaskWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(CurrentTaskWidgetConfig.CURRENT_WIDGET_COLOR + widgetID);
            editor.remove(CurrentTaskWidgetConfig.CURRENT_WIDGET_HEADER_COLOR + widgetID);
            editor.remove(CurrentTaskWidgetConfig.CURRENT_WIDGET_TEXT_COLOR + widgetID);
        }
        editor.commit();
    }
}
