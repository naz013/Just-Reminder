package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Data;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GTasksHelper {

    Context ctx;
    SharedPrefs prefs;

    private final HttpTransport m_transport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory m_jsonFactory = GsonFactory.getDefaultInstance();
    private GoogleAccountCredential m_credential;
    private Tasks service;
    private static final String APPLICATION_NAME = "Just Reminder/2.3.4";

    public GTasksHelper(Context context){
        this.ctx = context;
    }

    public void authorize(){
        prefs = new SharedPrefs(ctx);
        m_credential = GoogleAccountCredential.usingOAuth2(ctx, Collections.singleton(TasksScopes.TASKS));
        m_credential.setSelectedAccountName(new SyncHelper(ctx).decrypt(prefs.loadPrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER)));
        service = new Tasks.Builder(m_transport, m_jsonFactory, m_credential).setApplicationName(APPLICATION_NAME).build();
    }

    public boolean isLinked(){
        prefs = new SharedPrefs(ctx);
        return new SyncHelper(ctx).decrypt(prefs.loadPrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER)).matches(".*@.*");
    }

    public void insertTask(String taskTitle, String listId, long time, String note, long localId) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Task task = new Task();
            task.setTitle(taskTitle);
            if (note != null) task.setNotes(note);
            if (time != 0) task.setDue(new DateTime(time));

            TasksData data = new TasksData(ctx);
            data.open();

            Task result;
            if (listId != null && !listId.matches("")){
                result = service.tasks().insert(listId, task).execute();
            } else {
                Cursor c = data.getDefaultTasksList();
                if (c != null && c.moveToFirst()) {
                    listId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                    result = service.tasks().insert(listId, task).execute();
                } else {
                    result = service.tasks().insert("@default", task).execute();
                }
                if (c != null) c.close();
            }

            if (result != null){
                DateTime dueDate = result.getDue();
                long due = dueDate != null ? dueDate.getValue() : 0;

                DateTime completeDate = result.getCompleted();
                long complete = completeDate != null ? completeDate.getValue() : 0;

                DateTime updateDate = result.getUpdated();
                long update = updateDate != null ? updateDate.getValue() : 0;

                String taskId = result.getId();

                boolean isDeleted = false;
                try {
                    isDeleted = result.getDeleted();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                boolean isHidden = false;
                try {
                    isHidden = result.getHidden();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                Cursor cursor = data.getTask(localId);
                if (cursor != null && cursor.moveToFirst()){
                    data.updateFullTask(localId,
                            result.getTitle(), taskId, complete, isDeleted, due,
                            result.getEtag(), result.getKind(), result.getNotes(),
                            result.getParent(), result.getPosition(), result.getSelfLink(), update,
                            cursor.getLong(cursor.getColumnIndex(TasksConstants.COLUMN_REMINDER_ID)),
                            listId, result.getStatus(), isHidden);
                } else {
                    data.addTask(result.getTitle(), taskId, complete, isDeleted, due,
                            result.getEtag(), result.getKind(), result.getNotes(),
                            result.getParent(), result.getPosition(), result.getSelfLink(), update, 0,
                            listId, result.getStatus(), isHidden);
                }
                if (cursor != null) cursor.close();
            }
            data.close();
        }
    }

    public void updateTaskStatus(String status, String listId, String taskId) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(status);
            if (status.matches(Constants.TASKS_NEED_ACTION)) {
                task.setCompleted(Data.NULL_DATE_TIME);
            }
            task.setUpdated(new DateTime(System.currentTimeMillis()));

            Task result = service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    public void deleteTask(String listId, String taskId) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            service.tasks().delete(listId, taskId).execute();
        }
    }

    public void updateTask(String text, String listId, String taskId, String note, long time) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(Constants.TASKS_NEED_ACTION);
            task.setTitle(text);
            task.setCompleted(Data.NULL_DATE_TIME);
            if (time != 0) task.setDue(new DateTime(time));
            if (note != null) task.setNotes(note);
            task.setUpdated(new DateTime(System.currentTimeMillis()));

            Task result = service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    public List<Task> getTasks(String listId){
        List<Task> taskLists = new ArrayList<>();
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            try {
                taskLists = service.tasks().list(listId).execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return taskLists;
    }

    //Tasks list

    public TaskLists getTaskLists() throws IOException {
        TaskLists taskLists = null;
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            taskLists = service.tasklists().list().execute();
        }
        return taskLists;
    }

    public void insertTasksList(String listTitle, long id, int color) {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            TaskList taskList = new TaskList();
            taskList.setTitle(listTitle);

            try {
                TaskList result = service.tasklists().insert(taskList).execute();
                TasksData data = new TasksData(ctx);
                data.open();
                DateTime dateTime = result.getUpdated();
                long updated = dateTime != null ? dateTime.getValue() : 0;
                data.updateTasksList(id, result.getTitle(), result.getId(), 0, result.getEtag(), result.getKind(),
                        result.getSelfLink(), updated, color);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTasksList(final String listTitle, final String listId) throws IOException {
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            TaskList taskList = service.tasklists().get(listId).execute();
            taskList.setTitle(listTitle);

            TaskList result = service.tasklists().update(listId, taskList).execute();
        }
    }

    public void deleteTaskList (final String listId){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            try {
                service.tasklists().delete(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearTaskList (final String listId){
        if (isLinked()) {
            prefs = new SharedPrefs(ctx);
            authorize();
            try {
                service.tasks().clear(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
