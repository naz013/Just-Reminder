package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

public class MarkerStyle extends Activity implements View.OnClickListener{
    TextView themeClose;
    RadioButton red_flat, green_flat, blue_flat, yellow_flat, red_simple, green_simple, blue_simple, yellow_simple,
            red_round, orange_round, green_round, blue_round;
    RadioGroup themeGroup, themeGroup2, themeGroup3, themeGroup4;
    SharedPrefs sPrefs;
    ColorSetter cs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(MarkerStyle.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.marker_style_layout);

        themeClose = (TextView) findViewById(R.id.themeClose);
        themeClose.setOnClickListener(this);

        red_flat = (RadioButton) findViewById(R.id.red_flat);
        green_flat = (RadioButton) findViewById(R.id.green_flat);
        blue_flat = (RadioButton) findViewById(R.id.blue_flat);
        yellow_flat = (RadioButton) findViewById(R.id.yellow_flat);
        red_simple = (RadioButton) findViewById(R.id.red_simple);
        green_simple = (RadioButton) findViewById(R.id.green_simple);
        blue_simple = (RadioButton) findViewById(R.id.blue_simple);
        yellow_simple = (RadioButton) findViewById(R.id.yellow_simple);
        red_round = (RadioButton) findViewById(R.id.red_round);
        orange_round = (RadioButton) findViewById(R.id.orange_round);
        green_round = (RadioButton) findViewById(R.id.green_round);
        blue_round = (RadioButton) findViewById(R.id.blue_round);

        themeGroup = (RadioGroup) findViewById(R.id.themeGroup);
        themeGroup2 = (RadioGroup) findViewById(R.id.themeGroup2);
        themeGroup3 = (RadioGroup) findViewById(R.id.themeGroup3);
        themeGroup4 = (RadioGroup) findViewById(R.id.themeGroup4);

        themeGroup.clearCheck();
        themeGroup2.clearCheck();
        themeGroup3.clearCheck();
        themeGroup4.clearCheck();
        themeGroup.setOnCheckedChangeListener(listener1);
        themeGroup2.setOnCheckedChangeListener(listener2);
        themeGroup3.setOnCheckedChangeListener(listener3);
        themeGroup4.setOnCheckedChangeListener(listener4);

        setUpRadio();
    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup4.clearCheck();
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup3.clearCheck();
                themeGroup4.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup4.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };

    public void setUpRadio(){
        sPrefs = new SharedPrefs(MarkerStyle.this);
        int loaded = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_MARKER_STYLE);
        if (loaded == 1){
            red_flat.setChecked(true);
        } else if (loaded == 2){
            green_flat.setChecked(true);
        } else if (loaded == 3){
            blue_flat.setChecked(true);
        } else if (loaded == 4){
            yellow_flat.setChecked(true);
        } else if (loaded == 5){
            red_simple.setChecked(true);
        } else if (loaded == 6){
            green_simple.setChecked(true);
        } else if (loaded == 7){
            blue_simple.setChecked(true);
        } else if (loaded == 8){
            yellow_simple.setChecked(true);
        } else if (loaded == 9){
            red_round.setChecked(true);
        } else if (loaded == 10){
            orange_round.setChecked(true);
        } else if (loaded == 11){
            green_round.setChecked(true);
        } else if (loaded == 12){
            blue_round.setChecked(true);
        } else {
            blue_flat.setChecked(true);
        }
    }

    private void themeColorSwitch(int radio){
        switch (radio){
            case R.id.red_flat:
                saveColor(1);
                break;
            case R.id.green_flat:
                saveColor(2);
                break;
            case R.id.blue_flat:
                saveColor(3);
                break;
            case R.id.yellow_flat:
                saveColor(4);
                break;
            case R.id.red_simple:
                saveColor(5);
                break;
            case R.id.green_simple:
                saveColor(6);
                break;
            case R.id.blue_simple:
                saveColor(7);
                break;
            case R.id.yellow_simple:
                saveColor(8);
                break;
            case R.id.red_round:
                saveColor(9);
                break;
            case R.id.orange_round:
                saveColor(10);
                break;
            case R.id.green_round:
                saveColor(11);
                break;
            case R.id.blue_round:
                saveColor(12);
                break;
        }
    }

    void saveColor(int style) {
        sPrefs = new SharedPrefs(MarkerStyle.this);
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_MARKER_STYLE, style);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.themeClose:
                finish();
                break;
        }
    }
}