package com.cray.software.justreminder.google_tasks;

import android.content.Context;

import java.util.List;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TasksHelper {

    private static TasksHelper groupHelper;
    private Context mContext;

    private TasksHelper(Context context) {
        this.mContext = context;
    }

    public static TasksHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new TasksHelper(context);
        }
        return groupHelper;
    }

    public TaskItem getTask(long id) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        TaskItem item = db.getTask(id);
        db.close();
        return item;
    }

    public TaskItem getTaskByReminder(long reminderId) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        TaskItem item = db.getTaskByReminder(reminderId);
        db.close();
        return item;
    }

    public TaskItem getTask(String taskId) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        TaskItem item = db.getTask(taskId);
        db.close();
        return item;
    }

    public boolean deleteTask(long id){
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        boolean isDeleted = db.deleteTask(id);
        db.close();
        return isDeleted;
    }

    public long saveTask(TaskItem item) {
        if (item == null) return 0;
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        long id = db.saveTask(item);
        db.close();
        return id;
    }

    public List<TaskItem> getTasks() {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskItem> list = db.getTasks();
        db.close();
        return list;
    }

    public List<TaskItem> getTasks(String listId) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskItem> list = db.getTasks(listId);
        db.close();
        return list;
    }

    public void deleteTasks(String listId) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskItem> list = db.getTasks(listId);
        for (TaskItem item : list) db.deleteTask(item.getId());
        db.close();
    }

    public void deleteTasks() {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskItem> list = db.getTasks();
        for (TaskItem item : list) db.deleteTask(item.getId());
        db.close();
    }

    public void deleteCompletedTasks(String listId) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskItem> list = db.getCompletedTasks(listId);
        for (TaskItem item : list) db.deleteTask(item.getId());
        db.close();
    }

    public TaskListItem getTaskList(long id) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        TaskListItem item = db.getTasksList(id);
        db.close();
        return item;
    }

    public TaskListItem getTaskList(String listId) {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        TaskListItem item = db.getTasksList(listId);
        db.close();
        return item;
    }

    public TaskListItem getDefaultTaskList() {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        TaskListItem item = db.getDefaultTasksList();
        db.close();
        return item;
    }

    public List<TaskListItem> getTaskLists() {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskListItem> list = db.getTaskLists();
        db.close();
        return list;
    }

    public void deleteTaskLists() {
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        List<TaskListItem> list = db.getTaskLists();
        for (TaskListItem item : list) db.deleteTasksList(item.getId());
        db.close();
    }

    public boolean deleteTaskList(long id){
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        boolean isDeleted = db.deleteTasksList(id);
        db.close();
        return isDeleted;
    }

    public long saveTaskList(TaskListItem item) {
        if (item == null) return 0;
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        long id = db.saveTaskList(item);
        db.close();
        return id;
    }

    public boolean setDefault(long id){
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        boolean isDeleted = db.setDefault(id);
        db.close();
        return isDeleted;
    }

    public boolean setSystemDefault(long id){
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        boolean isDeleted = db.setSystemDefault(id);
        db.close();
        return isDeleted;
    }

    public boolean setSimple(long id){
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        boolean isDeleted = db.setSimple(id);
        db.close();
        return isDeleted;
    }
}
