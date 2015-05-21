package com.cray.software.justreminder.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FollowReminder extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener, DatePickerDialog.OnDateSetListener {

    DataBase DB;
    ColorSetter cs;
    SyncHelper sHelp;

    FloatingEditText textField;
    TextView buttonSave, buttonCancel, contactInfo, tomorrowTime, nextWorkingTime, customDate, customTime;
    RadioButton typeMessage, typeCall, timeTomorrow, timeNextWorking, timeAfter, timeCustom;
    Spinner afterTime;
    CheckBox exportCheck, taskExport;

    int myHour = 0, customHour = 0;
    int myMinute = 0, customMinute = 0;
    int myYear = 0, customYear = 0;
    int myMonth = 0, customMonth = 0;
    int myDay = 1, customDay = 1;
    int mySeconds = 0;
    long tomorrow, nextWork, currTime;

    boolean is24Hour = true;
    SimpleDateFormat hour24Format = new SimpleDateFormat("HH:mm");
    SimpleDateFormat hour12Format = new SimpleDateFormat("K:mm a");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    SimpleDateFormat full12Format = new SimpleDateFormat("EEE, dd MMMM yyyy, K:mm a");
    SimpleDateFormat full24Format = new SimpleDateFormat("EEE, dd MMMM yyyy, HH:mm");

    SharedPrefs sPrefs = new SharedPrefs(FollowReminder.this);
    GTasksHelper gtx = new GTasksHelper(FollowReminder.this);

    UpdatesHelper updatesHelper;
    AlarmReceiver alarm = new AlarmReceiver();
    Typeface typeface;
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(FollowReminder.this);
        runOnUiThread(new Runnable() {
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        });
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.follow_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        Intent i = getIntent();
        long receivedDate = i.getLongExtra(Constants.SELECTED_RADIUS, 0);
        number = i.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
        Contacts contacts = new Contacts(FollowReminder.this);
        String name = contacts.getContactNameFromNumber(number, FollowReminder.this);

        Calendar c = Calendar.getInstance();
        if (receivedDate != 0) {
            c.setTimeInMillis(receivedDate);
        } else c.setTimeInMillis(System.currentTimeMillis());
        currTime = c.getTimeInMillis();

        textField = (FloatingEditText) findViewById(R.id.textField);
        textField.setHint(getString(R.string.message_field_hint) + getString(R.string.hint_attention));

        contactInfo = (TextView) findViewById(R.id.contactInfo);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        contactInfo.setTypeface(typeface);
        if (name != null && !name.matches("")) {
            contactInfo.setText(name + "\n" + number);
        } else {
            contactInfo.setText(number);
        }

        taskExport = (CheckBox) findViewById(R.id.taskExport);
        if (gtx.isLinked()){
            taskExport.setVisibility(View.VISIBLE);
        }

        buttonSave = (TextView) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDateTask();
            }
        });
        buttonCancel = (TextView) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFlags();
                finish();
            }
        });

        tomorrowTime = (TextView) findViewById(R.id.tomorrowTime);
        nextWorkingTime = (TextView) findViewById(R.id.nextWorkingTime);
        customTime = (TextView) findViewById(R.id.customTime);
        customDate = (TextView) findViewById(R.id.customDate);

        typeMessage = (RadioButton) findViewById(R.id.typeMessage);
        typeCall = (RadioButton) findViewById(R.id.typeCall);
        typeCall.setChecked(true);

        timeTomorrow = (RadioButton) findViewById(R.id.timeTomorrow);
        timeTomorrow.setOnCheckedChangeListener(this);
        timeAfter = (RadioButton) findViewById(R.id.timeAfter);
        timeAfter.setOnCheckedChangeListener(this);
        timeCustom = (RadioButton) findViewById(R.id.timeCustom);
        timeCustom.setOnCheckedChangeListener(this);
        timeNextWorking = (RadioButton) findViewById(R.id.timeNextWorking);
        timeNextWorking.setOnCheckedChangeListener(this);
        timeTomorrow.setChecked(true);

        exportCheck = (CheckBox) findViewById(R.id.exportCheck);
        exportCheck.setVisibility(View.GONE);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
            exportCheck.setVisibility(View.VISIBLE);
        }

        afterTime = (Spinner) findViewById(R.id.afterTime);
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getString(R.string.repeat_5_min));
        spinnerArray.add(getString(R.string.repeat_10_min));
        spinnerArray.add(getString(R.string.repeat_15_min));
        spinnerArray.add(getString(R.string.repeat_30_min));
        spinnerArray.add(getString(R.string.repeat_45_min));
        spinnerArray.add(getString(R.string.repeat_1_hour));
        spinnerArray.add(getString(R.string.repeat_2_hours));
        spinnerArray.add(getString(R.string.repeat_3_hours));
        spinnerArray.add(getString(R.string.repeat_4_hours));
        spinnerArray.add(getString(R.string.repeat_5_hours));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
        afterTime.setAdapter(spinnerArrayAdapter);

        is24Hour = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT);

        //Calculate custom time
        customDate.setText(dateFormat.format(c.getTime()));
        if (is24Hour) customTime.setText(hour24Format.format(c.getTime()));
        else customTime.setText(hour12Format.format(c.getTime()));
        customHour = c.get(Calendar.HOUR_OF_DAY);
        customMinute = c.get(Calendar.MINUTE);
        customYear = c.get(Calendar.YEAR);
        customMonth = c.get(Calendar.MONTH);
        customDay = c.get(Calendar.DAY_OF_MONTH);
        customDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        customTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });

        //Calculate tomorrow time
        int currDay = c.get(Calendar.DAY_OF_WEEK);
        c.setTimeInMillis(c.getTimeInMillis() + (1000 * 60 * 60 * 24));
        tomorrow = c.getTimeInMillis();
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);

        if (is24Hour) tomorrowTime.setText(full24Format.format(c.getTime()));
        else tomorrowTime.setText(full12Format.format(c.getTime()));

        //Calculate next business day time
        if (currDay == Calendar.FRIDAY){
            c.setTimeInMillis(currTime + (1000 * 60 * 60 * 24 * 3));
        } else if (currDay == Calendar.SATURDAY){
            c.setTimeInMillis(currTime + (1000 * 60 * 60 * 24 * 2));
        } else {
            c.setTimeInMillis(currTime + (1000 * 60 * 60 * 24));
        }
        nextWork = c.getTimeInMillis();
        if (is24Hour) nextWorkingTime.setText(full24Format.format(c.getTime()));
        else nextWorkingTime.setText(full12Format.format(c.getTime()));
    }

    private int getAfterMins(int progress) {
        int mins = 0;
        if (progress == 0) mins = 5;
        else if (progress == 1) mins = 10;
        else if (progress == 2) mins = 15;
        else if (progress == 3) mins = 30;
        else if (progress == 4) mins = 45;
        else if (progress == 5) mins = 60;
        else if (progress == 6) mins = 120;
        else if (progress == 7) mins = 180;
        else if (progress == 8) mins = 240;
        else if (progress == 9) mins = 300;
        return mins;
    }

    protected void dateDialog() {
        final DatePickerDialog datePickerDialog =
                DatePickerDialog.newInstance(this, myYear, myMonth, myDay, false);
        datePickerDialog.setCloseOnSingleTapDay(false);
        datePickerDialog.show(getSupportFragmentManager(), "taa");
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        customYear = year;
        customMonth = monthOfYear;
        customDay = dayOfMonth;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        customDate.setText(dateFormat.format(c.getTime()));
    }

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, customHour, customMinute, is24Hour);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            customHour = hourOfDay;
            customMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            if (is24Hour) customTime.setText(hour24Format.format(c.getTime()));
            else customTime.setText(hour12Format.format(c.getTime()));
        }
    };

    private void saveDateTask(){
        String text = textField.getText().toString().trim();
        if (text.matches("") && typeMessage.isChecked()){
            textField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getType();
        setUpTimes();
        DB = new DataBase(FollowReminder.this);
        DB.open();
        sHelp = new SyncHelper(FollowReminder.this);
        String uuID = sHelp.generateID();
        SharedPrefs prefs = new SharedPrefs(FollowReminder.this);
        long id;
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        if (prefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                prefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) {
            id = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, number,
                    0, 0, 0, 0, 0, uuID, null, 1, null, 0, 0, 0, categoryId);
            exportToCalendar(text.matches("") ? number : text, getTime(myDay, myMonth, myYear, myHour, myMinute), id);
        } else {
            id = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, number,
                    0, 0, 0, 0, 0, uuID, null, 0, null, 0, 0, 0, categoryId);
        }
        if (gtx.isLinked() && taskExport.isChecked()){
            exportToTasks(text, getTime(myDay, myMonth, myYear, myHour, myMinute), id);
        }
        DB.updateDateTime(id);
        alarm.setAlarm(FollowReminder.this, id);
        DB.close();
        updatesHelper = new UpdatesHelper(FollowReminder.this);
        updatesHelper.updateWidget();
        new Notifier(FollowReminder.this).recreatePermanent();
        removeFlags();
        finish();
    }

    private void setUpTimes() {
        if (timeNextWorking.isChecked()){
            setUpNextBisiness();
        } else if (timeTomorrow.isChecked()){
            setUpTomorrow();
        } else if (timeCustom.isChecked()){
            myDay = customDay;
            myHour = customHour;
            myMinute = customMinute;
            myMonth = customMonth;
            myYear = customYear;
            mySeconds = 0;
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currTime + (1000 * 60 * getAfterMins(afterTime.getSelectedItemPosition())));
            myHour = c.get(Calendar.HOUR_OF_DAY);
            myMinute = c.get(Calendar.MINUTE);
            myYear = c.get(Calendar.YEAR);
            myMonth = c.get(Calendar.MONTH);
            myDay = c.get(Calendar.DAY_OF_MONTH);
            mySeconds = c.get(Calendar.SECOND);
        }
    }

    private String getType() {
        if (typeCall.isChecked()) return Constants.TYPE_CALL;
        else return Constants.TYPE_MESSAGE;
    }

    private void exportToCalendar(String summary, long startTime, long id){
        SharedPrefs sPrefs = new SharedPrefs(FollowReminder.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            new CalendarManager(FollowReminder.this)
                    .addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            new CalendarManager(FollowReminder.this)
                    .addEventToStock(summary, startTime);
        }
    }

    private void exportToTasks(String summary, long startTime, long id){
        new TaskAsync(FollowReminder.this, summary, null, null,
                TasksConstants.INSERT_TASK, startTime, getString(R.string.string_task_from_just_reminder), 0).execute();
    }

    private long getTime(int day, int month, int year, int hour, int minute){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTimeInMillis();
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

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.timeTomorrow:
                if (timeTomorrow.isChecked()) {
                    timeNextWorking.setChecked(false);
                    timeAfter.setChecked(false);
                    timeCustom.setChecked(false);
                }
                setUpTomorrow();
                break;
            case R.id.timeNextWorking:
                if (timeNextWorking.isChecked()) {
                    timeTomorrow.setChecked(false);
                    timeAfter.setChecked(false);
                    timeCustom.setChecked(false);
                }
                setUpNextBisiness();
                break;
            case R.id.timeAfter:
                if (timeAfter.isChecked()) {
                    timeTomorrow.setChecked(false);
                    timeNextWorking.setChecked(false);
                    timeCustom.setChecked(false);
                }
                break;
            case R.id.timeCustom:
                if (timeCustom.isChecked()) {
                    timeTomorrow.setChecked(false);
                    timeNextWorking.setChecked(false);
                    timeAfter.setChecked(false);
                }
                break;
        }
    }

    private void setUpNextBisiness() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nextWork);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
        mySeconds = 0;
    }

    private void setUpTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(tomorrow);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
        mySeconds = 0;
    }
}
