package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.SyncHelper;

import org.json.JSONException;

import java.io.IOException;

public class ScanTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;

    public ScanTask(Context context){
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SyncHelper sHelp = new SyncHelper(mContext);
        try {
            sHelp.findJson();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}