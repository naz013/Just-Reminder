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
 * Created by nazar on 20.08.15.
 */
public class LoadAsync extends AsyncTask<Void, Void, Void> {

    private Context context;
    private int month;
    private ImageCheck imageCheck;

    public LoadAsync(Context context, int month){
        this.context = context;
        this.month = month;
        imageCheck = new ImageCheck(context);
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
                    image.createNewFile();
                    FileOutputStream ostream = new FileOutputStream(image);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();
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
