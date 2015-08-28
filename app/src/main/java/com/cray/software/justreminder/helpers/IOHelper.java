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

public class IOHelper {

    Context context;
    boolean isConnected;

    public IOHelper (Context context){
        this.context = context;
        isConnected = SyncHelper.isConnected(context);
    }

    public void deleteReminder(String name){
        SyncHelper syncHelper = new SyncHelper(context);
        if (syncHelper.isSdPresent()) {
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
            new DropboxHelper(context).deleteFile(name);
            new GDriveHelper(context).deleteFile(name);
            //new BoxHelper(context).deleteFile(name);
        }
    }

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

    public void backupGroup(boolean isCloud){
        try {
            new SyncHelper(context).exportGroups();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadGroupToCloud();
            try {
                new GDriveHelper(context).saveGroupToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadGroup();
        }
    }

    public void restoreGroup(boolean isCloud){
        SyncHelper sHelp = new SyncHelper(context);
        if (sHelp.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_GROUP_SD);
            if (sdPathDr.exists()) {
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
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadGroupFromCloud();
            try {
                new GDriveHelper(context).loadGroupsFromDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadGroup();
        }
    }

    public void backupReminder(boolean isCloud){
        try {
            new SyncHelper(context).exportReminderToJSON();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadToCloud(null);
            try {
                new GDriveHelper(context).saveFileToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadReminder();
        }
    }

    public void restoreReminder(boolean isCloud){
        SyncHelper sHelp = new SyncHelper(context);
        if (sHelp.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/" + Constants.DIR_SD);
            if (sdPathDr.exists()) {
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
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadFromCloud();
            try {
                new GDriveHelper(context).loadFileFromDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadReminder();
        }
    }

    public void backupNote(boolean isCloud){
        try {
            new SyncHelper(context).exportNotes();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadNoteToCloud();
            try {
                new GDriveHelper(context).saveNoteToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadNote();
        }
    }

    public void restoreNote(boolean isCloud){
        try {
            new SyncHelper(context).importNotes(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadNoteFromCloud();
            try {
                new GDriveHelper(context).loadNoteFromDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadNote();
        }
    }

    public void backupBirthday(boolean isCloud){
        try {
            new SyncHelper(context).exportBirthdays();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).uploadBirthToCloud();
            try {
                new GDriveHelper(context).saveBirthToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new BoxHelper(context).uploadBirthday();
        }
    }

    public void restoreBirthday(boolean isCloud){
        try {
            new SyncHelper(context).importBirthday(null, null);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new DropboxHelper(context).downloadBirthFromCloud();
            try {
                new GDriveHelper(context).loadBirthFromDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //new BoxHelper(context).downloadBirthday();
        }
    }
}
