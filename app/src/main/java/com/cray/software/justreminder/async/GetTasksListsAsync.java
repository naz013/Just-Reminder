package com.cray.software.justreminder.async;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class GetTasksListsAsync extends AsyncTask<Void, Void, Void> {
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

        TasksData data = new TasksData(mContext);
        data.open();

        if (lists != null && lists.size() > 0){
            for (TaskList item : lists.getItems()){
                DateTime dateTime = item.getUpdated();
                String listId = item.getId();
                Cursor c = data.getTasksList(listId);
                if (c != null && c.moveToFirst() && c.getCount() == 1){
                    data.updateTasksList(c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID)),
                            item.getTitle(), listId, c.getInt(c.getColumnIndex(TasksConstants.COLUMN_DEFAULT)),
                            item.getEtag(), item.getKind(),
                                item.getSelfLink(), dateTime != null ? dateTime.getValue() : 0,
                            c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR)));
                } else if (c != null && c.moveToFirst() && c.getCount() > 1){
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
                if (c != null) c.close();

                Cursor cc = data.getTasksLists();
                if (cc != null && cc.moveToFirst()){
                    data.setDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                    data.setSystemDefault(cc.getLong(cc.getColumnIndex(TasksConstants.COLUMN_ID)));
                }
                if (cc != null) cc.close();

                List<Task> tasks = helper.getTasks(listId);
                if (tasks != null && tasks.size() > 0){
                    for (Task task : tasks){
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
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        }

                        boolean isHidden = false;
                        try {
                            isHidden = task.getHidden();
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        }

                        Cursor cursor = data.getTask(taskId);
                        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 1){
                            do {
                                data.deleteTask(cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_ID)));
                            } while (cursor.moveToNext());
                            data.addTask(task.getTitle(), taskId, complete, isDeleted, due,
                                    task.getEtag(), task.getKind(), task.getNotes(),
                                    task.getParent(), task.getPosition(), task.getSelfLink(), update, 0,
                                    listId, task.getStatus(), isHidden);
                        } else if (cursor != null && cursor.moveToFirst() && cursor.getCount() == 1){
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
                        if (cursor != null) cursor.close();
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(mContext).updateTasksWidget();
        if (mListener != null) {
            mListener.endExecution(true);
        }
    }
}
