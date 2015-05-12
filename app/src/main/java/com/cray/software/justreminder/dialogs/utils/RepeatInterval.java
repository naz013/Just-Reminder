package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

public class RepeatInterval extends Activity {

    TextView aboutClose;
    SeekBar radiusBar;
    TextView radiusValue, titleDialog;
    SharedPrefs sPrefs = new SharedPrefs(RepeatInterval.this);
    ColorSetter cs;
    Button minusButton, plusButton;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(RepeatInterval.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.radius_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(RepeatInterval.this);

        Intent intent = getIntent();
        index = intent.getIntExtra(Constants.ITEM_ID_INTENT, 0);

        titleDialog = (TextView) findViewById(R.id.titleDialog);
        titleDialog.setText(getString(R.string.repeat_interval_dialog_title));

        radiusValue = (TextView) findViewById(R.id.radiusValue);
        if (index == 2){
            radiusValue.setText(String.valueOf(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_MISSED_CALL_TIME)));
        } else {
            radiusValue.setText(String.valueOf(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL)));
        }

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setMax(60);
        if (index == 2){
            radiusBar.setProgress(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_MISSED_CALL_TIME));
        } else {
            radiusBar.setProgress(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL));
        }
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText(String.valueOf(i));
                if (index == 2){
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_MISSED_CALL_TIME, i);
                } else {
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL, i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        plusButton = (Button) findViewById(R.id.plusButton);
        plusButton.setVisibility(View.GONE);

        minusButton = (Button) findViewById(R.id.minusButton);
        minusButton.setVisibility(View.GONE);

        aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}