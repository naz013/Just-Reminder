/*
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

package com.cray.software.justreminder.groups;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.Dropbox;
import com.cray.software.justreminder.cloud.GoogleDrive;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.utils.MemoryUtil;

import java.io.File;
import java.util.List;

public class DeleteGroupAsync extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private String uuId;

    public DeleteGroupAsync(Context context, String uuID){
        this.mContext = context;
        this.uuId = uuID;
    }

    @Override
    protected Void doInBackground(Void... params) {
        File dir = MemoryUtil.getGroupsDir();
        String exportFileName = uuId + FileConfig.FILE_NAME_GROUP;
        File file = new File(dir, exportFileName);
        if (file.exists()) {
            file.delete();
        }
        dir = MemoryUtil.getDGroupsDir();
        file = new File(dir, exportFileName);
        if (file.exists()) {
            file.delete();
        }
        dir = MemoryUtil.getGGroupsDir();
        file = new File(dir, exportFileName);
        if (file.exists()) {
            file.delete();
        }
        boolean isInternet = SyncHelper.isConnected(mContext);
        Dropbox dbx = new Dropbox(mContext);
        GoogleDrive gdx = new GoogleDrive(mContext);
        if (dbx.isLinked() && isInternet) {
            dbx.deleteGroup(uuId);
        }
        if (gdx.isLinked() && isInternet) {
            gdx.deleteGroupFileByName(uuId);
        }
        List<ReminderItem> list = ReminderHelper.getInstance(mContext).getReminders(uuId);
        for (ReminderItem item : list) {
            dir = MemoryUtil.getRDir();
            exportFileName = item.getUuId() + FileConfig.FILE_NAME_REMINDER;
            file = new File(dir, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            dir = MemoryUtil.getDRDir();
            file = new File(dir, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            dir = MemoryUtil.getGRDir();
            file = new File(dir, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            if (dbx.isLinked() && isInternet) {
                dbx.deleteReminder(item.getUuId());
            }
            if (gdx.isLinked() && isInternet) {
                gdx.deleteReminderFileByName(uuId);
            }
        }
        ReminderHelper.getInstance(mContext).deleteReminders(list);
        return null;
    }
}
