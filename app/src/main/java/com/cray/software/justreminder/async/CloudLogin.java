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
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.LoginListener;
import com.cray.software.justreminder.utils.MemoryUtil;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CloudLogin extends AsyncTask<Void, String, Void> {

    private Context mContext;
    private boolean isChecked = false;
    private LoginListener listener;
    private ProgressDialog dialog;

    public CloudLogin(Context context, boolean isChecked,
                    LoginListener listener){
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
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage(values[0]);
            }
        });
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
                ioHelper.restoreBirthday(true, false);
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

        TasksData data = new TasksData(mContext);
        data.open();
        if (lists != null && lists.size() > 0) {
            publishProgress(mContext.getString(R.string.syncing_google_tasks));
            for (TaskList item : lists.getItems()) {
                DateTime dateTime = item.getUpdated();
                String listId = item.getId();
                Cursor c = data.getTasksList(listId);
                if (c != null && c.moveToFirst() && c.getCount() == 1) {
                    data.updateTasksList(c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)),
                            item.getTitle(), listId, c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT)),
                            item.getEtag(), item.getKind(),
                            item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0,
                            c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR)));
                } else if (c != null && c.moveToFirst() && c.getCount() > 1) {
                    do {
                        data.deleteTasksList(c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)));
                    } while (c.moveToNext());
                    Random r = new Random();
                    int color = r.nextInt(15);
                    data.addTasksList(item.getTitle(), listId, 0, item.getEtag(), item.getKind(),
                            item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0, color);
                } else {
                    Random r = new Random();
                    int color = r.nextInt(15);
                    data.addTasksList(item.getTitle(), listId, 0, item.getEtag(), item.getKind(),
                            item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0, color);
                }
                if (c != null) {
                    c.close();
                }

                Cursor cc = data.getTasksLists();
                if (cc != null && cc.moveToFirst()) {
                    data.setDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                    data.setSystemDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                }
                if (cc != null) {
                    cc.close();
                }

                List<Task> tasks = helper.getTasks(listId);
                if (tasks != null && tasks.size() > 0) {
                    for (Task task : tasks) {
                        DateTime dueDate = task.getDue();
                        long due = dueDate != null ? dueDate.getValue() : 0;

                        DateTime completeDate = task.getCompleted();
                        long complete = completeDate != null ? completeDate.getValue() : 0;

                        DateTime updateDate = task.getUpdated();
                        long update = updateDate != null ? updateDate.getValue() : 0;

                        String taskId = task.getId();

                        boolean isDeleted = false;
                        try {
                            isDeleted = task.getDeleted();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                        boolean isHidden = false;
                        try {
                            isHidden = task.getHidden();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                        Cursor cursor = data.getTask(taskId);
                        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 1) {
                            do {
                                data.deleteTask(cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_ID)));
                            } while (cursor.moveToNext());
                            data.addTask(task.getTitle(), taskId, complete, isDeleted, due,
                                    task.getEtag(), task.getKind(), task.getNotes(),
                                    task.getParent(), task.getPosition(), task.getSelfLink(), update, 0,
                                    listId, task.getStatus(), isHidden);
                        } else if (cursor != null && cursor.moveToFirst() && cursor.getCount() == 1) {
                            data.updateFullTask(cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_ID)),
                                    task.getTitle(), taskId, complete, isDeleted, due,
                                    task.getEtag(), task.getKind(), task.getNotes(),
                                    task.getParent(), task.getPosition(), task.getSelfLink(), update,
                                    cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_REMINDER_ID)),
                                    listId, task.getStatus(), isHidden);
                        } else {
                            data.addTask(task.getTitle(), taskId, complete, isDeleted, due,
                                    task.getEtag(), task.getKind(), task.getNotes(),
                                    task.getParent(), task.getPosition(), task.getSelfLink(), update, 0,
                                    listId, task.getStatus(), isHidden);
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
        data.close();
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
        DataBase DB = new DataBase(mContext);
        DB.open();
        Cursor cat = DB.queryCategories();
        if (cat == null || cat.getCount() == 0){
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            DB.addCategory("General", time, defUiID, 5);
            DB.addCategory("Work", time, SyncHelper.generateID(), 3);
            DB.addCategory("Personal", time, SyncHelper.generateID(), 0);

            NextBase db = new NextBase(mContext);
            db.open();
            Cursor c = db.getReminders();
            if (c != null && c.moveToFirst()){
                do {
                    db.setGroup(c.getLong(c.getColumnIndex(NextBase._ID)), defUiID);
                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }
            db.close();
        }
        if (cat != null) cat.close();
        DB.close();
    }
}
