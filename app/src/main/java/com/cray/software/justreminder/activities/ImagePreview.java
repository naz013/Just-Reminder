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

package com.cray.software.justreminder.activities;

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

    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ColorSetter.getInstance(this).getFullscreenStyle());
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
