package com.cray.software.justreminder.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Copyright 2015 Nazar Suhovich
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
public class LoadSounds extends AsyncTask<Void, Void, ArrayList<File>> {
    private ProgressDialog pd;
    private Context mContext;
    private String prefs;
    private ArrayList<String> names, foldersFile;
    private DialogInterface.OnDismissListener listener;

    public LoadSounds(Context context, String prefs, DialogInterface.OnDismissListener listener) {
        this.mContext = context;
        this.prefs = prefs;
        this.listener = listener;
        pd = new ProgressDialog(context);
        pd.setCancelable(true);
        pd.setMessage(context.getString(R.string.sounds_loading_text));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd.show();
    }

    @Override
    protected ArrayList<File> doInBackground(Void... params) {
        ArrayList<File> fileList = new ArrayList<>();
        File dir;
        if (SyncHelper.isSdPresent()) {
            dir = new File(SyncHelper.getSdCardPath());
            listf(dir.toString(), fileList);
        } else {
            dir = new File(Environment.getDataDirectory().toString());
            listf(dir.toString(), fileList);
        }
        Collections.sort(fileList);
        names = new ArrayList<>();
        foldersFile = new ArrayList<>();
        names.clear();
        foldersFile.clear();
        for (File aFile : fileList) {
            names.add(aFile.getName());
            foldersFile.add(aFile.toString());
        }
        return fileList;
    }

    @Override
    protected void onPostExecute(ArrayList<File> files) {
        try {
            if ((pd != null) && pd.isShowing()) {
                pd.dismiss();
            }
        } catch (final Exception e) {
            // Handle or log or ignore
        }
        if (files != null) {
            Dialogues.customMelody(mContext, prefs, names, foldersFile, listener);
        } else {
            Messages.toast(mContext, R.string.no_music);
        }
    }

    private void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.canRead()) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        if (fileName.endsWith(".mp3") 
                                || fileName.endsWith(".ogg")
                                || fileName.endsWith(".m4a")
                                || fileName.endsWith(".flac")) {
                            files.add(file);
                        }
                    } else if (file.isDirectory()) {
                        listf(file.toString(), files);
                    }
                } else {
                    Log.d(Constants.LOG_TAG, "secure file");
                }
            }
        } else Log.i(Constants.LOG_TAG, "No files");
    }
}
