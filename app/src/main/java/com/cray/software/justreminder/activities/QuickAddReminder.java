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

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.Calendar;

public class QuickAddReminder extends AppCompatActivity {

    private RoboEditText task_text;
    private RoboEditText repeatDays;
    private RoboCheckBox taskExport;
    private RoboTextView dateField, timeField;

    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;

    private SharedPrefs sPrefs = new SharedPrefs(QuickAddReminder.this);
    private GTasksHelper gtx = new GTasksHelper(QuickAddReminder.this);
    private ColorSetter cs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(QuickAddReminder.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.quick_add_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        initActionBar();
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        initIcons();
        task_text = (RoboEditText) findViewById(R.id.task_text);
        Intent i = getIntent();
        long receivedDate = i.getLongExtra("date", 0);

        taskExport = (RoboCheckBox) findViewById(R.id.taskExport);
        if (gtx.isLinked()) {
            taskExport.setVisibility(View.VISIBLE);
        }

        Calendar c = Calendar.getInstance();
        if (receivedDate != 0) {
            c.setTimeInMillis(receivedDate);
        } else {
            c.setTimeInMillis(System.currentTimeMillis());
        }
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);

        dateField = (RoboTextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(v -> dateDialog());
        dateField.setText(TimeUtil.getDate(c.getTime()));

        timeField = (RoboTextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(v -> timeDialog().show());
        timeField.setText(TimeUtil.getTime(c.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));

        repeatDays = (RoboEditText) findViewById(R.id.repeatDays);

        SeekBar repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDateInt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                repeatDays.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        repeatDays.setText(String.valueOf(repeatDateInt.getProgress()));
    }

    private void initIcons() {
        ImageView timeIcon = (ImageView) findViewById(R.id.timeIcon);
        ImageView repeatIcon = (ImageView) findViewById(R.id.repeatIcon);
        if (cs.isDark()){
            timeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
            repeatIcon.setImageResource(R.drawable.ic_refresh_white_24dp);
        } else {
            timeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
            repeatIcon.setImageResource(R.drawable.ic_refresh_black_24dp);
        }
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    /**
     * Show date picker dialog.
     */
    protected void dateDialog() {
        new DatePickerDialog(this, myDateCallBack, myYear, myMonth, myDay).show();
    }

    /**
     * Date selection callback.
     */
    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(myYear, myMonth, myDay);

            dateField.setText(TimeUtil.getDate(calendar.getTime()));
        }
    };

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                new SharedPrefs(QuickAddReminder.this).loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            timeField.setText(TimeUtil.getTime(c.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        }
    };

    private void saveDateTask() {
        String text = task_text.getText().toString().trim();
        if (text.matches("")) {
            task_text.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String type = Constants.TYPE_REMINDER;
        int repeat = Integer.parseInt(repeatDays.getText().toString().trim());
        String categoryId = CategoryModel.getDefault(QuickAddReminder.this);
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        boolean isTasks = gtx.isLinked() && taskExport.isChecked();
        int isCal = isCalendar || isStock ? 1 : 0;
        JExport jExport = new JExport(isTasks ? 1 : 0, isCal, null);
        JRecurrence jRecurrence = new JRecurrence(0, repeat * AlarmManager.INTERVAL_DAY, -1, null, 0);
        JModel jModel = new JModel(text, type, categoryId,
                SyncHelper.generateID(), startTime, startTime, jRecurrence, null, jExport);
        long remId = new DateType(QuickAddReminder.this, Constants.TYPE_REMINDER).save(jModel);
        if (isCalendar || isStock) {
            ReminderUtils.exportToCalendar(this, text, startTime, remId, isCalendar, isStock);
        }
        if (isTasks) {
            ReminderUtils.exportToTasks(this, text, startTime, remId);
        }
        new SharedPrefs(this).saveBoolean(Prefs.REMINDER_CHANGED, true);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveDateTask();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
