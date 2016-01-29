package com.cray.software.justreminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import com.cray.software.justreminder.activities.FileExplore;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.async.GeocoderTask;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.dialogs.ExclusionPickerDialog;
import com.cray.software.justreminder.dialogs.ExtraPickerDialog;
import com.cray.software.justreminder.dialogs.LedColor;
import com.cray.software.justreminder.dialogs.SelectVolume;
import com.cray.software.justreminder.dialogs.TargetRadius;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.fragments.helpers.PlacesMap;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.ActionCallbacksExtended;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.json.JAction;
import com.cray.software.justreminder.json.JExclusion;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JLed;
import com.cray.software.justreminder.json.JMelody;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JParser;
import com.cray.software.justreminder.json.JPlace;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.json.JShopping;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.LocationType;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.Type;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.spinner.SpinnerItem;
import com.cray.software.justreminder.spinner.TitleNavigationAdapter;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.views.RepeatView;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Reminder creation activity.
 */
public class ReminderManager extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener,
        MapListener, GeocoderTask.GeocoderListener, Dialogues.OnCategorySelectListener,
        DateTimeView.OnSelectListener, RepeatView.OnRepeatListener, ActionView.OnActionListener,
        ActionCallbacksExtended {

    private static final String HAS_SHOWCASE = "create_showcase";
    private static final int FILE_REQUEST = 556;
    private static final int REQUEST_EXTRA = 557;
    /**
     * Date reminder type variables.
     */
    private CheckBox dateTaskExport;
    private CheckBox dateExport;

    /**
     * Weekday reminder type variables.
     */
    private TextView weekTimeField;
    private CheckBox weekExport;
    private ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck,
            fridayCheck, saturdayCheck, sundayCheck;
    private CheckBox weekTaskExport;
    private ActionView actionViewWeek;

    /**
     * Monthday reminder type variables.
     */
    private CheckBox monthDayExport, monthDayTaskExport;
    private TextView monthDayField, monthDayTimeField;
    private RadioButton dayCheck, lastCheck;
    private ActionView actionViewMonth;

    /**
     * Call reminder variables.
     */
    private FloatingEditText phoneNumber;
    private CheckBox callExport;
    private CheckBox callTaskExport;

    /**
     * Message reminder variables.
     */
    private FloatingEditText messageNumber;
    private CheckBox messageExport;
    private CheckBox messageTaskExport;

    /**
     * Mail reminder variables.
     */
    private EditText mail, subject;
    private CheckBox mailExport;
    private TextView fileName;
    private CheckBox mailTaskExport;

    /**
     * Time reminder variables.
     */
    private CheckBox timeExport;
    private CheckBox timeTaskExport;
    private TextView hoursView, minutesView, secondsView, selectExclusion;
    private ImageButton deleteButton, exclusionClear;
    private String timeString = "000000";

    /**
     * Application reminder type variables.
     */
    private CheckBox appExport, appTaskExport;
    private EditText browseLink;
    private RadioButton application, browser;
    private TextView applicationName;
    private RelativeLayout applicationLayout;

    /**
     * Skype reminder type variables.
     */
    private CheckBox skypeExport, skypeTaskExport;
    private EditText skypeUser;
    private RadioButton skypeCall;
    private RadioButton skypeVideo;

    /**
     * Location reminder variables.
     */
    private LinearLayout delayLayout;
    private CheckBox attackDelay;
    private RelativeLayout mapContainer;
    private ScrollView specsContainer;
    private MapFragment map;
    private AutoCompleteTextView searchField;
    private ActionView actionViewLocation;

    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView currentLocation, mapLocation, radiusMark;
    private CheckBox attachDelayOut;
    private RadioButton currentCheck, mapCheck;
    private MapFragment mapOut;
    private ActionView actionViewLocationOut;
    private SeekBar pointRadius;

    /**
     * Shopping list reminder type variables.
     */
    private EditText shopEdit;
    private TaskListRecyclerAdapter shoppingAdapter;
    private ShoppingListDataProvider shoppingLists;
    private TextView shoppingNoTime;
    private RelativeLayout shoppingTimeContainer;
    private DateTimeView dateViewShopping;
    private RecyclerView todoList;

    private PlacesMap placesMap;

    /**
     * Extra options views.
     */
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
    private ImageButton insertVoice, changeExtra;

    /**
     * Reminder preferences flags.
     */
    private int myHour = 0;
    private int myMinute = 0;
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
    private long eventTime;
    private long repeatCode = 0;
    private int volume;
    private String categoryId;
    private String exclusion = null;
    private String uuId = null;
    private String attachment = null;
    private String type, melody = null, selectedPackage = null;
    private int radius = -1, ledColor = -1;
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
    private boolean isCalendar = false, isStock = false, isDark = false;
    private boolean isDelayed = false;

    private Type remControl = new Type(this);
    private JModel item;
    private Handler handler = new Handler();
    private GeocoderTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(ReminderManager.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.create_edit_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

        isCalendar = sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        isStock = sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Module.isLollipop()) toolbar.setElevation(R.dimen.toolbar_elevation);
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
                            case R.id.action_volume:
                                selectVolume();
                                return true;
                            case R.id.action_limit:
                                showLimit();
                                return true;
                            case MENU_ITEM_DELETE:
                                deleteReminder();
                                return true;
                        }
                        return true;
                    }
                });

        toolbar.setOnTouchListener(this);

        repeatFrame = (FrameLayout) findViewById(R.id.repeatFrame);
        repeatFrame.setBackgroundResource(cSetter.getCardDrawableStyle());
        repeatLabel = (TextView) findViewById(R.id.repeatLabel);
        repeatLabel.setVisibility(View.GONE);
        repeatFrame.setVisibility(View.GONE);
        SeekBar repeatSeek = (SeekBar) findViewById(R.id.repeatSeek);
        repeatSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    repeats = -1;
                    repeatLabel.setText(R.string.no_limits);
                } else {
                    repeats = progress;
                    repeatLabel.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ViewUtils.fadeInAnimation(repeatLabel);
                handler.removeCallbacks(seek);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.fadeOutAnimation(repeatLabel);
                        ViewUtils.fadeOutAnimation(repeatFrame);
                    }
                }, 500);
            }
        });

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
        insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        changeExtra = (ImageButton) findViewById(R.id.changeExtra);
        insertVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperUtil.startVoiceRecognitionActivity(ReminderManager.this, VOICE_RECOGNITION_REQUEST_CODE);
            }
        });
        changeExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ReminderManager.this, ExtraPickerDialog.class)
                        .putExtra("type", getType())
                        .putExtra("prefs", new int[]{voice, vibration, wake, unlock,
                                notificationRepeat, auto}),
                        REQUEST_EXTRA);
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
        Cursor c = db.queryCategories();
        if (c != null && c.moveToFirst()) {
            String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
            category.setText(title);
        }
        if (c != null) c.close();
        db.close();

        setUpNavigation();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewUtils.slideInDown(ReminderManager.this, toolbar);
            }
        }, 500);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mFab.hide();
                return false;
            }
        });

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);
        String filePath = intent.getStringExtra(Constants.EDIT_PATH);
        int i = intent.getIntExtra(Constants.EDIT_WIDGET, 0);
        if (i != 0) Reminder.disableReminder(id, this);

        spinner.setSelection(sPrefs.loadInt(Prefs.LAST_USED_REMINDER));

        if (id != 0){
            item = remControl.getItem(id);
            readReminder();
        } else if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                item = new JParser(SyncHelper.readFile(filePath)).parse();
                uuId = item.getUuId();
                readReminder();
            } else {
                Messages.toast(this, getString(R.string.something_went_wrong));
                finish();
            }
        }

        clearViews();
    }

    private void selectVolume() {
        Intent i = new Intent(ReminderManager.this, SelectVolume.class).putExtra("int", 1);
        startActivityForResult(i, Constants.REQUEST_CODE_VOLUME);
    }

    private void readReminder() {
        if (item != null) {
            type = item.getType();
            vibration = item.getVibrate();
            voice = item.getVoice();
            notificationRepeat = item.getNotificationRepeat();
            wake = item.getAwake();
            unlock = item.getUnlock();

            radius = item.getPlace().getRadius();
            ledColor = item.getLed().getColor();
            auto = item.getAction().getAuto();
            melody = item.getMelody().getMelodyPath();
            repeats = item.getRecurrence().getLimit();
            String catId = item.getCategory();

            if (radius == 0) radius = -1;

            if (catId != null && !catId.matches("")) categoryId = catId;

            DataBase db = new DataBase(this);
            db.open();
            if (categoryId != null && !categoryId.matches("")) {
                Cursor c = db.getCategory(categoryId);
                if (c != null && c.moveToFirst()) {
                    String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                    category.setText(title);
                }
                if (c != null) c.close();
            }
            db.close();
        } else {
            Messages.toast(this, getString(R.string.something_went_wrong));
            finish();
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
        } else if (type.matches(Constants.TYPE_MAIL)){
            spinner.setSelection(11);
        } else if (type.matches(Constants.TYPE_PLACES) && Module.isPro()){
            spinner.setSelection(12);
        } else {
            spinner.setSelection(0);
        }
    }

    private void showLimit() {
        ViewUtils.fadeInAnimation(repeatFrame);
        handler.postDelayed(seek, 3000);
    }

    /**
     * Select contact button click listener.
     */
    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Permissions.checkPermission(ReminderManager.this, Permissions.READ_CONTACTS)) {
                SuperUtil.selectContact(ReminderManager.this, Constants.REQUEST_CODE_CONTACTS);
            } else {
                Permissions.requestPermission(ReminderManager.this, 107, Permissions.READ_CONTACTS);
            }
        }
    };

    /**
     * Runnable for hiding repeat limit seekbar.
     */
    private Runnable seek = new Runnable() {
        @Override
        public void run() {
            if (repeatFrame.getVisibility() == View.VISIBLE) {
                ViewUtils.fadeOutAnimation(repeatFrame);
                repeats = -1;
            }
        }
    };

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
        findViewById(R.id.mailLayout).setVisibility(View.GONE);
        findViewById(R.id.placesLayout).setVisibility(View.GONE);

        map = new MapFragment();
        map.setListener(this);
        map.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));
        map.setMarkerStyle(sPrefs.loadInt(Prefs.MARKER_STYLE));

        mapOut = new MapFragment();
        mapOut.setListener(this);
        mapOut.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));
        mapOut.setMarkerStyle(sPrefs.loadInt(Prefs.MARKER_STYLE));

        placesMap = new PlacesMap();
        placesMap.setListener(this);
        placesMap.setRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));
        placesMap.setMarkerStyle(sPrefs.loadInt(Prefs.MARKER_STYLE));

        addFragment(R.id.map, map);
        addFragment(R.id.mapOut, mapOut);
        addFragment(R.id.mapPlace, placesMap);
    }

    private void addFragment(int res, Fragment fragment) {
        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.add(res, fragment);
        fragTransaction.commitAllowingStateLoss();
    }

    /**
     * Set selecting reminder type spinner adapter.
     */
    private void setUpNavigation() {
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        isDark = sPrefs.loadBoolean(Prefs.USE_DARK_THEME);
        if (isDark) {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call), R.drawable.ic_call_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.sms), R.drawable.ic_textsms_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_place_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.skype), R.drawable.skype_icon_white));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application), R.drawable.ic_launch_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_shopping_cart_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email_white_24dp));
            if (Module.isPro()) navSpinner.add(new SpinnerItem(getString(R.string.places), R.drawable.ic_near_me_white_24dp));
        } else {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_event_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call), R.drawable.ic_call_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.sms), R.drawable.ic_textsms_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_place_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.skype), R.drawable.skype_icon));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application), R.drawable.ic_launch_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_event_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_shopping_cart_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email_black_24dp));
            if (Module.isPro()) navSpinner.add(new SpinnerItem(getString(R.string.places), R.drawable.ic_near_me_black_24dp));
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * Delete or move to trash reminder.
     */
    private void deleteReminder() {
        NextBase db = new NextBase(this);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            int isArchived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
            if (isArchived == 1) {
                Reminder.delete(id, this);
                Messages.toast(ReminderManager.this, getString(R.string.deleted));
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
                if (mFab.getVisibility() == View.GONE)
                    mFab.show();
                else restoreTask();
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
        if (spinner.getSelectedItemPosition() == 11 && type.matches(Constants.TYPE_MAIL)) is = true;
        if (spinner.getSelectedItemPosition() == 12 && type.matches(Constants.TYPE_PLACES)) is = true;
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
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        ViewUtils.fadeInAnimation(by_date_layout);

        DateType dateType = new DateType(this, Constants.TYPE_REMINDER);
        dateType.inflateView(R.id.by_date_layout);
        remControl = dateType;

        DateTimeView dateView = (DateTimeView) findViewById(R.id.dateView);
        dateView.setListener(this);
        dateView.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        dateExport = (CheckBox) findViewById(R.id.dateExport);
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)))
            dateExport.setVisibility(View.VISIBLE);

        dateTaskExport = (CheckBox) findViewById(R.id.dateTaskExport);
        if (gtx.isLinked()) dateTaskExport.setVisibility(View.VISIBLE);

        RepeatView repeatView = (RepeatView) findViewById(R.id.repeatView);
        repeatView.setListener(this);
        repeatView.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (item != null && isSame()) {
            String text = item.getSummary();
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();

            if (exp == 1) dateExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                dateTaskExport.setChecked(true);

            taskField.setText(text);
            dateView.setDateTime(updateCalendar(eventTime, true));
            repeatView.setProgress(repeatCode);
        }
    }

    private Date updateTime(long millis, boolean deny) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        if (myYear > 0 && !deny) cal.set(myYear, myMonth, myDay, myHour, myMinute);
        else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }
        return cal.getTime();
    }

    private long updateCalendar(long millis, boolean deny) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        if (myYear > 0 && !deny) cal.set(myYear, myMonth, myDay, myHour, myMinute);
        else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }
        return cal.getTimeInMillis();
    }

    /**
     * Show by day of month reminder creation layout.
     */
    private void attachMonthDay(){
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        ViewUtils.fadeInAnimation(monthDayLayout);

        DateType dateType = new DateType(this, Constants.TYPE_MONTHDAY);
        dateType.inflateView(R.id.monthDayLayout);
        remControl = dateType;

        monthDayField = (TextView) findViewById(R.id.monthDayField);
        monthDayField.setOnClickListener(dateClick);

        monthDayExport = (CheckBox) findViewById(R.id.monthDayExport);
        if (isCalendar || isStock)
            monthDayExport.setVisibility(View.VISIBLE);

        monthDayTaskExport = (CheckBox) findViewById(R.id.monthDayTaskExport);
        if (gtx.isLinked()) monthDayTaskExport.setVisibility(View.VISIBLE);

        monthDayTimeField = (TextView) findViewById(R.id.monthDayTimeField);
        monthDayTimeField.setOnClickListener(timeClick);
        monthDayTimeField.setText(TimeUtil.getTime(updateTime(System.currentTimeMillis(), false),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        monthDayTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        String dayStr;
        if (myDay > 28) myDay = 28;
        if (myDay < 10) dayStr = "0" + myDay;
        else dayStr = String.valueOf(myDay);

        monthDayField.setText(dayStr);
        monthDayField.setTypeface(AssetsUtil.getMediumTypeface(this));

        dayCheck = (RadioButton) findViewById(R.id.dayCheck);
        dayCheck.setChecked(true);
        lastCheck = (RadioButton) findViewById(R.id.lastCheck);
        dayCheck.setOnCheckedChangeListener(this);
        lastCheck.setOnCheckedChangeListener(this);

        actionViewMonth = (ActionView) findViewById(R.id.actionViewMonth);
        actionViewMonth.setListener(this);
        actionViewMonth.setActivity(this);

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();

            eventTime = item.getStartTime();
            repeatCode = item.getRecurrence().getRepeat();

            if (exp == 1) monthDayExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                monthDayTaskExport.setChecked(true);

            taskField.setText(text);
            monthDayTimeField.setText(TimeUtil.getTime(updateTime(eventTime, true),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));

            if (myDay == 0) myDay = 1;
            if (myDay < 10) dayStr = "0" + myDay;
            else dayStr = String.valueOf(myDay);
            monthDayField.setText(dayStr);

            if (type.matches(Constants.TYPE_MONTHDAY)){
                actionViewMonth.setAction(false);
                dayCheck.setChecked(true);
            } else if (type.matches(Constants.TYPE_MONTHDAY_LAST)) {
                actionViewMonth.setAction(false);
                lastCheck.setChecked(true);
            } else {
                actionViewMonth.setAction(true);
                actionViewMonth.setNumber(number);
                if (type.matches(Constants.TYPE_MONTHDAY_CALL_LAST) ||
                        type.matches(Constants.TYPE_MONTHDAY_MESSAGE_LAST)){
                    lastCheck.setChecked(true);
                } else {
                    dayCheck.setChecked(true);
                }
                if (type.matches(Constants.TYPE_MONTHDAY_CALL)){
                    actionViewMonth.setType(ActionView.TYPE_CALL);
                } else {
                    actionViewMonth.setType(ActionView.TYPE_MESSAGE);
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
                    mLocList = new CurrentLocation();
                    updateListener();
                }
                break;
            case R.id.mapCheck:
                if (mapCheck.isChecked()) {
                    currentCheck.setChecked(false);
                    toggleMap();
                    removeUpdates();
                }
                break;
        }
    }

    private void removeUpdates() {
        if (mLocList != null) {
            if (Permissions.checkPermission(ReminderManager.this,
                    Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager.removeUpdates(mLocList);
            } else {
                Permissions.requestPermission(ReminderManager.this, 201,
                        Permissions.ACCESS_FINE_LOCATION,
                        Permissions.ACCESS_COARSE_LOCATION);
            }
        }
    }

    /**
     * Show alarm clock reminder type creation layout.
     */
    private void attachWeekDayReminder(){
        taskField.setHint(getString(R.string.remind_me));

        cSetter = new ColorSetter(ReminderManager.this);

        LinearLayout weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        ViewUtils.fadeInAnimation(weekday_layout);

        DateType dateType = new DateType(this, Constants.TYPE_WEEKDAY);
        dateType.inflateView(R.id.weekday_layout);
        remControl = dateType;

        weekExport = (CheckBox) findViewById(R.id.weekExport);
        if (isCalendar || isStock)
            weekExport.setVisibility(View.VISIBLE);

        weekTaskExport = (CheckBox) findViewById(R.id.weekTaskExport);
        if (gtx.isLinked()) weekTaskExport.setVisibility(View.VISIBLE);

        weekTimeField = (TextView) findViewById(R.id.weekTimeField);
        weekTimeField.setOnClickListener(timeClick);
        weekTimeField.setText(TimeUtil.getTime(updateTime(System.currentTimeMillis(), false),
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

        actionViewWeek = (ActionView) findViewById(R.id.actionViewWeek);
        actionViewWeek.setListener(this);
        actionViewWeek.setActivity(this);

        if (item != null && isSame()) {
            String text = item.getSummary();
            ArrayList<Integer> weekdays = item.getRecurrence().getWeekdays();
            String number = item.getAction().getTarget();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            eventTime = item.getEventTime();

            if (exp == 1) weekExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                weekTaskExport.setChecked(true);

            weekTimeField.setText(TimeUtil.getTime(updateTime(eventTime, true),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            taskField.setText(text);

            setCheckForDays(weekdays);

            if (type.matches(Constants.TYPE_WEEKDAY))
                actionViewWeek.setAction(false);
            else {
                actionViewWeek.setAction(true);
                actionViewWeek.setNumber(number);
                if (type.matches(Constants.TYPE_WEEKDAY_CALL))
                    actionViewWeek.setType(ActionView.TYPE_CALL);
                else actionViewWeek.setType(ActionView.TYPE_MESSAGE);
            }
        }
    }

    /**
     * Check days toggle buttons depends on weekday string.
     * @param weekdays weekday string.
     */
    private void setCheckForDays(ArrayList<Integer> weekdays){
        if (weekdays.get(0) == Constants.DAY_CHECKED)
            sundayCheck.setChecked(true);
        else sundayCheck.setChecked(false);

        if (weekdays.get(1) == Constants.DAY_CHECKED)
            mondayCheck.setChecked(true);
        else mondayCheck.setChecked(false);

        if (weekdays.get(2) == Constants.DAY_CHECKED)
            tuesdayCheck.setChecked(true);
        else tuesdayCheck.setChecked(false);

        if (weekdays.get(3) == Constants.DAY_CHECKED)
            wednesdayCheck.setChecked(true);
        else wednesdayCheck.setChecked(false);

        if (weekdays.get(4) == Constants.DAY_CHECKED)
            thursdayCheck.setChecked(true);
        else thursdayCheck.setChecked(false);

        if (weekdays.get(5) == Constants.DAY_CHECKED)
            fridayCheck.setChecked(true);
        else fridayCheck.setChecked(false);

        if (weekdays.get(6) == Constants.DAY_CHECKED)
            saturdayCheck.setChecked(true);
        else saturdayCheck.setChecked(false);
    }

    /**
     * Show timer reminder type creation layout.
     */
    private void attachTimeReminder(){
        taskField.setHint(getString(R.string.remind_me));

        cSetter = new ColorSetter(ReminderManager.this);
        LinearLayout after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        ViewUtils.fadeInAnimation(after_time_layout);

        DateType dateType = new DateType(this, Constants.TYPE_TIME);
        dateType.inflateView(R.id.after_time_layout);
        remControl = dateType;

        timeExport = (CheckBox) findViewById(R.id.timeExport);
        if (isCalendar || isStock)
            timeExport.setVisibility(View.VISIBLE);

        timeTaskExport = (CheckBox) findViewById(R.id.timeTaskExport);
        if (gtx.isLinked()) timeTaskExport.setVisibility(View.VISIBLE);

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
                    selectExclusion.setText(getString(R.string.exclusion));
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

        RepeatView repeatViewTime = (RepeatView) findViewById(R.id.repeatViewTime);
        repeatViewTime.setListener(this);
        repeatViewTime.setMax(120);

        if (item != null && isSame()) {
            String text = item.getSummary();
            JExport jExport = item.getExport();
            int  exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            JRecurrence jRecurrence = item.getRecurrence();
            long repeat = jRecurrence.getRepeat();
            long afterTime = jRecurrence.getAfter();
            exclusion = item.getExclusion().toString();

            timeString = TimeUtil.generateAfterString(afterTime);
            updateTimeView();
            setExclusion(exclusion);

            if (exp == 1) timeExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                timeTaskExport.setChecked(true);

            repeatViewTime.setProgress(repeat);
            taskField.setText(text);
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
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout skype_layout = (LinearLayout) findViewById(R.id.skype_layout);
        ViewUtils.fadeInAnimation(skype_layout);

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
                if (b) taskField.setHint(getString(R.string.message));
                else taskField.setHint(getString(R.string.remind_me));
            }
        });

        skypeExport = (CheckBox) findViewById(R.id.skypeExport);
        if (isCalendar || isStock)
            skypeExport.setVisibility(View.VISIBLE);

        skypeTaskExport = (CheckBox) findViewById(R.id.skypeTaskExport);
        if (gtx.isLinked()) skypeTaskExport.setVisibility(View.VISIBLE);

        DateTimeView dateViewSkype = (DateTimeView) findViewById(R.id.dateViewSkype);
        dateViewSkype.setListener(this);
        dateViewSkype.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        RepeatView repeatViewSkype = (RepeatView) findViewById(R.id.repeatViewSkype);
        repeatViewSkype.setListener(this);
        repeatViewSkype.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            repeatCode = item.getRecurrence().getRepeat();
            eventTime = item.getStartTime();

            if (exp == 1) skypeExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                skypeTaskExport.setChecked(true);

            if(type.matches(Constants.TYPE_SKYPE)) skypeCall.setChecked(true);
            if(type.matches(Constants.TYPE_SKYPE_VIDEO)) skypeVideo.setChecked(true);
            if(type.matches(Constants.TYPE_SKYPE_CHAT)) skypeChat.setChecked(true);

            taskField.setText(text);
            skypeUser.setText(number);
            dateViewSkype.setDateTime(updateCalendar(eventTime, true));
            repeatViewSkype.setProgress(repeatCode);
        }
    }

    /**
     * Show application reminder type creation layout.
     */
    private void attachApplication(){
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout application_layout = (LinearLayout) findViewById(R.id.application_layout);
        ViewUtils.fadeInAnimation(application_layout);

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
        if (isDark) pickApplication.setImageResource(R.drawable.ic_launch_white_24dp);
        else pickApplication.setImageResource(R.drawable.ic_launch_black_24dp);

        application = (RadioButton) findViewById(R.id.application);
        application.setChecked(true);
        browser = (RadioButton) findViewById(R.id.browser);
        application.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    ViewUtils.collapse(applicationLayout);
                    ViewUtils.expand(browseLink);
                } else {
                    ViewUtils.collapse(browseLink);
                    ViewUtils.expand(applicationLayout);
                }
            }
        });

        appExport = (CheckBox) findViewById(R.id.appExport);
        if (isCalendar || isStock)
            appExport.setVisibility(View.VISIBLE);

        appTaskExport = (CheckBox) findViewById(R.id.appTaskExport);
        if (gtx.isLinked()) appTaskExport.setVisibility(View.VISIBLE);

        DateTimeView dateViewApp = (DateTimeView) findViewById(R.id.dateViewApp);
        dateViewApp.setListener(this);
        dateViewApp.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        RepeatView repeatViewApp = (RepeatView) findViewById(R.id.repeatViewApp);
        repeatViewApp.setListener(this);
        repeatViewApp.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            repeatCode = item.getRecurrence().getRepeat();
            eventTime = item.getStartTime();

            if (exp == 1) appExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                appTaskExport.setChecked(true);

            if (type.matches(Constants.TYPE_APPLICATION)) {
                application.setChecked(true);
                selectedPackage = number;
                PackageManager packageManager = getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                    ignored.printStackTrace();
                }

                final String name = (String)((applicationInfo != null) ?
                        packageManager.getApplicationLabel(applicationInfo) : "???");
                applicationName.setText(name);

            }
            if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                browser.setChecked(true);
                browseLink.setText(number);
            }

            taskField.setText(text);

            dateViewApp.setDateTime(updateCalendar(eventTime, true));
            repeatViewApp.setProgress(repeatCode);
        }
    }

    /**
     * Show call reminder type creation layout.
     */
    private void attachCall(){
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout call_layout = (LinearLayout) findViewById(R.id.call_layout);
        ViewUtils.fadeInAnimation(call_layout);

        DateType dateType = new DateType(this, Constants.TYPE_CALL);
        dateType.inflateView(R.id.call_layout);
        remControl = dateType;

        ImageButton addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addNumberButton, isDark);

        phoneNumber = (FloatingEditText) findViewById(R.id.phoneNumber);

        callExport = (CheckBox) findViewById(R.id.callExport);
        if (isCalendar || isStock)
            callExport.setVisibility(View.VISIBLE);

        callTaskExport = (CheckBox) findViewById(R.id.callTaskExport);
        if (gtx.isLinked()) callTaskExport.setVisibility(View.VISIBLE);

        DateTimeView dateViewCall = (DateTimeView) findViewById(R.id.dateViewCall);
        dateViewCall.setListener(this);
        dateViewCall.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        RepeatView repeatViewCall = (RepeatView) findViewById(R.id.repeatViewCall);
        repeatViewCall.setListener(this);
        repeatViewCall.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            repeatCode = item.getRecurrence().getRepeat();
            eventTime = item.getStartTime();

            if (exp == 1) callExport.setChecked(true);
            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                callTaskExport.setChecked(true);

            taskField.setText(text);
            phoneNumber.setText(number);
            dateViewCall.setDateTime(updateCalendar(eventTime, true));
            repeatViewCall.setProgress(repeatCode);
        }
    }

    /**
     * Show message reminder type creation layout.
     */
    private void attachMessage(){
        taskField.setHint(getString(R.string.message));

        LinearLayout message_layout = (LinearLayout) findViewById(R.id.message_layout);
        ViewUtils.fadeInAnimation(message_layout);

        DateType dateType = new DateType(this, Constants.TYPE_MESSAGE);
        dateType.inflateView(R.id.message_layout);
        remControl = dateType;

        ImageButton addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addMessageNumberButton, isDark);

        messageNumber = (FloatingEditText) findViewById(R.id.messageNumber);

        messageExport = (CheckBox) findViewById(R.id.messageExport);
        if (isCalendar || isStock)
            messageExport.setVisibility(View.VISIBLE);

        messageTaskExport = (CheckBox) findViewById(R.id.messageTaskExport);
        if (gtx.isLinked()) messageTaskExport.setVisibility(View.VISIBLE);

        DateTimeView dateViewMessage = (DateTimeView) findViewById(R.id.dateViewMessage);
        dateViewMessage.setListener(this);
        dateViewMessage.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        RepeatView repeatViewMessage = (RepeatView) findViewById(R.id.repeatViewMessage);
        repeatViewMessage.setListener(this);
        repeatViewMessage.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            repeatCode = item.getRecurrence().getRepeat();
            eventTime = item.getStartTime();

            if (exp == 1) messageExport.setChecked(true);

            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                messageTaskExport.setChecked(true);

            taskField.setText(text);
            messageNumber.setText(number);
            dateViewMessage.setDateTime(updateCalendar(eventTime, true));
            repeatViewMessage.setProgress(repeatCode);
        }
    }

    /**
     * Show mail reminder type creation layout.
     */
    private void attachMail(){
        taskField.setHint(getString(R.string.subject));

        LinearLayout mailLayout = (LinearLayout) findViewById(R.id.mailLayout);
        ViewUtils.fadeInAnimation(mailLayout);

        DateType dateType = new DateType(this, Constants.TYPE_MAIL);
        dateType.inflateView(R.id.mailLayout);
        remControl = dateType;

        ImageButton chooseFile = (ImageButton) findViewById(R.id.chooseFile);
        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ReminderManager.this, FileExplore.class)
                        .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST);
            }
        });
        if (isDark) chooseFile.setImageResource(R.drawable.ic_attach_file_white_24dp);
        else chooseFile.setImageResource(R.drawable.ic_attach_file_black_24dp);

        fileName = (TextView) findViewById(R.id.fileName);
        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachment = null;
                showAttachment();
            }
        });

        mail = (EditText) findViewById(R.id.mail);
        subject = (EditText) findViewById(R.id.subject);

        mailExport = (CheckBox) findViewById(R.id.mailExport);
        if (isCalendar || isStock)
            mailExport.setVisibility(View.VISIBLE);

        mailTaskExport = (CheckBox) findViewById(R.id.mailTaskExport);
        if (gtx.isLinked()) mailTaskExport.setVisibility(View.VISIBLE);

        DateTimeView dateViewMail = (DateTimeView) findViewById(R.id.dateViewMail);
        dateViewMail.setListener(this);
        dateViewMail.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        RepeatView repeatViewMail = (RepeatView) findViewById(R.id.repeatViewMail);
        repeatViewMail.setListener(this);
        repeatViewMail.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (item != null && isSame()) {
            String text = item.getSummary();
            JAction jAction = item.getAction();
            String mailContact = jAction.getTarget();
            String subjectT = jAction.getSubject();
            attachment = jAction.getAttachment();

            JExport jExport = item.getExport();
            int exp = jExport.getCalendar();
            int expTasks = jExport.getgTasks();
            repeatCode = item.getRecurrence().getRepeat();
            eventTime = item.getStartTime();

            if (exp == 1) messageExport.setChecked(true);

            if (expTasks == Constants.SYNC_GTASKS_ONLY)
                messageTaskExport.setChecked(true);

            showAttachment();

            taskField.setText(text);
            mail.setText(mailContact);
            subject.setText(subjectT);
            dateViewMail.setDateTime(updateCalendar(eventTime, true));
            repeatViewMail.setProgress(repeatCode);
        }
    }

    private void showAttachment() {
        if (attachment != null) {
            File file = new File(attachment);
            fileName.setText(file.getName());
        } else fileName.setText(getString(R.string.no_files_attached));
    }

    @Override
    public void placeChanged(LatLng place) {
        curPlace = place;
        if (isLocationOutAttached())
            mapLocation.setText(LocationUtil.getAddress(place.latitude, place.longitude));
    }

    @Override
    public void onBackClick() {
        if (isLocationAttached()) {
            if (map.isFullscreen()) {
                map.setFullscreen(false);
                ViewUtils.expand(toolbar);
            }
            ViewUtils.fadeOutAnimation(mapContainer);
            ViewUtils.fadeInAnimation(specsContainer);
        }
        if (isLocationOutAttached()) {
            if (mapOut.isFullscreen()) {
                mapOut.setFullscreen(false);
                ViewUtils.collapse(toolbar);
            }
            ViewUtils.fadeOutAnimation(mapContainerOut);
            ViewUtils.fadeInAnimation(specsContainerOut);
        }
    }

    @Override
    public void onZoomClick(boolean isFull) {
        if (isFull) {
            ViewUtils.collapse(toolbar);
        } else {
            ViewUtils.expand(toolbar);
        }
    }

    @Override
    public void placeName(String name) {

    }

    /**
     * Show location reminder type creation layout.
     */
    private void attachLocation() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        ViewUtils.fadeInAnimation(geolocationlayout);

        LocationType dateType = new LocationType(this, Constants.TYPE_LOCATION);
        dateType.inflateView(R.id.geolocationlayout);
        remControl = dateType;

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
        specsContainer = (ScrollView) findViewById(R.id.specsContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);

        attackDelay = (CheckBox) findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) delayLayout.setVisibility(View.VISIBLE);
                else delayLayout.setVisibility(View.GONE);
            }
        });

        if (attackDelay.isChecked()) ViewUtils.expand(delayLayout);

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
                toggleMap();
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
                if (task != null && !task.isCancelled())
                    task.cancel(true);
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
                if (title.matches(""))
                    title = pos.toString();
                if (map != null)
                    map.addMarker(pos, title, true, true, radius);
            }
        });

        actionViewLocation = (ActionView) findViewById(R.id.actionViewLocation);
        actionViewLocation.setListener(this);
        actionViewLocation.setActivity(this);

        DateTimeView dateViewLocation = (DateTimeView) findViewById(R.id.dateViewLocation);
        dateViewLocation.setListener(this);
        dateViewLocation.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        if (curPlace != null) {
            if (map != null) {
                map.addMarker(curPlace, null, true, true, radius);
                toggleMap();
            }
        }

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JPlace jPlace = item.getPlace();
            double latitude = jPlace.getLatitude();
            double longitude = jPlace.getLongitude();
            eventTime = item.getStartTime();
            radius = jPlace.getRadius();

            if (item != null && eventTime > 0) {
                dateViewLocation.setDateTime(updateCalendar(eventTime, true));
                attackDelay.setChecked(true);
                isDelayed = true;
            } else attackDelay.setChecked(false);

            if (type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_MESSAGE)){
                actionViewLocation.setAction(true);
                actionViewLocation.setNumber(number);
                if (type.matches(Constants.TYPE_LOCATION_CALL))
                    actionViewLocation.setType(ActionView.TYPE_CALL);
                else actionViewLocation.setType(ActionView.TYPE_MESSAGE);
            } else {
                actionViewLocation.setAction(false);
            }

            taskField.setText(text);
            if (map != null) {
                map.addMarker(new LatLng(latitude, longitude), text, true, false, radius);
                toggleMap();
            }
        }
    }

    private boolean isMapVisible() {
        if (isLocationAttached()) {
            return mapContainer != null && mapContainer.getVisibility() == View.VISIBLE;
        }
        return isLocationOutAttached() && mapContainerOut != null &&
                mapContainerOut.getVisibility() == View.VISIBLE;
    }

    private void toggleMap() {
        if (isLocationAttached()) {
            if (isMapVisible()) {
                ViewUtils.fadeOutAnimation(mapContainer);
                ViewUtils.fadeInAnimation(specsContainer);
            } else {
                ViewUtils.fadeOutAnimation(specsContainer);
                ViewUtils.fadeInAnimation(mapContainer);
                if (map != null) {
                    map.showShowcase();
                }
            }
        }
        if (isLocationOutAttached()) {
            if (isMapVisible()) {
                ViewUtils.fadeOutAnimation(mapContainerOut);
                ViewUtils.fadeInAnimation(specsContainerOut);
            } else {
                ViewUtils.fadeOutAnimation(specsContainerOut);
                ViewUtils.fadeInAnimation(mapContainerOut);
                if (mapOut != null) {
                    mapOut.showShowcase();
                }
            }
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
    private void attachLocationOut() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        ViewUtils.fadeInAnimation(locationOutLayout);

        LocationType dateType = new LocationType(this, Constants.TYPE_LOCATION_OUT);
        dateType.inflateView(R.id.locationOutLayout);
        remControl = dateType;

        /*
      LocationOut reminder type variables.
     */
        LinearLayout delayLayoutOut = (LinearLayout) findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) findViewById(R.id.specsContainerOut);
        mapContainerOut = (RelativeLayout) findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        attachDelayOut = (CheckBox) findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) attachDelayOut.setVisibility(View.VISIBLE);
                else attachDelayOut.setVisibility(View.GONE);
            }
        });

        if (attachDelayOut.isChecked()) ViewUtils.expand(delayLayoutOut);

        ImageButton mapButtonOut = (ImageButton) findViewById(R.id.mapButtonOut);
        if (isDark)
            mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);
        else
            mapButtonOut.setImageResource(R.drawable.ic_map_black_24dp);

        mapButtonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapCheck.isChecked()) {
                    toggleMap();
                }
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

        pointRadius = (SeekBar) findViewById(R.id.pointRadius);
        pointRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusMark.setText(String.format(getString(R.string.radius_x_meters), progress));
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

        actionViewLocationOut = (ActionView) findViewById(R.id.actionViewLocationOut);
        actionViewLocationOut.setListener(this);
        actionViewLocationOut.setActivity(this);

        DateTimeView dateViewLocationOut = (DateTimeView) findViewById(R.id.dateViewLocationOut);
        dateViewLocationOut.setListener(this);
        dateViewLocationOut.setDateTime(updateCalendar(System.currentTimeMillis(), false));

        if (curPlace != null) {
            if (mapOut != null)
                mapOut.addMarker(curPlace, null, true, true, radius);
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (item != null && isSame()) {
            String text = item.getSummary();
            String number = item.getAction().getTarget();
            JPlace jPlace = item.getPlace();
            double latitude = jPlace.getLatitude();
            double longitude = jPlace.getLongitude();
            eventTime = item.getStartTime();
            radius = jPlace.getRadius();

            if (item != null && eventTime > 0) {
                dateViewLocationOut.setDateTime(updateCalendar(eventTime, true));
                attachDelayOut.setChecked(true);
                isDelayed = true;
            } else attachDelayOut.setChecked(false);

            if (type.matches(Constants.TYPE_LOCATION_OUT_CALL) || type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                actionViewLocationOut.setAction(true);
                actionViewLocationOut.setNumber(number);
                if (type.matches(Constants.TYPE_LOCATION_OUT_CALL))
                    actionViewLocationOut.setType(ActionView.TYPE_CALL);
                else
                    actionViewLocationOut.setType(ActionView.TYPE_MESSAGE);
            } else actionViewLocationOut.setAction(false);

            taskField.setText(text);
            LatLng pos = new LatLng(latitude, longitude);
            if (mapOut != null)
                mapOut.addMarker(pos, text, true, true, radius);

            mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
            mapCheck.setChecked(true);
        }
    }

    /**
     * Show places reminder type creation layout.
     */
    private void attachPLaces() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout placesLayout = (LinearLayout) findViewById(R.id.placesLayout);
        ViewUtils.fadeInAnimation(placesLayout);

        LocationType dateType = new LocationType(this, Constants.TYPE_PLACES);
        dateType.inflateView(R.id.placesLayout);
        remControl = dateType;

        if (item != null && isSame()) {
            String text = item.getSummary();
            ArrayList<JPlace> list = item.getPlaces();
            eventTime = item.getStartTime();

            taskField.setText(text);
            if (placesMap != null)
                placesMap.addMarkers(list);
        }
        if (placesMap != null) placesMap.showShowcase();
    }

    /**
     * Show shopping list reminder type creation layout.
     */
    private void attachShoppingList(){
        taskField.setHint(R.string.title);

        RelativeLayout shoppingLayout = (RelativeLayout) findViewById(R.id.shoppingLayout);
        ViewUtils.fadeInAnimation(shoppingLayout);

        DateType dateType = new DateType(this, Constants.TYPE_SHOPPING_LIST);
        dateType.inflateView(R.id.shoppingLayout);
        remControl = dateType;

        todoList = (RecyclerView) findViewById(R.id.todoList);
        todoList.setLayoutManager(new LinearLayoutManager(this));
        CardView cardContainer = (CardView) findViewById(R.id.cardContainer);
        cardContainer.setCardBackgroundColor(cSetter.getCardStyle());

        shoppingTimeContainer = (RelativeLayout) findViewById(R.id.shoppingTimeContainer);

        dateViewShopping = (DateTimeView) findViewById(R.id.dateViewShopping);
        dateViewShopping.setListener(this);

        ImageView shopTimeIcon = (ImageView) findViewById(R.id.shopTimeIcon);
        shopTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingTimeContainer.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingTimeContainer);
                }
                ViewUtils.show(shoppingNoTime);
                myYear = 0;
                myMonth = 0;
                myDay = 0;
                myHour = 0;
                myMinute = 0;
                isShoppingReminder = false;
            }
        });
        if (isDark)
            shopTimeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
        else
            shopTimeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);

        shoppingNoTime  = (TextView) findViewById(R.id.shoppingNoTime);
        shoppingNoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingNoTime.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingNoTime);
                }
                ViewUtils.show(shoppingTimeContainer);
                dateViewShopping.setDateTime(updateCalendar(System.currentTimeMillis(), false));
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
                        shopEdit.setError(getString(R.string.must_be_not_empty));
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
                    shopEdit.setError(getString(R.string.must_be_not_empty));
                    return;
                }

                shoppingLists.addItem(new ShoppingList(task));
                shoppingAdapter.notifyDataSetChanged();
                shopEdit.setText("");
            }
        });

        shoppingLists = new ShoppingListDataProvider(this);
        loadShoppings();
        if (item != null && isSame()){
            shoppingLists.clear();
            shoppingLists = new ShoppingListDataProvider(item.getShoppings(), true);
            loadShoppings();

            eventTime = item.getStartTime();

            if (eventTime > 0) {
                dateViewShopping.setDateTime(updateCalendar(eventTime, true));
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

            taskField.setText(item.getSummary());
        }
    }

    private void loadShoppings() {
        shoppingAdapter = new TaskListRecyclerAdapter(this, shoppingLists, new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                ShoppingList item = shoppingLists.getItem(position);
                if (item.isChecked() == 1) item.setIsChecked(0);
                else item.setIsChecked(1);
                loadShoppings();
            }

            @Override
            public void onItemDelete(int position) {
                shoppingLists.removeItem(position);
                loadShoppings();
            }

            @Override
            public void onItemChange(int position) {
                ShoppingList item = shoppingLists.getItem(position);
                if (item.getStatus() == 1) item.setStatus(0);
                else item.setStatus(1);
                loadShoppings();
            }
        });
        todoList.setAdapter(shoppingAdapter);
    }

    /**
     * Hide currently attached layout.
     */
    private void detachCurrentView(){
        if (remControl.getView() != 0)
            ViewUtils.fadeOutAnimation(findViewById(remControl.getView()));
    }

    /**
     * Save new or update current reminder.
     */
    private void save() {
        JModel item = getData();
        if (item == null) return;
        if (id != 0) remControl.save(id, item);
        else {
            if (!Reminder.isUuId(this, uuId)) remControl.save(item);
            else {
                Messages.snackbar(mFab, getString(R.string.same_reminder_also_present));
                return;
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
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_TIME);
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
        return remControl.getType() != null &&
                remControl.getType().contains("weekday");
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
     * Check if mail reminder type layout visible.
     * @return Boolean
     */
    private boolean isMailAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_MAIL);
    }

    /**
     * Check if monthDay reminder type layout visible.
     * @return Boolean
     */
    private boolean isMonthDayAttached() {
        return remControl.getType() != null &&
                remControl.getType().contains("month");
    }

    /**
     * Check if shopping list reminder type layout visible.
     * @return Boolean
     */
    private boolean isShoppingAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_SHOPPING_LIST);
    }

    /**
     * Check if places reminder type layout visible.
     * @return Boolean
     */
    private boolean isPlacesAttached() {
        return remControl.getType() != null &&
                remControl.getType().matches(Constants.TYPE_PLACES);
    }

    /**
     * Get reminder type string.
     * @return String
     */
    private String getType(){
        String type;
        if (remControl instanceof LocationType){
            if (remControl.getType().startsWith(Constants.TYPE_LOCATION_OUT)){
                if (actionViewLocationOut.hasAction()){
                    if (actionViewLocationOut.getType() == ActionView.TYPE_CALL)
                        type = Constants.TYPE_LOCATION_OUT_CALL;
                    else type = Constants.TYPE_LOCATION_OUT_MESSAGE;
                } else type = Constants.TYPE_LOCATION_OUT;
            } else if (remControl.getType().startsWith(Constants.TYPE_LOCATION)) {
                if (actionViewLocation.hasAction()){
                    if (actionViewLocation.getType() == ActionView.TYPE_CALL)
                        type = Constants.TYPE_LOCATION_CALL;
                    else type = Constants.TYPE_LOCATION_MESSAGE;
                } else type = Constants.TYPE_LOCATION;
            } else type = remControl.getType();
        } else {
            if (isSkypeAttached()){
                if (skypeCall.isChecked())type = Constants.TYPE_SKYPE;
                else if (skypeVideo.isChecked())type = Constants.TYPE_SKYPE_VIDEO;
                else type = Constants.TYPE_SKYPE_CHAT;
            } else if (isApplicationAttached()){
                if (application.isChecked())type = Constants.TYPE_APPLICATION;
                else type = Constants.TYPE_APPLICATION_BROWSER;
            } else if (isWeekDayReminderAttached()){
                if (actionViewWeek.hasAction()){
                    if (actionViewWeek.getType() == ActionView.TYPE_CALL)
                        type = Constants.TYPE_WEEKDAY_CALL;
                    else type = Constants.TYPE_WEEKDAY_MESSAGE;
                } else type = Constants.TYPE_WEEKDAY;
            } else if (isMonthDayAttached()) {
                if (actionViewMonth.hasAction()){
                    if (actionViewMonth.getType() == ActionView.TYPE_CALL){
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
            } else type = remControl.getType();
        }
        return type;
    }

    /**
     * Create reminder object.
     * @return Reminder object
     */
    private JModel getData() {
        String type = getType();
        if (type != null) {
            ArrayList<JShopping> jShoppings = new ArrayList<>();
            if (isShoppingAttached()){
                if (shoppingLists.getCount() == 0) {
                    Messages.snackbar(mFab, getString(R.string.shopping_list_is_empty));
                    return null;
                } else {
                    for (ShoppingList shoppingList : shoppingLists.getData()) {
                        String title = shoppingList.getTitle();
                        String uuid = shoppingList.getUuId();
                        long time = shoppingList.getTime();
                        int status = shoppingList.getIsChecked();
                        int deleted = shoppingList.getStatus();
                        jShoppings.add(new JShopping(title, status, uuid, time, deleted));
                    }
                }
                if (!isShoppingReminder) {
                    myDay = 0;
                    myMonth = 0;
                    myYear = 0;
                    myHour = 0;
                    myMinute = 0;
                }
            }


            ArrayList<Integer> weekdays = new ArrayList<>();
            if (isWeekDayReminderAttached()) {
                weekdays = IntervalUtil.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(), wednesdayCheck.isChecked(),
                        thursdayCheck.isChecked(), fridayCheck.isChecked(), saturdayCheck.isChecked(), sundayCheck.isChecked());
                if (!IntervalUtil.isWeekday(weekdays)) {
                    Messages.snackbar(mFab, getString(R.string.you_dont_select_any_day));
                    return null;
                }
            }
            String task = taskField.getText().toString().trim();
            if (!type.contains(Constants.TYPE_CALL) && !type.matches(Constants.TYPE_SHOPPING_LIST)
                    && !type.contains(Constants.TYPE_MAIL)) {
                if (task.matches("")) {
                    Messages.snackbar(mFab, "");
                    return null;
                }
            }

            String number = getNumber();
            if (type.contains(Constants.TYPE_MESSAGE) || type.contains(Constants.TYPE_CALL)) {
                if (!checkNumber(number)) return null;
            }

            if (isApplicationAttached()) {
                if (application.isChecked()) {
                    number = selectedPackage;
                    if (number == null) {
                        Messages.snackbar(mFab, getString(R.string.you_dont_select_application));
                        return null;
                    }
                } else if (browser.isChecked()) {
                    number = browseLink.getText().toString().trim();
                    if (number.matches("") || number.matches(".*https?://")) {
                        Messages.snackbar(mFab, getString(R.string.you_dont_insert_link));
                        return null;
                    }
                    if (!number.startsWith("http://") && !number.startsWith("https://"))
                        number = "http://" + number;

                }
            }

            String subjectString = null;
            if (isMailAttached()) {
                String email = mail.getText().toString().trim();
                if (email.matches("") || !email.matches(".*@.*..*")) {
                    Messages.snackbar(mFab, getString(R.string.email_is_incorrect));
                } else number = email;

                String subString = subject.getText().toString().trim();
                if (subString.matches("")) {
                    showSnackbar(getString(R.string.you_dont_insert_any_message));
                    return null;
                }
                subjectString = task;
                task = subString;
            }

            String uuId = SyncHelper.generateID();

            Double latitude = 0.0;
            Double longitude = 0.0;
            int style = -1;
            if (isLocationAttached() || isLocationOutAttached()) {
                if (!LocationUtil.checkLocationEnable(this)) {
                    LocationUtil.showLocationAlert(this, this);
                    return null;
                }
                LatLng dest = null;
                boolean isNull = true;
                if (curPlace != null) {
                    dest = curPlace;
                    isNull = false;
                }
                if (isNull) {
                    Messages.snackbar(mFab, getString(R.string.you_dont_select_place));
                    return null;
                }
                if (isLocationAttached()) {
                    if (!attackDelay.isChecked()) {
                        myDay = 0;
                        myMonth = 0;
                        myYear = 0;
                        myHour = 0;
                        myMinute = 0;
                    }
                    style = map.getMarkerStyle();
                }
                if (isLocationOutAttached()) {
                    if (!attachDelayOut.isChecked()) {
                        myDay = 0;
                        myMonth = 0;
                        myYear = 0;
                        myHour = 0;
                        myMinute = 0;
                    }
                    style = mapOut.getMarkerStyle();
                }

                latitude = dest.latitude;
                longitude = dest.longitude;
            }

            ArrayList<JPlace> places = new ArrayList<>();
            if (isPlacesAttached()) {
                if (!LocationUtil.checkLocationEnable(this)) {
                    LocationUtil.showLocationAlert(this, this);
                    return null;
                }

                places = placesMap.getPlaces();
                if (places == null || places.size() == 0) {
                    Messages.snackbar(mFab, getString(R.string.you_dont_select_place));
                    return null;
                }

                myDay = 0;
                myMonth = 0;
                myYear = 0;
                myHour = 0;
                myMinute = 0;
            }

            int mySeconds = 0;
            long timeAfter = 0;
            if (isTimeReminderAttached()) {
                timeAfter = SuperUtil.getAfterTime(timeString);
                if (timeAfter == 0) {
                    Messages.snackbar(mFab, getString(R.string.you_dont_insert_timer_time));
                    return null;
                }
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                myYear = c.get(Calendar.YEAR);
                myMonth = c.get(Calendar.MONTH);
                myDay = c.get(Calendar.DAY_OF_MONTH);
                myHour = c.get(Calendar.HOUR_OF_DAY);
                myMinute = c.get(Calendar.MINUTE);
                mySeconds = c.get(Calendar.SECOND);
            }

            long repeat = getRepeat();
            int gTaskSync = getSyncCode();

            int calendarSync = getExportCode();
            if (isMonthDayAttached()) {
                if (type.endsWith("_last")) myDay = 0;
            }

            long startTime = new TimeCount(this).generateStartEvent(type, myDay, myMonth,
                    myYear, myHour, myMinute, mySeconds, weekdays, timeAfter);

            if (repeat == 0) repeats = -1;

            JExclusion jExclusion = new JExclusion(exclusion);
            JLed jLed = new JLed(ledColor, ledColor == -1 ? 0 : 1);
            JMelody jMelody = new JMelody(melody, volume);
            JRecurrence jRecurrence = new JRecurrence(myDay, repeat, repeats, weekdays, timeAfter);
            JAction jAction = new JAction(type, number, auto, subjectString, attachment);
            JExport jExport = new JExport(gTaskSync, calendarSync, null);
            JPlace jPlace = new JPlace(latitude, longitude, radius, style);

            Log.d("----RECORD_TIME-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
            Log.d("----EVENT_TIME-----", TimeUtil.getFullDateTime(startTime, true));

            return new JModel(task, type, categoryId, uuId, startTime, startTime, 0, vibration,
                    notificationRepeat, voice, wake, unlock, jExclusion, jLed, jMelody,
                    jRecurrence, jAction, jExport, jPlace, null, places, jShoppings);
        } else return null;
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
            else if (isMailAttached()) return mailExport.isChecked() ? 1 : 0;
            else return 0;
        } else return 0;
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
        } else if (isMailAttached()){
            return ReminderUtils.getSyncCode(mailTaskExport);
        } else return 0;
    }

    /**
     * Get repeat code for reminder .
     * @return Integer
     */
    private long getRepeat() {
        if (isSkypeAttached() || isApplicationAttached() || isDateReminderAttached() ||
                isTimeReminderAttached() || isCallAttached() || isMessageAttached() ||
                isMailAttached()){
            return repeatCode;
        } else {
            return 0;
        }
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
        } else if (isLocationAttached() && actionViewLocation.hasAction()){
            return actionViewLocation.getNumber();
        } else if (isWeekDayReminderAttached() && actionViewWeek.hasAction()) {
            return actionViewWeek.getNumber();
        } else if (isMonthDayAttached() && actionViewMonth.hasAction()) {
            return actionViewMonth.getNumber();
        } else if (isLocationOutAttached() && actionViewLocationOut.hasAction()) {
            return actionViewLocationOut.getNumber();
        } else return null;
    }

    /**
     * Check if number inserted.
     * @return Boolean
     */
    private boolean checkNumber(String number){
        if (number == null || number.matches("")) {
            Messages.snackbar(mFab, getString(R.string.you_dont_insert_number));
            return false;
        } else return true;
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
            case R.id.timeField:
                timeDialog().show();
                break;
        }
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

            if (isMonthDayAttached()){
                String dayStr;
                if (myDay > 28) {
                    myDay = 28;
                    Messages.snackbar(mFab, getString(R.string.max_day_supported));
                }

                if (myDay < 10) dayStr = "0" + myDay;
                else dayStr = String.valueOf(myDay);

                monthDayField.setText(dayStr);
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
            if (isWeekDayReminderAttached()){
                weekTimeField.setText(formattedTime);
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (map != null && !map.onBackPressed()) return;
        if (mapOut != null && !mapOut.onBackPressed()) return;
        if (placesMap != null && !placesMap.onBackPressed()) return;

        if (mFab.getVisibility() == View.GONE){
            mFab.show();
            return;
        }

        restoreTask();
    }

    /**
     * Restore currently edited reminder.
     */
    private void restoreTask(){
        if (id != 0) {
            NextBase db = new NextBase(this);
            db.open();
            new DisableAsync(this).execute();
            Cursor c = db.getReminder(id);
            if (c != null && c.moveToFirst()) {
                String type = c.getString(c.getColumnIndex(NextBase.TYPE));
                int isDone = c.getInt(c.getColumnIndex(NextBase.DB_STATUS));
                int isArchive = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
                if (isDone != 1 && isArchive != 1) {
                    if (type.contains(Constants.TYPE_LOCATION) && isDelayed)
                        new PositionDelayReceiver().setDelay(ReminderManager.this, id);
                    else new AlarmReceiver().enableReminder(ReminderManager.this, id);
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
        if (navContainer.getVisibility() == View.VISIBLE) switchIt(position);
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
        if (navContainer.getVisibility() == View.GONE) ViewUtils.expand(navContainer);
    }

    /**
     * Hide toolbar.
     */
    private void onUpSwipe() {
        if (navContainer.getVisibility() == View.VISIBLE) ViewUtils.collapse(navContainer);
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
            spinner.setSelection(Module.isPro() ? Configs.NUMBER_OF_REMINDERS : Configs.NUMBER_OF_REMINDERS - 1);
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
        int max = Module.isPro() ? Configs.NUMBER_OF_REMINDERS : Configs.NUMBER_OF_REMINDERS - 1;
        if (current == max){
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
                if (Permissions.checkPermission(ReminderManager.this,
                        Permissions.CALL_PHONE,
                        Permissions.SEND_SMS,
                        Permissions.READ_CONTACTS)) {
                    attachWeekDayReminder();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 113,
                            Permissions.CALL_PHONE, Permissions.READ_CONTACTS,
                            Permissions.SEND_SMS);
                }
                break;
            case 1:
                detachCurrentView();
                attachTimeReminder();
                break;
            case 3:
                detachCurrentView();
                if (Permissions.checkPermission(ReminderManager.this, Permissions.CALL_PHONE)) {
                    attachCall();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 109, Permissions.CALL_PHONE);
                }
                break;
            case 4:
                detachCurrentView();
                if (Permissions.checkPermission(ReminderManager.this, Permissions.SEND_SMS)) {
                    attachMessage();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 108, Permissions.SEND_SMS);
                }
                break;
            case 5:
                detachCurrentView();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (Permissions.checkPermission(ReminderManager.this,
                            Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                            Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.READ_CONTACTS)) {
                        attachLocation();
                    } else {
                        Permissions.requestPermission(ReminderManager.this, 105,
                                Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                Permissions.SEND_SMS, Permissions.READ_CONTACTS);
                    }
                } else spinner.setSelection(0);
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
                if (Permissions.checkPermission(ReminderManager.this,
                        Permissions.CALL_PHONE,
                        Permissions.SEND_SMS,
                        Permissions.READ_CONTACTS)) {
                    attachMonthDay();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 114,
                            Permissions.CALL_PHONE, Permissions.READ_CONTACTS,
                            Permissions.SEND_SMS);
                }
                break;
            case 9:
                detachCurrentView();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (Permissions.checkPermission(ReminderManager.this,
                            Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                            Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.READ_CONTACTS)) {
                        attachLocationOut();
                    } else {
                        Permissions.requestPermission(ReminderManager.this, 106,
                                Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                Permissions.SEND_SMS, Permissions.READ_CONTACTS);
                    }
                } else spinner.setSelection(0);
                break;
            case 10:
                detachCurrentView();
                attachShoppingList();
                break;
            case 11:
                detachCurrentView();
                attachMail();
                break;
            case 12:
                detachCurrentView();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (Permissions.checkPermission(ReminderManager.this,
                            Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION)) {
                        attachPLaces();
                    } else {
                        Permissions.requestPermission(ReminderManager.this, 121,
                                Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION);
                    }
                } else spinner.setSelection(0);
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachLocation();
                else spinner.setSelection(0);
                break;
            case 106:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachLocationOut();
                else spinner.setSelection(0);
                break;
            case 107:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    SuperUtil.selectContact(ReminderManager.this, Constants.REQUEST_CODE_CONTACTS);
                } else {
                    Permissions.showInfo(ReminderManager.this, Permissions.READ_CONTACTS);
                }
                break;
            case 108:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachMessage();
                else spinner.setSelection(0);
                break;
            case 109:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachCall();
                else spinner.setSelection(0);
                break;
            case 113:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachWeekDayReminder();
                else spinner.setSelection(0);
                break;
            case 114:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachMonthDay();
                else spinner.setSelection(0);
                break;
            case 121:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    attachPLaces();
                else spinner.setSelection(0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                //Use Data to get string
                String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
                if (isCallAttached()) phoneNumber.setText(number);
                if (isMessageAttached()) messageNumber.setText(number);
                if (isWeekDayReminderAttached() && actionViewWeek.hasAction())
                    actionViewWeek.setNumber(number);

                if (isMonthDayAttached() && actionViewMonth.hasAction())
                    actionViewMonth.setNumber(number);

                if (isLocationAttached() && actionViewLocation.hasAction())
                    actionViewLocation.setNumber(number);

                if (isLocationOutAttached() && actionViewLocationOut.hasAction())
                    actionViewLocationOut.setNumber(number);
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
                    Messages.snackbar(mFab, String.format(getString(R.string.melody_x), musicFile.getName()));
                }
            }
        }

        if (requestCode == FILE_REQUEST) {
            if (resultCode == RESULT_OK){
                attachment = data.getStringExtra(Constants.FILE_PICKED);
                if (attachment != null) {
                    File file = new File(attachment);
                    showAttachment();
                    Messages.snackbar(mFab, String.format(getString(R.string.file_x_attached), file.getName()));
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == RESULT_OK){
                radius = data.getIntExtra(Constants.SELECTED_RADIUS, -1);
                if (radius != -1) {
                    String str = String.format(getString(R.string.radius_x_meters), radius);
                    showSnackbar(str, R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            radius = -1;
                            if (isLocationAttached()) map.recreateMarker(radius);
                            if (isLocationOutAttached()) mapOut.recreateMarker(radius);
                        }
                    });
                    if (isLocationAttached()) {
                        map.recreateMarker(radius);
                    }
                    if (isLocationOutAttached()) {
                        mapOut.recreateMarker(radius);
                        pointRadius.setProgress(radius);
                    }

                    if (isPlacesAttached()) placesMap.recreateMarker(radius);
                }
            }
        }

        if (requestCode == 1111) {
            if (resultCode == RESULT_OK){
                exclusion = data.getStringExtra("excl");
                setExclusion(exclusion);
            }
        }

        if (requestCode == REQUEST_EXTRA) {
            if (resultCode == RESULT_OK){
                int[] array = data.getIntArrayExtra("prefs");
                Log.d(Constants.LOG_TAG, Arrays.toString(array));
                vibration = array[1];
                voice = array[0];
                wake = array[2];
                unlock = array[3];
                notificationRepeat = array[4];
                auto = array[5];
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_COLOR) {
            if (resultCode == RESULT_OK){
                int position = data.getIntExtra(Constants.SELECTED_LED_COLOR, -1);
                String selColor = LED.getTitle(this, position);
                ledColor = LED.getLED(position);

                String str = String.format(getString(R.string.led_color_x), selColor);
                showSnackbar(str, R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ledColor = -1;
                    }
                });
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

        if (requestCode == Constants.REQUEST_CODE_VOLUME) {
            if (resultCode == RESULT_OK){
                volume = data.getIntExtra(Constants.SELECTED_VOLUME, -1);

                String str = String.format(getString(R.string.selected_loudness_x_for_reminder), volume);
                showSnackbar(str, R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        volume = -1;
                    }
                });
            }
        }
    }

    /**
     * Set up exclusion for reminder.
     * @param jsonObject json object string.
     */
    private void setExclusion(String jsonObject){
        if (jsonObject != null) {
            JExclusion recurrence = new JExclusion(jsonObject);
            if (recurrence.getHours() != null) {
                selectExclusion.setText(String.format(getString(R.string.excluded_hours_x), recurrence.getHours().toString()));
                exclusionClear.setVisibility(View.VISIBLE);
            } else {
                String fromHour = recurrence.getFromHour();
                String toHour = recurrence.getToHour();
                if (fromHour != null && toHour != null) {
                    selectExclusion.setText(String.format(getString(R.string.excluded_time_from_x_to_x), fromHour, toHour));
                    exclusionClear.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_menu, menu);
        if (isLocationAttached()){
            menu.getItem(2).setVisible(true);
        }
        if (isLocationAttached() || isLocationOutAttached()
                || isShoppingAttached() || isPlacesAttached()){
            menu.getItem(4).setVisible(false);
        } else {
            menu.getItem(4).setVisible(true);
        }
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (Module.isPro() && sPrefs.loadBoolean(Prefs.LED_STATUS)){
            menu.getItem(3).setVisible(true);
        }
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isLocationAttached()){
            menu.getItem(1).setVisible(true);
        }
        if (isLocationAttached() || isLocationOutAttached()
                || isShoppingAttached() || isPlacesAttached()){
            menu.getItem(4).setVisible(false);
        } else {
            menu.getItem(4).setVisible(true);
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
        showShowcase();
    }

    public void showShowcase() {
        if (!new SharedPrefs(this).loadBoolean(HAS_SHOWCASE)) {
            new SharedPrefs(this).saveBoolean(HAS_SHOWCASE, true);
            ColorSetter coloring = new ColorSetter(this);
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(coloring.getColor(coloring.colorAccent()));
            config.setContentTextColor(coloring.getColor(R.color.whitePrimary));
            config.setDismissTextColor(coloring.getColor(R.color.whitePrimary));

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
            sequence.setConfig(config);

            sequence.addSequenceItem(spinner,
                    getString(R.string.click_to_select_reminder_type),
                    getString(R.string.got_it));

            sequence.addSequenceItem(insertVoice,
                    getString(R.string.to_insert_task_by_voice),
                    getString(R.string.got_it));

            sequence.addSequenceItem(changeExtra,
                    getString(R.string.click_to_customize),
                    getString(R.string.got_it));

            sequence.addSequenceItem(category,
                    getString(R.string.click_to_change_reminder_group),
                    getString(R.string.got_it));
            sequence.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeUpdates();
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
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

    @Override
    public void onDateSelect(long mills, int day, int month, int year) {
        myDay = day;
        myMonth = month;
        myYear = year;
    }

    @Override
    public void onTimeSelect(long mills, int hour, int minute) {
        myHour = hour;
        myMinute = minute;
    }

    @Override
    public void onProgress(int progress) {
        if (isTimeReminderAttached()) repeatCode = progress * TimeCount.MINUTE;
        else repeatCode = progress * TimeCount.DAY;
    }

    @Override
    public void onActionChange(boolean b) {
        if (!b) taskField.setHint(getString(R.string.remind_me));
    }

    @Override
    public void onTypeChange(boolean type) {
        if (type) taskField.setHint(getString(R.string.message));
        else taskField.setHint(getString(R.string.remind_me));
    }

    @Override
    public void showSnackbar(int message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    @Override
    public void showSnackbar(int message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showSnackbar(String message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
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
            updateListener();
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateListener();
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateListener();
        }
    }

    private void updateListener() {
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