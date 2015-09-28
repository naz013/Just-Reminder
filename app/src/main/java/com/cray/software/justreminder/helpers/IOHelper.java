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

    private Context mContext;
    private boolean isConnected;

    public IOHelper (Context context){
        this.mContext = context;
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
            new DropboxHelper(mContext).deleteReminder(name);
            new GDriveHelper(mContext).deleteReminder(name);
            //new BoxHelper(mContext).deleteReminder(name);
        }
    }

    /**
     * Create backup files for reminders, groups, birthdays and notes.
     */
    public void backup(){
        backupGroup(true);
        backupReminder(true);

        SharedPrefs prefs = new SharedPrefs(mContext);
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
            new SyncHelper(mContext).groupToJson();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadGroup();
            try {
                new GDriveHelper(mContext).saveGroupToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(mContext).uploadGroup();
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
                        new SyncHelper(mContext).groupFromJson(null, null);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadGroup();
            try {
                new GDriveHelper(mContext).downloadGroup();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(mContext).downloadGroup();
        }
    }

    /**
     * Create backup files for reminder.
     * @param isCloud create cloud backup.
     */
    public void backupReminder(boolean isCloud){
        try {
            new SyncHelper(mContext).reminderToJson();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadReminder(null);
            try {
                new GDriveHelper(mContext).saveReminderToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(mContext).uploadReminder();
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
                        new SyncHelper(mContext).reminderFromJson(null, null);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadReminder();
            try {
                new GDriveHelper(mContext).downloadReminder();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(mContext).downloadReminder();
        }
    }

    /**
     * Create backup files for notes.
     * @param isCloud create cloud backup.
     */
    public void backupNote(boolean isCloud){
        try {
            new SyncHelper(mContext).noteToJson();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadNote();
            try {
                new GDriveHelper(mContext).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(mContext).uploadNote();
        }
    }

    /**
     * Restore all notes from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreNote(boolean isCloud){
        try {
            new SyncHelper(mContext).noteFromJson(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadNote();
            try {
                new GDriveHelper(mContext).downloadNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(mContext).downloadNote();
        }
    }

    /**
     * Create backup files for birthdays.
     * @param isCloud create cloud backup.
     */
    public void backupBirthday(boolean isCloud){
        try {
            new SyncHelper(mContext).birthdayToJson();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).uploadBirthday();
            try {
                new GDriveHelper(mContext).saveBirthToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(mContext).uploadBirthday();
        }
    }

    /**
     * Restore all birthdays from backup files.
     * @param isCloud restore from cloud.
     */
    public void restoreBirthday(boolean isCloud){
        try {
            new SyncHelper(mContext).birthdayFromJson(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(mContext).downloadBirthday();
            try {
                new GDriveHelper(mContext).downloadBirthday();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(mContext).downloadBirthday();
        }
    }
}
