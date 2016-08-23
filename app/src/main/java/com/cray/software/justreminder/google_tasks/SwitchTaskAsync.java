/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.google_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.cloud.GoogleTasks;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;

import java.io.IOException;

public class SwitchTaskAsync extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private String taskId, listId;
    private SyncListener mListener;
    private boolean status;

    public SwitchTaskAsync(Context context, String listId, String taskId, boolean status, SyncListener listener){
        this.mContext = context;
        this.listId = listId;
        this.taskId = taskId;
        this.status = status;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GoogleTasks helper = new GoogleTasks(mContext);
        boolean isConnected = SyncHelper.isConnected(mContext);
        TasksDataBase data = new TasksDataBase(mContext);
        data.open();
        if (status){
            if (isConnected) {
                try {
                    helper.updateTaskStatus(GoogleTasks.TASKS_COMPLETE, listId, taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(null, listId, TasksConstants.UPDATE_STATUS, 0, taskId, null, 0, 0, GoogleTasks.TASKS_COMPLETE);
            }
        } else {
            if (isConnected) {
                try {
                    helper.updateTaskStatus(GoogleTasks.TASKS_NEED_ACTION, listId, taskId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                data.add(null, listId, TasksConstants.UPDATE_STATUS, 0, taskId, null, 0, 0, GoogleTasks.TASKS_NEED_ACTION);
            }
        }
        data.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        UpdatesHelper.getInstance(mContext).updateTasksWidget();
        if (mListener != null) {
            mListener.endExecution(true);
        }
    }
}
