package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.SyncHelper;

import org.json.JSONException;

import java.io.IOException;

public class ScanTask extends AsyncTask<Void, Void, Boolean> {

    Context tContext;

    public ScanTask(Context context){
        this.tContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SyncHelper sHelp = new SyncHelper(tContext);
        try {
            sHelp.scanFoldersForJSON();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}