package com.cray.software.justreminder.note;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.BoxHelper;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import org.json.JSONException;

import java.io.IOException;

public class SyncNotes extends AsyncTask<Void, Void, Boolean> {

    Context tContext;
    NotificationManagerCompat mNotifyMgr;
    NotificationCompat.Builder builder;
    private SyncListener mListener;

    public SyncNotes(Context context, SyncListener mListener){
        this.tContext = context;
        builder = new NotificationCompat.Builder(context);
        this.mListener = mListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder.setContentTitle(tContext.getString(R.string.sync_start_message));
        builder.setContentText(tContext.getString(R.string.loading_wait));
        builder.setSmallIcon(R.drawable.ic_cached_white_24dp);
        mNotifyMgr = NotificationManagerCompat.from(tContext);
        mNotifyMgr.notify(2, builder.build());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SyncHelper sHelp = new SyncHelper(tContext);
        try {
            sHelp.exportNotes();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        try {
            sHelp.importNotes(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        boolean isConnected = SyncHelper.isConnected(tContext);
        if (isConnected) {
            new DropboxHelper(tContext).uploadNote();
            try {
                new GDriveHelper(tContext).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(tContext).uploadNote();
            new DropboxHelper(tContext).downloadNote();
            try {
                new GDriveHelper(tContext).downloadNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(tContext).downloadNote();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        builder.setContentTitle(tContext.getString(R.string.sync_end_message));
        builder.setSmallIcon(R.drawable.ic_done_white_24dp);
        if (Module.isPro()){
            builder.setContentText(tContext.getString(R.string.app_name_pro));
        } else builder.setContentText(tContext.getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
        mListener.endExecution(aVoid);
        new UpdatesHelper(tContext).updateNotesWidget();
    }
}