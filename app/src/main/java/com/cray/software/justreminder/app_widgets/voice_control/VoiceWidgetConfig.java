/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.app_widgets.voice_control;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.WidgetUtils;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoiceWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String VOICE_WIDGET_PREF = "widget_pref";
    public final static String VOICE_WIDGET_COLOR = "widget_color_";
    private int color;

    private LinearLayout widgetBg;

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

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        ColorSetter cSetter = ColorSetter.getInstance(VoiceWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.voice_widget_config_layout);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.voice_control));

        widgetBg = (LinearLayout) findViewById(R.id.widgetBg);

        Spinner headerBgColor = (Spinner) findViewById(R.id.headerBgColor);
            boolean isPro = Module.isPro();
        List<String> spinnerArray = new ArrayList<>();
        String[] colorsArray = getResources().getStringArray(R.array.color_list);
        Collections.addAll(spinnerArray, colorsArray);
        if (isPro){
            spinnerArray.add(getString(R.string.dark_purple));
            spinnerArray.add(getString(R.string.dark_orange));
            spinnerArray.add(getString(R.string.lime));
            spinnerArray.add(getString(R.string.indigo));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        headerBgColor.setAdapter(spinnerArrayAdapter);
        headerBgColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                color = WidgetUtils.getDrawable(i);
                widgetBg.setBackgroundResource(color);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                SharedPreferences sp = getSharedPreferences(VOICE_WIDGET_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(VOICE_WIDGET_COLOR + widgetID, color);
                editor.apply();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                VoiceWidget.updateWidget(VoiceWidgetConfig.this, appWidgetManager, sp, widgetID);
                setResult(RESULT_OK, resultValue);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
