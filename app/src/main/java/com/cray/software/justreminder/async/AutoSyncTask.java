package com.cray.software.justreminder.async;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.IOHelper;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

import org.json.JSONException;

import java.io.IOException;

public class AutoSyncTask extends AsyncTask<Void, String, Boolean> {

    private Context mContext;

    public AutoSyncTask(Context context){
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DataBase DB = new DataBase(mContext);
        DB.open();
        IOHelper ioHelper = new IOHelper(mContext);

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

        DB.close();

        try {
            new SyncHelper(mContext).findJson();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        new UpdatesHelper(mContext).updateWidget();
        new UpdatesHelper(mContext).updateNotesWidget();
    }
}