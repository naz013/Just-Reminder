package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.dialogs.utils.ContactsList;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BackupFileEdit extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, DatePickerDialog.OnDateSetListener {

    LinearLayout call_layout, by_date_layout, after_time_layout, geolocationlayout,
            message_layout, location_call_layout, location_message_layout, weekday_layout, action_layout,
            skype_layout, application_layout;
    LinearLayout callDateRing, dateRing, messageDateRing;
    FloatingEditText phoneNumber, messageNumber, locationCallPhoneNumber, locationMessagePhoneNumber, weekPhoneNumber;
    TextView callDate, callTime, dateField, timeField, callYearDate, dateYearField,
            repeatTimeIntLabel, afterTimeIntLabel, messageDate, messageYearDate, messageTime, weekTimeField;
    ImageButton insertVoice, pickApplication;
    ImageButton addNumberButton, addMessageNumberButton, locationCallAddNumberButton, locationMessageAddNumberButton,
            weekAddNumberButton;
    SeekBar repeatCallInt, repeatDateInt, afterTimeInt, repeatTimeInt, repeatMessageInt;

    Spinner placesList, placesListCall, placesListMessage;
    ArrayList<String> spinnerArray = null;
    LinearLayout layoutContainer, delayLayout, delayMessageLayout, delayCallLayout;
    CheckBox attackDelay, attackCallDelay, attackMessageDelay;

    int myHour = 0;
    int myMinute = 0;
    int mySeconds = 0;
    int myYear = 0;
    int myMonth = 0;
    int myDay = 1;
    String uuID = "";

    ProgressDialog pd;
    DataBase DB = new DataBase(BackupFileEdit.this);
    FilesDataBase fdb = new FilesDataBase(BackupFileEdit.this);
    UpdatesHelper updatesHelper;

    AlarmReceiver alarm = new AlarmReceiver();
    boolean isGPSEnabled = false, isNetworkEnabled = false;
    protected LocationManager locationManager;

    ColorSetter cSetter = new ColorSetter(BackupFileEdit.this);
    SharedPrefs sPrefs = new SharedPrefs(BackupFileEdit.this);
    Interval interval = new Interval(BackupFileEdit.this);
    GTasksHelper gtx = new GTasksHelper(BackupFileEdit.this);
    Typeface typeface;

    private MapFragment googleMap, locationCallMap, locationMessageMap;
    private Marker destination, locationCallDestination, locationMessageDestination;

    long id;
    String type, selectedPackage = null;
    Toolbar toolbar;
    FloatingEditText taskField;
    Spinner spinner;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        taskField = (FloatingEditText) findViewById(R.id.task_message);
        spinner = (Spinner) findViewById(R.id.navSpinner);
        spinner.setVisibility(View.INVISIBLE);

        toolbar.setVisibility(View.GONE);

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    toolbar.startAnimation(slide);
                    toolbar.setVisibility(View.VISIBLE);
                }
            }, 500);
        } else toolbar.setVisibility(View.VISIBLE);

        layoutContainer = (LinearLayout) findViewById(R.id.layoutContainer);
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        insertVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);

        clearForm();
        loadPlaces();

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
            } else if (type.matches(Constants.TYPE_LOCATION)){
                if (checkGooglePlayServicesAvailability()) {
                    attachLocation();
                } else {
                    Toast.makeText(BackupFileEdit.this, getString(R.string.play_services_check_error), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (type.matches(Constants.TYPE_LOCATION_CALL)){
                if (checkGooglePlayServicesAvailability()) {
                    attachLocationCall();
                } else {
                    Toast.makeText(BackupFileEdit.this, getString(R.string.play_services_check_error), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (type.matches(Constants.TYPE_LOCATION_MESSAGE)){
                if (checkGooglePlayServicesAvailability()) {
                    attachLocationMessage();
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
            } else {
                Toast.makeText(BackupFileEdit.this, getString(R.string.file_error_message), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SharedPrefs sPrefs = new SharedPrefs(BackupFileEdit.this);
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_VOICE_LANGUAGE));
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

    private void loadPlaces(){
        DB.open();
        Cursor c = DB.queryPlaces();
        spinnerArray = new ArrayList<>();
        spinnerArray.clear();
        spinnerArray.add(getString(R.string.other_settings));
        if (c != null && c.moveToFirst()){
            do {
                String namePlace = c.getString(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_NAME));
                spinnerArray.add(namePlace);

            } while (c.moveToNext());
        } else spinnerArray.clear();

        if (c != null) c.close();
    }

    private void fadeInAnimation(View view){
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    private void setImage(ImageButton ib){
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            ib.setImageResource(R.drawable.ic_person_add_white_24dp);
        } else ib.setImageResource(R.drawable.ic_person_add_grey600_24dp);
    }

    CheckBox dateTaskExport;
    EditText repeatDays;

    private void attachDateReminder(){
        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(by_date_layout);
        } else by_date_layout.setVisibility(View.VISIBLE);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        dateRing = (LinearLayout) findViewById(R.id.dateRing);
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

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
        timeField.setText(formattedTime);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        timeField.setTypeface(typeface);

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatDays.setTypeface(typeface);

        repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setOnSeekBarChangeListener(this);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDays.setText(String.valueOf(getRepeat(repeatDateInt.getProgress())));

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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            taskField.setText(text);
            timeField.setText(formattedTime);
            dateField.setText(dayStr + "/" + monthStr);
            dateYearField.setText(String.valueOf(myYear));
            repeatDateInt.setProgress(interval.getProgressFromCode(repCode));
            repeatDays.setText(String.valueOf(repCode));
        }
    }

    ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck, fridayCheck, saturdayCheck, sundayCheck;
    RadioButton callCheck, messageCheck;
    CheckBox attachAction, weekTaskExport;

    private void attachWeekDayReminder(){
        cSetter = new ColorSetter(BackupFileEdit.this);
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(weekday_layout);
        } else weekday_layout.setVisibility(View.VISIBLE);

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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(c.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(c.getTime());
        }

        weekTimeField = (TextView) findViewById(R.id.weekTimeField);
        weekTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        weekTimeField.setText(formattedTime);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        weekTimeField.setTypeface(typeface);

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
                    setImage(weekAddNumberButton);

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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            weekTimeField.setText(formattedTime);
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

    CheckBox timeTaskExport;

    private void attachTimeReminder(){
        cSetter = new ColorSetter(BackupFileEdit.this);
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(after_time_layout);
        } else after_time_layout.setVisibility(View.VISIBLE);

        repeatTimeIntLabel = (TextView) findViewById(R.id.repeatTimeIntLabel);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatTimeIntLabel.setTypeface(typeface);

        repeatTimeInt = (SeekBar) findViewById(R.id.repeatTimeInt);
        repeatTimeInt.setOnSeekBarChangeListener(this);

        afterTimeIntLabel = (TextView) findViewById(R.id.afterTimeIntLabel);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        afterTimeIntLabel.setTypeface(typeface);

        afterTimeInt = (SeekBar) findViewById(R.id.afterTimeInt);
        afterTimeInt.setOnSeekBarChangeListener(this);

        timeTaskExport = (CheckBox) findViewById(R.id.timeTaskExport);
        if (gtx.isLinked()){
            timeTaskExport.setVisibility(View.VISIBLE);
        }

        getTimeRepeat(repeatTimeInt.getProgress());
        getAfterTime(afterTimeInt.getProgress());

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "";
            int repCode=0, afterTime=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                afterTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (c != null) c.close();
            taskField.setText(text);
            repeatTimeInt.setProgress(interval.getTimeProgressFromCode(repCode));
            getTimeRepeat(repeatTimeInt.getProgress());
            afterTimeInt.setProgress(interval.getAfterTimeProgressFromCode(afterTime));
            getAfterTime(afterTimeInt.getProgress());
        }
    }

    CheckBox skypeTaskExport;
    EditText skypeUser, repeatDaysSkype;
    RadioButton skypeCall, skypeVideo, skypeChat;
    LinearLayout skypeDateRing;
    TextView skypeDate, skypeYearDate, skypeTime;
    SeekBar repeatSkype;

    private void attachSkype(){
        taskField.setHint(getString(R.string.tast_hint));

        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        fadeInAnimation(skype_layout);

        skypeUser = (EditText) findViewById(R.id.skypeUser);

        skypeCall = (RadioButton) findViewById(R.id.skypeCall);
        skypeVideo = (RadioButton) findViewById(R.id.skypeVideo);
        skypeCall.setChecked(true);
        skypeChat = (RadioButton) findViewById(R.id.skypeChat);
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

        skypeDateRing = (LinearLayout) findViewById(R.id.skypeDateRing);
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        skypeDate = (TextView) findViewById(R.id.skypeDate);
        skypeDate.setText(dayStr + "/" + monthStr);
        skypeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        skypeDate.setTypeface(typeface);

        skypeYearDate = (TextView) findViewById(R.id.skypeYearDate);
        skypeYearDate.setText(String.valueOf(myYear));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        skypeYearDate.setTypeface(typeface);

        skypeTime = (TextView) findViewById(R.id.skypeTime);
        skypeTime.setText(formattedTime);
        skypeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        skypeTime.setTypeface(typeface);

        repeatDaysSkype = (EditText) findViewById(R.id.repeatDaysSkype);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatDaysSkype.setTypeface(typeface);

        repeatSkype = (SeekBar) findViewById(R.id.repeatSkype);
        repeatSkype.setOnSeekBarChangeListener(this);
        repeatSkype.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysSkype.setText(String.valueOf(getRepeat(repeatSkype.getProgress())));

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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            taskField.setText(text);
            skypeUser.setText(number);
            skypeDate.setText(dayStr + "/" + monthStr);
            skypeYearDate.setText(String.valueOf(myYear));
            skypeTime.setText(formattedTime);
            repeatSkype.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysSkype.setText(String.valueOf(repCode));
        }
    }

    CheckBox appTaskExport;
    EditText browseLink, repeatDaysApp;
    RadioButton application, browser;
    LinearLayout appDateRing;
    TextView appDate, appYearDate, appTime, applicationName;
    SeekBar repeatApp;
    RelativeLayout applicationLayout;

    private void attachApplication(){
        taskField.setHint(getString(R.string.tast_hint));

        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        fadeInAnimation(application_layout);

        browseLink = (EditText) findViewById(R.id.browseLink);
        browseLink.setVisibility(View.GONE);
        applicationLayout = (RelativeLayout) findViewById(R.id.applicationLayout);
        applicationLayout.setVisibility(View.VISIBLE);
        applicationName = (TextView) findViewById(R.id.applicationName);

        pickApplication = (ImageButton) findViewById(R.id.pickApplication);
        pickApplication.setVisibility(View.GONE);
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        } else pickApplication.setImageResource(R.drawable.ic_launch_grey600_24dp);

        application = (RadioButton) findViewById(R.id.application);
        application.setChecked(true);
        browser = (RadioButton) findViewById(R.id.browser);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        appDateRing = (LinearLayout) findViewById(R.id.appDateRing);
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        appDate = (TextView) findViewById(R.id.appDate);
        appDate.setText(dayStr + "/" + monthStr);
        appDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        appDate.setTypeface(typeface);

        appYearDate = (TextView) findViewById(R.id.appYearDate);
        appYearDate.setText(String.valueOf(myYear));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        appYearDate.setTypeface(typeface);

        appTime = (TextView) findViewById(R.id.appTime);
        appTime.setText(formattedTime);
        appTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        appTime.setTypeface(typeface);

        repeatDaysApp = (EditText) findViewById(R.id.repeatDaysApp);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatDaysApp.setTypeface(typeface);

        repeatApp = (SeekBar) findViewById(R.id.repeatApp);
        repeatApp.setOnSeekBarChangeListener(this);
        repeatApp.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysApp.setText(String.valueOf(getRepeat(repeatApp.getProgress())));

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
                final String title = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            taskField.setText(text);

            appDate.setText(dayStr + "/" + monthStr);
            appYearDate.setText(String.valueOf(myYear));
            appTime.setText(formattedTime);
            repeatApp.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysApp.setText(String.valueOf(repCode));
        }
    }

    CheckBox callTaskExport;
    EditText repeatDaysCall;

    private void attachCall(){
        call_layout = (LinearLayout) findViewById(R.id.call_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(call_layout);
        } else call_layout.setVisibility(View.VISIBLE);

        addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
            }
        });
        setImage(addNumberButton);

        phoneNumber = (FloatingEditText) findViewById(R.id.phoneNumber);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        callDateRing = (LinearLayout) findViewById(R.id.callDateRing);
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        callDate = (TextView) findViewById(R.id.callDate);
        callDate.setText(dayStr + "/" + monthStr);
        callDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        callDate.setTypeface(typeface);

        callYearDate = (TextView) findViewById(R.id.callYearDate);
        callYearDate.setText(String.valueOf(myYear));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        callYearDate.setTypeface(typeface);

        callTime = (TextView) findViewById(R.id.callTime);
        callTime.setText(formattedTime);
        callTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        callTime.setTypeface(typeface);

        repeatDaysCall = (EditText) findViewById(R.id.repeatDaysCall);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatDaysCall.setTypeface(typeface);

        repeatCallInt = (SeekBar) findViewById(R.id.repeatCallInt);
        repeatCallInt.setOnSeekBarChangeListener(this);
        repeatCallInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysCall.setText(String.valueOf(getRepeat(repeatCallInt.getProgress())));

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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            taskField.setText(text);
            phoneNumber.setText(number);
            callDate.setText(dayStr + "/" + monthStr);
            callYearDate.setText(String.valueOf(myYear));
            callTime.setText(formattedTime);
            repeatCallInt.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysCall.setText(String.valueOf(repCode));
        }
    }

    CheckBox messageTaskExport;
    EditText repeatDaysMessage;

    private void attachMessage(){
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(message_layout);
        } else message_layout.setVisibility(View.VISIBLE);

        addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
            }
        });
        setImage(addMessageNumberButton);

        messageNumber = (FloatingEditText) findViewById(R.id.messageNumber);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        messageDateRing = (LinearLayout) findViewById(R.id.messageDateRing);
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        messageDate = (TextView) findViewById(R.id.messageDate);
        messageDate.setText(dayStr + "/" + monthStr);
        messageDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        messageDate.setTypeface(typeface);

        messageYearDate = (TextView) findViewById(R.id.messageYearDate);
        messageYearDate.setText(String.valueOf(myYear));
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        messageYearDate.setTypeface(typeface);

        messageTime = (TextView) findViewById(R.id.messageTime);
        messageTime.setText(formattedTime);
        messageTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        messageTime.setTypeface(typeface);

        repeatDaysMessage = (EditText) findViewById(R.id.repeatDaysMessage);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        repeatDaysMessage.setTypeface(typeface);

        repeatMessageInt = (SeekBar) findViewById(R.id.repeatMessageInt);
        repeatMessageInt.setOnSeekBarChangeListener(this);
        repeatMessageInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysMessage.setText(String.valueOf(getRepeat(repeatMessageInt.getProgress())));

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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            taskField.setText(text);
            messageNumber.setText(number);
            messageDate.setText(dayStr + "/" + monthStr);
            messageYearDate.setText(String.valueOf(myYear));
            messageTime.setText(formattedTime);
            repeatMessageInt.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysMessage.setText(String.valueOf(repCode));
        }
    }

    LinearLayout locationDateRing;
    TextView locationDateField, locationDateYearField, locationTimeField;
    AutoCompleteTextView searchField;
    ImageButton clearField;
    List<Address> foundPlaces;
    ArrayAdapter<String> adapter;
    GeocoderTask task;
    ArrayList<String> namesList;

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
                if (isLocationAttached()){
                    searchField.setAdapter(adapter);
                } else if (isLocationCallAttached()){
                    searchFieldCall.setAdapter(adapter);
                } else if (isLocationMessageAttached()){
                    searchFieldMessage.setAdapter(adapter);
                }

                adapter.notifyDataSetChanged();
            }
        }
    }

    private void attachLocation() {
        geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(geolocationlayout);
        } else geolocationlayout.setVisibility(View.VISIBLE);

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        delayLayout.setVisibility(View.GONE);

        attackDelay = (CheckBox) findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
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
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            clearField.setImageResource(R.drawable.ic_clear_white_24dp);
        } else clearField.setImageResource(R.drawable.ic_clear_grey600_24dp);
        clearField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
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
                GoogleMap mMap = googleMap.getMap();
                mMap.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                destination = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        locationDateRing = (LinearLayout) findViewById(R.id.locationDateRing);
        locationDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationDateField = (TextView) findViewById(R.id.locationDateField);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        locationDateField.setTypeface(typeface);
        locationDateField.setText(dayStr + "/" + monthStr);
        locationDateYearField = (TextView) findViewById(R.id.locationDateYearField);
        locationTimeField = (TextView) findViewById(R.id.locationTimeField);
        locationTimeField.setTypeface(typeface);
        locationTimeField.setText(formattedTime);
        locationTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        locationDateYearField.setTypeface(typeface);
        locationDateYearField.setText(String.valueOf(myYear));

        placesList = (Spinner) findViewById(R.id.placesList);
        placesList.setBackgroundColor(cSetter.getSpinnerStyle());
        if (spinnerArray.isEmpty()){
            placesList.setVisibility(View.GONE);
        } else {
            placesList.setVisibility(View.VISIBLE);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            placesList.setAdapter(spinnerArrayAdapter);
            placesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position > 0){
                        String placeName = spinnerArray.get(position);
                        DB.open();
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));

                            LatLng latLng = new LatLng(latitude, longitude);
                            googleMap.getMap().clear();
                            String title = taskField.getText().toString().trim();
                            if (title.matches("")) {
                                title = latLng.toString();
                            }
                            destination = googleMap.getMap().addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                                    .draggable(true));

                            googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        googleMap = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            googleMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        googleMap.getMap().setMyLocationEnabled(true);
        if (googleMap.getMap().getMyLocation() != null) {
            double lat = googleMap.getMap().getMyLocation().getLatitude();
            double lon = googleMap.getMap().getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
        googleMap.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.getMap().clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = latLng.toString();
                }
                destination = googleMap.getMap().addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
            }
        });

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "";
            double latitude=0, longitude=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
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

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, myHour);
                calendar.set(Calendar.MINUTE, myMinute);
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    formattedTime = sdf.format(calendar.getTime());
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                    formattedTime = sdf.format(calendar.getTime());
                }

                locationTimeField.setText(formattedTime);
                locationDateField.setText(dayStr + "/" + monthStr);
                locationDateYearField.setText(String.valueOf(myYear));
                attackDelay.setChecked(true);
            } else {
                attackDelay.setChecked(false);
            }

            taskField.setText(text);
            if (longitude != 0 && latitude != 0) {
                destination = googleMap.getMap().addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(text)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
            }
        }
    }

    LinearLayout locationCallDateRing;
    TextView locationCallDateField, locationCallDateYearField, locationCallTimeField;
    AutoCompleteTextView searchFieldCall;
    ImageButton clearFieldCall;

    private void attachLocationCall() {
        location_call_layout = (LinearLayout) findViewById(R.id.location_call_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(location_call_layout);
        } else location_call_layout.setVisibility(View.VISIBLE);

        locationCallAddNumberButton = (ImageButton) findViewById(R.id.locationCallAddNumberButton);
        locationCallAddNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
            }
        });
        setImage(locationCallAddNumberButton);

        locationCallPhoneNumber = (FloatingEditText) findViewById(R.id.locationCallPhoneNumber);

        delayCallLayout = (LinearLayout) findViewById(R.id.delayCallLayout);
        delayCallLayout.setVisibility(View.GONE);

        attackCallDelay = (CheckBox) findViewById(R.id.attackCallDelay);
        attackCallDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                        delayCallLayout.startAnimation(slide);
                        delayCallLayout.setVisibility(View.VISIBLE);
                    } else delayCallLayout.setVisibility(View.VISIBLE);
                }
                else {
                    delayCallLayout.setVisibility(View.GONE);
                }
            }
        });

        clearFieldCall = (ImageButton) findViewById(R.id.clearButtonCall);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            clearFieldCall.setImageResource(R.drawable.ic_clear_white_24dp);
        } else clearFieldCall.setImageResource(R.drawable.ic_clear_grey600_24dp);
        clearFieldCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFieldCall.setText("");
            }
        });

        searchFieldCall = (AutoCompleteTextView) findViewById(R.id.searchFieldCall);
        searchFieldCall.setThreshold(3);
        adapter = new ArrayAdapter<>(
                BackupFileEdit.this, android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        searchFieldCall.addTextChangedListener(new TextWatcher() {
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
        searchFieldCall.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                GoogleMap mMap = locationCallMap.getMap();
                mMap.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                destination = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        locationCallDateRing = (LinearLayout) findViewById(R.id.locationCallDateRing);
        locationCallDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationCallDateField = (TextView) findViewById(R.id.locationCallDateField);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        locationCallDateField.setTypeface(typeface);
        locationCallDateField.setText(dayStr + "/" + monthStr);
        locationCallDateYearField = (TextView) findViewById(R.id.locationCallDateYearField);
        locationCallTimeField = (TextView) findViewById(R.id.locationCallTimeField);
        locationCallTimeField.setTypeface(typeface);
        locationCallTimeField.setText(formattedTime);
        locationCallTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        locationCallDateYearField.setTypeface(typeface);
        locationCallDateYearField.setText(String.valueOf(myYear));

        placesListCall = (Spinner) findViewById(R.id.placesListCall);
        placesListCall.setBackgroundColor(cSetter.getSpinnerStyle());
        if (spinnerArray.isEmpty()){
            placesListCall.setVisibility(View.GONE);
        } else {
            placesListCall.setVisibility(View.VISIBLE);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            placesListCall.setAdapter(spinnerArrayAdapter);
            placesListCall.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position > 0){
                        DB.open();
                        String placeName = spinnerArray.get(position);
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));

                            LatLng latLng = new LatLng(latitude, longitude);
                            locationCallMap.getMap().clear();
                            String title = taskField.getText().toString().trim();
                            if (title.matches("")) {
                                title = latLng.toString();
                            }
                            locationCallDestination = locationCallMap.getMap().addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                                    .draggable(true));

                            locationCallMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        locationCallMap = (MapFragment)getFragmentManager().findFragmentById(R.id.locationCallMap);
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            locationCallMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            locationCallMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            locationCallMap.getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            locationCallMap.getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            locationCallMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        locationCallMap.getMap().setMyLocationEnabled(true);
        if (locationCallMap.getMap().getMyLocation() != null) {
            double lat = locationCallMap.getMap().getMyLocation().getLatitude();
            double lon = locationCallMap.getMap().getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            locationCallMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
        locationCallMap.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationCallMap.getMap().clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = latLng.toString();
                }
                locationCallDestination = locationCallMap.getMap().addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
            }
        });

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "", number="";
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

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, myHour);
                calendar.set(Calendar.MINUTE, myMinute);
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    formattedTime = sdf.format(calendar.getTime());
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                    formattedTime = sdf.format(calendar.getTime());
                }

                locationCallTimeField.setText(formattedTime);
                locationCallDateField.setText(dayStr + "/" + monthStr);
                locationCallDateYearField.setText(String.valueOf(myYear));
                attackCallDelay.setChecked(true);
            } else {
                attackCallDelay.setChecked(false);
            }

            taskField.setText(text);
            locationCallPhoneNumber.setText(number);
            if (longitude != 0 && latitude != 0) {
                locationCallDestination = locationCallMap.getMap().addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(text)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
            }
        }
    }

    LinearLayout locationMessageDateRing;
    TextView locationMessageDateField, locationMessageDateYearField, locationMessageTimeField;
    AutoCompleteTextView searchFieldMessage;
    ImageButton clearFieldMessage;

    private void attachLocationMessage() {
        location_message_layout = (LinearLayout) findViewById(R.id.location_message_layout);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            fadeInAnimation(location_message_layout);
        } else location_message_layout.setVisibility(View.VISIBLE);

        locationMessageAddNumberButton = (ImageButton) findViewById(R.id.locationMessageAddNumberButton);
        locationMessageAddNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(BackupFileEdit.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
            }
        });
        setImage(locationMessageAddNumberButton);

        locationMessagePhoneNumber = (FloatingEditText) findViewById(R.id.locationMessagePhoneNumber);

        delayMessageLayout = (LinearLayout) findViewById(R.id.delayMessageLayout);
        delayMessageLayout.setVisibility(View.GONE);

        attackMessageDelay = (CheckBox) findViewById(R.id.attackMessageDelay);
        attackMessageDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                        delayMessageLayout.startAnimation(slide);
                        delayMessageLayout.setVisibility(View.VISIBLE);
                    } else delayMessageLayout.setVisibility(View.VISIBLE);
                }
                else {
                    delayMessageLayout.setVisibility(View.GONE);
                }
            }
        });

        clearFieldMessage = (ImageButton) findViewById(R.id.clearButtonMessage);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            clearFieldMessage.setImageResource(R.drawable.ic_clear_white_24dp);
        } else clearFieldMessage.setImageResource(R.drawable.ic_clear_grey600_24dp);
        clearFieldMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFieldMessage.setText("");
            }
        });

        searchFieldMessage = (AutoCompleteTextView) findViewById(R.id.searchFieldMessage);
        searchFieldMessage.setThreshold(3);
        adapter = new ArrayAdapter<>(
                BackupFileEdit.this, android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        searchFieldMessage.addTextChangedListener(new TextWatcher() {
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
        searchFieldMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                GoogleMap mMap = locationMessageMap.getMap();
                mMap.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                destination = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
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

        String formattedTime;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            formattedTime = sdf.format(cal.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
            formattedTime = sdf.format(cal.getTime());
        }

        locationMessageDateRing = (LinearLayout) findViewById(R.id.locationMessageDateRing);
        locationMessageDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationMessageDateField = (TextView) findViewById(R.id.locationMessageDateField);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        locationMessageDateField.setTypeface(typeface);
        locationMessageDateField.setText(dayStr + "/" + monthStr);
        locationMessageDateYearField = (TextView) findViewById(R.id.locationMessageDateYearField);
        locationMessageTimeField = (TextView) findViewById(R.id.locationMessageTimeField);
        locationMessageTimeField.setTypeface(typeface);
        locationMessageTimeField.setText(formattedTime);
        locationMessageTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        locationMessageDateYearField.setTypeface(typeface);
        locationMessageDateYearField.setText(String.valueOf(myYear));

        placesListMessage = (Spinner) findViewById(R.id.placesListMessage);
        placesListMessage.setBackgroundColor(cSetter.getSpinnerStyle());
        if (spinnerArray.isEmpty()){
            placesListMessage.setVisibility(View.GONE);
        } else {
            placesListMessage.setVisibility(View.VISIBLE);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            placesListMessage.setAdapter(spinnerArrayAdapter);
            placesListMessage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position > 0){
                        String placeName = spinnerArray.get(position);
                        DB.open();
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));

                            LatLng latLng = new LatLng(latitude, longitude);
                            locationMessageMap.getMap().clear();
                            String title = taskField.getText().toString().trim();
                            if (title.matches("")) {
                                title = latLng.toString();
                            }
                            locationMessageDestination = locationMessageMap.getMap().addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                                    .draggable(true));

                            locationMessageMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        locationMessageMap = (MapFragment)getFragmentManager().findFragmentById(R.id.locationMessageMap);
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            locationMessageMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            locationMessageMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            locationMessageMap.getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            locationMessageMap.getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            locationMessageMap.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        locationMessageMap.getMap().setMyLocationEnabled(true);
        if (locationMessageMap.getMap().getMyLocation() != null) {
            double lat = locationMessageMap.getMap().getMyLocation().getLatitude();
            double lon = locationMessageMap.getMap().getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            locationMessageMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
        locationMessageMap.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationMessageMap.getMap().clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = latLng.toString();
                }
                locationMessageDestination = locationMessageMap.getMap().addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
            }
        });

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getTask(id);
            String text = "", number="";
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

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, myHour);
                calendar.set(Calendar.MINUTE, myMinute);
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    formattedTime = sdf.format(calendar.getTime());
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                    formattedTime = sdf.format(calendar.getTime());
                }

                locationMessageTimeField.setText(formattedTime);
                locationMessageDateField.setText(dayStr + "/" + monthStr);
                locationMessageDateYearField.setText(String.valueOf(myYear));
                attackMessageDelay.setChecked(true);
            } else {
                attackMessageDelay.setChecked(false);
            }

            taskField.setText(text);
            locationMessagePhoneNumber.setText(number);
            if (longitude != 0 && latitude != 0) {
                locationMessageDestination = locationMessageMap.getMap().addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(text)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
            }
        }
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

    private boolean isLocationCallAttached(){
        location_call_layout = (LinearLayout) findViewById(R.id.location_call_layout);
        return location_call_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isLocationMessageAttached(){
        location_message_layout = (LinearLayout) findViewById(R.id.location_message_layout);
        return location_message_layout.getVisibility() == View.VISIBLE;
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
        location_call_layout = (LinearLayout) findViewById(R.id.location_call_layout);
        location_call_layout.setVisibility(View.GONE);
        location_message_layout = (LinearLayout) findViewById(R.id.location_message_layout);
        location_message_layout.setVisibility(View.GONE);
        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        skype_layout.setVisibility(View.GONE);
        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        application_layout.setVisibility(View.GONE);
    }

    private int getRepeat(int progress) {
        return new Interval(BackupFileEdit.this).getRepeatDays(progress);
    }

    private  void getTimeRepeat(int progress){
        if (progress == 0){
            repeatTimeIntLabel.setText(getString(R.string.interval_once));
        } else if (progress == 1){
            repeatTimeIntLabel.setText(getString(R.string.every_minute));
        } else if (progress == 2){
            repeatTimeIntLabel.setText(getString(R.string.every_five_minutes));
        } else if (progress == 3){
            repeatTimeIntLabel.setText(getString(R.string.every_ten_minutes));
        } else if (progress == 4){
            repeatTimeIntLabel.setText(getString(R.string.every_half_hour));
        } else if (progress == 5){
            repeatTimeIntLabel.setText(getString(R.string.every_hour));
        } else if (progress == 6){
            repeatTimeIntLabel.setText(getString(R.string.every_two_hours));
        } else if (progress == 7){
            repeatTimeIntLabel.setText(getString(R.string.every_five_hours));
        }
    }

    private void getAfterTime(int progress){
        if (progress == 0){
            afterTimeIntLabel.setText(getString(R.string.after_one_minute));
        } else if (progress == 1){
            afterTimeIntLabel.setText(getString(R.string.after_two_minutes));
        } else if (progress == 2){
            afterTimeIntLabel.setText(getString(R.string.after_five_minutes));
        } else if (progress == 3){
            afterTimeIntLabel.setText(getString(R.string.after_ten_minutes));
        } else if (progress == 4){
            afterTimeIntLabel.setText(getString(R.string.after_fifteen_minutes));
        } else if (progress == 5){
            afterTimeIntLabel.setText(getString(R.string.after_half_hour));
        } else if (progress == 6){
            afterTimeIntLabel.setText(getString(R.string.after_one_hour));
        } else if (progress == 7){
            afterTimeIntLabel.setText(getString(R.string.after_two_hours));
        } else if (progress == 8){
            afterTimeIntLabel.setText(getString(R.string.after_five_hours));
        }
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
            type = Constants.TYPE_LOCATION;
        } else if (isLocationCallAttached()){
            type = Constants.TYPE_LOCATION_CALL;
        } else if (isLocationMessageAttached()){
            type = Constants.TYPE_LOCATION_MESSAGE;
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
                if (isLocationCallAttached()){
                    locationCallPhoneNumber.setText(number);
                }
                if (isLocationMessageAttached()){
                    locationMessagePhoneNumber.setText(number);
                }
                if (isWeekDayReminderAttached() && attachAction.isChecked()){
                    weekPhoneNumber.setText(number);
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
        if (isLocationAttached() || isLocationCallAttached() || isLocationMessageAttached()){
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled || isNetworkEnabled) {
                if (isLocationAttached()) {
                    addLocation();
                } else if (isLocationCallAttached()) {
                    if (!checkNumber()) {
                        addLocationCall();
                    } else {
                        locationCallPhoneNumber.setError(getString(R.string.number_error));
                    }
                } else if (isLocationMessageAttached()){
                    if (!checkNumber()) {
                        if (!checkMessage()){
                            addLocationMessage();
                        } else {
                            taskField.setError(getString(R.string.message_empty_error));
                        }
                    } else {
                        locationMessagePhoneNumber.setError(getString(R.string.number_error));
                    }
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
            }
        }
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

        interval = new Interval(BackupFileEdit.this);
        String repeat = interval.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(), wednesdayCheck.isChecked(),
                thursdayCheck.isChecked(), fridayCheck.isChecked(), saturdayCheck.isChecked(), sundayCheck.isChecked());
        if (repeat.matches(Constants.NOTHING_CHECKED)) {
            Toast.makeText(BackupFileEdit.this, getString(R.string.weekday_nothing_checked), Toast.LENGTH_SHORT).show();
            return;
        }
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(task, type, 0, 0, 0, myHour, myMinute, 0, number,
                    0, 0, 0, 0, 0, uuID, repeat, 0, null, 0, -1, 0, categoryId);
            new WeekDayReceiver().setAlarm(BackupFileEdit.this, idN);
            if (gtx.isLinked() && weekTaskExport.isChecked()){
                exportToTasks(task, getWeekTime(myHour, myMinute, repeat), idN);
            }
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
            return;
        }

        DB.close();
        updatesHelper = new UpdatesHelper(BackupFileEdit.this);
        updatesHelper.updateWidget();
        finish();
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
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                    uuID, null, 0, null, 0, -1, 0, categoryId);
            alarm.setAlarm(BackupFileEdit.this, idN);
            DB.updateDateTime(idN);
            DB.close();
            updatesHelper = new UpdatesHelper(BackupFileEdit.this);
            updatesHelper.updateWidget();
            exportToCalendar(task, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            if (gtx.isLinked() && appTaskExport.isChecked()){
                exportToTasks(task, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
        }
        finish();
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
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                    uuID, null, 0, null, 0, -1, 0, categoryId);
            alarm.setAlarm(BackupFileEdit.this, idN);
            DB.updateDateTime(idN);
            DB.close();
            updatesHelper = new UpdatesHelper(BackupFileEdit.this);
            updatesHelper.updateWidget();
            exportToCalendar(task, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            if (gtx.isLinked() && skypeTaskExport.isChecked()){
                exportToTasks(task, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public boolean checkGooglePlayServicesAvailability()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 69);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return false;
        } else {
            Log.d("GooglePlayServicesUtil", "Result is: " + resultCode);
            return true;
        }
    }

    private boolean checkNumber(){
        if (isCallAttached()) {
            return phoneNumber.getText().toString().trim().matches("");
        } else if (isSkypeAttached()){
            return skypeUser.getText().toString().trim().matches("");
        } else if (isMessageAttached()){
            return messageNumber.getText().toString().trim().matches("");
        } else if (isLocationMessageAttached()){
            return locationMessagePhoneNumber.getText().toString().trim().matches("");
        } else if (isWeekDayReminderAttached() && attachAction.isChecked()) {
            return weekPhoneNumber.getText().toString().trim().matches("");
        } else
            return isLocationCallAttached() && locationCallPhoneNumber.getText().toString().trim().matches("");
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
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, repeat, 0, 0, 0, 0,
                    uuID, null, 0, null, 0, -1, 0, categoryId);
            alarm.setAlarm(BackupFileEdit.this, idN);
            DB.updateDateTime(idN);
            exportToCalendar(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            if (gtx.isLinked() && dateTaskExport.isChecked()){
                exportToTasks(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
            return;
        }

        DB.close();
        updatesHelper = new UpdatesHelper(BackupFileEdit.this);
        updatesHelper.updateWidget();

        finish();
    }

    private void saveTimeTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        interval = new Interval(BackupFileEdit.this);
        int time = interval.getAfterTimeCode(afterTimeInt.getProgress());
        int repeat = interval.getTimeRepeatCode(repeatTimeInt.getProgress());
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        final Calendar c = Calendar.getInstance();
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        mySeconds = c.get(Calendar.SECOND);
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, null, repeat, time, 0, 0, 0,
                    uuID, null, 0, null, 0, -1, 0, categoryId);
            alarm.setAlarm(BackupFileEdit.this, idN);
            DB.updateDateTime(idN);
            exportToCalendar(text, getTime(myDay, myMonth, myYear, myHour, myMinute, time), idN);
            if (gtx.isLinked() && timeTaskExport.isChecked()){
                exportToTasks(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
            return;
        }

        DB.close();
        updatesHelper = new UpdatesHelper(BackupFileEdit.this);
        updatesHelper.updateWidget();

        finish();
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
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                    uuID, null, 0, null, 0, -1, 0, categoryId);
            alarm.setAlarm(BackupFileEdit.this, idN);
            DB.updateDateTime(idN);
            DB.close();
            updatesHelper = new UpdatesHelper(BackupFileEdit.this);
            updatesHelper.updateWidget();
            exportToCalendar(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            if (gtx.isLinked() && callTaskExport.isChecked()){
                exportToTasks(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            finish();
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
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
        if (!isUID(uuID)) {
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                    uuID, null, 0, null, 0, -1, 0, categoryId);
            DB.updateDateTime(idN);
            alarm.setAlarm(BackupFileEdit.this, idN);
            DB.close();
            updatesHelper = new UpdatesHelper(BackupFileEdit.this);
            updatesHelper.updateWidget();
            exportToCalendar(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            if (gtx.isLinked() && messageTaskExport.isChecked()){
                exportToTasks(text, getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            finish();
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
        }
    }

    PositionDelayReceiver positionDelayReceiver = new PositionDelayReceiver();

    private void addLocation(){
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        LatLng dest = null;
        boolean isNull = false;
        try {
            dest = destination.getPosition();
        } catch (NullPointerException e){
            isNull = true;
        }
        if (!isNull) {
            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            DB = new DataBase(BackupFileEdit.this);
            DB.open();
            if (!isUID(uuID)) {
                Cursor cf = DB.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                if (attackDelay.isChecked()){
                    long newIds = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                            null, 0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
                    DB.updateDateTime(newIds);
                    DB.close();
                    positionDelayReceiver.setDelay(BackupFileEdit.this, newIds);
                    updatesHelper = new UpdatesHelper(BackupFileEdit.this);
                    updatesHelper.updateWidget();
                    finish();
                } else {
                    long ids = DB.insertTask(task, type, 0, 0, 0, 0, 0, 0, null,
                            0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
                    DB.updateDateTime(ids);
                    DB.close();
                    startService(new Intent(BackupFileEdit.this, GeolocationService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    updatesHelper = new UpdatesHelper(BackupFileEdit.this);
                    updatesHelper.updateWidget();
                    finish();
                }
            } else {
                Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.point_warning), Toast.LENGTH_SHORT).show();
        }
    }

    private void addLocationCall(){
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = locationCallPhoneNumber.getText().toString().trim();
        LatLng dest = null;
        boolean isNull = false;
        try {
            dest = locationCallDestination.getPosition();
        } catch (NullPointerException e){
            isNull = true;
        }
        if (!isNull) {
            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            DB = new DataBase(BackupFileEdit.this);
            DB.open();
            if (!isUID(uuID)) {
                Cursor cf = DB.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                if (attackCallDelay.isChecked()){
                    long newIds = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                            number, 0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
                    DB.updateDateTime(newIds);
                    DB.close();
                    positionDelayReceiver.setDelay(BackupFileEdit.this, newIds);
                } else {
                    long ids = DB.insertTask(task, type, 0, 0, 0, 0, 0, 0, number,
                            0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
                    DB.updateDateTime(ids);
                    DB.close();
                    startService(new Intent(BackupFileEdit.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else {
                Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
                return;
            }

            updatesHelper = new UpdatesHelper(BackupFileEdit.this);
            updatesHelper.updateWidget();
            finish();
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.point_warning), Toast.LENGTH_SHORT).show();
        }
    }

    private void addLocationMessage(){
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = locationMessagePhoneNumber.getText().toString().trim();
        LatLng dest = null;
        boolean isNull = false;
        try {
            dest = locationMessageDestination.getPosition();
        } catch (NullPointerException e){
            isNull = true;
        }
        if (!isNull) {
            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            DB = new DataBase(BackupFileEdit.this);
            DB.open();
            if (!isUID(uuID)) {
                Cursor cf = DB.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                if (attackMessageDelay.isChecked()){
                    DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number,
                            0, 0, 0, latitude, longitude, null, 0, null, 0, -1, 0, categoryId);
                    DB.updateDateTime(id);
                    DB.close();
                    positionDelayReceiver.setDelay(BackupFileEdit.this, id);
                } else {
                    long ids = DB.insertTask(task, type, 0, 0, 0, 0, 0, 0, number,
                            0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId);
                    DB.updateDateTime(ids);
                    DB.close();
                    startService(new Intent(BackupFileEdit.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else {
                Toast.makeText(BackupFileEdit.this, getString(R.string.same_uuid_error), Toast.LENGTH_SHORT).show();
                return;
            }

            updatesHelper = new UpdatesHelper(BackupFileEdit.this);
            updatesHelper.updateWidget();
            finish();
        } else {
            Toast.makeText(BackupFileEdit.this, getString(R.string.point_warning), Toast.LENGTH_SHORT).show();
        }
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
        Cursor a = DB.queryArchived();
        if (a != null && a.moveToFirst()){
            do {
                ids.add(a.getString(a.getColumnIndex(Constants.COLUMN_TECH_VAR)));
            } while (a.moveToNext());
        }
        return ids.contains(uuId);
    }

    private void exportToCalendar(String summary, long startTime, long id){
        sPrefs = new SharedPrefs(BackupFileEdit.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            new CalendarManager(BackupFileEdit.this).addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            new CalendarManager(BackupFileEdit.this).addEventToStock(summary, startTime);
        }
    }

    private void exportToTasks(String summary, long startTime, long id){
        long localId = new TasksData(BackupFileEdit.this).addTask(summary, null, 0, false, startTime,
                null, null, getString(R.string.string_task_from_just_reminder),
                null, null, null, 0, id, null, Constants.TASKS_NEED_ACTION, false);
        new TaskAsync(BackupFileEdit.this, summary, null, null,
                TasksConstants.INSERT_TASK, startTime, getString(R.string.string_task_from_just_reminder),
                localId).execute();
    }

    private long getWeekTime(int hour, int minute, String weekdays){
        TimeCount count = new TimeCount(BackupFileEdit.this);
        return count.getNextWeekdayTime(hour, minute, weekdays, 0);
    }

    private long getTime(int day, int month, int year, int hour, int minute, int after){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTimeInMillis() + (60 * 1000 * after);
    }

    @Override
    public void onClick(View v) {
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
                repeatDaysSkype.setText(String.valueOf(getRepeat(progress)));
                break;
            case R.id.repeatApp:
                repeatDaysApp.setText(String.valueOf(getRepeat(progress)));
                break;
            case R.id.repeatCallInt:
                repeatDaysCall.setText(String.valueOf(getRepeat(progress)));
                break;
            case R.id.repeatMessageInt:
                repeatDaysMessage.setText(String.valueOf(getRepeat(progress)));
                break;
            case R.id.repeatDateInt:
                repeatDays.setText(String.valueOf(getRepeat(progress)));
                break;
            case R.id.repeatTimeInt:
                if (progress == 0){
                    repeatTimeIntLabel.setText(getString(R.string.interval_once));
                } else if (progress == 1){
                    repeatTimeIntLabel.setText(getString(R.string.every_minute));
                } else if (progress == 2){
                    repeatTimeIntLabel.setText(getString(R.string.every_five_minutes));
                } else if (progress == 3){
                    repeatTimeIntLabel.setText(getString(R.string.every_ten_minutes));
                } else if (progress == 4){
                    repeatTimeIntLabel.setText(getString(R.string.every_half_hour));
                } else if (progress == 5){
                    repeatTimeIntLabel.setText(getString(R.string.every_hour));
                } else if (progress == 6){
                    repeatTimeIntLabel.setText(getString(R.string.every_two_hours));
                } else if (progress == 7){
                    repeatTimeIntLabel.setText(getString(R.string.every_five_hours));
                }
                break;
            case R.id.afterTimeInt:
                if (progress == 0){
                    afterTimeIntLabel.setText(getString(R.string.after_one_minute));
                } else if (progress == 1){
                    afterTimeIntLabel.setText(getString(R.string.after_two_minutes));
                } else if (progress == 2){
                    afterTimeIntLabel.setText(getString(R.string.after_five_minutes));
                } else if (progress == 3){
                    afterTimeIntLabel.setText(getString(R.string.after_ten_minutes));
                } else if (progress == 4){
                    afterTimeIntLabel.setText(getString(R.string.after_fifteen_minutes));
                } else if (progress == 5){
                    afterTimeIntLabel.setText(getString(R.string.after_half_hour));
                } else if (progress == 6){
                    afterTimeIntLabel.setText(getString(R.string.after_one_hour));
                } else if (progress == 7){
                    afterTimeIntLabel.setText(getString(R.string.after_two_hours));
                } else if (progress == 8){
                    afterTimeIntLabel.setText(getString(R.string.after_five_hours));
                }
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
        if (isLocationCallAttached()){
            if (attackCallDelay.isChecked()){
                if (delayCallLayout.getVisibility() == View.VISIBLE) {
                    locationCallDateField.setText(dayStr + "/" + monthStr);
                    locationCallDateYearField.setText(String.valueOf(myYear));
                }
            }
        }
        if (isLocationMessageAttached()){
            if (attackMessageDelay.isChecked()){
                if (delayMessageLayout.getVisibility() == View.VISIBLE) {
                    locationMessageDateField.setText(dayStr + "/" + monthStr);
                    locationMessageDateYearField.setText(String.valueOf(myYear));
                }
            }
        }
    }

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute, sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            String formattedTime;
            if (new SharedPrefs(BackupFileEdit.this).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(c.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(c.getTime());
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
            if (isLocationCallAttached()){
                if (attackCallDelay.isChecked()){
                    if (delayCallLayout.getVisibility() == View.VISIBLE) locationCallTimeField.setText(formattedTime);
                }
            }
            if (isLocationMessageAttached()) {
                if (attackMessageDelay.isChecked()) {
                    if (delayMessageLayout.getVisibility() == View.VISIBLE) locationMessageTimeField.setText(formattedTime);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DB != null) DB.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}