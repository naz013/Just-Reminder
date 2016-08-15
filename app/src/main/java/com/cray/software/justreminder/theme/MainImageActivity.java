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

package com.cray.software.justreminder.theme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboRadioButton;
import com.cray.software.justreminder.utils.ViewUtils;

public class MainImageActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String DEFAULT_PHOTO = "https://hd.unsplash.com/photo-1460500063983-994d4c27756c";
    private static final String NONE_PHOTO = "";
    private static final String TAG = "MainImageActivity";

    private Toolbar toolbar;

    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(MainImageActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_main_image_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.main_image));
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        RoboRadioButton defaultCheck = (RoboRadioButton) findViewById(R.id.defaultCheck);
        RoboRadioButton noneCheck = (RoboRadioButton) findViewById(R.id.noneCheck);
        defaultCheck.setOnCheckedChangeListener(this);
        noneCheck.setOnCheckedChangeListener(this);
        id = SharedPrefs.getInstance(this).getLong(Prefs.MAIN_IMAGE_ID);
        String path = SharedPrefs.getInstance(this).getString(Prefs.MAIN_IMAGE_PATH);
        if (id == 0 && path.matches(DEFAULT_PHOTO)) {
            defaultCheck.setChecked(true);
        } else {
            noneCheck.setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.defaultCheck:
                if (b) setImageUrl(DEFAULT_PHOTO, 0);
                break;
            case R.id.noneCheck:
                if (b) setImageUrl(NONE_PHOTO, 0);
                break;
        }
    }

    private void setImageUrl(String imageUrl, long id) {
        SharedPrefs.getInstance(this).putString(Prefs.MAIN_IMAGE_PATH, imageUrl);
        SharedPrefs.getInstance(this).putLong(Prefs.MAIN_IMAGE_ID, id);
    }
}