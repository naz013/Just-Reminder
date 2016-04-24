package com.cray.software.justreminder.app_widgets.services;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.models.Task;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.app_widgets.configs.TasksWidgetConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TasksFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int widgetID;
    private ColorSetter cs;
    private ArrayList<Task> mData;
    private Map<String, Integer> map;

    TasksFactory(Context ctx, Intent intent) {
        mContext = ctx;
        widgetID = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mData = new ArrayList<>();
        cs = new ColorSetter(mContext);
        map = new HashMap<>();
    }

    @Override
    public void onDataSetChanged() {
        mData.clear();
        map.clear();
        TasksData data = new TasksData(mContext);
        data.open();
        Cursor c = data.getTasksLists();
        if (c != null && c.moveToFirst()){
            do {
                String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                int color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                map.put(listId, color);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        c = data.getTasks();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                if (title != null && !title.matches("")) {
                    long date = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                    String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                    String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    String checks = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                    String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                    long mId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));

                    int color = 0;
                    if (map.containsKey(listId)) {
                        color = map.get(listId);
                    }

                    if (checks.matches(GTasksHelper.TASKS_NEED_ACTION)) {
                        mData.add(new Task(title, mId, checks, taskId, date, listId, note, color));
                    }
                }
            } while (c.moveToNext());
        }
        if(c != null) c.close();
        data.close();
    }

    @Override
    public void onDestroy() {
        map.clear();
        mData.clear();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        SharedPreferences sp = mContext.getSharedPreferences(
                TasksWidgetConfig.CURRENT_WIDGET_PREF, Context.MODE_PRIVATE);
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_tasks_widget);
        int itemTextColor = sp.getInt(TasksWidgetConfig.CURRENT_WIDGET_ITEM_COLOR + widgetID, 0);

        rView.setTextColor(R.id.task, itemTextColor);
        rView.setTextColor(R.id.note, itemTextColor);
        rView.setTextColor(R.id.taskDate, itemTextColor);

        rView.setViewVisibility(R.id.checkDone, View.GONE);

        rView.setInt(R.id.listColor, "setBackgroundColor", cs.getNoteColor(map.get(mData.get(i).getListId())));

        final String name = mData.get(i).getTitle();

        rView.setTextViewText(R.id.task, name);

        SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());

        String notes = mData.get(i).getNote();
        if (notes != null && !notes.matches("")) rView.setTextViewText(R.id.note, notes);
        else rView.setViewVisibility(R.id.note, View.GONE);

        long date = mData.get(i).getDate();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (date != 0) {
            calendar.setTimeInMillis(date);
            String update = full24Format.format(calendar.getTime());
            rView.setTextViewText(R.id.taskDate, update);
        } else rView.setViewVisibility(R.id.taskDate, View.GONE);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.ITEM_ID_INTENT, mData.get(i).getId());
        fillInIntent.putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT);
        rView.setOnClickFillInIntent(R.id.task, fillInIntent);
        rView.setOnClickFillInIntent(R.id.note, fillInIntent);
        rView.setOnClickFillInIntent(R.id.taskDate, fillInIntent);
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}