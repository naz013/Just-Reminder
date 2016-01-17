package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;

public class TrackerOption extends Activity {

    private SeekBar radiusBar, timeBar;
    private TextView radiusValue, timeValue;
    private SharedPrefs sPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(TrackerOption.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.tracker_settings_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(TrackerOption.this);

        radiusValue = (TextView) findViewById(R.id.radiusValue);
        radiusValue.setText(String.format(getString(R.string.radius_x_meters), sPrefs.loadInt(Prefs.TRACK_DISTANCE)));

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setMax(99);
        radiusBar.setProgress(sPrefs.loadInt(Prefs.TRACK_DISTANCE) - 1);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText(String.format(getString(R.string.radius_x_meters), (i + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timeValue = (TextView) findViewById(R.id.timeValue);
        timeValue.setText(String.format(getString(R.string.x_seconds), sPrefs.loadInt(Prefs.TRACK_TIME)));

        timeBar = (SeekBar) findViewById(R.id.timeBar);
        timeBar.setMax(29);
        timeBar.setProgress(sPrefs.loadInt(Prefs.TRACK_TIME) - 1);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                timeValue.setText(String.format(getString(R.string.x_seconds), (i + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        TextView aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sPrefs = new SharedPrefs(TrackerOption.this);
                sPrefs.saveInt(Prefs.TRACK_DISTANCE, radiusBar.getProgress() + 1);
                sPrefs.saveInt(Prefs.TRACK_TIME, timeBar.getProgress() + 1);
                new DisableAsync(TrackerOption.this).execute();
                finish();
            }
        });
    }
}