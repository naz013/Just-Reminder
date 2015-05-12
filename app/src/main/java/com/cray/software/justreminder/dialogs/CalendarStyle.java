package com.cray.software.justreminder.dialogs;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class CalendarStyle extends AppCompatActivity {
    RadioButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox, blue_checkbox, light_blue_checkbox,
            yellow_checkbox, orange_checkbox, grey_checkbox, pink_checkbox, sand_checkbox, brown_checkbox;
    RadioGroup themeGroup;
    SharedPrefs sPrefs;
    ColorSetter cs;
    Toolbar toolbar;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(CalendarStyle.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.theme_color_layout);

        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.theme_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        Intent intent = getIntent();
        i = intent.getIntExtra("type", 1);

        if (i == 2) toolbar.setTitle(getString(R.string.birthdays_color_title));
        else if (i == 3) toolbar.setTitle(getString(R.string.reminders_color_title));
        else toolbar.setTitle(getString(R.string.current_color_title));

        red_checkbox = (RadioButton) findViewById(R.id.red_checkbox);
        violet_checkbox = (RadioButton) findViewById(R.id.violet_checkbox);
        green_checkbox = (RadioButton) findViewById(R.id.green_checkbox);
        light_green_checkbox = (RadioButton) findViewById(R.id.light_green_checkbox);
        blue_checkbox = (RadioButton) findViewById(R.id.blue_checkbox);
        light_blue_checkbox = (RadioButton) findViewById(R.id.light_blue_checkbox);
        yellow_checkbox = (RadioButton) findViewById(R.id.yellow_checkbox);
        orange_checkbox = (RadioButton) findViewById(R.id.orange_checkbox);
        grey_checkbox = (RadioButton) findViewById(R.id.grey_checkbox);
        pink_checkbox = (RadioButton) findViewById(R.id.pink_checkbox);
        sand_checkbox = (RadioButton) findViewById(R.id.sand_checkbox);
        brown_checkbox = (RadioButton) findViewById(R.id.brown_checkbox);

        themeGroup = (RadioGroup) findViewById(R.id.themeGroup);

        themeGroup.clearCheck();
        themeGroup.setOnCheckedChangeListener(listener1);

        setUpRadio();
    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };

    public void setUpRadio(){
        sPrefs = new SharedPrefs(CalendarStyle.this);
        String loaded;
        if (i == 2) loaded = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_BIRTHDAY_COLOR);
        else if (i == 3) loaded = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_REMINDERS_COLOR);
        else loaded = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_CURRENT_COLOR);
        switch (loaded) {
            case "1":
                red_checkbox.setChecked(true);
                break;
            case "2":
                violet_checkbox.setChecked(true);
                break;
            case "3":
                light_green_checkbox.setChecked(true);
                break;
            case "4":
                green_checkbox.setChecked(true);
                break;
            case "5":
                light_blue_checkbox.setChecked(true);
                break;
            case "6":
                blue_checkbox.setChecked(true);
                break;
            case "7":
                yellow_checkbox.setChecked(true);
                break;
            case "8":
                orange_checkbox.setChecked(true);
                break;
            case "9":
                grey_checkbox.setChecked(true);
                break;
            case "10":
                pink_checkbox.setChecked(true);
                break;
            case "11":
                sand_checkbox.setChecked(true);
                break;
            case "12":
                brown_checkbox.setChecked(true);
                break;
            default:
                green_checkbox.setChecked(true);
                break;
        }
    }

    private void themeColorSwitch(int radio){
        switch (radio){
            case R.id.red_checkbox:
                saveColor("1");
                break;
            case R.id.violet_checkbox:
                saveColor("2");
                break;
            case R.id.green_checkbox:
                saveColor("4");
                break;
            case R.id.light_green_checkbox:
                saveColor("3");
                break;
            case R.id.light_blue_checkbox:
                saveColor("5");
                break;
            case R.id.blue_checkbox:
                saveColor("6");
                break;
            case R.id.yellow_checkbox:
                saveColor("7");
                break;
            case R.id.orange_checkbox:
                saveColor("8");
                break;
            case R.id.grey_checkbox:
                saveColor("9");
                break;
            case R.id.pink_checkbox:
                saveColor("10");
                break;
            case R.id.sand_checkbox:
                saveColor("11");
                break;
            case R.id.brown_checkbox:
                saveColor("12");
                break;
        }
    }

    void saveColor(String string) {
        sPrefs = new SharedPrefs(CalendarStyle.this);
        if (i == 2) sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_BIRTHDAY_COLOR, string);
        else if (i == 3) sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_REMINDERS_COLOR, string);
        else sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_CURRENT_COLOR, string);

        new UpdatesHelper(CalendarStyle.this).updateCalendarWidget();
    }

    @Override
    public void onBackPressed() {
        if (new SharedPrefs(CalendarStyle.this).loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)) {
            new Notifier(CalendarStyle.this).recreatePermanent();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (new SharedPrefs(CalendarStyle.this).loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)) {
                    new Notifier(CalendarStyle.this).recreatePermanent();
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}