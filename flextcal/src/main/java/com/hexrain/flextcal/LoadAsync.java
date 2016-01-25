package com.hexrain.flextcal;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Copyright 2015 Nazar Suhovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class LoadAsync extends AsyncTask<Void, Void, Void> {

    private Context context;
    private int month;
    private ImageCheck imageCheck;

    public LoadAsync(Context context, int month){
        this.context = context;
        this.month = month;
        imageCheck = new ImageCheck();
    }

    @Override
    protected Void doInBackground(Void... params) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!imageCheck.isImage(month) && mWifi.isConnected()){
            try {
                Bitmap bitmap = Picasso.with(context)
                        .load(imageCheck.getImageUrl(month))
                        .resize(1920, 1080)
                        .get();
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + "image_cache");
                if (!sdPathDr.exists()) {
                    sdPathDr.mkdirs();
                }

                File image = new File(sdPathDr, imageCheck.getImageName(month));
                try {
                    if (image.createNewFile()) {
                        FileOutputStream ostream = new FileOutputStream(image);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                        ostream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
