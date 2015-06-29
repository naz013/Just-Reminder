package com.cray.software.justreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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

public class CalendarWidgetConfig extends AppCompatActivity implements
        RadioGroup.OnCheckedChangeListener {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    public final static String CURRENT_WIDGET_PREF = "calendar_pref";
    public final static String CURRENT_WIDGET_COLOR = "calendar_color_";
    public final static String CURRENT_WIDGET_ROW_COLOR = "calendar_row_color_";
    public final static String CURRENT_WIDGET_ITEM_TEXT_COLOR = "calendar_item_text_color_";
    public final static String CURRENT_WIDGET_BORDER_COLOR = "calendar_border_color_";
    public final static String CURRENT_WIDGET_HEADER_COLOR = "calendar_header_color_";
    public final static String CURRENT_WIDGET_LEFT_ARROW_COLOR = "calendar_left_arrow_color_";
    public final static String CURRENT_WIDGET_RIGHT_ARROW_COLOR = "calendar_right_arrow_color_";
    public final static String CURRENT_WIDGET_BUTTON_COLOR = "calendar_button_color_";
    public final static String CURRENT_WIDGET_BUTTON_VOICE_COLOR = "calendar_button_voice_color_";
    public final static String CURRENT_WIDGET_BUTTON_SETTINGS_COLOR = "calendar_button_settings_color_";
    public final static String CURRENT_WIDGET_TITLE_COLOR = "calendar_title_color_";

    public final static String CURRENT_WIDGET_REMINDER_COLOR = "calendar_reminder_color_";
    public final static String CURRENT_WIDGET_BIRTHDAY_COLOR = "calendar_birthday_color_";
    public final static String CURRENT_WIDGET_CURRENT_COLOR = "calendar_current_color_";

    public final static String CURRENT_WIDGET_MONTH = "calendar_month_";
    public final static String CURRENT_WIDGET_YEAR = "calendar_year_";
    int color, title, buttonColor, buttonVoice, buttonSettings, rowColor, itemTextColor,
            leftArrow, rightArrow, headerColor;
    ColorSetter cSetter;

    Toolbar toolbar;

    TextView note, widgetTitle;
    Spinner widgetBgSpinner;
    RadioGroup colorsTitleGroup, colorsButtonGroup, group;
    RadioButton radioTitleBlack, radioButtonBlack, radioButtonWhite, radioTitleWhite, custom, theme;
    ViewPager themePager;
    LinearLayout customContainer;
    MyFragmentPagerAdapter adapter;
    ArrayList<ThemeItem> list;

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
                        headerColor = getResources().getColor(R.color.colorWhite);
                        rowColor = getResources().getColor(R.color.colorWhite);
                        break;
                    case 1:
                        color = getResources().getColor(android.R.color.transparent);
                        headerColor = getResources().getColor(android.R.color.transparent);
                        rowColor = getResources().getColor(android.R.color.transparent);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.colorBlack);
                        headerColor = getResources().getColor(R.color.colorBlack);
                        rowColor = getResources().getColor(R.color.colorBlack);
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
                        itemTextColor = getResources().getColor(R.color.colorBlack);
                        break;
                    case R.id.radioTitleWhite:
                        title = getResources().getColor(R.color.colorWhite);
                        itemTextColor = getResources().getColor(R.color.colorWhite);
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
                        leftArrow = R.drawable.ic_keyboard_arrow_left_grey600_24dp;
                        rightArrow = R.drawable.ic_keyboard_arrow_right_grey600_24dp;
                        break;
                    case R.id.radioButtonWhite:
                        buttonColor = R.drawable.ic_add_white_24dp;
                        buttonVoice = R.drawable.ic_mic_white_24dp;
                        buttonSettings = R.drawable.ic_settings_white_24dp;
                        leftArrow = R.drawable.ic_keyboard_arrow_left_white_24dp;
                        rightArrow = R.drawable.ic_keyboard_arrow_right_white_24dp;
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

        customContainer = (LinearLayout) findViewById(R.id.customContainer);
        themePager = (ViewPager) findViewById(R.id.themePager);
        customContainer.setVisibility(View.GONE);
        themePager.setVisibility(View.GONE);

        loadThemes();

        custom = (RadioButton) findViewById(R.id.custom);
        theme = (RadioButton) findViewById(R.id.theme);

        group = (RadioGroup) findViewById(R.id.group);
        group.setOnCheckedChangeListener(this);
        theme.setChecked(true);
        group.setVisibility(View.GONE);
    }

    private int getColor(int res){
        return getResources().getColor(res);
    }

    private void loadThemes(){
        list = new ArrayList<>();
        list.clear();
        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorWhite,
                R.color.colorSand, R.color.colorGrey,
                getColor(R.color.colorWhite), R.color.colorWhite,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Teal", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorWhite,
                R.color.colorBlueDark, R.color.colorGrey,
                getColor(R.color.colorWhite), R.color.colorWhite,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Blue", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorWhite), R.color.colorBlueGrey,
                R.color.colorGrey900, R.color.colorBlueGrey,
                getColor(R.color.colorWhite), R.color.colorBlueGrey,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Gray", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorWhite,
                R.color.colorGreenDark, R.color.colorGrey,
                getColor(R.color.colorWhite), R.color.colorWhite,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Green", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorWhite), R.color.colorBlack,
                R.color.colorBlack, R.color.colorBlack,
                getColor(R.color.colorWhite), R.color.colorBlack,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Dark", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorWhite,
                R.color.colorWhite, R.color.colorWhite,
                getColor(R.color.colorBlack), R.color.colorWhite,
                R.drawable.simple_left_arrow_black, R.drawable.simple_right_arrow_black,
                R.drawable.simple_plus_button_black, R.drawable.simple_voice_button_black,
                R.drawable.simple_settings_button_black, "White", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorWhite,
                R.color.colorDeepOrangeDark, R.color.colorWhite,
                getColor(R.color.colorWhite), R.color.colorWhite,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Orange", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorWhite,
                R.color.colorRedDark, R.color.colorGrey,
                getColor(R.color.colorWhite), R.color.colorWhite,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Red", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorOrangeLight,
                R.color.colorGrey900, R.color.colorGrey,
                getColor(R.color.colorWhite), R.color.colorWhite,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Simple Black", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorWhite), R.color.simple_transparent_widget_color,
                R.color.simple_transparent_header_color, R.color.simple_transparent_border_color,
                getColor(R.color.colorWhite), R.color.simple_transparent_row_color,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Transparent Light", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.simple_transparent_widget_color,
                R.color.simple_transparent_header_color, R.color.simple_transparent_border_color,
                getColor(R.color.colorBlack), R.color.simple_transparent_row_color,
                R.drawable.simple_left_arrow_black, R.drawable.simple_right_arrow_black,
                R.drawable.simple_plus_button_black, R.drawable.simple_voice_button_black,
                R.drawable.simple_settings_button_black, "Transparent Dark", 0, 0, 0));

        list.add(new ThemeItem(getColor(R.color.colorBlack), R.color.colorOrangeLight,
                R.color.colorBrown, R.color.colorGrey,
                getColor(R.color.colorWhite), R.color.colorOrangeLight,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Simple Brown", 0, 0, 0));

        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), list);
        themePager.setAdapter(adapter);
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
                cal.setTimeInMillis(System.currentTimeMillis());
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                if (group.getCheckedRadioButtonId() == R.id.theme){
                    int position = themePager.getCurrentItem();
                    ThemeItem themeItem = list.get(position);
                    editor.putInt(CURRENT_WIDGET_COLOR + widgetID, themeItem.getWidgetBgColor());
                    editor.putInt(CURRENT_WIDGET_TITLE_COLOR + widgetID, themeItem.getTitleColor());
                    editor.putInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, themeItem.getIconPlus());
                    editor.putInt(CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, themeItem.getIconVoice());
                    editor.putInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, themeItem.getIconSettings());
                    editor.putInt(CURRENT_WIDGET_RIGHT_ARROW_COLOR + widgetID, themeItem.getRightArrow());
                    editor.putInt(CURRENT_WIDGET_LEFT_ARROW_COLOR + widgetID, themeItem.getLeftArrow());
                    editor.putInt(CURRENT_WIDGET_HEADER_COLOR + widgetID, themeItem.getHeaderColor());
                    editor.putInt(CURRENT_WIDGET_BORDER_COLOR + widgetID, themeItem.getBorderColor());
                    editor.putInt(CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, themeItem.getItemTextColor());
                    editor.putInt(CURRENT_WIDGET_ROW_COLOR + widgetID, themeItem.getRowColor());
                    editor.putInt(CURRENT_WIDGET_REMINDER_COLOR + widgetID, themeItem.getReminderMark());
                    editor.putInt(CURRENT_WIDGET_BIRTHDAY_COLOR + widgetID, themeItem.getBirthdayMark());
                    editor.putInt(CURRENT_WIDGET_CURRENT_COLOR + widgetID, themeItem.getCurrentMark());
                } else {
                    editor.putInt(CURRENT_WIDGET_COLOR + widgetID, color);
                    editor.putInt(CURRENT_WIDGET_TITLE_COLOR + widgetID, title);
                    editor.putInt(CURRENT_WIDGET_BUTTON_COLOR + widgetID, buttonColor);
                    editor.putInt(CURRENT_WIDGET_BUTTON_VOICE_COLOR + widgetID, buttonVoice);
                    editor.putInt(CURRENT_WIDGET_BUTTON_SETTINGS_COLOR + widgetID, buttonSettings);
                    editor.putInt(CURRENT_WIDGET_RIGHT_ARROW_COLOR + widgetID, rightArrow);
                    editor.putInt(CURRENT_WIDGET_LEFT_ARROW_COLOR + widgetID, leftArrow);
                    editor.putInt(CURRENT_WIDGET_HEADER_COLOR + widgetID, headerColor);
                    editor.putInt(CURRENT_WIDGET_BORDER_COLOR + widgetID, getColor(R.color.colorGrey));
                    editor.putInt(CURRENT_WIDGET_ITEM_TEXT_COLOR + widgetID, itemTextColor);
                    editor.putInt(CURRENT_WIDGET_ROW_COLOR + widgetID, rowColor);
                    editor.putInt(CURRENT_WIDGET_REMINDER_COLOR + widgetID, 0);
                    editor.putInt(CURRENT_WIDGET_BIRTHDAY_COLOR + widgetID, 0);
                    editor.putInt(CURRENT_WIDGET_CURRENT_COLOR + widgetID, 0);
                }
                editor.putInt(CURRENT_WIDGET_MONTH + widgetID, month);
                editor.putInt(CURRENT_WIDGET_YEAR + widgetID, year);
                editor.commit();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                CalendarWidget.updateWidget(CalendarWidgetConfig.this, appWidgetManager, sp, widgetID);
                setResult(RESULT_OK, resultValue);
                finish();
                return true;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getCheckedRadioButtonId()){
            case R.id.theme:
                customContainer.setVisibility(View.GONE);
                themePager.setVisibility(View.VISIBLE);
                break;
            case R.id.custom:
                customContainer.setVisibility(View.VISIBLE);
                themePager.setVisibility(View.GONE);
                break;
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<ThemeItem> arrayList;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<ThemeItem> list) {
            super(fm);
            this.arrayList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarThemeFragment.newInstance(position, list);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }

    public class ThemeItem implements Parcelable {
        int itemTextColor, widgetBgColor, headerColor, borderColor, titleColor, rowColor;
        int leftArrow, rightArrow, iconPlus, iconVoice, iconSettings;
        int currentMark, birthdayMark, reminderMark;
        String title;

        public ThemeItem(int itemTextColor, int widgetBgColor, int headerColor, int borderColor,
                         int titleColor, int rowColor, int leftArrow, int rightArrow, int iconPlus, int iconVoice,
                         int iconSettings, String title, int currentMark, int birthdayMark, int reminderMark){
            this.itemTextColor = itemTextColor;
            this.widgetBgColor = widgetBgColor;
            this.headerColor = headerColor;
            this.borderColor = borderColor;
            this.titleColor = titleColor;
            this.leftArrow = leftArrow;
            this.rightArrow = rightArrow;
            this.iconPlus = iconPlus;
            this.iconVoice = iconVoice;
            this.iconSettings = iconSettings;
            this.rowColor = rowColor;
            this.title = title;
            this.currentMark = currentMark;
            this.birthdayMark = birthdayMark;
            this.reminderMark = reminderMark;
        }

        public void setCurrentMark(int currentMark){
            this.currentMark = currentMark;
        }

        public int getCurrentMark(){
            return currentMark;
        }

        public void setBirthdayMark(int birthdayMark){
            this.birthdayMark = birthdayMark;
        }

        public int getBirthdayMark(){
            return birthdayMark;
        }

        public void setReminderMark(int reminderMark){
            this.reminderMark = reminderMark;
        }

        public int getReminderMark(){
            return reminderMark;
        }

        public void setItemTextColor(int itemTextColor){
            this.itemTextColor = itemTextColor;
        }

        public int getItemTextColor(){
            return itemTextColor;
        }

        public void setRowColor(int rowColor){
            this.rowColor = rowColor;
        }

        public int getRowColor(){
            return rowColor;
        }

        public void setWidgetBgColor(int widgetBgColor){
            this.widgetBgColor = widgetBgColor;
        }

        public int getWidgetBgColor(){
            return widgetBgColor;
        }

        public void setHeaderColor(int headerColor){
            this.headerColor = headerColor;
        }

        public int getHeaderColor(){
            return headerColor;
        }

        public void setBorderColor(int borderColor){
            this.borderColor = borderColor;
        }

        public int getBorderColor(){
            return borderColor;
        }

        public void setTitleColor(int titleColor){
            this.titleColor = titleColor;
        }

        public int getTitleColor(){
            return titleColor;
        }

        public void setLeftArrow(int leftArrow){
            this.leftArrow = leftArrow;
        }

        public int getLeftArrow(){
            return leftArrow;
        }

        public void setRightArrow(int rightArrow){
            this.rightArrow = rightArrow;
        }

        public int getRightArrow(){
            return rightArrow;
        }

        public void setIconPlus(int iconPlus){
            this.iconPlus = iconPlus;
        }

        public int getIconPlus(){
            return iconPlus;
        }

        public void setIconVoice(int iconVoice){
            this.iconVoice = iconVoice;
        }

        public int getIconVoice(){
            return iconVoice;
        }

        public void setIconSettings(int iconSettings){
            this.iconSettings = iconSettings;
        }

        public int getIconSettings(){
            return iconSettings;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public String getTitle(){
            return title;
        }

        public ThemeItem(Parcel in) {
            super();
            readFromParcel(in);
        }

        public final Parcelable.Creator<ThemeItem> CREATOR = new Parcelable.Creator<ThemeItem>() {
            public ThemeItem createFromParcel(Parcel in) {
                return new ThemeItem(in);
            }

            public ThemeItem[] newArray(int size) {

                return new ThemeItem[size];
            }

        };

        public void readFromParcel(Parcel in) {
            title = in.readString();
            itemTextColor = in.readInt();
            widgetBgColor = in.readInt();
            rowColor = in.readInt();
            borderColor = in.readInt();
            headerColor = in.readInt();
            titleColor = in.readInt();
            leftArrow = in.readInt();
            rightArrow = in.readInt();
            iconPlus = in.readInt();
            iconSettings = in.readInt();
            iconVoice = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeInt(itemTextColor);
            dest.writeInt(widgetBgColor);
            dest.writeInt(rowColor);
            dest.writeInt(borderColor);
            dest.writeInt(headerColor);
            dest.writeInt(titleColor);
            dest.writeInt(leftArrow);
            dest.writeInt(rightArrow);
            dest.writeInt(iconPlus);
            dest.writeInt(iconVoice);
            dest.writeInt(iconSettings);
        }
    }
}
