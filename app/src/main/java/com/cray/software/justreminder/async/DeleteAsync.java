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
import com.cray.software.justreminder.fragments.BackupsFragment;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SyncListener;

import java.io.File;

public class DeleteAsync extends AsyncTask<String, Void, Integer> {

    private Context mContext;
    private ProgressDialog progressDialog;
    private NavigationCallbacks mCallbacks;
    private SyncListener listener;

    private int type;

    public DeleteAsync(Context context, NavigationCallbacks mCallbacks,
                       SyncListener listener, int type){
        this.mContext = context;
        this.mCallbacks = mCallbacks;
        this.listener = listener;
        this.type = type;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog =
                ProgressDialog.show(mContext, null, mContext.getString(R.string.deleting), false);
    }

    @Override
    protected Integer doInBackground(String... params) {
        int res = 0;
        if (type == BackupsFragment.DROPBOX_INT) {
            DropboxHelper dbx = new DropboxHelper(mContext);
            dbx.startSession();
            boolean isLinked = dbx.isLinked();
            boolean isConnected = SyncHelper.isConnected(mContext);
            for (String filePath : params) {
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            File[] files = file.listFiles();
                            if (files != null) {
                                for (File f : files) {
                                    if (isLinked && isConnected) {
                                        dbx.deleteFile(f.getName());
                                    }
                                    f.delete();
                                }
                            }
                            res = 2;
                        } else {
                            if (isLinked && isConnected)
                                dbx.deleteFile(file.getName());
                            if (file.delete()) res = 1;
                        }
                    }
                }
            }
        } else if (type == BackupsFragment.GOOGLE_DRIVE_INT) {
            GDriveHelper gdx = new GDriveHelper(mContext);
            boolean isLinked = gdx.isLinked();
            boolean isConnected = SyncHelper.isConnected(mContext);
            for (String filePath : params) {
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            File[] files = file.listFiles();
                            if (files != null) {
                                for (File f : files) {
                                    if (isLinked && isConnected)
                                        gdx.deleteFile(f.getName());
                                    f.delete();
                                }
                            }
                            res = 2;
                        } else {
                            if (isLinked && isConnected)
                                gdx.deleteFile(file.getName());
                            if (file.delete()) res = 1;
                        }
                    }
                }
            }
        } else {
            for (String filePath : params) {
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            File[] files = file.listFiles();
                            if (files != null) {
                                for (File f : files) f.delete();
                            }
                            res = 2;
                        } else {
                            if (file.delete()) res = 1;
                        }
                    }
                }
            }
        }
        return res;
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (aVoid == 1) {
            mCallbacks.showSnackbar(R.string.deleted);
        } else if (aVoid == 2) {
            if (mCallbacks != null) {
                mCallbacks.showSnackbar(R.string.all_files_removed);
            }
        }

        if (listener != null && mContext != null) listener.endExecution(true);
    }
}
