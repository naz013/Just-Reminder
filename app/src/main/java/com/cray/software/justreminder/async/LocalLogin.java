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
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.LoginListener;

public class LocalLogin extends AsyncTask<Void, String, Void> {

    private Context mContext;
    private boolean isChecked = false;
    private LoginListener listener;
    private ProgressDialog dialog;

    public LocalLogin(Context context, boolean isChecked, LoginListener listener){
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
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage(values[0]);
            }
        });
    }

    @Override
    protected Void doInBackground(Void... params) {
        IOHelper ioHelper = new IOHelper(mContext);

        publishProgress(mContext.getString(R.string.syncing_groups));
        ioHelper.restoreGroup(false);

        checkGroups();

        //import reminders
        publishProgress(mContext.getString(R.string.syncing_reminders));
        ioHelper.restoreReminder(false);

        //import notes
        publishProgress(mContext.getString(R.string.syncing_notes));
        ioHelper.restoreNote(false);

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
        DataBase DB = new DataBase(mContext);
        DB.open();
        Cursor cat = DB.queryCategories();
        if (cat == null || cat.getCount() == 0){
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            DB.addCategory("General", time, defUiID, 5);
            DB.addCategory("Work", time, SyncHelper.generateID(), 3);
            DB.addCategory("Personal", time, SyncHelper.generateID(), 0);

            NextBase db = new NextBase(mContext);
            db.open();
            Cursor c = db.getReminders();
            if (c != null && c.moveToFirst()){
                do {
                    db.setGroup(c.getLong(c.getColumnIndex(NextBase._ID)), defUiID);
                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }
            db.close();
        }
        if (cat != null) cat.close();
        DB.close();
    }
}
