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
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.GoogleTasks;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;

import java.io.IOException;

public class DelayedAsync extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private SyncListener mListener;

    public DelayedAsync(Context context, SyncListener listener){
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GoogleTasks helper = new GoogleTasks(mContext);
        TasksDataBase mData = new TasksDataBase(mContext);
        mData.open();
        boolean isConnected = SyncHelper.isConnected(mContext);
        if (isConnected) {
            Cursor c = mData.get();
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_ID));
                    int code = c.getInt(c.getColumnIndex(TasksDataBase.COLUMN_CODE));

                    // lists
                    if (code == TasksConstants.INSERT_LIST) {
                        String title = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TITLE));
                        long localId = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_LOCAL_ID));
                        int color = c.getInt(c.getColumnIndex(TasksDataBase.COLUMN_COLOR));
                        helper.insertTasksList(title, localId, color);
                        mData.delete(id);
                    }
                    if (code == TasksConstants.UPDATE_LIST) {
                        String title = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        if (listId != null) {
                            try {
                                helper.updateTasksList(title, listId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.DELETE_LIST) {
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        if (listId != null) {
                            helper.deleteTaskList(listId);
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.CLEAR_LIST) {
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        if (listId != null) {
                            helper.clearTaskList(listId);
                            mData.delete(id);
                        }
                    }

                    if (code == TasksConstants.UPDATE_STATUS) {
                        String status = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_STATUS));
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TASK_ID));
                        if (status.matches(GoogleTasks.TASKS_COMPLETE) && listId != null && taskId != null) {
                            try {
                                helper.updateTaskStatus(GoogleTasks.TASKS_COMPLETE, listId, taskId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                        if (status.matches(GoogleTasks.TASKS_NEED_ACTION) && listId != null && taskId != null) {
                            try {
                                helper.updateTaskStatus(GoogleTasks.TASKS_NEED_ACTION, listId, taskId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.INSERT) {
                        String title = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        String note = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_NOTES));
                        long time = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_DUE));
                        long localId = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_LOCAL_ID));
                        if (listId != null && localId != 0) {
                            try {
                                helper.insertTask(title, listId, time, note, localId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.UPDATE) {
                        String title = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TASK_ID));
                        String note = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_NOTES));
                        long time = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_DUE));
                        if (listId != null && taskId != null) {
                            try {
                                helper.updateTask(title, listId, taskId, note, time);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.MOVE){
                        String title = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TASK_ID));
                        String note = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_NOTES));
                        String oldList = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_STATUS));
                        long localId = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_LOCAL_ID));
                        long time = c.getLong(c.getColumnIndex(TasksDataBase.COLUMN_DUE));
                        if (listId != null && taskId != null) {
                            try {
                                helper.updateTask(title, listId, taskId, note, time);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            helper.moveTask(listId, taskId, oldList, localId);
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.DELETE) {
                        String listId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksDataBase.COLUMN_TASK_ID));
                        if (listId != null && taskId != null) {
                            try {
                                helper.deleteTask(listId, taskId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }
        mData.close();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        new SyncGoogleTasksAsync(mContext, mListener).execute();
    }
}
