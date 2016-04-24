package com.cray.software.justreminder.app_widgets.configs;

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
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.app_widgets.CurrentTaskWidget;
import com.cray.software.justreminder.app_widgets.utils.WidgetUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CurrentTaskWidgetConfig extends AppCompatActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
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
    private int color, headerColor, textColor, itemColor, itemTextColor, button = 0, buttonVoice, buttonSettings;
    private float textMultiply;

    private LinearLayout headerBg, widgetBg, listItemCard;
    private TextView widgetDate, taskText, taskNumber, taskDate, taskTime;
    private SeekBar alphaSeek;
    private ImageButton tasksCount;
    private Spinner headerBgColor, widgetBgSpinner, itemBgSpinner;
    private RadioButton headerButton;
    private RadioButton bodyButton;
    private RadioButton itemButton;

    private int headerTr = 255, bodyTr = 255, itemTr = 255;

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
        textColor = sp.getInt(CURRENT_WIDGET_TEXT_COLOR + widgetID, 0);
        button = sp.getInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, 0);
        itemColor = sp.getInt(CURRENT_WIDGET_ITEM_COLOR + widgetID, 0);
        itemTextColor = sp.getInt(CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, 0);
        buttonSettings = sp.getInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, 0);
        buttonVoice = sp.getInt(CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, 0);
        textMultiply = sp.getFloat(CURRENT_WIDGET_TEXT_SIZE + widgetID, 0);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);

        ColorSetter cSetter = new ColorSetter(CurrentTaskWidgetConfig.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.current_widget_config_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.active_reminders));

        headerBg = (LinearLayout) findViewById(R.id.headerBg);
        widgetBg = (LinearLayout) findViewById(R.id.widgetBg);
        widgetDate = (TextView) findViewById(R.id.widgetDate);
        taskText = (TextView) findViewById(R.id.taskText);
        taskNumber = (TextView) findViewById(R.id.taskNumber);
        taskDate = (TextView) findViewById(R.id.taskDate);
        taskTime = (TextView) findViewById(R.id.taskTime);
        tasksCount = (ImageButton) findViewById(R.id.tasksCount);

        SeekBar textSize = (SeekBar) findViewById(R.id.textSize);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault());
        dateFormat.setCalendar(cal);
        String date = dateFormat.format(cal.getTime());
        widgetDate.setText(date);

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
                    case R.id.itemButton:
                        alphaSeek.setProgress(itemTr);
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

        itemBgSpinner = (Spinner) findViewById(R.id.itemBgSpinner);
        itemBgSpinner.setAdapter(spinnerArrayAdapter);
        itemBgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                itemColor = convertColor(getResources().getColor(WidgetUtils.getColor(i)), itemTr);
                listItemCard.setBackgroundColor(itemColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RadioGroup colorsGroup = (RadioGroup) findViewById(R.id.colorsGroup);
        colorsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioBlack:
                        textColor = getResources().getColor(R.color.blackPrimary);
                        widgetDate.setTextColor(textColor);
                        break;
                    case R.id.radioWhite:
                        textColor = getResources().getColor(R.color.whitePrimary);
                        widgetDate.setTextColor(textColor);
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
                        buttonVoice = R.drawable.ic_mic_black_24dp;
                        buttonSettings = R.drawable.ic_settings_black_24dp;
                        break;
                    case R.id.radioButtonWhite:
                        button = R.drawable.ic_add_white_24dp;
                        tasksCount.setImageResource(button);
                        buttonVoice = R.drawable.ic_mic_white_24dp;
                        buttonSettings = R.drawable.ic_settings_white_24dp;
                }
            }
        });

        RadioGroup colorsItemGroup = (RadioGroup) findViewById(R.id.colorsItemGroup);
        colorsItemGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioItemBlack:
                        itemTextColor = getResources().getColor(R.color.blackPrimary);
                        taskText.setTextColor(itemTextColor);
                        taskNumber.setTextColor(itemTextColor);
                        taskDate.setTextColor(itemTextColor);
                        taskTime.setTextColor(itemTextColor);
                        break;
                    case R.id.radioItemWhite:
                        itemTextColor = getResources().getColor(R.color.whitePrimary);
                        taskText.setTextColor(itemTextColor);
                        taskNumber.setTextColor(itemTextColor);
                        taskDate.setTextColor(itemTextColor);
                        taskTime.setTextColor(itemTextColor);
                        break;
                }
            }
        });

        RadioButton radioBlack = (RadioButton) findViewById(R.id.radioBlack);
        RadioButton radioItemBlack = (RadioButton) findViewById(R.id.radioItemBlack);
        headerButton = (RadioButton) findViewById(R.id.headerButton);
        bodyButton = (RadioButton) findViewById(R.id.bodyButton);
        itemButton = (RadioButton) findViewById(R.id.itemButton);
        RadioButton radioButtonBlack = (RadioButton) findViewById(R.id.radioButtonBlack);
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
