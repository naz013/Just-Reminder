package com.cray.software.justreminder.cloud;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.SyncHelper;

/**
 * Asynchronously get Dropbox user name
 */
public class AccountInfo extends AsyncTask<Void, Void, String> {

    /**
     * Dropbox helper field.
     */
    private DropboxHelper dbx;

    /**
     * Context field.
     */
    private Context mContext;

    /**
     * Constructor.
     * @param context application context.
     */
    public AccountInfo(final Context context){
        this.mContext = context;
        this.dbx = new DropboxHelper(mContext);
    }

    @Override
    protected String doInBackground(Void... voids) {
        String name = null;
        dbx.startSession();
        if (dbx.isLinked()){
            if (SyncHelper.isConnected(mContext)){
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
