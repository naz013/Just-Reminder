package com.cray.software.justreminder.reminder;

import com.cray.software.justreminder.reminder.json.JParser;
import com.cray.software.justreminder.reminder.json.JsonModel;

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
public class ReminderItem {

    public static final int ACTIVE = 0;
    public static final int TRASH = 1;
    public static final int ENABLED = 0;
    public static final int DISABLED = 1;

    private JsonModel model;
    private String summary;
    private String json;
    private String type;
    private String uuId;
    private String categoryUuId;
    private String tags;
    private int list;
    private int status;
    private int location;
    private int reminder;
    private int notification;
    private long dateTime;
    private long delay;
    private long id;

    public ReminderItem(String summary, String json, String type, String uuId,
                        String categoryUuId, String tags, int list, int status, int location,
                        int reminder, int notification, long dateTime, long delay, long id) {
        this.model = new JParser(json).parse();
        this.summary = summary;
        this.json = json;
        this.type = type;
        this.uuId = uuId;
        this.categoryUuId = categoryUuId;
        this.tags = tags;
        this.list = list;
        this.status = status;
        this.location = location;
        this.reminder = reminder;
        this.notification = notification;
        this.dateTime = dateTime;
        this.delay = delay;
        this.id = id;
    }

    public JsonModel getModel() {
        return model;
    }

    public void setModel(JsonModel model) {
        this.model = model;
        this.json = new JParser().toJsonString(model);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
        this.model = new JParser(json).parse();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getCategoryUuId() {
        return categoryUuId;
    }

    public void setCategoryUuId(String categoryUuId) {
        this.categoryUuId = categoryUuId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getList() {
        return list;
    }

    public void setList(int list) {
        this.list = list;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getReminder() {
        return reminder;
    }

    public void setReminder(int reminder) {
        this.reminder = reminder;
    }

    public int getNotification() {
        return notification;
    }

    public void setNotification(int notification) {
        this.notification = notification;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
