/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                new GDriveHelper(mContext).deleteNoteFileByName(uuID);
            }
        }
        return null;
    }
}
