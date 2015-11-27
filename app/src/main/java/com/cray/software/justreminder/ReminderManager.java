package com.cray.software.justreminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.cray.software.justreminder.activities.HelpOverflow;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.async.GeocoderTask;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.dialogs.ExclusionPickerDialog;
import com.cray.software.justreminder.dialogs.LedColor;
import com.cray.software.justreminder.dialogs.TargetRadius;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.activities.FileExplore;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.LocationType;
import com.cray.software.justreminder.reminder.MonthdayType;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.ShoppingType;
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
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Reminder creation activity.
 */
public class ReminderManager extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener, View.OnTouchListener,
        CompoundButton.OnCheckedChangeListener, MapListener, GeocoderTask.GeocoderListener,
        Dialogues.OnCategorySelectListener {

    /**
     * Date reminder type variables.
     */
    private CheckBox dateTaskExport;
    private EditText repeatDays;
    private TextView dateField, timeField;
    private CheckBox dateExport;

    /**
     * Weekday reminder type variables.
     */
    private LinearLayout action_layout;
    private FloatingEditText weekPhoneNumber;
    private TextView weekTimeField;
    private ImageButton weekAddNumberButton;
    private CheckBox weekExport;
    private ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck, fridayCheck, saturdayCheck, sundayCheck;
    private RadioButton callCheck, messageCheck;
    private CheckBox attachAction, weekTaskExport;

    /**
     * Monthday reminder type variables.
     */
    private CheckBox monthDayExport, monthDayTaskExport, monthDayAttachAction;
    private LinearLayout monthDayActionLayout;
    private TextView monthDayField, monthDayTimeField;
    private RadioButton monthDayCallCheck, monthDayMessageCheck, dayCheck, lastCheck;
    private ImageButton monthDayAddNumberButton;
    private FloatingEditText monthDayPhoneNumber;

    /**
     * Call reminder variables.
     */
    private FloatingEditText phoneNumber;
    private TextView callDate, callTime;
    private CheckBox callExport;
    private CheckBox callTaskExport;
    private EditText repeatDaysCall;

    /**
     * Message reminder variables.
     */
    private FloatingEditText messageNumber;
    private TextView messageDate, messageTime;
    private CheckBox messageExport;
    private CheckBox messageTaskExport;
    private EditText repeatDaysMessage;

    /**
     * Time reminder variables.
     */
    private CheckBox timeExport;
    private CheckBox timeTaskExport;
    private TextView hoursView, minutesView, secondsView, selectExclusion;
    private ImageButton deleteButton, exclusionClear;
    private EditText repeatMinutes;
    private String timeString = "000000";

    /**
     * Application reminder type variables.
     */
    private CheckBox appExport, appTaskExport;
    private EditText browseLink, repeatDaysApp;
    private RadioButton application, browser;
    private TextView appDate, appTime, applicationName;
    private RelativeLayout applicationLayout;

    /**
     * Skype reminder type variables.
     */
    private CheckBox skypeExport, skypeTaskExport;
    private EditText skypeUser, repeatDaysSkype;
    private RadioButton skypeCall;
    private RadioButton skypeVideo;
    private TextView skypeDate, skypeTime;

    /**
     * Location reminder variables.
     */
    private LinearLayout delayLayout;
    private CheckBox attackDelay;
    private ImageButton addNumberButtonLocation;
    private LinearLayout actionLocation;
    private RelativeLayout mapContainer;
    private ScrollView specsContainer;
    private CheckBox attachLocationAction;
    private RadioButton callCheckLocation, messageCheckLocation;
    private FloatingEditText phoneNumberLocation;
    private MapFragment map;
    private TextView locationDateField, locationTimeField;
    private AutoCompleteTextView searchField;

    /**
     * LocationOut reminder type variables.
     */
    private ImageButton addNumberButtonLocationOut;
    private LinearLayout actionLocationOut;
    private LinearLayout delayLayoutOut;
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView locationOutDateField, locationOutTimeField, currentLocation,
            mapLocation, radiusMark;
    private CheckBox attachLocationOutAction, attachDelayOut;
    private RadioButton callCheckLocationOut, messageCheckLocationOut, currentCheck, mapCheck;
    private FloatingEditText phoneNumberLocationOut;
    private MapFragment mapOut;

    /**
     * Shopping list reminder type variables.
     */
    private EditText shopEdit;
    private TaskListRecyclerAdapter shoppingAdapter;
    private ShoppingListDataProvider shoppingLists;
    private TextView shoppingNoTime, shoppingDate, shoppingTime;
    private RelativeLayout shoppingTimeContainer;

    /**
     * Extra options views.
     */
    private ScrollView extraScroll;
    private ImageButton extraVibration, extraUnlock, extraVoice, extraWake, extraRepeat, extraAuto,
            extraLimit, extraSwitch;
    private RelativeLayout extraHolder;
    private FrameLayout repeatFrame;
    private TextView repeatLabel;

    /**
     * General views.
     */
    private Toolbar toolbar;
    private Spinner spinner;
    private FloatingEditText taskField;
    private TextView category;
    private FloatingActionButton mFab;
    private LinearLayout navContainer;

    /**
     * Reminder preferences flags.
     */
    private int myHour = 0;
    private int myMinute = 0;
    private int mySeconds = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private int vibration = -1;
    private int voice = -1;
    private int notificationRepeat = -1;
    private int wake = -1;
    private int unlock = -1;
    private int auto = -1;
    private long repeats = -1;
    private long id;
    private String categoryId;
    private String exclusion = null;
    private String type, melody = null, selectedPackage = null;
    private int radius = -1, ledColor = 0;
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> namesList;
    private LatLng curPlace;
    private boolean isShoppingReminder;

    private ColorSetter cSetter = new ColorSetter(ReminderManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(ReminderManager.this);
    private GTasksHelper gtx = new GTasksHelper(ReminderManager.this);

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;
    private boolean isAnimation = false, isCalendar = false, isStock = false, isDark = false;
    private boolean isLocationMessage = false;
    private boolean isLocationOutMessage = false;
    private boolean isWeekMessage = false;
    private boolean isMonthMessage = false;
    private boolean isDelayed = false;

    private Type remControl = new Type(this);
    private Reminder item;
    private Handler handler = new Handler();
    private GeocoderTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(ReminderManager.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
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
                                save();
                                return true;
                            case R.id.action_custom_melody:
                                startActivityForResult(new Intent(ReminderManager.this, FileExplore.class),
                                        Constants.REQUEST_CODE_SELECTED_MELODY);
                                return true;
                            case R.id.action_custom_radius:
                                selectRadius();
                                return true;
                            case R.id.action_custom_color:
                                chooseLEDColor();
                                return true;
                            case MENU_ITEM_DELETE:
                                deleteReminder();
                                return true;
                        }
                        return true;
                    }
                });

        toolbar.setOnTouchListener(this);

        extraHolder = (RelativeLayout) findViewById(R.id.extraHolder);
        extraScroll = (ScrollView) findViewById(R.id.extraScroll);
        extraVibration = (ImageButton) findViewById(R.id.extraVibration);
        extraVoice = (ImageButton) findViewById(R.id.extraVoice);
        extraUnlock = (ImageButton) findViewById(R.id.extraUnlock);
        extraWake = (ImageButton) findViewById(R.id.extraWake);
        extraRepeat = (ImageButton) findViewById(R.id.extraRepeat);
        extraAuto = (ImageButton) findViewById(R.id.extraAuto);
        extraSwitch = (ImageButton) findViewById(R.id.extraSwitch);
        extraLimit = (ImageButton) findViewById(R.id.extraLimit);
        colorifyButtons();

        extraScroll.setVisibility(View.GONE);
        extraHolder.setVisibility(View.GONE);

        extraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchOptions();
            }
        });

        repeatFrame = (FrameLayout) findViewById(R.id.repeatFrame);
        repeatFrame.setBackgroundResource(cSetter.getCardDrawableStyle());
        repeatLabel = (TextView) findViewById(R.id.repeatLabel);
        repeatLabel.setVisibility(View.GONE);
        repeatFrame.setVisibility(View.GONE);
        SeekBar repeatSeek = (SeekBar) findViewById(R.id.repeatSeek);
        repeatSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                repeats = progress + 1;
                repeatLabel.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ViewUtils.fadeInAnimation(repeatLabel, isAnimation);
                handler.removeCallbacks(seek);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.fadeOutAnimation(repeatLabel, isAnimation);
                        ViewUtils.fadeOutAnimation(repeatFrame, isAnimation);
                    }
                }, 500);
            }
        });

        extraUnlock.setOnClickListener(clickListener);
        extraVibration.setOnClickListener(clickListener);
        extraVoice.setOnClickListener(clickListener);
        extraWake.setOnClickListener(clickListener);
        extraRepeat.setOnClickListener(clickListener);
        extraAuto.setOnClickListener(clickListener);
        extraLimit.setOnClickListener(clickListener);

        extraUnlock.setOnLongClickListener(longClickListener);
        extraVibration.setOnLongClickListener(longClickListener);
        extraVoice.setOnLongClickListener(longClickListener);
        extraWake.setOnLongClickListener(longClickListener);
        extraRepeat.setOnLongClickListener(longClickListener);
        extraAuto.setOnLongClickListener(longClickListener);
        extraLimit.setOnLongClickListener(longClickListener);

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
                SuperUtil.startVoiceRecognitionActivity(ReminderManager.this, VOICE_RECOGNITION_REQUEST_CODE);
            }
        });

        category = (TextView) findViewById(R.id.category);
        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.selectCategory(ReminderManager.this, categoryId, ReminderManager.this);
            }
        });

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());
        findViewById(R.id.windowBackground).setOnTouchListener(this);

        DataBase db = new DataBase(this);
        db.open();
        Cursor cf = db.queryCategories();
        if (cf != null && cf.moveToFirst()) {
            String title = cf.getString(cf.getColumnIndex(Constants.COLUMN_TEXT));
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            category.setText(title);
        }
        if (cf != null) cf.close();
        db.close();

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
        mFab.setColorNormal(cSetter.colorAccent());
        mFab.setColorPressed(cSetter.colorAccent());
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ViewUtils.hide(ReminderManager.this, mFab, isAnimation);
                return false;
            }
        });
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
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
                vibration = item.getVibration();
                voice = item.getVoice();
                notificationRepeat = item.getNotificationRepeat();
                wake = item.getWake();
                unlock = item.getUnlock();
                auto = item.getAuto();
                repeats = item.getLimit();
                String catId = item.getCategoryId();
                if (radius == 0) radius = -1;

                if (catId != null && !catId.matches("")) categoryId = catId;

                db = new DataBase(this);
                db.open();
                if (categoryId != null && !categoryId.matches("")) {
                    Cursor cx = db.getCategory(categoryId);
                    if (cx != null && cx.moveToFirst()) {
                        String title = cx.getString(cx.getColumnIndex(Constants.COLUMN_TEXT));
                        category.setText(title);
                    }
                    if (cf != null) cf.close();
                }
                db.close();
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
            } else if (type.matches(Constants.TYPE_SHOPPING_LIST)){
                spinner.setSelection(10);
            } else {
                spinner.setSelection(0);
            }
        }
        clearViews();
    }

    /**
     * Long click listener for extra options buttons.
     * Show toast with button action explanation.
     */
    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            String message = "";
            switch (v.getId()){
                case R.id.extraAuto:
                    message = getString(R.string.silent_sms_explanation);
                    if (isApplicationAttached()) message = getString(R.string.auto_launch_explanation);
                    break;
                case R.id.extraLimit:
                    message = getString(R.string.repeat_limit_explanation);
                    break;
                case R.id.extraVibration:
                    message = getString(R.string.vibration_explanation);
                    break;
                case R.id.extraVoice:
                    message = getString(R.string.settings_tts_explanation);
                    break;
                case R.id.extraWake:
                    message = getString(R.string.wake_explanation);
                    break;
                case R.id.extraUnlock:
                    message = getString(R.string.settings_unlock_explanation);
                    break;
                case R.id.extraRepeat:
                    message = getString(R.string.repeat_explanation);
                    break;
            }
            Messages.toast(ReminderManager.this, message);
            addHandler();
            return false;
        }
    };

    /**
     * Extra options button click listener.
     * Switch button state (selected/don't selected).
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean is = !v.isSelected();
            v.setSelected(is);
            addHandler();
            if (v.getId() == R.id.extraLimit){
                if (is){
                    ViewUtils.fadeInAnimation(repeatFrame, isAnimation);
                    handler.postDelayed(seek, 3000);
                }
            }
        }
    };

    /**
     * Select contact button click listener.
     */
    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Permissions permissions = new Permissions(ReminderManager.this);
            if (permissions.checkPermission(Permissions.READ_CONTACTS)) {
                SuperUtil.selectContact(ReminderManager.this, Constants.REQUEST_CODE_CONTACTS);
            } else {
                permissions.requestPermission(ReminderManager.this,
                        new String[]{Permissions.READ_CONTACTS}, 107);
            }
        }
    };

    /**
     * Delayed handler for hiding extra options container.
     */
    private void addHandler(){
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 5000);
    }

    /**
     * Runnable for hiding repeat limit seekbar.
     */
    private Runnable seek = new Runnable() {
        @Override
        public void run() {
            if (repeatFrame.getVisibility() == View.VISIBLE) {
                ViewUtils.fadeOutAnimation(repeatFrame, isAnimation);
                repeats = -1;
                extraLimit.setSelected(false);
            }
        }
    };

    /**
     * Runnable for hiding extra options container.
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isOptionsVisible()) {
                ViewUtils.hideReveal(extraScroll, isAnimation);
                switchIcon();
            }
        }
    };

    /**
     * Show extra button depending on reminder type.
     */
    private void invalidateButtons(){
        if (isShoppingAttached()){
            if (extraHolder.getVisibility() == View.VISIBLE) ViewUtils.hideOver(extraHolder, isAnimation);
        } else {
            if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.showOver(extraHolder, isAnimation);
                    }
                }, 750);
            } else extraHolder.setVisibility(View.GONE);
            if (notificationRepeat == 1) extraRepeat.setSelected(true);
            else extraRepeat.setSelected(false);

            if (isLocationAttached() || isLocationOutAttached()) {
                extraLimit.setEnabled(false);
            } else {
                extraLimit.setEnabled(true);
                if (repeats > 0) extraLimit.setSelected(true);
                else extraLimit.setSelected(false);
            }

            if (isMessageAttached() || isApplicationAttached() ||
                    (isWeekDayReminderAttached() && isWeekMessage) ||
                    (isMonthDayAttached() && isMonthMessage) ||
                    (isLocationAttached() && isLocationMessage) ||
                    (isLocationOutAttached() && isLocationOutMessage)) {
                extraAuto.setEnabled(true);
                if (auto == 1) extraAuto.setSelected(true);
                else extraAuto.setSelected(false);
            } else extraAuto.setEnabled(false);

            if (vibration == 1) extraVibration.setSelected(true);
            else extraVibration.setSelected(false);

            if (voice == 1) extraVoice.setSelected(true);
            else extraVoice.setSelected(false);

            if (wake == 1) extraWake.setSelected(true);
            else extraWake.setSelected(false);

            if (unlock == 1) extraUnlock.setSelected(true);
            else extraUnlock.setSelected(false);
        }
    }

    /**
     * Set extra option button image depending on dark or light theme.
     */
    private void colorifyButtons() {
        if (isDark){
            extraSwitch.setImageResource(R.drawable.ic_expand_more_white_24dp);
            extraLimit.setImageResource(R.drawable.button_limit_light);
            extraVoice.setImageResource(R.drawable.button_voice_light);
            extraVibration.setImageResource(R.drawable.button_vibration_light);
            extraRepeat.setImageResource(R.drawable.button_repeat_light);
            extraAuto.setImageResource(R.drawable.button_auto_light);
            extraUnlock.setImageResource(R.drawable.button_unlock_light);
            extraWake.setImageResource(R.drawable.button_wake_light);
        } else {
            extraSwitch.setImageResource(R.drawable.ic_expand_more_black_24dp);
            extraLimit.setImageResource(R.drawable.button_limit_dark);
            extraVoice.setImageResource(R.drawable.button_voice_dark);
            extraVibration.setImageResource(R.drawable.button_vibration_dark);
            extraRepeat.setImageResource(R.drawable.button_repeat_dark);
            extraAuto.setImageResource(R.drawable.button_auto_dark);
            extraUnlock.setImageResource(R.drawable.button_unlock_dark);
            extraWake.setImageResource(R.drawable.button_wake_dark);
        }
    }

    /**
     * Check if extra options button container is visible.
     * @return Boolean
     */
    private boolean isOptionsVisible(){
        return extraScroll.getVisibility() == View.VISIBLE;
    }

    /**
     * Show/hide extra container.
     */
    private void switchOptions(){
        if (isOptionsVisible()) {
            ViewUtils.hideReveal(extraScroll, isAnimation);
            handler.removeCallbacks(runnable);
        }
        else {
            ViewUtils.showReveal(extraScroll, isAnimation);
            addHandler();
        }
        switchIcon();
    }

    /**
     * Set image to extra container show/hide button.
     */
    private void switchIcon() {
        if (isOptionsVisible()) {
            if (isDark){
                extraSwitch.setImageResource(R.drawable.ic_expand_less_white_24dp);
            } else {
                extraSwitch.setImageResource(R.drawable.ic_expand_less_black_24dp);
            }
        } else {
            if (isDark){
                extraSwitch.setImageResource(R.drawable.ic_expand_more_white_24dp);
            } else {
                extraSwitch.setImageResource(R.drawable.ic_expand_more_black_24dp);
            }
        }
    }

    /**
     * Hide all reminder types layouts.
     */
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
        findViewById(R.id.shoppingLayout).setVisibility(View.GONE);
    }

    /**
     * Set selecting reminder type spinner adapter.
     */
    private void setUpNavigation() {
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date_title), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.after_time_title), R.drawable.ic_access_time_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_weekdays_title), R.drawable.ic_alarm_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call_title), R.drawable.ic_call_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.send_message_title), R.drawable.ic_textsms_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_location_title), R.drawable.ic_place_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.skype_reminder_type), R.drawable.skype_icon_white));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application_reminder_type), R.drawable.ic_launch_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_by_day_of_month), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_place_out), R.drawable.ic_beenhere_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_shopping_cart_white_24dp));
        } else {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date_title), R.drawable.ic_event_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.after_time_title), R.drawable.ic_access_time_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_weekdays_title), R.drawable.ic_alarm_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call_title), R.drawable.ic_call_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.send_message_title), R.drawable.ic_textsms_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.by_location_title), R.drawable.ic_place_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.skype_reminder_type), R.drawable.skype_icon));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application_reminder_type), R.drawable.ic_launch_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_by_day_of_month), R.drawable.ic_event_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.string_place_out), R.drawable.ic_beenhere_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_shopping_cart_black_24dp));
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * Delete or move to trash reminder.
     */
    private void deleteReminder() {
        DataBase db = new DataBase(this);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            int isArchived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
            if (isArchived == 1) {
                Reminder.delete(id, this);
                Messages.toast(ReminderManager.this, getString(R.string.string_deleted));
            } else Reminder.moveToTrash(id, this, null);
            finish();
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Show location radius selection dialog.
     */
    private void selectRadius() {
        Intent i = new Intent(ReminderManager.this, TargetRadius.class);
        i.putExtra("item", 1);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_RADIUS);
    }

    /**
     * Open LED indicator color selecting window.
     */
    private void chooseLEDColor() {
        Intent i = new Intent(ReminderManager.this, LedColor.class);
        i.putExtra(Constants.BIRTHDAY_INTENT_ID, 4);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_COLOR);
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

    /**
     * Check if selected reminder in spinner matches type that was edited.
     * @return Boolean
     */
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
        if (spinner.getSelectedItemPosition() == 10 && type.matches(Constants.TYPE_SHOPPING_LIST)) is = true;
        return is;
    }

    /**
     * Click listener for date fields.
     */
    private View.OnClickListener dateClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dateDialog();
        }
    };

    /**
     * Click listener for time fields.
     */
    private View.OnClickListener timeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            timeDialog().show();
        }
    };

    /**
     * Show simple date reminder creation layout.
     */
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
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        dateField = (TextView) findViewById(R.id.dateField);
        dateField.setOnClickListener(dateClick);

        dateExport = (CheckBox) findViewById(R.id.dateExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            dateExport.setVisibility(View.VISIBLE);
        }

        dateTaskExport = (CheckBox) findViewById(R.id.dateTaskExport);
        if (gtx.isLinked()){
            dateTaskExport.setVisibility(View.VISIBLE);
        }

        dateField.setText(TimeUtil.getDate(cal.getTime()));
        dateField.setTypeface(AssetsUtil.getMediumTypeface(this));

        timeField = (TextView) findViewById(R.id.timeField);
        timeField.setOnClickListener(timeClick);
        timeField.setText(TimeUtil.getTime(cal.getTime(), sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        timeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDays = (EditText) findViewById(R.id.repeatDays);
        repeatDays.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatDateInt = (SeekBar) findViewById(R.id.repeatDateInt);
        repeatDateInt.setOnSeekBarChangeListener(this);
        repeatDateInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDays.setText(String.valueOf(repeatDateInt.getProgress()));

        invalidateButtons();

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

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            timeField.setText(TimeUtil.getTime(cal.getTime(), sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            dateField.setText(TimeUtil.getDate(cal.getTime()));
            repeatDateInt.setProgress(repCode);
            repeatDays.setText(String.valueOf(repCode));
            invalidateButtons();
        }
    }

    /**
     * Show by day of month reminder creation layout.
     */
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
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        monthDayField = (TextView) findViewById(R.id.monthDayField);
        monthDayField.setOnClickListener(dateClick);

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
        monthDayTimeField.setOnClickListener(timeClick);
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
                        monthDayAddNumberButton.setOnClickListener(contactClick);
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
                                isMonthMessage = b;
                                invalidateButtons();
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

        invalidateButtons();

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

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            monthDayTimeField.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            monthDayField.setText(dayStr);

            invalidateButtons();

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

    /**
     * Show alarm clock reminder type creation layout.
     */
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
            c.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = c.get(Calendar.YEAR);
            myMonth = c.get(Calendar.MONTH);
            myDay = c.get(Calendar.DAY_OF_MONTH);
            myHour = c.get(Calendar.HOUR_OF_DAY);
            myMinute = c.get(Calendar.MINUTE);
        }

        weekTimeField = (TextView) findViewById(R.id.weekTimeField);
        weekTimeField.setOnClickListener(timeClick);
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
                        weekAddNumberButton.setOnClickListener(contactClick);
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
                                isWeekMessage = b;
                                invalidateButtons();
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

        invalidateButtons();

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

            invalidateButtons();

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

    /**
     * Check days toggle buttons depends on weekday string.
     * @param weekdays weekday string.
     */
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

    /**
     * Show timer reminder type creation layout.
     */
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
        selectExclusion = (TextView) findViewById(R.id.selectExclusion);
        ViewUtils.setTypeFont(this, hoursView, minutesView, secondsView, selectExclusion);
        selectExclusion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ReminderManager.this, ExclusionPickerDialog.class), 1111);
            }
        });

        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        exclusionClear = (ImageButton) findViewById(R.id.exclusionClear);
        exclusionClear.setVisibility(View.INVISIBLE);
        sPrefs = new SharedPrefs(this);
        if (isDark) {
            deleteButton.setImageResource(R.drawable.ic_backspace_white_24dp);
            exclusionClear.setImageResource(R.drawable.ic_clear_white_24dp);
        } else {
            deleteButton.setImageResource(R.drawable.ic_backspace_black_24dp);
            exclusionClear.setImageResource(R.drawable.ic_clear_black_24dp);
        }
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
        exclusionClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exclusion != null){
                    exclusion = null;
                    selectExclusion.setText(getString(R.string.select_exclusion));
                    exclusionClear.setVisibility(View.INVISIBLE);
                }
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
            b1.setId(Integer.valueOf(101));
            b2.setId(Integer.valueOf(102));
            b3.setId(Integer.valueOf(103));
            b4.setId(Integer.valueOf(104));
            b5.setId(Integer.valueOf(105));
            b6.setId(Integer.valueOf(106));
            b7.setId(Integer.valueOf(107));
            b8.setId(Integer.valueOf(108));
            b9.setId(Integer.valueOf(109));
            b0.setId(Integer.valueOf(100));
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

        invalidateButtons();

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
                exclusion = item.getExclusion();
            }

            timeString = TimeUtil.generateAfterString(afterTime);
            updateTimeView();
            setExclusion(exclusion);

            if (exp == 1){
                timeExport.setChecked(true);
            }

            if (expTasks == Constants.SYNC_GTASKS_ONLY || expTasks == Constants.SYNC_ALL) {
                timeTaskExport.setChecked(true);
            }

            if (repeat < repeatMinutesSeek.getMax()) repeatMinutesSeek.setProgress(repeat);
            repeatMinutes.setText(String.valueOf(repeat));
            taskField.setText(text);
            invalidateButtons();
        }
    }

    /**
     * Set time in time view fields.
     */
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

    /**
     * Show Skype reminder type creation layout.
     */
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
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        skypeExport = (CheckBox) findViewById(R.id.skypeExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            skypeExport.setVisibility(View.VISIBLE);
        }

        skypeTaskExport = (CheckBox) findViewById(R.id.skypeTaskExport);
        if (gtx.isLinked()){
            skypeTaskExport.setVisibility(View.VISIBLE);
        }

        skypeDate = (TextView) findViewById(R.id.skypeDate);
        skypeDate.setText(TimeUtil.getDate(cal.getTime()));
        skypeDate.setOnClickListener(dateClick);
        skypeDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        skypeTime = (TextView) findViewById(R.id.skypeTime);
        skypeTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        skypeTime.setOnClickListener(timeClick);
        skypeTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysSkype = (EditText) findViewById(R.id.repeatDaysSkype);
        repeatDaysSkype.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatSkype = (SeekBar) findViewById(R.id.repeatSkype);
        repeatSkype.setOnSeekBarChangeListener(this);
        repeatSkype.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysSkype.setText(String.valueOf(repeatSkype.getProgress()));

        invalidateButtons();

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

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            skypeUser.setText(number);
            skypeDate.setText(TimeUtil.getDate(cal.getTime()));
            skypeTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatSkype.setProgress(repCode);
            repeatDaysSkype.setText(String.valueOf(repCode));

            invalidateButtons();
        }
    }

    /**
     * Show application reminder type creation layout.
     */
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
                SuperUtil.selectApplication(ReminderManager.this, Constants.REQUEST_CODE_APPLICATION);
            }
        });
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (isDark){
            pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        } else pickApplication.setImageResource(R.drawable.ic_launch_black_24dp);

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
                invalidateButtons();
            }
        });

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        appExport = (CheckBox) findViewById(R.id.appExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            appExport.setVisibility(View.VISIBLE);
        }

        appTaskExport = (CheckBox) findViewById(R.id.appTaskExport);
        if (gtx.isLinked()){
            appTaskExport.setVisibility(View.VISIBLE);
        }

        appDate = (TextView) findViewById(R.id.appDate);
        appDate.setText(TimeUtil.getDate(cal.getTime()));
        appDate.setOnClickListener(dateClick);
        appDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        appTime = (TextView) findViewById(R.id.appTime);
        appTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        appTime.setOnClickListener(timeClick);
        appTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysApp = (EditText) findViewById(R.id.repeatDaysApp);
        repeatDaysApp.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatApp = (SeekBar) findViewById(R.id.repeatApp);
        repeatApp.setOnSeekBarChangeListener(this);
        repeatApp.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysApp.setText(String.valueOf(repeatApp.getProgress()));

        invalidateButtons();

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

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);

            appDate.setText(TimeUtil.getDate(cal.getTime()));
            appTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatApp.setProgress(repCode);
            repeatDaysApp.setText(String.valueOf(repCode));
            invalidateButtons();
        }
    }

    /**
     * Show call reminder type creation layout.
     */
    private void attachCall(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout call_layout = (LinearLayout) findViewById(R.id.call_layout);
        ViewUtils.fadeInAnimation(call_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_CALL);
        dateType.inflateView(R.id.call_layout);
        remControl = dateType;

        ImageButton addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addNumberButton, isDark);

        phoneNumber = (FloatingEditText) findViewById(R.id.phoneNumber);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        callExport = (CheckBox) findViewById(R.id.callExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) || sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            callExport.setVisibility(View.VISIBLE);
        }

        callTaskExport = (CheckBox) findViewById(R.id.callTaskExport);
        if (gtx.isLinked()){
            callTaskExport.setVisibility(View.VISIBLE);
        }

        callDate = (TextView) findViewById(R.id.callDate);
        callDate.setText(TimeUtil.getDate(cal.getTime()));
        callDate.setOnClickListener(dateClick);
        callDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        callTime = (TextView) findViewById(R.id.callTime);
        callTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        callTime.setOnClickListener(timeClick);
        callTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysCall = (EditText) findViewById(R.id.repeatDaysCall);
        repeatDaysCall.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatCallInt = (SeekBar) findViewById(R.id.repeatCallInt);
        repeatCallInt.setOnSeekBarChangeListener(this);
        repeatCallInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysCall.setText(String.valueOf(repeatCallInt.getProgress()));

        invalidateButtons();

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

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            phoneNumber.setText(number);
            callDate.setText(TimeUtil.getDate(cal.getTime()));
            callTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatCallInt.setProgress(repCode);
            repeatDaysCall.setText(String.valueOf(repCode));
            invalidateButtons();
        }
    }

    /**
     * Show message reminder type creation layout.
     */
    private void attachMessage(){
        taskField.setHint(getString(R.string.message_field_hint));

        LinearLayout message_layout = (LinearLayout) findViewById(R.id.message_layout);
        ViewUtils.fadeInAnimation(message_layout, isAnimation);

        DateType dateType = new DateType(this, Constants.TYPE_MESSAGE);
        dateType.inflateView(R.id.message_layout);
        remControl = dateType;

        ImageButton addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addMessageNumberButton, isDark);

        messageNumber = (FloatingEditText) findViewById(R.id.messageNumber);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        messageExport = (CheckBox) findViewById(R.id.messageExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK))){
            messageExport.setVisibility(View.VISIBLE);
        }

        messageTaskExport = (CheckBox) findViewById(R.id.messageTaskExport);
        if (gtx.isLinked()){
            messageTaskExport.setVisibility(View.VISIBLE);
        }

        messageDate = (TextView) findViewById(R.id.messageDate);
        messageDate.setText(TimeUtil.getDate(cal.getTime()));
        messageDate.setOnClickListener(dateClick);
        messageDate.setTypeface(AssetsUtil.getMediumTypeface(this));

        messageTime = (TextView) findViewById(R.id.messageTime);
        messageTime.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        messageTime.setOnClickListener(timeClick);
        messageTime.setTypeface(AssetsUtil.getMediumTypeface(this));

        repeatDaysMessage = (EditText) findViewById(R.id.repeatDaysMessage);
        repeatDaysMessage.setTypeface(AssetsUtil.getLightTypeface(this));

        SeekBar repeatMessageInt = (SeekBar) findViewById(R.id.repeatMessageInt);
        repeatMessageInt.setOnSeekBarChangeListener(this);
        repeatMessageInt.setMax(Configs.REPEAT_SEEKBAR_MAX);
        repeatDaysMessage.setText(String.valueOf(repeatMessageInt.getProgress()));

        invalidateButtons();

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

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            messageNumber.setText(number);
            messageDate.setText(TimeUtil.getDate(cal.getTime()));
            messageTime.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            repeatMessageInt.setProgress(repCode);
            repeatDaysMessage.setText(String.valueOf(repCode));
            invalidateButtons();
        }
    }

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

    /**
     * Show location reminder type creation layout.
     */
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

        map = new MapFragment();
        map.setListener(this);
        map.enableTouch(true);
        map.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.add(R.id.map, map);
        fragTransaction.commitAllowingStateLoss();

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
            clearField.setImageResource(R.drawable.ic_backspace_black_24dp);
            mapButton.setImageResource(R.drawable.ic_map_black_24dp);
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
                task = new GeocoderTask(ReminderManager.this, ReminderManager.this);
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
                    addNumberButtonLocation.setOnClickListener(contactClick);
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
                            isLocationMessage = b;
                            invalidateButtons();
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
            cal.set(myYear, myMonth, myDay, myHour, myMinute);

        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        locationDateField = (TextView) findViewById(R.id.locationDateField);
        locationDateField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationDateField.setText(TimeUtil.getDate(cal.getTime()));
        locationTimeField = (TextView) findViewById(R.id.locationTimeField);
        locationTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationTimeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        locationDateField.setOnClickListener(dateClick);
        locationTimeField.setOnClickListener(timeClick);

        invalidateButtons();

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

            if (item != null && item.getDue() > 0) {
                cal.set(myYear, myMonth, myDay, myHour, myMinute);

                locationTimeField.setText(TimeUtil.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                locationDateField.setText(TimeUtil.getDate(cal.getTime()));
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
            invalidateButtons();
            if (map != null) map.addMarker(new LatLng(latitude, longitude), text, true, false, radius);
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
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

        mapOut = new MapFragment();
        mapOut.setListener(this);
        mapOut.enableTouch(true);
        mapOut.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.add(R.id.mapOut, mapOut);
        fragTransaction.commitAllowingStateLoss();

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
            mapButtonOut.setImageResource(R.drawable.ic_map_black_24dp);
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
                    addNumberButtonLocationOut.setOnClickListener(contactClick);
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
                            isLocationOutMessage = b;
                            invalidateButtons();
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
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        locationOutDateField = (TextView) findViewById(R.id.locationOutDateField);
        locationOutDateField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationOutDateField.setOnClickListener(dateClick);
        locationOutDateField.setText(TimeUtil.getDate(cal.getTime()));
        locationOutTimeField = (TextView) findViewById(R.id.locationOutTimeField);
        locationOutTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));
        locationOutTimeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        locationOutTimeField.setOnClickListener(timeClick);

        if (curPlace != null) {
            if (mapOut != null) mapOut.addMarker(curPlace, null, true, true, radius);
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        invalidateButtons();

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

            if (item != null && item.getDue() > 0) {
                cal.set(myYear, myMonth, myDay, myHour, myMinute);

                locationOutTimeField.setText(TimeUtil.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                locationOutDateField.setText(TimeUtil.getDate(cal.getTime()));
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

            taskField.setText(text);
            LatLng pos = new LatLng(latitude, longitude);
            if (mapOut != null) mapOut.addMarker(pos, text, true, true, radius);
            mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
            mapCheck.setChecked(true);
            invalidateButtons();
        }
    }

    /**
     * Show shopping list reminder type creation layout.
     */
    private void attachShoppingList(){
        taskField.setHint(R.string.title);

        RelativeLayout shoppingLayout = (RelativeLayout) findViewById(R.id.shoppingLayout);
        ViewUtils.fadeInAnimation(shoppingLayout, isAnimation);

        ShoppingType dateType = new ShoppingType(this);
        dateType.inflateView(R.id.shoppingLayout);
        remControl = dateType;

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        RecyclerView todoList = (RecyclerView) findViewById(R.id.todoList);
        CardView cardContainer = (CardView) findViewById(R.id.cardContainer);
        cardContainer.setCardBackgroundColor(cSetter.getCardStyle());

        shoppingTimeContainer = (RelativeLayout) findViewById(R.id.shoppingTimeContainer);

        shoppingDate = (TextView) findViewById(R.id.shoppingDate);
        shoppingTime = (TextView) findViewById(R.id.shoppingTime);
        shoppingTime.setOnClickListener(timeClick);
        shoppingDate.setOnClickListener(dateClick);

        ImageView shopTimeIcon = (ImageView) findViewById(R.id.shopTimeIcon);
        shopTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingTimeContainer.getVisibility() == View.VISIBLE)
                    ViewUtils.hide(shoppingTimeContainer);
                ViewUtils.show(shoppingNoTime);
                myYear = 0;
                myMonth = 0;
                myDay = 0;
                myHour = 0;
                myMinute = 0;
                isShoppingReminder = false;
            }
        });
        if (isDark) shopTimeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
        else shopTimeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);

        shoppingNoTime  = (TextView) findViewById(R.id.shoppingNoTime);
        shoppingNoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingNoTime.getVisibility() == View.VISIBLE)
                    ViewUtils.hide(shoppingNoTime);
                ViewUtils.show(shoppingTimeContainer);
                cal.setTimeInMillis(System.currentTimeMillis());
                if (myYear > 0){
                    cal.set(myYear, myMonth, myDay, myHour, myMinute);
                } else {
                    myYear = cal.get(Calendar.YEAR);
                    myMonth = cal.get(Calendar.MONTH);
                    myDay = cal.get(Calendar.DAY_OF_MONTH);
                    myHour = cal.get(Calendar.HOUR_OF_DAY);
                    myMinute = cal.get(Calendar.MINUTE);
                }
                shoppingTime.setText(TimeUtil.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                shoppingDate.setText(TimeUtil.getDate(cal.getTime()));
                isShoppingReminder = true;
            }
        });

        shopEdit = (EditText) findViewById(R.id.shopEdit);
        shopEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    String task = shopEdit.getText().toString().trim();
                    if (task.matches("")) {
                        shopEdit.setError(getString(R.string.empty_task));
                        return false;
                    } else {
                        shoppingLists.addItem(new ShoppingList(task));
                        shoppingAdapter.notifyDataSetChanged();
                        shopEdit.setText("");
                        return true;
                    }
                } else return false;
            }
        });
        ImageButton addButton = (ImageButton) findViewById(R.id.addButton);
        if (isDark) addButton.setImageResource(R.drawable.ic_add_white_24dp);
        else addButton.setImageResource(R.drawable.ic_add_black_24dp);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = shopEdit.getText().toString().trim();
                if (task.matches("")) {
                    shopEdit.setError(getString(R.string.empty_task));
                    return;
                }

                shoppingLists.addItem(new ShoppingList(task));
                shoppingAdapter.notifyDataSetChanged();
                shopEdit.setText("");
            }
        });

        shoppingLists = new ShoppingListDataProvider(this);
        shoppingAdapter = new TaskListRecyclerAdapter(this, shoppingLists, new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                ShoppingList item = shoppingLists.getItem(position);
                if (item.isChecked() == 1) item.setIsChecked(0);
                else item.setIsChecked(1);
                shoppingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemDelete(int position) {
                shoppingLists.removeItem(position);
                shoppingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemChange(int position) {

            }
        });
        todoList.setLayoutManager(new LinearLayoutManager(this));
        todoList.setAdapter(shoppingAdapter);
        invalidateButtons();
        if (id != 0 && isSame()){
            if (remControl instanceof ShoppingType) {
                shoppingLists.clear();
                shoppingLists = new ShoppingListDataProvider(this, id, ShoppingList.ACTIVE);
                shoppingAdapter = new TaskListRecyclerAdapter(this, shoppingLists, new TaskListRecyclerAdapter.ActionListener() {
                    @Override
                    public void onItemCheck(int position, boolean isChecked) {
                        ShoppingList item = shoppingLists.getItem(position);
                        if (item.isChecked() == 1) item.setIsChecked(0);
                        else item.setIsChecked(1);
                        shoppingAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onItemDelete(int position) {
                        shoppingLists.removeItem(position);
                        shoppingAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onItemChange(int position) {

                    }
                });
                todoList.setAdapter(shoppingAdapter);
            }
            invalidateButtons();

            myHour = item.getHour();
            myMinute = item.getMinute();
            myDay = item.getDay();
            myMonth = item.getMonth();
            myYear = item.getYear();

            if (item.getDue() > 0) {
                cal.set(myYear, myMonth, myDay, myHour, myMinute);

                shoppingTime.setText(TimeUtil.getTime(cal.getTime(),
                        sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
                shoppingDate.setText(TimeUtil.getDate(cal.getTime()));
                if (shoppingNoTime.getVisibility() == View.VISIBLE)
                    ViewUtils.hide(shoppingNoTime);
                ViewUtils.show(shoppingTimeContainer);
                isShoppingReminder = true;
            } else {
                if (shoppingTimeContainer.getVisibility() == View.VISIBLE)
                    ViewUtils.hide(shoppingTimeContainer);
                ViewUtils.show(shoppingNoTime);
                isShoppingReminder = false;
            }

            taskField.setText(item.getTitle());
        }
    }

    /**
     * Hide currently attached layout.
     */
    private void detachCurrentView(){
        if (remControl.getView() != 0) {
            ViewUtils.fadeOutAnimation(findViewById(remControl.getView()), isAnimation);
        }
    }

    /**
     * Save new or update current reminder.
     */
    private void save() {
        Reminder item = getData();
        if (item == null) return;
        if (id != 0){
            remControl.save(id, item);
            if (remControl instanceof ShoppingType){
                ((ShoppingType) remControl).saveShopList(id, shoppingLists.getData(), shoppingLists.getRemovedItems());
            }
        } else {
            long remId = remControl.save(item);
            if (remControl instanceof ShoppingType){
                ((ShoppingType) remControl).saveShopList(remId, shoppingLists.getData(), null);
            }
        }
        new SharedPrefs(this).saveBoolean(Prefs.REMINDER_CHANGED, true);
        finish();
    }

    /**
     * Check if location reminder type layout visible.
     * @return Boolean
     */
    private boolean isLocationAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_LOCATION);
    }

    /**
     * Check if location out reminder type layout visible.
     * @return Boolean
     */
    private boolean isLocationOutAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_LOCATION_OUT);
    }

    /**
     * Check if date reminder type layout visible.
     * @return Boolean
     */
    private boolean isDateReminderAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_REMINDER);
    }

    /**
     * Check if time reminder type layout visible.
     * @return Boolean
     */
    private boolean isTimeReminderAttached() {
        return remControl instanceof TimerType;
    }

    /**
     * Check if call reminder type layout visible.
     * @return Boolean
     */
    private boolean isCallAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_CALL);
    }

    /**
     * Check if Skype reminder type layout visible.
     * @return Boolean
     */
    private boolean isSkypeAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_SKYPE);
    }

    /**
     * Check if application reminder type layout visible.
     * @return Boolean
     */
    private boolean isApplicationAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_APPLICATION);
    }

    /**
     * Check if weekday reminder type layout visible.
     * @return Boolean
     */
    private boolean isWeekDayReminderAttached() {
        return remControl instanceof WeekdayType;
    }

    /**
     * Check if message reminder type layout visible.
     * @return Boolean
     */
    private boolean isMessageAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_MESSAGE);
    }

    /**
     * Check if monthDay reminder type layout visible.
     * @return Boolean
     */
    private boolean isMonthDayAttached() {
        return remControl instanceof MonthdayType;
    }

    /**
     * Check if shopping list reminder type layout visible.
     * @return Boolean
     */
    private boolean isShoppingAttached() {
        return remControl instanceof ShoppingType;
    }

    /**
     * Get reminder type string.
     * @return String
     */
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

    /**
     * Create reminder object.
     * @return Reminder object
     */
    private Reminder getData() {
        if (isShoppingAttached() && shoppingLists.getCount() == 0){
            Messages.toast(ReminderManager.this, getString(R.string.no_tasks_warming));
            return null;
        }
        String type = getType();
        Log.d(Constants.LOG_TAG, "Task type " + (type != null ? type : "no type"));
        if (type != null) {
            String weekdays = null;
            if (isWeekDayReminderAttached()) {
                Interval interval = new Interval(ReminderManager.this);
                weekdays = interval.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(), wednesdayCheck.isChecked(),
                        thursdayCheck.isChecked(), fridayCheck.isChecked(), saturdayCheck.isChecked(), sundayCheck.isChecked());
                if (weekdays.matches(Constants.NOTHING_CHECKED)) {
                    Messages.toast(ReminderManager.this, getString(R.string.weekday_nothing_checked));
                    return null;
                }
            }
            String task = taskField.getText().toString().trim();
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_WEEKDAY_CALL) ||
                    type.matches(Constants.TYPE_MONTHDAY_CALL) || type.matches(Constants.TYPE_MONTHDAY_CALL_LAST) ||
                    type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_OUT_CALL) ||
                    type.matches(Constants.TYPE_SHOPPING_LIST)) {

            } else {
                if (task.matches("")) {
                    taskField.setError(getString(R.string.empty_field_error));
                    return null;
                }
            }
            if (checkNumber()) return null;
            String number = getNumber();
            if (isApplicationAttached()) {
                if (application.isChecked()) {
                    number = selectedPackage;
                    if (number == null) {
                        Messages.toast(ReminderManager.this, getString(R.string.dont_selected_application_message));
                        return null;
                    }
                } else if (browser.isChecked()) {
                    number = browseLink.getText().toString().trim();
                    if (number.matches("") || number.matches(".*https?://"))
                        return null;
                    if (!number.startsWith("http://") && !number.startsWith("https://"))
                        number = "http://" + number;
                }
            }
            Log.d(Constants.LOG_TAG, "Task number " + (number != null ? number : "no number"));
            String uuId = SyncHelper.generateID();

            Double latitude = 0.0;
            Double longitude = 0.0;
            if (isLocationAttached() || isLocationOutAttached()) {
                if (!LocationUtil.checkLocationEnable(this)) {
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
                    Messages.toast(ReminderManager.this, getString(R.string.point_warning));
                    return null;
                }

                latitude = dest.latitude;
                longitude = dest.longitude;
            }
            Log.d(Constants.LOG_TAG, "Place coords " + latitude + "," + longitude);

            if (isTimeReminderAttached()) {
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
            if (isTimeReminderAttached()) {
                repMinute = SuperUtil.getAfterTime(this, timeString);
                if (repMinute == 0) return null;
            }
            Log.d(Constants.LOG_TAG, "Task after minute " + repMinute);

            int export = getExportCode();
            Log.d(Constants.LOG_TAG, "Task export code " + export);

            if (isMonthDayAttached()) {
                if (type.endsWith("_last")) myDay = 0;
            }

            long due = getDue(weekdays, repMinute);
            Log.d(Constants.LOG_TAG, "Task due " + due);

            if (isLocationAttached() || isLocationOutAttached() || isShoppingAttached()) {
                if (isLocationAttached() && !attackDelay.isChecked()) {
                    myDay = 0;
                    myMonth = 0;
                    myYear = 0;
                    myHour = 0;
                    myMinute = 0;
                    mySeconds = 0;
                }
                if (isLocationOutAttached() && !attachDelayOut.isChecked()) {
                    myDay = 0;
                    myMonth = 0;
                    myYear = 0;
                    myHour = 0;
                    myMinute = 0;
                    mySeconds = 0;
                }
                if (!isShoppingReminder){
                    myDay = 0;
                    myMonth = 0;
                    myYear = 0;
                    myHour = 0;
                    myMinute = 0;
                    mySeconds = 0;
                }
            }

            int vibro = getVibro();
            int voice = getVoice();
            int notification = getNotification();
            int wake = getWake();
            int unlock = getUnlock();
            int auto = getAuto();
            long limit = getLimit();
            if (repeat == 0) limit = -1;

            Log.d(Constants.LOG_TAG, "V " + vibro + ", Vo " + voice + ", N " + notification + ", W " +
                    wake + ", U " + unlock + ", A " + auto + ", L " + limit);

            return new Reminder(0, task, type, weekdays, melody, categoryId, uuId,
                    new double[]{latitude, longitude}, number, myDay, myMonth, myYear, myHour, myMinute,
                    mySeconds, repeat, export, radius, ledColor, sync, repMinute, due, 0, vibro, voice,
                    notification, wake, unlock, auto, limit, exclusion);
        } else return null;
    }

    /**
     * Get repeat limit for reminder.
     * @return Long
     */
    private long getLimit() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (extraLimit.isEnabled()) {
                if (extraLimit.isSelected()) return repeats;
                else return -1;
            } else return -1;
        } else return -1;
    }

    /**
     * Get auto action for reminder.
     * @return Integer
     */
    private int getAuto() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (extraAuto.isEnabled()) {
                if (extraAuto.isSelected()) return 1;
                else return 0;
            } else return -1;
        } else return -1;
    }

    /**
     * Get unlock screen flag for reminder.
     * @return Integer
     */
    private int getUnlock() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (extraUnlock.isSelected()) return 1;
            else return 0;
        } else return -1;
    }

    /**
     * Get awake screen flag for reminder.
     * @return Integer
     */
    private int getWake() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (extraWake.isSelected()) return 1;
            else return 0;
        } else return -1;
    }

    /**
     * Get notification repeat flag for reminder.
     * @return Integer
     */
    private int getNotification() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (isOptionsVisible() && extraRepeat.isEnabled()) {
                if (extraRepeat.isSelected()) return 1;
                else return 0;
            } else return -1;
        } else return -1;
    }

    /**
     * Get voice notification flag for reminder.
     * @return Integer
     */
    private int getVoice() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (extraVoice.isSelected()) return 1;
            else return 0;
        } else return -1;
    }

    /**
     * Get vibration flag for reminder.
     * @return Integer
     */
    private int getVibro() {
        if (sPrefs.loadBoolean(Prefs.EXTRA_OPTIONS)) {
            if (extraVibration.isSelected()) return 1;
            else return 0;
        } else return -1;
    }

    /**
     * Get export to calendar code for reminder.
     * @return Integer
     */
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

    /**
     * Calculate due time for reminder.
     * @param weekdays selected day of week string.
     * @param time current time in millisecond for timer.
     * @return time in milliseconds.
     */
    private long getDue(String weekdays, long time) {
        if (isWeekDayReminderAttached()){
            return ReminderUtils.getWeekTime(myHour, myMinute, weekdays);
        } else if (isMonthDayAttached()){
            return ReminderUtils.getMonthTime(myHour, myMinute, myDay);
        } else if (isTimeReminderAttached()){
            return ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, time);
        } else return ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
    }

    /**
     * Get Google Tasks export code for reminder.
     * @return Integer
     */
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

    /**
     * Get repeat code for reminder .
     * @return Integer
     */
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

    /**
     * Get number for reminder.
     * @return String
     */
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

    /**
     * Check if number inserted.
     * @return Boolean
     */
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
            case R.id.callTime:
                timeDialog().show();
                break;
            case R.id.messageTime:
                timeDialog().show();
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
            calendar.set(year, monthOfYear, dayOfMonth);

            if (isCallAttached()){
                callDate.setText(TimeUtil.getDate(calendar.getTime()));
            }
            if (isMonthDayAttached()){
                if (myDay < 29) monthDayField.setText(dayOfMonth);
                else {
                    myDay = 28;
                    Messages.toast(ReminderManager.this, getString(R.string.string_max_day_message));
                }
            }
            if (isSkypeAttached()){
                skypeDate.setText(TimeUtil.getDate(calendar.getTime()));
            }
            if (isApplicationAttached()){
                appDate.setText(TimeUtil.getDate(calendar.getTime()));
            }
            if (isDateReminderAttached()){
                dateField.setText(TimeUtil.getDate(calendar.getTime()));
            }
            if (isMessageAttached()){
                messageDate.setText(TimeUtil.getDate(calendar.getTime()));
            }
            if (isLocationAttached()){
                if (attackDelay.isChecked()){
                    if (delayLayout.getVisibility() == View.VISIBLE) {
                        locationDateField.setText(TimeUtil.getDate(calendar.getTime()));
                    }
                }
            }
            if (isLocationOutAttached()){
                if (attachDelayOut.isChecked()){
                    if (delayLayoutOut.getVisibility() == View.VISIBLE) {
                        locationOutDateField.setText(TimeUtil.getDate(calendar.getTime()));
                    }
                }
            }
            if (isShoppingAttached()){
                shoppingDate.setText(TimeUtil.getDate(calendar.getTime()));
            }
        }
    };

    /**
     * Create time picker dialog.
     * @return Time picker dialog
     */
    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, myHour, myMinute,
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
    }

    /**
     * Time selection callback.
     */
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
            if (isShoppingAttached()){
                shoppingTime.setText(formattedTime);
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

    /**
     * Restore currently edited reminder.
     */
    private void restoreTask(){
        if (id != 0) {
            DataBase db = new DataBase(this);
            db.open();
            if (db.getCount() == 0) {
                stopService(new Intent(ReminderManager.this, GeolocationService.class));
                stopService(new Intent(ReminderManager.this, CheckPosition.class));
            } else {
                Cursor c = db.queryGroup();
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
            Cursor c = db.getReminder(id);
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
                            type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_SHOPPING_LIST)) {
                        new AlarmReceiver().setAlarm(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                        new MonthDayReceiver().setAlarm(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_LOCATION) && isDelayed) {
                        new PositionDelayReceiver().setDelay(ReminderManager.this, id);
                    } else if (type.startsWith(Constants.TYPE_LOCATION_OUT) && isDelayed) {
                        new PositionDelayReceiver().setDelay(ReminderManager.this, id);
                    }
                }
            }
            if (c != null) c.close();
            db.close();
        }
        new Notifier(ReminderManager.this).recreatePermanent();
        finish();
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

    /**
     * Show toolbar.
     */
    private void onDownSwipe() {
        if (navContainer.getVisibility() == View.GONE) {
            if (isAnimation) {
                ViewUtils.expand(navContainer);
            } else navContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide toolbar.
     */
    private void onUpSwipe() {
        if (navContainer.getVisibility() == View.VISIBLE) {
            if (isAnimation) {
                ViewUtils.collapse(navContainer);
            } else navContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Switch to previous reminder type in list.
     */
    private void onLeftSwipe() {
        int current = spinner.getSelectedItemPosition();
        if (current > 0){
            spinner.setSelection(current - 1);
            switchIt(current - 1);
        }
        if (0 == current){
            spinner.setSelection(Configs.NUMBER_OF_REMINDERS);
            switchIt(Configs.NUMBER_OF_REMINDERS);
        }
    }

    /**
     * Switch to next reminder type in list.
     */
    private void onRightSwipe() {
        int current = spinner.getSelectedItemPosition();
        if (current < Configs.NUMBER_OF_REMINDERS){
            spinner.setSelection(current + 1);
            switchIt(current + 1);
        }
        if (current == Configs.NUMBER_OF_REMINDERS){
            spinner.setSelection(0);
            switchIt(0);
        }
    }

    /**
     * Show reminder layout.
     * @param position spinner position.
     */
    private void switchIt(int position){
        radius = -1;
        selectedPackage = null;
        switch (position){
            case 0:
                detachCurrentView();
                attachDateReminder();
                break;
            case 2:
                detachCurrentView();
                attachWeekDayReminder();
                break;
            case 1:
                detachCurrentView();
                attachTimeReminder();
                break;
            case 3:
                detachCurrentView();
                if (new Permissions(ReminderManager.this).checkPermission(Permissions.CALL_PHONE)) {
                    attachCall();
                } else {
                    new Permissions(ReminderManager.this)
                            .requestPermission(ReminderManager.this,
                                    new String[]{Permissions.CALL_PHONE}, 109);
                }
                break;
            case 4:
                detachCurrentView();
                if (new Permissions(ReminderManager.this).checkPermission(Permissions.SEND_SMS)) {
                    attachMessage();
                } else {
                    new Permissions(ReminderManager.this)
                            .requestPermission(ReminderManager.this,
                                    new String[]{Permissions.SEND_SMS}, 108);
                }
                break;
            case 5:
                detachCurrentView();
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
                detachCurrentView();
                if (SuperUtil.isSkypeClientInstalled(ReminderManager.this)) {
                    attachSkype();
                } else {
                    spinner.setSelection(0);
                    SuperUtil.installSkype(ReminderManager.this);
                }
                break;
            case 7:
                detachCurrentView();
                attachApplication();
                break;
            case 8:
                detachCurrentView();
                attachMonthDay();
                break;
            case 9:
                detachCurrentView();
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
            case 10:
                detachCurrentView();
                attachShoppingList();
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
                    SuperUtil.selectContact(ReminderManager.this, Constants.REQUEST_CODE_CONTACTS);
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
                melody = data.getStringExtra(Constants.FILE_PICKED);
                if (melody != null) {
                    File musicFile = new File(melody);
                    Messages.toast(ReminderManager.this, getString(R.string.selected_melody_string) + musicFile.getName());
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == RESULT_OK){
                radius = data.getIntExtra(Constants.SELECTED_RADIUS, -1);
                if (radius != -1) {
                    Messages.toast(ReminderManager.this, getString(R.string.selected_radius_string) + radius + " " + getString(R.string.meter));
                    if (isLocationAttached()) map.recreateMarker(radius);
                    if (isLocationOutAttached()) mapOut.recreateMarker(radius);
                }
            }
        }

        if (requestCode == 1111) {
            if (resultCode == RESULT_OK){
                exclusion = data.getStringExtra("excl");
                setExclusion(exclusion);
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
                Messages.toast(ReminderManager.this, getString(R.string.string_selected_led_color) + " " + selColor);
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

    /**
     * Set up exclusion for reminder.
     * @param jsonObject json object string.
     */
    private void setExclusion(String jsonObject){
        if (jsonObject != null) {
            Recurrence recurrence = new Recurrence(jsonObject);
            if (recurrence.getHours() != null) {
                selectExclusion.setText(getString(R.string.excluded_hours) + " " + recurrence.getHours().toString());
                exclusionClear.setVisibility(View.VISIBLE);
            } else {
                String fromHour = recurrence.getFromHour();
                String toHour = recurrence.getToHour();
                selectExclusion.setText(getString(R.string.from_) + " " + fromHour + " " + getString(R.string.to_) + " " + toHour);
                exclusionClear.setVisibility(View.VISIBLE);
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
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
        super.onDestroy();
    }

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    @Override
    public void onAddressReceived(List<Address> addresses) {
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

    @Override
    public void onCategory(String catId, String title) {
        category.setText(title);
        categoryId = catId;
    }

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