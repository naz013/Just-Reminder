package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.os.Environment;

import com.cray.software.justreminder.cloud.BoxHelper;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Backup files save, sync, delete helper class.
 */
public class IOHelper {

    private Context context;
    private boolean isConnected;

    public IOHelper (Context context){
        this.context = context;
        isConnected = SyncHelper.isConnected(context);
    }

    /**
     * Delete all local and cloud file copies.
     * @param name file name.
     */
    public void deleteReminder(String name){
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
            String exportFileName = name + Constants.FILE_NAME_REMINDER;
            File file = new File(sdPathDr, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_DBX_TMP);
            file = new File(sdPathDr, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
            file = new File(sdPathDr, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_BOX_TMP);
            file = new File(sdPathDr, exportFileName);
            if (file.exists()) {
                file.delete();
            }
        }
        if (isConnected){
            new DropboxHelper(context).deleteReminder(name);
            new GDriveHelper(context).deleteReminder(name);
            //new BoxHelper(context).deleteReminder(name);
        }
    }

    /**
     * Create backup files for reminders, groups, birthdays and notes.
     */
    public void backup(){
        backupGroup(true);
        backupReminder(true);

        SharedPrefs prefs = new SharedPrefs(context);
        if (prefs.loadBoolean(Prefs.SYNC_NOTES)){
            backupNote(true);
        }
        if (prefs.loadBoolean(Prefs.SYNC_BIRTHDAYS)){
            backupBirthday(true);
        }
    }

    /**
     * Create backup files for groups.
     * @param isCloud create cloud backup.
     */
    public void backupGroup(boolean isCloud){
        try {
            new SyncHelper(context).exportGroups();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadGroup();
            try {
                new GDriveHelper(context).saveGroupToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadGroup();
        }
    }

    /**
     * Restore all groups from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreGroup(boolean isCloud){
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_GROUP_SD);
            if (sdPathDr.exists()) {
                File[] files = sdPathDr.listFiles();
                final int x = files.length;
                if (x > 0) {
                    try {
                        new SyncHelper(context).importGroup(null, null);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadGroup();
            try {
                new GDriveHelper(context).downloadGroup();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadGroup();
        }
    }

    /**
     * Create backup files for reminder.
     * @param isCloud create cloud backup.
     */
    public void backupReminder(boolean isCloud){
        try {
            new SyncHelper(context).exportReminderToJSON();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadReminder(null);
            try {
                new GDriveHelper(context).saveReminderToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadReminder();
        }
    }

    /**
     * Restore all reminder from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreReminder(boolean isCloud){
        if (SyncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_SD);
            if (sdPathDr.exists()) {
                File[] files = sdPathDr.listFiles();
                final int x = files.length;
                if (x > 0) {
                    try {
                        new SyncHelper(context).importReminderFromJSON(null, null);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadReminder();
            try {
                new GDriveHelper(context).downloadReminder();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadReminder();
        }
    }

    /**
     * Create backup files for notes.
     * @param isCloud create cloud backup.
     */
    public void backupNote(boolean isCloud){
        try {
            new SyncHelper(context).exportNotes();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadNote();
            try {
                new GDriveHelper(context).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadNote();
        }
    }

    /**
     * Restore all notes from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreNote(boolean isCloud){
        try {
            new SyncHelper(context).importNotes(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadNote();
            try {
                new GDriveHelper(context).downloadNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadNote();
        }
    }

    /**
     * Create backup files for birthdays.
     * @param isCloud create cloud backup.
     */
    public void backupBirthday(boolean isCloud){
        try {
            new SyncHelper(context).exportBirthdays();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadBirthday();
            try {
                new GDriveHelper(context).saveBirthToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadBirthday();
        }
    }

    /**
     * Restore all birthdays from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreBirthday(boolean isCloud){
        try {
            new SyncHelper(context).importBirthday(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadBirthday();
            try {
                new GDriveHelper(context).downloadBirthday();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadBirthday();
        }
    }
}
