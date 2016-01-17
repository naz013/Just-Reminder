package com.cray.software.justreminder.widgets.configs;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.widgets.CurrentNotesWidget;
import com.cray.software.justreminder.widgets.utils.WidgetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentNotesWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String CURRENT_WIDGET_PREF = "notes_pref";
    public final static String CURRENT_WIDGET_COLOR = "notes_color_";
    public final static String CURRENT_WIDGET_HEADER_COLOR = "notes_header_color_";
    public final static String CURRENT_WIDGET_TITLE_COLOR = "notes_title_color_";
    public final static String CURRENT_WIDGET_BUTTON_COLOR = "notes_button_color_";
    public final static String CURRENT_WIDGET_BUTTON_SETTINGS_COLOR = "calendar_button_settings_color_";
    private int color, headerColor, title, button, buttonSettings;

    private LinearLayout headerBg, widgetBg;
    private TextView widgetTitle;
    private ImageButton tasksCount;
    private SeekBar alphaSeek;
    private Spinner headerBgColor, widgetBgSpinner;
    private RadioButton headerButton;
    private RadioButton bodyButton;

    private int headerTr = 255, bodyTr = 255;

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
        headerColor = sp.getInt(CURRENT_WIDGET_HEADER_COLOR + widgetID, 0);
        title = sp.getInt(CURRENT_WIDGET_TITLE_COLOR + widgetID, 0);
        button = sp.getInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, 0);
        buttonSettings = sp.getInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, 0);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        ColorSetter cSetter = new ColorSetter(CurrentNotesWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.note_widget_config_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        headerBg = (LinearLayout) findViewById(R.id.headerBg);
        widgetBg = (LinearLayout) findViewById(R.id.widgetBg);
        TextView note = (TextView) findViewById(R.id.note);
        widgetTitle = (TextView) findViewById(R.id.widgetTitle);
        ImageView image = (ImageView) findViewById(R.id.imageView);
        tasksCount = (ImageButton) findViewById(R.id.tasksCount);

        alphaSeek = (SeekBar) findViewById(R.id.alphaSeek);
        alphaSeek.setMax(255);
        alphaSeek.setProgress(255);
        alphaSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bodyButton.isChecked()) {
                    bodyTr = progress;
                    String colorN = Integer.toHexString(getColor(widgetBgSpinner));
                    int colorC = (int) Long.parseLong(colorN, 16);
                    int r = (colorC >> 16) & 0xFF;
                    int g = (colorC >> 8) & 0xFF;
                    int b = (colorC >> 0) & 0xFF;
                    color = android.graphics.Color.argb(bodyTr, r, g, b);
                    widgetBg.setBackgroundColor(color);
                } else if (headerButton.isChecked()) {
                    headerTr = progress;
                    String colorN = Integer.toHexString(getColor(headerBgColor));
                    int colorC = (int) Long.parseLong(colorN, 16);
                    int r = (colorC >> 16) & 0xFF;
                    int g = (colorC >> 8) & 0xFF;
                    int b = (colorC >> 0) & 0xFF;
                    headerColor = android.graphics.Color.argb(headerTr, r, g, b);
                    headerBg.setBackgroundColor(headerColor);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RadioGroup switcherGroup = (RadioGroup) findViewById(R.id.switcherGroup);
        switcherGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.headerButton:
                        alphaSeek.setProgress(headerTr);
                        break;
                    case R.id.bodyButton:
                        alphaSeek.setProgress(bodyTr);
                        break;
                }
            }
        });

        headerBgColor = (Spinner) findViewById(R.id.headerBgColor);
        boolean isPro = Module.isPro();
        List<String> spinnerArray = new ArrayList<>();
        String[] colorsArray = getResources().getStringArray(R.array.colors_list);
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
                headerColor = convertColor(getResources().getColor(WidgetUtils.getColor(i)), headerTr);
                headerBg.setBackgroundColor(headerColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        widgetBgSpinner = (Spinner) findViewById(R.id.widgetBgSpinner);
        widgetBgSpinner.setAdapter(spinnerArrayAdapter);
        widgetBgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                color = convertColor(getResources().getColor(WidgetUtils.getColor(i)), bodyTr);
                widgetBg.setBackgroundColor(color);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RadioGroup colorsTitleGroup = (RadioGroup) findViewById(R.id.colorsTitleGroup);
        colorsTitleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioTitleBlack:
                        title = getResources().getColor(R.color.blackPrimary);
                        widgetTitle.setTextColor(title);
                        break;
                    case R.id.radioTitleWhite:
                        title = getResources().getColor(R.color.whitePrimary);
                        widgetTitle.setTextColor(title);
                        break;
                }
            }
        });

        RadioGroup colorsButtonGroup = (RadioGroup) findViewById(R.id.colorsButtonGroup);
        colorsButtonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioButtonBlack:
                        button = R.drawable.ic_add_black_24dp;
                        tasksCount.setImageResource(button);
                        buttonSettings = R.drawable.ic_settings_black_24dp;
                        break;
                    case R.id.radioButtonWhite:
                        button = R.drawable.ic_add_white_24dp;
                        tasksCount.setImageResource(button);
                        buttonSettings = R.drawable.ic_settings_white_24dp;
                }
            }
        });

        headerButton = (RadioButton) findViewById(R.id.headerButton);
        bodyButton = (RadioButton) findViewById(R.id.bodyButton);
        RadioButton radioButtonBlack = (RadioButton) findViewById(R.id.radioButtonBlack);
        RadioButton radioTitleBlack = (RadioButton) findViewById(R.id.radioTitleBlack);
        bodyButton = (RadioButton) findViewById(R.id.bodyButton);
        headerButton.setChecked(true);
        radioButtonBlack.setChecked(true);
        radioTitleBlack.setChecked(true);
    }

    private int convertColor(int toHex, int tr){
        String colorN = Integer.toHexString(toHex);
        int colorC = (int)Long.parseLong(colorN, 16);
        int r = (colorC >> 16) & 0xFF;
        int g = (colorC >> 8) & 0xFF;
        int b = (colorC >> 0) & 0xFF;
        return android.graphics.Color.argb(tr, r, g, b);
    }

    private int getColor(Spinner spinner){
        int i = spinner.getSelectedItemPosition();
        return getResources().getColor(WidgetUtils.getColor(i));
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
                editor.putInt(CURRENT_WIDGET_HEADER_COLOR + widgetID, headerColor);
                editor.putInt(CURRENT_WIDGET_COLOR + widgetID, color);
                editor.putInt(CURRENT_WIDGET_TITLE_COLOR + widgetID, title);
                editor.putInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, button);
                editor.putInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, buttonSettings);
                editor.commit();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                CurrentNotesWidget.updateWidget(CurrentNotesWidgetConfig.this, appWidgetManager, sp, widgetID);
                setResult(RESULT_OK, resultValue);
                finish();
                return true;
        }
        return true;
    }
}
