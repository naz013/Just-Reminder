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

import java.io.IOException;

public class TaskListAsync extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private String title, listId, taskType;
    private long id;
    private int color;

    public TaskListAsync(Context context, String title, long id, int color, String listId, String taskType){
        this.mContext = context;
        this.title = title;
        this.id = id;
        this.color = color;
        this.listId = listId;
        this.taskType = taskType;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GoogleTasks helper = new GoogleTasks(mContext);
        boolean isConnected = SyncHelper.isConnected(mContext);
        TasksDataBase data = new TasksDataBase(mContext);
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
        UpdatesHelper.getInstance(mContext).updateTasksWidget();
    }
}
