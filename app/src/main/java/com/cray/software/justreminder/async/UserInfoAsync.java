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

package com.cray.software.justreminder.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.Dropbox;
import com.cray.software.justreminder.cloud.GoogleDrive;
import com.cray.software.justreminder.datas.models.UserItem;
import com.cray.software.justreminder.fragments.BackupsFragment;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.DataListener;
import com.cray.software.justreminder.utils.MemoryUtil;

import java.io.File;

/**
 * Asynchronously get Dropbox used, shared and all space.
 */
public class UserInfoAsync extends AsyncTask<Void, Void, UserItem> {

    private Context mContext;
    private ProgressDialog progressDialog;
    private int type;
    private DataListener listener;

    public UserInfoAsync(Context context, int type, DataListener listener){
        this.mContext = context;
        this.type = type;
        this.listener = listener;
        if (context != null) {
            progressDialog = ProgressDialog.show(context, null, context.getString(R.string.retrieving_data), false);
        }
    }

    @Override
    protected UserItem doInBackground(Void... voids) {
        if (type == BackupsFragment.DROPBOX_INT) {
            Dropbox dbx = new Dropbox(mContext);
            dbx.startSession();
            if (dbx.isLinked()) {
                if (SyncHelper.isConnected(mContext)) {
                    long quota = dbx.userQuota();
                    long quotaUsed = dbx.userQuotaNormal();
                    String name = dbx.userName();
                    long count = dbx.countFiles();
                    return new UserItem(name, quota, quotaUsed, count, null);
                }
            }
        } else if (type == BackupsFragment.GOOGLE_DRIVE_INT) {
            GoogleDrive gdx = new GoogleDrive(mContext);
            if (gdx.isLinked()) {
                if (SyncHelper.isConnected(mContext)) {
                    return gdx.getData();
                }
            }
        } else {
            return new UserItem(null, 0, 0, getCountFiles(), null);
        }
        return null;
    }

    private long getCountFiles() {
        int count = 0;
        File dir = MemoryUtil.getRDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getNDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getBDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        return count;
    }

    @Override
    protected void onPostExecute(UserItem userItem) {
        super.onPostExecute(userItem);
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        if (listener != null && mContext != null)
            listener.onReceive(userItem);
    }
}