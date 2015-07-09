package com.cray.software.justreminder.note;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;

public class DeleteNoteFile extends AsyncTask<String, Void, Boolean> {

    Context ctx;

    public DeleteNoteFile(Context context){
        this.ctx = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params.length > 0) {
            String uuID = params[0];
            SyncHelper syncHelper = new SyncHelper(ctx);
            if (syncHelper.isSdPresent()) {
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
                String exportFileName = uuID + Constants.FILE_NAME_NOTE;
                File file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }
                sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_DBX_TMP);
                file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }
                sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_GDRIVE_TMP);
                file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }

                boolean isConnected = SyncHelper.isConnected(ctx);
                DropboxHelper dbx = new DropboxHelper(ctx);
                if (isConnected) {
                    dbx.deleteNote(uuID);
                }
                GDriveHelper gdx = new GDriveHelper(ctx);
                if (isConnected) {
                    gdx.deleteNote(uuID);
                }
            }
        }
        return null;
    }
}
