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
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.json.JExclusion;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExclusionPickerDialog extends Activity implements CompoundButton.OnCheckedChangeListener {

    private RoboCheckBox selectInterval, selectHours;
    private RoboTextView from, to;

    private int fromHour, fromMinute;
    private int toHour, toMinute;
    private ArrayList<ToggleButton> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(ExclusionPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_select_exlusion);

        from = (RoboTextView) findViewById(R.id.from);
        to = (RoboTextView) findViewById(R.id.to);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        fromHour = calendar.get(Calendar.HOUR_OF_DAY);
        fromMinute = calendar.get(Calendar.MINUTE);

        from.setText(getString(R.string.from) + " " + TimeUtil.getTime(calendar.getTime(), true));

        calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_HOUR * 3);

        toHour = calendar.get(Calendar.HOUR_OF_DAY);
        toMinute = calendar.get(Calendar.MINUTE);

        to.setText(getString(R.string.to) + " "  + TimeUtil.getTime(calendar.getTime(), true));
        from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromTime().show();
            }
        });
        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toTime().show();
            }
        });

        RoboButton buttonOk = (RoboButton) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResult();
            }
        });
        initButtons();
        initCheckBoxes();
    }

    private void initCheckBoxes() {
        selectInterval = (RoboCheckBox) findViewById(R.id.selectInterval);
        selectHours = (RoboCheckBox) findViewById(R.id.selectHours);
        selectInterval.setChecked(true);
        selectInterval.setOnCheckedChangeListener(this);
        selectHours.setOnCheckedChangeListener(this);
    }

    private void saveResult() {
        if (selectHours.isChecked()) {
            JExclusion recurrence = new JExclusion();
            List<Integer> list = getSelectedList();
            if(list.size() == 0) {
                Messages.toast(this, getString(R.string.you_dont_select_any_hours));
                return;
            }
            recurrence.addExclusion(list);
            Intent i = new Intent();
            i.putExtra("excl", recurrence.toString());
            setResult(RESULT_OK, i);
            finish();
        }
        if (selectInterval.isChecked()) {
            JExclusion recurrence = new JExclusion();
            recurrence.addExclusion(getHour(fromHour, fromMinute), getHour(toHour, toMinute));
            Intent i = new Intent();
            i.putExtra("excl", recurrence.toString());
            setResult(RESULT_OK, i);
            finish();
        }
    }

    private List<Integer> getSelectedList() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (ToggleButton button : buttons){
            if (button.isChecked()) ids.add(button.getId() - 100);
        }
        return ids;
    }

    private String getHour(int hour, int minute){
        return hour + ":" + minute;
    }

    private void initButtons() {
        ToggleButton zero = (ToggleButton) findViewById(R.id.zero);
        ToggleButton one = (ToggleButton) findViewById(R.id.one);
        ToggleButton two = (ToggleButton) findViewById(R.id.two);
        ToggleButton three = (ToggleButton) findViewById(R.id.three);
        ToggleButton four = (ToggleButton) findViewById(R.id.four);
        ToggleButton five = (ToggleButton) findViewById(R.id.five);
        ToggleButton six = (ToggleButton) findViewById(R.id.six);
        ToggleButton seven = (ToggleButton) findViewById(R.id.seven);
        ToggleButton eight = (ToggleButton) findViewById(R.id.eight);
        ToggleButton nine = (ToggleButton) findViewById(R.id.nine);
        ToggleButton ten = (ToggleButton) findViewById(R.id.ten);
        ToggleButton eleven = (ToggleButton) findViewById(R.id.eleven);
        ToggleButton twelve = (ToggleButton) findViewById(R.id.twelve);
        ToggleButton thirteen = (ToggleButton) findViewById(R.id.thirteen);
        ToggleButton fourteen = (ToggleButton) findViewById(R.id.fourteen);
        ToggleButton fifteen = (ToggleButton) findViewById(R.id.fifteen);
        ToggleButton sixteen = (ToggleButton) findViewById(R.id.sixteen);
        ToggleButton seventeen = (ToggleButton) findViewById(R.id.seventeen);
        ToggleButton eighteen = (ToggleButton) findViewById(R.id.eighteen);
        ToggleButton nineteen = (ToggleButton) findViewById(R.id.nineteen);
        ToggleButton twenty = (ToggleButton) findViewById(R.id.twenty);
        ToggleButton twentyOne = (ToggleButton) findViewById(R.id.twentyOne);
        ToggleButton twentyTwo = (ToggleButton) findViewById(R.id.twentyTwo);
        ToggleButton twentyThree = (ToggleButton) findViewById(R.id.twentyThree);
        if (one != null){
            setId(zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen,
                    fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty, twentyOne,
                    twentyThree, twentyTwo);
        }
        colorify(zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen,
                fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty, twentyOne,
                twentyThree, twentyTwo);
    }

    private void setId(ToggleButton... buttons){
        int i = 100;
        this.buttons = new ArrayList<>();
        for (ToggleButton button : buttons){
            button.setId(i);
            this.buttons.add(button);
            i++;
        }
    }

    private void colorify(ToggleButton... buttons){
        ColorSetter cs = new ColorSetter(this);
        for (ToggleButton button : buttons){
            button.setBackgroundDrawable(cs.toggleDrawable());
        }
    }

    protected Dialog fromTime() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                fromHour = hourOfDay;
                fromMinute = minute;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                from.setText(getString(R.string.from) + " " + TimeUtil.getTime(calendar.getTime(), true));
            }
        }, fromHour, fromMinute, true);
    }

    protected Dialog toTime() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                toHour = hourOfDay;
                toMinute = minute;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                to.setText(getString(R.string.to) + " "  + TimeUtil.getTime(calendar.getTime(), true));
            }
        }, toHour, toMinute, true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.selectInterval:
                if (isChecked) {
                    selectInterval.setChecked(true);
                    selectHours.setChecked(false);
                } else {
                    selectInterval.setChecked(false);
                    selectHours.setChecked(true);
                }
                break;
            case R.id.selectHours:
                if (isChecked) {
                    selectInterval.setChecked(false);
                    selectHours.setChecked(true);
                } else {
                    selectInterval.setChecked(true);
                    selectHours.setChecked(false);
                }
                break;
        }
    }
}