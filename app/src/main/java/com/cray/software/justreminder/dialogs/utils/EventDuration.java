package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

public class EventDuration extends Activity {

    private TextView radiusValue;
    private SharedPrefs sPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(EventDuration.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.radius_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(EventDuration.this);

        TextView titleDialog = (TextView) findViewById(R.id.titleDialog);
        titleDialog.setText(getString(R.string.event_duration_title));

        radiusValue = (TextView) findViewById(R.id.radiusValue);
        radiusValue.setText(String.valueOf(sPrefs.loadInt(Prefs.EVENT_DURATION)));

        SeekBar radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setMax(120);
        radiusBar.setProgress(sPrefs.loadInt(Prefs.EVENT_DURATION));
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText(String.valueOf(i));
                sPrefs.saveInt(Prefs.EVENT_DURATION, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button plusButton = (Button) findViewById(R.id.plusButton);
        plusButton.setVisibility(View.GONE);

        Button minusButton = (Button) findViewById(R.id.minusButton);
        minusButton.setVisibility(View.GONE);

        TextView aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
