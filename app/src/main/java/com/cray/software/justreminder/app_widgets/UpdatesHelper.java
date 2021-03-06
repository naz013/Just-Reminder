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

package com.cray.software.justreminder.app_widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.cray.software.justreminder.app_widgets.calendar.CalendarWidget;
import com.cray.software.justreminder.app_widgets.events.EventsWidget;
import com.cray.software.justreminder.app_widgets.notes.NotesWidget;
import com.cray.software.justreminder.app_widgets.tasks.TasksWidget;

public class UpdatesHelper {

    private static UpdatesHelper helper;
    private Context mContext;

    private UpdatesHelper(Context context){
        this.mContext = context;
    }

    public static UpdatesHelper getInstance(Context context) {
        if (helper == null) {
            helper = new UpdatesHelper(context);
        }
        return helper;
    }

    public void updateWidget(){
        Intent intent = new Intent(mContext, EventsWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new
                ComponentName(mContext, EventsWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        mContext.sendBroadcast(intent);
        updateCalendarWidget();
        updateTasksWidget();
    }

    public void updateNotesWidget(){
        Intent intent = new Intent(mContext, NotesWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new
                ComponentName(mContext, NotesWidget.class));
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
