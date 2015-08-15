package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;

public class DeleteReminderFiles extends AsyncTask<Void, Void, Void> {

    Context mContext;
    String uuId;

    public DeleteReminderFiles(Context context, String uuId){
        this.mContext = context;
        this.uuId = uuId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        new IOHelper(mContext).deleteReminder(uuId);
        return null;
    }
}
