package com.cray.software.justreminder.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;

public class QuickAddReminder extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener{

    private EditText task_text;
    private EditText repeatDays;
    private CheckBox taskExport;
    private TextView dateField, timeField, dateYearField;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.quick_add_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.add_reminder_dialog_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        task_text = (EditText) findViewById(R.id.task_text);

        Intent i = getIntent();
        long receivedDate = i.getLongExtra("date", 0);

        taskExport = (CheckBox) findViewById(R.id.taskExport);
        if (gtx.isLinked()){
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

        LinearLayout dateRing = (LinearLayout) findViewById(R.id.dateRing);
        dateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateDialog();
            }
        });

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        dateField.setText(SuperUtil.appendString(dayStr, "/", monthStr));
        dateField.setTypeface(AssetsUtil.getMediumTypeface(this));

        dateYearField = (TextView) findViewById(R.id.dateYearField);
        dateYearField.setText(String.valueOf(myYear));
        dateYearField.setTypeface(AssetsUtil.getThinTypeface(this));

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

    protected void dateDialog() {
        final DatePickerDialog datePickerDialog =
                DatePickerDialog.newInstance(this, myYear, myMonth, myDay, false);
        datePickerDialog.setCloseOnSingleTapDay(false);
        datePickerDialog.show(getSupportFragmentManager(), "taa");
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        myYear = year;
        myMonth = monthOfYear;
        myDay = dayOfMonth;

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        dateField.setText(SuperUtil.appendString(dayStr, "/", monthStr));
        dateYearField.setText(String.valueOf(myYear));
    }

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

    private void saveDateTask(){
        String text = task_text.getText().toString().trim();
        if (text.matches("")){
            task_text.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = Constants.TYPE_REMINDER;
        int repeat = Integer.parseInt(repeatDays.getText().toString().trim());
        DataBase DB = new DataBase(QuickAddReminder.this);
        DB.open();
        String uuID = SyncHelper.generateID();
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long id;
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        boolean isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        if (isCalendar || isStock) {
            id = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                    repeat, 0, 0, 0, 0, uuID, null, 1, null, 0, 0, 0, categoryId);
            ReminderUtils.exportToCalendar(this, text, startTime, id, isCalendar, isStock);
        } else {
            id = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                    repeat, 0, 0, 0, 0, uuID, null, 0, null, 0, 0, 0, categoryId);
        }
        if (gtx.isLinked() && taskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, id);
        }
        DB.updateReminderDateTime(id);
        DB.close();
        new AlarmReceiver().setAlarm(QuickAddReminder.this, id);
        new UpdatesHelper(QuickAddReminder.this).updateWidget();
        new Notifier(QuickAddReminder.this).recreatePermanent();
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
