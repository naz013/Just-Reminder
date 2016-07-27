/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.reminder.DisableAsync;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.roboto_views.RoboTextView;

public class TrackerOption extends Activity {

    private SeekBar radiusBar, timeBar;
    private RoboTextView radiusValue, timeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(TrackerOption.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.tracker_settings_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        radiusValue = (RoboTextView) findViewById(R.id.radiusValue);
        radiusValue.setText(String.format(getString(R.string.x_meters), SharedPrefs.getInstance(this).getInt(Prefs.TRACK_DISTANCE)));

        radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        radiusBar.setMax(149);
        radiusBar.setProgress(SharedPrefs.getInstance(this).getInt(Prefs.TRACK_DISTANCE) - 1);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText(String.format(getString(R.string.x_meters), (i + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timeValue = (RoboTextView) findViewById(R.id.timeValue);
        timeValue.setText(String.format(getString(R.string.x_seconds), SharedPrefs.getInstance(this).getInt(Prefs.TRACK_TIME)));

        timeBar = (SeekBar) findViewById(R.id.timeBar);
        timeBar.setMax(59);
        timeBar.setProgress(SharedPrefs.getInstance(this).getInt(Prefs.TRACK_TIME) - 1);
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

        findViewById(R.id.aboutClose).setOnClickListener(v -> {
            SharedPrefs.getInstance(this).putInt(Prefs.TRACK_DISTANCE, radiusBar.getProgress() + 1);
            SharedPrefs.getInstance(this).putInt(Prefs.TRACK_TIME, timeBar.getProgress() + 1);
            new DisableAsync(TrackerOption.this).execute();
            finish();
        });
    }
}