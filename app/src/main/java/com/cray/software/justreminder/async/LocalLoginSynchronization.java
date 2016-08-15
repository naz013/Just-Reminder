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

package com.cray.software.justreminder.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.LoginListener;
import com.cray.software.justreminder.reminder.ReminderHelper;
import com.cray.software.justreminder.reminder.ReminderItem;

import java.util.List;

public class LocalLoginSynchronization extends AsyncTask<Void, String, Void> {

    private Context mContext;
    private boolean isChecked = false;
    private LoginListener listener;
    private ProgressDialog dialog;

    public LocalLoginSynchronization(Context context, boolean isChecked, LoginListener listener){
        this.mContext = context;
        this.isChecked = isChecked;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialog.show(mContext, mContext.getString(R.string.please_wait),
                mContext.getString(R.string.local_sync), true, false);
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        super.onProgressUpdate(values);
        new android.os.Handler().post(() -> dialog.setMessage(values[0]));
    }

    @Override
    protected Void doInBackground(Void... params) {
        IOHelper ioHelper = new IOHelper(mContext);
        publishProgress(mContext.getString(R.string.syncing_groups));
        ioHelper.restoreGroup(false, false);
        checkGroups();
        //import reminders
        publishProgress(mContext.getString(R.string.syncing_reminders));
        ioHelper.restoreReminder(false, false);
        //import notes
        publishProgress(mContext.getString(R.string.syncing_notes));
        ioHelper.restoreNote(false, false);
        //import birthdays
        if (isChecked) {
            publishProgress(mContext.getString(R.string.syncing_birthdays));
            ioHelper.restoreBirthday(false, false);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        if (listener != null) listener.onLocal();
    }

    private void checkGroups() {
        GroupHelper helper = GroupHelper.getInstance(mContext);
        List<GroupItem> list = helper.getAll();
        if (list.size() == 0) {
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            helper.saveGroup(new GroupItem("General", defUiID, 5, 0, time));
            helper.saveGroup(new GroupItem("Work", SyncHelper.generateID(), 3, 0, time));
            helper.saveGroup(new GroupItem("Personal", SyncHelper.generateID(), 0, 0, time));
            List<ReminderItem> items = ReminderHelper.getInstance(mContext).getAll();
            for (ReminderItem item : items) {
                item.setGroupId(defUiID);
            }
            ReminderHelper.getInstance(mContext).saveReminders(items);
        }
    }
}
