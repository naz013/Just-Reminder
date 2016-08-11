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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

public class CalendarStyle extends AppCompatActivity {
    private ImageButton red, green, blue, yellow, greenLight, blueLight, cyan, purple,
            amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime;
    private int i;
    private int prevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(CalendarStyle.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.theme_color_layout);

        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.theme));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        Intent intent = getIntent();
        i = intent.getIntExtra("type", 1);

        if (i == 2) toolbar.setTitle(getString(R.string.birthdays_color));
        else if (i == 3) toolbar.setTitle(getString(R.string.reminders_color));
        else toolbar.setTitle(getString(R.string.today_color));

        red = (ImageButton) findViewById(R.id.red_checkbox);
        purple = (ImageButton) findViewById(R.id.violet_checkbox);
        green = (ImageButton) findViewById(R.id.green_checkbox);
        greenLight = (ImageButton) findViewById(R.id.light_green_checkbox);
        blue = (ImageButton) findViewById(R.id.blue_checkbox);
        blueLight = (ImageButton) findViewById(R.id.light_blue_checkbox);
        yellow = (ImageButton) findViewById(R.id.yellow_checkbox);
        orange = (ImageButton) findViewById(R.id.orange_checkbox);
        cyan = (ImageButton) findViewById(R.id.grey_checkbox);
        pink = (ImageButton) findViewById(R.id.pink_checkbox);
        teal = (ImageButton) findViewById(R.id.sand_checkbox);
        amber = (ImageButton) findViewById(R.id.brown_checkbox);

        deepPurple = (ImageButton) findViewById(R.id.deepPurple);
        indigo = (ImageButton) findViewById(R.id.indigoCheckbox);
        lime = (ImageButton) findViewById(R.id.limeCheckbox);
        deepOrange = (ImageButton) findViewById(R.id.deepOrange);

        LinearLayout themeGroupPro = (LinearLayout) findViewById(R.id.themeGroupPro);
        if (Module.isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        } else themeGroupPro.setVisibility(View.GONE);

        findViewById(R.id.fab).setVisibility(View.GONE);

        setOnClickListener(red, green, blue, yellow, greenLight, blueLight, cyan, purple,
                amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime);

        setUpRadio();
    }

    private void setOnClickListener(View... views){
        for (View view : views){
            view.setOnClickListener(listener);
        }
    }

    private View.OnClickListener listener = v -> themeColorSwitch(v.getId());

    private void setUpRadio(){
        SharedPrefs sPrefs = SharedPrefs.getInstance(this);
        int loaded;
        if (i == 2) loaded = sPrefs.getInt(Prefs.BIRTH_COLOR);
        else if (i == 3) loaded = sPrefs.getInt(Prefs.REMINDER_COLOR);
        else loaded = sPrefs.getInt(Prefs.TODAY_COLOR);
        switch (loaded) {
            case 0:
                red.setSelected(true);
                break;
            case 1:
                purple.setSelected(true);
                break;
            case 2:
                greenLight.setSelected(true);
                break;
            case 3:
                green.setSelected(true);
                break;
            case 4:
                blueLight.setSelected(true);
                break;
            case 5:
                blue.setSelected(true);
                break;
            case 6:
                yellow.setSelected(true);
                break;
            case 7:
                orange.setSelected(true);
                break;
            case 8:
                cyan.setSelected(true);
                break;
            case 9:
                pink.setSelected(true);
                break;
            case 10:
                teal.setSelected(true);
                break;
            case 11:
                amber.setSelected(true);
                break;
            case 12:
                deepPurple.setSelected(true);
                break;
            case 13:
                deepOrange.setSelected(true);
                break;
            case 14:
                lime.setSelected(true);
                break;
            case 15:
                indigo.setSelected(true);
                break;
            default:
                blue.setSelected(true);
                break;
        }
    }

    private void themeColorSwitch(int radio){
        if (radio == prevId) return;
        prevId = radio;
        disableAll();
        setSelected(radio);
        switch (radio){
            case R.id.red_checkbox:
                saveColor(0);
                break;
            case R.id.violet_checkbox:
                saveColor(1);
                break;
            case R.id.light_green_checkbox:
                saveColor(2);
                break;
            case R.id.green_checkbox:
                saveColor(3);
                break;
            case R.id.light_blue_checkbox:
                saveColor(4);
                break;
            case R.id.blue_checkbox:
                saveColor(5);
                break;
            case R.id.yellow_checkbox:
                saveColor(6);
                break;
            case R.id.orange_checkbox:
                saveColor(7);
                break;
            case R.id.grey_checkbox:
                saveColor(8);
                break;
            case R.id.pink_checkbox:
                saveColor(9);
                break;
            case R.id.sand_checkbox:
                saveColor(10);
                break;
            case R.id.brown_checkbox:
                saveColor(11);
                break;
            case R.id.deepPurple:
                saveColor(12);
                break;
            case R.id.deepOrange:
                saveColor(13);
                break;
            case R.id.limeCheckbox:
                saveColor(14);
                break;
            case R.id.indigoCheckbox:
                saveColor(15);
                break;
        }
    }

    private void setSelected(int radio) {
        findViewById(radio).setSelected(true);
    }

    private void disableAll() {
        red.setSelected(false);
        purple.setSelected(false);
        greenLight.setSelected(false);
        green.setSelected(false);
        blueLight.setSelected(false);
        blue.setSelected(false);
        yellow.setSelected(false);
        orange.setSelected(false);
        cyan.setSelected(false);
        pink.setSelected(false);
        teal.setSelected(false);
        amber.setSelected(false);
        deepOrange.setSelected(false);
        deepPurple.setSelected(false);
        lime.setSelected(false);
        indigo.setSelected(false);
    }

    void saveColor(int code) {
        SharedPrefs sPrefs = SharedPrefs.getInstance(this);
        if (i == 2) sPrefs.putInt(Prefs.BIRTH_COLOR, code);
        else if (i == 3) sPrefs.putInt(Prefs.REMINDER_COLOR, code);
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
            new Notifier(CalendarStyle.this).recreatePermanent();
        }
        finish();
    }
}