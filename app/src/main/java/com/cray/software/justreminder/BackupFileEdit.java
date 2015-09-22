package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.dialogs.utils.ContactsList;
import com.cray.software.justreminder.fragments.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BackupFileEdit extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, DatePickerDialog.OnDateSetListener, CompoundButton.OnCheckedChangeListener,
        MapListener {

    private LinearLayout call_layout, by_date_layout, after_time_layout, geolocationlayout, message_layout,
            weekday_layout, action_layout, skype_layout, application_layout, monthDayLayout;
    private FloatingEditText phoneNumber, messageNumber, weekPhoneNumber;
    private TextView callDate, callTime, dateField, timeField, callYearDate, dateYearField,
            messageDate, messageYearDate, messageTime, weekTimeField;
    private ImageButton addNumberButton, addMessageNumberButton, weekAddNumberButton;
    private SeekBar repeatCallInt, repeatDateInt, repeatMessageInt;

    private LinearLayout delayLayout;
    private CheckBox attackDelay;

    private MapFragment map;

    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private String uuID = "";

    private ProgressDialog pd;
    private DataBase DB = new DataBase(BackupFileEdit.this);
    private FilesDataBase fdb = new FilesDataBase(BackupFileEdit.this);

    private AlarmReceiver alarm = new AlarmReceiver();
    private boolean isGPSEnabled = false, isNetworkEnabled = false;
    protected LocationManager locationManager;

    private ColorSetter cSetter = new ColorSetter(BackupFileEdit.this);
    private SharedPrefs sPrefs = new SharedPrefs(BackupFileEdit.this);
    private GTasksHelper gtx = new GTasksHelper(BackupFileEdit.this);

    private long id;
    private String type, selectedPackage = null;
    private Toolbar toolbar;
    private FloatingEditText taskField;

    private  static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private boolean isAnimation = false, isCalendar = false, isStock = false, isDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(BackupFileEdit.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.create_edit_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        isAnimation = sPrefs.loadBoolean(Prefs.ANIMATIONS);
        isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        taskField = (FloatingEditText) findViewById(R.id.task_message);
        Spinner spinner = (Spinner) findViewById(R.id.navSpinner);
        spinner.setVisibility(View.INVISIBLE);

        toolbar.setVisibility(View.GONE);

        if (isAnimation) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    toolbar.startAnimation(slide);
                    toolbar.setVisibility(View.VISIBLE);
                }
            }, 500);
        } else toolbar.setVisibility(View.VISIBLE);

        LinearLayout layoutContainer = (LinearLayout) findViewById(R.id.layoutContainer);
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        map = new MapFragment();
        map.enableTouch(true);
        map.setListener(this);

        ImageButton insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        insertVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);

        clearForm();

        if (id != 0){
            fdb.open();
            Cursor c = fdb.getTask(id);
            if (c != null && c.moveToNext()) {
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            }
            if (c != null) c.close();
            if (type.matches(Constants.TYPE_REMINDER)) {
                attachDateReminder();
            } else if (type.matches(Constants.TYPE_TIME)){
                attachTimeReminder();
            } else if (type.matches(Constants.TYPE_CALL)){
                attachCall();
            } else if (type.matches(Constants.TYPE_MESSAGE)){
                attachMessage();
            } else if (type.startsWith(Constants.TYPE_LOCATION)){
                if (LocationUtil.checkGooglePlayServicesAvailability(BackupFileEdit.this)) {
                    attachLocation();
                } else {
                    Toast.makeText(BackupFileEdit.this, getString(R.string.play_services_check_error), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
                if (LocationUtil.checkGooglePlayServicesAvailability(BackupFileEdit.this)) {
                    attachLocationOut();
                } else {
                    Toast.makeText(BackupFileEdit.this, getString(R.string.play_services_check_error), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (type.startsWith(Constants.TYPE_WEEKDAY)){
                attachWeekDayReminder();
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                attachSkype();
            } else if (type.startsWith(Constants.TYPE_APPLICATION)){
                attachApplication();
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                attachMonthDay();
            } else {
                Toast.makeText(BackupFileEdit.this, getString(R.string.file_error_message), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SharedPrefs sPrefs = new SharedPrefs(BackupFileEdit.this);
        if (!sPrefs.loadBoolean(Prefs.AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sPrefs.loadPrefs(Prefs.VOICE_LANGUAGE));
        } else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_say_something));
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e){
            Toast t = Toast.makeText(getApplicationContext(),
                    getString(R.string.recognizer_not_found_error_message),
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    private CheckBox dateTaskExport;
    private EditText repeatDays;

    private void attachDateReminder(){
        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        ViewUtils.fadeInAnimation(by_date_layout, isAnimation);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        LinearLayout dateRing = (LinearLayout) findViewById(R.id.dateRing);
        dateRing.setOnClickListener(this);

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        dateTaskExport = (CheckBox) findViewById(R.id.dateTaskExport);
        if (gtx.isLinked()){
            dateTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        dateField.setText(dayStr + "/" + monthStr);
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
        timeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        timeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        repeatDays.setTypeface(AssetsUtil.getLightTypeface(this));

        repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setOnSeekBarChangeListener(this);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDays.setText(String.valueOf(repeatDateInt.getProgress()));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "";
            int repCode=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (c != null) c.close();

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            timeField.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            dateField.setText(dayStr + "/" + monthStr);
            dateYearField.setText(String.valueOf(myYear));
            repeatDateInt.setProgress(repCode);
            repeatDays.setText(String.valueOf(repCode));
        }
    }

    private CheckBox monthDayTaskExport;
    private CheckBox monthDayAttachAction;
    private LinearLayout monthDayActionLayout;
    private TextView monthDayField, monthDayTimeField;
    private RadioButton monthDayCallCheck, monthDayMessageCheck, dayCheck, lastCheck;
    private ImageButton monthDayAddNumberButton;
    private FloatingEditText monthDayPhoneNumber;

    private void attachMonthDay(){
        taskField.setHint(getString(R.string.tast_hint));

        monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        ViewUtils.fadeInAnimation(monthDayLayout, isAnimation);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(Calendar.YEAR, myYear);
            cal.set(Calendar.MONTH, myMonth);
            cal.set(Calendar.DAY_OF_MONTH, myDay);
            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        monthDayField = (TextView) findViewById(R.id.monthDayField);
        monthDayField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        CheckBox monthDayExport = (CheckBox) findViewById(R.id.monthDayExport);
        if ((isCalendar || isStock)){
            monthDayExport.setVisibility(View.VISIBLE);
        }

        monthDayTaskExport = (CheckBox) findViewById(R.id.monthDayTaskExport);
        if (gtx.isLinked()){
            monthDayTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        if (myDay > 28) myDay = 28;
        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        monthDayField.setText(dayStr);
        monthDayField.setTypeface(AssetsUtil.getMediumTypeface(this));

        monthDayTimeField = (TextView) findViewById(R.id.monthDayTimeField);
        monthDayTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        monthDayTimeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        monthDayTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        monthDayActionLayout = (LinearLayout) findViewById(R.id.monthDayActionLayout);
        monthDayActionLayout.setVisibility(View.GONE);

        dayCheck = (RadioButton) findViewById(R.id.dayCheck);
        dayCheck.setChecked(true);
        lastCheck = (RadioButton) findViewById(R.id.lastCheck);
        dayCheck.setOnCheckedChangeListener(this);
        lastCheck.setOnCheckedChangeListener(this);

        monthDayAttachAction = (CheckBox) findViewById(R.id.monthDayAttachAction);
        monthDayAttachAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (isAnimation) {
                        ViewUtils.expand(monthDayActionLayout);
                    } else action_layout.setVisibility(View.VISIBLE);
                    monthDayAddNumberButton = (ImageButton) findViewById(R.id.monthDayAddNumberButton);
                    monthDayAddNumberButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(BackupFileEdit.this, null, getString(R.string.load_contats), true);
                            pickContacts(pd);
                        }
                    });
                    ViewUtils.setImage(monthDayAddNumberButton, isDark);

                    monthDayPhoneNumber = (FloatingEditText) findViewById(R.id.monthDayPhoneNumber);

                    monthDayCallCheck = (RadioButton) findViewById(R.id.monthDayCallCheck);
                    monthDayCallCheck.setChecked(true);
                    monthDayMessageCheck = (RadioButton) findViewById(R.id.monthDayMessageCheck);
                    monthDayMessageCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) taskField.setHint(getString(R.string.message_field_hint));
                            else taskField.setHint(getString(R.string.tast_hint));
                        }
                    });
                } else {
                    if (isAnimation) {
                        ViewUtils.collapse(monthDayActionLayout);
                    } else monthDayActionLayout.setVisibility(View.GONE);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "";
            String number = "";
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

            if (exp == 1){
                monthDayExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                monthDayTaskExport.setChecked(true);
            }

            if (myDay == 0) myDay = 1;
            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            monthDayTimeField.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            monthDayField.setText(dayStr);

            if (type.matches(Constants.TYPE_MONTHDAY)){
                monthDayAttachAction.setChecked(false);
                dayCheck.setChecked(true);
            } else if(type.matches(Constants.TYPE_MONTHDAY_LAST)){
                monthDayAttachAction.setChecked(false);
                lastCheck.setChecked(true);
            } else {
                monthDayAttachAction.setChecked(true);
                monthDayPhoneNumber = (FloatingEditText) findViewById(R.id.monthDayPhoneNumber);
                monthDayPhoneNumber.setText(number);
                if (type.matches(Constants.TYPE_MONTHDAY_CALL_LAST) ||
                        type.matches(Constants.TYPE_MONTHDAY_MESSAGE_LAST)){
                    lastCheck.setChecked(true);
                } else dayCheck.setChecked(true);
                if (type.matches(Constants.TYPE_MONTHDAY_CALL)){
                    monthDayCallCheck = (RadioButton) findViewById(R.id.monthDayCallCheck);
                    monthDayCallCheck.setChecked(true);
                } else {
                    monthDayMessageCheck = (RadioButton) findViewById(R.id.monthDayMessageCheck);
                    monthDayMessageCheck.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.dayCheck:
                if (dayCheck.isChecked()) {
                    lastCheck.setChecked(false);
                    ViewUtils.expand(monthDayField);
                    myDay = 1;
                }
                break;
            case R.id.lastCheck:
                if (lastCheck.isChecked()) {
                    dayCheck.setChecked(false);
                    ViewUtils.collapse(monthDayField);
                    myDay = 0;
                }
                break;
            case R.id.currentCheck:
                if (currentCheck.isChecked()) {
                    mapCheck.setChecked(false);
                    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    mLocList = new CurrentLocation();
                    SharedPrefs prefs = new SharedPrefs(getApplicationContext());
                    long time;
                    time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
                    int distance;
                    distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
                }
                break;
            case R.id.mapCheck:
                if (mapCheck.isChecked()) {
                    currentCheck.setChecked(false);
                    ViewUtils.fadeOutAnimation(specsContainerOut, isAnimation);
                    ViewUtils.fadeInAnimation(mapContainerOut, isAnimation);
                    if (mLocList != null) mLocationManager.removeUpdates(mLocList);
                }
                break;
        }
    }

    private ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck, fridayCheck, saturdayCheck, sundayCheck;
    private RadioButton callCheck, messageCheck;
    private CheckBox attachAction, weekTaskExport;

    private void attachWeekDayReminder(){
        cSetter = new ColorSetter(BackupFileEdit.this);
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        ViewUtils.fadeInAnimation(weekday_layout, isAnimation);

        final Calendar c = Calendar.getInstance();
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);

        weekTaskExport = (CheckBox) findViewById(R.id.weekTaskExport);
        if (gtx.isLinked()){
            weekTaskExport.setVisibility(View.VISIBLE);
        }

        weekTimeField = (TextView) findViewById(R.id.weekTimeField);
        weekTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        weekTimeField.setText(TimeUtil.getTime(c.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        weekTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        mondayCheck = (ToggleButton) findViewById(R.id.mondayCheck);
        tuesdayCheck = (ToggleButton) findViewById(R.id.tuesdayCheck);
        wednesdayCheck = (ToggleButton) findViewById(R.id.wednesdayCheck);
        thursdayCheck = (ToggleButton) findViewById(R.id.thursdayCheck);
        fridayCheck = (ToggleButton) findViewById(R.id.fridayCheck);
        saturdayCheck = (ToggleButton) findViewById(R.id.saturdayCheck);
        sundayCheck = (ToggleButton) findViewById(R.id.sundayCheck);
        mondayCheck.setBackgroundDrawable(cSetter.toggleDrawable());
        tuesdayCheck.setBackgroundDrawable(cSetter.toggleDrawable());
        wednesdayCheck.setBackgroundDrawable(cSetter.toggleDrawable());
        thursdayCheck.setBackgroundDrawable(cSetter.toggleDrawable());
        fridayCheck.setBackgroundDrawable(cSetter.toggleDrawable());
        saturdayCheck.setBackgroundDrawable(cSetter.toggleDrawable());
        sundayCheck.setBackgroundDrawable(cSetter.toggleDrawable());

        action_layout = (LinearLayout) findViewById(R.id.action_layout);
        action_layout.setVisibility(View.GONE);

        attachAction = (CheckBox) findViewById(R.id.attachAction);
        attachAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    action_layout.setVisibility(View.VISIBLE);
                    weekAddNumberButton = (ImageButton) findViewById(R.id.weekAddNumberButton);
                    weekAddNumberButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                            pickContacts(pd);
                        }
                    });
                    ViewUtils.setImage(weekAddNumberButton, isDark);

                    weekPhoneNumber = (FloatingEditText) findViewById(R.id.weekPhoneNumber);

                    callCheck = (RadioButton) findViewById(R.id.callCheck);
                    callCheck.setChecked(true);
                    messageCheck = (RadioButton) findViewById(R.id.messageCheck);
                    messageCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) taskField.setHint(getString(R.string.message_field_hint));
                            else taskField.setHint(getString(R.string.tast_hint));
                        }
                    });
                } else {
                    action_layout.setVisibility(View.GONE);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (id != 0) {
            fdb.open();
            Cursor x = fdb.getTask(id);
            String text = "";
            String type = "";
            String weekdays = "";
            String number = "";
            if (x != null && x.moveToFirst()) {
                myHour = x.getInt(x.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = x.getInt(x.getColumnIndex(Constants.COLUMN_MINUTE));
                text = x.getString(x.getColumnIndex(Constants.COLUMN_TEXT));
                type = x.getString(x.getColumnIndex(Constants.COLUMN_TYPE));
                weekdays = x.getString(x.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                number = x.getString(x.getColumnIndex(Constants.COLUMN_NUMBER));
                uuID = x.getString(x.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (x != null) x.close();

            c.set(Calendar.HOUR_OF_DAY, myHour);
            c.set(Calendar.MINUTE, myMinute);

            weekTimeField.setText(TimeUtil.getTime(c.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            taskField.setText(text);

            setCheckForDays(weekdays);

            if (type.matches(Constants.TYPE_WEEKDAY)){
                attachAction.setChecked(false);
            } else {
                attachAction.setChecked(true);
                weekPhoneNumber = (FloatingEditText) findViewById(R.id.weekPhoneNumber);
                weekPhoneNumber.setText(number);
                if (type.matches(Constants.TYPE_WEEKDAY_CALL)){
                    callCheck = (RadioButton) findViewById(R.id.callCheck);
                    callCheck.setChecked(true);
                } else {
                    messageCheck = (RadioButton) findViewById(R.id.messageCheck);
                    messageCheck.setChecked(true);
                }
            }
        }
    }

    private void setCheckForDays(String weekdays){
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECKED))
            mondayCheck.setChecked(true);
        else mondayCheck.setChecked(false);

        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECKED))
            tuesdayCheck.setChecked(true);
        else tuesdayCheck.setChecked(false);

        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECKED))
            wednesdayCheck.setChecked(true);
        else wednesdayCheck.setChecked(false);

        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECKED))
            thursdayCheck.setChecked(true);
        else thursdayCheck.setChecked(false);

        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECKED))
            fridayCheck.setChecked(true);
        else fridayCheck.setChecked(false);

        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECKED))
            saturdayCheck.setChecked(true);
        else saturdayCheck.setChecked(false);

        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECKED))
            sundayCheck.setChecked(true);
        else sundayCheck.setChecked(false);
    }

    private CheckBox timeTaskExport;
    private TextView hoursView, minutesView, secondsView;
    private ImageButton deleteButton;
    private EditText repeatMinutes;
    private String timeString = "000000";

    private void attachTimeReminder(){
        cSetter = new ColorSetter(BackupFileEdit.this);
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        ViewUtils.fadeInAnimation(after_time_layout, isAnimation);

        timeTaskExport = (CheckBox) findViewById(R.id.timeTaskExport);
        if (gtx.isLinked()){
            timeTaskExport.setVisibility(View.VISIBLE);
        }

        hoursView = (TextView) findViewById(R.id.hoursView);
        minutesView = (TextView) findViewById(R.id.minutesView);
        secondsView = (TextView) findViewById(R.id.secondsView);
        ViewUtils.setTypeFont(this, hoursView, minutesView, secondsView);

        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        sPrefs = new SharedPrefs(this);
        if (isDark)
            deleteButton.setImageResource(R.drawable.ic_backspace_white_24dp);
        else deleteButton.setImageResource(R.drawable.ic_backspace_grey600_24dp);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeString = timeString.substring(0, timeString.length() - 1);
                timeString = "0" + timeString;
                updateTimeView();
            }
        });
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                timeString = "000000";
                updateTimeView();
                return true;
            }
        });

        updateTimeView();

        Button b1 = (Button) findViewById(R.id.b1);
        Button b2 = (Button) findViewById(R.id.b2);
        Button b3 = (Button) findViewById(R.id.b3);
        Button b4 = (Button) findViewById(R.id.b4);
        Button b5 = (Button) findViewById(R.id.b5);
        Button b6 = (Button) findViewById(R.id.b6);
        Button b7 = (Button) findViewById(R.id.b7);
        Button b8 = (Button) findViewById(R.id.b8);
        Button b9 = (Button) findViewById(R.id.b9);
        Button b0 = (Button) findViewById(R.id.b0);
        b1.setId(101);
        b2.setId(102);
        b3.setId(103);
        b4.setId(104);
        b5.setId(105);
        b6.setId(106);
        b7.setId(107);
        b8.setId(108);
        b9.setId(109);
        b0.setId(100);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        b5.setOnClickListener(this);
        b6.setOnClickListener(this);
        b7.setOnClickListener(this);
        b8.setOnClickListener(this);
        b9.setOnClickListener(this);
        b0.setOnClickListener(this);

        repeatMinutes = (EditText) findViewById(R.id.repeatMinutes);
        repeatMinutes.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatMinutesSeek = (SeekBar) findViewById(R.id.repeatMinutesSeek);
        repeatMinutesSeek.setOnSeekBarChangeListener(this);
        repeatMinutes.setText(String.valueOf(repeatMinutesSeek.getProgress()));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "";
            int repeat = 0;
            long afterTime=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                afterTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                repeat = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            }
            if (c != null) c.close();
            taskField.setText(text);
            repeatMinutesSeek.setProgress(repeat);
            generateString(afterTime);
        }
    }

    private void generateString(long time){
        long s = 1000;
        long m = s * 60;
        long h = m * 60;
        long hours = (time / h);
        long minutes = ((time - hours * h) / (m));
        long seconds = ((time - (hours * h) - (minutes * m)) / (s));
        String hourStr;
        if (hours < 10) hourStr = "0" + hours;
        else hourStr = String.valueOf(hours);
        String minuteStr;
        if (minutes < 10) minuteStr = "0" + minutes;
        else minuteStr = String.valueOf(minutes);
        String secondStr;
        if (seconds < 10) secondStr = "0" + seconds;
        else secondStr = String.valueOf(seconds);
        timeString = hourStr + minuteStr + secondStr;
        updateTimeView();
    }

    private void updateTimeView() {
        if (timeString.matches("000000")) deleteButton.setEnabled(false);
        else deleteButton.setEnabled(true);
        if (timeString.length() == 6){
            String hours = timeString.substring(0, 2);
            String minutes = timeString.substring(2, 4);
            String seconds = timeString.substring(4, 6);
            hoursView.setText(hours);
            minutesView.setText(minutes);
            secondsView.setText(seconds);
        }
    }

    private CheckBox skypeTaskExport;
    private EditText skypeUser, repeatDaysSkype;
    private RadioButton skypeCall;
    private RadioButton skypeVideo;
    private TextView skypeDate, skypeYearDate, skypeTime;

    private void attachSkype(){
        taskField.setHint(getString(R.string.tast_hint));

        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        ViewUtils.fadeInAnimation(skype_layout, isAnimation);

        skypeUser = (EditText) findViewById(R.id.skypeUser);

        skypeCall = (RadioButton) findViewById(R.id.skypeCall);
        skypeVideo = (RadioButton) findViewById(R.id.skypeVideo);
        skypeCall.setChecked(true);
        RadioButton skypeChat = (RadioButton) findViewById(R.id.skypeChat);
        skypeChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) taskField.setHint(getString(R.string.message_field_hint));
                else taskField.setHint(getString(R.string.tast_hint));
            }
        });

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        LinearLayout skypeDateRing = (LinearLayout) findViewById(R.id.skypeDateRing);
        skypeDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        skypeTaskExport = (CheckBox) findViewById(R.id.skypeTaskExport);
        if (gtx.isLinked()){
            skypeTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        skypeDate = (TextView) findViewById(R.id.skypeDate);
        skypeDate.setText(dayStr + "/" + monthStr);
        skypeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        skypeDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        skypeYearDate = (TextView) findViewById(R.id.skypeYearDate);
        skypeYearDate.setText(String.valueOf(myYear));
        skypeYearDate.setTypeface(AssetsUtil.getThinTypeface(this));

        skypeTime = (TextView) findViewById(R.id.skypeTime);
        skypeTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        skypeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        skypeTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysSkype = (EditText) findViewById(R.id.repeatDaysSkype);
        repeatDaysSkype.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatSkype = (SeekBar) findViewById(R.id.repeatSkype);
        repeatSkype.setOnSeekBarChangeListener(this);
        repeatSkype.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysSkype.setText(String.valueOf(repeatSkype.getProgress()));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text="", number="";
            int repCode=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (c != null) c.close();

            if(type.matches(Constants.TYPE_SKYPE)){
                skypeCall.setChecked(true);
            }
            if(type.matches(Constants.TYPE_SKYPE_VIDEO)){
                skypeVideo.setChecked(true);
            }
            if(type.matches(Constants.TYPE_SKYPE_CHAT)){
                skypeChat.setChecked(true);
            }

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            skypeUser.setText(number);
            skypeDate.setText(dayStr + "/" + monthStr);
            skypeYearDate.setText(String.valueOf(myYear));
            skypeTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatSkype.setProgress(repCode);
            repeatDaysSkype.setText(String.valueOf(repCode));
        }
    }

    private CheckBox appTaskExport;
    private EditText browseLink, repeatDaysApp;
    private RadioButton application, browser;
    private TextView appDate, appYearDate, appTime, applicationName;

    private void attachApplication(){
        taskField.setHint(getString(R.string.tast_hint));

        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        ViewUtils.fadeInAnimation(application_layout, isAnimation);

        browseLink = (EditText) findViewById(R.id.browseLink);
        browseLink.setVisibility(View.GONE);
        RelativeLayout applicationLayout = (RelativeLayout) findViewById(R.id.applicationLayout);
        applicationLayout.setVisibility(View.VISIBLE);
        applicationName = (TextView) findViewById(R.id.applicationName);

        ImageButton pickApplication = (ImageButton) findViewById(R.id.pickApplication);
        pickApplication.setVisibility(View.GONE);
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        if (isDark){
            pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        } else pickApplication.setImageResource(R.drawable.ic_launch_grey600_24dp);

        application = (RadioButton) findViewById(R.id.application);
        application.setChecked(true);
        browser = (RadioButton) findViewById(R.id.browser);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        LinearLayout appDateRing = (LinearLayout) findViewById(R.id.appDateRing);
        appDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        appTaskExport = (CheckBox) findViewById(R.id.appTaskExport);
        if (gtx.isLinked()){
            appTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        appDate = (TextView) findViewById(R.id.appDate);
        appDate.setText(dayStr + "/" + monthStr);
        appDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        appDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        appYearDate = (TextView) findViewById(R.id.appYearDate);
        appYearDate.setText(String.valueOf(myYear));
        appYearDate.setTypeface(AssetsUtil.getThinTypeface(this));

        appTime = (TextView) findViewById(R.id.appTime);
        appTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        appTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        appTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysApp = (EditText) findViewById(R.id.repeatDaysApp);
        repeatDaysApp.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatApp = (SeekBar) findViewById(R.id.repeatApp);
        repeatApp.setOnSeekBarChangeListener(this);
        repeatApp.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysApp.setText(String.valueOf(repeatApp.getProgress()));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text="", number="";
            int repCode=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (c != null) c.close();

            selectedPackage = number;

            if(type.matches(Constants.TYPE_APPLICATION)){
                application.setChecked(true);
                browser.setEnabled(false);
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(selectedPackage, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String title = (String)((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                applicationName.setText(title);
            }

            if(type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                browser.setChecked(true);
                application.setEnabled(false);
                browseLink.setText(number);
            }

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);

            appDate.setText(dayStr + "/" + monthStr);
            appYearDate.setText(String.valueOf(myYear));
            appTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatApp.setProgress(repCode);
            repeatDaysApp.setText(String.valueOf(repCode));
        }
    }

    private CheckBox callTaskExport;
    private EditText repeatDaysCall;

    private void attachCall(){
        call_layout = (LinearLayout) findViewById(R.id.call_layout);
        ViewUtils.fadeInAnimation(call_layout, isAnimation);

        addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
            }
        });
        ViewUtils.setImage(addNumberButton, isDark);

        phoneNumber = (FloatingEditText) findViewById(R.id.phoneNumber);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        LinearLayout callDateRing = (LinearLayout) findViewById(R.id.callDateRing);
        callDateRing.setOnClickListener(this);

        callTaskExport = (CheckBox) findViewById(R.id.callTaskExport);
        if (gtx.isLinked()){
            callTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        callDate = (TextView) findViewById(R.id.callDate);
        callDate.setText(dayStr + "/" + monthStr);
        callDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        callDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        callYearDate = (TextView) findViewById(R.id.callYearDate);
        callYearDate.setText(String.valueOf(myYear));
        callYearDate.setTypeface(AssetsUtil.getThinTypeface(this));

        callTime = (TextView) findViewById(R.id.callTime);
        callTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        callTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        callTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysCall = (EditText) findViewById(R.id.repeatDaysCall);
        repeatDaysCall.setTypeface(AssetsUtil.getLightTypeface(this));

        repeatCallInt = (SeekBar) findViewById(R.id.repeatCallInt);
        repeatCallInt.setOnSeekBarChangeListener(this);
        repeatCallInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysCall.setText(String.valueOf(repeatCallInt.getProgress()));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text="", number="";
            int repCode=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (c != null) c.close();

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            phoneNumber.setText(number);
            callDate.setText(dayStr + "/" + monthStr);
            callYearDate.setText(String.valueOf(myYear));
            callTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatCallInt.setProgress(repCode);
            repeatDaysCall.setText(String.valueOf(repCode));
        }
    }

    private CheckBox messageTaskExport;
    private EditText repeatDaysMessage;

    private void attachMessage(){
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        ViewUtils.fadeInAnimation(message_layout, isAnimation);

        addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
            }
        });
        ViewUtils.setImage(addMessageNumberButton, isDark);

        messageNumber = (FloatingEditText) findViewById(R.id.messageNumber);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        LinearLayout messageDateRing = (LinearLayout) findViewById(R.id.messageDateRing);
        messageDateRing.setOnClickListener(this);

        messageTaskExport = (CheckBox) findViewById(R.id.messageTaskExport);
        if (gtx.isLinked()){
            messageTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        messageDate = (TextView) findViewById(R.id.messageDate);
        messageDate.setText(dayStr + "/" + monthStr);
        messageDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        messageDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        messageYearDate = (TextView) findViewById(R.id.messageYearDate);
        messageYearDate.setText(String.valueOf(myYear));
        messageYearDate.setTypeface(AssetsUtil.getThinTypeface(this));

        messageTime = (TextView) findViewById(R.id.messageTime);
        messageTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        messageTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        messageTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysMessage = (EditText) findViewById(R.id.repeatDaysMessage);
        repeatDaysMessage.setTypeface(AssetsUtil.getLightTypeface(this));

        repeatMessageInt = (SeekBar) findViewById(R.id.repeatMessageInt);
        repeatMessageInt.setOnSeekBarChangeListener(this);
        repeatMessageInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysMessage.setText(String.valueOf(repeatMessageInt.getProgress()));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text="", number="";
            int repCode=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (c != null) c.close();

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            messageNumber.setText(number);
            messageDate.setText(dayStr + "/" + monthStr);
            messageYearDate.setText(String.valueOf(myYear));
            messageTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatMessageInt.setProgress(repCode);
            repeatDaysMessage.setText(String.valueOf(repCode));
        }
    }

    private TextView locationDateField, locationDateYearField, locationTimeField;
    private AutoCompleteTextView searchField;
    private ImageButton clearField;
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private GeocoderTask task;
    private ArrayList<String> namesList;
    private LatLng curPlace;

    @Override
    public void place(LatLng place) {
        curPlace = place;
        if (isLocationOutAttached()) mapLocation.setText(LocationUtil.getAddress(place.latitude, place.longitude));
    }

    @Override
    public void onZoomOutClick() {
        if (isLocationAttached()) {
            ViewUtils.fadeOutAnimation(mapContainer, isAnimation);
            ViewUtils.fadeInAnimation(specsContainer, isAnimation);
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(BackupFileEdit.this);
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses==null || addresses.size()==0){
                Log.d(Constants.LOG_TAG,  "No Location found");
            } else {
                foundPlaces = addresses;

                namesList = new ArrayList<>();
                namesList.clear();
                for (Address selected:addresses){
                    String addressText = String.format("%s, %s%s",
                            selected.getMaxAddressLineIndex() > 0 ? selected.getAddressLine(0) : "",
                            selected.getMaxAddressLineIndex() > 1 ? selected.getAddressLine(1) + ", " : "",
                            selected.getCountryName());
                    namesList.add(addressText);
                }
                adapter = new ArrayAdapter<>(
                        BackupFileEdit.this, android.R.layout.simple_dropdown_item_1line, namesList);
                searchField.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private ImageButton addNumberButtonLocation;
    private LinearLayout actionLocation;
    private RelativeLayout mapContainer;
    private ScrollView specsContainer;
    private CheckBox attachLocationAction;
    private RadioButton callCheckLocation, messageCheckLocation;
    private FloatingEditText phoneNumberLocation;

    private boolean isMapVisible(){
        return mapContainer != null && mapContainer.getVisibility() == View.VISIBLE;
    }

    private void attachLocation() {
        geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        ViewUtils.fadeInAnimation(geolocationlayout, isAnimation);

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        specsContainer = (ScrollView) findViewById(R.id.specsContainer);
        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);
        replace(map, R.id.mapContainer);

        attackDelay = (CheckBox) findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isAnimation) {
                        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                        delayLayout.startAnimation(slide);
                        delayLayout.setVisibility(View.VISIBLE);
                    } else delayLayout.setVisibility(View.VISIBLE);
                }
                else {
                    delayLayout.setVisibility(View.GONE);
                }
            }
        });

        clearField = (ImageButton) findViewById(R.id.clearButton);
        ImageButton mapButton = (ImageButton) findViewById(R.id.mapButton);
        if (isDark){
            clearField.setImageResource(R.drawable.ic_backspace_white_24dp);
            mapButton.setImageResource(R.drawable.ic_map_white_24dp);
        } else {
            clearField.setImageResource(R.drawable.ic_backspace_grey600_24dp);
            mapButton.setImageResource(R.drawable.ic_map_grey600_24dp);
        }
        clearField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.fadeOutAnimation(specsContainer, isAnimation);
                ViewUtils.fadeInAnimation(mapContainer, isAnimation);
            }
        });

        searchField = (AutoCompleteTextView) findViewById(R.id.searchField);
        searchField.setThreshold(3);
        adapter = new ArrayAdapter<>(
                BackupFileEdit.this, android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null && !task.isCancelled()) task.cancel(true);
                task = new GeocoderTask();
                task.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                curPlace = pos;
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                if (map != null) map.addMarker(pos, title, true);
            }
        });

        actionLocation = (LinearLayout) findViewById(R.id.actionLocation);
        actionLocation.setVisibility(View.GONE);

        attachLocationAction = (CheckBox) findViewById(R.id.attachLocationAction);
        attachLocationAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ViewUtils.showOver(actionLocation, isAnimation);
                    addNumberButtonLocation = (ImageButton) findViewById(R.id.addNumberButtonLocation);
                    addNumberButtonLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(BackupFileEdit.this, null, getString(R.string.load_contats), true);
                            pickContacts(pd);
                        }
                    });
                    ViewUtils.setImage(addNumberButtonLocation, isDark);

                    phoneNumberLocation = (FloatingEditText) findViewById(R.id.phoneNumberLocation);

                    callCheckLocation = (RadioButton) findViewById(R.id.callCheckLocation);
                    callCheckLocation.setChecked(true);
                    messageCheckLocation = (RadioButton) findViewById(R.id.messageCheckLocation);
                    messageCheckLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) taskField.setHint(getString(R.string.message_field_hint));
                            else taskField.setHint(getString(R.string.tast_hint));
                        }
                    });
                } else {
                    ViewUtils.hideOver(actionLocation, isAnimation);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        LinearLayout locationDateRing = (LinearLayout) findViewById(R.id.locationDateRing);
        locationDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationDateField = (TextView) findViewById(R.id.locationDateField);
        locationDateField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationDateField.setText(dayStr + "/" + monthStr);
        locationDateYearField = (TextView) findViewById(R.id.locationDateYearField);
        locationTimeField = (TextView) findViewById(R.id.locationTimeField);
        locationTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationTimeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        locationTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        locationDateYearField.setTypeface(AssetsUtil.getThinTypeface(this));
        locationDateYearField.setText(String.valueOf(myYear));

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "", number = null;
            double latitude=0, longitude=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            }
            if (c != null) c.close();

            if (myDay > 0 && myHour > 0 && myMinute > 0 && myMonth > 0 && myYear > 0) {
                if (myDay < 10) dayStr = "0" + myDay;
                else dayStr = String.valueOf(myDay);

                if (myMonth < 9) monthStr = "0" + (myMonth + 1);
                else monthStr = String.valueOf(myMonth + 1);

                cal.set(Calendar.HOUR_OF_DAY, myHour);
                cal.set(Calendar.MINUTE, myMinute);

                locationTimeField.setText(TimeUtil.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                locationDateField.setText(dayStr + "/" + monthStr);
                locationDateYearField.setText(String.valueOf(myYear));
                attackDelay.setChecked(true);
            } else {
                attackDelay.setChecked(false);
            }

            if (type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_MESSAGE)){
                attachLocationAction.setChecked(true);
                phoneNumberLocation = (FloatingEditText) findViewById(R.id.phoneNumberLocation);
                phoneNumberLocation.setText(number);
                if (type.matches(Constants.TYPE_LOCATION_CALL)){
                    callCheckLocation = (RadioButton) findViewById(R.id.callCheckLocation);
                    callCheckLocation.setChecked(true);
                } else {
                    messageCheckLocation = (RadioButton) findViewById(R.id.messageCheckLocation);
                    messageCheckLocation.setChecked(true);
                }
            } else {
                attachLocationAction.setChecked(false);
            }

            taskField.setText(text);
            if (longitude != 0 && latitude != 0) {
                if (map != null) map.addMarker(new LatLng(latitude, longitude), text, true);
            }
        }
    }

    private ImageButton addNumberButtonLocationOut;
    private LinearLayout actionLocationOut;
    private LinearLayout locationOutLayout;
    private LinearLayout delayLayoutOut;
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView locationOutDateField, locationOutDateYearField, locationOutTimeField, currentLocation,
            mapLocation, radiusMark;
    private CheckBox attachLocationOutAction, attachDelayOut;
    private RadioButton callCheckLocationOut, messageCheckLocationOut, currentCheck, mapCheck;
    private FloatingEditText phoneNumberLocationOut;

    private boolean isMapOutVisible(){
        return mapContainerOut != null && mapContainerOut.getVisibility() == View.VISIBLE;
    }

    private void attachLocationOut() {
        taskField.setHint(getString(R.string.tast_hint));

        locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        ViewUtils.fadeInAnimation(locationOutLayout, isAnimation);

        delayLayoutOut = (LinearLayout) findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) findViewById(R.id.specsContainerOut);
        mapContainerOut = (RelativeLayout) findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);
        replace(map, R.id.mapContainerOut);

        attachDelayOut = (CheckBox) findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isAnimation) {
                        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                        delayLayoutOut.startAnimation(slide);
                        delayLayoutOut.setVisibility(View.VISIBLE);
                    } else delayLayoutOut.setVisibility(View.VISIBLE);
                }
                else {
                    delayLayoutOut.setVisibility(View.GONE);
                }
            }
        });

        if (attachDelayOut.isChecked()) {
            if (isAnimation) {
                ViewUtils.expand(delayLayoutOut);
            } else delayLayoutOut.setVisibility(View.VISIBLE);
        }

        ImageButton mapButtonOut = (ImageButton) findViewById(R.id.mapButtonOut);
        if (isDark){
            mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);
        } else {
            mapButtonOut.setImageResource(R.drawable.ic_map_grey600_24dp);
        }

        mapButtonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.fadeOutAnimation(specsContainerOut, isAnimation);
                ViewUtils.fadeInAnimation(mapContainerOut, isAnimation);
                mapCheck.setChecked(true);
            }
        });

        currentCheck = (RadioButton) findViewById(R.id.currentCheck);
        mapCheck = (RadioButton) findViewById(R.id.mapCheck);
        currentCheck.setOnCheckedChangeListener(this);
        mapCheck.setOnCheckedChangeListener(this);
        currentCheck.setChecked(true);

        SeekBar pointRadius = (SeekBar) findViewById(R.id.pointRadius);
        pointRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusMark.setText(String.format(getString(R.string.string_selected_radius), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (pointRadius.getProgress() == 0)
            pointRadius.setProgress(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        actionLocationOut = (LinearLayout) findViewById(R.id.actionLocationOut);
        actionLocationOut.setVisibility(View.GONE);

        attachLocationOutAction = (CheckBox) findViewById(R.id.attachLocationOutAction);
        attachLocationOutAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ViewUtils.showOver(actionLocationOut, isAnimation);
                    addNumberButtonLocationOut = (ImageButton) findViewById(R.id.addNumberButtonLocationOut);
                    addNumberButtonLocationOut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(BackupFileEdit.this, null, getString(R.string.load_contats), true);
                            pickContacts(pd);
                        }
                    });
                    ViewUtils.setImage(addNumberButtonLocationOut, isDark);

                    phoneNumberLocationOut = (FloatingEditText) findViewById(R.id.phoneNumberLocationOut);

                    callCheckLocationOut = (RadioButton) findViewById(R.id.callCheckLocationOut);
                    callCheckLocationOut.setChecked(true);
                    messageCheckLocationOut = (RadioButton) findViewById(R.id.messageCheckLocationOut);
                    messageCheckLocationOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) taskField.setHint(getString(R.string.message_field_hint));
                            else taskField.setHint(getString(R.string.tast_hint));
                        }
                    });
                } else {
                    ViewUtils.hideOver(actionLocationOut, isAnimation);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        String dayStr;
        String monthStr;

        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        if (myMonth < 9) monthStr = "0" + (myMonth + 1);
        else monthStr = String.valueOf(myMonth + 1);

        LinearLayout locationOutDateRing = (LinearLayout) findViewById(R.id.locationOutDateRing);
        locationOutDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationOutDateField = (TextView) findViewById(R.id.locationOutDateField);
        locationOutDateField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationOutDateField.setText(dayStr + "/" + monthStr);
        locationOutDateYearField = (TextView) findViewById(R.id.locationOutDateYearField);
        locationOutTimeField = (TextView) findViewById(R.id.locationOutTimeField);
        locationOutTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationOutTimeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        locationOutTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        locationOutDateYearField.setTypeface(AssetsUtil.getThinTypeface(this));
        locationOutDateYearField.setText(String.valueOf(myYear));

        if (curPlace != null) {
            if (map != null) map.addMarker(curPlace, null, true);
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "", number = null;
            double latitude=0, longitude=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            }
            if (c != null) c.close();

            if (myDay > 0 && myHour > 0 && myMinute > 0 && myMonth > 0 && myYear > 0) {
                if (myDay < 10) dayStr = "0" + myDay;
                else dayStr = String.valueOf(myDay);

                if (myMonth < 9) monthStr = "0" + (myMonth + 1);
                else monthStr = String.valueOf(myMonth + 1);

                cal.set(Calendar.HOUR_OF_DAY, myHour);
                cal.set(Calendar.MINUTE, myMinute);

                locationOutTimeField.setText(TimeUtil.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                locationOutDateField.setText(dayStr + "/" + monthStr);
                locationOutDateYearField.setText(String.valueOf(myYear));
                attachDelayOut.setChecked(true);
            } else {
                attachDelayOut.setChecked(false);
            }

            if (type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_MESSAGE)){
                attachLocationOutAction.setChecked(true);
                phoneNumberLocationOut = (FloatingEditText) findViewById(R.id.phoneNumberLocationOut);
                phoneNumberLocationOut.setText(number);
                if (type.matches(Constants.TYPE_LOCATION_CALL)){
                    callCheckLocationOut = (RadioButton) findViewById(R.id.callCheckLocationOut);
                    callCheckLocationOut.setChecked(true);
                } else {
                    messageCheckLocationOut = (RadioButton) findViewById(R.id.messageCheckLocationOut);
                    messageCheckLocationOut.setChecked(true);
                }
            } else {
                attachLocationOutAction.setChecked(false);
            }

            taskField.setText(text);
            if (longitude != 0 && latitude != 0) {
                LatLng pos = new LatLng(latitude, longitude);
                if (map != null) map.addMarker(pos, text, true);
                mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
                mapCheck.setChecked(true);
            }
        }
    }

    private void replace(Fragment fragment, int container){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(container, fragment, null);
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.commitAllowingStateLoss();
    }

    private boolean isDateReminderAttached(){
        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        return by_date_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isWeekDayReminderAttached(){
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        return weekday_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isApplicationAttached(){
        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        return application_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isTimeReminderAttached(){
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        return after_time_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isSkypeAttached(){
        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        return skype_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isCallAttached(){
        call_layout = (LinearLayout) findViewById(R.id.call_layout);
        return call_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isMessageAttached(){
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        return message_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isLocationAttached(){
        geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        return geolocationlayout.getVisibility() == View.VISIBLE;
    }

    private boolean isMonthDayAttached(){
        monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        return monthDayLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isLocationOutAttached(){
        locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        return locationOutLayout.getVisibility() == View.VISIBLE;
    }

    private void clearForm(){
        call_layout = (LinearLayout) findViewById(R.id.call_layout);
        call_layout.setVisibility(View.GONE);
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        weekday_layout.setVisibility(View.GONE);
        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        by_date_layout.setVisibility(View.GONE);
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        after_time_layout.setVisibility(View.GONE);
        geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        geolocationlayout.setVisibility(View.GONE);
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        message_layout.setVisibility(View.GONE);
        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        skype_layout.setVisibility(View.GONE);
        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        application_layout.setVisibility(View.GONE);
        monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        monthDayLayout.setVisibility(View.GONE);
        locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        locationOutLayout.setVisibility(View.GONE);
    }

    private String getTaskType(){
        String type="";
        if (isDateReminderAttached()){
            type = Constants.TYPE_REMINDER;
        } else if (isTimeReminderAttached()){
            type = Constants.TYPE_TIME;
        } else if (isCallAttached()){
            type = Constants.TYPE_CALL;
        } else if (isMessageAttached()){
            type = Constants.TYPE_MESSAGE;
        } else if (isLocationAttached()){
            if (attachLocationAction.isChecked()){
                if (callCheckLocation.isChecked()) type = Constants.TYPE_LOCATION_CALL;
                else type = Constants.TYPE_LOCATION_MESSAGE;
            } else type = Constants.TYPE_LOCATION;
        } else if (isLocationOutAttached()){
            if (attachLocationOutAction.isChecked()){
                if (callCheckLocationOut.isChecked()) type = Constants.TYPE_LOCATION_OUT_CALL;
                else type = Constants.TYPE_LOCATION_OUT_MESSAGE;
            } else type = Constants.TYPE_LOCATION_OUT;
        }
        return type;
    }

    private void pickContacts(final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                final ArrayList<String> contacts = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1"))
                        hasPhone = "true";
                    else
                        hasPhone = "false" ;
                    if (name != null) {
                        if (Boolean.parseBoolean(hasPhone)) {
                            contacts.add(name);
                        }
                    }
                }
                cursor.close();
                try {
                    Collections.sort(contacts, new Comparator<String>() {
                        @Override
                        public int compare(String e1, String e2) {
                            return e1.compareToIgnoreCase(e2);
                        }
                    });
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ((pd != null) && pd.isShowing()) {
                                pd.dismiss();
                            }
                        } catch (final Exception e) {
                            // Handle or log or ignore
                        }
                        Intent i = new Intent(BackupFileEdit.this, ContactsList.class);
                        i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                        startActivityForResult(i, Constants.REQUEST_CODE_CONTACTS);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                //Use Data to get string
                String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
                if (isCallAttached()) {
                    phoneNumber.setText(number);
                }
                if (isMessageAttached()){
                    messageNumber.setText(number);
                }
                if (isWeekDayReminderAttached() && attachAction.isChecked()){
                    weekPhoneNumber.setText(number);
                }
                if (isMonthDayAttached() && monthDayAttachAction.isChecked()){
                    monthDayPhoneNumber.setText(number);
                }
                if (isLocationAttached() && attachLocationAction.isChecked()){
                    phoneNumberLocation.setText(number);
                }
                if (isLocationOutAttached() && attachLocationOutAction.isChecked()){
                    phoneNumberLocationOut.setText(number);
                }
            }
        }

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null){
                String text = matches.get(0).toString();
                taskField.setText(text);
            }
        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BackupFileEdit.this);
        alertDialog.setTitle(getString(R.string.gps_title));
        alertDialog.setMessage(getString(R.string.gps_text));

        alertDialog.setPositiveButton(getString(R.string.action_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveTask();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveTask(){
        if (isLocationAttached() || isLocationOutAttached()){
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled || isNetworkEnabled) {
                if (isLocationOutAttached()){
                    if (attachLocationOutAction.isChecked() && !checkNumber()) addLocationOut();
                    else if (attachLocationOutAction.isChecked() && checkNumber())
                        phoneNumberLocationOut.setError(getString(R.string.number_error));
                    else addLocationOut();
                } else {
                    if (attachLocationAction.isChecked() && !checkNumber()) addLocation();
                    else if (attachLocationAction.isChecked() && checkNumber())
                        phoneNumberLocation.setError(getString(R.string.number_error));
                    else addLocation();
                }
            } else {
                showSettingsAlert();
            }
        } else {
            if (isDateReminderAttached()) {
                saveDateTask();
            } else if (isTimeReminderAttached()) {
                saveTimeTask();
            } else if (isCallAttached()){
                if (!checkNumber()) {
                    saveCallTask();
                } else {
                    phoneNumber.setError(getString(R.string.number_error));
                }
            } else if (isWeekDayReminderAttached()) {
                if (!checkNumber()) {
                    saveWeekTask();
                } else {
                    weekPhoneNumber.setError(getString(R.string.number_error));
                }
            } else if (isSkypeAttached()){
                if (!checkNumber()) {
                    saveSkypeTask();
                } else {
                    skypeUser.setError(getString(R.string.number_error));
                }
            } else if (isApplicationAttached()){
                if (!checkApplication()) {
                    saveAppTask();
                } else {
                    if (browser.isChecked()){
                        browseLink.setError(getString(R.string.empty_field_error));
                    } else if (application.isChecked()){
                        Toast.makeText(BackupFileEdit.this, getString(R.string.not_selected_application_message), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (isMessageAttached()){
                if (!checkNumber()) {
                    if (!checkMessage()){
                        saveMessageTask();
                    } else {
                        taskField.setError(getString(R.string.message_empty_error));
                    }
                } else {
                    messageNumber.setError(getString(R.string.number_error));
                }
            } else if (isMonthDayAttached()) {
                if (!checkNumber()) {
                    saveMonthTask();
                } else {
                    monthDayPhoneNumber.setError(getString(R.string.number_error));
                }
            }
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    private String getWeekTaskType(){
        String type;
        if (attachAction.isChecked()){
            if (callCheck.isChecked()){
                type = Constants.TYPE_WEEKDAY_CALL;
            } else type = Constants.TYPE_WEEKDAY_MESSAGE;
        } else {
            type = Constants.TYPE_WEEKDAY;
        }
        return type;
    }

    private String getMonthTaskType(){
        String type;
        if (monthDayAttachAction.isChecked()){
            if (monthDayCallCheck.isChecked()){
                if (lastCheck.isChecked()) type = Constants.TYPE_MONTHDAY_CALL_LAST;
                else type = Constants.TYPE_MONTHDAY_CALL;
            } else {
                if (lastCheck.isChecked()) type = Constants.TYPE_MONTHDAY_MESSAGE_LAST;
                else type = Constants.TYPE_MONTHDAY_MESSAGE;
            }
        } else {
            if (lastCheck.isChecked()) type = Constants.TYPE_MONTHDAY_LAST;
            else type = Constants.TYPE_MONTHDAY;
        }
        return type;
    }

    private String getAppTaskType(){
        String type;
        if (application.isChecked()){
            type = Constants.TYPE_APPLICATION;
        } else {
            type = Constants.TYPE_APPLICATION_BROWSER;
        }
        return type;
    }

    private String getSkypeTaskType(){
        String type;
        if (skypeCall.isChecked()){
            type = Constants.TYPE_SKYPE;
        } else if (skypeVideo.isChecked()){
            type = Constants.TYPE_SKYPE_VIDEO;
        } else {
            type = Constants.TYPE_SKYPE_CHAT;
        }
        return type;
    }

    private void saveWeekTask() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getWeekTaskType();
        String number = null;
        if (attachAction.isChecked()) {
            number = weekPhoneNumber.getText().toString().trim();
        }

        Interval interval = new Interval(BackupFileEdit.this);
        String repeat = interval.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(),
                wednesdayCheck.isChecked(), thursdayCheck.isChecked(), fridayCheck.isChecked(),
                saturdayCheck.isChecked(), sundayCheck.isChecked());
        if (repeat.matches(Constants.NOTHING_CHECKED)) {
            Toast.makeText(BackupFileEdit.this, getString(R.string.weekday_nothing_checked),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(task, type, 0, 0, 0, myHour, myMinute, 0, number,
                0, 0, 0, 0, 0, uuID, repeat, 0, null, 0, -1, 0, categoryId);
        new WeekDayReceiver().setAlarm(BackupFileEdit.this, idN);
        long startTime = ReminderUtils.getWeekTime(myHour, myMinute, repeat);
        ReminderUtils.exportToCalendar(this, task, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && weekTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, task, startTime, idN);
        }
        DB.close();
    }

    private void saveAppTask() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getAppTaskType();
        String number = null;
        if (application.isChecked()){
            number = selectedPackage;
        } else if (browser.isChecked()){
            number = browseLink.getText().toString().trim();
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://" + number;
        }

        int repeat = Integer.parseInt(repeatDaysApp.getText().toString().trim());
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                number, repeat, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId);
        alarm.setAlarm(BackupFileEdit.this, idN);
        DB.updateReminderDateTime(idN);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, task, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && appTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, task, startTime, idN);
        }
    }

    private void saveSkypeTask() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getSkypeTaskType();
        String number = skypeUser.getText().toString().trim();

        int repeat = Integer.parseInt(repeatDaysSkype.getText().toString().trim());
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                number, repeat, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId);
        alarm.setAlarm(BackupFileEdit.this, idN);
        DB.updateReminderDateTime(idN);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, task, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && skypeTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, task, startTime, idN);
        }
    }

    private boolean checkNumber(){
        if (isCallAttached()) {
            return phoneNumber.getText().toString().trim().matches("");
        } else if (isSkypeAttached()){
            return skypeUser.getText().toString().trim().matches("");
        } else if (isMessageAttached()){
            return messageNumber.getText().toString().trim().matches("");
        } else if (isLocationAttached() && attachLocationAction.isChecked()){
            return phoneNumberLocation.getText().toString().trim().matches("");
        } else if (isWeekDayReminderAttached() && attachAction.isChecked()) {
            return weekPhoneNumber.getText().toString().trim().matches("");
        } else if (isMonthDayAttached() && monthDayAttachAction.isChecked()) {
            return monthDayPhoneNumber.getText().toString().trim().matches("");
        } else if (isLocationOutAttached() && attachLocationOutAction.isChecked()) {
            return phoneNumberLocationOut.getText().toString().trim().matches("");
        } else return false;
    }

    private boolean checkApplication(){
        if (application.isChecked()) {
            return applicationName.getText().toString().trim().matches("");
        } else return browser.isChecked() && browseLink.getText().toString().trim().matches("");
    }

    private boolean checkMessage(){
        if (isMessageAttached()){
            return taskField.getText().toString().trim().matches("");
        } else if (isWeekDayReminderAttached() && attachAction.isChecked()) {
            return taskField.getText().toString().trim().matches("");
        } else {
            return taskField.getText().toString().trim().matches("");
        }
    }

    private void saveMonthTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }

        String type = getMonthTaskType();
        String number = null;
        if (monthDayAttachAction.isChecked()) {
            number = monthDayPhoneNumber.getText().toString().trim();
        }

        int day = myDay;
        if (type.endsWith("_last")) day = 0;

        DB.open();
        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(text, type, myDay, 0, 0, myHour, myMinute, 0, number, 0, 0, 0, 0, 0,
                uuID, null, 0, null, 0, -1, 0, categoryId);
        DB.updateReminderDateTime(idN);
        new MonthDayReceiver().setAlarm(this, id);

        long startTime = ReminderUtils.getMonthTime(myHour, myMinute, day);
        ReminderUtils.exportToCalendar(this, text, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && monthDayTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, id);
        }
        DB.updateReminderDateTime(id);
    }

    private void saveDateTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        int repeat = Integer.parseInt(repeatDays.getText().toString().trim());
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                repeat, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId);
        alarm.setAlarm(BackupFileEdit.this, idN);
        DB.updateReminderDateTime(idN);
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, text, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && dateTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, idN);
        }
    }

    private void saveTimeTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        long time = getAfterTime();
        if (time == 0) return;
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        final Calendar c = Calendar.getInstance();
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        int mySeconds = c.get(Calendar.SECOND);

        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds,
                null, 0, time, 0, 0, 0,
                uuID, null, 0, null, 0, -1, 0, categoryId);
        alarm.setAlarm(BackupFileEdit.this, idN);
        DB.updateReminderDateTime(idN);
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, time);
        ReminderUtils.exportToCalendar(this, text, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && timeTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, idN);
        }
    }

    private long getAfterTime() {
        long res = 0;
        if (timeString.length() == 6 && !timeString.matches("000000")){
            String hours = timeString.substring(0, 2);
            String minutes = timeString.substring(2, 4);
            String seconds = timeString.substring(4, 6);
            int hour = Integer.parseInt(hours);
            int minute = Integer.parseInt(minutes);
            int sec = Integer.parseInt(seconds);
            long s = 1000;
            long m = s * 60;
            long h = m * 60;
            res = (hour * h) + (minute * m) + (sec * s);
        } else Toast.makeText(this, "You don't insert any time!", Toast.LENGTH_SHORT).show();
        return res;
    }

    private void saveCallTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = phoneNumber.getText().toString().trim();
        int repeat = Integer.parseInt(repeatDaysCall.getText().toString().trim());
        DB = new DataBase(BackupFileEdit.this);
        DB.open();

        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                uuID, null, 0, null, 0, -1, 0, categoryId);
        alarm.setAlarm(BackupFileEdit.this, idN);
        DB.updateReminderDateTime(idN);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, text, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && callTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, idN);
        }
    }

    private void saveMessageTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = messageNumber.getText().toString().trim();
        int repeat = Integer.parseInt(repeatDaysMessage.getText().toString().trim());
        DB = new DataBase(BackupFileEdit.this);
        DB.open();

        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        long idN = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                uuID, null, 0, null, 0, -1, 0, categoryId);
        DB.updateReminderDateTime(idN);
        alarm.setAlarm(BackupFileEdit.this, idN);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, text, startTime, idN, isCalendar, isStock);
        if (gtx.isLinked() && messageTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, idN);
        }
    }

    private PositionDelayReceiver positionDelayReceiver = new PositionDelayReceiver();

    private void addLocation(){
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = null;
        if (attachLocationAction.isChecked()) number = phoneNumberLocation.getText().toString().trim();
        LatLng dest = null;
        boolean isNull = false;
        if (curPlace != null) {
            dest = curPlace;
            isNull = false;
        }

        if (isNull){
            Toast.makeText(BackupFileEdit.this, getString(R.string.point_warning),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Double latitude = dest.latitude;
        Double longitude = dest.longitude;
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        if (attackDelay.isChecked()){
            long newIds = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                    number, 0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
            DB.updateReminderDateTime(newIds);
            positionDelayReceiver.setDelay(BackupFileEdit.this, newIds);
        } else {
            long ids = DB.insertReminder(task, type, 0, 0, 0, 0, 0, 0, number,
                    0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
            DB.updateReminderDateTime(ids);
            startService(new Intent(BackupFileEdit.this, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        DB.close();
    }

    private void addLocationOut() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = null;
        if (attachLocationOutAction.isChecked()) number = phoneNumberLocationOut.getText().toString().trim();
        LatLng dest = null;
        boolean isNull = true;
        if (curPlace != null) {
            dest = curPlace;
            isNull = false;
        }

        if (isNull){
            Toast.makeText(BackupFileEdit.this, getString(R.string.point_warning),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUID(uuID)){
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Double latitude = dest.latitude;
        Double longitude = dest.longitude;
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        if (attachDelayOut.isChecked()){
            long newIds = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                    number, 0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
            DB.updateReminderDateTime(newIds);
            positionDelayReceiver.setDelay(BackupFileEdit.this, newIds);
        } else {
            long ids = DB.insertReminder(task, type, 0, 0, 0, 0, 0, 0, number,
                    0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
            DB.updateReminderDateTime(ids);
            startService(new Intent(BackupFileEdit.this, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        DB.close();
    }

    private boolean isUID(String uuId){
        ArrayList<String> ids = new ArrayList<>();
        DB = new DataBase(BackupFileEdit.this);
        Cursor c = DB.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                ids.add(c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR)));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        Cursor a = DB.getArchivedReminders();
        if (a != null && a.moveToFirst()){
            do {
                ids.add(a.getString(a.getColumnIndex(Constants.COLUMN_TECH_VAR)));
            } while (a.moveToNext());
        }
        return ids.contains(uuId);
    }

    @Override
    public void onClick(View v) {
        int ids = v.getId();
        if (ids >= 100 && ids < 110){
            String charS = String.valueOf(timeString.charAt(0));
            if (charS.matches("0")){
                timeString = timeString.substring(1, timeString.length());
                timeString = timeString + String.valueOf(ids - 100);
                updateTimeView();
            }
        }
        switch (v.getId()){
            case R.id.callDateRing:
                dateDialog();
                break;
            case R.id.callTime:
                timeDialog().show();
                break;
            case R.id.messageDateRing:
                dateDialog();
                break;
            case R.id.messageTime:
                timeDialog().show();
                break;
            case R.id.dateRing:
                dateDialog();
                break;
            case R.id.timeField:
                timeDialog().show();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.repeatSkype:
                repeatDaysSkype.setText(String.valueOf(progress));
                break;
            case R.id.repeatApp:
                repeatDaysApp.setText(String.valueOf(progress));
                break;
            case R.id.repeatCallInt:
                repeatDaysCall.setText(String.valueOf(progress));
                break;
            case R.id.repeatMessageInt:
                repeatDaysMessage.setText(String.valueOf(progress));
                break;
            case R.id.repeatDateInt:
                repeatDays.setText(String.valueOf(progress));
                break;
            case R.id.repeatMinutesSeek:
                repeatMinutes.setText(String.valueOf(progress));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

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

        if (isCallAttached()){
            callDate.setText(dayStr + "/" + monthStr);
            callYearDate.setText(String.valueOf(myYear));
        }
        if (isMonthDayAttached()){
            if (myDay < 29) monthDayField.setText(dayStr);
            else {
                myDay = 28;
                Toast.makeText(BackupFileEdit.this, getString(R.string.string_max_day_message), Toast.LENGTH_SHORT).show();
            }
        }
        if (isSkypeAttached()){
            skypeDate.setText(dayStr + "/" + monthStr);
            skypeYearDate.setText(String.valueOf(myYear));
        }
        if (isApplicationAttached()){
            appDate.setText(dayStr + "/" + monthStr);
            appYearDate.setText(String.valueOf(myYear));
        }
        if (isDateReminderAttached()){
            dateField.setText(dayStr + "/" + monthStr);
            dateYearField.setText(String.valueOf(myYear));
        }
        if (isMessageAttached()){
            messageDate.setText(dayStr + "/" + monthStr);
            messageYearDate.setText(String.valueOf(myYear));
        }
        if (isLocationAttached()){
            if (attackDelay.isChecked()){
                if (delayLayout.getVisibility() == View.VISIBLE) {
                    locationDateField.setText(dayStr + "/" + monthStr);
                    locationDateYearField.setText(String.valueOf(myYear));
                }
            }
        }
        if (isLocationOutAttached()){
            if (attachDelayOut.isChecked()){
                if (delayLayoutOut.getVisibility() == View.VISIBLE) {
                    locationOutDateField.setText(dayStr + "/" + monthStr);
                    locationOutDateYearField.setText(String.valueOf(myYear));
                }
            }
        }
    }

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            String formattedTime = TimeUtil.getTime(c.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));

            if (isMonthDayAttached()){
                monthDayTimeField.setText(formattedTime);
            }
            if (isCallAttached()){
                callTime.setText(formattedTime);
            }
            if (isDateReminderAttached()){
                timeField.setText(formattedTime);
            }
            if (isSkypeAttached()){
                skypeTime.setText(formattedTime);
            }
            if (isApplicationAttached()){
                appTime.setText(formattedTime);
            }
            if (isMessageAttached()){
                messageTime.setText(formattedTime);
            }
            if (isWeekDayReminderAttached()){
                weekTimeField.setText(formattedTime);
            }
            if (isLocationAttached()){
                if (attackDelay.isChecked()){
                    if (delayLayout.getVisibility() == View.VISIBLE) locationTimeField.setText(formattedTime);
                }
            }
            if (isLocationOutAttached()){
                if (attachDelayOut.isChecked()){
                    if (delayLayoutOut.getVisibility() == View.VISIBLE)
                        locationOutTimeField.setText(formattedTime);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mLocList != null) mLocationManager.removeUpdates(mLocList);
        if (DB != null) DB.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (map != null && !map.onBackPressed()) return;
        finish();
    }

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    public class CurrentLocation implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double currentLat = location.getLatitude();
            double currentLong = location.getLongitude();
            curPlace = new LatLng(currentLat, currentLong);
            String _Location = LocationUtil.getAddress(currentLat, currentLong);
            String text = taskField.getText().toString().trim();
            if (text == null || text.matches("")) text = _Location;
            if (isLocationOutAttached()) {
                currentLocation.setText(_Location);
                if (map != null) {
                    map.addMarker(new LatLng(currentLat, currentLong), text, true);
                    map.moveCamera(new LatLng(currentLat, currentLong));
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }
    }
}