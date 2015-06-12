package com.cray.software.justreminder.widgets;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.cray.software.justreminder.CalendarActivity;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.dialogs.VoiceWidgetDialog;
import com.hexrain.design.ScreenManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarWidget extends AppWidgetProvider {

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
                CalendarWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Bundle options = appWidgetManager.getAppWidgetOptions(i);
                onAppWidgetOptionsChanged(context, appWidgetManager, i,
                        options);
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){
        Calendar cal = new GregorianCalendar();
        int month  = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetID, 0);
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime());

        int widgetBgColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_COLOR + widgetID, 0);
        int widgetTextColor = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_TITLE_COLOR + widgetID, 0);
        int widgetButton = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_BUTTON_COLOR + widgetID, 0);
        int widgetButtonVoice = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, 0);
        int widgetButtonSettings = sp.getInt(CalendarWidgetConfig.CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, 0);

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.calendar_widget_layout);
        rv.setTextViewText(R.id.currentDate, date);
        rv.setTextColor(R.id.currentDate, widgetTextColor);
        rv.setInt(R.id.widgetBg, "setBackgroundColor", widgetBgColor);

        Intent weekdayAdapter = new Intent(context, CalendarWeekdayService.class);
        weekdayAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        rv.setRemoteAdapter(R.id.weekdayGrid, weekdayAdapter);

        Intent startActivityIntent = new Intent(context, CalendarActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.monthGrid, startActivityPendingIntent);

        Intent monthAdapter = new Intent(context, CalendarMonthService.class);
        monthAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        rv.setRemoteAdapter(R.id.monthGrid, monthAdapter);

        Intent configIntent = new Intent(context, ReminderManager.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.plusButton, configPendingIntent);
        rv.setInt(R.id.plusButton, "setImageResource", widgetButton);

        configIntent = new Intent(context, VoiceWidgetDialog.class);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.voiceButton, configPendingIntent);
        rv.setInt(R.id.voiceButton, "setImageResource", widgetButtonVoice);

        configIntent = new Intent(context, CalendarWidgetConfig.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent);
        rv.setInt(R.id.settingsButton, "setImageResource", widgetButtonSettings);

        Intent serviceIntent = new Intent(context, CalendarUpdateService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        serviceIntent.putExtra("actionPlus", 2);
        PendingIntent servicePendingIntent =
                PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.nextMonth, servicePendingIntent);

        serviceIntent = new Intent(context, CalendarUpdateMinusService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        serviceIntent.putExtra("actionMinus", 1);
        servicePendingIntent =
                PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.prevMonth, servicePendingIntent);

        if (widgetButton == R.drawable.ic_add_white_24dp){
            //white
            rv.setInt(R.id.nextMonth, "setImageResource", R.drawable.ic_keyboard_arrow_right_white_24dp);
            rv.setInt(R.id.prevMonth, "setImageResource", R.drawable.ic_keyboard_arrow_left_white_24dp);
        } else {
            //black
            rv.setInt(R.id.nextMonth, "setImageResource", R.drawable.ic_keyboard_arrow_right_grey600_24dp);
            rv.setInt(R.id.prevMonth, "setImageResource", R.drawable.ic_keyboard_arrow_left_grey600_24dp);
        }

        appWidgetManager.updateAppWidget(widgetID, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.weekdayGrid);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.monthGrid);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                CurrentTaskWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(CalendarWidgetConfig.CURRENT_WIDGET_COLOR + widgetID);
            editor.remove(CalendarWidgetConfig.CURRENT_WIDGET_TITLE_COLOR + widgetID);
            editor.remove(CalendarWidgetConfig.CURRENT_WIDGET_BUTTON_COLOR + widgetID);
            editor.remove(CalendarWidgetConfig.CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID);
            editor.remove(CalendarWidgetConfig.CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID);
            editor.remove(CalendarWidgetConfig.CURRENT_WIDGET_MONTH + widgetID);
        }
        editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context ctxt,
                                          AppWidgetManager mgr,
                                          int appWidgetId,
                                          Bundle newOptions) {
        RemoteViews updateViews=
                new RemoteViews(ctxt.getPackageName(), R.layout.calendar_widget_layout);

        mgr.updateAppWidget(appWidgetId, updateViews);
    }
}
