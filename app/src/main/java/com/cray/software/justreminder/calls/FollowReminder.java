/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.calls;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TimePicker;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JAction;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboRadioButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FollowReminder extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private RoboEditText mMessageField;
    private RoboTextView mCustomDateView;
    private RoboTextView mCustomTimeView;
    private RoboRadioButton mMessageRadio, mCallRadio, mTomorrowRadio, mNextWorkingRadio, mAfterRadio, mCustomRadio;
    private Spinner mAfterSpinner;
    private RoboCheckBox mTasksCheck;
    private RoboCheckBox mCalendarCheck;

    private int mHour = 0, mCustomHour = 0;
    private int mMinute = 0, mCustomMinute = 0;
    private int mYear = 0, mCustomYear = 0;
    private int mMonth = 0, mCustomMonth = 0;
    private int mDay = 1, mCustomDay = 1;
    private long mTomorrowTime, mNextWorkTime, mCurrentTime;

    private boolean mIs24Hour = true;
    private boolean mCalendar = true;
    private boolean mStock = true;
    private boolean mTasks = true;

    private GTasksHelper mGoogleTasks = new GTasksHelper(FollowReminder.this);
    private String mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(FollowReminder.this);
        runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.follow_reminder_layout);
        setRequestedOrientation(cs.getRequestOrientation());
        initActionBar();
        setStyles();

        Intent i = getIntent();
        long receivedDate = i.getLongExtra(Constants.SELECTED_RADIUS, 0);
        mNumber = i.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
        String name = Contacts.getNameFromNumber(mNumber, FollowReminder.this);

        Calendar c = Calendar.getInstance();
        if (receivedDate != 0) {
            c.setTimeInMillis(receivedDate);
        } else c.setTimeInMillis(System.currentTimeMillis());
        mCurrentTime = c.getTimeInMillis();

        mMessageField = (RoboEditText) findViewById(R.id.textField);
        mMessageField.setHint(getString(R.string.message));

        RoboTextView contactInfo = (RoboTextView) findViewById(R.id.contactInfo);
        if (name != null && !name.matches("")) {
            contactInfo.setText(SuperUtil.appendString(name, "\n", mNumber));
        } else {
            contactInfo.setText(mNumber);
        }

        RoboTextView tomorrowTime = (RoboTextView) findViewById(R.id.tomorrowTime);
        RoboTextView nextWorkingTime = (RoboTextView) findViewById(R.id.nextWorkingTime);
        mCustomTimeView = (RoboTextView) findViewById(R.id.customTime);
        mCustomDateView = (RoboTextView) findViewById(R.id.customDate);

        mMessageRadio = (RoboRadioButton) findViewById(R.id.typeMessage);
        mCallRadio = (RoboRadioButton) findViewById(R.id.typeCall);
        mCallRadio.setChecked(true);

        mTomorrowRadio = (RoboRadioButton) findViewById(R.id.timeTomorrow);
        mTomorrowRadio.setOnCheckedChangeListener(this);
        mAfterRadio = (RoboRadioButton) findViewById(R.id.timeAfter);
        mAfterRadio.setOnCheckedChangeListener(this);
        mCustomRadio = (RoboRadioButton) findViewById(R.id.timeCustom);
        mCustomRadio.setOnCheckedChangeListener(this);
        mNextWorkingRadio = (RoboRadioButton) findViewById(R.id.timeNextWorking);
        mNextWorkingRadio.setOnCheckedChangeListener(this);
        mTomorrowRadio.setChecked(true);

        mCalendarCheck = (RoboCheckBox) findViewById(R.id.exportCheck);
        mTasksCheck = (RoboCheckBox) findViewById(R.id.taskExport);

        mCalendar = SharedPrefs.getInstance(this).getBoolean(Prefs.EXPORT_TO_CALENDAR);
        mStock = SharedPrefs.getInstance(this).getBoolean(Prefs.EXPORT_TO_STOCK);
        mTasks = mGoogleTasks.isLinked();

        if (mCalendar || mStock){
            mCalendarCheck.setVisibility(View.VISIBLE);
        }

        if (mTasks){
            mTasksCheck.setVisibility(View.VISIBLE);
        }

        if (!mCalendar && !mStock && !mTasks) {
            findViewById(R.id.card5).setVisibility(View.GONE);
        }

        mAfterSpinner = (Spinner) findViewById(R.id.afterTime);
        mAfterSpinner.setAdapter(getAdapter());

        mIs24Hour = SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT);

        //Calculate custom time
        mCustomDateView.setText(TimeUtil.dateFormat.format(c.getTime()));
        mCustomTimeView.setText(TimeUtil.getTime(c.getTime(), mIs24Hour));
        mCustomHour = c.get(Calendar.HOUR_OF_DAY);
        mCustomMinute = c.get(Calendar.MINUTE);
        mCustomYear = c.get(Calendar.YEAR);
        mCustomMonth = c.get(Calendar.MONTH);
        mCustomDay = c.get(Calendar.DAY_OF_MONTH);
        mCustomDateView.setOnClickListener(v -> {
            mCustomRadio.setChecked(true);
            dateDialog();
        });
        mCustomTimeView.setOnClickListener(v -> {
            mCustomRadio.setChecked(true);
            timeDialog().show();
        });

        //Calculate tomorrow time
        int currDay = c.get(Calendar.DAY_OF_WEEK);
        c.setTimeInMillis(c.getTimeInMillis() + (1000 * 60 * 60 * 24));
        mTomorrowTime = c.getTimeInMillis();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        tomorrowTime.setText(TimeUtil.getDateTime(c.getTime(), mIs24Hour));

        //Calculate next business day time
        if (currDay == Calendar.FRIDAY){
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24 * 3));
        } else if (currDay == Calendar.SATURDAY){
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24 * 2));
        } else {
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24));
        }
        mNextWorkTime = c.getTimeInMillis();
        nextWorkingTime.setText(TimeUtil.getDateTime(c.getTime(), mIs24Hour));
    }

    private void setStyles() {
        ColorSetter cs = new ColorSetter(this);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        ((CardView)findViewById(R.id.card1)).setCardBackgroundColor(cs.getCardStyle());
        ((CardView)findViewById(R.id.card2)).setCardBackgroundColor(cs.getCardStyle());
        ((CardView)findViewById(R.id.card3)).setCardBackgroundColor(cs.getCardStyle());
        ((CardView)findViewById(R.id.card4)).setCardBackgroundColor(cs.getCardStyle());
        ((CardView)findViewById(R.id.card5)).setCardBackgroundColor(cs.getCardStyle());
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if (toolbar != null) {
            toolbar.setTitle(R.string.create_task);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
    }

    private SpinnerAdapter getAdapter() {
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
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
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
        new DatePickerDialog(this, myDateCallBack, mYear, mMonth, mDay).show();
    }

    /**
     * Date selection callback.
     */
    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mCustomYear = year;
            mCustomMonth = monthOfYear;
            mCustomDay = dayOfMonth;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            mCustomDateView.setText(TimeUtil.dateFormat.format(c.getTime()));
        }
    };

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, mCustomHour, mCustomMinute, mIs24Hour);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCustomHour = hourOfDay;
            mCustomMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            mCustomTimeView.setText(TimeUtil.getTime(c.getTime(), mIs24Hour));
        }
    };

    /**
     * Save event to DB and enable reminder.
     */
    private void saveDateTask(){
        String text = mMessageField.getText().toString().trim();
        if (text.matches("") && mMessageRadio.isChecked()){
            mMessageField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String type = getType();
        setUpTimes();
        DataBase db = new DataBase(FollowReminder.this);
        db.open();
        Cursor cf = db.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        db.close();

        long due = ReminderUtils.getTime(mDay, mMonth, mYear, mHour, mMinute, 0);
        JAction jAction = new JAction(type, mNumber, -1, null, null);

        int isTasks = -1;
        if (mTasksCheck.getVisibility() == View.VISIBLE) {
            if (mTasksCheck.isChecked()) isTasks = 1;
            else isTasks = 0;
        }

        int isCalendar = -1;
        if (mCalendarCheck.getVisibility() == View.VISIBLE) {
            if (mCalendarCheck.isChecked()) isCalendar = 1;
            else isCalendar = 0;
        }

        JExport jExport = new JExport(isTasks, isCalendar, null);
        JModel jModel = new JModel(text, type, categoryId,
                SyncHelper.generateID(), due, due, null, jAction, jExport);
        long remId = new DateType(FollowReminder.this, Constants.TYPE_REMINDER).save(jModel);

        if (isCalendar == 1) {
            ReminderUtils.exportToCalendar(this, text.matches("") ? mNumber : text, due,
                    remId, mCalendar, mStock);
        }
        if (mTasks && isTasks == 1){
            ReminderUtils.exportToTasks(this, text, due, remId);
        }

        removeFlags();
        finish();
    }

    private void setUpTimes() {
        if (mNextWorkingRadio.isChecked()){
            setUpNextBusiness();
        } else if (mTomorrowRadio.isChecked()){
            setUpTomorrow();
        } else if (mCustomRadio.isChecked()){
            mDay = mCustomDay;
            mHour = mCustomHour;
            mMinute = mCustomMinute;
            mMonth = mCustomMonth;
            mYear = mCustomYear;
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * getAfterMins(mAfterSpinner.getSelectedItemPosition())));
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }
    }

    private String getType() {
        if (mCallRadio.isChecked()) return Constants.TYPE_CALL;
        else return Constants.TYPE_MESSAGE;
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
                if (mTomorrowRadio.isChecked()) {
                    mNextWorkingRadio.setChecked(false);
                    mAfterRadio.setChecked(false);
                    mCustomRadio.setChecked(false);
                }
                setUpTomorrow();
                break;
            case R.id.timeNextWorking:
                if (mNextWorkingRadio.isChecked()) {
                    mTomorrowRadio.setChecked(false);
                    mAfterRadio.setChecked(false);
                    mCustomRadio.setChecked(false);
                }
                setUpNextBusiness();
                break;
            case R.id.timeAfter:
                if (mAfterRadio.isChecked()) {
                    mTomorrowRadio.setChecked(false);
                    mNextWorkingRadio.setChecked(false);
                    mCustomRadio.setChecked(false);
                }
                break;
            case R.id.timeCustom:
                if (mCustomRadio.isChecked()) {
                    mTomorrowRadio.setChecked(false);
                    mNextWorkingRadio.setChecked(false);
                    mAfterRadio.setChecked(false);
                }
                break;
        }
    }

    /**
     * Select time for event in next business day
     */
    private void setUpNextBusiness() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mNextWorkTime);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Set time of event for tomorrow
     */
    private void setUpTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mTomorrowTime);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_follow_menu, menu);
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
