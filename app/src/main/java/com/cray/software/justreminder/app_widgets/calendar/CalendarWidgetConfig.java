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

package com.cray.software.justreminder.app_widgets.calendar;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String CALENDAR_WIDGET_PREF = "calendar_pref";
    public final static String CALENDAR_WIDGET_THEME = "calendar_theme_";
    public final static String CALENDAR_WIDGET_MONTH = "calendar_month_";
    public final static String CALENDAR_WIDGET_YEAR = "calendar_year_";

    private ViewPager mThemePager;
    private ArrayList<CalendarTheme> mThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        SharedPreferences sp = getSharedPreferences(CALENDAR_WIDGET_PREF, MODE_PRIVATE);
        int theme = sp.getInt(CALENDAR_WIDGET_THEME + widgetID, 0);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        ColorSetter cSetter = ColorSetter.getInstance(CalendarWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.calendar_widget_config_layout);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.calendar));

        mThemePager = (ViewPager) findViewById(R.id.themePager);
        loadThemes();
        mThemePager.setCurrentItem(theme, true);
    }

    private void loadThemes(){
        mThemes = CalendarTheme.getThemes(this);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mThemes);
        mThemePager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.current_widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                updateWidget();
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void updateWidget() {
        SharedPreferences sp = getSharedPreferences(CALENDAR_WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int position = mThemePager.getCurrentItem();
        editor.putInt(CALENDAR_WIDGET_THEME + widgetID, position);
        editor.putInt(CALENDAR_WIDGET_MONTH + widgetID, month);
        editor.putInt(CALENDAR_WIDGET_YEAR + widgetID, year);
        editor.apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        CalendarWidget.updateWidget(CalendarWidgetConfig.this, appWidgetManager, sp, widgetID);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<CalendarTheme> arrayList;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<CalendarTheme> list) {
            super(fm);
            this.arrayList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarThemeFragment.newInstance(position, mThemes);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }
}
