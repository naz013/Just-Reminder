/*
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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboSwitchCompat;

public class ExtraPickerDialog extends Activity {

    private RoboCheckBox vibrationCheck, voiceCheck, wakeCheck, unlockCheck, repeatCheck, autoCheck;
    private RoboSwitchCompat extraSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(ExtraPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_select_extra);

        Intent intent = getIntent();
        int[] array = intent.getIntArrayExtra("prefs");
        String type = intent.getStringExtra("type");

        extraSwitch = (RoboSwitchCompat) findViewById(R.id.extraSwitch);
        extraSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchIt(isChecked);
            if (isChecked) extraSwitch.setText(R.string.custom);
            else extraSwitch.setText(R.string.default_string);
        });

        autoCheck = (RoboCheckBox) findViewById(R.id.autoCheck);
        vibrationCheck = (RoboCheckBox) findViewById(R.id.vibrationCheck);
        voiceCheck = (RoboCheckBox) findViewById(R.id.voiceCheck);
        wakeCheck = (RoboCheckBox) findViewById(R.id.wakeCheck);
        unlockCheck = (RoboCheckBox) findViewById(R.id.unlockCheck);
        repeatCheck = (RoboCheckBox) findViewById(R.id.repeatCheck);

        if (type.contains(Constants.TYPE_MESSAGE)) {
            autoCheck.setVisibility(View.VISIBLE);
        } else autoCheck.setVisibility(View.GONE);

        if (type.contains(Constants.TYPE_APPLICATION)) {
            autoCheck.setText(R.string.launch_application);
            autoCheck.setVisibility(View.VISIBLE);
        }

        if (array != null) {
            if (array[0] == 1) voiceCheck.setChecked(true);
            if (array[1] == 1) vibrationCheck.setChecked(true);
            if (array[2] == 1) wakeCheck.setChecked(true);
            if (array[3] == 1) unlockCheck.setChecked(true);
            if (array[4] == 1) repeatCheck.setChecked(true);
            if (array[5] == 1) autoCheck.setChecked(true);
        }

        switchIt(extraSwitch.isChecked());

        setChecked(array);

        Button buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(v -> saveResult());
    }

    private void setChecked(int[] array) {
        for (int anArray : array) {
            if (anArray != -1) {
                extraSwitch.setChecked(true);
                break;
            }
        }
    }

    private void switchIt(boolean checked) {
        autoCheck.setEnabled(checked);
        vibrationCheck.setEnabled(checked);
        voiceCheck.setEnabled(checked);
        wakeCheck.setEnabled(checked);
        unlockCheck.setEnabled(checked);
        repeatCheck.setEnabled(checked);
    }

    private int[] getExtra() {
        int voice = voiceCheck.isChecked() ? 1 : 0;
        int vibro = vibrationCheck.isChecked() ? 1 : 0;
        int wake = wakeCheck.isChecked() ? 1 : 0;
        int unlock = unlockCheck.isChecked() ? 1 : 0;
        int repeat = repeatCheck.isChecked() ? 1 : 0;
        int auto = autoCheck.isChecked() ? 1 : 0;
        if (autoCheck.getVisibility() == View.GONE) auto = -1;
        if (!extraSwitch.isChecked()) return new int[]{-1, -1, -1, -1, -1, -1};
        else return new int[]{voice, vibro, wake, unlock, repeat, auto};
    }

    private void saveResult() {
        Intent i = new Intent();
        i.putExtra("prefs", getExtra());
        setResult(RESULT_OK, i);
        finish();
    }
}