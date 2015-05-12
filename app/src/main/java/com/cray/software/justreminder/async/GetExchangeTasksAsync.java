package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.ExchangeHelper;
import com.cray.software.justreminder.interfaces.SyncListener;

public class GetExchangeTasksAsync extends AsyncTask<Void, Void, Void> {
    Context ctx;
    SyncListener mListener;

    public GetExchangeTasksAsync(Context context, SyncListener listener){
        this.ctx = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExchangeHelper helper = new ExchangeHelper(ctx);
        try {
            helper.getTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null) {
            mListener.endExecution(true);
        }
    }
}
