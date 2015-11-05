package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.Calendar;

public class ExclusionPickerDialog extends Activity implements CompoundButton.OnCheckedChangeListener {

    private RadioButton selectInterval, selectHours;
    private LinearLayout intervalContainer, hoursContainer;
    private TextView from, to;
    private ToggleButton zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven,
            twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty,
            twentyOne, twentyTwo, twentyThree;

    private int fromHour, fromMinute;
    private int toHour, toMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(ExclusionPickerDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.dialog_select_exlusion);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        fromHour = calendar.get(Calendar.HOUR_OF_DAY);
        fromMinute = calendar.get(Calendar.MINUTE);

        calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_HOUR * 3);

        toHour = calendar.get(Calendar.HOUR_OF_DAY);
        toMinute = calendar.get(Calendar.MINUTE);

        from = (TextView) findViewById(R.id.from);
        to = (TextView) findViewById(R.id.to);
        from.setText("From:" + fromHour + ":" + fromMinute);
        to.setText("to:" + toHour + ":" + toMinute);
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
                Intent i = new Intent();
                //i.putExtra("excl", selectedPosition);
                setResult(RESULT_OK, i);
            }
        });

        initButtons();

        intervalContainer = (LinearLayout) findViewById(R.id.intervalContainer);
        hoursContainer = (LinearLayout) findViewById(R.id.hoursContainer);

        selectInterval = (RadioButton) findViewById(R.id.selectInterval);
        selectHours = (RadioButton) findViewById(R.id.selectHours);
        selectInterval.setChecked(true);
        selectHours.setOnCheckedChangeListener(this);
        selectInterval.setOnCheckedChangeListener(this);
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
        colorify(one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen,
                fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty, twentyOne,
                twentyThree, twentyTwo);
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
                from.setText("From:" + hourOfDay + ":" + minute);
            }
        }, fromHour, fromMinute, true);
    }

    protected Dialog toTime() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                toHour = hourOfDay;
                toMinute = minute;
                to.setText("to:" + hourOfDay + ":" + minute);
            }
        }, toHour, toMinute, true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.selectHours:
                selectInterval.setChecked(false);
                selectHours.setChecked(true);
                if (intervalContainer.getVisibility() == View.VISIBLE) ViewUtils.collapse(intervalContainer);
                ViewUtils.expand(hoursContainer);
                break;
            case R.id.selectInterval:
                selectHours.setChecked(false);
                selectInterval.setChecked(true);
                if (hoursContainer.getVisibility() == View.VISIBLE) ViewUtils.collapse(hoursContainer);
                ViewUtils.expand(intervalContainer);
                break;
        }
    }
}