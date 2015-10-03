package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import com.cray.software.justreminder.adapters.SimpleAdapter;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.Category;
import com.cray.software.justreminder.dialogs.utils.ContactsList;
import com.cray.software.justreminder.dialogs.utils.LedColor;
import com.cray.software.justreminder.dialogs.utils.SelectApplication;
import com.cray.software.justreminder.dialogs.utils.SelectMelody;
import com.cray.software.justreminder.dialogs.utils.TargetRadius;
import com.cray.software.justreminder.fragments.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.LED;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.LocationType;
import com.cray.software.justreminder.reminder.MonthdayType;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.TimerType;
import com.cray.software.justreminder.reminder.Type;
import com.cray.software.justreminder.reminder.WeekdayType;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.spinner.SpinnerItem;
import com.cray.software.justreminder.spinner.TitleNavigationAdapter;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ReminderManager extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener, View.OnTouchListener,
        DatePickerDialog.OnDateSetListener, CompoundButton.OnCheckedChangeListener, MapListener {

    private LinearLayout action_layout;
    private FloatingEditText phoneNumber, messageNumber, weekPhoneNumber;
    private TextView callDate, callTime, dateField, timeField, callYearDate, dateYearField,
            messageDate, messageYearDate, messageTime, weekTimeField;
    private ImageButton weekAddNumberButton;

    private LinearLayout delayLayout;
    private LinearLayout navContainer;
    private CheckBox attackDelay;
    private CheckBox timeExport, messageExport, dateExport, callExport, weekExport;

    private int myHour = 0;
    private int myMinute = 0;
    private int mySeconds = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;

    private ProgressDialog pd;
    private DataBase DB = new DataBase(ReminderManager.this);

    private boolean isDelayed = false;

    private ColorSetter cSetter = new ColorSetter(ReminderManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(ReminderManager.this);
    private GTasksHelper gtx = new GTasksHelper(ReminderManager.this);

    private long id;
    private String categoryId;
    private String type, melody = null, selectedPackage = null;
    private int radius = -1, ledColor = 0;
    private Toolbar toolbar;
    private Spinner spinner;
    private FloatingEditText taskField;
    private TextView category;
    private FloatingActionButton mFab;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;
    private boolean isAnimation = false, isCalendar = false, isStock = false, isDark = false;

    private Type remControl = new Type(this);
    private Reminder item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(ReminderManager.this);
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

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_add:
                                saveTask();
                                return true;
                            case R.id.action_custom_melody:
                                loadMelody();
                                return true;
                            case R.id.action_custom_radius:
                                selectRadius();
                                return true;
                            case R.id.action_custom_color:
                                selectColor();
                                return true;
                            case MENU_ITEM_DELETE:
                                deleteReminder();
                                return true;
                        }
                        return true;
                    }
                });

        toolbar.setOnTouchListener(this);

        navContainer = (LinearLayout) findViewById(R.id.navContainer);
        spinner = (Spinner) findViewById(R.id.navSpinner);
        taskField = (FloatingEditText) findViewById(R.id.task_message);
        taskField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (map != null) map.setMarkerTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ImageButton insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        insertVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });

        category = (TextView) findViewById(R.id.category);
        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCategory();
            }
        });

        LinearLayout layoutContainer = (LinearLayout) findViewById(R.id.layoutContainer);
        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        findViewById(R.id.windowBackground).setOnTouchListener(this);

        DB.open();
        Cursor cf = DB.queryCategories();
        if (cf != null && cf.moveToFirst()) {
            String title = cf.getString(cf.getColumnIndex(Constants.COLUMN_TEXT));
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            category.setText(title);
        }
        if (cf != null) cf.close();

        setUpNavigation();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        mFab = new FloatingActionButton(ReminderManager.this);
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorStatus());
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ViewUtils.hide(ReminderManager.this, mFab, isAnimation);
                return false;
            }
        });
        mFab.setSize(com.getbase.floatingactionbutton.FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);
        int i = intent.getIntExtra(Constants.EDIT_WIDGET, 0);
        if (i != 0){
            new AlarmReceiver().cancelAlarm(ReminderManager.this, id);
            new WeekDayReceiver().cancelAlarm(ReminderManager.this, id);
            new MonthDayReceiver().cancelAlarm(ReminderManager.this, id);
            new DelayReceiver().cancelAlarm(ReminderManager.this, id);
            new PositionDelayReceiver().cancelDelay(ReminderManager.this, id);
            new DisableAsync(ReminderManager.this).execute();
        }

        spinner.setSelection(sPrefs.loadInt(Prefs.LAST_USED_REMINDER));

        if (id != 0){
            item = remControl.getItem(id);
            if (item != null) {
                type = item.getType();
                radius = item.getRadius();
                ledColor = item.getColor();
                melody = item.getWeekdays();
                String catId = item.getCategoryId();
                if (radius == 0) radius = -1;

                if (catId != null && !catId.matches("")) categoryId = catId;

                if (categoryId != null && !categoryId.matches("")) {
                    Cursor cx = DB.getCategory(categoryId);
                    if (cx != null && cx.moveToFirst()) {
                        String title = cx.getString(cx.getColumnIndex(Constants.COLUMN_TEXT));
                        category.setText(title);
                    }
                    if (cf != null) cf.close();
                }
            }

            if (type.matches(Constants.TYPE_REMINDER)) {
                spinner.setSelection(0);
            } else if (type.matches(Constants.TYPE_TIME)){
                spinner.setSelection(1);
            } else if (type.matches(Constants.TYPE_CALL)){
                spinner.setSelection(3);
            } else if (type.matches(Constants.TYPE_MESSAGE)){
                spinner.setSelection(4);
            } else if (type.startsWith(Constants.TYPE_LOCATION)){
                spinner.setSelection(5);
            } else if (type.startsWith(Constants.TYPE_WEEKDAY)){
                spinner.setSelection(2);
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                spinner.setSelection(6);
            } else if (type.startsWith(Constants.TYPE_APPLICATION)){
                spinner.setSelection(7);
            } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                spinner.setSelection(8);
            } else if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
                spinner.setSelection(9);
            } else {
                spinner.setSelection(0);
            }
        }
        clearViews();
    }

    private void clearViews() {
        findViewById(R.id.by_date_layout).setVisibility(View.GONE);
        findViewById(R.id.call_layout).setVisibility(View.GONE);
        findViewById(R.id.weekday_layout).setVisibility(View.GONE);
        findViewById(R.id.message_layout).setVisibility(View.GONE);
        findViewById(R.id.after_time_layout).setVisibility(View.GONE);
        findViewById(R.id.geolocationlayout).setVisibility(View.GONE);
        findViewById(R.id.skype_layout).setVisibility(View.GONE);
        findViewById(R.id.application_layout).setVisibility(View.GONE);
        findViewById(R.id.monthDayLayout).setVisibility(View.GONE);
        findViewById(R.id.locationOutLayout).setVisibility(View.GONE);
    }

    private void showHelp() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReminderManager.this);
        alertDialog.setMessage(getString(R.string.gps_text));
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void changeCategory() {
        DB = new DataBase(ReminderManager.this);
        DB.open();
        final ArrayList<Category> categories = new ArrayList<>();
        Cursor c = DB.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                categories.add(new Category(title, uuId));
            } while (c.moveToNext());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.string_select_category));
        builder.setAdapter(new SimpleAdapter(ReminderManager.this,
                DB.queryCategories()), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                category.setText(categories.get(which).getTitle());
                categoryId = categories.get(which).getUuID();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpNavigation() {
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date_title), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.after_time_title), R.drawable.ic_access_time_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_weekdays_title), R.drawable.ic_alarm_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call_title), R.drawable.ic_call_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.send_message_title), R.drawable.ic_message_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_location_title), R.drawable.ic_place_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.skype_reminder_type), R.drawable.skype_icon_white));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application_reminder_type), R.drawable.ic_launch_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_by_day_of_month), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_place_out), R.drawable.ic_beenhere_white_24dp));
        } else {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date_title), R.drawable.ic_event_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.after_time_title), R.drawable.ic_access_time_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_weekdays_title), R.drawable.ic_alarm_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call_title), R.drawable.ic_call_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.send_message_title), R.drawable.ic_message_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_location_title), R.drawable.ic_place_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.skype_reminder_type), R.drawable.skype_icon));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application_reminder_type), R.drawable.ic_launch_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_by_day_of_month), R.drawable.ic_event_grey600_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_place_out), R.drawable.ic_beenhere_grey600_24dp));
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void deleteReminder() {
        DB.open();
        Cursor c = DB.getReminder(id);
        if (c != null && c.moveToFirst()) {
            int isArchived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
            if (isArchived == 1) {
                Reminder.delete(id, this);
                Toast.makeText(ReminderManager.this, getString(R.string.string_deleted), Toast.LENGTH_SHORT).show();
            } else Reminder.moveToTrash(id, this);
            finish();
        }
        if (c != null) c.close();
    }

    private void selectRadius() {
        Intent i = new Intent(ReminderManager.this, TargetRadius.class);
        i.putExtra("item", 1);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_RADIUS);
    }

    private void selectColor() {
        Intent i = new Intent(ReminderManager.this, LedColor.class);
        i.putExtra(Constants.BIRTHDAY_INTENT_ID, 4);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_COLOR);
    }

    private void loadMelody() {
        pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.sounds_loading_text), true);
        pickSounds(pd);
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SharedPrefs sPrefs = new SharedPrefs(ReminderManager.this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mFab.getVisibility() == View.GONE){
                    ViewUtils.show(ReminderManager.this, mFab, isAnimation);
                } else {
                    restoreTask();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isSame(){
        boolean is = false;
        if (spinner.getSelectedItemPosition() == 0 && type.matches(Constants.TYPE_REMINDER)) is = true;
        if (spinner.getSelectedItemPosition() == 1 && type.matches(Constants.TYPE_TIME)) is = true;
        if (spinner.getSelectedItemPosition() == 2 && type.startsWith(Constants.TYPE_WEEKDAY)) is = true;
        if (spinner.getSelectedItemPosition() == 3 && type.matches(Constants.TYPE_CALL)) is = true;
        if (spinner.getSelectedItemPosition() == 4 && type.matches(Constants.TYPE_MESSAGE)) is = true;
        if (spinner.getSelectedItemPosition() == 5 && type.startsWith(Constants.TYPE_LOCATION)) is = true;
        if (spinner.getSelectedItemPosition() == 6 && type.startsWith(Constants.TYPE_SKYPE)) is = true;
        if (spinner.getSelectedItemPosition() == 7 && type.startsWith(Constants.TYPE_APPLICATION)) is = true;
        if (spinner.getSelectedItemPosition() == 8 && type.startsWith(Constants.TYPE_MONTHDAY)) is = true;
        if (spinner.getSelectedItemPosition() == 9 && type.startsWith(Constants.TYPE_LOCATION_OUT)) is = true;
        return is;
    }

    private CheckBox dateTaskExport;
    private EditText repeatDays;

    private void attachDateReminder(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        ViewUtils.fadeInAnimation(by_date_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_REMINDER);
        dateType.inflateView(R.id.by_date_layout);
        remControl = dateType;

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

        LinearLayout dateRing = (LinearLayout) findViewById(R.id.dateRing);
        dateRing.setOnClickListener(this);

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        dateExport = (CheckBox) findViewById(R.id.dateExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            dateExport.setVisibility(View.VISIBLE);
        }

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
        timeField.setText(TimeUtil.getTime(cal.getTime(), sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        timeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        repeatDays.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setOnSeekBarChangeListener(this);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDays.setText(String.valueOf(repeatDateInt.getProgress()));

        if (id != 0 && isSame()) {
            String text = "";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (item != null){
                text = item.getTitle();
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                repCode = item.getRepCode();
                exp = item.getExport();
                expTasks = item.getCode();
            }

            if (exp == 1){
                dateExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                dateTaskExport.setChecked(true);
            }

            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);

            if (myMonth < 9) monthStr = "0" + (myMonth + 1);
            else monthStr = String.valueOf(myMonth + 1);

            cal.set(Calendar.HOUR_OF_DAY, myHour);
            cal.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            timeField.setText(TimeUtil.getTime(cal.getTime(), sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            dateField.setText(dayStr + "/" + monthStr);
            dateYearField.setText(String.valueOf(myYear));
            repeatDateInt.setProgress(repCode);
            repeatDays.setText(String.valueOf(repCode));
        }
    }

    private CheckBox monthDayExport, monthDayTaskExport, monthDayAttachAction;
    private LinearLayout monthDayActionLayout;
    private TextView monthDayField, monthDayTimeField;
    private RadioButton monthDayCallCheck, monthDayMessageCheck, dayCheck, lastCheck;
    private ImageButton monthDayAddNumberButton;
    private FloatingEditText monthDayPhoneNumber;

    private void attachMonthDay(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        ViewUtils.fadeInAnimation(monthDayLayout, isAnimation);

        MonthdayType dateType = new MonthdayType(this);
        dateType.inflateView(R.id.monthDayLayout);
        remControl = dateType;

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

        monthDayExport = (CheckBox) findViewById(R.id.monthDayExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
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
                    if (new Permissions(ReminderManager.this).checkPermission(Permissions.CALL_PHONE)) {
                        ViewUtils.showOver(monthDayActionLayout, isAnimation);
                        monthDayAddNumberButton = (ImageButton) findViewById(R.id.monthDayAddNumberButton);
                        monthDayAddNumberButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
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
                        new Permissions(ReminderManager.this)
                                .requestPermission(ReminderManager.this,
                                        new String[]{Permissions.CALL_PHONE, Permissions.SEND_SMS}, 110);
                    }
                } else {
                    ViewUtils.hideOver(monthDayActionLayout, isAnimation);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (monthDayAttachAction.isChecked()) ViewUtils.showOver(monthDayActionLayout, isAnimation);

        if (id != 0 && isSame()) {
            String text = "";
            String number = "";
            int exp = 0;
            int expTasks = 0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                exp = item.getExport();
                expTasks = item.getCode();
            }

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
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time,
                            distance, mLocList);
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time,
                            distance, mLocList);
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
        taskField.setHint(getString(R.string.tast_hint));

        cSetter = new ColorSetter(ReminderManager.this);

        LinearLayout weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        ViewUtils.fadeInAnimation(weekday_layout, isAnimation);

        WeekdayType dateType = new WeekdayType(this);
        dateType.inflateView(R.id.weekday_layout);
        remControl = dateType;

        weekExport = (CheckBox) findViewById(R.id.weekExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            weekExport.setVisibility(View.VISIBLE);
        }

        weekTaskExport = (CheckBox) findViewById(R.id.weekTaskExport);
        if (gtx.isLinked()){
            weekTaskExport.setVisibility(View.VISIBLE);
        }

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            c.set(Calendar.YEAR, myYear);
            c.set(Calendar.MONTH, myMonth);
            c.set(Calendar.DAY_OF_MONTH, myDay);
            c.set(Calendar.HOUR_OF_DAY, myHour);
            c.set(Calendar.MINUTE, myMinute);
        } else {
            myYear = c.get(Calendar.YEAR);
            myMonth = c.get(Calendar.MONTH);
            myDay = c.get(Calendar.DAY_OF_MONTH);
            myHour = c.get(Calendar.HOUR_OF_DAY);
            myMinute = c.get(Calendar.MINUTE);
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
                    if (new Permissions(ReminderManager.this).checkPermission(Permissions.CALL_PHONE)){
                        ViewUtils.showOver(action_layout, isAnimation);
                        weekAddNumberButton = (ImageButton) findViewById(R.id.weekAddNumberButton);
                        weekAddNumberButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
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
                        new Permissions(ReminderManager.this)
                                .requestPermission(ReminderManager.this,
                                        new String[]{Permissions.CALL_PHONE, Permissions.SEND_SMS}, 111);
                    }
                } else {
                    ViewUtils.hideOver(action_layout, isAnimation);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (attachAction.isChecked()) ViewUtils.showOver(action_layout, isAnimation);

        if (id != 0 && isSame()) {
            String text = "";
            String type = "";
            String weekdays = "";
            String number = "";
            int exp = 0;
            int expTasks = 0;
            if (item != null) {
                myHour = item.getHour();
                myMinute = item.getMinute();
                exp = item.getExport();
                expTasks = item.getCode();
                text = item.getTitle();
                type = item.getType();
                weekdays = item.getWeekdays();
                number = item.getNumber();
            }

            if (exp == 1){
                weekExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                weekTaskExport.setChecked(true);
            }

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
        taskField.setHint(getString(R.string.tast_hint));

        cSetter = new ColorSetter(ReminderManager.this);
        LinearLayout after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        ViewUtils.fadeInAnimation(after_time_layout, isAnimation);

        TimerType dateType = new TimerType(this);
        dateType.inflateView(R.id.after_time_layout);
        remControl = dateType;

        timeExport = (CheckBox) findViewById(R.id.timeExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            timeExport.setVisibility(View.VISIBLE);
        }

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
        if (isDark) deleteButton.setImageResource(R.drawable.ic_backspace_white_24dp);
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
        if (b1 != null) {
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
        }

        repeatMinutes = (EditText) findViewById(R.id.repeatMinutes);
        repeatMinutes.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatMinutesSeek = (SeekBar) findViewById(R.id.repeatMinutesSeek);
        repeatMinutesSeek.setOnSeekBarChangeListener(this);
        repeatMinutes.setText(String.valueOf(repeatMinutesSeek.getProgress()));

        if (id != 0 && isSame()) {
            String text = "";
            int  exp = 0, expTasks = 0, repeat = 0;
            long afterTime = 0;
            if (item != null){
                text = item.getTitle();
                afterTime = item.getRepMinute();
                exp = item.getExport();
                expTasks = item.getCode();
                repeat = item.getRepCode();
            }

            generateString(afterTime);

            if (exp == 1){
                timeExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                timeTaskExport.setChecked(true);
            }

            if (repeat < repeatMinutesSeek.getMax()) repeatMinutesSeek.setProgress(repeat);
            repeatMinutes.setText(String.valueOf(repeat));

            taskField.setText(text);
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

    private CheckBox skypeExport, skypeTaskExport;
    private EditText skypeUser, repeatDaysSkype;
    private RadioButton skypeCall;
    private RadioButton skypeVideo;
    private TextView skypeDate, skypeYearDate, skypeTime;

    private void attachSkype(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        ViewUtils.fadeInAnimation(skype_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_SKYPE);
        dateType.inflateView(R.id.skype_layout);
        remControl = dateType;

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

        LinearLayout skypeDateRing = (LinearLayout) findViewById(R.id.skypeDateRing);
        skypeDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        skypeExport = (CheckBox) findViewById(R.id.skypeExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            skypeExport.setVisibility(View.VISIBLE);
        }

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

        if (id != 0 && isSame()) {
            String text="", number="", type="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (item != null){
                text = item.getTitle();
                type = item.getType();
                number = item.getNumber();
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                repCode = item.getRepCode();
                exp = item.getExport();
                expTasks = item.getCode();
            }

            if (exp == 1){
                skypeExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                skypeTaskExport.setChecked(true);
            }

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

    private CheckBox appExport, appTaskExport;
    private EditText browseLink, repeatDaysApp;
    private RadioButton application, browser;
    private TextView appDate, appYearDate, appTime, applicationName;
    private RelativeLayout applicationLayout;

    private void attachApplication(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout application_layout = (LinearLayout) findViewById(R.id.application_layout);
        ViewUtils.fadeInAnimation(application_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_APPLICATION);
        dateType.inflateView(R.id.application_layout);
        remControl = dateType;

        browseLink = (EditText) findViewById(R.id.browseLink);
        browseLink.setVisibility(View.GONE);
        applicationLayout = (RelativeLayout) findViewById(R.id.applicationLayout);
        applicationLayout.setVisibility(View.VISIBLE);
        applicationName = (TextView) findViewById(R.id.applicationName);

        ImageButton pickApplication = (ImageButton) findViewById(R.id.pickApplication);
        pickApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.loading_applications_message), true);
                pickApplications(pd);
            }
        });
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (isDark){
            pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        } else pickApplication.setImageResource(R.drawable.ic_launch_grey600_24dp);

        application = (RadioButton) findViewById(R.id.application);
        application.setChecked(true);
        browser = (RadioButton) findViewById(R.id.browser);
        application.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    if (isAnimation) {
                        ViewUtils.collapse(applicationLayout);
                        ViewUtils.expand(browseLink);
                    } else {
                        applicationLayout.setVisibility(View.GONE);
                        browseLink.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isAnimation) {
                        ViewUtils.collapse(browseLink);
                        ViewUtils.expand(applicationLayout);
                    } else {
                        browseLink.setVisibility(View.GONE);
                        applicationLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

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

        LinearLayout appDateRing = (LinearLayout) findViewById(R.id.appDateRing);
        appDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        appExport = (CheckBox) findViewById(R.id.appExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            appExport.setVisibility(View.VISIBLE);
        }

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

        if (id != 0 && isSame()) {
            String text="", number="", type="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (item != null){
                text = item.getTitle();
                type = item.getType();
                number = item.getNumber();
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                repCode = item.getRepCode();
                exp = item.getExport();
                expTasks = item.getCode();
            }

            if (exp == 1){
                appExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                appTaskExport.setChecked(true);
            }

            if(type.matches(Constants.TYPE_APPLICATION)){
                application.setChecked(true);
                selectedPackage = number;
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String name = (String)((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                applicationName.setText(name);

            }
            if(type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                browser.setChecked(true);
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
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout call_layout = (LinearLayout) findViewById(R.id.call_layout);
        ViewUtils.fadeInAnimation(call_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_CALL);
        dateType.inflateView(R.id.call_layout);
        remControl = dateType;

        ImageButton addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                pickContacts(pd);
            }
        });
        ViewUtils.setImage(addNumberButton, isDark);

        phoneNumber = (FloatingEditText) findViewById(R.id.phoneNumber);

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

        LinearLayout callDateRing = (LinearLayout) findViewById(R.id.callDateRing);
        callDateRing.setOnClickListener(this);

        callExport = (CheckBox) findViewById(R.id.callExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            callExport.setVisibility(View.VISIBLE);
        }

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

        SeekBar repeatCallInt = (SeekBar) findViewById(R.id.repeatCallInt);
        repeatCallInt.setOnSeekBarChangeListener(this);
        repeatCallInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysCall.setText(String.valueOf(repeatCallInt.getProgress()));

        if (id != 0 && isSame()) {
            String text="", number="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                repCode = item.getRepCode();
                exp = item.getExport();
                expTasks = item.getCode();
            }

            if (exp == 1){
                callExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                callTaskExport.setChecked(true);
            }

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
        taskField.setHint(getString(R.string.message_field_hint));

        LinearLayout message_layout = (LinearLayout) findViewById(R.id.message_layout);
        ViewUtils.fadeInAnimation(message_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_MESSAGE);
        dateType.inflateView(R.id.message_layout);
        remControl = dateType;

        ImageButton addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                pickContacts(pd);
            }
        });
        ViewUtils.setImage(addMessageNumberButton, isDark);

        messageNumber = (FloatingEditText) findViewById(R.id.messageNumber);

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

        LinearLayout messageDateRing = (LinearLayout) findViewById(R.id.messageDateRing);
        messageDateRing.setOnClickListener(this);

        messageExport = (CheckBox) findViewById(R.id.messageExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            messageExport.setVisibility(View.VISIBLE);
        }

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

        SeekBar repeatMessageInt = (SeekBar) findViewById(R.id.repeatMessageInt);
        repeatMessageInt.setOnSeekBarChangeListener(this);
        repeatMessageInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysMessage.setText(String.valueOf(repeatMessageInt.getProgress()));

        if (id != 0 && isSame()) {
            String text="", number="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                repCode = item.getRepCode();
                exp = item.getExport();
                expTasks = item.getCode();
            }

            if (exp == 1){
                messageExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                messageTaskExport.setChecked(true);
            }

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
        if (isLocationOutAttached()) {
            ViewUtils.fadeOutAnimation(mapContainerOut, isAnimation);
            ViewUtils.fadeInAnimation(specsContainerOut, isAnimation);
        }
    }

    @Override
    public void placeName(String name) {

    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(ReminderManager.this);
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
            if(addresses == null || addresses.size() == 0){
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
                        ReminderManager.this, android.R.layout.simple_dropdown_item_1line, namesList);
                if (isLocationAttached()){
                    searchField.setAdapter(adapter);
                }
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
    private MapFragment map;

    private boolean isMapVisible(){
        return mapContainer != null && mapContainer.getVisibility() == View.VISIBLE;
    }

    private void attachLocation() {
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        ViewUtils.fadeInAnimation(geolocationlayout, isAnimation);

        LocationType dateType = new LocationType(this, Constants.TYPE_LOCATION);
        dateType.inflateView(R.id.geolocationlayout);
        remControl = dateType;

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
        specsContainer = (ScrollView) findViewById(R.id.specsContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);

        map = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.setListener(this);
        map.enableTouch(true);
        map.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        attackDelay = (CheckBox) findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isAnimation) {
                        ViewUtils.expand(delayLayout);
                    } else delayLayout.setVisibility(View.VISIBLE);
                } else {
                    if (isAnimation) {
                        ViewUtils.collapse(delayLayout);
                    } else delayLayout.setVisibility(View.GONE);
                }
            }
        });

        if (attackDelay.isChecked()) {
            if (isAnimation) {
                ViewUtils.expand(delayLayout);
            } else delayLayout.setVisibility(View.VISIBLE);
        }

        ImageButton clearField = (ImageButton) findViewById(R.id.clearButton);
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
                ReminderManager.this, android.R.layout.simple_dropdown_item_1line, namesList);
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
                if (map != null) map.addMarker(pos, title, true, true, radius);
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
                            pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
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

        if (attachLocationAction.isChecked()) ViewUtils.showOver(actionLocation, isAnimation);

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

        if (id != 0 && isSame()) {
            String text = "", number = null, remType = "";
            double latitude = 0.0, longitude = 0.0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                remType = item.getType();
                latitude = item.getPlace()[0];
                longitude = item.getPlace()[1];
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                radius = item.getRadius();
            }

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
                isDelayed = true;
            } else {
                attackDelay.setChecked(false);
            }

            if (remType.matches(Constants.TYPE_LOCATION_CALL) || remType.matches(Constants.TYPE_LOCATION_MESSAGE)){
                attachLocationAction.setChecked(true);
                phoneNumberLocation = (FloatingEditText) findViewById(R.id.phoneNumberLocation);
                phoneNumberLocation.setText(number);
                if (remType.matches(Constants.TYPE_LOCATION_CALL)){
                    callCheckLocation = (RadioButton) findViewById(R.id.callCheckLocation);
                    callCheckLocation.setChecked(true);
                } else {
                    messageCheckLocation = (RadioButton) findViewById(R.id.messageCheckLocation);
                    messageCheckLocation.setChecked(true);
                }
            } else {
                attachLocationAction.setChecked(false);
            }

            Log.d(Constants.LOG_TAG, "lat " + latitude + ", long " + longitude);

            taskField.setText(text);
            if (map != null) map.addMarker(new LatLng(latitude, longitude), text, true, false, radius);
        }
    }

    private ImageButton addNumberButtonLocationOut;
    private LinearLayout actionLocationOut;
    private LinearLayout delayLayoutOut;
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView locationOutDateField, locationOutDateYearField, locationOutTimeField, currentLocation,
            mapLocation, radiusMark;
    private CheckBox attachLocationOutAction, attachDelayOut;
    private RadioButton callCheckLocationOut, messageCheckLocationOut, currentCheck, mapCheck;
    private FloatingEditText phoneNumberLocationOut;
    private MapFragment mapOut;

    private boolean isMapOutVisible(){
        return mapContainerOut != null && mapContainerOut.getVisibility() == View.VISIBLE;
    }

    private void attachLocationOut() {
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        ViewUtils.fadeInAnimation(locationOutLayout, isAnimation);

        LocationType dateType = new LocationType(this, Constants.TYPE_LOCATION_OUT);
        dateType.inflateView(R.id.locationOutLayout);
        remControl = dateType;

        delayLayoutOut = (LinearLayout) findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) findViewById(R.id.specsContainerOut);
        mapContainerOut = (RelativeLayout) findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        mapOut = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapOut);
        mapOut.setListener(this);
        mapOut.enableTouch(true);
        mapOut.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        attachDelayOut = (CheckBox) findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isAnimation) {
                        ViewUtils.expand(delayLayoutOut);
                    } else delayLayoutOut.setVisibility(View.VISIBLE);
                } else {
                    if (isAnimation) {
                        ViewUtils.collapse(delayLayoutOut);
                    } else delayLayoutOut.setVisibility(View.GONE);
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
        currentLocation = (TextView) findViewById(R.id.currentLocation);
        mapLocation = (TextView) findViewById(R.id.mapLocation);
        radiusMark = (TextView) findViewById(R.id.radiusMark);

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
                            pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
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

        if (attachLocationOutAction.isChecked()) ViewUtils.showOver(actionLocationOut, isAnimation);

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
            if (mapOut != null) mapOut.addMarker(curPlace, null, true, true, radius);
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (id != 0 && isSame()) {
            String text = "", number = null, remType = "";
            double latitude = 0, longitude = 0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                remType = item.getType();
                latitude = item.getPlace()[0];
                longitude = item.getPlace()[1];
                myHour = item.getHour();
                myMinute = item.getMinute();
                myDay = item.getDay();
                myMonth = item.getMonth();
                myYear = item.getYear();
                radius = item.getRadius();
            }

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
                isDelayed = true;
            } else {
                attachDelayOut.setChecked(false);
            }

            if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL) || remType.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                attachLocationOutAction.setChecked(true);
                phoneNumberLocationOut = (FloatingEditText) findViewById(R.id.phoneNumberLocationOut);
                phoneNumberLocationOut.setText(number);
                if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL)){
                    callCheckLocationOut = (RadioButton) findViewById(R.id.callCheckLocationOut);
                    callCheckLocationOut.setChecked(true);
                } else {
                    messageCheckLocationOut = (RadioButton) findViewById(R.id.messageCheckLocationOut);
                    messageCheckLocationOut.setChecked(true);
                }
            } else {
                attachLocationOutAction.setChecked(false);
            }

            Log.d(Constants.LOG_TAG, "lat " + latitude + ", long " + longitude);

            taskField.setText(text);
            LatLng pos = new LatLng(latitude, longitude);
            if (mapOut != null) mapOut.addMarker(pos, text, true, true, radius);
            mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
            mapCheck.setChecked(true);
        }
    }

    private void detachCurrentView(){
        if (remControl.getView() != 0) {
            ViewUtils.fadeOutAnimation(findViewById(remControl.getView()), isAnimation);
        }
    }

    private String getTaskType(){
        return remControl.getType();
    }

    private void pickApplications(final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                final ArrayList<String> contacts = new ArrayList<>();
                contacts.clear();
                final PackageManager pm = getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                for (ApplicationInfo packageInfo : packages) {
                    contacts.add(packageInfo.packageName);
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
                        Intent i = new Intent(ReminderManager.this, SelectApplication.class);
                        i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                        startActivityForResult(i, Constants.REQUEST_CODE_APPLICATION);
                    }
                });
            }
        }).start();
    }

    private void pickContacts(final ProgressDialog pd) {
        Permissions permissions = new Permissions(this);
        if (permissions.checkPermission(Permissions.READ_CONTACTS)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();

                    Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                    final ArrayList<String> contacts = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1"))
                            hasPhone = "true";
                        else
                            hasPhone = "false";
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
                    } catch (NullPointerException e) {
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
                            Intent i = new Intent(ReminderManager.this, ContactsList.class);
                            i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                            startActivityForResult(i, Constants.REQUEST_CODE_CONTACTS);
                        }
                    });
                }
            }).start();
        } else {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                // Handle or log or ignore
            }
            permissions.requestPermission(ReminderManager.this,
                    new String[]{Permissions.READ_CONTACTS}, 107);
        }
    }

    private void saveTask(){
        save();
    }

    private void save() {
        Reminder item = getData();
        if (item == null) return;
        if (id != 0){
            remControl.save(id, item);
        } else {
            remControl.save(item);
        }
        finish();
    }

    private boolean isLocationAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_LOCATION);
    }

    private boolean isLocationOutAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_LOCATION_OUT);
    }

    private boolean isDateReminderAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_REMINDER);
    }

    private boolean isTimeReminderAttached() {
        return remControl instanceof TimerType;
    }

    private boolean isCallAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_CALL);
    }

    private boolean isSkypeAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_SKYPE);
    }

    private boolean isApplicationAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_APPLICATION);
    }

    private boolean isWeekDayReminderAttached() {
        return remControl instanceof WeekdayType;
    }

    private boolean isMessageAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_MESSAGE);
    }

    private boolean isMonthDayAttached() {
        return remControl instanceof MonthdayType;
    }

    private String getType(){
        String type;
        if (remControl instanceof MonthdayType){
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
        } else if (remControl instanceof WeekdayType){
            if (attachAction.isChecked()){
                if (callCheck.isChecked()){
                    type = Constants.TYPE_WEEKDAY_CALL;
                } else type = Constants.TYPE_WEEKDAY_MESSAGE;
            } else {
                type = Constants.TYPE_WEEKDAY;
            }
        } else if (remControl instanceof LocationType){
            if (remControl.getType().startsWith(Constants.TYPE_LOCATION_OUT)){
                if (attachLocationOutAction.isChecked()){
                    if (callCheckLocationOut.isChecked()) type = Constants.TYPE_LOCATION_OUT_CALL;
                    else type = Constants.TYPE_LOCATION_OUT_MESSAGE;
                } else type = Constants.TYPE_LOCATION_OUT;
            } else {
                if (attachLocationAction.isChecked()){
                    if (callCheckLocation.isChecked()) type = Constants.TYPE_LOCATION_CALL;
                    else type = Constants.TYPE_LOCATION_MESSAGE;
                } else type = Constants.TYPE_LOCATION;
            }
        } else if (remControl instanceof TimerType) {
            type = remControl.getType();
        } else {
            if (isSkypeAttached()){
                if (skypeCall.isChecked()){
                    type = Constants.TYPE_SKYPE;
                } else if (skypeVideo.isChecked()){
                    type = Constants.TYPE_SKYPE_VIDEO;
                } else {
                    type = Constants.TYPE_SKYPE_CHAT;
                }
            } else if (isApplicationAttached()){
                if (application.isChecked()){
                    type = Constants.TYPE_APPLICATION;
                } else {
                    type = Constants.TYPE_APPLICATION_BROWSER;
                }
            } else type = remControl.getType();
        }
        return type;
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

    private String getAppTaskType(){
        String type;
        if (application.isChecked()){
            type = Constants.TYPE_APPLICATION;
        } else {
            type = Constants.TYPE_APPLICATION_BROWSER;
        }
        return type;
    }

    private Reminder getData() {
        String type = getType();
        Log.d(Constants.LOG_TAG, "Task type " + (type != null ? type : "no type"));
        String weekdays = null;
        if (isWeekDayReminderAttached()) {
            Interval interval = new Interval(ReminderManager.this);
            weekdays = interval.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(), wednesdayCheck.isChecked(),
                    thursdayCheck.isChecked(), fridayCheck.isChecked(), saturdayCheck.isChecked(), sundayCheck.isChecked());
            if (weekdays.matches(Constants.NOTHING_CHECKED)) {
                Toast.makeText(ReminderManager.this, getString(R.string.weekday_nothing_checked), Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        String task = taskField.getText().toString().trim();
        if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_WEEKDAY_CALL) ||
                type.matches(Constants.TYPE_MONTHDAY_CALL) || type.matches(Constants.TYPE_MONTHDAY_CALL_LAST) ||
                type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {

        } else {
            if (task.matches("")) {
                taskField.setError(getString(R.string.empty_field_error));
                return null;
            }
        }
        if (checkNumber()) return null;
        String number = getNumber();
        if (isApplicationAttached()){
            if (application.isChecked()){
                number = selectedPackage;
                if (number == null){
                    Toast.makeText(ReminderManager.this,
                            getString(R.string.dont_selected_application_message), Toast.LENGTH_SHORT).show();
                    return null;
                }
            } else if (browser.isChecked()){
                number = browseLink.getText().toString().trim();
                if (number == null || number.matches("") || number.matches(".*https?://")) return null;
                if (!number.startsWith("http://") && !number.startsWith("https://"))
                    number = "http://" + number;
            }
        }
        Log.d(Constants.LOG_TAG, "Task number " + (number != null ? number : "no number"));
        String uuId = SyncHelper.generateID();

        Double latitude = 0.0;
        Double longitude = 0.0;
        if (isLocationAttached() || isLocationOutAttached()){
            if (!LocationUtil.checkLocationEnable(this)){
                LocationUtil.showLocationAlert(this);
                return null;
            }
            LatLng dest = null;
            boolean isNull = true;
            if (curPlace != null) {
                dest = curPlace;
                isNull = false;
            }
            if (isNull) {
                Toast.makeText(ReminderManager.this, getString(R.string.point_warning),
                        Toast.LENGTH_SHORT).show();
                return null;
            }

            latitude = dest.latitude;
            longitude = dest.longitude;
        }
        Log.d(Constants.LOG_TAG, "Place coords " + latitude + "," + longitude);

        if (isTimeReminderAttached()){
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            myYear = c.get(Calendar.YEAR);
            myMonth = c.get(Calendar.MONTH);
            myDay = c.get(Calendar.DAY_OF_MONTH);
            myHour = c.get(Calendar.HOUR_OF_DAY);
            myMinute = c.get(Calendar.MINUTE);
            mySeconds = c.get(Calendar.SECOND);
        }

        int repeat = getRepeat();
        Log.d(Constants.LOG_TAG, "Task repeat " + repeat);

        int sync = getSyncCode();
        Log.d(Constants.LOG_TAG, "Task sync code " + sync);

        long repMinute = 0;
        if (isTimeReminderAttached()){
            repMinute = getAfterTime();
            if (repMinute == 0) return null;
        }
        Log.d(Constants.LOG_TAG, "Task after minute " + repMinute);

        int export = getExportCode();
        Log.d(Constants.LOG_TAG, "Task export code " + export);

        if (isMonthDayAttached()){
            if (type != null && type.endsWith("_last")) myDay = 0;
        }

        long due = getDue(weekdays, repMinute);
        Log.d(Constants.LOG_TAG, "Task due " + due);

        if (isLocationAttached() || isLocationOutAttached()){
            if (isLocationAttached() && !attackDelay.isChecked()) {
                myDay = 0;
                myMonth = 0;
                myYear = 0;
                myHour = 0;
                myMinute = 0;
            }
            if (isLocationOutAttached() && !attachDelayOut.isChecked()) {
                myDay = 0;
                myMonth = 0;
                myYear = 0;
                myHour = 0;
                myMinute = 0;
            }
        }

        return new Reminder(task, type, weekdays, melody, categoryId, uuId,
                new double[]{latitude, longitude}, number, myDay, myMonth, myYear, myHour, myMinute,
                mySeconds, repeat, export, radius, ledColor, sync, repMinute, due);
    }

    private int getExportCode() {
        if (isStock || isCalendar){
            if (isMonthDayAttached()) return monthDayExport.isChecked() ? 1 : 0;
            else if (isWeekDayReminderAttached()) return weekExport.isChecked() ? 1 : 0;
            else if (isTimeReminderAttached()) return timeExport.isChecked() ? 1 : 0;
            else if (isCallAttached()) return callExport.isChecked() ? 1 : 0;
            else if (isMessageAttached()) return messageExport.isChecked() ? 1 : 0;
            else if (isSkypeAttached()) return skypeExport.isChecked() ? 1 : 0;
            else if (isApplicationAttached()) return appExport.isChecked() ? 1 : 0;
            else if (isDateReminderAttached()) return dateExport.isChecked() ? 1 : 0;
            else return 0;
        } else return 0;
    }

    private long getDue(String weekdays, long time) {
        if (isWeekDayReminderAttached()){
            return ReminderUtils.getWeekTime(myHour, myMinute, weekdays);
        } else if (isMonthDayAttached()){
            return ReminderUtils.getMonthTime(myHour, myMinute, myDay);
        } else if (isTimeReminderAttached()){
            return ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, time);
        } else return ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
    }

    private int getSyncCode() {
        if (isWeekDayReminderAttached()){
            return ReminderUtils.getSyncCode(weekTaskExport);
        } else if (isSkypeAttached()){
            return ReminderUtils.getSyncCode(skypeTaskExport);
        } else if (isApplicationAttached()){
            return ReminderUtils.getSyncCode(appTaskExport);
        } else if (isMonthDayAttached()){
            return ReminderUtils.getSyncCode(monthDayTaskExport);
        } else if (isCallAttached()){
            return ReminderUtils.getSyncCode(callTaskExport);
        } else if (isMessageAttached()){
            return ReminderUtils.getSyncCode(messageTaskExport);
        } else if (isTimeReminderAttached()){
            return ReminderUtils.getSyncCode(timeTaskExport);
        } else if (isDateReminderAttached()){
            return ReminderUtils.getSyncCode(dateTaskExport);
        } else return 0;
    }

    private int getRepeat() {
        if (isSkypeAttached()){
            return Integer.parseInt(repeatDaysSkype.getText().toString().trim());
        } else if (isApplicationAttached()){
            return Integer.parseInt(repeatDaysApp.getText().toString().trim());
        } else if (isDateReminderAttached()){
            return Integer.parseInt(repeatDays.getText().toString().trim());
        } else if (isTimeReminderAttached()){
            try {
                return Integer.parseInt(repeatMinutes.getText().toString());
            } catch (NumberFormatException e){
                e.printStackTrace();
                return 0;
            }
        } else if (isCallAttached()){
            return Integer.parseInt(repeatDaysCall.getText().toString().trim());
        } else if (isMessageAttached()){
            return Integer.parseInt(repeatDaysMessage.getText().toString().trim());
        } else return 0;
    }

    private String getNumber() {
        if (isCallAttached()) {
            return phoneNumber.getText().toString().trim();
        } else if (isSkypeAttached()){
            return skypeUser.getText().toString().trim();
        } else if (isMessageAttached()){
            return messageNumber.getText().toString().trim();
        } else if (isLocationAttached() && attachLocationAction.isChecked()){
            return phoneNumberLocation.getText().toString().trim();
        } else if (isWeekDayReminderAttached() && attachAction.isChecked()) {
            return weekPhoneNumber.getText().toString().trim();
        } else if (isMonthDayAttached() && monthDayAttachAction.isChecked()) {
            return monthDayPhoneNumber.getText().toString().trim();
        } else if (isLocationOutAttached() && attachLocationOutAction.isChecked()) {
            return phoneNumberLocationOut.getText().toString().trim();
        } else return null;
    }

    private boolean checkApplication(){
        if (application.isChecked()) {
            return applicationName.getText().toString().trim().matches("");
        } else return browser.isChecked() && browseLink.getText().toString().trim().matches("");
    }

    private boolean checkNumber(){
        if (isCallAttached()) {
            boolean is = phoneNumber.getText().toString().trim().matches("");
            if (is) {
                phoneNumber.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else if (isSkypeAttached()){
            boolean is = skypeUser.getText().toString().trim().matches("");
            if (is) {
                skypeUser.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else if (isMessageAttached()){
            boolean is = messageNumber.getText().toString().trim().matches("");
            if (is) {
                messageNumber.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else if (isLocationAttached() && attachLocationAction.isChecked()){
            boolean is = phoneNumberLocation.getText().toString().trim().matches("");
            if (is) {
                phoneNumberLocation.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else if (isWeekDayReminderAttached() && attachAction.isChecked()) {
            boolean is = weekPhoneNumber.getText().toString().trim().matches("");
            if (is) {
                weekPhoneNumber.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else if (isMonthDayAttached() && monthDayAttachAction.isChecked()) {
            boolean is = monthDayPhoneNumber.getText().toString().trim().matches("");
            if (is) {
                monthDayPhoneNumber.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else if (isLocationOutAttached() && attachLocationOutAction.isChecked()) {
            boolean is = phoneNumberLocationOut.getText().toString().trim().matches("");
            if (is) {
                phoneNumberLocationOut.setError(getString(R.string.empty_field_error));
                return true;
            } else return false;
        } else return false;
    }

    private boolean checkMessage(){
        return taskField.getText().toString().trim().matches("");
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
        } else Toast.makeText(this, R.string.string_timer_warming, Toast.LENGTH_SHORT).show();
        return res;
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

    private void detachLayout(){
        detachCurrentView();
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

        if (isMonthDayAttached()){
            if (myDay < 29) monthDayField.setText(dayStr);
            else {
                myDay = 28;
                Toast.makeText(ReminderManager.this, getString(R.string.string_max_day_message),
                        Toast.LENGTH_SHORT).show();
            }
        }
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
            if (isSkypeAttached()){
                skypeTime.setText(formattedTime);
            }
            if (isApplicationAttached()){
                appTime.setText(formattedTime);
            }
            if (isDateReminderAttached()){
                timeField.setText(formattedTime);
            }
            if (isMessageAttached()){
                messageTime.setText(formattedTime);
            }
            if (isWeekDayReminderAttached()){
                weekTimeField.setText(formattedTime);
            }
            if (isLocationAttached()){
                if (attackDelay.isChecked()){
                    if (delayLayout.getVisibility() == View.VISIBLE)
                        locationTimeField.setText(formattedTime);
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
    public void onBackPressed() {
        if (map != null && !map.onBackPressed()) return;
        if (mapOut != null && !mapOut.onBackPressed()) return;

        if (mFab.getVisibility() == View.GONE){
            ViewUtils.show(ReminderManager.this, mFab, isAnimation);
            return;
        }

        restoreTask();
    }

    private void restoreTask(){
        if (id != 0) {
            DB.open();
            if (DB.getCount() == 0) {
                stopService(new Intent(ReminderManager.this, GeolocationService.class));
                stopService(new Intent(ReminderManager.this, CheckPosition.class));
            } else {
                Cursor c = DB.queryGroup();
                if (c != null && c.moveToFirst()) {
                    ArrayList<String> types = new ArrayList<>();
                    do {
                        String tp = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                        int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
                        if (isDone != 1) {
                            types.add(tp);
                        }
                    } while (c.moveToNext());
                    if (!types.contains(Constants.TYPE_LOCATION) ||
                            !types.contains(Constants.TYPE_LOCATION_CALL) ||
                            !types.contains(Constants.TYPE_LOCATION_MESSAGE) ||
                            !types.contains(Constants.TYPE_LOCATION_OUT) ||
                            !types.contains(Constants.TYPE_LOCATION_OUT_CALL) ||
                            !types.contains(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                        stopService(new Intent(ReminderManager.this, GeolocationService.class));
                        stopService(new Intent(ReminderManager.this, CheckPosition.class));
                    } else {
                        startService(new Intent(ReminderManager.this, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
                if (c != null) c.close();
            }
            Cursor c = DB.getReminder(id);
            if (c != null && c.moveToFirst()) {
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                int isArchive = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
                if (isDone != 1 && isArchive != 1) {
                    if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                        new WeekDayReceiver().setAlarm(ReminderManager.this, id);
                    } else if (type.matches(Constants.TYPE_REMINDER) ||
                            type.matches(Constants.TYPE_TIME) ||
                            type.matches(Constants.TYPE_CALL) ||
                            type.matches(Constants.TYPE_MESSAGE)) {
                        new AlarmReceiver().setAlarm(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                        new MonthDayReceiver().setAlarm(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_LOCATION) && isDelayed) {
                        new PositionDelayReceiver().setDelay(ReminderManager.this, id);
                    }
                }
            }
            if (c != null) c.close();
            new Notifier(ReminderManager.this).recreatePermanent();
            finish();
        } else {
            new Notifier(ReminderManager.this).recreatePermanent();
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (navContainer.getVisibility() == View.VISIBLE) {
            switchIt(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private static final int MIN_DISTANCE = 40;
    private float downX;
    private float downY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isToolbar = v.getId() != R.id.windowBackground;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;
                if(Math.abs(deltaX) > Math.abs(deltaY)) {
                    if(Math.abs(deltaX) > MIN_DISTANCE){
                        if(deltaX < 0) { this.onRightSwipe(); return true; }
                        if(deltaX > 0) { this.onLeftSwipe(); return true; }
                    } else {
                        return false; // We don't consume the event
                    }
                } else {
                    if(Math.abs(deltaY) > MIN_DISTANCE && isToolbar){
                        if(deltaY < 0) { this.onDownSwipe(); return true; }
                        if(deltaY > 0) { this.onUpSwipe(); return true; }
                    } else {
                        return false; // We don't consume the event
                    }
                }

                return true;
            }
        }
        return false;
    }

    private void onDownSwipe() {
        if (navContainer.getVisibility() == View.GONE) {
            if (isAnimation) {
                ViewUtils.expand(navContainer);
            } else navContainer.setVisibility(View.VISIBLE);
        }
    }

    private void onUpSwipe() {
        if (navContainer.getVisibility() == View.VISIBLE) {
            if (isAnimation) {
                ViewUtils.collapse(navContainer);
            } else navContainer.setVisibility(View.GONE);
        }
    }

    private void onLeftSwipe() {
        int current = spinner.getSelectedItemPosition();
        int maxInt = 9;
        if (current > 0){
            spinner.setSelection(current - 1);
            switchIt(current - 1);
        }
        if (0 == current){
            spinner.setSelection(maxInt);
            switchIt(maxInt);
        }
    }

    private void onRightSwipe() {
        int current = spinner.getSelectedItemPosition();
        int maxInt = 9;
        if (current < maxInt){
            spinner.setSelection(current + 1);
            switchIt(current + 1);
        }
        if (current == maxInt){
            spinner.setSelection(0);
            switchIt(0);
        }
    }

    private void switchIt(int position){
        radius = -1;
        selectedPackage = null;
        switch (position){
            case 0:
                detachLayout();
                attachDateReminder();
                break;
            case 2:
                detachLayout();
                attachWeekDayReminder();
                break;
            case 1:
                detachLayout();
                attachTimeReminder();
                break;
            case 3:
                detachLayout();
                if (new Permissions(ReminderManager.this).checkPermission(Permissions.CALL_PHONE)) {
                    attachCall();
                } else {
                    new Permissions(ReminderManager.this)
                            .requestPermission(ReminderManager.this,
                                    new String[]{Permissions.CALL_PHONE}, 109);
                }
                break;
            case 4:
                detachLayout();
                if (new Permissions(ReminderManager.this).checkPermission(Permissions.SEND_SMS)) {
                    attachMessage();
                } else {
                    new Permissions(ReminderManager.this)
                            .requestPermission(ReminderManager.this,
                                    new String[]{Permissions.SEND_SMS}, 108);
                }
                break;
            case 5:
                detachLayout();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (new Permissions(ReminderManager.this).checkPermission(Permissions.ACCESS_FINE_LOCATION)) {
                        attachLocation();
                    } else {
                        new Permissions(ReminderManager.this)
                                .requestPermission(ReminderManager.this,
                                        new String[]{Permissions.ACCESS_COURSE_LOCATION,
                                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                                Permissions.SEND_SMS}, 105);
                    }
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 6:
                detachLayout();
                if (ReminderUtils.isSkypeClientInstalled(ReminderManager.this)) {
                    attachSkype();
                } else {
                    spinner.setSelection(0);
                    goToMarket();
                }
                break;
            case 7:
                detachLayout();
                attachApplication();
                break;
            case 8:
                detachLayout();
                attachMonthDay();
                break;
            case 9:
                detachLayout();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (new Permissions(ReminderManager.this).checkPermission(Permissions.ACCESS_FINE_LOCATION)) {
                        attachLocationOut();
                    } else {
                        new Permissions(ReminderManager.this)
                                .requestPermission(ReminderManager.this,
                                        new String[]{Permissions.ACCESS_COURSE_LOCATION,
                                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                                Permissions.SEND_SMS}, 106);
                    }
                } else {
                    spinner.setSelection(0);
                }
                break;
        }
        sPrefs.saveInt(Prefs.LAST_USED_REMINDER, position);
        invalidateOptionsMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 105:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachLocation();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 106:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachLocationOut();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 107:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                    pickContacts(pd);
                } else {
                    new Permissions(ReminderManager.this)
                            .showInfo(ReminderManager.this, Permissions.READ_CONTACTS);
                }
                break;
            case 108:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachMessage();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 109:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachCall();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 110:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    monthDayAttachAction.setChecked(true);
                } else {
                    new Permissions(ReminderManager.this)
                            .showInfo(ReminderManager.this, Permissions.CALL_PHONE);
                    monthDayAttachAction.setChecked(false);
                }
            case 111:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachAction.setChecked(true);
                } else {
                    new Permissions(ReminderManager.this)
                            .showInfo(ReminderManager.this, Permissions.CALL_PHONE);
                    attachAction.setChecked(false);
                }
                break;
        }
    }

    public void goToMarket() {
        Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
        Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }

    private ArrayList<String> names, foldersFile;
    private ArrayList<File> fileList;
    private void pickSounds(final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                fileList = new ArrayList<>();
                fileList.clear();
                File dir;
                if (SyncHelper.isSdPresent()) {
                    dir = new File(Environment.getExternalStorageDirectory()
                            .toString());
                    listf(dir.toString(), fileList);
                } else {
                    dir = new File(Environment.getDataDirectory().toString());
                    listf(dir.toString(), fileList);
                }
                Collections.sort(fileList);
                names = new ArrayList<>();
                foldersFile = new ArrayList<>();
                names.clear();
                foldersFile.clear();
                for (File aFile : fileList) {
                    names.add(aFile.getName());
                    String folder = aFile.toString();
                    foldersFile.add(folder);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ((pd != null) && pd.isShowing()) {
                                pd.dismiss();
                            }
                        } catch (final Exception ignored) {
                        }
                        if (fileList != null){
                            Intent i = new Intent(ReminderManager.this, SelectMelody.class);
                            i.putStringArrayListExtra("names", names);
                            i.putStringArrayListExtra("folders", foldersFile);
                            i.putExtra(Constants.BIRTHDAY_INTENT_ID, 1);
                            startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_MELODY);
                        } else {
                            Toast.makeText(ReminderManager.this,
                                    getString(R.string.no_music),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.canRead()) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".mp3") || file.getName().endsWith(".ogg")) {
                            files.add(file);
                        }
                    } else if (file.isDirectory()) {
                        listf(file.toString(), files);
                    }
                } else {
                    Log.d(Constants.LOG_TAG, "secure file");
                }
            }
        } else Log.i(Constants.LOG_TAG, "No files");
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
        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY) {
            if (resultCode == RESULT_OK){
                melody = data.getStringExtra(Constants.SELECTED_MELODY);
                if (melody != null) {
                    File musicFile = new File(melody);
                    Toast.makeText(ReminderManager.this,
                            getString(R.string.selected_melody_string) + musicFile.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == RESULT_OK){
                radius = data.getIntExtra(Constants.SELECTED_RADIUS, -1);
                if (radius != -1) {
                    Toast.makeText(ReminderManager.this,
                            getString(R.string.selected_radius_string) + radius + " " + getString(R.string.meter),
                            Toast.LENGTH_SHORT).show();
                    if (isLocationAttached()) map.recreateMarker(radius);
                    if (isLocationOutAttached()) mapOut.recreateMarker(radius);
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_COLOR) {
            if (resultCode == RESULT_OK){
                int position = data.getIntExtra(Constants.SELECTED_LED_COLOR, -1);
                String selColor = null;
                if (position == 0) {
                    ledColor = LED.WHITE;
                    selColor = getString(R.string.led_color_white);
                } else if (position == 1) {
                    ledColor = LED.RED;
                    selColor = getString(R.string.led_color_red);
                } else if (position == 2) {
                    ledColor = LED.GREEN;
                    selColor = getString(R.string.led_color_green);
                } else if (position == 3) {
                    ledColor = LED.BLUE;
                    selColor = getString(R.string.led_color_blue);
                } else if (position == 4) {
                    ledColor = LED.ORANGE;
                    selColor = getString(R.string.led_color_orange);
                } else if (position == 5) {
                    ledColor = LED.YELLOW;
                    selColor = getString(R.string.led_color_yellow);
                } else if (position == 6) {
                    ledColor = LED.PINK;
                    selColor = getString(R.string.led_color_pink);
                } else if (position == 7) {
                    ledColor = LED.GREEN_LIGHT;
                    selColor = getString(R.string.led_color_green_light);
                } else if (position == 8) {
                    ledColor = LED.BLUE_LIGHT;
                    selColor = getString(R.string.led_color_blue_light);
                }

                Toast.makeText(ReminderManager.this, getString(R.string.string_selected_led_color) + " " + selColor,
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == Constants.REQUEST_CODE_APPLICATION) {
            if (resultCode == RESULT_OK){
                selectedPackage = data.getStringExtra(Constants.SELECTED_APPLICATION);
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(selectedPackage, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String title = (String)((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                applicationName.setText(title);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_menu, menu);
        if (isLocationAttached()){
            menu.getItem(1).setVisible(true);
        }
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (Module.isPro() && sPrefs.loadBoolean(Prefs.LED_STATUS)){
            menu.getItem(2).setVisible(true);
        }
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_menu_option));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isLocationAttached()){
            menu.getItem(1).setVisible(true);
        }
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (Module.isPro() && sPrefs.loadBoolean(Prefs.LED_STATUS)){
            menu.getItem(2).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isListFirstTime()){
            startActivity(new Intent(ReminderManager.this, HelpOverflow.class)
                    .putExtra(Constants.ITEM_ID_INTENT, 2));
        }
    }

    private boolean isListFirstTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("JustReminderBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("JustReminderBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }

    @Override
    protected void onDestroy() {
        if (mLocList != null) mLocationManager.removeUpdates(mLocList);
        super.onDestroy();
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
            if (text.matches("")) text = _Location;
            if (isLocationOutAttached()) {
                currentLocation.setText(_Location);
                if (mapOut != null) {
                    mapOut.addMarker(new LatLng(currentLat, currentLong), text, true, true, radius);
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