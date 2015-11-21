package com.cray.software.justreminder.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimesOfDay extends AppCompatActivity implements View.OnClickListener {

    private TextView nightTime, eveningTime, dayTime, morningTime;
    private int morningHour, morningMinute;
    private int dayHour, dayMinute;
    private int eveningHour, eveningMinute;
    private int nightHour, nightMinute;
    private SharedPrefs prefs = new SharedPrefs(TimesOfDay.this);
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(TimesOfDay.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.times_of_day_layout);

        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.settings_voice_time));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        nightTime = (TextView) findViewById(R.id.nightTime);
        nightTime.setOnClickListener(this);
        eveningTime = (TextView) findViewById(R.id.eveningTime);
        eveningTime.setOnClickListener(this);
        dayTime = (TextView) findViewById(R.id.dayTime);
        dayTime.setOnClickListener(this);
        morningTime = (TextView) findViewById(R.id.morningTime);
        morningTime.setOnClickListener(this);

        String morning = prefs.loadPrefs(Prefs.TIME_MORNING);
        String day = prefs.loadPrefs(Prefs.TIME_DAY);
        String evening = prefs.loadPrefs(Prefs.TIME_EVENING);
        String night = prefs.loadPrefs(Prefs.TIME_NIGHT);

        Date date = null;
        try {
            date = format.parse(morning);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) calendar.setTime(date);
        morningHour = calendar.get(Calendar.HOUR_OF_DAY);
        morningMinute = calendar.get(Calendar.MINUTE);
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);
        morningTime.setText(TimeUtil.getTime(calendar.getTime(), is24));

        try {
            date = format.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) calendar.setTime(date);
        dayHour = calendar.get(Calendar.HOUR_OF_DAY);
        dayMinute = calendar.get(Calendar.MINUTE);
        dayTime.setText(TimeUtil.getTime(calendar.getTime(), is24));

        try {
            date = format.parse(evening);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) calendar.setTime(date);
        eveningHour = calendar.get(Calendar.HOUR_OF_DAY);
        eveningMinute = calendar.get(Calendar.MINUTE);
        eveningTime.setText(TimeUtil.getTime(calendar.getTime(), is24));

        try {
            date = format.parse(night);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) calendar.setTime(date);
        nightHour = calendar.get(Calendar.HOUR_OF_DAY);
        nightMinute = calendar.get(Calendar.MINUTE);
        nightTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog morningDialog() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                morningHour = hourOfDay;
                morningMinute = minute;
                String time = morningHour + ":" + morningMinute;
                prefs.savePrefs(Prefs.TIME_MORNING, time);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                morningTime.setText(TimeUtil.getTime(calendar.getTime(),
                        prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            }
        }, morningHour, morningMinute, prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
}

    protected Dialog dayDialog() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                dayHour = hourOfDay;
                dayMinute = minute;
                String time = dayHour + ":" + dayMinute;
                prefs.savePrefs(Prefs.TIME_DAY, time);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                dayTime.setText(TimeUtil.getTime(calendar.getTime(),
                        prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            }
        }, dayHour, dayMinute, prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    protected Dialog nightDialog() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                nightHour = hourOfDay;
                nightMinute = minute;
                String time = nightHour + ":" + nightMinute;
                prefs.savePrefs(Prefs.TIME_NIGHT, time);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                nightTime.setText(TimeUtil.getTime(calendar.getTime(),
                        prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            }
        }, nightHour, nightMinute, prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    protected Dialog eveningDialog() {
        return new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eveningHour = hourOfDay;
                eveningMinute = minute;
                String time = eveningHour + ":" + eveningMinute;
                prefs.savePrefs(Prefs.TIME_EVENING, time);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                eveningTime.setText(TimeUtil.getTime(calendar.getTime(),
                        prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            }
        }, eveningHour, eveningMinute, prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.morningTime:
                morningDialog().show();
                break;
            case R.id.dayTime:
                dayDialog().show();
                break;
            case R.id.eveningTime:
                eveningDialog().show();
                break;
            case R.id.nightTime:
                nightDialog().show();
                break;
        }
    }
}