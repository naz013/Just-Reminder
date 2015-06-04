package com.cray.software.justreminder.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.modules.ManageModule;

public class ThemerDialog extends AppCompatActivity {

    RadioButton red_checkbox, violet_checkbox, green_checkbox, light_green_checkbox, blue_checkbox, light_blue_checkbox,
            yellow_checkbox, orange_checkbox, grey_checkbox, pink_checkbox, sand_checkbox, brown_checkbox,
            deepPurple, indigoCheckbox, limeCheckbox, deepOrange;
    RadioGroup themeGroup;
    SharedPrefs sPrefs;
    ColorSetter cs;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(ThemerDialog.this);
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

        deepPurple = (RadioButton) findViewById(R.id.deepPurple);
        indigoCheckbox = (RadioButton) findViewById(R.id.indigoCheckbox);
        limeCheckbox = (RadioButton) findViewById(R.id.limeCheckbox);
        deepOrange = (RadioButton) findViewById(R.id.deepOrange);

        themeGroup = (RadioGroup) findViewById(R.id.themeGroup);
        if (new ManageModule().isPro()) {
            deepPurple.setVisibility(View.VISIBLE);
            indigoCheckbox.setVisibility(View.VISIBLE);
            limeCheckbox.setVisibility(View.VISIBLE);
            deepOrange.setVisibility(View.VISIBLE);
        }

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
        sPrefs = new SharedPrefs(ThemerDialog.this);
        String loaded = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_THEME);
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
            case "13":
                deepPurple.setChecked(true);
                break;
            case "14":
                deepOrange.setChecked(true);
                break;
            case "15":
                limeCheckbox.setChecked(true);
                break;
            case "16":
                indigoCheckbox.setChecked(true);
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
            case R.id.deepPurple:
                saveColor("13");
                break;
            case R.id.deepOrange:
                saveColor("14");
                break;
            case R.id.limeCheckbox:
                saveColor("15");
                break;
            case R.id.indigoCheckbox:
                saveColor("16");
                break;
        }
        cs = new ColorSetter(ThemerDialog.this);
        toolbar.setBackgroundColor(cs.colorSetter());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
    }

    void saveColor(String string) {
        sPrefs = new SharedPrefs(ThemerDialog.this);
        sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_THEME, string);
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED, true);
    }

    @Override
    public void onBackPressed() {
        if (new SharedPrefs(ThemerDialog.this).loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)) {
            new Notifier(ThemerDialog.this).recreatePermanent();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (new SharedPrefs(ThemerDialog.this).loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)) {
                    new Notifier(ThemerDialog.this).recreatePermanent();
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}