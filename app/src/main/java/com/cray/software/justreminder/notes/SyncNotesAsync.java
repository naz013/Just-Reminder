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

package com.cray.software.justreminder.notes;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;

import org.json.JSONException;

import java.io.IOException;

public class SyncNotesAsync extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private SyncListener mListener;

    public SyncNotesAsync(Context context, SyncListener mListener){
        this.mContext = context;
        builder = new NotificationCompat.Builder(context);
        this.mListener = mListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder.setContentTitle(mContext.getString(R.string.notes));
        builder.setContentText(mContext.getString(R.string.syncing_notes));
        builder.setSmallIcon(R.drawable.ic_cached_white_24dp);
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify(2, builder.build());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SyncHelper sHelp = new SyncHelper(mContext);
        try {
            sHelp.noteFromJson(null, null);
            sHelp.noteToJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean isConnected = SyncHelper.isConnected(mContext);
        if (isConnected) {
            new DropboxHelper(mContext).downloadNote();
            new DropboxHelper(mContext).uploadNote();
            try {
                new GDriveHelper(mContext).downloadNote(false);
                new GDriveHelper(mContext).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        builder.setContentTitle(mContext.getString(R.string.done));
        builder.setSmallIcon(R.drawable.ic_done_white_24dp);
        if (Module.isPro()){
            builder.setContentText(mContext.getString(R.string.app_name_pro));
        } else builder.setContentText(mContext.getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
        mListener.endExecution(aVoid);
        UpdatesHelper.getInstance(mContext).updateNotesWidget();
    }
}