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

package com.cray.software.justreminder.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.google_tasks.TaskItem;
import com.cray.software.justreminder.google_tasks.TaskListItem;
import com.cray.software.justreminder.google_tasks.TasksHelper;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.LoginListener;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.utils.MemoryUtil;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CloudLoginSynchronization extends AsyncTask<Void, String, Void> {

    private Context mContext;
    private boolean isChecked = false;
    private LoginListener listener;
    private ProgressDialog dialog;

    public CloudLoginSynchronization(Context context, boolean isChecked, LoginListener listener){
        this.mContext = context;
        this.isChecked = isChecked;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialog.show(mContext, mContext.getString(R.string.please_wait),
                mContext.getString(R.string.cloud_sync), true, false);
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        super.onProgressUpdate(values);
        new android.os.Handler().post(() -> dialog.setMessage(values[0]));
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (MemoryUtil.isSdPresent()) {
            IOHelper ioHelper = new IOHelper(mContext);
            publishProgress(mContext.getString(R.string.syncing_groups));
            ioHelper.restoreGroup(true);
            checkGroups();
            //import reminders
            publishProgress(mContext.getString(R.string.syncing_reminders));
            ioHelper.restoreReminder(true);
            //import notes
            publishProgress(mContext.getString(R.string.syncing_notes));
            ioHelper.restoreNote(true);
            //import birthdays
            if (isChecked) {
                publishProgress(mContext.getString(R.string.syncing_birthdays));
                ioHelper.restoreBirthday(true);
            }
        }
        //getting Google Tasks
        GTasksHelper helper = new GTasksHelper(mContext);
        TaskLists lists = null;
        try {
            lists = helper.getTaskLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lists != null && lists.size() > 0) {
            publishProgress(mContext.getString(R.string.syncing_google_tasks));
            for (TaskList item : lists.getItems()) {
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
        try {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (listener != null) listener.onCloud();
    }

    private void checkGroups() {
        GroupHelper helper = GroupHelper.getInstance(mContext);
        List<GroupItem> list = helper.getAll();
        if (list.size() == 0) {
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            helper.saveGroup(new GroupItem("General", defUiID, 5, 0, time));
            helper.saveGroup(new GroupItem("Work", SyncHelper.generateID(), 3, 0, time));
            helper.saveGroup(new GroupItem("Personal", SyncHelper.generateID(), 0, 0, time));
            List<ReminderItem> items = ReminderHelper.getInstance(mContext).getAll();
            for (ReminderItem item : items) {
                item.setGroupId(defUiID);
            }
            ReminderHelper.getInstance(mContext).saveReminders(items);
        }
    }
}
