package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.SyncHelper;

/**
 * Asynchronously get Dropbox used, shared and all space.
 */
public class DropboxQuota extends AsyncTask<Void, Void, Long[]> {

    private DropboxHelper dbx;
    private Context mContext;

    public DropboxQuota(Context context){
        this.mContext = context;
        this.dbx = new DropboxHelper(mContext);
    }

    @Override
    protected Long[] doInBackground(Void... voids) {
        Long[] res = new Long[0];
        dbx.startSession();
        if (dbx.isLinked()){
            if (SyncHelper.isConnected(mContext)){
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