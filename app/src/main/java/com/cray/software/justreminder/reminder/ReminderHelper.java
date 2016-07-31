package com.cray.software.justreminder.reminder;

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
public class ReminderHelper {

    private static ReminderHelper groupHelper;
    private Context mContext;

    private ReminderHelper(Context context) {
        this.mContext = context;
    }

    public static ReminderHelper getInstance(Context context) {
        if (groupHelper == null) {
            groupHelper = new ReminderHelper(context);
        }
        return groupHelper;
    }

    public long saveReminder(ReminderItem item) {
        NextBase db = new NextBase(mContext);
        db.open();
        long id = db.saveReminder(item);
        db.close();
        return id;
    }

    public void saveReminders(List<ReminderItem> items) {
        NextBase db = new NextBase(mContext);
        db.open();
        for (ReminderItem item : items) db.saveReminder(item);
        db.close();
    }

    public ReminderItem getReminder(long id) {
        NextBase db = new NextBase(mContext);
        db.open();
        ReminderItem item = db.getReminder(id);
        db.close();
        return item;
    }

    public ReminderItem getReminder(String uuId) {
        NextBase db = new NextBase(mContext);
        db.open();
        ReminderItem item = db.getReminder(uuId);
        db.close();
        return item;
    }

    public List<ReminderItem> getRemindersActive() {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getReminders();
        db.close();
        return list;
    }

    public List<ReminderItem> getReminders(String groupId) {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getReminders(groupId);
        db.close();
        return list;
    }

    public List<ReminderItem> getReminders(long time) {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getReminders(time);
        db.close();
        return list;
    }

    public List<ReminderItem> findReminders(String key) {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getByKey(key);
        db.close();
        return list;
    }

    public List<ReminderItem> getRemindersArchived() {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getArchivedReminders();
        db.close();
        return list;
    }

    public List<ReminderItem> getRemindersEnabled() {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getActiveReminders();
        db.close();
        return list;
    }

    public List<ReminderItem> getLocationReminders() {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.getAllLocations();
        db.close();
        return list;
    }

    public List<ReminderItem> getAll() {
        NextBase db = new NextBase(mContext);
        db.open();
        List<ReminderItem> list = db.queryAllReminders();
        db.close();
        return list;
    }

    public ReminderItem deleteReminder(long id){
        NextBase db = new NextBase(mContext);
        db.open();
        ReminderItem item = db.getReminder(id);
        db.deleteReminder(id);
        db.close();
        return item;
    }

    public void deleteReminders(List<ReminderItem> items){
        NextBase db = new NextBase(mContext);
        db.open();
        for (ReminderItem item : items) db.deleteReminder(item.getId());
        db.close();
    }

    public void setStatus(long id, boolean status) {
        NextBase db = new NextBase(mContext);
        db.open();
        if (status) db.setDone(id);
        else db.setUnDone(id);
        db.close();
    }

    public void moveToArchive(long id) {
        NextBase db = new NextBase(mContext);
        db.open();
        db.toArchive(id);
        db.close();
    }

    public void setLocation(long id, int status) {
        NextBase db = new NextBase(mContext);
        db.open();
        db.setLocationStatus(id, status);
        db.close();
    }

    public void changeGroup(long id, String groupId) {
        NextBase db = new NextBase(mContext);
        db.open();
        db.setGroup(id, groupId);
        db.close();
    }
}
