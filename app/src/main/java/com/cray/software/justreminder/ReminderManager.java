package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
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
import android.graphics.Typeface;
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
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
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
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.Item;
import com.cray.software.justreminder.dialogs.utils.ContactsList;
import com.cray.software.justreminder.dialogs.utils.LedColor;
import com.cray.software.justreminder.dialogs.utils.SelectApplication;
import com.cray.software.justreminder.dialogs.utils.SelectMelody;
import com.cray.software.justreminder.dialogs.utils.TargetRadius;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.spinnerMenu.SpinnerItem;
import com.cray.software.justreminder.spinnerMenu.TitleNavigationAdapter;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.Utils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ReminderManager extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener, View.OnTouchListener,
        DatePickerDialog.OnDateSetListener, CompoundButton.OnCheckedChangeListener {

    LinearLayout call_layout, by_date_layout, after_time_layout, geolocationlayout, message_layout,
            weekday_layout, action_layout, skype_layout, application_layout, monthDayLayout;
    LinearLayout callDateRing, dateRing, messageDateRing;
    FloatingEditText phoneNumber, messageNumber, weekPhoneNumber;
    TextView callDate, callTime, dateField, timeField, callYearDate, dateYearField,
            messageDate, messageYearDate, messageTime, weekTimeField;
    ImageButton addNumberButton, addMessageNumberButton, weekAddNumberButton;
    SeekBar repeatCallInt, repeatDateInt, repeatMessageInt;
    ImageButton insertVoice, pickApplication;

    Spinner placesList;
    ArrayList<String> spinnerArray = null;
    LinearLayout layoutContainer, delayLayout, navContainer;
    CheckBox attackDelay;
    CheckBox timeExport, messageExport, dateExport, callExport, weekExport;

    int myHour = 0;
    int myMinute = 0;
    int mySeconds = 0;
    int myYear = 0;
    int myMonth = 0;
    int myDay = 1;

    ProgressDialog pd;
    DataBase DB = new DataBase(ReminderManager.this);
    SyncHelper sHelp;
    UpdatesHelper updatesHelper;

    AlarmReceiver alarm = new AlarmReceiver();
    boolean isGPSEnabled = false, isNetworkEnabled = false, isDelayed = false;
    protected LocationManager locationManager;

    ColorSetter cSetter = new ColorSetter(ReminderManager.this);
    SharedPrefs sPrefs = new SharedPrefs(ReminderManager.this);
    Interval interval = new Interval(ReminderManager.this);
    GTasksHelper gtx = new GTasksHelper(ReminderManager.this);

    private GoogleMap googleMap;
    private Marker destination;

    long id;
    String categoryId;
    String type, melody = null, selectedPackage = null;
    int radius = -1, ledColor = 0;
    Toolbar toolbar;
    Spinner spinner;
    FloatingEditText taskField;
    TextView category;
    FloatingActionButton mFab;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    public static final int MENU_ITEM_DELETE = 12;

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
        insertVoice = (ImageButton) findViewById(R.id.insertVoice);
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
        findViewById(R.id.windowBackground).setOnTouchListener(this);

        mFab = new FloatingActionButton(ReminderManager.this);
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorChooser());
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                hide(mFab);
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
            alarm.cancelAlarm(ReminderManager.this, id);
            new WeekDayReceiver().cancelAlarm(ReminderManager.this, id);
            new MonthDayReceiver().cancelAlarm(ReminderManager.this, id);
            new DelayReceiver().cancelAlarm(ReminderManager.this, id);
            new PositionDelayReceiver().cancelDelay(ReminderManager.this, id);
            new DisableAsync(ReminderManager.this).execute();
        }

        spinner.setSelection(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LAST_USED_REMINDER));

        if (id != 0){
            DB.open();
            Cursor c = DB.getTask(id);
            if (c != null && c.moveToNext()) {
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
                ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
                melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
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
            if (c != null) c.close();
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

        loadPlaces();
        clearForm();
    }

    private void changeCategory() {
        DB = new DataBase(ReminderManager.this);
        DB.open();
        final ArrayList<Item> items = new ArrayList<>();
        Cursor c = DB.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                items.add(new Item(title, uuId));
            } while (c.moveToNext());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.string_select_category));
        builder.setAdapter(new SimpleAdapter(ReminderManager.this,
                DB.queryCategories()), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                category.setText(items.get(which).getTitle());
                categoryId = items.get(which).getUuID();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpNavigation() {
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        boolean isDark = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.by_date_title), R.drawable.ic_event_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.by_date_title), R.drawable.ic_event_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.after_time_title), R.drawable.ic_access_time_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.after_time_title), R.drawable.ic_access_time_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.by_weekdays_title), R.drawable.ic_alarm_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.by_weekdays_title), R.drawable.ic_alarm_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.make_call_title), R.drawable.ic_call_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.make_call_title), R.drawable.ic_call_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.send_message_title), R.drawable.ic_message_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.send_message_title), R.drawable.ic_message_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.by_location_title), R.drawable.ic_place_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.by_location_title), R.drawable.ic_place_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.skype_reminder_type), R.drawable.skype_icon_white));
        else navSpinner.add(new SpinnerItem(getString(R.string.skype_reminder_type), R.drawable.skype_icon));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.launch_application_reminder_type), R.drawable.ic_launch_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.launch_application_reminder_type), R.drawable.ic_launch_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.string_by_day_of_month), R.drawable.ic_event_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.string_by_day_of_month), R.drawable.ic_event_grey600_24dp));

        if (isDark) navSpinner.add(new SpinnerItem(getString(R.string.string_place_out), R.drawable.ic_beenhere_white_24dp));
        else navSpinner.add(new SpinnerItem(getString(R.string.string_place_out), R.drawable.ic_beenhere_grey600_24dp));

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void deleteReminder() {
        DB.open();
        Cursor c = DB.getTask(id);
        if (c != null && c.moveToFirst()) {
            int isArchived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
            WeekDayReceiver week = new WeekDayReceiver();
            Integer i = (int) (long) id;
            alarm.cancelAlarm(ReminderManager.this, i);
            week.cancelAlarm(ReminderManager.this, i);
            new PositionDelayReceiver().cancelDelay(ReminderManager.this, id);
            new MonthDayReceiver().cancelAlarm(ReminderManager.this, i);
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(i);
            if (isArchived == 1) DB.deleteTask(id);
            else DB.toArchive(id);
            new DisableAsync(ReminderManager.this).execute();
            updatesHelper = new UpdatesHelper(ReminderManager.this);
            updatesHelper.updateWidget();
            new Notifier(ReminderManager.this).recreatePermanent();
            if (isArchived == 1) Toast.makeText(ReminderManager.this, getString(R.string.string_deleted), Toast.LENGTH_SHORT).show();
            else Toast.makeText(ReminderManager.this, getString(R.string.archived_result_message), Toast.LENGTH_SHORT).show();

            finish();
        }
        if (c != null) c.close();
    }

    public void show(final View v) {
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_zoom);
            v.startAnimation(slide);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void hide(final View v) {
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_zoom_out);
            v.startAnimation(slide);
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.GONE);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mFab.getVisibility() == View.GONE){
                    show(mFab);
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
        return is;
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
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setStartOffset(400);
            fadeIn.setDuration(400);
            view.setAnimation(fadeIn);
            view.setVisibility(View.VISIBLE);
        } else view.setVisibility(View.VISIBLE);
    }

    private void showOver(View view){
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new OvershootInterpolator());
            fadeIn.setDuration(300);
            view.setAnimation(fadeIn);
            view.setVisibility(View.VISIBLE);
        } else view.setVisibility(View.VISIBLE);
    }

    private void hideOver(View view){
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            Animation fadeIn = new AlphaAnimation(1, 0);
            fadeIn.setInterpolator(new OvershootInterpolator());
            fadeIn.setDuration(300);
            view.setAnimation(fadeIn);
            view.setVisibility(View.GONE);
        } else view.setVisibility(View.GONE);
    }

    private void setImage(ImageButton ib){
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            ib.setImageResource(R.drawable.ic_person_add_white_24dp);
        } else ib.setImageResource(R.drawable.ic_person_add_grey600_24dp);
    }

    CheckBox dateTaskExport;
    EditText repeatDays;

    private void attachDateReminder(){
        taskField.setHint(getString(R.string.tast_hint));

        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        fadeInAnimation(by_date_layout);

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

        dateRing = (LinearLayout) findViewById(R.id.dateRing);
        dateRing.setOnClickListener(this);

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        dateExport = (CheckBox) findViewById(R.id.dateExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        dateField.setTypeface(Utils.getMediumTypeface(this));

        dateYearField = (TextView) findViewById(R.id.dateYearField);
        dateYearField.setText(String.valueOf(myYear));
        dateYearField.setTypeface(Utils.getThinTypeface(this));

        timeField = (TextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        timeField.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        timeField.setTypeface(Utils.getMediumTypeface(this));

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        repeatDays.setTypeface(Utils.getLightTypeface(this));

        repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setOnSeekBarChangeListener(this);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDays.setText(String.valueOf(getRepeat(repeatDateInt.getProgress())));

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text = "";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

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

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, myHour);
            calendar.set(Calendar.MINUTE, myMinute);

            taskField.setText(text);
            timeField.setText(Utils.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
            dateField.setText(dayStr + "/" + monthStr);
            dateYearField.setText(String.valueOf(myYear));
            repeatDateInt.setProgress(interval.getProgressFromCode(repCode));
            repeatDays.setText(String.valueOf(repCode));
        }
    }

    CheckBox monthDayExport, monthDayTaskExport, monthDayAttachAction;
    LinearLayout monthDayActionLayout;
    TextView monthDayField, monthDayTimeField;
    RadioButton monthDayCallCheck, monthDayMessageCheck, dayCheck, lastCheck;
    ImageButton monthDayAddNumberButton;
    FloatingEditText monthDayPhoneNumber;

    private void attachMonthDay(){
        taskField.setHint(getString(R.string.tast_hint));

        monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        fadeInAnimation(monthDayLayout);

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
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        monthDayField.setTypeface(Utils.getMediumTypeface(this));

        monthDayTimeField = (TextView) findViewById(R.id.monthDayTimeField);
        monthDayTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        monthDayTimeField.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        monthDayTimeField.setTypeface(Utils.getMediumTypeface(this));

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
                    showOver(monthDayActionLayout);
                    monthDayAddNumberButton = (ImageButton) findViewById(R.id.monthDayAddNumberButton);
                    monthDayAddNumberButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                            pickContacts(pd);
                        }
                    });
                    setImage(monthDayAddNumberButton);

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
                    hideOver(monthDayActionLayout);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (monthDayAttachAction.isChecked()) showOver(monthDayActionLayout);

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text = "";
            String number = "";
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
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
            monthDayTimeField.setText(Utils.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
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
                    expand(monthDayField);
                    myDay = 1;
                }
                break;
            case R.id.lastCheck:
                if (lastCheck.isChecked()) {
                    dayCheck.setChecked(false);
                    collapse(monthDayField);
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
                    time = (prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_TIME) * 1000);
                    int distance;
                    distance = prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
                }
                break;
            case R.id.mapCheck:
                if (mapCheck.isChecked()) {
                    currentCheck.setChecked(false);
                    fadeOutAnimation(specsContainerOut);
                    fadeInAnimation(mapContainerOut);
                    if (mLocList != null) mLocationManager.removeUpdates(mLocList);
                }
                break;
        }
    }

    ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck, fridayCheck, saturdayCheck, sundayCheck;
    RadioButton callCheck, messageCheck;
    CheckBox attachAction, weekTaskExport;

    private void attachWeekDayReminder(){
        taskField.setHint(getString(R.string.tast_hint));

        cSetter = new ColorSetter(ReminderManager.this);
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        fadeInAnimation(weekday_layout);

        weekExport = (CheckBox) findViewById(R.id.weekExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        weekTimeField.setText(Utils.getTime(c.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        weekTimeField.setTypeface(Utils.getMediumTypeface(this));

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
                    showOver(action_layout);
                    weekAddNumberButton = (ImageButton) findViewById(R.id.weekAddNumberButton);
                    weekAddNumberButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
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
                    hideOver(action_layout);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (attachAction.isChecked()) showOver(action_layout);

        if (id != 0 && isSame()) {
            DB.open();
            Cursor x = DB.getTask(id);
            String text = "";
            String type = "";
            String weekdays = "";
            String number = "";
            int exp = 0;
            int expTasks = 0;
            if (x != null && x.moveToFirst()) {
                myHour = x.getInt(x.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = x.getInt(x.getColumnIndex(Constants.COLUMN_MINUTE));
                exp = x.getInt(x.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = x.getInt(x.getColumnIndex(Constants.COLUMN_SYNC_CODE));
                text = x.getString(x.getColumnIndex(Constants.COLUMN_TEXT));
                type = x.getString(x.getColumnIndex(Constants.COLUMN_TYPE));
                weekdays = x.getString(x.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                number = x.getString(x.getColumnIndex(Constants.COLUMN_NUMBER));
            }
            if (x != null) x.close();

            if (exp == 1){
                weekExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                weekTaskExport.setChecked(true);
            }

            c.set(Calendar.HOUR_OF_DAY, myHour);
            c.set(Calendar.MINUTE, myMinute);

            weekTimeField.setText(Utils.getTime(c.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
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
    TextView hoursView, minutesView, secondsView;
    ImageButton deleteButton;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0;
    String timeString = "000000";

    private void attachTimeReminder(){
        taskField.setHint(getString(R.string.tast_hint));

        cSetter = new ColorSetter(ReminderManager.this);
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        fadeInAnimation(after_time_layout);

        timeExport = (CheckBox) findViewById(R.id.timeExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
            timeExport.setVisibility(View.VISIBLE);
        }

        timeTaskExport = (CheckBox) findViewById(R.id.timeTaskExport);
        if (gtx.isLinked()){
            timeTaskExport.setVisibility(View.VISIBLE);
        }

        hoursView = (TextView) findViewById(R.id.hoursView);
        minutesView = (TextView) findViewById(R.id.minutesView);
        secondsView = (TextView) findViewById(R.id.secondsView);
        setTypeFont(hoursView, minutesView, secondsView);

        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        sPrefs = new SharedPrefs(this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME))
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

        b1 = (Button) findViewById(R.id.b1);
        b2 = (Button) findViewById(R.id.b2);
        b3 = (Button) findViewById(R.id.b3);
        b4 = (Button) findViewById(R.id.b4);
        b5 = (Button) findViewById(R.id.b5);
        b6 = (Button) findViewById(R.id.b6);
        b7 = (Button) findViewById(R.id.b7);
        b8 = (Button) findViewById(R.id.b8);
        b9 = (Button) findViewById(R.id.b9);
        b0 = (Button) findViewById(R.id.b0);
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

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text = "";
            int  exp = 0, expTasks = 0;
            long afterTime = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                afterTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

            generateString(afterTime);

            if (exp == 1){
                timeExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL){
                timeTaskExport.setChecked(true);
            }

            taskField.setText(text);
        }
    }

    private void setTypeFont(TextView... views){
        Typeface typeface = Utils.getLightTypeface(this);
        for (TextView v : views){
            v.setTypeface(typeface);
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

    CheckBox skypeExport, skypeTaskExport;
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

        skypeDateRing = (LinearLayout) findViewById(R.id.skypeDateRing);
        skypeDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        skypeExport = (CheckBox) findViewById(R.id.skypeExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        skypeDate.setTypeface(Utils.getMediumTypeface(this));

        skypeYearDate = (TextView) findViewById(R.id.skypeYearDate);
        skypeYearDate.setText(String.valueOf(myYear));
        skypeYearDate.setTypeface(Utils.getThinTypeface(this));

        skypeTime = (TextView) findViewById(R.id.skypeTime);
        skypeTime.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        skypeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        skypeTime.setTypeface(Utils.getMediumTypeface(this));

        repeatDaysSkype = (EditText) findViewById(R.id.repeatDaysSkype);
        repeatDaysSkype.setTypeface(Utils.getLightTypeface(this));

        repeatSkype = (SeekBar) findViewById(R.id.repeatSkype);
        repeatSkype.setOnSeekBarChangeListener(this);
        repeatSkype.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysSkype.setText(String.valueOf(getRepeat(repeatSkype.getProgress())));

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text="", number="", type="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

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
            skypeTime.setText(Utils.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
            repeatSkype.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysSkype.setText(String.valueOf(repCode));
        }
    }

    CheckBox appExport, appTaskExport;
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
        pickApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.loading_applications_message), true);
                pickApplications(pd);
            }
        });
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        } else pickApplication.setImageResource(R.drawable.ic_launch_grey600_24dp);

        application = (RadioButton) findViewById(R.id.application);
        application.setChecked(true);
        browser = (RadioButton) findViewById(R.id.browser);
        application.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        collapse(applicationLayout);
                        expand(browseLink);
                    } else {
                        applicationLayout.setVisibility(View.GONE);
                        browseLink.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        collapse(browseLink);
                        expand(applicationLayout);
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

        appDateRing = (LinearLayout) findViewById(R.id.appDateRing);
        appDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        appExport = (CheckBox) findViewById(R.id.appExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        appDate.setTypeface(Utils.getMediumTypeface(this));

        appYearDate = (TextView) findViewById(R.id.appYearDate);
        appYearDate.setText(String.valueOf(myYear));
        appYearDate.setTypeface(Utils.getThinTypeface(this));

        appTime = (TextView) findViewById(R.id.appTime);
        appTime.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        appTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        appTime.setTypeface(Utils.getMediumTypeface(this));

        repeatDaysApp = (EditText) findViewById(R.id.repeatDaysApp);
        repeatDaysApp.setTypeface(Utils.getLightTypeface(this));

        repeatApp = (SeekBar) findViewById(R.id.repeatApp);
        repeatApp.setOnSeekBarChangeListener(this);
        repeatApp.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysApp.setText(String.valueOf(getRepeat(repeatApp.getProgress())));

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text="", number="", type="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

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
                final String name = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
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
            appTime.setText(Utils.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
            repeatApp.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysApp.setText(String.valueOf(repCode));
        }
    }

    CheckBox callTaskExport;
    EditText repeatDaysCall;

    private void attachCall(){
        taskField.setHint(getString(R.string.tast_hint));

        call_layout = (LinearLayout) findViewById(R.id.call_layout);
        fadeInAnimation(call_layout);

        addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                pickContacts(pd);
            }
        });
        setImage(addNumberButton);

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

        callDateRing = (LinearLayout) findViewById(R.id.callDateRing);
        callDateRing.setOnClickListener(this);

        callExport = (CheckBox) findViewById(R.id.callExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        callDate.setTypeface(Utils.getMediumTypeface(this));

        callYearDate = (TextView) findViewById(R.id.callYearDate);
        callYearDate.setText(String.valueOf(myYear));
        callYearDate.setTypeface(Utils.getThinTypeface(this));

        callTime = (TextView) findViewById(R.id.callTime);
        callTime.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        callTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        callTime.setTypeface(Utils.getMediumTypeface(this));

        repeatDaysCall = (EditText) findViewById(R.id.repeatDaysCall);
        repeatDaysCall.setTypeface(Utils.getLightTypeface(this));

        repeatCallInt = (SeekBar) findViewById(R.id.repeatCallInt);
        repeatCallInt.setOnSeekBarChangeListener(this);
        repeatCallInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysCall.setText(String.valueOf(getRepeat(repeatCallInt.getProgress())));

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text="", number="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

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
            callTime.setText(Utils.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
            repeatCallInt.setProgress(interval.getProgressFromCode(repCode));
            repeatDaysCall.setText(String.valueOf(repCode));
        }
    }

    CheckBox messageTaskExport;
    EditText repeatDaysMessage;

    private void attachMessage(){
        taskField.setHint(getString(R.string.message_field_hint));

        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        fadeInAnimation(message_layout);

        addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                pickContacts(pd);
            }
        });
        setImage(addMessageNumberButton);

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

        messageDateRing = (LinearLayout) findViewById(R.id.messageDateRing);
        messageDateRing.setOnClickListener(this);

        messageExport = (CheckBox) findViewById(R.id.messageExport);
        if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))){
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
        messageDate.setTypeface(Utils.getMediumTypeface(this));

        messageYearDate = (TextView) findViewById(R.id.messageYearDate);
        messageYearDate.setText(String.valueOf(myYear));
        messageYearDate.setTypeface(Utils.getThinTypeface(this));

        messageTime = (TextView) findViewById(R.id.messageTime);
        messageTime.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        messageTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        messageTime.setTypeface(Utils.getMediumTypeface(this));

        repeatDaysMessage = (EditText) findViewById(R.id.repeatDaysMessage);
        repeatDaysMessage.setTypeface(Utils.getLightTypeface(this));

        repeatMessageInt = (SeekBar) findViewById(R.id.repeatMessageInt);
        repeatMessageInt.setOnSeekBarChangeListener(this);
        repeatMessageInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysMessage.setText(String.valueOf(getRepeat(repeatMessageInt.getProgress())));

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text="", number="";
            int repCode = 0;
            int exp = 0;
            int expTasks = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
                expTasks = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            }
            if (c != null) c.close();

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
            messageTime.setText(Utils.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
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
                    if (isMapVisible()) cardSearch.setAdapter(adapter);
                    else searchField.setAdapter(adapter);
                }
                if (isLocationOutAttached()){
                    if (isMapOutVisible()) cardSearchOut.setAdapter(adapter);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    ImageButton mapButton, cardClear, zoomOut, layers, addNumberButtonLocation, myLocation;
    LinearLayout layersContainer, actionLocation;
    RelativeLayout mapContainer;
    ScrollView specsContainer;
    AutoCompleteTextView cardSearch;
    TextView typeNormal, typeSatellite, typeHybrid, typeTerrain;
    CheckBox attachLocationAction;
    RadioButton callCheckLocation, messageCheckLocation;
    FloatingEditText phoneNumberLocation;
    CardView card;

    private boolean isMapVisible(){
        return mapContainer != null && mapContainer.getVisibility() == View.VISIBLE;
    }

    private boolean isLayersVisible(){
        return layersContainer != null && layersContainer.getVisibility() == View.VISIBLE;
    }

    private void attachLocation() {
        taskField.setHint(getString(R.string.tast_hint));

        geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        fadeInAnimation(geolocationlayout);

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        specsContainer = (ScrollView) findViewById(R.id.specsContainer);
        layersContainer = (LinearLayout) findViewById(R.id.layersContainer);
        layersContainer.setVisibility(View.GONE);
        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);

        attackDelay = (CheckBox) findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        expand(delayLayout);
                    } else delayLayout.setVisibility(View.VISIBLE);
                } else {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        collapse(delayLayout);
                    } else delayLayout.setVisibility(View.GONE);
                }
            }
        });

        card = (CardView) findViewById(R.id.card);
        card.setCardBackgroundColor(cSetter.getCardStyle());

        clearField = (ImageButton) findViewById(R.id.clearButton);
        cardClear = (ImageButton) findViewById(R.id.cardClear);
        zoomOut = (ImageButton) findViewById(R.id.zoomOut);
        layers = (ImageButton) findViewById(R.id.layers);
        mapButton = (ImageButton) findViewById(R.id.mapButton);
        myLocation = (ImageButton) findViewById(R.id.myLocation);

        zoomOut.setBackgroundColor(cSetter.getBackgroundStyle());
        layers.setBackgroundColor(cSetter.getBackgroundStyle());
        myLocation.setBackgroundColor(cSetter.getBackgroundStyle());

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            clearField.setImageResource(R.drawable.ic_backspace_white_24dp);
            cardClear.setImageResource(R.drawable.ic_backspace_white_24dp);
            mapButton.setImageResource(R.drawable.ic_map_white_24dp);
            zoomOut.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
            layers.setImageResource(R.drawable.ic_layers_white_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_white_24dp);
            layersContainer.setBackgroundResource(R.drawable.popup_dark);
        } else {
            clearField.setImageResource(R.drawable.ic_backspace_grey600_24dp);
            cardClear.setImageResource(R.drawable.ic_backspace_grey600_24dp);
            mapButton.setImageResource(R.drawable.ic_map_grey600_24dp);
            zoomOut.setImageResource(R.drawable.ic_fullscreen_exit_grey600_24dp);
            layers.setImageResource(R.drawable.ic_layers_grey600_24dp);
            myLocation.setImageResource(R.drawable.ic_my_location_grey600_24dp);
            layersContainer.setBackgroundResource(R.drawable.popup);
        }

        clearField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });
        cardClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardSearch.setText("");
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOutAnimation(specsContainer);
                fadeInAnimation(mapContainer);
            }
        });
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersVisible()) hideOver(layersContainer);
                fadeOutAnimation(mapContainer);
                fadeInAnimation(specsContainer);
            }
        });
        layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersVisible()) hideOver(layersContainer);
                else showOver(layersContainer);
            }
        });
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersVisible()) hideOver(layersContainer);
                Location location = googleMap.getMyLocation();
                if (location != null){
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    LatLng pos = new LatLng(lat, lon);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                }
            }
        });

        typeNormal = (TextView) findViewById(R.id.typeNormal);
        typeSatellite = (TextView) findViewById(R.id.typeSatellite);
        typeHybrid = (TextView) findViewById(R.id.typeHybrid);
        typeTerrain = (TextView) findViewById(R.id.typeTerrain);
        typeNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                layersContainer.setVisibility(View.GONE);
            }
        });
        typeSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                layersContainer.setVisibility(View.GONE);
            }
        });
        typeHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                layersContainer.setVisibility(View.GONE);
            }
        });
        typeTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                layersContainer.setVisibility(View.GONE);
            }
        });

        searchField = (AutoCompleteTextView) findViewById(R.id.searchField);
        cardSearch = (AutoCompleteTextView) findViewById(R.id.cardSearch);
        searchField.setThreshold(3);
        cardSearch.setThreshold(3);
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
                googleMap.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                destination = googleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        });

        cardSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isLayersVisible()) hideOver(layersContainer);
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
        cardSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                googleMap.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                destination = googleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        });

        actionLocation = (LinearLayout) findViewById(R.id.actionLocation);
        actionLocation.setVisibility(View.GONE);

        attachLocationAction = (CheckBox) findViewById(R.id.attachLocationAction);
        attachLocationAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showOver(actionLocation);
                    addNumberButtonLocation = (ImageButton) findViewById(R.id.addNumberButtonLocation);
                    addNumberButtonLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                            pickContacts(pd);
                        }
                    });
                    setImage(addNumberButtonLocation);

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
                    hideOver(actionLocation);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (attachLocationAction.isChecked()) showOver(actionLocation);

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

        locationDateRing = (LinearLayout) findViewById(R.id.locationDateRing);
        locationDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationDateField = (TextView) findViewById(R.id.locationDateField);
        locationDateField.setTypeface(Utils.getMediumTypeface(this));
        locationDateField.setText(dayStr + "/" + monthStr);
        locationDateYearField = (TextView) findViewById(R.id.locationDateYearField);
        locationTimeField = (TextView) findViewById(R.id.locationTimeField);
        locationTimeField.setTypeface(Utils.getMediumTypeface(this));
        locationTimeField.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        locationTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        locationDateYearField.setTypeface(Utils.getThinTypeface(this));
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
                    if (isLayersVisible()) hideOver(layersContainer);
                    if (position > 0){
                        String placeName = spinnerArray.get(position);
                        DB.open();
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));

                            LatLng latLng = new LatLng(latitude, longitude);
                            googleMap.clear();
                            String title = taskField.getText().toString().trim();
                            if (title.matches("")) {
                                title = latLng.toString();
                            }
                            destination = googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                                    .draggable(true));

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isLayersVisible()) hideOver(layersContainer);
                }
            });
        }

        MapFragment fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        googleMap = fragment.getMap();
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        sPrefs = new SharedPrefs(ReminderManager.this);
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        googleMap.setMyLocationEnabled(true);
        if (googleMap.getMyLocation() != null) {
            double lat = googleMap.getMyLocation().getLatitude();
            double lon = googleMap.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!spinnerArray.isEmpty()){
                    placesList.setSelection(0);
                }
                if (isLayersVisible()) hideOver(layersContainer);
                googleMap.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = latLng.toString();
                }
                destination = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
            }
        });

        if (destination != null) {
            destination = googleMap.addMarker(new MarkerOptions()
                    .position(destination.getPosition())
                    .title(destination.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                    .draggable(true));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination.getPosition(), 13));
        }

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text = "", number = null, remType = "";
            double latitude = 0, longitude = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                remType = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
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

                locationTimeField.setText(Utils.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
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

            taskField.setText(text);
            if (longitude != 0 && latitude != 0) {
                destination = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(text)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
            }
        }
    }

    ImageButton mapButtonOut, cardClearOut, zoomOutOut, layersOut, addNumberButtonLocationOut,
            myLocationOut;
    LinearLayout layersContainerOut, actionLocationOut, locationOutLayout, delayLayoutOut,
            locationOutDateRing;
    RelativeLayout mapContainerOut;
    ScrollView specsContainerOut;
    AutoCompleteTextView cardSearchOut;
    TextView typeNormalOut, typeSatelliteOut, typeHybridOut, typeTerrainOut, locationOutDateField,
            locationOutDateYearField, locationOutTimeField, currentLocation, mapLocation, radiusMark;
    CheckBox attachLocationOutAction, attachDelayOut;
    RadioButton callCheckLocationOut, messageCheckLocationOut, currentCheck, mapCheck;
    FloatingEditText phoneNumberLocationOut;
    CardView cardOut;
    GoogleMap mapOut;
    Spinner placesListOut;
    SeekBar pointRadius;

    private boolean isMapOutVisible(){
        return mapContainerOut != null && mapContainerOut.getVisibility() == View.VISIBLE;
    }

    private boolean isLayersOutVisible(){
        return layersContainerOut != null && layersContainerOut.getVisibility() == View.VISIBLE;
    }

    private void attachLocationOut() {
        taskField.setHint(getString(R.string.tast_hint));

        locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        fadeInAnimation(locationOutLayout);

        delayLayoutOut = (LinearLayout) findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) findViewById(R.id.specsContainerOut);
        layersContainerOut = (LinearLayout) findViewById(R.id.layersContainerOut);
        layersContainerOut.setVisibility(View.GONE);
        mapContainerOut = (RelativeLayout) findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        attachDelayOut = (CheckBox) findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        expand(delayLayoutOut);
                    } else delayLayoutOut.setVisibility(View.VISIBLE);
                } else {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        collapse(delayLayoutOut);
                    } else delayLayoutOut.setVisibility(View.GONE);
                }
            }
        });

        cardOut = (CardView) findViewById(R.id.cardOut);
        cardOut.setCardBackgroundColor(cSetter.getCardStyle());

        cardClearOut = (ImageButton) findViewById(R.id.cardClearOut);
        zoomOutOut = (ImageButton) findViewById(R.id.zoomOutOut);
        layersOut = (ImageButton) findViewById(R.id.layersOut);
        mapButtonOut = (ImageButton) findViewById(R.id.mapButtonOut);
        myLocationOut = (ImageButton) findViewById(R.id.myLocationOut);

        zoomOutOut.setBackgroundColor(cSetter.getBackgroundStyle());
        layersOut.setBackgroundColor(cSetter.getBackgroundStyle());
        myLocationOut.setBackgroundColor(cSetter.getBackgroundStyle());
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            cardClearOut.setImageResource(R.drawable.ic_backspace_white_24dp);
            mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);
            zoomOutOut.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
            layersOut.setImageResource(R.drawable.ic_layers_white_24dp);
            myLocationOut.setImageResource(R.drawable.ic_my_location_white_24dp);
            layersContainerOut.setBackgroundResource(R.drawable.popup_dark);
        } else {
            cardClearOut.setImageResource(R.drawable.ic_backspace_grey600_24dp);
            mapButtonOut.setImageResource(R.drawable.ic_map_grey600_24dp);
            zoomOutOut.setImageResource(R.drawable.ic_fullscreen_exit_grey600_24dp);
            layersOut.setImageResource(R.drawable.ic_layers_grey600_24dp);
            myLocationOut.setImageResource(R.drawable.ic_my_location_grey600_24dp);
            layersContainerOut.setBackgroundResource(R.drawable.popup);
        }

        cardClearOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardSearchOut.setText("");
            }
        });
        mapButtonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOutAnimation(specsContainerOut);
                fadeInAnimation(mapContainerOut);
                mapCheck.setChecked(true);
            }
        });
        zoomOutOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersOutVisible()) hideOver(layersContainerOut);
                fadeOutAnimation(mapContainerOut);
                fadeInAnimation(specsContainerOut);
            }
        });
        layersOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersOutVisible()) hideOver(layersContainerOut);
                else showOver(layersContainerOut);
            }
        });
        myLocationOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayersOutVisible()) hideOver(layersContainerOut);
                Location location = mapOut.getMyLocation();
                if (location != null){
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    LatLng pos = new LatLng(lat, lon);
                    mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                }
            }
        });

        typeNormalOut = (TextView) findViewById(R.id.typeNormalOut);
        typeSatelliteOut = (TextView) findViewById(R.id.typeSatelliteOut);
        typeHybridOut = (TextView) findViewById(R.id.typeHybridOut);
        typeTerrainOut = (TextView) findViewById(R.id.typeTerrainOut);
        currentLocation = (TextView) findViewById(R.id.currentLocation);
        mapLocation = (TextView) findViewById(R.id.mapLocation);
        radiusMark = (TextView) findViewById(R.id.radiusMark);
        typeNormalOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapOut.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                layersContainerOut.setVisibility(View.GONE);
            }
        });
        typeSatelliteOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapOut.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                layersContainerOut.setVisibility(View.GONE);
            }
        });
        typeHybridOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapOut.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                layersContainerOut.setVisibility(View.GONE);
            }
        });
        typeTerrainOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapOut.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                layersContainerOut.setVisibility(View.GONE);
            }
        });

        currentCheck = (RadioButton) findViewById(R.id.currentCheck);
        mapCheck = (RadioButton) findViewById(R.id.mapCheck);
        currentCheck.setOnCheckedChangeListener(this);
        mapCheck.setOnCheckedChangeListener(this);
        currentCheck.setChecked(true);

        pointRadius = (SeekBar) findViewById(R.id.pointRadius);
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
            pointRadius.setProgress(sPrefs.loadInt(Constants.APP_UI_PREFERENCES_LOCATION_RADIUS));

        cardSearchOut = (AutoCompleteTextView) findViewById(R.id.cardSearchOut);
        cardSearchOut.setThreshold(3);
        adapter = new ArrayAdapter<>(
                ReminderManager.this, android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        cardSearchOut.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isLayersOutVisible()) hideOver(layersContainerOut);
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
        cardSearchOut.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                mapOut.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                destination = mapOut.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }
        });

        actionLocationOut = (LinearLayout) findViewById(R.id.actionLocationOut);
        actionLocationOut.setVisibility(View.GONE);

        attachLocationOutAction = (CheckBox) findViewById(R.id.attachLocationOutAction);
        attachLocationOutAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showOver(actionLocationOut);
                    addNumberButtonLocationOut = (ImageButton) findViewById(R.id.addNumberButtonLocationOut);
                    addNumberButtonLocationOut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = ProgressDialog.show(ReminderManager.this, null, getString(R.string.load_contats), true);
                            pickContacts(pd);
                        }
                    });
                    setImage(addNumberButtonLocationOut);

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
                    hideOver(actionLocationOut);
                    taskField.setHint(getString(R.string.tast_hint));
                }
            }
        });

        if (attachLocationOutAction.isChecked()) showOver(actionLocationOut);

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

        locationOutDateRing = (LinearLayout) findViewById(R.id.locationOutDateRing);
        locationOutDateRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        locationOutDateField = (TextView) findViewById(R.id.locationOutDateField);
        locationOutDateField.setTypeface(Utils.getMediumTypeface(this));
        locationOutDateField.setText(dayStr + "/" + monthStr);
        locationOutDateYearField = (TextView) findViewById(R.id.locationOutDateYearField);
        locationOutTimeField = (TextView) findViewById(R.id.locationOutTimeField);
        locationOutTimeField.setTypeface(Utils.getMediumTypeface(this));
        locationOutTimeField.setText(Utils.getTime(cal.getTime(),
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
        locationOutTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog().show();
            }
        });
        locationOutDateYearField.setTypeface(Utils.getThinTypeface(this));
        locationOutDateYearField.setText(String.valueOf(myYear));

        placesListOut = (Spinner) findViewById(R.id.placesListOut);
        placesListOut.setBackgroundColor(cSetter.getSpinnerStyle());
        if (spinnerArray.isEmpty()){
            placesListOut.setVisibility(View.GONE);
        } else {
            placesListOut.setVisibility(View.VISIBLE);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            placesListOut.setAdapter(spinnerArrayAdapter);
            placesListOut.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (isLayersOutVisible()) hideOver(layersContainerOut);
                    if (position > 0){
                        String placeName = spinnerArray.get(position);
                        DB.open();
                        Cursor c = DB.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(Constants.LocationConstants.COLUMN_LOCATION_LONGITUDE));

                            LatLng latLng = new LatLng(latitude, longitude);
                            mapOut.clear();
                            String title = taskField.getText().toString().trim();
                            if (title.matches("")) {
                                title = latLng.toString();
                            }
                            destination = mapOut.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                                    .draggable(true));

                            mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        if (c != null) c.close();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    if (isLayersOutVisible()) hideOver(layersContainerOut);
                }
            });
        }

        MapFragment fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.mapOut);
        mapOut = fragment.getMap();
        mapOut.getUiSettings().setMyLocationButtonEnabled(false);
        sPrefs = new SharedPrefs(ReminderManager.this);
        String type = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            mapOut.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            mapOut.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            mapOut.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            mapOut.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            mapOut.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        mapOut.setMyLocationEnabled(true);
        if (mapOut.getMyLocation() != null) {
            double lat = mapOut.getMyLocation().getLatitude();
            double lon = mapOut.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
        mapOut.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!spinnerArray.isEmpty()){
                    placesListOut.setSelection(0);
                }
                if (isLayersVisible()) hideOver(layersContainerOut);
                mapOut.clear();
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = latLng.toString();
                }
                destination = mapOut.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                        .draggable(true));
                mapLocation.setText(getAddress(latLng.latitude, latLng.longitude));
            }
        });

        if (destination != null) {
            LatLng latLng = destination.getPosition();
            destination = mapOut.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(destination.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle()))
                    .draggable(true));
            mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            mapLocation.setText(getAddress(latLng.latitude, latLng.longitude));
        }

        if (id != 0 && isSame()) {
            DB.open();
            Cursor c = DB.getTask(id);
            String text = "", number = null, remType = "";
            double latitude = 0, longitude = 0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                remType = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
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

                locationTimeField.setText(Utils.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)));
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

            taskField.setText(text);
            if (longitude != 0 && latitude != 0) {
                destination = mapOut.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(text)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
                mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
            }
        }
    }

    private void fadeOutAnimation(View view){
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
            fadeOut.setDuration(400);
            view.setAnimation(fadeOut);
            view.setVisibility(View.GONE);
        } else view.setVisibility(View.GONE);
    }

    private void detachDateReminder(){
        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        fadeOutAnimation(by_date_layout);
    }

    private void detachWeekDayReminder(){
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        fadeOutAnimation(weekday_layout);
    }

    private void detachTimeReminder(){
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        fadeOutAnimation(after_time_layout);
    }

    private void detachSkype(){
        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        fadeOutAnimation(skype_layout);
    }

    private void detachApplication(){
        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        fadeOutAnimation(application_layout);
    }

    private void detachCall(){
        call_layout = (LinearLayout) findViewById(R.id.call_layout);
        fadeOutAnimation(call_layout);
    }

    private void detachMessage(){
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        fadeOutAnimation(message_layout);
    }

    private void detachLocation(){
        geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        fadeOutAnimation(geolocationlayout);
    }

    private void detachMonthDay(){
        monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        fadeOutAnimation(monthDayLayout);
    }

    private void detachLocationOut(){
        locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        fadeOutAnimation(locationOutLayout);
        if (mLocList != null) mLocationManager.removeUpdates(mLocList);
    }

    private boolean isDateReminderAttached(){
        by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        return by_date_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isWeekDayReminderAttached(){
        weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        return weekday_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isTimeReminderAttached(){
        after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        return after_time_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isSkypeAttached(){
        skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        return skype_layout.getVisibility() == View.VISIBLE;
    }

    private boolean isApplicationAttached(){
        application_layout = (LinearLayout) findViewById(R.id.application_layout);
        return application_layout.getVisibility() == View.VISIBLE;
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
                        Intent i = new Intent(ReminderManager.this, ContactsList.class);
                        i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contacts);
                        startActivityForResult(i, Constants.REQUEST_CODE_CONTACTS);
                    }
                });
            }
        }).start();
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReminderManager.this);
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
                        Toast.makeText(ReminderManager.this, getString(R.string.not_selected_application_message), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (isWeekDayReminderAttached()) {
                if (!checkNumber()) {
                    saveWeekTask();
                } else {
                    weekPhoneNumber.setError(getString(R.string.number_error));
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

        new Notifier(ReminderManager.this).recreatePermanent();
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

    private int getSyncCode(CheckBox tasks){
        if (tasks.isChecked()) return Constants.SYNC_GTASKS_ONLY;
        else return Constants.SYNC_NO;
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

        interval = new Interval(ReminderManager.this);
        String repeat = interval.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(), wednesdayCheck.isChecked(),
                thursdayCheck.isChecked(), fridayCheck.isChecked(), saturdayCheck.isChecked(), sundayCheck.isChecked());
        if (repeat.matches(Constants.NOTHING_CHECKED)) {
            Toast.makeText(ReminderManager.this, getString(R.string.weekday_nothing_checked), Toast.LENGTH_SHORT).show();
            return;
        }

        DB.open();
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && weekExport.isChecked()){
                DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, 0,
                        0, 0, 0, 0, repeat, 1, melody, 0, ledColor, getSyncCode(weekTaskExport), categoryId);
                exportToCalendar(task, ReminderUtils.getWeekTime(myHour, myMinute, repeat), id);
            } else {
                DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, 0,
                        0, 0, 0, 0, repeat, 0, melody, 0, ledColor, getSyncCode(weekTaskExport), categoryId);
            }
            if (gtx.isLinked() && weekTaskExport.isChecked()){
                exportToTasks(task, ReminderUtils.getWeekTime(myHour, myMinute, repeat), id);
            }
            DB.updateDateTime(id);
            new WeekDayReceiver().setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && weekExport.isChecked()) {
                idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, 0, 0, 0, 0, 0,
                        uuID, repeat, 1, melody, 0, ledColor, getSyncCode(weekTaskExport), categoryId);
                exportToCalendar(task, ReminderUtils.getWeekTime(myHour, myMinute, repeat), idN);
            } else {
                idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, 0, 0, 0, 0, 0,
                        uuID, repeat, 0, melody, 0, ledColor, getSyncCode(weekTaskExport), categoryId);
            }
            if (gtx.isLinked() && weekTaskExport.isChecked()){
                exportToTasks(task, ReminderUtils.getWeekTime(myHour, myMinute, repeat), idN);
            }
            DB.updateDateTime(idN);
            new WeekDayReceiver().setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
    }

    private void saveSkypeTask() {
        String task = taskField.getText().toString().trim();
        String type = getSkypeTaskType();
        String number = skypeUser.getText().toString().trim();

        int repeat = Integer.parseInt(repeatDaysSkype.getText().toString().trim());

        DB.open();
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && skypeExport.isChecked()){
                DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(skypeTaskExport), categoryId);
                exportToCalendar(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            } else {
                DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(skypeTaskExport), categoryId);
            }
            if (gtx.isLinked() && skypeTaskExport.isChecked()){
                exportToTasks(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            }
            DB.updateDateTime(id);
            alarm.setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && skypeExport.isChecked()) {
                idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                        uuID, null, 1, melody, 0, ledColor, getSyncCode(skypeTaskExport), categoryId);
                exportToCalendar(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            } else {
                idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                        uuID, null, 0, melody, 0, ledColor, getSyncCode(skypeTaskExport), categoryId);
            }
            if (gtx.isLinked() && skypeTaskExport.isChecked()){
                exportToTasks(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            DB.updateDateTime(idN);
            alarm.setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
    }

    private void saveAppTask() {
        String task = taskField.getText().toString().trim();
        String type = getAppTaskType();
        String number = null;
        if (application.isChecked()){
            number = selectedPackage;
            if (number == null){
                Toast.makeText(ReminderManager.this,
                        getString(R.string.dont_selected_application_message), Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (browser.isChecked()){
            number = browseLink.getText().toString().trim();
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://" + number;
        }

        int repeat = Integer.parseInt(repeatDaysApp.getText().toString().trim());

        DB.open();
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && appExport.isChecked()){
                DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(appTaskExport), categoryId);
                exportToCalendar(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            } else {
                DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(appTaskExport), categoryId);
            }
            if (gtx.isLinked() && appTaskExport.isChecked()){
                exportToTasks(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            }
            DB.updateDateTime(id);
            alarm.setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && appExport.isChecked()) {
                idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                        uuID, null, 1, melody, 0, ledColor, getSyncCode(appTaskExport), categoryId);
                exportToCalendar(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            } else {
                idN = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat, 0, 0, 0, 0,
                        uuID, null, 0, melody, 0, ledColor, getSyncCode(appTaskExport), categoryId);
            }
            if (gtx.isLinked() && appTaskExport.isChecked()){
                exportToTasks(task, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            DB.updateDateTime(idN);
            alarm.setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
    }

    public boolean checkGooglePlayServicesAvailability() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
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
            Log.d("GooglePlayServices", "Result is: " + resultCode);
            return true;
        }
    }

    private boolean checkApplication(){
        if (application.isChecked()) {
            return applicationName.getText().toString().trim().matches("");
        } else return browser.isChecked() && browseLink.getText().toString().trim().matches("");
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

    private boolean checkMessage(){
        return taskField.getText().toString().trim().matches("");
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
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && monthDayExport.isChecked()){
                DB.updateTask(id, text, type, day, 0, 0, myHour, myMinute, 0, number, 0,
                        0, 0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(monthDayTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getMonthTime(myHour, myMinute, day), id);
            } else {
                DB.updateTask(id, text, type, day, 0, 0, myHour, myMinute, 0, number, 0,
                        0, 0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(monthDayTaskExport), categoryId);
            }
            if (gtx.isLinked() && monthDayTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getMonthTime(myHour, myMinute, day), id);
            }
            DB.updateDateTime(id);
            new MonthDayReceiver().setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && monthDayExport.isChecked()) {
                idN = DB.insertTask(text, type, day, 0, 0, myHour, myMinute, 0, number,
                        0, 0, 0, 0, 0,
                        uuID, null, 1, melody, 0, ledColor, getSyncCode(monthDayTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getMonthTime(myHour, myMinute, day), idN);
            } else {
                idN = DB.insertTask(text, type, day, 0, 0, myHour, myMinute, 0, number,
                        0, 0, 0, 0, 0,
                        uuID, null, 0, melody, 0, ledColor, getSyncCode(monthDayTaskExport), categoryId);
            }
            if (gtx.isLinked() && monthDayTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getMonthTime(myHour, myMinute, day), idN);
            }
            DB.updateDateTime(idN);
            new MonthDayReceiver().setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
    }

    private void saveDateTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        int repeat = Integer.parseInt(repeatDays.getText().toString().trim());

        DB.open();
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && dateExport.isChecked()){
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, repeat,
                        0, 0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(dateTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            } else {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, repeat,
                        0, 0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(dateTaskExport), categoryId);
            }
            if (gtx.isLinked() && dateTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            }
            DB.updateDateTime(id);
            alarm.setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && dateExport.isChecked()) {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, repeat, 0, 0, 0, 0,
                        uuID, null, 1, melody, 0, ledColor, getSyncCode(dateTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            } else {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null, repeat, 0, 0, 0, 0,
                        uuID, null, 0, melody, 0, ledColor, getSyncCode(dateTaskExport), categoryId);
            }
            if (gtx.isLinked() && dateTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            DB.updateDateTime(idN);
            alarm.setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
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
        long time = getAfterTime();
        if (time == 0) return;

        DB.open();
        final Calendar c = Calendar.getInstance();
        myYear = c.get(Calendar.YEAR);
        myMonth = c.get(Calendar.MONTH);
        myDay = c.get(Calendar.DAY_OF_MONTH);
        myHour = c.get(Calendar.HOUR_OF_DAY);
        myMinute = c.get(Calendar.MINUTE);
        mySeconds = c.get(Calendar.SECOND);
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && timeExport.isChecked()) {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, null, 0, time,
                        0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(timeTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, time), id);
            } else {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, null, 0, time,
                        0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(timeTaskExport), categoryId);
            }
            if (gtx.isLinked() && timeTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            }
            DB.updateDateTime(id);
            alarm.setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && timeExport.isChecked()) {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, null,
                        0, time, 0, 0, 0, uuID, null, 1, melody, 0, ledColor, getSyncCode(timeTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, time), idN);
            } else {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds, null,
                        0, time, 0, 0, 0, uuID, null, 0, melody, 0, ledColor, getSyncCode(timeTaskExport), categoryId);
            }
            if (gtx.isLinked() && timeTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            DB.updateDateTime(idN);
            alarm.setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
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
        String type = getTaskType();
        String number = phoneNumber.getText().toString().trim();
        int repeat = Integer.parseInt(repeatDaysCall.getText().toString().trim());

        DB.open();
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && callExport.isChecked()) {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number,
                        repeat, 0, 0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(callTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            } else {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number,
                        repeat, 0, 0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(callTaskExport), categoryId);
            }
            if (gtx.isLinked() && callTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            }
            DB.updateDateTime(id);
            alarm.setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && callExport.isChecked()) {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, uuID, null, 1, melody, 0, ledColor, getSyncCode(callTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            } else {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, uuID, null, 0, melody, 0, ledColor, getSyncCode(callTaskExport), categoryId);
            }
            if (gtx.isLinked() && callTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            DB.updateDateTime(idN);
            alarm.setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
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

        DB.open();
        if (id != 0) {
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && messageExport.isChecked()) {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, null, 1, melody, 0, ledColor, getSyncCode(messageTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            } else {
                DB.updateTask(id, text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, null, 0, melody, 0, ledColor, getSyncCode(messageTaskExport), categoryId);
            }
            if (gtx.isLinked() && messageTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), id);
            }
            DB.updateDateTime(id);
            alarm.setAlarm(ReminderManager.this, id);
        } else {
            sHelp = new SyncHelper(ReminderManager.this);
            String uuID = sHelp.generateID();
            long idN;
            if ((sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)) && messageExport.isChecked()) {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, uuID, null, 1, melody, 0, ledColor, getSyncCode(messageTaskExport), categoryId);
                exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            } else {
                idN = DB.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, number, repeat,
                        0, 0, 0, 0, uuID, null, 0, melody, 0, ledColor, getSyncCode(messageTaskExport), categoryId);
            }
            if (gtx.isLinked() && messageTaskExport.isChecked()){
                exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0), idN);
            }
            DB.updateDateTime(idN);
            alarm.setAlarm(ReminderManager.this, idN);
        }
        updatesHelper = new UpdatesHelper(ReminderManager.this);
        updatesHelper.updateWidget();
        finish();
    }

    PositionDelayReceiver positionDelayReceiver = new PositionDelayReceiver();

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
        try {
            dest = destination.getPosition();
        } catch (NullPointerException e){
            isNull = true;
        }
        if (!isNull) {
            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            DB.open();
            if (id != 0) {
                if (attackDelay.isChecked()){
                    DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number,
                            0, 0, 0, latitude, longitude, null, 0, melody, radius, ledColor, 0, categoryId);
                    positionDelayReceiver.setDelay(ReminderManager.this, id);
                    DB.updateDateTime(id);
                } else {
                    DB.updateTask(id, task, type, 0, 0, 0, 0, 0, 0, number,
                            0, 0, 0, latitude, longitude, null, 0, melody, radius, ledColor, 0, categoryId);
                    DB.updateDateTime(id);
                    startService(new Intent(ReminderManager.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else {
                sHelp = new SyncHelper(ReminderManager.this);
                String uuID = sHelp.generateID();
                if (attackDelay.isChecked()){
                    long newIds = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                            number, 0, 0, 0, latitude, longitude, uuID, null, 0, melody, radius, ledColor, 0, categoryId);
                    positionDelayReceiver.setDelay(ReminderManager.this, newIds);
                    DB.updateDateTime(newIds);
                } else {
                    long ids = DB.insertTask(task, type, 0, 0, 0, 0, 0, 0, number,
                            0, 0, 0, latitude, longitude, uuID, null, 0, melody, radius, ledColor, 0, categoryId);
                    DB.updateDateTime(ids);
                    startService(new Intent(ReminderManager.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
            updatesHelper = new UpdatesHelper(ReminderManager.this);
            updatesHelper.updateWidget();
            finish();
        } else {
            Toast.makeText(ReminderManager.this, getString(R.string.point_warning), Toast.LENGTH_SHORT).show();
        }
    }

    private void addLocationOut(){
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = null;
        if (attachLocationOutAction.isChecked()) number = phoneNumberLocationOut.getText().toString().trim();
        LatLng dest = null;
        boolean isNull = false;
        try {
            dest = destination.getPosition();
        } catch (NullPointerException e){
            isNull = true;
        }
        int techRadius = pointRadius.getProgress();
        if (!isNull) {
            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            DB.open();
            if (id != 0) {
                if (attachDelayOut.isChecked()){
                    DB.updateTask(id, task, type, myDay, myMonth, myYear, myHour, myMinute, 0, number,
                            0, 0, 0, latitude, longitude, null, 0, melody, techRadius, ledColor, 0,
                            categoryId);
                    positionDelayReceiver.setDelay(ReminderManager.this, id);
                    DB.updateDateTime(id);
                } else {
                    DB.updateTask(id, task, type, 0, 0, 0, 0, 0, 0, number,
                            0, 0, 0, latitude, longitude, null, 0, melody, techRadius, ledColor, 0,
                            categoryId);
                    DB.updateDateTime(id);
                    startService(new Intent(ReminderManager.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else {
                sHelp = new SyncHelper(ReminderManager.this);
                String uuID = sHelp.generateID();
                if (attachDelayOut.isChecked()){
                    long newIds = DB.insertTask(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                            number, 0, 0, 0, latitude, longitude, uuID, null, 0, melody, techRadius,
                            ledColor, 0, categoryId);
                    positionDelayReceiver.setDelay(ReminderManager.this, newIds);
                    DB.updateDateTime(newIds);
                } else {
                    long ids = DB.insertTask(task, type, 0, 0, 0, 0, 0, 0, number,
                            0, 0, 0, latitude, longitude, uuID, null, 0, melody, techRadius, ledColor, 0,
                            categoryId);
                    DB.updateDateTime(ids);
                    startService(new Intent(ReminderManager.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
            updatesHelper = new UpdatesHelper(ReminderManager.this);
            updatesHelper.updateWidget();
            finish();
        } else {
            Toast.makeText(ReminderManager.this, getString(R.string.point_warning), Toast.LENGTH_SHORT).show();
        }
    }

    public void exportToCalendar(String summary, long startTime, long id){
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            new CalendarManager(ReminderManager.this).addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            new CalendarManager(ReminderManager.this).addEventToStock(summary, startTime);
        }
    }

    public void exportToTasks(String summary, long startTime, long mId){
        long localId = new TasksData(ReminderManager.this).addTask(summary, null, 0, false, startTime,
                null, null, getString(R.string.string_task_from_just_reminder),
                null, null, null, 0, mId, null, Constants.TASKS_NEED_ACTION, false);
        new TaskAsync(ReminderManager.this, summary, null, null,
                TasksConstants.INSERT_TASK, startTime, getString(R.string.string_task_from_just_reminder), localId).execute();
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
        }
    }

    public int getRepeat(int progress) {
        return Interval.getRepeatDays(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void detachLayout(){
        if (isDateReminderAttached()) detachDateReminder();
        if (isTimeReminderAttached()) detachTimeReminder();
        if (isCallAttached()) detachCall();
        if (isMessageAttached()) detachMessage();
        if (isLocationAttached()) detachLocation();
        if (isWeekDayReminderAttached()) detachWeekDayReminder();
        if (isSkypeAttached()) detachSkype();
        if (isApplicationAttached()) detachApplication();
        if (isMonthDayAttached()) detachMonthDay();
        if (isLocationOutAttached()) detachLocationOut();
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
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT));
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            String formattedTime = Utils.getTime(c.getTime(),
                    sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT));

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
        if (isLayersVisible()) {
            hideOver(layersContainer);
            return;
        }
        if (isLayersOutVisible()) {
            hideOver(layersContainerOut);
            return;
        }
        if (mFab.getVisibility() == View.GONE){
            show(mFab);
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
            Cursor c = DB.getTask(id);
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
                        alarm.setAlarm(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                        new MonthDayReceiver().setAlarm(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_LOCATION) && isDelayed) {
                        positionDelayReceiver.setDelay(ReminderManager.this, id);
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

    static final int MIN_DISTANCE = 40;
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
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                expand(navContainer);
            } else navContainer.setVisibility(View.VISIBLE);
        }
    }

    private void onUpSwipe() {
        if (navContainer.getVisibility() == View.VISIBLE) {
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                collapse(navContainer);
            } else navContainer.setVisibility(View.GONE);
        }
    }

    private void onLeftSwipe() {
        int current = spinner.getSelectedItemPosition();
        int maxInt = spinnerArray.size();
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
        int maxInt = spinnerArray.size();
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
                attachCall();
                break;
            case 4:
                detachLayout();
                attachMessage();
                break;
            case 5:
                detachLayout();
                if (checkGooglePlayServicesAvailability()) {
                    attachLocation();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 6:
                detachLayout();
                if (isSkypeClientInstalled()) {
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
                if (checkGooglePlayServicesAvailability()) {
                    attachLocationOut();
                } else {
                    spinner.setSelection(0);
                }
                break;
        }
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_USED_REMINDER, position);
        invalidateOptionsMenu();
    }

    public void goToMarket() {
        Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
        Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }

    public boolean isSkypeClientInstalled() {
        PackageManager myPackageMgr = getPackageManager();
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
        }
        catch (PackageManager.NameNotFoundException e) {
            return (false);
        }
        return (true);
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static boolean isSdPresent() {
        return Environment.getExternalStorageState() != null;
    }

    ArrayList<String> names, foldersFile;
    ArrayList<File> fileList;
    private void pickSounds(final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                fileList = new ArrayList<>();
                fileList.clear();
                File dir;
                if (isSdPresent()) {
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
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_COLOR) {
            if (resultCode == RESULT_OK){
                int position = data.getIntExtra(Constants.SELECTED_LED_COLOR, -1);
                String selColor = null;
                if (position == 0) {
                    ledColor = Constants.ColorConstants.COLOR_WHITE;
                    selColor = getString(R.string.led_color_white);
                } else if (position == 1) {
                    ledColor = Constants.ColorConstants.COLOR_RED;
                    selColor = getString(R.string.led_color_red);
                } else if (position == 2) {
                    ledColor = Constants.ColorConstants.COLOR_GREEN;
                    selColor = getString(R.string.led_color_green);
                } else if (position == 3) {
                    ledColor = Constants.ColorConstants.COLOR_BLUE;
                    selColor = getString(R.string.led_color_blue);
                } else if (position == 4) {
                    ledColor = Constants.ColorConstants.COLOR_ORANGE;
                    selColor = getString(R.string.led_color_orange);
                } else if (position == 5) {
                    ledColor = Constants.ColorConstants.COLOR_YELLOW;
                    selColor = getString(R.string.led_color_yellow);
                } else if (position == 6) {
                    ledColor = Constants.ColorConstants.COLOR_PINK;
                    selColor = getString(R.string.led_color_pink);
                } else if (position == 7) {
                    ledColor = Constants.ColorConstants.COLOR_GREEN_LIGHT;
                    selColor = getString(R.string.led_color_green_light);
                } else if (position == 8) {
                    ledColor = Constants.ColorConstants.COLOR_BLUE_LIGHT;
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
        if (new ManageModule().isPro() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_LED_STATUS)){
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
        if (new ManageModule().isPro() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_LED_STATUS)){
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

    ProgressDialog progressDlg;

    private String getAddress(double currentLat, double currentLong){
        return String.format("%.5f", currentLat) + ", " +
                String.format("%.5f", currentLong);
    }

    @Override
    protected void onDestroy() {
        if (mLocList != null) mLocationManager.removeUpdates(mLocList);
        super.onDestroy();
    }

    LocationManager mLocationManager;
    LocationListener mLocList;

    public class CurrentLocation implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double currentLat = location.getLatitude();
            double currentLong = location.getLongitude();
            String _Location = getAddress(currentLat, currentLong);
            String text = taskField.getText().toString().trim();
            if (text == null || text.matches("")) text = _Location;
            if (isLocationOutAttached()) {
                currentLocation.setText(_Location);
                destination = mapOut.addMarker(new MarkerOptions()
                        .position(new LatLng(currentLat, currentLong))
                        .title(text)
                        .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle())));
                mapOut.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLong), 13));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE) * 2;
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
            long time = (prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE) * 2;
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
            long time = (prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Constants.APP_UI_PREFERENCES_TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }
    }
}