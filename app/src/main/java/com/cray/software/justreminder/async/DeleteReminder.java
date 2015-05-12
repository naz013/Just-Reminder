package com.cray.software.justreminder.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;

/**
 * Created by Nazar on 08.05.2015.
 */
public class DeleteReminder extends AsyncTask<Void, Void, Void> {

    Context mContext;
    String uuId;

    public DeleteReminder(Context context, String uuId){
        this.mContext = context;
        this.uuId = uuId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SyncHelper syncHelper = new SyncHelper(mContext);
        if (syncHelper.isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
            String exportFileName = uuId + Constants.FILE_NAME;
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
        }
        boolean isInternet = SyncHelper.isConnected(mContext);
        DropboxHelper dbx = new DropboxHelper(mContext);
        GDriveHelper gdx = new GDriveHelper(mContext);
        if (dbx.isLinked() && isInternet) dbx.deleteFile(uuId);
        if (gdx.isLinked() && isInternet) gdx.deleteFile(uuId);
        return null;
    }
}
