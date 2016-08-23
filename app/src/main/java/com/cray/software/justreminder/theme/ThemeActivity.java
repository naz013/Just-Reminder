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
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ColorPickerView;

public class ThemeActivity extends AppCompatActivity implements ColorPickerView.OnColorListener {

    private FloatingActionButton mFab;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(ThemeActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.theme_color_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.theme));
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        int loaded = SharedPrefs.getInstance(this).getInt(Prefs.APP_THEME);
        ColorPickerView pickerView = (ColorPickerView) findViewById(R.id.pickerView);
        pickerView.setListener(this);
        pickerView.setSelectedColor(loaded);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cs.colorAccent(), cs.colorPrimary()));
    }

    private void saveColor(int code) {
        SharedPrefs.getInstance(this).putInt(Prefs.APP_THEME, code);
        SharedPrefs.getInstance(this).putBoolean(Prefs.UI_CHANGED, true);
    }

    @Override
    public void onBackPressed() {
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.STATUS_BAR_NOTIFICATION)) {
            new Notifier(ThemeActivity.this).recreatePermanent();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (SharedPrefs.getInstance(this).getBoolean(Prefs.STATUS_BAR_NOTIFICATION)) {
                    new Notifier(ThemeActivity.this).recreatePermanent();
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onColorSelect(int code, @ColorRes int color) {
        saveColor(code);
        refreshUi();
    }

    private void refreshUi() {
        ColorSetter cs = ColorSetter.getInstance(ThemeActivity.this);
        toolbar.setBackgroundColor(ViewUtils.getColor(this, cs.colorPrimary()));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, cs.colorAccent(), cs.colorPrimary()));
        mFab.setRippleColor(ViewUtils.getColor(this, cs.colorPrimary()));
    }
}