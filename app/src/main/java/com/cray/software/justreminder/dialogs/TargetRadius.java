package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;

public class TargetRadius extends Activity {

    private SeekBar radiusBar;
    private TextView radiusValue;
    private SharedPrefs sPrefs;
    private int progressInt, i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(TargetRadius.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.radius_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(TargetRadius.this);
        Intent intent = getIntent();
        i = intent.getIntExtra("item", 0);
        radiusValue = (TextView) findViewById(R.id.radiusValue);
        progressInt = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
        radiusValue.setText(progressInt + getString(R.string.meter));

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setProgress(progressInt);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressInt = i;
                radiusValue.setText(progressInt + getString(R.string.meter));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button plusButton = (Button) findViewById(R.id.plusButton);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiusBar.setProgress(progressInt + 1);
            }
        });

        Button minusButton = (Button) findViewById(R.id.minusButton);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiusBar.setProgress(progressInt - 1);
            }
        });

        CheckBox transportCheck = (CheckBox) findViewById(R.id.transportCheck);
        transportCheck.setVisibility(View.VISIBLE);
        if (progressInt > 2000){
            transportCheck.setChecked(true);
        }
        if (transportCheck.isChecked()){
            radiusBar.setMax(5000);
            radiusBar.setProgress(progressInt);
        } else {
            radiusBar.setMax(2000);
            radiusBar.setProgress(progressInt);
        }

        transportCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radiusBar.setMax(5000);
                    radiusBar.setProgress(progressInt);
                } else {
                    radiusBar.setMax(2000);
                    radiusBar.setProgress(progressInt);
                }
            }
        });

        TextView aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (i == 0) {
                    sPrefs = new SharedPrefs(TargetRadius.this);
                    sPrefs.saveInt(Prefs.LOCATION_RADIUS, radiusBar.getProgress());
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.SELECTED_RADIUS, radiusBar.getProgress());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}