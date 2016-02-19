package com.cray.software.justreminder.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
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
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.Calendar;

public class QuickAddReminder extends AppCompatActivity {

    private EditText task_text;
    private EditText repeatDays;
    private CheckBox taskExport;
    private TextView dateField, timeField;

    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;

    private SharedPrefs sPrefs = new SharedPrefs(QuickAddReminder.this);
    private GTasksHelper gtx = new GTasksHelper(QuickAddReminder.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(QuickAddReminder.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.quick_add_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        ImageView timeIcon = (ImageView) findViewById(R.id.timeIcon);
        ImageView repeatIcon = (ImageView) findViewById(R.id.repeatIcon);

        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)){
            timeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
            repeatIcon.setImageResource(R.drawable.ic_refresh_white_24dp);
        } else {
            timeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
            repeatIcon.setImageResource(R.drawable.ic_refresh_black_24dp);
        }

        task_text = (EditText) findViewById(R.id.task_text);

        Intent i = getIntent();
        long receivedDate = i.getLongExtra("date", 0);

        taskExport = (CheckBox) findViewById(R.id.taskExport);
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

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        dateField.setText(TimeUtil.getDate(c.getTime()));
        dateField.setTypeface(AssetsUtil.getMediumTypeface(this));

        timeField = (TextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        timeField.setText(TimeUtil.getTime(c.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        timeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        repeatDays.setTypeface(AssetsUtil.getLightTypeface(this));

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
