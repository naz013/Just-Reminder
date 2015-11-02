package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import org.json.JSONException;

import java.io.IOException;

public class SyncNotes extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private SyncListener mListener;

    public SyncNotes(Context context, SyncListener mListener){
        this.mContext = context;
        builder = new NotificationCompat.Builder(context);
        this.mListener = mListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder.setContentTitle(mContext.getString(R.string.sync_start_message));
        builder.setContentText(mContext.getString(R.string.loading_wait));
        builder.setSmallIcon(R.drawable.ic_cached_white_24dp);
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify(2, builder.build());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        SyncHelper sHelp = new SyncHelper(mContext);
        try {
            sHelp.noteToJson();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        try {
            sHelp.noteFromJson(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        boolean isConnected = SyncHelper.isConnected(mContext);
        if (isConnected) {
            new DropboxHelper(mContext).uploadNote();
            try {
                new GDriveHelper(mContext).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new DropboxHelper(mContext).downloadNote();
            try {
                new GDriveHelper(mContext).downloadNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        builder.setContentTitle(mContext.getString(R.string.sync_end_message));
        builder.setSmallIcon(R.drawable.ic_done_white_24dp);
        if (Module.isPro()){
            builder.setContentText(mContext.getString(R.string.app_name_pro));
        } else builder.setContentText(mContext.getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
        mListener.endExecution(aVoid);
        new UpdatesHelper(mContext).updateNotesWidget();
    }
}