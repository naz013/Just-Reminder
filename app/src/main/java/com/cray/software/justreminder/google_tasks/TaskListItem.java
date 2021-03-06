package com.cray.software.justreminder.google_tasks;

import com.google.api.services.tasks.model.TaskList;

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
public class TaskListItem {

    private String title;
    private String listId;
    private int def;
    private String eTag;
    private String kind;
    private String selfLink;
    private long updated;
    private int color;
    private int systemDefault;
    private long id;

    public TaskListItem() {}

    public TaskListItem(String title, String listId, int def, String eTag, String kind, String selfLink,
                        long updated, int color, long id, int systemDefault) {
        this.title = title;
        this.listId = listId;
        this.def = def;
        this.eTag = eTag;
        this.kind = kind;
        this.selfLink = selfLink;
        this.updated = updated;
        this.color = color;
        this.id = id;
        this.systemDefault = systemDefault;
    }

    public void fromTaskList(TaskList taskList) {
        setTitle(taskList.getTitle());
        setListId(taskList.getId());
        seteTag(taskList.getEtag());
        setKind(taskList.getKind());
        setSelfLink(taskList.getSelfLink());
        setUpdated(taskList.getUpdated() != null ? taskList.getUpdated().getValue() : 0);
    }

    public int getSystemDefault() {
        return systemDefault;
    }

    public void setSystemDefault(int systemDefault) {
        this.systemDefault = systemDefault;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
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

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
