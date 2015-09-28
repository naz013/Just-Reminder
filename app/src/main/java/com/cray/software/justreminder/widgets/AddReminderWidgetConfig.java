package com.cray.software.justreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddReminderWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String ADD_REMINDER_WIDGET_PREF = "widget_pref";
    public final static String ADD_REMINDER_WIDGET_COLOR = "widget_color_";
    private int color;

    private LinearLayout widgetBg;
    private boolean isPro = false;

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

        ColorSetter cSetter = new ColorSetter(AddReminderWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.add_reminder_widget_config_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        widgetBg = (LinearLayout) findViewById(R.id.widgetBg);

        Spinner headerBgColor = (Spinner) findViewById(R.id.headerBgColor);
        isPro = Module.isPro();
        List<String> spinnerArray = new ArrayList<>();
        String[] colorsArray = getResources().getStringArray(R.array.color_list);
        Collections.addAll(spinnerArray, colorsArray);
        if (isPro){
            spinnerArray.add(getString(R.string.color_deep_purple));
            spinnerArray.add(getString(R.string.color_deep_orange));
            spinnerArray.add(getString(R.string.color_lime));
            spinnerArray.add(getString(R.string.color_indigo));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        headerBgColor.setAdapter(spinnerArrayAdapter);
        headerBgColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isPro) {
                    switch (i) {
                        case 0:
                            color = R.drawable.rectangle_stroke_red;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 1:
                            color = R.drawable.rectangle_stroke_violet;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 2:
                            color = R.drawable.rectangle_stroke_light_green;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 3:
                            color = R.drawable.rectangle_stroke_green;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 4:
                            color = R.drawable.rectangle_stroke_light_blue;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 5:
                            color = R.drawable.rectangle_stroke_blue;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 6:
                            color = R.drawable.rectangle_stroke_yellow;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 7:
                            color = R.drawable.rectangle_stroke_orange;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 8:
                            color = R.drawable.rectangle_stroke_grey;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 9:
                            color = R.drawable.rectangle_stroke;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 10:
                            color = R.drawable.rectangle_stroke_sand;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 11:
                            color = R.drawable.rectangle_stroke_brown;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 12:
                            color = R.drawable.rectangle_stroke_transparent;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 13:
                            color = R.drawable.rectangle_stroke_deep_purple;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 14:
                            color = R.drawable.rectangle_stroke_deep_orange;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 15:
                            color = R.drawable.rectangle_stroke_lime;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 16:
                            color = R.drawable.rectangle_stroke_indigo;
                            widgetBg.setBackgroundResource(color);
                            break;
                        default:
                            color = R.drawable.rectangle_stroke_blue;
                            widgetBg.setBackgroundResource(color);
                            break;
                    }
                } else {
                    switch (i) {
                        case 0:
                            color = R.drawable.rectangle_stroke_red;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 1:
                            color = R.drawable.rectangle_stroke_violet;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 2:
                            color = R.drawable.rectangle_stroke_light_green;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 3:
                            color = R.drawable.rectangle_stroke_green;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 4:
                            color = R.drawable.rectangle_stroke_light_blue;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 5:
                            color = R.drawable.rectangle_stroke_blue;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 6:
                            color = R.drawable.rectangle_stroke_yellow;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 7:
                            color = R.drawable.rectangle_stroke_orange;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 8:
                            color = R.drawable.rectangle_stroke_grey;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 9:
                            color = R.drawable.rectangle_stroke;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 10:
                            color = R.drawable.rectangle_stroke_sand;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 11:
                            color = R.drawable.rectangle_stroke_brown;
                            widgetBg.setBackgroundResource(color);
                            break;
                        case 12:
                            color = R.drawable.rectangle_stroke_transparent;
                            widgetBg.setBackgroundResource(color);
                            break;
                        default:
                            color = R.drawable.rectangle_stroke_blue;
                            widgetBg.setBackgroundResource(color);
                            break;
                    }
                }
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
                SharedPreferences sp = getSharedPreferences(ADD_REMINDER_WIDGET_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(ADD_REMINDER_WIDGET_COLOR + widgetID, color);
                editor.commit();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                AddReminderWidget.updateWidget(AddReminderWidgetConfig.this, appWidgetManager, sp, widgetID);

                setResult(RESULT_OK, resultValue);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
