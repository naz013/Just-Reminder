/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.app_widgets.events;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.reminder.AddReminderActivity;
import com.cray.software.justreminder.dialogs.VoiceWidgetDialog;
import com.cray.software.justreminder.reminder.ReminderActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class EventsWidget extends AppWidgetProvider {

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
                EventsWidgetConfig.EVENTS_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){
        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault());
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime());

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.current_tasks_widget_layout);
        rv.setTextViewText(R.id.widgetDate, date);
        int theme = sp.getInt(EventsWidgetConfig.EVENTS_WIDGET_THEME + widgetID, 0);
        EventsTheme eventsTheme = EventsTheme.getThemes(context).get(theme);
        int headerColor = eventsTheme.getHeaderColor();
        int backgroundColor = eventsTheme.getBackgroundColor();
        int titleColor = eventsTheme.getTitleColor();
        int plusIcon = eventsTheme.getPlusIcon();
        int voiceIcon = eventsTheme.getVoiceIcon();
        int settingsIcon = eventsTheme.getSettingsIcon();

        rv.setTextColor(R.id.widgetDate, titleColor);
        rv.setInt(R.id.headerBg, "setBackgroundResource", headerColor);
        rv.setInt(R.id.widgetBg, "setBackgroundResource", backgroundColor);

        Intent configIntent = new Intent(context, AddReminderActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent);
        rv.setInt(R.id.tasksCount, "setImageResource", plusIcon);

        configIntent = new Intent(context, VoiceWidgetDialog.class);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.voiceButton, configPendingIntent);
        rv.setInt(R.id.voiceButton, "setImageResource", voiceIcon);

        configIntent = new Intent(context, EventsWidgetConfig.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent);
        rv.setInt(R.id.settingsButton, "setImageResource", settingsIcon);

        Intent startActivityIntent = new Intent(context, ReminderActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent);

        Intent adapter = new Intent(context, EventsService.class);
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
                EventsWidgetConfig.EVENTS_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(EventsWidgetConfig.EVENTS_WIDGET_THEME + widgetID);
            editor.remove(EventsWidgetConfig.EVENTS_WIDGET_TEXT_SIZE + widgetID);
        }
        editor.apply();
    }
}
