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
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;

public class ExtraPickerDialog extends Activity {

    CheckBox vibrationCheck, voiceCheck, wakeCheck, unlockCheck, repeatCheck, autoCheck;
    SwitchCompat extraSwitch;
    boolean isAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(ExtraPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_select_extra);

        Intent intent = getIntent();
        isAuto = intent.getBooleanExtra("auto", false);
        int[] array = intent.getIntArrayExtra("prefs");
        String type = intent.getStringExtra("type");

        extraSwitch = (SwitchCompat) findViewById(R.id.extraSwitch);
        extraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchIt(isChecked);
                if (isChecked) extraSwitch.setText(R.string.custom);
                else extraSwitch.setText(R.string.default_string);
            }
        });

        autoCheck = (CheckBox) findViewById(R.id.autoCheck);
        vibrationCheck = (CheckBox) findViewById(R.id.vibrationCheck);
        voiceCheck = (CheckBox) findViewById(R.id.voiceCheck);
        wakeCheck = (CheckBox) findViewById(R.id.wakeCheck);
        unlockCheck = (CheckBox) findViewById(R.id.unlockCheck);
        repeatCheck = (CheckBox) findViewById(R.id.repeatCheck);

        if (type.contains(Constants.TYPE_APPLICATION)) autoCheck.setText(R.string.launch_application);

        if (!isAuto) autoCheck.setVisibility(View.GONE);

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
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResult();
            }
        });
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
        if (!isAuto) auto = -1;
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