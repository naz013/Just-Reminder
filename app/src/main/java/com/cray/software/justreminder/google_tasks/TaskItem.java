package com.cray.software.justreminder.google_tasks;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

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
public class TaskItem {

    private String title;
    private String taskId;
    private long completeDate;
    private int del;
    private long dueDate;
    private String eTag;
    private String kind;
    private String notes;
    private String parent;
    private String position;
    private String selfLink;
    private long updateDate;
    private long reminderId;
    private String listId;
    private String status;
    private int hidden;
    private long id;

    public TaskItem() {}

    public TaskItem(String title, String taskId, long completeDate, int del, long dueDate, String eTag,
                    String kind, String notes, String parent, String position, String selfLink,
                    long updateDate, long reminderId, String listId, String status, int hidden, long id) {
        this.title = title;
        this.taskId = taskId;
        this.completeDate = completeDate;
        this.del = del;
        this.dueDate = dueDate;
        this.eTag = eTag;
        this.kind = kind;
        this.notes = notes;
        this.parent = parent;
        this.position = position;
        this.selfLink = selfLink;
        this.updateDate = updateDate;
        this.reminderId = reminderId;
        this.listId = listId;
        this.status = status;
        this.hidden = hidden;
        this.id = id;
    }

    public void fromTask(Task task) {
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
        this.selfLink = task.getSelfLink();
        this.kind = task.getKind();
        this.eTag = task.getEtag();
        this.title = task.getTitle();
        this.taskId = taskId;
        this.completeDate = complete;
        this.del = isDeleted ? 1 : 0;
        this.hidden = isHidden ? 1 : 0;
        this.dueDate = due;
        this.notes = task.getNotes();
        this.parent = task.getParent();
        this.position = task.getPosition();
        this.updateDate = update;
        this.status = task.getStatus();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(long completeDate) {
        this.completeDate = completeDate;
    }

    public int getDel() {
        return del;
    }

    public void setDel(int del) {
        this.del = del;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public long getReminderId() {
        return reminderId;
    }

    public void setReminderId(long reminderId) {
        this.reminderId = reminderId;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
