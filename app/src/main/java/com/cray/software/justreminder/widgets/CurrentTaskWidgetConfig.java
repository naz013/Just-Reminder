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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.ManageModule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class CurrentTaskWidgetConfig extends AppCompatActivity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    public final static String CURRENT_WIDGET_PREF = "widget_pref";
    public final static String CURRENT_WIDGET_TEXT_COLOR = "widget_text_color_";
    public final static String CURRENT_WIDGET_ITEM_TEXT_COLOR = "widget_item_text_color_";
    public final static String CURRENT_WIDGET_COLOR = "widget_color_";
    public final static String CURRENT_WIDGET_HEADER_COLOR = "widget_header_color_";
    public final static String CURRENT_WIDGET_ITEM_COLOR = "widget_item_color_";
    public final static String CURRENT_WIDGET_BUTTON_COLOR = "widget_button_color_";
    public final static String CURRENT_WIDGET_BUTTON_VOICE_COLOR = "widget_button_voice_color_";
    public final static String CURRENT_WIDGET_BUTTON_SETTINGS_COLOR = "widget_button_settings_color_";
    public final static String CURRENT_WIDGET_TEXT_SIZE = "widget_text_size_";
    int color, headerColor, textColor, itemColor, itemTextColor, button = 0, buttonVoice, buttonSettings;
    float textMultiply;
    ColorSetter cSetter;

    Toolbar toolbar;

    LinearLayout headerBg, widgetBg, listItemCard;
    TextView widgetDate, taskText, taskNumber, taskDate, taskTime;
    SeekBar alphaSeek, textSize;
    ImageButton tasksCount;
    Spinner headerBgColor, widgetBgSpinner, itemBgSpinner;
    RadioGroup colorsGroup, switcherGroup, colorsItemGroup, colorsButtonGroup;
    RadioButton radioBlack, headerButton, bodyButton, itemButton, radioItemBlack, radioButtonBlack;

    int headerTr = 255, bodyTr = 255, itemTr = 255;

    boolean isPro = false;

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

        cSetter = new ColorSetter(CurrentTaskWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.current_widget_config_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        headerBg = (LinearLayout) findViewById(R.id.headerBg);
        widgetBg = (LinearLayout) findViewById(R.id.widgetBg);
        widgetDate = (TextView) findViewById(R.id.widgetDate);
        taskText = (TextView) findViewById(R.id.taskText);
        taskNumber = (TextView) findViewById(R.id.taskNumber);
        taskDate = (TextView) findViewById(R.id.taskDate);
        taskTime = (TextView) findViewById(R.id.taskTime);
        tasksCount = (ImageButton) findViewById(R.id.tasksCount);

        textSize = (SeekBar) findViewById(R.id.textSize);
        textSize.setMax(13);
        textSize.setProgress(2);
        textMultiply = 14;
        textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textMultiply = 12 + progress;
                taskText.setTextSize(textMultiply);
                taskNumber.setTextSize(textMultiply);
                taskDate.setTextSize(textMultiply);
                taskTime.setTextSize(textMultiply);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        alphaSeek = (SeekBar) findViewById(R.id.alphaSeek);
        alphaSeek.setMax(255);
        alphaSeek.setProgress(255);
        alphaSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bodyButton.isChecked()){
                    bodyTr = progress;
                    String colorN = Integer.toHexString(getColor(widgetBgSpinner));
                    int colorC = (int)Long.parseLong(colorN, 16);
                    int r = (colorC >> 16) & 0xFF;
                    int g = (colorC >> 8) & 0xFF;
                    int b = (colorC >> 0) & 0xFF;
                    color = android.graphics.Color.argb(bodyTr, r, g, b);
                    widgetBg.setBackgroundColor(color);
                } else if (headerButton.isChecked()){
                    headerTr = progress;
                    String colorN = Integer.toHexString(getColor(headerBgColor));
                    int colorC = (int)Long.parseLong(colorN, 16);
                    int r = (colorC >> 16) & 0xFF;
                    int g = (colorC >> 8) & 0xFF;
                    int b = (colorC >> 0) & 0xFF;
                    headerColor = android.graphics.Color.argb(headerTr, r, g, b);
                    headerBg.setBackgroundColor(headerColor);
                } else if (itemButton.isChecked()){
                    itemTr = progress;
                    String colorN = Integer.toHexString(getColor(itemBgSpinner));
                    int colorC = (int)Long.parseLong(colorN, 16);
                    int r = (colorC >> 16) & 0xFF;
                    int g = (colorC >> 8) & 0xFF;
                    int b = (colorC >> 0) & 0xFF;
                    itemColor = android.graphics.Color.argb(itemTr, r, g, b);
                    listItemCard.setBackgroundColor(itemColor);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        listItemCard = (LinearLayout) findViewById(R.id.listItemCard);

        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM yyyy");
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime());
        widgetDate.setText(date);

        switcherGroup = (RadioGroup) findViewById(R.id.switcherGroup);
        switcherGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.headerButton:
                        alphaSeek.setProgress(headerTr);
                        break;
                    case R.id.bodyButton:
                        alphaSeek.setProgress(bodyTr);
                        break;
                    case R.id.itemButton:
                        alphaSeek.setProgress(itemTr);
                        break;
                }
            }
        });

        headerBgColor = (Spinner) findViewById(R.id.headerBgColor);
        isPro = new ManageModule().isPro();
        List<String> spinnerArray = new ArrayList<>();
        String[] colorsArray = getResources().getStringArray(R.array.colors_list);
        Collections.addAll(spinnerArray, colorsArray);
        if (isPro){
            spinnerArray.add(getString(R.string.color_deep_purple));
            spinnerArray.add(getString(R.string.color_deep_orange));
            spinnerArray.add(getString(R.string.color_lime));
            spinnerArray.add(getString(R.string.color_indigo));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        headerBgColor.setAdapter(spinnerArrayAdapter);
        headerBgColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isPro){
                    switch (i) {
                        case 0:
                            headerColor = convertColor(getResources().getColor(R.color.colorWhite), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 1:
                            headerColor = convertColor(getResources().getColor(R.color.colorRed), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 2:
                            headerColor = convertColor(getResources().getColor(R.color.colorViolet), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 3:
                            headerColor = convertColor(getResources().getColor(R.color.colorLightCreen), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 4:
                            headerColor = convertColor(getResources().getColor(R.color.colorGreen), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 5:
                            headerColor = convertColor(getResources().getColor(R.color.colorLightBlue), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 6:
                            headerColor = convertColor(getResources().getColor(R.color.colorBlue), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 7:
                            headerColor = convertColor(getResources().getColor(R.color.colorYellow), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 8:
                            headerColor = convertColor(getResources().getColor(R.color.colorOrange), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 9:
                            headerColor = convertColor(getResources().getColor(R.color.colorGrey), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 10:
                            headerColor = convertColor(getResources().getColor(R.color.colorPink), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 11:
                            headerColor = convertColor(getResources().getColor(R.color.colorSand), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 12:
                            headerColor = convertColor(getResources().getColor(R.color.colorBrown), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 13:
                            headerColor = convertColor(getResources().getColor(android.R.color.transparent), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 14:
                            headerColor = convertColor(getResources().getColor(R.color.colorDeepPurple), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 15:
                            headerColor = convertColor(getResources().getColor(R.color.colorDeepOrange), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 16:
                            headerColor = convertColor(getResources().getColor(R.color.colorLime), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 17:
                            headerColor = convertColor(getResources().getColor(R.color.colorIndigo), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        default:
                            headerColor = convertColor(getResources().getColor(R.color.colorBlue), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                    }
                } else {
                    switch (i) {
                        case 0:
                            headerColor = convertColor(getResources().getColor(R.color.colorWhite), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 1:
                            headerColor = convertColor(getResources().getColor(R.color.colorRed), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 2:
                            headerColor = convertColor(getResources().getColor(R.color.colorViolet), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 3:
                            headerColor = convertColor(getResources().getColor(R.color.colorLightCreen), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 4:
                            headerColor = convertColor(getResources().getColor(R.color.colorGreen), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 5:
                            headerColor = convertColor(getResources().getColor(R.color.colorLightBlue), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 6:
                            headerColor = convertColor(getResources().getColor(R.color.colorBlue), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 7:
                            headerColor = convertColor(getResources().getColor(R.color.colorYellow), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 8:
                            headerColor = convertColor(getResources().getColor(R.color.colorOrange), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 9:
                            headerColor = convertColor(getResources().getColor(R.color.colorGrey), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 10:
                            headerColor = convertColor(getResources().getColor(R.color.colorPink), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 11:
                            headerColor = convertColor(getResources().getColor(R.color.colorSand), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 12:
                            headerColor = convertColor(getResources().getColor(R.color.colorBrown), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        case 13:
                            headerColor = convertColor(getResources().getColor(android.R.color.transparent), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                        default:
                            headerColor = convertColor(getResources().getColor(R.color.colorBlue), headerTr);
                            headerBg.setBackgroundColor(headerColor);
                            break;
                    }
                }
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
                if (isPro){
                    switch (i) {
                        case 0:
                            color = convertColor(getResources().getColor(R.color.colorWhite), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 1:
                            color = convertColor(getResources().getColor(R.color.colorRed), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 2:
                            color = convertColor(getResources().getColor(R.color.colorViolet), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 3:
                            color = convertColor(getResources().getColor(R.color.colorLightCreen), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 4:
                            color = convertColor(getResources().getColor(R.color.colorGreen), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 5:
                            color = convertColor(getResources().getColor(R.color.colorLightBlue), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 6:
                            color = convertColor(getResources().getColor(R.color.colorBlue), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 7:
                            color = convertColor(getResources().getColor(R.color.colorYellow), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 8:
                            color = convertColor(getResources().getColor(R.color.colorOrange), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 9:
                            color = convertColor(getResources().getColor(R.color.colorGrey), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 10:
                            color = convertColor(getResources().getColor(R.color.colorPink), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 11:
                            color = convertColor(getResources().getColor(R.color.colorSand), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 12:
                            color = convertColor(getResources().getColor(R.color.colorBrown), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 13:
                            color = convertColor(getResources().getColor(android.R.color.transparent), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 14:
                            color = convertColor(getResources().getColor(R.color.colorDeepPurple), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 15:
                            color = convertColor(getResources().getColor(R.color.colorDeepOrange), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 16:
                            color = convertColor(getResources().getColor(R.color.colorLime), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 17:
                            color = convertColor(getResources().getColor(R.color.colorIndigo), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        default:
                            color = convertColor(getResources().getColor(R.color.colorBlue), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                    }
                } else {
                    switch (i) {
                        case 0:
                            color = convertColor(getResources().getColor(R.color.colorWhite), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 1:
                            color = convertColor(getResources().getColor(R.color.colorRed), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 2:
                            color = convertColor(getResources().getColor(R.color.colorViolet), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 3:
                            color = convertColor(getResources().getColor(R.color.colorLightCreen), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 4:
                            color = convertColor(getResources().getColor(R.color.colorGreen), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 5:
                            color = convertColor(getResources().getColor(R.color.colorLightBlue), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 6:
                            color = convertColor(getResources().getColor(R.color.colorBlue), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 7:
                            color = convertColor(getResources().getColor(R.color.colorYellow), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 8:
                            color = convertColor(getResources().getColor(R.color.colorOrange), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 9:
                            color = convertColor(getResources().getColor(R.color.colorGrayDark), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 10:
                            color = convertColor(getResources().getColor(R.color.colorPink), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 11:
                            color = convertColor(getResources().getColor(R.color.colorSand), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 12:
                            color = convertColor(getResources().getColor(R.color.colorBrown), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                        case 13:
                            color = convertColor(getResources().getColor(android.R.color.transparent), bodyTr);
                            widgetBg.setBackgroundColor(color);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        itemBgSpinner = (Spinner) findViewById(R.id.itemBgSpinner);
        itemBgSpinner.setAdapter(spinnerArrayAdapter);
        itemBgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isPro){
                    switch (i) {
                        case 0:
                            itemColor = convertColor(getResources().getColor(R.color.colorWhite), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 1:
                            itemColor = convertColor(getResources().getColor(R.color.colorRed), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 2:
                            itemColor = convertColor(getResources().getColor(R.color.colorViolet), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 3:
                            itemColor = convertColor(getResources().getColor(R.color.colorLightCreen), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 4:
                            itemColor = convertColor(getResources().getColor(R.color.colorGreen), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 5:
                            itemColor = convertColor(getResources().getColor(R.color.colorLightBlue), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 6:
                            itemColor = convertColor(getResources().getColor(R.color.colorBlue), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 7:
                            itemColor = convertColor(getResources().getColor(R.color.colorYellow), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 8:
                            itemColor = convertColor(getResources().getColor(R.color.colorOrange), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 9:
                            itemColor = convertColor(getResources().getColor(R.color.colorGrey), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 10:
                            itemColor = convertColor(getResources().getColor(R.color.colorPink), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 11:
                            itemColor = convertColor(getResources().getColor(R.color.colorSand), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 12:
                            itemColor = convertColor(getResources().getColor(R.color.colorBrown), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 13:
                            itemColor = convertColor(getResources().getColor(android.R.color.transparent), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 14:
                            itemColor = convertColor(getResources().getColor(R.color.colorDeepPurple), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 15:
                            itemColor = convertColor(getResources().getColor(R.color.colorDeepOrange), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 16:
                            itemColor = convertColor(getResources().getColor(R.color.colorLime), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 17:
                            itemColor = convertColor(getResources().getColor(R.color.colorIndigo), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        default:
                            itemColor = convertColor(getResources().getColor(R.color.colorBlue), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                    }
                } else {
                    switch (i) {
                        case 0:
                            itemColor = convertColor(getResources().getColor(R.color.colorWhite), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 1:
                            itemColor = convertColor(getResources().getColor(R.color.colorRed), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 2:
                            itemColor = convertColor(getResources().getColor(R.color.colorViolet), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 3:
                            itemColor = convertColor(getResources().getColor(R.color.colorLightCreen), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 4:
                            itemColor = convertColor(getResources().getColor(R.color.colorGreen), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 5:
                            itemColor = convertColor(getResources().getColor(R.color.colorLightBlue), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 6:
                            itemColor = convertColor(getResources().getColor(R.color.colorBlue), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 7:
                            itemColor = convertColor(getResources().getColor(R.color.colorYellow), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 8:
                            itemColor = convertColor(getResources().getColor(R.color.colorOrange), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 9:
                            itemColor = convertColor(getResources().getColor(R.color.colorGrayDark), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 10:
                            itemColor = convertColor(getResources().getColor(R.color.colorPink), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 11:
                            itemColor = convertColor(getResources().getColor(R.color.colorSand), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 12:
                            itemColor = convertColor(getResources().getColor(R.color.colorBrown), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                        case 13:
                            itemColor = convertColor(getResources().getColor(android.R.color.transparent), itemTr);
                            listItemCard.setBackgroundColor(itemColor);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        colorsGroup = (RadioGroup) findViewById(R.id.colorsGroup);
        colorsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioBlack:
                        textColor = getResources().getColor(R.color.colorBlack);
                        widgetDate.setTextColor(textColor);
                        break;
                    case R.id.radioWhite:
                        textColor = getResources().getColor(R.color.colorWhite);
                        widgetDate.setTextColor(textColor);
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
                        button = R.drawable.ic_add_grey600_24dp;
                        tasksCount.setImageResource(button);
                        buttonVoice = R.drawable.ic_mic_grey600_24dp;
                        buttonSettings = R.drawable.ic_settings_grey600_24dp;
                        break;
                    case R.id.radioButtonWhite:
                        button = R.drawable.ic_add_white_24dp;
                        tasksCount.setImageResource(button);
                        buttonVoice = R.drawable.ic_mic_white_24dp;
                        buttonSettings = R.drawable.ic_settings_white_24dp;
                }
            }
        });

        colorsItemGroup = (RadioGroup) findViewById(R.id.colorsItemGroup);
        colorsItemGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioItemBlack:
                        itemTextColor = getResources().getColor(R.color.colorBlack);
                        taskText.setTextColor(itemTextColor);
                        taskNumber.setTextColor(itemTextColor);
                        taskDate.setTextColor(itemTextColor);
                        taskTime.setTextColor(itemTextColor);
                        break;
                    case R.id.radioItemWhite:
                        itemTextColor = getResources().getColor(R.color.colorWhite);
                        taskText.setTextColor(itemTextColor);
                        taskNumber.setTextColor(itemTextColor);
                        taskDate.setTextColor(itemTextColor);
                        taskTime.setTextColor(itemTextColor);
                        break;
                }
            }
        });

        radioBlack = (RadioButton) findViewById(R.id.radioBlack);
        radioItemBlack = (RadioButton) findViewById(R.id.radioItemBlack);
        headerButton = (RadioButton) findViewById(R.id.headerButton);
        bodyButton = (RadioButton) findViewById(R.id.bodyButton);
        itemButton = (RadioButton) findViewById(R.id.itemButton);
        radioButtonBlack = (RadioButton) findViewById(R.id.radioButtonBlack);
        radioBlack.setChecked(true);
        radioItemBlack.setChecked(true);
        headerButton.setChecked(true);
        radioButtonBlack.setChecked(true);
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
        int c = 0;
        int i = spinner.getSelectedItemPosition();
        if (isPro) {
            switch (i) {
                case 0:
                    c = getResources().getColor(R.color.colorSemiTrWhite);
                    break;
                case 1:
                    c = getResources().getColor(R.color.colorSemiTrRed);
                    break;
                case 2:
                    c = getResources().getColor(R.color.colorSemiTrViolet);
                    break;
                case 3:
                    c = getResources().getColor(R.color.colorSemiTrLightCreen);
                    break;
                case 4:
                    c = getResources().getColor(R.color.colorSemiTrGreen);
                    break;
                case 5:
                    c = getResources().getColor(R.color.colorSemiTrLightBlue);
                    break;
                case 6:
                    c = getResources().getColor(R.color.colorSemiTrBlue);
                    break;
                case 7:
                    c = getResources().getColor(R.color.colorSemiTrYellow);
                    break;
                case 8:
                    c = getResources().getColor(R.color.colorSemiTrOrange);
                    break;
                case 9:
                    c = getResources().getColor(R.color.colorSemiTrGrayDark);
                    break;
                case 10:
                    c = getResources().getColor(R.color.colorSemiTrPink);
                    break;
                case 11:
                    c = getResources().getColor(R.color.colorSemiTrSand);
                    break;
                case 12:
                    c = getResources().getColor(R.color.colorSemiTrBrown);
                    break;
                case 13:
                    c = getResources().getColor(android.R.color.transparent);
                    break;
                case 14:
                    c = getResources().getColor(R.color.colorSemiTrDeepPurple);
                    break;
                case 15:
                    c = getResources().getColor(R.color.colorSemiTrDeepOrange);
                    break;
                case 16:
                    c = getResources().getColor(R.color.colorSemiTrLime);
                    break;
                case 17:
                    c = getResources().getColor(R.color.colorSemiTrIndigo);
                    break;
            }
        } else {
            switch (i) {
                case 0:
                    c = getResources().getColor(R.color.colorSemiTrWhite);
                    break;
                case 1:
                    c = getResources().getColor(R.color.colorSemiTrRed);
                    break;
                case 2:
                    c = getResources().getColor(R.color.colorSemiTrViolet);
                    break;
                case 3:
                    c = getResources().getColor(R.color.colorSemiTrLightCreen);
                    break;
                case 4:
                    c = getResources().getColor(R.color.colorSemiTrGreen);
                    break;
                case 5:
                    c = getResources().getColor(R.color.colorSemiTrLightBlue);
                    break;
                case 6:
                    c = getResources().getColor(R.color.colorSemiTrBlue);
                    break;
                case 7:
                    c = getResources().getColor(R.color.colorSemiTrYellow);
                    break;
                case 8:
                    c = getResources().getColor(R.color.colorSemiTrOrange);
                    break;
                case 9:
                    c = getResources().getColor(R.color.colorSemiTrGrayDark);
                    break;
                case 10:
                    c = getResources().getColor(R.color.colorSemiTrPink);
                    break;
                case 11:
                    c = getResources().getColor(R.color.colorSemiTrSand);
                    break;
                case 12:
                    c = getResources().getColor(R.color.colorSemiTrBrown);
                    break;
                case 13:
                    c = getResources().getColor(android.R.color.transparent);
                    break;
            }
        }
        return c;
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
                editor.putInt(CURRENT_WIDGET_TEXT_COLOR + widgetID, textColor);
                editor.putInt(CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, itemTextColor);
                editor.putInt(CURRENT_WIDGET_COLOR + widgetID, color);
                editor.putInt(CURRENT_WIDGET_ITEM_COLOR + widgetID, itemColor);
                editor.putInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, button);
                editor.putInt(CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, buttonVoice);
                editor.putInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, buttonSettings);
                editor.putFloat(CURRENT_WIDGET_TEXT_SIZE + widgetID, textMultiply);
                editor.commit();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                CurrentTaskWidget.updateWidget(CurrentTaskWidgetConfig.this, appWidgetManager, sp, widgetID);
                setResult(RESULT_OK, resultValue);
                finish();
                return true;
        }
        return true;
    }
}
