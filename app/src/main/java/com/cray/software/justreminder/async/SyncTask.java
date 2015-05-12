package com.cray.software.justreminder.async;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.widgets.UpdatesHelper;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class SyncTask extends AsyncTask<Void, Void, Boolean> {

    Context tContext;
    NotificationManager mNotifyMgr;
    Notification.Builder builder;
    private SyncListener mListener;

    public SyncTask(Context context, SyncListener mListener){
        this.tContext = context;
        builder = new Notification.Builder(context);
        this.mListener = mListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder.setContentTitle(tContext.getString(R.string.sync_start_message));
        builder.setContentText(tContext.getString(R.string.loading_wait));
        builder.setSmallIcon(R.drawable.ic_cached_white_24dp);
        mNotifyMgr =
                (NotificationManager) tContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(2, builder.build());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DataBase DB = new DataBase(tContext);
        DB.open();
        SyncHelper sHelp = new SyncHelper(tContext);
        boolean isConnected = SyncHelper.isConnected(tContext);
        DropboxHelper dbx = new DropboxHelper(tContext);
        GDriveHelper gdx = new GDriveHelper(tContext);
        try {
            sHelp.exportGroups();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (sHelp.isSdPresent()){
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_GROUP_SD);
            if (sdPathDr.exists()){
                File[] files = sdPathDr.listFiles();
                final int x = files.length;
                if (x > 0) {
                    try {
                        sHelp.importGroup(null, null);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (isConnected){
            dbx.uploadGroupToCloud();
            dbx.downloadGroupFromCloud();
            try {
                gdx.saveGroupToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                gdx.loadGroupsFromDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Cursor cat = DB.queryCategories();
        if (cat == null || cat.getCount() == 0){
            long time = System.currentTimeMillis();
            String defUiID = sHelp.generateID();
            DB.addCategory("General", time, defUiID, 5);
            DB.addCategory("Work", time, sHelp.generateID(), 3);
            DB.addCategory("Personal", time, sHelp.generateID(), 0);
            Cursor c = DB.queryGroup();
            if (c != null && c.moveToFirst()){
                do {
                    DB.setGroup(c.getLong(c.getColumnIndex(Constants.COLUMN_ID)), defUiID);
                } while (c.moveToNext());
            }
            if (c != null) c.close();
        }

        //export & import reminders
        if (DB.getCount() > 0) {
            try {
                sHelp.exportReminderToJSON();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            if (sHelp.isSdPresent()){
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_SD);
                if (sdPathDr.exists()){
                    File[] files = sdPathDr.listFiles();
                    final int x = files.length;
                    if (x > 0) {
                        try {
                            sHelp.importReminderFromJSON(null, null);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            if (sHelp.isSdPresent()){
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_SD);
                if (sdPathDr.exists()){
                    File[] files = sdPathDr.listFiles();
                    final int x = files.length;
                    if (x > 0) {
                        try {
                            sHelp.importReminderFromJSON(null, null);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (isConnected) {
            dbx.uploadToCloud(null);
            dbx.downloadFromCloud();

            try {
                gdx.saveFileToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                gdx.loadFileFromDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //export & import notes
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

            try {
                sHelp.importNotes(null, null);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            if (isConnected) {
                dbx.downloadNoteFromCloud();
                try {
                    gdx.loadNoteFromDrive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        DB.close();

        try {
            sHelp.scanFoldersForJSON();
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
        if (new ManageModule().isPro()){
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