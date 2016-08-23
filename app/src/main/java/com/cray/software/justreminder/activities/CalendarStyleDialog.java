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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ColorPickerView;

public class CalendarStyleDialog extends AppCompatActivity implements ColorPickerView.OnColorListener {

    private int mReceivedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(CalendarStyleDialog.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.theme_color_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        Intent intent = getIntent();
        mReceivedCode = intent.getIntExtra("type", 1);
        initActionBar();
        findViewById(R.id.fab).setVisibility(View.GONE);
        initColorPicker();
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.today_color));
        if (mReceivedCode == 2) toolbar.setTitle(getString(R.string.birthdays_color));
        else if (mReceivedCode == 3) toolbar.setTitle(getString(R.string.reminders_color));
    }

    private void initColorPicker() {
        ColorPickerView pickerView = (ColorPickerView) findViewById(R.id.pickerView);
        pickerView.setListener(this);
        SharedPrefs sPrefs = SharedPrefs.getInstance(this);
        int loaded = sPrefs.getInt(Prefs.TODAY_COLOR);
        if (mReceivedCode == 2) loaded = sPrefs.getInt(Prefs.BIRTH_COLOR);
        else if (mReceivedCode == 3) loaded = sPrefs.getInt(Prefs.REMINDER_COLOR);
        pickerView.setSelectedColor(loaded);
    }

    void saveColor(int code) {
        SharedPrefs sPrefs = SharedPrefs.getInstance(this);
        if (mReceivedCode == 2) sPrefs.putInt(Prefs.BIRTH_COLOR, code);
        else if (mReceivedCode == 3) sPrefs.putInt(Prefs.REMINDER_COLOR, code);
        else sPrefs.putInt(Prefs.TODAY_COLOR, code);
        UpdatesHelper.getInstance(this).updateCalendarWidget();
    }

    @Override
    public void onBackPressed() {
        closeScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeScreen() {
        if (SharedPrefs.getInstance(this).getBoolean(Prefs.STATUS_BAR_NOTIFICATION)) {
            new Notifier(CalendarStyleDialog.this).recreatePermanent();
        }
        finish();
    }

    @Override
    public void onColorSelect(int code, @ColorRes int color) {
        saveColor(code);
    }
}