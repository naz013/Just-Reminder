package com.cray.software.justreminder.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FollowReminder extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private FloatingEditText textField;
    private TextView customDate;
    private TextView customTime;
    private RadioButton typeMessage, typeCall, timeTomorrow, timeNextWorking, timeAfter, timeCustom;
    private Spinner afterTime;
    private CheckBox taskExport;

    private int myHour = 0, customHour = 0;
    private int myMinute = 0, customMinute = 0;
    private int myYear = 0, customYear = 0;
    private int myMonth = 0, customMonth = 0;
    private int myDay = 1, customDay = 1;
    private long tomorrow, nextWork, currTime;

    private boolean is24Hour = true;

    private SharedPrefs sPrefs = new SharedPrefs(FollowReminder.this);
    private GTasksHelper gtx = new GTasksHelper(FollowReminder.this);
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(FollowReminder.this);
        runOnUiThread(new Runnable() {
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        });
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.follow_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        Intent i = getIntent();
        long receivedDate = i.getLongExtra(Constants.SELECTED_RADIUS, 0);
        number = i.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
        String name = Contacts.getContactNameFromNumber(number, FollowReminder.this);

        Calendar c = Calendar.getInstance();
        if (receivedDate != 0) {
            c.setTimeInMillis(receivedDate);
        } else c.setTimeInMillis(System.currentTimeMillis());
        currTime = c.getTimeInMillis();

        textField = (FloatingEditText) findViewById(R.id.textField);
        textField.setHint(getString(R.string.message));

        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        contactInfo.setTypeface(AssetsUtil.getMediumTypeface(this));
        if (name != null && !name.matches("")) {
            contactInfo.setText(SuperUtil.appendString(name, "\n", number));
        } else {
            contactInfo.setText(number);
        }

        taskExport = (CheckBox) findViewById(R.id.taskExport);
        if (gtx.isLinked()){
            taskExport.setVisibility(View.VISIBLE);
        }

        TextView buttonSave = (TextView) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDateTask();
            }
        });
        TextView buttonCancel = (TextView) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFlags();
                finish();
            }
        });

        TextView tomorrowTime = (TextView) findViewById(R.id.tomorrowTime);
        TextView nextWorkingTime = (TextView) findViewById(R.id.nextWorkingTime);
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

        CheckBox exportCheck = (CheckBox) findViewById(R.id.exportCheck);
        exportCheck.setVisibility(View.GONE);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            exportCheck.setVisibility(View.VISIBLE);
        }

        afterTime = (Spinner) findViewById(R.id.afterTime);
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(String.format(getString(R.string.x_minutes), 5));
        spinnerArray.add(String.format(getString(R.string.x_minutes), 10));
        spinnerArray.add(String.format(getString(R.string.x_minutes), 15));
        spinnerArray.add(String.format(getString(R.string.x_minutes), 30));
        spinnerArray.add(String.format(getString(R.string.x_minutes), 45));
        spinnerArray.add(String.format(getString(R.string.x_minutes), 60));
        spinnerArray.add(String.format(getString(R.string.x_hours), 2));
        spinnerArray.add(String.format(getString(R.string.x_hours), 3));
        spinnerArray.add(String.format(getString(R.string.x_hours), 4));
        spinnerArray.add(String.format(getString(R.string.x_hours), 5));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
        afterTime.setAdapter(spinnerArrayAdapter);

        is24Hour = sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);

        //Calculate custom time
        customDate.setText(TimeUtil.dateFormat.format(c.getTime()));
        customTime.setText(TimeUtil.getTime(c.getTime(), is24Hour));
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

        tomorrowTime.setText(TimeUtil.getDateTime(c.getTime(), is24Hour));

        //Calculate next business day time
        if (currDay == Calendar.FRIDAY){
            c.setTimeInMillis(currTime + (1000 * 60 * 60 * 24 * 3));
        } else if (currDay == Calendar.SATURDAY){
            c.setTimeInMillis(currTime + (1000 * 60 * 60 * 24 * 2));
        } else {
            c.setTimeInMillis(currTime + (1000 * 60 * 60 * 24));
        }
        nextWork = c.getTimeInMillis();
        nextWorkingTime.setText(TimeUtil.getDateTime(c.getTime(), is24Hour));
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
            customYear = year;
            customMonth = monthOfYear;
            customDay = dayOfMonth;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            customDate.setText(TimeUtil.dateFormat.format(c.getTime()));
        }
    };

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

            customTime.setText(TimeUtil.getTime(c.getTime(), is24Hour));
        }
    };

    private void saveDateTask(){
        String text = textField.getText().toString().trim();
        if (text.matches("") && typeMessage.isChecked()){
            textField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String type = getType();
        setUpTimes();
        DataBase db = new DataBase(FollowReminder.this);
        db.open();
        SharedPrefs prefs = new SharedPrefs(FollowReminder.this);
        Cursor cf = db.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        db.close();

        long due = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        JsonModel jsonModel = new JsonModel(text, type, categoryId,
                SyncHelper.generateID(), due, due, null, null, null);
        long remId = new DateType(FollowReminder.this, Constants.TYPE_REMINDER).save(jsonModel);
        boolean cal = prefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean stock = prefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        if (cal || stock) {
            ReminderUtils.exportToCalendar(this, text.matches("") ? number : text, due,
                    remId, cal, stock);
        }
        if (gtx.isLinked() && taskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, due, remId);
        }

        removeFlags();
        finish();
    }

    private void setUpTimes() {
        if (timeNextWorking.isChecked()){
            setUpNextBusiness();
        } else if (timeTomorrow.isChecked()){
            setUpTomorrow();
        } else if (timeCustom.isChecked()){
            myDay = customDay;
            myHour = customHour;
            myMinute = customMinute;
            myMonth = customMonth;
            myYear = customYear;
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currTime + (1000 * 60 * getAfterMins(afterTime.getSelectedItemPosition())));
            myHour = c.get(Calendar.HOUR_OF_DAY);
            myMinute = c.get(Calendar.MINUTE);
            myYear = c.get(Calendar.YEAR);
            myMonth = c.get(Calendar.MONTH);
            myDay = c.get(Calendar.DAY_OF_MONTH);
        }
    }

    private String getType() {
        if (typeCall.isChecked()) return Constants.TYPE_CALL;
        else return Constants.TYPE_MESSAGE;
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
                setUpNextBusiness();
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

    private void setUpNextBusiness() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nextWork);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
    }

    private void setUpTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(tomorrow);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
    }
}
