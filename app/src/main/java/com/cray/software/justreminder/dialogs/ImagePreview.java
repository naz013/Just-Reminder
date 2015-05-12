package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePreview extends Activity {

    String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(ImagePreview.this);
        setTheme(cs.getFullscreenStyle());
        setContentView(R.layout.activity_image_preview);

        ImageView mImageView = (ImageView) findViewById(R.id.iv_photo);

        photoPath = getIntent().getStringExtra("image");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        mImageView.setImageBitmap(bitmap);

        PhotoViewAttacher mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    @Override
    protected void onDestroy() {
        if (photoPath != null && !photoPath.matches("")) {
            File sdPathDr = new File(photoPath);
            if (sdPathDr.exists()) {
                sdPathDr.delete();
            }
        }
        super.onDestroy();
    }
}
