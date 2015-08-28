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

public class NoteReminderTime extends Activity {

    TextView aboutClose;
    SeekBar radiusBar;
    TextView radiusValue, titleDialog;
    SharedPrefs sPrefs;
    ColorSetter cs;
    Button minusButton, plusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(NoteReminderTime.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.radius_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        sPrefs = new SharedPrefs(NoteReminderTime.this);

        titleDialog = (TextView) findViewById(R.id.titleDialog);
        titleDialog.setText(getString(R.string.note_reminder_time_dialog));

        radiusValue = (TextView) findViewById(R.id.radiusValue);
        radiusValue.setText(String.valueOf(sPrefs.loadInt(Prefs.QUICK_NOTE_REMINDER_TIME))
                + getString(R.string.string_minute));

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setMax(120);
        radiusBar.setProgress(sPrefs.loadInt(Prefs.QUICK_NOTE_REMINDER_TIME));
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText(String.valueOf(i) + getString(R.string.string_minute));
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
                sPrefs.saveInt(Prefs.QUICK_NOTE_REMINDER_TIME, radiusBar.getProgress());
                finish();
            }
        });
    }
}