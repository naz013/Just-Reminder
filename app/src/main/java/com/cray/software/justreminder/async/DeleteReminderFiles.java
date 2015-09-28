package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.helpers.IOHelper;

public class DeleteReminderFiles extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private String uuId;

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
