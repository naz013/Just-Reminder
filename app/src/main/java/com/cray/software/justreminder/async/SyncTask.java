package com.cray.software.justreminder.async;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import org.json.JSONException;

import java.io.IOException;

public class SyncTask extends AsyncTask<Void, String, Boolean> {

    Context tContext;
    NotificationManagerCompat mNotifyMgr;
    NotificationCompat.Builder builder;
    private SyncListener mListener;

    public SyncTask(Context context, SyncListener mListener){
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
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        builder.setContentTitle(values[0]);
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DataBase DB = new DataBase(tContext);
        DB.open();
        IOHelper ioHelper = new IOHelper(tContext);

        ioHelper.backupGroup(true);
        ioHelper.restoreGroup(true);

        Cursor cat = DB.queryCategories();
        if (cat == null || cat.getCount() == 0) {
            long time = System.currentTimeMillis();
            String defUiID = SyncHelper.generateID();
            DB.addCategory("General", time, defUiID, 5);
            DB.addCategory("Work", time, SyncHelper.generateID(), 3);
            DB.addCategory("Personal", time, SyncHelper.generateID(), 0);
            Cursor c = DB.queryGroup();
            if (c != null && c.moveToFirst()) {
                do {
                    DB.setGroup(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)), defUiID);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }

        //export & import reminders
        publishProgress(tContext.getString(R.string.message_sync_reminders));

        ioHelper.backupReminder(true);
        ioHelper.restoreReminder(true);

        //export & import notes
        SharedPrefs prefs = new SharedPrefs(tContext);
        if (prefs.loadBoolean(Prefs.SYNC_NOTES)) {
            publishProgress(tContext.getString(R.string.message_sync_notes));
            ioHelper.backupNote(true);
            ioHelper.restoreNote(true);
        }

        //export & import birthdays
        if (prefs.loadBoolean(Prefs.SYNC_BIRTHDAYS)) {
            publishProgress(tContext.getString(R.string.message_sync_birthdays));
            ioHelper.backupBirthday(true);
            ioHelper.restoreBirthday(true);
        }

        DB.close();

        try {
            new SyncHelper(tContext).scanFoldersForJSON();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        builder.setContentTitle(tContext.getString(R.string.sync_end_message));
        builder.setSmallIcon(R.drawable.ic_done_white_24dp);
        if (Module.isPro()) {
            builder.setContentText(tContext.getString(R.string.app_name_pro));
        } else builder.setContentText(tContext.getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify(2, builder.build());
        if (mListener != null) {
            mListener.endExecution(aVoid);
        }
        new UpdatesHelper(tContext).updateWidget();
        new UpdatesHelper(tContext).updateNotesWidget();
    }
}