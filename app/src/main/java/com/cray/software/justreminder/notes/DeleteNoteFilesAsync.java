package com.cray.software.justreminder.notes;

import android.content.Context;
import android.os.AsyncTask;

import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.utils.MemoryUtil;

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
            File dir = MemoryUtil.getNDir();
            String exportFileName = uuID + FileConfig.FILE_NAME_NOTE;
            File file = new File(dir, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            dir = MemoryUtil.getDNDir();
            file = new File(dir, exportFileName);
            if (file.exists()) {
                file.delete();
            }
            dir = MemoryUtil.getGNDir();
            file = new File(dir, exportFileName);
            if (file.exists()) {
                file.delete();
            }

            boolean isConnected = SyncHelper.isConnected(mContext);
            if (isConnected) {
                new DropboxHelper(mContext).deleteNote(uuID);
                new GDriveHelper(mContext).deleteNote(uuID);
            }
        }
        return null;
    }
}
