package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.database.Cursor;

import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
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

/**
 * Google Tasks API helper class;
 */
public class GTasksHelper {

    public static final String TASKS_ALL = "all";
    public static final String TASKS_NEED_ACTION = "needsAction";
    public static final String TASKS_COMPLETE = "completed";

    private Context mContext;
    private SharedPrefs prefs;

    private final HttpTransport m_transport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory m_jsonFactory = GsonFactory.getDefaultInstance();
    private Tasks service;
    private static final String APPLICATION_NAME = "Reminder/5.0";

    public GTasksHelper(Context context){
        this.mContext = context;
    }

    /**
     * API authorization method;
     */
    public void authorize(){
        prefs = new SharedPrefs(mContext);
        GoogleAccountCredential m_credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(TasksScopes.TASKS));
        m_credential.setSelectedAccountName(SyncHelper.decrypt(prefs.loadPrefs(Prefs.DRIVE_USER)));
        service = new Tasks.Builder(m_transport, m_jsonFactory, m_credential).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Check if user has already login to Google Tasks;
     * @return Boolean
     */
    public boolean isLinked(){
        prefs = new SharedPrefs(mContext);
        return SyncHelper.decrypt(prefs.loadPrefs(Prefs.DRIVE_USER)).matches(".*@.*");
    }

    /**
     * Add new task to selected task list or add task to Tasks default list;
     * @param taskTitle title for a task.
     * @param listId list identifier.
     * @param time due time in milliseconds.
     * @param note note for task.
     * @param localId local identifier of task.
     * @throws IOException
     */
    public void insertTask(String taskTitle, String listId, long time, String note, long localId) throws IOException {
        if (isLinked()) {
            authorize();
            Task task = new Task();
            task.setTitle(taskTitle);
            if (note != null) task.setNotes(note);
            if (time != 0) task.setDue(new DateTime(time));

            TasksData data = new TasksData(mContext);
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

    /**
     * Update status of task (needsAction or completed);
     * @param status new status for a task (needsAction/completed)
     * @param listId list identifier.
     * @param taskId task identifier.
     * @throws IOException
     */
    public void updateTaskStatus(String status, String listId, String taskId) throws IOException {
        if (isLinked() && taskId != null && listId != null) {
            authorize();
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(status);
            if (status.matches(TASKS_NEED_ACTION)) {
                task.setCompleted(Data.NULL_DATE_TIME);
            }
            task.setUpdated(new DateTime(System.currentTimeMillis()));

            service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    /**
     * Delete selected task from task list
     * @param listId list identifier.
     * @param taskId task identifier.
     * @throws IOException
     */
    public void deleteTask(String listId, String taskId) throws IOException {
        if (isLinked() && taskId != null && listId != null) {
            authorize();
            service.tasks().delete(listId, taskId).execute();
        }
    }

    /**
     * Update information of selected task
     * @param text new title for a task.
     * @param listId list identifier.
     * @param taskId task identifier.
     * @param note note for task.
     * @param time due time (milliseconds).
     * @throws IOException
     */
    public void updateTask(String text, String listId, String taskId, String note, long time) throws IOException {
        if (isLinked() && taskId != null && listId != null) {
            authorize();
            Task task = service.tasks().get(listId, taskId).execute();
            task.setStatus(TASKS_NEED_ACTION);
            task.setTitle(text);
            task.setCompleted(Data.NULL_DATE_TIME);
            if (time != 0) task.setDue(new DateTime(time));
            if (note != null) task.setNotes(note);
            task.setUpdated(new DateTime(System.currentTimeMillis()));

            service.tasks().update(listId, task.getId(), task).execute();
        }
    }

    /**
     * Get list of task items by task list id
     * @param listId list identifier.
     * @return List of tasks for selected list id.
     */
    public List<Task> getTasks(String listId){
        List<Task> taskLists = new ArrayList<>();
        if (isLinked() && listId != null) {
            authorize();
            try {
                taskLists = service.tasks().list(listId).execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return taskLists;
    }

    /**
     * Get all available task lists
     * @return Task list object.
     * @throws IOException
     */
    public TaskLists getTaskLists() throws IOException {
        TaskLists taskLists = null;
        if (isLinked()) {
            authorize();
            taskLists = service.tasklists().list().execute();
        }
        return taskLists;
    }

    /**
     * Add new task list to Google Tasks
     * @param listTitle list title.
     * @param id local list id.
     * @param color local list color.
     */
    public void insertTasksList(String listTitle, long id, int color) {
        if (isLinked()) {
            authorize();
            TaskList taskList = new TaskList();
            taskList.setTitle(listTitle);

            try {
                TaskList result = service.tasklists().insert(taskList).execute();
                TasksData data = new TasksData(mContext);
                data.open();
                DateTime dateTime = result.getUpdated();
                long updated = dateTime != null ? dateTime.getValue() : 0;
                data.updateTasksList(id, result.getTitle(), result.getId(), 0, result.getEtag(), result.getKind(),
                        result.getSelfLink(), updated, color);
                data.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update information about task list
     * @param listTitle new list title.
     * @param listId list identifier.
     * @throws IOException
     */
    public void updateTasksList(final String listTitle, final String listId) throws IOException {
        if (isLinked()) {
            authorize();
            TaskList taskList = service.tasklists().get(listId).execute();
            taskList.setTitle(listTitle);

            service.tasklists().update(listId, taskList).execute();
        }
    }

    /**
     * Delete selected task list from Google Tasks
     * @param listId list identifier.
     */
    public void deleteTaskList (final String listId){
        if (isLinked()) {
            authorize();
            try {
                service.tasklists().delete(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete all completed tasks from selected task list
     * @param listId list identifier.
     */
    public void clearTaskList (final String listId){
        if (isLinked()) {
            authorize();
            try {
                service.tasks().clear(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Move task to other task list
     * @param listId list identifier.
     * @param taskId task identifier.
     */
    public void moveTask (String listId, String taskId){
        if (isLinked()) {
            authorize();
            try {
                service.tasks().move(listId, taskId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
