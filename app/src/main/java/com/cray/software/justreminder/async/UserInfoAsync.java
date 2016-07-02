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
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.datas.models.UserModel;
import com.cray.software.justreminder.fragments.BackupsFragment;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.DataListener;
import com.cray.software.justreminder.utils.MemoryUtil;

import java.io.File;

/**
 * Asynchronously get Dropbox used, shared and all space.
 */
public class UserInfoAsync extends AsyncTask<Void, Void, UserModel> {

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
    protected UserModel doInBackground(Void... voids) {
        if (type == BackupsFragment.DROPBOX_INT) {
            DropboxHelper dbx = new DropboxHelper(mContext);
            dbx.startSession();
            if (dbx.isLinked()) {
                if (SyncHelper.isConnected(mContext)) {
                    long quota = dbx.userQuota();
                    long quotaUsed = dbx.userQuotaNormal();
                    String name = dbx.userName();
                    long count = dbx.countFiles();
                    return new UserModel(name, quota, quotaUsed, count, null);
                }
            }
        } else if (type == BackupsFragment.GOOGLE_DRIVE_INT) {
            GDriveHelper gdx = new GDriveHelper(mContext);
            if (gdx.isLinked()) {
                if (SyncHelper.isConnected(mContext)) {
                    return gdx.getData();
                }
            }
        } else {
            return new UserModel(null, 0, 0, getCountFiles(), null);
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
    protected void onPostExecute(UserModel userModel) {
        super.onPostExecute(userModel);
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        if (listener != null && mContext != null)
            listener.onReceive(userModel);
    }
}