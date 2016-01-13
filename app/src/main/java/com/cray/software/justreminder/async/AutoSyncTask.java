package com.cray.software.justreminder.async;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class AutoSyncTask extends AsyncTask<Void, String, Boolean> {

    private Context mContext;

    public AutoSyncTask(Context context){
        this.mContext = context;
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
                do {
                    db.setGroup(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)), defUiID);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }

        //export & import reminders

        ioHelper.restoreReminder(true);
        ioHelper.backupReminder(true);

        //export & import notes
        SharedPrefs prefs = new SharedPrefs(mContext);
        if (prefs.loadBoolean(Prefs.SYNC_NOTES)) {
            ioHelper.restoreNote(true);
            ioHelper.backupNote(true);
        }

        //export & import birthdays
        if (prefs.loadBoolean(Prefs.SYNC_BIRTHDAYS)) {
            ioHelper.restoreBirthday(true);
            ioHelper.backupBirthday(true);
        }

        db.close();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(mContext).updateWidget();
        new UpdatesHelper(mContext).updateNotesWidget();
    }
}