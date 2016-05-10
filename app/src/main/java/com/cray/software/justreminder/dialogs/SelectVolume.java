package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.roboto_views.RoboTextView;

public class SelectVolume extends Activity {

    private RoboTextView radiusValue;
    private SharedPrefs sPrefs;
    private boolean isDark;
    private ImageView volumeImage;
    private int volume, code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(SelectVolume.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.volume_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        code = getIntent().getIntExtra("int", 0);

        sPrefs = new SharedPrefs(SelectVolume.this);
        isDark = cs.isDark();

        radiusValue = (RoboTextView) findViewById(R.id.radiusValue);
        radiusValue.setText(String.valueOf(sPrefs.loadInt(Prefs.VOLUME)));

        volumeImage = (ImageView) findViewById(R.id.volumeImage);

        SeekBar radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        int n = sPrefs.loadInt(Prefs.VOLUME);
        radiusBar.setProgress(n);
        radiusValue.setText(String.valueOf(n));
        setValue(n);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (code == 1) {
                    volume = i;
                } else {
                    sPrefs.saveInt(Prefs.VOLUME, i);
                }
                radiusValue.setText(String.valueOf(i));
                setValue(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.aboutClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (code == 1) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.SELECTED_VOLUME, volume);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
    }

    private void setValue(int i) {
        if (i < 7 && i > 0){
            if (isDark) volumeImage.setImageResource(R.drawable.ic_volume_mute_white_24dp);
            else volumeImage.setImageResource(R.drawable.ic_volume_mute_black_24dp);
        } else if (i > 18){
            if (isDark) volumeImage.setImageResource(R.drawable.ic_volume_up_white_24dp);
            else volumeImage.setImageResource(R.drawable.ic_volume_up_black_24dp);
        } else if (i == 0){
            if (isDark) volumeImage.setImageResource(R.drawable.ic_volume_off_white_24dp);
            else volumeImage.setImageResource(R.drawable.ic_volume_off_black_24dp);
        } else {
            if (isDark) volumeImage.setImageResource(R.drawable.ic_volume_down_white_24dp);
            else volumeImage.setImageResource(R.drawable.ic_volume_down_black_24dp);
        }
    }
}