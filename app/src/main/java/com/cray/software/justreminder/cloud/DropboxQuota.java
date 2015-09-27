package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.SyncHelper;

/**
 * Asynchronously get Dropbox used, shared and all space.
 */
public class DropboxQuota extends AsyncTask<Void, Void, Long[]> {

    DropboxHelper dbx;
    Context ctx;

    public DropboxQuota(Context context){
        this.ctx = context;
        this.dbx = new DropboxHelper(ctx);
    }

    @Override
    protected Long[] doInBackground(Void... voids) {
        Long[] res = new Long[0];
        dbx.startSession();
        if (dbx.isLinked()){
            if (SyncHelper.isConnected(ctx)){
                long quota = dbx.userQuota();
                long quotaNormal = dbx.userQuotaNormal();
                long quotaShared = dbx.userQuotaShared();
                res = new Long[]{quota, quotaNormal, quotaShared};
            }
        }
        return res;
    }

    @Override
    protected void onPostExecute(Long[] s) {
        super.onPostExecute(s);
    }
}