package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.SyncHelper;

/**
 * Asynchronously get Dropbox user name
 */
public class AccountInfo extends AsyncTask<Void, Void, String> {

    DropboxHelper dbx;
    Context ctx;

    public AccountInfo(Context context){
        this.ctx = context;
        this.dbx = new DropboxHelper(ctx);
    }

    @Override
    protected String doInBackground(Void... voids) {
        String name = null;
        dbx.startSession();
        if (dbx.isLinked()){
            if (SyncHelper.isConnected(ctx)){
                name = dbx.userName();
            }
        }
        return name;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}