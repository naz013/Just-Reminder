/**
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimesOfDayActivity extends AppCompatActivity implements View.OnClickListener {

    private RoboTextView nightTime, eveningTime, dayTime, morningTime;
    private int morningHour, morningMinute;
    private int dayHour, dayMinute;
    private int eveningHour, eveningMinute;
    private int nightHour, nightMinute;
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(TimesOfDayActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.times_of_day_layout);

        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.time));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        nightTime = (RoboTextView) findViewById(R.id.nightTime);
        nightTime.setOnClickListener(this);
        eveningTime = (RoboTextView) findViewById(R.id.eveningTime);
        eveningTime.setOnClickListener(this);
        dayTime = (RoboTextView) findViewById(R.id.dayTime);
        dayTime.setOnClickListener(this);
        morningTime = (RoboTextView) findViewById(R.id.morningTime);
        morningTime.setOnClickListener(this);

        String morning = SharedPrefs.getInstance(this).getString(Prefs.TIME_MORNING);
        String day = SharedPrefs.getInstance(this).getString(Prefs.TIME_DAY);
        String evening = SharedPrefs.getInstance(this).getString(Prefs.TIME_EVENING);
        String night = SharedPrefs.getInstance(this).getString(Prefs.TIME_NIGHT);

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
        boolean is24 = SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT);
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
        return new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            morningHour = hourOfDay;
            morningMinute = minute;
            String time = morningHour + ":" + morningMinute;
            SharedPrefs.getInstance(this).putString(Prefs.TIME_MORNING, time);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            morningTime.setText(TimeUtil.getTime(calendar.getTime(),
                    SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
        }, morningHour, morningMinute, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT));
}

    protected Dialog dayDialog() {
        return new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            dayHour = hourOfDay;
            dayMinute = minute;
            String time = dayHour + ":" + dayMinute;
            SharedPrefs.getInstance(this).putString(Prefs.TIME_DAY, time);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            dayTime.setText(TimeUtil.getTime(calendar.getTime(),
                    SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
        }, dayHour, dayMinute, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    protected Dialog nightDialog() {
        return new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            nightHour = hourOfDay;
            nightMinute = minute;
            String time = nightHour + ":" + nightMinute;
            SharedPrefs.getInstance(this).putString(Prefs.TIME_NIGHT, time);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            nightTime.setText(TimeUtil.getTime(calendar.getTime(),
                    SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
        }, nightHour, nightMinute, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    protected Dialog eveningDialog() {
        return new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            eveningHour = hourOfDay;
            eveningMinute = minute;
            String time = eveningHour + ":" + eveningMinute;
            SharedPrefs.getInstance(this).putString(Prefs.TIME_EVENING, time);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            eveningTime.setText(TimeUtil.getTime(calendar.getTime(),
                    SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
        }, eveningHour, eveningMinute, SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT));
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