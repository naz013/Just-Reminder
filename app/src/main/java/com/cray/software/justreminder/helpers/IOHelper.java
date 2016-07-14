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

package com.cray.software.justreminder.helpers;

import android.content.Context;

import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.MemoryUtil;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Backup files save, sync, delete helper class.
 */
public class IOHelper {

    private Context mContext;
    private boolean isConnected;

    public IOHelper (Context context){
        this.mContext = context;
        isConnected = SyncHelper.isConnected(context);
    }

    /**
     * Delete all local and cloud file copies.
     * @param name file name.
     */
    public void deleteReminder(String name){
        String exportFileName = name + FileConfig.FILE_NAME_REMINDER;
        File dir = MemoryUtil.getRDir();
        if (dir != null) {
            File file = new File(dir, exportFileName);
            if (file.exists()) file.delete();
        }
        dir = MemoryUtil.getDRDir();
        if (dir != null) {
            File file = new File(dir, exportFileName);
            if (file.exists()) file.delete();
        }
        dir = MemoryUtil.getGRDir();
        if (dir != null) {
            File file = new File(dir, exportFileName);
            if (file.exists()) file.delete();
        }
        if (isConnected){
            new DropboxHelper(mContext).deleteReminder(name);
            new GDriveHelper(mContext).deleteReminder(name);
        }
    }

    /**
     * Create backup files for reminders, groups, birthdays and notes.
     */
    public void backup(){
        backupGroup(true);
        backupReminder(true);
        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        if (prefs.getBoolean(Prefs.SYNC_NOTES)){
            backupNote(true);
        }
        if (prefs.getBoolean(Prefs.SYNC_BIRTHDAYS)){
            backupBirthday(true);
        }
    }

    /**
     * Create backup files for groups.
     * @param isCloud create cloud backup.
     */
    public void backupGroup(boolean isCloud){
        try {
            new SyncHelper(mContext).groupToJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadGroup();
            try {
                new GDriveHelper(mContext).saveGroupToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Restore all groups from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreGroup(boolean isCloud){
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                try {
                    new SyncHelper(mContext).groupFromJson(null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadGroup();
            try {
                new GDriveHelper(mContext).downloadGroup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for reminder.
     * @param isCloud create cloud backup.
     */
    public void backupReminder(boolean isCloud){
        try {
            new SyncHelper(mContext).reminderToJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadReminder(null);
            try {
                new GDriveHelper(mContext).saveReminderToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Restore all reminder from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreReminder(boolean isCloud){
        try {
            new SyncHelper(mContext).reminderFromJson(null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadReminder();
            try {
                new GDriveHelper(mContext).downloadReminder();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for notes.
     * @param isCloud create cloud backup.
     */
    public void backupNote(boolean isCloud){
        try {
            new SyncHelper(mContext).noteToJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadNote();
            try {
                new GDriveHelper(mContext).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Restore all notes from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreNote(boolean isCloud){
        try {
            new SyncHelper(mContext).noteFromJson(null, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadNote();
            try {
                new GDriveHelper(mContext).downloadNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for birthdays.
     * @param isCloud create cloud backup.
     */
    public void backupBirthday(boolean isCloud){
        try {
            new SyncHelper(mContext).birthdayToJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadBirthday();
            try {
                new GDriveHelper(mContext).saveBirthToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreBirthday(boolean isCloud, boolean deleteFile){
        try {
            new SyncHelper(mContext).birthdayFromJson(null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadBirthday(deleteFile);
            try {
                new GDriveHelper(mContext).downloadBirthday(deleteFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
