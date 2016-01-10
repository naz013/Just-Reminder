package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;

public class MarkerStyle extends Activity implements View.OnClickListener{
    private RadioButton red, green, blue, yellow, greenLight, blueLight, cyan, purple,
            amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime;
    private RadioGroup themeGroup, themeGroup2, themeGroup3, themeGroup4;
    private SharedPrefs sPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(MarkerStyle.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.marker_style_layout);

        TextView themeClose = (TextView) findViewById(R.id.themeClose);
        themeClose.setOnClickListener(this);

        red = (RadioButton) findViewById(R.id.redCheck);
        green = (RadioButton) findViewById(R.id.greenCheck);
        blue = (RadioButton) findViewById(R.id.blueCheck);
        yellow = (RadioButton) findViewById(R.id.yellowCheck);
        greenLight = (RadioButton) findViewById(R.id.greenLightCheck);
        blueLight = (RadioButton) findViewById(R.id.blueLightCheck);
        cyan = (RadioButton) findViewById(R.id.cyanCheck);
        purple = (RadioButton) findViewById(R.id.purpleCheck);
        amber = (RadioButton) findViewById(R.id.amberCheck);
        orange = (RadioButton) findViewById(R.id.orangeCheck);
        pink = (RadioButton) findViewById(R.id.pinkCheck);
        teal = (RadioButton) findViewById(R.id.tealCheck);
        deepPurple = (RadioButton) findViewById(R.id.deepPurpleCheck);
        deepOrange = (RadioButton) findViewById(R.id.deepOrangeCheck);
        indigo = (RadioButton) findViewById(R.id.indigoCheck);
        lime = (RadioButton) findViewById(R.id.limeCheck);

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
        int loaded = sPrefs.loadInt(Prefs.MARKER_STYLE);
        if (loaded == 0){
            red.setChecked(true);
        } else if (loaded == 1){
            purple.setChecked(true);
        } else if (loaded == 2){
            greenLight.setChecked(true);
        } else if (loaded == 3){
            green.setChecked(true);
        } else if (loaded == 4){
            blueLight.setChecked(true);
        } else if (loaded == 5){
            blue.setChecked(true);
        } else if (loaded == 6){
            yellow.setChecked(true);
        } else if (loaded == 7){
            orange.setChecked(true);
        } else if (loaded == 8){
            cyan.setChecked(true);
        } else if (loaded == 9){
            pink.setChecked(true);
        } else if (loaded == 10){
            teal.setChecked(true);
        } else if (loaded == 11){
            amber.setChecked(true);
        } else if (loaded == 12){
            deepPurple.setChecked(true);
        } else if (loaded == 13){
            deepOrange.setChecked(true);
        } else if (loaded == 14){
            lime.setChecked(true);
        } else if (loaded == 15){
            indigo.setChecked(true);
        } else {
            blue.setChecked(true);
        }
    }

    private void themeColorSwitch(int radio){
        switch (radio){
            case R.id.redCheck:
                saveColor(0);
                break;
            case R.id.purpleCheck:
                saveColor(1);
                break;
            case R.id.greenLightCheck:
                saveColor(2);
                break;
            case R.id.greenCheck:
                saveColor(3);
                break;
            case R.id.blueLightCheck:
                saveColor(4);
                break;
            case R.id.blueCheck:
                saveColor(5);
                break;
            case R.id.yellowCheck:
                saveColor(6);
                break;
            case R.id.orangeCheck:
                saveColor(7);
                break;
            case R.id.cyanCheck:
                saveColor(8);
                break;
            case R.id.pinkCheck:
                saveColor(9);
                break;
            case R.id.tealCheck:
                saveColor(10);
                break;
            case R.id.amberCheck:
                saveColor(11);
                break;
            case R.id.deepPurpleCheck:
                saveColor(12);
                break;
            case R.id.deepOrangeCheck:
                saveColor(13);
                break;
            case R.id.limeCheck:
                saveColor(14);
                break;
            case R.id.indigoCheck:
                saveColor(15);
                break;
        }
    }

    void saveColor(int style) {
        sPrefs = new SharedPrefs(MarkerStyle.this);
        sPrefs.saveInt(Prefs.MARKER_STYLE, style);
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