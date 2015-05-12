package com.cray.software.justreminder.async;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.interfaces.TasksConstants;

import java.io.IOException;

public class DelayedAsync extends AsyncTask<Void, Void, Void> {

    Context mContext;
    SyncListener mListener;

    public DelayedAsync(Context context, SyncListener listener){
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        GTasksHelper helper = new GTasksHelper(mContext);
        TasksData mData = new TasksData(mContext);
        mData.open();
        boolean isConnected = SyncHelper.isConnected(mContext);
        if (isConnected) {
            Cursor c = mData.get();
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                    int code = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_CODE));

                    // lists
                    if (code == TasksConstants.INSERT_LIST) {
                        String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                        long localId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_LOCAL_ID));
                        int color = c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR));
                        helper.insertTasksList(title, localId, color);
                        mData.delete(id);
                    }
                    if (code == TasksConstants.UPDATE_LIST) {
                        String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
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
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        if (listId != null) {
                            helper.deleteTaskList(listId);
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.CLEAR_LIST) {
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        if (listId != null) {
                            helper.clearTaskList(listId);
                            mData.delete(id);
                        }
                    }

                    if (code == TasksConstants.UPDATE_STATUS) {
                        String status = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        if (status.matches(Constants.TASKS_COMPLETE) && listId != null && taskId != null) {
                            try {
                                helper.updateTaskStatus(Constants.TASKS_COMPLETE, listId, taskId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                        if (status.matches(Constants.TASKS_NEED_ACTION) && listId != null && taskId != null) {
                            try {
                                helper.updateTaskStatus(Constants.TASKS_NEED_ACTION, listId, taskId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.INSERT) {
                        String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long time = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        long localId = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_LOCAL_ID));
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
                        String title = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                        String note = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                        long time = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                        if (listId != null && taskId != null) {
                            try {
                                helper.updateTask(title, listId, taskId, note, time);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mData.delete(id);
                        }
                    }
                    if (code == TasksConstants.DELETE) {
                        String listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                        String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
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
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null) {
            new SyncGoogleTasksAsync(mContext, mListener).execute();
        }
    }
}
