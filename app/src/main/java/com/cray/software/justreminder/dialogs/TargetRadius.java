package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;

public class TargetRadius extends Activity implements View.OnTouchListener {

    private SeekBar radiusBar;
    private RoboTextView radiusValue;
    private SharedPrefs sPrefs;
    private int progressInt, i;
    private long touchTime;
    private static final long TRIGGER_TIME = 500;
    private Handler handler = new Handler();

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
        radiusValue = (RoboTextView) findViewById(R.id.radiusValue);
        progressInt = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
        radiusValue.setText(String.format(getString(R.string.radius_x_meters), progressInt));

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setProgress(progressInt);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressInt = i;
                radiusValue.setText(String.format(getString(R.string.radius_x_meters), progressInt));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RoboButton plusButton = (RoboButton) findViewById(R.id.plusButton);
        RoboButton minusButton = (RoboButton) findViewById(R.id.minusButton);
        plusButton.setOnTouchListener(this);
        minusButton.setOnTouchListener(this);

        RoboCheckBox transportCheck = (RoboCheckBox) findViewById(R.id.transportCheck);
        transportCheck.setVisibility(View.VISIBLE);
        if (progressInt > 2000){
            transportCheck.setChecked(true);
        }
        changeMax(transportCheck.isChecked());

        transportCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeMax(isChecked);
            }
        });

        RoboButton aboutClose = (RoboButton) findViewById(R.id.aboutClose);
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

    private void changeMax(boolean isChecked) {
        if (isChecked){
            radiusBar.setMax(5000);
            radiusBar.setProgress(progressInt);
        } else {
            radiusBar.setMax(2000);
            radiusBar.setProgress(progressInt);
        }
    }

    private Runnable plus = new Runnable() {
        @Override
        public void run() {
            radiusBar.setProgress(radiusBar.getProgress() + 1);
            handler.postDelayed(plus, 5);
        }
    };

    private Runnable minus = new Runnable() {
        @Override
        public void run() {
            radiusBar.setProgress(radiusBar.getProgress() - 1);
            handler.postDelayed(minus, 5);
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.minusButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchTime = System.currentTimeMillis();
                    handler.postDelayed(minus, TRIGGER_TIME);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(minus);
                    if (System.currentTimeMillis() - touchTime < TRIGGER_TIME) {
                        radiusBar.setProgress(radiusBar.getProgress() - 1);
                    }
                }
                break;
            case R.id.plusButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchTime = System.currentTimeMillis();
                    handler.postDelayed(plus, TRIGGER_TIME);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(plus);
                    if (System.currentTimeMillis() - touchTime < TRIGGER_TIME) {
                        radiusBar.setProgress(radiusBar.getProgress() + 1);
                    }
                }
                break;
        }
        return false;
    }
}