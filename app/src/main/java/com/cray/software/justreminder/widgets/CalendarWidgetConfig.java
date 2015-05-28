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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CalendarWidgetConfig extends AppCompatActivity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    public final static String CURRENT_WIDGET_PREF = "calendar_pref";
    public final static String CURRENT_WIDGET_COLOR = "calendar_color_";
    public final static String CURRENT_WIDGET_BUTTON_COLOR = "calendar_button_color_";
    public final static String CURRENT_WIDGET_BUTTON_VOICE_COLOR = "calendar_button_voice_color_";
    public final static String CURRENT_WIDGET_BUTTON_SETTINGS_COLOR = "calendar_button_settings_color_";
    public final static String CURRENT_WIDGET_TITLE_COLOR = "calendar_title_color_";
    public final static String CURRENT_WIDGET_MONTH = "calendar_month_";
    int color, title, buttonColor, buttonVoice, buttonSettings;
    ColorSetter cSetter;

    Toolbar toolbar;

    TextView note, widgetTitle;
    Spinner widgetBgSpinner;
    RadioGroup colorsTitleGroup, colorsButtonGroup;
    RadioButton radioTitleBlack, radioButtonBlack, radioButtonWhite, radioTitleWhite;


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

        SharedPreferences sp = getSharedPreferences(CURRENT_WIDGET_PREF, MODE_PRIVATE);
        color = sp.getInt(CURRENT_WIDGET_COLOR + widgetID, 0);
        title = sp.getInt(CURRENT_WIDGET_TITLE_COLOR + widgetID, 0);
        buttonColor = sp.getInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, 0);
        buttonVoice = sp.getInt(CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, 0);
        buttonSettings = sp.getInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, 0);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        cSetter = new ColorSetter(CalendarWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.calendar_widget_config_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        note = (TextView) findViewById(R.id.note);
        widgetTitle = (TextView) findViewById(R.id.widgetTitle);

        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getString(R.string.led_color_white));
        spinnerArray.add(getString(R.string.color_transparent));
        spinnerArray.add(getString(R.string.color_black));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);

        widgetBgSpinner = (Spinner) findViewById(R.id.widgetBgSpinner);
        widgetBgSpinner.setAdapter(spinnerArrayAdapter);
        widgetBgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        color = getResources().getColor(R.color.colorWhite);
                        break;
                    case 1:
                        color = getResources().getColor(android.R.color.transparent);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.colorBlack);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        colorsTitleGroup = (RadioGroup) findViewById(R.id.colorsTitleGroup);
        colorsTitleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioTitleBlack:
                        title = getResources().getColor(R.color.colorBlack);
                        break;
                    case R.id.radioTitleWhite:
                        title = getResources().getColor(R.color.colorWhite);
                        break;
                }
            }
        });

        colorsButtonGroup = (RadioGroup) findViewById(R.id.colorsButtonGroup);
        colorsButtonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioButtonBlack:
                        buttonColor = R.drawable.ic_add_grey600_24dp;
                        buttonVoice = R.drawable.ic_mic_grey600_24dp;
                        buttonSettings = R.drawable.ic_settings_grey600_24dp;
                        break;
                    case R.id.radioButtonWhite:
                        buttonColor = R.drawable.ic_add_white_24dp;
                        buttonVoice = R.drawable.ic_mic_white_24dp;
                        buttonSettings = R.drawable.ic_settings_white_24dp;
                }
            }
        });


        radioTitleBlack = (RadioButton) findViewById(R.id.radioTitleBlack);
        radioTitleWhite = (RadioButton) findViewById(R.id.radioTitleWhite);
        radioTitleBlack.setChecked(true);
        radioButtonBlack = (RadioButton) findViewById(R.id.radioButtonBlack);
        radioButtonWhite = (RadioButton) findViewById(R.id.radioButtonWhite);
        radioButtonBlack.setChecked(true);

        if (buttonColor == R.drawable.ic_add_grey600_24dp) radioButtonBlack.setChecked(true);
        else radioButtonWhite.setChecked(true);

        if (title == getResources().getColor(R.color.colorBlack)) radioTitleBlack.setChecked(true);
        else radioTitleWhite.setChecked(true);

        widgetBgSpinner.setSelection(0);
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
                SharedPreferences sp = getSharedPreferences(CURRENT_WIDGET_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                Calendar cal = new GregorianCalendar();
                int month = cal.get(Calendar.MONTH);
                editor.putInt(CURRENT_WIDGET_COLOR + widgetID, color);
                editor.putInt(CURRENT_WIDGET_TITLE_COLOR + widgetID, title);
                editor.putInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, buttonColor);
                editor.putInt(CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, buttonVoice);
                editor.putInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, buttonSettings);
                editor.putInt(CURRENT_WIDGET_MONTH + widgetID, month);
                editor.commit();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                CalendarWidget.updateWidget(CalendarWidgetConfig.this, appWidgetManager, sp, widgetID);
                setResult(RESULT_OK, resultValue);
                finish();
                return true;
        }
        return true;
    }
}
