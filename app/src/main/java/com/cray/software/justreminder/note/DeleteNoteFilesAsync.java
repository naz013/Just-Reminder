package com.cray.software.justreminder.note;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;

public class DeleteNoteFilesAsync extends AsyncTask<String, Void, Boolean> {

    private Context mContext;

    public DeleteNoteFilesAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params.length > 0) {
            String uuID = params[0];
            if (SyncHelper.isSdPresent()) {
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
                sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_BOX_TMP);
                file = new File(sdPathDr, exportFileName);
                if (file.exists()) {
                    file.delete();
                }

                boolean isConnected = SyncHelper.isConnected(mContext);
                if (isConnected) {
                    new DropboxHelper(mContext).deleteNote(uuID);
                    new GDriveHelper(mContext).deleteNote(uuID);
                    //new BoxHelper(mContext).deleteNoteFile(uuID);
                }
            }
        }
        return null;
    }
}