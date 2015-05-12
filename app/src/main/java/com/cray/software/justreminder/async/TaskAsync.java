package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.io.IOException;

public class TaskAsync extends AsyncTask<Void, Void, Void> {
    Context ctx;
    String title, listId, taskId, taskType, note;
    long time, localId;

    public TaskAsync(Context context, String title, String listId, String taskId, String taskType,
                     long time, String note, long localId){
        this.ctx = context;
        this.title = title;
        this.time = time;
        this.listId = listId;
        this.taskId = taskId;
        this.taskType = taskType;
        this.note = note;
        this.localId = localId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GTasksHelper helper = new GTasksHelper(ctx);
        boolean isConnected = SyncHelper.isConnected(ctx);
        TasksData data = new TasksData(ctx);
        data.open();
        if (taskType.matches(TasksConstants.DELETE_TASK)){
            //delete task
            if (isConnected) {
                try {
                    helper.deleteTask(listId, taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(null, listId, TasksConstants.DELETE, 0, taskId, null, 0, 0, null);
            }
        }
        if (taskType.matches(TasksConstants.UPDATE_TASK)){
            //update task
            if (isConnected) {
                try {
                    helper.updateTask(title, listId, taskId, note, time);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(title, listId, TasksConstants.UPDATE, 0, taskId, note, 0, time, null);
            }
        }
        if (taskType.matches(TasksConstants.INSERT_TASK)){
            //insert task
            if (isConnected) {
                try {
                    helper.insertTask(title, listId, time, note, localId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(title, listId, TasksConstants.INSERT, 0, null, note, localId, time, null);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(ctx).updateTasksWidget();
    }
}
