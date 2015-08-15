package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.IOHelper;

public class BackupTask extends AsyncTask<Void, Void, Boolean> {

    Context tContext;

    public BackupTask(Context context){
        this.tContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        new IOHelper(tContext).backup();
        return true;
    }
}