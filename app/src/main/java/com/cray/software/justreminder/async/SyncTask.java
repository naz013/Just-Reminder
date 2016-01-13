package com.cray.software.justreminder.async;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class SyncTask extends AsyncTask<Void, String, Boolean> {

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private SyncListener mListener;

    public SyncTask(Context context, SyncListener mListener){
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
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        builder.setContentTitle(values[0]);
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DataBase db = new DataBase(mContext);
        db.open();
        IOHelper ioHelper = new IOHelper(mContext);

        ioHelper.backupGroup(true);
        ioHelper.restoreGroup(true);

        Cursor cat = db.queryCategories();
        if (cat == null || cat.getCount() == 0) {
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            db.addCategory("General", time, defUiID, 5);
            db.addCategory("Work", time, SyncHelper.generateID(), 3);
            db.addCategory("Personal", time, SyncHelper.generateID(), 0);
            Cursor c = db.queryGroup();
            if (c != null && c.moveToFirst()) {
                db.setGroup(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)), defUiID);
            }
            if (c != null) c.close();
        }

        //export & import reminders
        publishProgress(mContext.getString(R.string.message_sync_reminders));

        ioHelper.restoreReminder(true);
        ioHelper.backupReminder(true);

        //export & import notes
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (prefs.loadBoolean(Prefs.SYNC_NOTES)) {
            publishProgress(mContext.getString(R.string.message_sync_notes));
            ioHelper.restoreNote(true);
            ioHelper.backupNote(true);
        }

        //export & import birthdays
        if (prefs.loadBoolean(Prefs.SYNC_BIRTHDAYS)) {
            publishProgress(mContext.getString(R.string.message_sync_birthdays));
            ioHelper.restoreBirthday(true);
            ioHelper.backupBirthday(true);
        }

        db.close();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        builder.setContentTitle(mContext.getString(R.string.sync_end_message));
        builder.setSmallIcon(R.drawable.ic_done_white_24dp);
        if (Module.isPro()) {
            builder.setContentText(mContext.getString(R.string.app_name_pro));
        } else builder.setContentText(mContext.getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
        if (mContext != null) {
            new UpdatesHelper(mContext).updateWidget();
            new UpdatesHelper(mContext).updateNotesWidget();
        }
        if (mListener != null && mContext != null) {
            mListener.endExecution(aVoid);
        }
    }
}