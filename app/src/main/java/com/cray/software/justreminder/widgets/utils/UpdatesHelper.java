package com.cray.software.justreminder.widgets.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.widgets.CalendarWidget;
import com.cray.software.justreminder.widgets.CurrentNotesWidget;
import com.cray.software.justreminder.widgets.CurrentTaskWidget;
import com.cray.software.justreminder.widgets.TasksWidget;

public class UpdatesHelper {

    private Context mContext;

    public UpdatesHelper(Context context){
        this.mContext = context;
    }

    public void updateWidget(){
        Intent intent = new Intent(mContext, CurrentTaskWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new
                ComponentName(mContext, CurrentTaskWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        mContext.sendBroadcast(intent);
        updateCalendarWidget();
        updateTasksWidget();
    }

    public void updateNotesWidget(){
        Intent intent = new Intent(mContext, CurrentNotesWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new
                ComponentName(mContext, CurrentNotesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        mContext.sendBroadcast(intent);
    }

    public void updateCalendarWidget(){
        Intent intent = new Intent(mContext, CalendarWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new
                ComponentName(mContext, CalendarWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        mContext.sendBroadcast(intent);
    }

    public void updateTasksWidget(){
        Intent intent = new Intent(mContext, TasksWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new
                ComponentName(mContext, TasksWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        mContext.sendBroadcast(intent);
    }
}
