package com.cray.software.justreminder.app_widgets.notes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.cray.software.justreminder.notes.NotesManager;
import com.cray.software.justreminder.R;

public class NotesWidget extends AppWidgetProvider {

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
                NotesWidgetConfig.NOTES_WIDGET_PREF, Context.MODE_PRIVATE);

        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int widgetID){

        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.note_widget_layout);
        int theme = sp.getInt(NotesWidgetConfig.NOTES_WIDGET_THEME + widgetID, 0);
        NotesTheme notesTheme = NotesTheme.getThemes(context).get(theme);

        int headerColor = notesTheme.getHeaderColor();
        int backgroundColor = notesTheme.getBackgroundColor();
        int titleColor = notesTheme.getTitleColor();
        int plusIcon = notesTheme.getPlusIcon();
        int settingsIcon = notesTheme.getSettingsIcon();

        rv.setInt(R.id.headerBg, "setBackgroundResource", headerColor);
        rv.setInt(R.id.widgetBg, "setBackgroundResource", backgroundColor);
        rv.setTextColor(R.id.widgetTitle, titleColor);
        rv.setInt(R.id.tasksCount, "setImageResource", plusIcon);

        Intent configIntent = new Intent(context, NotesManager.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.tasksCount, configPendingIntent);

        configIntent = new Intent(context, NotesWidgetConfig.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        rv.setOnClickPendingIntent(R.id.settingsButton, configPendingIntent);
        rv.setInt(R.id.settingsButton, "setImageResource", settingsIcon);

        Intent startActivityIntent = new Intent(context, NotesManager.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent);

        Intent adapter = new Intent(context, NotesService.class);
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
                NotesWidgetConfig.NOTES_WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(NotesWidgetConfig.NOTES_WIDGET_THEME + widgetID);
        }
        editor.commit();
    }
}
