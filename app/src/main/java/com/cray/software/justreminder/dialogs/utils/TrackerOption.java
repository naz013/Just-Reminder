package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

public class TrackerOption extends Activity {

    TextView aboutClose;
    SeekBar radiusBar, timeBar;
    TextView radiusValue, timeValue;
    SharedPrefs sPrefs;
    ColorSetter cs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(TrackerOption.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.tracker_settings_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(TrackerOption.this);

        radiusValue = (TextView) findViewById(R.id.radiusValue);
        radiusValue.setText(sPrefs.loadInt(Prefs.TRACK_DISTANCE) + getString(R.string.meter));

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setMax(99);
        radiusBar.setProgress(sPrefs.loadInt(Prefs.TRACK_DISTANCE) - 1);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText((i + 1) + getString(R.string.meter));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timeValue = (TextView) findViewById(R.id.timeValue);
        timeValue.setText(sPrefs.loadInt(Prefs.TRACK_TIME) + getString(R.string.seconds_string));

        timeBar = (SeekBar) findViewById(R.id.timeBar);
        timeBar.setMax(29);
        timeBar.setProgress(sPrefs.loadInt(Prefs.TRACK_TIME) - 1);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                timeValue.setText((i + 1) + getString(R.string.seconds_string));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sPrefs = new SharedPrefs(TrackerOption.this);
                sPrefs.saveInt(Prefs.TRACK_DISTANCE, radiusBar.getProgress() + 1);
                sPrefs.saveInt(Prefs.TRACK_TIME, timeBar.getProgress() + 1);
                finish();
            }
        });
    }
}