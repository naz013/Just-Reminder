package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.IOHelper;

public class BackupTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;

    public BackupTask(Context context){
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        new IOHelper(mContext).backup();
        return true;
    }
}