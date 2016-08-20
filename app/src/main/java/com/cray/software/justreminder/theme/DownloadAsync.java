package com.cray.software.justreminder.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cray.software.justreminder.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DownloadAsync extends AsyncTask<String, Void, Void> {

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private String filePath;
    private String fileName;
    private int width;
    private int height;
    private long id;

    public DownloadAsync(Context context, String fileName, String filePath, int width, int height, long id) {
        this.mContext = context;
        this.fileName = fileName;
        this.filePath = filePath;
        this.width = width;
        this.height = height;
        this.id = id;
        builder = new NotificationCompat.Builder(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder.setContentTitle(fileName);
        builder.setContentText(mContext.getString(R.string.downloading_start));
        builder.setSmallIcon(R.drawable.ic_get_app_black_24dp);
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify((int) id, builder.build());
    }

    @Override
    protected Void doInBackground(String... strings) {
        File file = new File(filePath);
        try {
            Bitmap bitmap = Picasso.with(mContext).load(RetrofitBuilder.getImageLink(id, width, height)).get();
            try {
                if (file.createNewFile()) {
                    FileOutputStream stream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    stream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        builder.setContentText(mContext.getString(R.string.done));
        builder.setSmallIcon(R.drawable.ic_done_white_24dp);
        builder.setWhen(System.currentTimeMillis());
        mNotifyMgr.notify((int) id, builder.build());
    }
}
