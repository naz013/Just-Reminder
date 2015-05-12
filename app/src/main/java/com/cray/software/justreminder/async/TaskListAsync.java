package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.io.IOException;

public class TaskListAsync extends AsyncTask<Void, Void, Void> {
    Context ctx;
    String title, listId, taskType;
    long id;
    int color;

    public TaskListAsync(Context context, String title, long id, int color, String listId, String taskType){
        this.ctx = context;
        this.title = title;
        this.id = id;
        this.color = color;
        this.listId = listId;
        this.taskType = taskType;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GTasksHelper helper = new GTasksHelper(ctx);
        boolean isConnected = SyncHelper.isConnected(ctx);
        TasksData data = new TasksData(ctx);
        data.open();
        if (taskType.matches(TasksConstants.UPDATE_TASK_LIST)){
            if (isConnected) {
                try {
                    helper.updateTasksList(title, listId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(title, listId, TasksConstants.UPDATE_LIST, 0, null, null, 0, 0, null);
            }
        }
        if (taskType.matches(TasksConstants.INSERT_TASK_LIST)){
            if (isConnected) {
                helper.insertTasksList(title, id, color);
            } else {
                data.add(title, null, TasksConstants.INSERT_LIST, color, null, null, id, 0, null);
            }
        }
        if (taskType.matches(TasksConstants.DELETE_TASK_LIST)){
            if (isConnected) {
                helper.deleteTaskList(listId);
            } else {
                data.add(null, listId, TasksConstants.DELETE_LIST, 0, null, null, 0, 0, null);
            }
        }
        if (taskType.matches(TasksConstants.CLEAR_TASK_LIST)){
            if (isConnected) {
                helper.clearTaskList(listId);
            } else {
                data.add(null, listId, TasksConstants.CLEAR_LIST, 0, null, null, 0, 0, null);
            }
        }
        data.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(ctx).updateTasksWidget();
    }
}
