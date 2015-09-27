package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import java.io.IOException;

public class SwitchTaskAsync extends AsyncTask<Void, Void, Void> {
    Context ctx;
    String taskId, listId;
    SyncListener mListener;
    boolean status;

    public SwitchTaskAsync(Context context, String listId, String taskId, boolean status, SyncListener listener){
        this.ctx = context;
        this.listId = listId;
        this.taskId = taskId;
        this.status = status;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GTasksHelper helper = new GTasksHelper(ctx);
        boolean isConnected = SyncHelper.isConnected(ctx);
        TasksData data = new TasksData(ctx);
        data.open();
        if (status){
            if (isConnected) {
                try {
                    helper.updateTaskStatus(GTasksHelper.TASKS_COMPLETE, listId, taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(null, listId, TasksConstants.UPDATE_STATUS, 0, taskId, null, 0, 0, GTasksHelper.TASKS_COMPLETE);
            }
        } else {
            if (isConnected) {
                try {
                    helper.updateTaskStatus(GTasksHelper.TASKS_NEED_ACTION, listId, taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(null, listId, TasksConstants.UPDATE_STATUS, 0, taskId, null, 0, 0, GTasksHelper.TASKS_NEED_ACTION);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(ctx).updateTasksWidget();
        if (mListener != null) {
            mListener.endExecution(true);
        }
    }
}
