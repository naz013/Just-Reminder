package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import org.json.JSONException;

import java.io.IOException;

public class BackupTask extends AsyncTask<Void, Void, Boolean> {

    Context tContext;
    DataBase DB;

    public BackupTask(Context context){
        this.tContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DB = new DataBase(tContext);
        SyncHelper sHelp = new SyncHelper(tContext);
        DropboxHelper dbx = new DropboxHelper(tContext);
        GDriveHelper gdx = new GDriveHelper(tContext);
        if (DB.getCount() > 0) {
            try {
                sHelp.exportReminderToJSON();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        DB.close();
        boolean isConnected = SyncHelper.isConnected(tContext);
        if (isConnected) {
            dbx.uploadToCloud(null);
            try {
                gdx.saveFileToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SharedPrefs prefs = new SharedPrefs(tContext);
        if (prefs.loadBoolean(Constants.APP_UI_PREFERENCES_SYNC_NOTES)){
            try {
                sHelp.exportNotes();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            if (isConnected) {
                dbx.uploadNoteToCloud();
                try {
                    gdx.saveNoteToDrive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}