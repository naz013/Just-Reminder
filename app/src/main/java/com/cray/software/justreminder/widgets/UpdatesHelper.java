package com.cray.software.justreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class UpdatesHelper {

    Context ctx;

    public UpdatesHelper(Context context){
        this.ctx = context;
    }

    public void updateWidget(){
        Intent intent = new Intent(ctx, CurrentTaskWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new
                ComponentName(ctx, CurrentTaskWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        ctx.sendBroadcast(intent);
        updateCalendarWidget();
        updateTasksWidget();
    }

    public void updateNotesWidget(){
        Intent intent = new Intent(ctx, CurrentNotesWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new
                ComponentName(ctx, CurrentNotesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(intent);
    }

    public void updateCalendarWidget(){
        Intent intent = new Intent(ctx, CalendarWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new
                ComponentName(ctx, CalendarWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(intent);
    }

    public void updateTasksWidget(){
        Intent intent = new Intent(ctx, TasksWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new
                ComponentName(ctx, TasksWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(intent);
    }
}
