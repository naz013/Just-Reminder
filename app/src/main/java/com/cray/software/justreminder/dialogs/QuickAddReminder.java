package com.cray.software.justreminder.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.Utils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class QuickAddReminder extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener{

    DataBase DB;
    ColorSetter cs;
    SyncHelper sHelp;

    LinearLayout by_date_layout, dateRing;
    FloatingEditText task_text;
    EditText repeatDays;
    CheckBox taskExport;
    TextView dateField, timeField, dateYearField;
    SeekBar repeatDateInt;

    int myHour = 0;
    int myMinute = 0;
    int myYear = 0;
    int myMonth = 0;
    int myDay = 1;

    SharedPrefs sPrefs = new SharedPrefs(QuickAddReminder.this);

    UpdatesHelper updatesHelper;
    AlarmReceiver alarm = new AlarmReceiver();
    Typeface typeface;
    Toolbar toolbar;
    FloatingActionButton mFab;
    GTasksHelper gtx = new GTasksHelper(QuickAddReminder.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(QuickAddReminder.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.quick_add_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.add_reminder_dialog_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        task_text = (FloatingEditText) findViewById(R.id.task_text);

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

        dateRing = (LinearLayout) findViewById(R.id.dateRing);
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

        dateField.setText(dayStr + "/" + monthStr);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        dateField.setTypeface(typeface);

        dateYearField = (TextView) findViewById(R.id.dateYearField);
        dateYearField.setText(String.valueOf(myYear));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        dateYearField.setTypeface(typeface);

        timeField = (TextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        timeField.setText(Utils.getTime(c.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        timeField.setTypeface(typeface);

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatDays.setTypeface(typeface);

        repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDateInt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                repeatDays.setText(String.valueOf(getRepeat(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        repeatDays.setText(String.valueOf(getRepeat(repeatDateInt.getProgress())));

        mFab = new FloatingActionButton(QuickAddReminder.this);
        mFab.setColorNormal(cs.colorSetter());
        mFab.setColorPressed(cs.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDateTask();
            }
        });
    }

    private int getRepeat(int progress) {
        return Interval.getRepeatDays(progress);
    }

    protected void dateDialog() {
        final DatePickerDialog datePickerDialog =
                DatePickerDialog.newInstance(this, myYear, myMonth, myDay, false);
        datePickerDialog.setCloseOnSingleTapDay(false);
        datePickerDialog.show(getSupportFragmentManager(), "taa");
    }

    @Override
    public void onDateSet(com.fourmob.datetimepicker.date.DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        myYear = year;
        myMonth = monthOfYear;
        myDay = dayOfMonth;

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        dateField.setText(dayStr + "/" + monthStr);
        dateYearField.setText(String.valueOf(myYear));
    }

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                new SharedPrefs(QuickAddReminder.this).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            timeField.setText(Utils.getTime(c.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
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
        DB = new DataBase(QuickAddReminder.this);
        DB.open();
        sHelp = new SyncHelper(QuickAddReminder.this);
        String uuID = sHelp.generateID();
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        SharedPrefs prefs = new SharedPrefs(QuickAddReminder.this);
        long id;
        if (prefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                prefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) {
            id = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                    repeat, 0, 0, 0, 0, uuID, null, 1, null, 0, 0, 0, categoryId);
            exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
        } else {
            id = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                    repeat, 0, 0, 0, 0, uuID, null, 0, null, 0, 0, 0, categoryId);
        }
        if (gtx.isLinked() && taskExport.isChecked()){
            exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
        }
        DB.updateDateTime(id);
        alarm.setAlarm(QuickAddReminder.this, id);
        DB.close();
        updatesHelper = new UpdatesHelper(QuickAddReminder.this);
        updatesHelper.updateWidget();
        new Notifier(QuickAddReminder.this).recreatePermanent();
        finish();
    }

    private void exportToCalendar(String summary, long startTime, long id){
        SharedPrefs sPrefs = new SharedPrefs(QuickAddReminder.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            new CalendarManager(QuickAddReminder.this)
                    .addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            new CalendarManager(QuickAddReminder.this)
                    .addEventToStock(summary, startTime);
        }
    }

    private void exportToTasks(String summary, long startTime, long id){
        long localId = new TasksData(QuickAddReminder.this).addTask(summary, null, 0, false, startTime,
                null, null, getString(R.string.string_task_from_just_reminder),
                null, null, null, 0, id, null, Constants.TASKS_NEED_ACTION, false);
        new TaskAsync(QuickAddReminder.this, summary, null, null,
                TasksConstants.INSERT_TASK, startTime, getString(R.string.string_task_from_just_reminder), localId).execute();
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
}
