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
import android.util.Log;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class GetTasksListsAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "GetTasksListsAsync";
    private Context mContext;
    private SyncListener mListener;

    public GetTasksListsAsync(Context context, SyncListener listener){
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GTasksHelper helper = new GTasksHelper(mContext);
        TaskLists lists = null;
        try {
            lists = helper.getTaskLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lists != null && lists.size() > 0){
            for (TaskList item : lists.getItems()){
                String listId = item.getId();
                TaskListItem taskList = TasksHelper.getInstance(mContext).getTaskList(listId);
                if (taskList != null){
                    taskList.fromTaskList(item);
                } else {
                    Random r = new Random();
                    int color = r.nextInt(15);
                    taskList = new TaskListItem();
                    taskList.setColor(color);
                    taskList.fromTaskList(item);
                }
                TasksHelper.getInstance(mContext).saveTaskList(taskList);
                TaskListItem listItem = TasksHelper.getInstance(mContext).getTaskLists().get(0);
                TasksHelper.getInstance(mContext).setDefault(listItem.getId());
                TasksHelper.getInstance(mContext).setSystemDefault(listItem.getId());
                List<Task> tasks = helper.getTasks(listId);
                for (Task task : tasks){
                    TaskItem taskItem = TasksHelper.getInstance(mContext).getTask(task.getId());
                    if (taskItem != null){
                        taskItem.setListId(listId);
                        taskItem.fromTask(task);
                    } else {
                        taskItem = new TaskItem();
                        taskItem.setListId(listId);
                        taskItem.fromTask(task);
                    }
                    TasksHelper.getInstance(mContext).saveTask(taskItem);
                }
            }
        }
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
