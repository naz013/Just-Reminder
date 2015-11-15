package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExclusionPickerDialog extends Activity implements CompoundButton.OnCheckedChangeListener {

    private CheckBox selectInterval, selectHours;
    private LinearLayout intervalContainer, hoursContainer;
    private TextView from, to;
    private ToggleButton zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven,
            twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty,
            twentyOne, twentyTwo, twentyThree;

    private int fromHour, fromMinute;
    private int toHour, toMinute;
    private ArrayList<ToggleButton> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(ExclusionPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_select_exlusion);

        from = (TextView) findViewById(R.id.from);
        to = (TextView) findViewById(R.id.to);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        fromHour = calendar.get(Calendar.HOUR_OF_DAY);
        fromMinute = calendar.get(Calendar.MINUTE);

        from.setText(getString(R.string.from_) + " " + TimeUtil.getTime(calendar.getTime(), true));

        calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_HOUR * 3);

        toHour = calendar.get(Calendar.HOUR_OF_DAY);
        toMinute = calendar.get(Calendar.MINUTE);

        to.setText(getString(R.string.to_) + " "  + TimeUtil.getTime(calendar.getTime(), true));
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

        Button buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResult();
            }
        });

        initButtons();

        intervalContainer = (LinearLayout) findViewById(R.id.intervalContainer);
        hoursContainer = (LinearLayout) findViewById(R.id.hoursContainer);

        selectInterval = (CheckBox) findViewById(R.id.selectInterval);
        selectHours = (CheckBox) findViewById(R.id.selectHours);
        selectInterval.setChecked(true);
        selectInterval.setOnCheckedChangeListener(this);
        selectHours.setOnCheckedChangeListener(this);
    }

    private void saveResult() {
        if (selectHours.isChecked()) {
            Recurrence recurrence = new Recurrence();
            List<Integer> list = getSelectedList();
            if(list.size() == 0) {
                Messages.toast(this, "You don't check any hours!");
                return;
            }
            recurrence.addExclusion(list);
            Intent i = new Intent();
            i.putExtra("excl", recurrence.getJsonString());
            setResult(RESULT_OK, i);
            finish();
        }
        if (selectInterval.isChecked()) {
            Recurrence recurrence = new Recurrence();
            recurrence.addExclusion(getHour(fromHour, fromMinute), getHour(toHour, toMinute));
            Intent i = new Intent();
            i.putExtra("excl", recurrence.getJsonString());
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
        zero = (ToggleButton) findViewById(R.id.zero);
        one = (ToggleButton) findViewById(R.id.one);
        two = (ToggleButton) findViewById(R.id.two);
        three = (ToggleButton) findViewById(R.id.three);
        four = (ToggleButton) findViewById(R.id.four);
        five = (ToggleButton) findViewById(R.id.five);
        six = (ToggleButton) findViewById(R.id.six);
        seven = (ToggleButton) findViewById(R.id.seven);
        eight = (ToggleButton) findViewById(R.id.eight);
        nine = (ToggleButton) findViewById(R.id.nine);
        ten = (ToggleButton) findViewById(R.id.ten);
        eleven = (ToggleButton) findViewById(R.id.eleven);
        twelve = (ToggleButton) findViewById(R.id.twelve);
        thirteen = (ToggleButton) findViewById(R.id.thirteen);
        fourteen = (ToggleButton) findViewById(R.id.fourteen);
        fifteen = (ToggleButton) findViewById(R.id.fifteen);
        sixteen = (ToggleButton) findViewById(R.id.sixteen);
        seventeen = (ToggleButton) findViewById(R.id.seventeen);
        eighteen = (ToggleButton) findViewById(R.id.eighteen);
        nineteen = (ToggleButton) findViewById(R.id.nineteen);
        twenty = (ToggleButton) findViewById(R.id.twenty);
        twentyOne = (ToggleButton) findViewById(R.id.twentyOne);
        twentyTwo = (ToggleButton) findViewById(R.id.twentyTwo);
        twentyThree = (ToggleButton) findViewById(R.id.twentyThree);
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
                from.setText(getString(R.string.from_) + " " + TimeUtil.getTime(calendar.getTime(), true));
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
                to.setText(getString(R.string.to_) + " "  + TimeUtil.getTime(calendar.getTime(), true));
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
                    if (hoursContainer.getVisibility() == View.VISIBLE)
                        ViewUtils.hide(hoursContainer);
                    ViewUtils.show(intervalContainer);
                } else {
                    selectInterval.setChecked(false);
                    selectHours.setChecked(true);
                    if (intervalContainer.getVisibility() == View.VISIBLE)
                        ViewUtils.hide(intervalContainer);
                    ViewUtils.show(hoursContainer);
                }
                break;
            case R.id.selectHours:
                if (isChecked) {
                    selectInterval.setChecked(false);
                    selectHours.setChecked(true);
                    if (intervalContainer.getVisibility() == View.VISIBLE)
                        ViewUtils.hide(intervalContainer);
                    ViewUtils.show(hoursContainer);
                } else {
                    selectInterval.setChecked(true);
                    selectHours.setChecked(false);
                    if (hoursContainer.getVisibility() == View.VISIBLE)
                        ViewUtils.hide(hoursContainer);
                    ViewUtils.show(intervalContainer);
                }
                break;
        }
    }
}