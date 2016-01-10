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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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

import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.async.GeocoderTask;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.FilesDataBase;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.dialogs.ExclusionPickerDialog;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.MapListener;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.ShoppingType;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.views.RepeatView;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Backup file edit activity.
 */
public class BackupFileEdit extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, MapListener, GeocoderTask.GeocoderListener,
        Dialogues.OnCategorySelectListener, DateTimeView.OnSelectListener,
        RepeatView.OnRepeatListener, ActionView.OnActionListener {

    /**
     * Date reminder type variables.
     */
    private CheckBox dateTaskExport;

    /**
     * Weekday reminder type variables.
     */
    private TextView weekTimeField;
    private ToggleButton mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck,
            fridayCheck, saturdayCheck, sundayCheck;
    private CheckBox weekTaskExport;
    private ActionView actionViewWeek;

    /**
     * Monthday reminder type variables.
     */
    private CheckBox monthDayTaskExport;
    private TextView monthDayField, monthDayTimeField;
    private RadioButton dayCheck, lastCheck;
    private ActionView actionViewMonth;

    /**
     * Call reminder variables.
     */
    private FloatingEditText phoneNumber;
    private CheckBox callTaskExport;

    /**
     * Message reminder variables.
     */
    private FloatingEditText messageNumber;
    private CheckBox messageTaskExport;

    /**
     * Time reminder variables.
     */
    private CheckBox timeTaskExport;
    private TextView hoursView, minutesView, secondsView, selectExclusion;
    private ImageButton deleteButton, exclusionClear;
    private String timeString = "000000";

    /**
     * Application reminder type variables.
     */
    private CheckBox appTaskExport;
    private EditText browseLink;
    private RadioButton application, browser;
    private TextView applicationName;

    /**
     * Skype reminder type variables.
     */
    private CheckBox skypeTaskExport;
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

    /**
     * LocationOut reminder type variables.
     */
    private LinearLayout delayLayoutOut;
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView mapLocation, radiusMark, currentLocation;
    private CheckBox attachDelayOut;
    private RadioButton currentCheck, mapCheck;
    private MapFragment mapOut;
    private ActionView actionViewLocationOut;

    /**
     * Shopping list reminder type variables.
     */
    private EditText shopEdit;
    private TaskListRecyclerAdapter shoppingAdapter;
    private ShoppingListDataProvider shoppingLists;
    private TextView shoppingNoTime;
    private RelativeLayout shoppingTimeContainer;

    /**
     * General views.
     */
    private Toolbar toolbar;
    private FloatingEditText taskField;
    private TextView category;

    /**
     * Reminder preferences flags.
     */
    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private String uuID = "";
    private int vibration = -1;
    private int voice = -1;
    private int notificationRepeat = -1;
    private int wake = -1;
    private int unlock = -1;
    private int auto = -1;
    private int repeatCode = 0;
    private long limits = -1;
    private long id;
    private String type, selectedPackage = null;
    private String categoryId;
    private String exclusion = null;
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> namesList;
    private LatLng curPlace;
    private boolean isShoppingReminder;

    private DataBase DB = new DataBase(BackupFileEdit.this);
    private FilesDataBase fdb = new FilesDataBase(BackupFileEdit.this);
    private AlarmReceiver alarm = new AlarmReceiver();
    private ColorSetter cSetter = new ColorSetter(BackupFileEdit.this);
    private SharedPrefs sPrefs = new SharedPrefs(BackupFileEdit.this);
    private GTasksHelper gtx = new GTasksHelper(BackupFileEdit.this);
    private LocationManager mLocationManager;
    private LocationListener mLocList;
    private GeocoderTask task;

    private  static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private boolean isCalendar = false, isStock = false, isDark = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(BackupFileEdit.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.create_edit_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                toolbar.startAnimation(slide);
                toolbar.setVisibility(View.VISIBLE);
            }
        }, 500);

        category = (TextView) findViewById(R.id.category);
        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.selectCategory(BackupFileEdit.this, categoryId, BackupFileEdit.this);
            }
        });

        RelativeLayout extraHolder = (RelativeLayout) findViewById(R.id.extraHolder);
        extraHolder.setVisibility(View.GONE);

        FrameLayout repeatFrame = (FrameLayout) findViewById(R.id.repeatFrame);
        repeatFrame.setVisibility(View.GONE);

        TextView frameLabel = (TextView) findViewById(R.id.repeatLabel);
        frameLabel.setVisibility(View.GONE);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        ImageButton insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        insertVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperUtil.startVoiceRecognitionActivity(BackupFileEdit.this, VOICE_RECOGNITION_REQUEST_CODE);
            }
        });

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);

        clearForm();

        if (id != 0){
            fdb.open();
            Cursor c = fdb.getFile(id);
            if (c != null && c.moveToNext()) {
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                vibration = c.getInt(c.getColumnIndex(Constants.COLUMN_VIBRATION));
                voice = c.getInt(c.getColumnIndex(Constants.COLUMN_VOICE));
                wake = c.getInt(c.getColumnIndex(Constants.COLUMN_WAKE_SCREEN));
                unlock = c.getInt(c.getColumnIndex(Constants.COLUMN_UNLOCK_DEVICE));
                auto = c.getInt(c.getColumnIndex(Constants.COLUMN_AUTO_ACTION));
                notificationRepeat = c.getInt(c.getColumnIndex(Constants.COLUMN_NOTIFICATION_REPEAT));
                limits = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT_LIMIT));
                DataBase db = new DataBase(this);
                db.open();
                Cursor cf = db.queryCategories();
                if (cf != null && cf.moveToFirst()) {
                    String title = cf.getString(cf.getColumnIndex(Constants.COLUMN_TEXT));
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                    category.setText(title);
                }
                if (cf != null) {
                    cf.close();
                }
                db.close();
            }
            if (c != null) {
                c.close();
            }
            if (type != null) {
                if (type.matches(Constants.TYPE_REMINDER)) {
                    attachDateReminder();
                } else if (type.matches(Constants.TYPE_TIME)) {
                    attachTimeReminder();
                } else if (type.matches(Constants.TYPE_CALL)) {
                    attachCall();
                } else if (type.matches(Constants.TYPE_MESSAGE)) {
                    attachMessage();
                } else if (type.startsWith(Constants.TYPE_LOCATION)) {
                    if (LocationUtil.checkGooglePlayServicesAvailability(BackupFileEdit.this)) {
                        attachLocation();
                    } else {
                        Messages.toast(BackupFileEdit.this, getString(R.string.play_services_check_error));
                        finish();
                    }
                } else if (type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                    if (LocationUtil.checkGooglePlayServicesAvailability(BackupFileEdit.this)) {
                        attachLocationOut();
                    } else {
                        Messages.toast(BackupFileEdit.this, getString(R.string.play_services_check_error));
                        finish();
                    }
                } else if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                    attachWeekDayReminder();
                } else if (type.startsWith(Constants.TYPE_SKYPE)) {
                    attachSkype();
                } else if (type.startsWith(Constants.TYPE_APPLICATION)) {
                    attachApplication();
                } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                    attachMonthDay();
                }  else if (type.matches(Constants.TYPE_SHOPPING_LIST)) {
                    attachShoppingList();
                } else {
                    Messages.toast(BackupFileEdit.this, getString(R.string.file_error_message));
                    finish();
                }
            } else {
                Messages.toast(BackupFileEdit.this, getString(R.string.file_error_message));
                finish();
            }
        }
    }

    /**
     * Select contact button click listener.
     */
    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Permissions.checkPermission(BackupFileEdit.this, Permissions.READ_CONTACTS)) {
                SuperUtil.selectContact(BackupFileEdit.this, Constants.REQUEST_CODE_CONTACTS);
            } else {
                Permissions.requestPermission(BackupFileEdit.this, 107,
                        Permissions.READ_CONTACTS);
            }
        }
    };

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
     * Show shopping list reminder type creation layout.
     */
    private void attachShoppingList(){
        taskField.setHint(R.string.title);

        RelativeLayout shoppingLayout = (RelativeLayout) findViewById(R.id.shoppingLayout);
        ViewUtils.fadeInAnimation(shoppingLayout);

        RecyclerView todoList = (RecyclerView) findViewById(R.id.todoList);
        CardView cardContainer = (CardView) findViewById(R.id.cardContainer);
        cardContainer.setCardBackgroundColor(cSetter.getCardStyle());

        shoppingTimeContainer = (RelativeLayout) findViewById(R.id.shoppingTimeContainer);

        DateTimeView dateViewShopping = (DateTimeView) findViewById(R.id.dateViewShopping);
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
        if (isDark) {
            shopTimeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
        } else {
            shopTimeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
        }

        shoppingNoTime  = (TextView) findViewById(R.id.shoppingNoTime);
        shoppingNoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingNoTime.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingNoTime);
                }
                ViewUtils.show(shoppingTimeContainer);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                if (myYear > 0) {
                    cal.set(myYear, myMonth, myDay, myHour, myMinute);
                } else {
                    myYear = cal.get(Calendar.YEAR);
                    myMonth = cal.get(Calendar.MONTH);
                    myDay = cal.get(Calendar.DAY_OF_MONTH);
                    myHour = cal.get(Calendar.HOUR_OF_DAY);
                    myMinute = cal.get(Calendar.MINUTE);
                }
                isShoppingReminder = true;
            }
        });
        shopEdit = (EditText) findViewById(R.id.shopEdit);
        shopEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String task = shopEdit.getText().toString().trim();
                    if (task.matches("")) {
                        shopEdit.setError(getString(R.string.empty_task));
                        return false;
                    }

                    shoppingLists.addItem(new ShoppingList(task));
                    shoppingAdapter.notifyDataSetChanged();
                    shopEdit.setText("");
                    return true;
                } else {
                    return false;
                }
            }
        });
        ImageButton addButton = (ImageButton) findViewById(R.id.addButton);
        if (isDark) {
            addButton.setImageResource(R.drawable.ic_add_white_24dp);
        } else {
            addButton.setImageResource(R.drawable.ic_add_black_24dp);
        }
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
                if (item.isChecked() == 1) {
                    item.setIsChecked(0);
                } else {
                    item.setIsChecked(1);
                }
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
        if (id != 0){
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "", fileLoc = null;
            if (c != null && c.moveToFirst()) {
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                fileLoc = c.getString(c.getColumnIndex(Constants.FilesConstants.COLUMN_FILE_LOCATION));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            }
            shoppingLists = new ShoppingListDataProvider(SyncHelper.getList(fileLoc));
            shoppingAdapter = new TaskListRecyclerAdapter(this, shoppingLists, new TaskListRecyclerAdapter.ActionListener() {
                @Override
                public void onItemCheck(int position, boolean isChecked) {
                    ShoppingList item = shoppingLists.getItem(position);
                    if (item.isChecked() == 1) {
                        item.setIsChecked(0);
                    } else {
                        item.setIsChecked(1);
                    }
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

            if (myDay > 0 && myHour > 0 && myMinute > 0 && myMonth > 0 && myYear > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.set(myYear, myMonth, myDay, myHour, myMinute);

                dateViewShopping.setDateTime(cal.getTimeInMillis());
                if (shoppingNoTime.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingNoTime);
                }
                ViewUtils.show(shoppingTimeContainer);
                isShoppingReminder = true;
            } else {
                if (shoppingTimeContainer.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingTimeContainer);
                }
                ViewUtils.show(shoppingNoTime);
                isShoppingReminder = false;
            }

            taskField.setText(text);
        }
    }

    /**
     * Show simple date reminder creation layout.
     */
    private void attachDateReminder(){
        LinearLayout by_date_layout = (LinearLayout) findViewById(R.id.by_date_layout);
        ViewUtils.fadeInAnimation(by_date_layout);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        DateTimeView dateView = (DateTimeView) findViewById(R.id.dateView);
        dateView.setListener(this);

        dateTaskExport = (CheckBox) findViewById(R.id.dateTaskExport);
        if (gtx.isLinked()){
            dateTaskExport.setVisibility(View.VISIBLE);
        }

        RepeatView repeatView = (RepeatView) findViewById(R.id.repeatView);
        repeatView.setListener(this);
        repeatView.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
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
            if (c != null) {
                c.close();
            }
            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            dateView.setDateTime(cal.getTimeInMillis());
            repeatView.setProgress(repCode);
        }
    }

    /**
     * Show by day of month reminder creation layout.
     */
    private void attachMonthDay(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout monthDayLayout = (LinearLayout) findViewById(R.id.monthDayLayout);
        ViewUtils.fadeInAnimation(monthDayLayout);

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

        CheckBox monthDayExport = (CheckBox) findViewById(R.id.monthDayExport);
        if ((isCalendar || isStock)){
            monthDayExport.setVisibility(View.VISIBLE);
        }

        monthDayTaskExport = (CheckBox) findViewById(R.id.monthDayTaskExport);
        if (gtx.isLinked()){
            monthDayTaskExport.setVisibility(View.VISIBLE);
        }

        String dayStr;
        if (myDay > 28) {
            myDay = 28;
        }
        if (myDay < 10) {
            dayStr = "0" + myDay;
        } else {
            dayStr = String.valueOf(myDay);
        }

        monthDayField.setText(dayStr);
        monthDayField.setTypeface(AssetsUtil.getMediumTypeface(this));

        monthDayTimeField = (TextView) findViewById(R.id.monthDayTimeField);
        monthDayTimeField.setOnClickListener(timeClick);
        monthDayTimeField.setText(TimeUtil.getTime(cal.getTime(),
                sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
        monthDayTimeField.setTypeface(AssetsUtil.getMediumTypeface(this));

        dayCheck = (RadioButton) findViewById(R.id.dayCheck);
        dayCheck.setChecked(true);
        lastCheck = (RadioButton) findViewById(R.id.lastCheck);
        dayCheck.setOnCheckedChangeListener(this);
        lastCheck.setOnCheckedChangeListener(this);

        actionViewMonth = (ActionView) findViewById(R.id.actionViewMonth);
        actionViewMonth.setListener(this);
        actionViewMonth.setActivity(this);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "";
            String number = "";
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            }
            if (c != null) {
                c.close();
            }

            if (myDay == 0) {
                myDay = 1;
            }
            if (myDay < 10) {
                dayStr = "0" + myDay;
            } else {
                dayStr = String.valueOf(myDay);
            }

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            monthDayTimeField.setText(TimeUtil.getTime(cal.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            monthDayField.setText(dayStr);

            if (type.matches(Constants.TYPE_MONTHDAY)){
                actionViewMonth.setAction(false);
                dayCheck.setChecked(true);
            } else if(type.matches(Constants.TYPE_MONTHDAY_LAST)){
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
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
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
                    ViewUtils.fadeOutAnimation(specsContainerOut);
                    ViewUtils.fadeInAnimation(mapContainerOut);
                    if (mLocList != null) {
                        mLocationManager.removeUpdates(mLocList);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Show alarm clock reminder type creation layout.
     */
    private void attachWeekDayReminder(){
        cSetter = new ColorSetter(BackupFileEdit.this);
        LinearLayout weekday_layout = (LinearLayout) findViewById(R.id.weekday_layout);
        ViewUtils.fadeInAnimation(weekday_layout);

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

        actionViewWeek = (ActionView) findViewById(R.id.actionViewWeek);
        actionViewWeek.setListener(this);
        actionViewWeek.setActivity(this);

        if (id != 0) {
            fdb.open();
            Cursor x = fdb.getFile(id);
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
            if (x != null) {
                x.close();
            }

            c.set(Calendar.HOUR_OF_DAY, myHour);
            c.set(Calendar.MINUTE, myMinute);

            weekTimeField.setText(TimeUtil.getTime(c.getTime(),
                    sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT)));
            taskField.setText(text);

            setCheckForDays(weekdays);

            if (type.matches(Constants.TYPE_WEEKDAY)){
                actionViewWeek.setAction(false);
            } else {
                actionViewWeek.setAction(true);
                actionViewWeek.setNumber(number);
                if (type.matches(Constants.TYPE_WEEKDAY_CALL)){
                    actionViewWeek.setType(ActionView.TYPE_CALL);
                } else {
                    actionViewWeek.setType(ActionView.TYPE_MESSAGE);
                }
            }
        }
    }

    /**
     * Check days toggle buttons depends on weekday string.
     * @param weekdays weekday string.
     */
    private void setCheckForDays(final String weekdays){
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECKED)) {
            mondayCheck.setChecked(true);
        } else {
            mondayCheck.setChecked(false);
        }

        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECKED)) {
            tuesdayCheck.setChecked(true);
        } else {
            tuesdayCheck.setChecked(false);
        }

        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECKED)) {
            wednesdayCheck.setChecked(true);
        } else {
            wednesdayCheck.setChecked(false);
        }

        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECKED)) {
            thursdayCheck.setChecked(true);
        } else {
            thursdayCheck.setChecked(false);
        }

        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECKED)) {
            fridayCheck.setChecked(true);
        } else {
            fridayCheck.setChecked(false);
        }

        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECKED)) {
            saturdayCheck.setChecked(true);
        } else {
            saturdayCheck.setChecked(false);
        }

        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECKED)) {
            sundayCheck.setChecked(true);
        } else {
            sundayCheck.setChecked(false);
        }
    }

    /**
     * Show timer reminder type creation layout.
     */
    private void attachTimeReminder(){
        cSetter = new ColorSetter(BackupFileEdit.this);
        LinearLayout after_time_layout = (LinearLayout) findViewById(R.id.after_time_layout);
        ViewUtils.fadeInAnimation(after_time_layout);

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
                startActivityForResult(new Intent(BackupFileEdit.this, ExclusionPickerDialog.class), 1111);
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
                if (exclusion != null) {
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

        RepeatView repeatViewTime = (RepeatView) findViewById(R.id.repeatViewTime);
        repeatViewTime.setListener(this);
        repeatViewTime.setMax(120);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "";
            int repeat = 0;
            long afterTime=0;
            if (c != null && c.moveToFirst()){
                text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                afterTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                exclusion = c.getString(c.getColumnIndex(Constants.COLUMN_EXTRA_3));
                repeat = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            }
            if (c != null) {
                c.close();
            }
            taskField.setText(text);
            repeatViewTime.setProgress(repeat);
            timeString = TimeUtil.generateAfterString(afterTime);
            updateTimeView();
            setExclusion(exclusion);
        }
    }

    /**
     * Set up exclusion for reminder.
     * @param jsonObject json object string.
     */
    private void setExclusion(final String jsonObject){
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

    /**
     * Set time in time view fields.
     */
    private void updateTimeView() {
        if (timeString.matches("000000")) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
        }
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
        ViewUtils.fadeInAnimation(skype_layout);

        skypeUser = (EditText) findViewById(R.id.skypeUser);

        skypeCall = (RadioButton) findViewById(R.id.skypeCall);
        skypeVideo = (RadioButton) findViewById(R.id.skypeVideo);
        skypeCall.setChecked(true);
        RadioButton skypeChat = (RadioButton) findViewById(R.id.skypeChat);
        skypeChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    taskField.setHint(getString(R.string.message_field_hint));
                } else {
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

        skypeTaskExport = (CheckBox) findViewById(R.id.skypeTaskExport);
        if (gtx.isLinked()){
            skypeTaskExport.setVisibility(View.VISIBLE);
        }

        DateTimeView dateViewSkype = (DateTimeView) findViewById(R.id.dateViewSkype);
        dateViewSkype.setListener(this);

        RepeatView repeatViewSkype = (RepeatView) findViewById(R.id.repeatViewSkype);
        repeatViewSkype.setListener(this);
        repeatViewSkype.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
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
            if (c != null) {
                c.close();
            }

            if (type.matches(Constants.TYPE_SKYPE)) {
                skypeCall.setChecked(true);
            }
            if (type.matches(Constants.TYPE_SKYPE_VIDEO)) {
                skypeVideo.setChecked(true);
            }
            if (type.matches(Constants.TYPE_SKYPE_CHAT)) {
                skypeChat.setChecked(true);
            }

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            skypeUser.setText(number);
            dateViewSkype.setDateTime(cal.getTimeInMillis());
            repeatViewSkype.setProgress(repCode);
        }
    }

    /**
     * Show application reminder type creation layout.
     */
    private void attachApplication(){
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout application_layout = (LinearLayout) findViewById(R.id.application_layout);
        ViewUtils.fadeInAnimation(application_layout);

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
        } else {
            pickApplication.setImageResource(R.drawable.ic_launch_black_24dp);
        }

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

        appTaskExport = (CheckBox) findViewById(R.id.appTaskExport);
        if (gtx.isLinked()){
            appTaskExport.setVisibility(View.VISIBLE);
        }

        DateTimeView dateViewApp = (DateTimeView) findViewById(R.id.dateViewApp);
        dateViewApp.setListener(this);

        RepeatView repeatViewApp = (RepeatView) findViewById(R.id.repeatViewApp);
        repeatViewApp.setListener(this);
        repeatViewApp.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "", number = "";
            int repCode = 0;
            if (c != null && c.moveToFirst()) {
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
            if (c != null) {
                c.close();
            }

            selectedPackage = number;

            if (type.matches(Constants.TYPE_APPLICATION)) {
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

            if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                browser.setChecked(true);
                application.setEnabled(false);
                browseLink.setText(number);
            }

            cal.set(myYear, myMonth, myDay, myHour, myMinute);
            taskField.setText(text);

            dateViewApp.setDateTime(cal.getTimeInMillis());
            repeatViewApp.setProgress(repCode);
        }
    }

    /**
     * Show call reminder type creation layout.
     */
    private void attachCall(){
        LinearLayout call_layout = (LinearLayout) findViewById(R.id.call_layout);
        ViewUtils.fadeInAnimation(call_layout);

        ImageButton addNumberButton = (ImageButton) findViewById(R.id.addNumberButton);
        addNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addNumberButton, isDark);

        phoneNumber = (FloatingEditText) findViewById(R.id.phoneNumber);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        callTaskExport = (CheckBox) findViewById(R.id.callTaskExport);
        if (gtx.isLinked()) {
            callTaskExport.setVisibility(View.VISIBLE);
        }

        DateTimeView dateViewCall = (DateTimeView) findViewById(R.id.dateViewCall);
        dateViewCall.setListener(this);

        RepeatView repeatViewCall = (RepeatView) findViewById(R.id.repeatViewCall);
        repeatViewCall.setListener(this);
        repeatViewCall.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "", number = "";
            int repCode = 0;
            if (c != null && c.moveToFirst()) {
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
            if (c != null) {
                c.close();
            }

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            phoneNumber.setText(number);
            dateViewCall.setDateTime(cal.getTimeInMillis());
            repeatViewCall.setProgress(repCode);
        }
    }

    /**
     * Show message reminder type creation layout.
     */
    private void attachMessage(){
        LinearLayout message_layout = (LinearLayout) findViewById(R.id.message_layout);
        ViewUtils.fadeInAnimation(message_layout);

        ImageButton addMessageNumberButton = (ImageButton) findViewById(R.id.addMessageNumberButton);
        addMessageNumberButton.setOnClickListener(contactClick);
        ViewUtils.setImage(addMessageNumberButton, isDark);

        messageNumber = (FloatingEditText) findViewById(R.id.messageNumber);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        messageTaskExport = (CheckBox) findViewById(R.id.messageTaskExport);
        if (gtx.isLinked()){
            messageTaskExport.setVisibility(View.VISIBLE);
        }

        DateTimeView dateViewMessage = (DateTimeView) findViewById(R.id.dateViewMessage);
        dateViewMessage.setListener(this);

        RepeatView repeatViewMessage = (RepeatView) findViewById(R.id.repeatViewMessage);
        repeatViewMessage.setListener(this);
        repeatViewMessage.setMax(Configs.REPEAT_SEEKBAR_MAX);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "", number = "";
            int repCode = 0;
            if (c != null && c.moveToFirst()) {
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
            if (c != null) {
                c.close();
            }

            cal.set(myYear, myMonth, myDay, myHour, myMinute);

            taskField.setText(text);
            messageNumber.setText(number);
            dateViewMessage.setDateTime(cal.getTimeInMillis());
            repeatViewMessage.setProgress(repCode);
        }
    }

    @Override
    public void place(final LatLng place) {
        curPlace = place;
        if (isLocationOutAttached()) {
            mapLocation.setText(LocationUtil.getAddress(place.latitude, place.longitude));
        }
    }

    @Override
    public void onZoomOutClick() {
        if (isLocationAttached()) {
            ViewUtils.fadeOutAnimation(mapContainer);
            ViewUtils.fadeInAnimation(specsContainer);
        }
        if (isLocationOutAttached()) {
            ViewUtils.fadeOutAnimation(mapContainerOut);
            ViewUtils.fadeInAnimation(specsContainerOut);
        }
    }

    @Override
    public void placeName(final String name) {

    }

    /**
     * Show location reminder type creation layout.
     */
    private void attachLocation() {
        LinearLayout geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        ViewUtils.fadeInAnimation(geolocationlayout);

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        specsContainer = (ScrollView) findViewById(R.id.specsContainer);
        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
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
                    Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    delayLayout.startAnimation(slide);
                    delayLayout.setVisibility(View.VISIBLE);
                } else {
                    delayLayout.setVisibility(View.GONE);
                }
            }
        });

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
                ViewUtils.fadeOutAnimation(specsContainer);
                ViewUtils.fadeInAnimation(mapContainer);
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
                if (task != null && !task.isCancelled()) {
                    task.cancel(true);
                }
                task = new GeocoderTask(BackupFileEdit.this, BackupFileEdit.this);
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
                int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
                if (map != null) {
                    map.addMarker(pos, title, true, false, radius);
                }
            }
        });

        actionViewLocation = (ActionView) findViewById(R.id.actionViewLocation);
        actionViewLocation.setListener(this);
        actionViewLocation.setActivity(this);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        DateTimeView dateViewLocation = (DateTimeView) findViewById(R.id.dateViewLocation);
        dateViewLocation.setListener(this);

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "", number = null;
            double latitude = 0, longitude = 0;
            if (c != null && c.moveToFirst()) {
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
            if (c != null) {
                c.close();
            }

            if (myDay > 0 && myMonth > 0 && myYear > 0) {
                cal.set(myYear, myMonth, myDay, myHour, myMinute);

                dateViewLocation.setDateTime(cal.getTimeInMillis());
                attackDelay.setChecked(true);
            } else {
                attackDelay.setChecked(false);
            }

            if (type.matches(Constants.TYPE_LOCATION_CALL) || type.matches(Constants.TYPE_LOCATION_MESSAGE)){
                actionViewLocation.setAction(true);
                actionViewLocation.setNumber(number);
                if (type.matches(Constants.TYPE_LOCATION_CALL)){
                    actionViewLocation.setType(ActionView.TYPE_CALL);
                } else {
                    actionViewLocation.setType(ActionView.TYPE_MESSAGE);
                }
            } else {
                actionViewLocation.setAction(false);
            }

            taskField.setText(text);
            int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
            if (map != null) {
                map.addMarker(new LatLng(latitude, longitude), text, true, true, radius);
            }
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
    private void attachLocationOut() {
        taskField.setHint(getString(R.string.tast_hint));

        LinearLayout locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        ViewUtils.fadeInAnimation(locationOutLayout);

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
                    Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    delayLayoutOut.startAnimation(slide);
                    delayLayoutOut.setVisibility(View.VISIBLE);
                }
                else {
                    delayLayoutOut.setVisibility(View.GONE);
                }
            }
        });

        if (attachDelayOut.isChecked()) {
            ViewUtils.expand(delayLayoutOut);
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
                ViewUtils.fadeOutAnimation(specsContainerOut);
                ViewUtils.fadeInAnimation(mapContainerOut);
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
        if (pointRadius.getProgress() == 0) {
            pointRadius.setProgress(sPrefs.loadInt(Prefs.LOCATION_RADIUS));
        }

        actionViewLocationOut = (ActionView) findViewById(R.id.actionViewLocationOut);
        actionViewLocationOut.setListener(this);
        actionViewLocationOut.setActivity(this);

        final Calendar cal = Calendar.getInstance();
        myYear = cal.get(Calendar.YEAR);
        myMonth = cal.get(Calendar.MONTH);
        myDay = cal.get(Calendar.DAY_OF_MONTH);
        myHour = cal.get(Calendar.HOUR_OF_DAY);
        myMinute = cal.get(Calendar.MINUTE);

        DateTimeView dateViewLocationOut = (DateTimeView) findViewById(R.id.dateViewLocationOut);
        dateViewLocationOut.setListener(this);

        if (curPlace != null) {
            int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
            if (mapOut != null) {
                mapOut.addMarker(curPlace, null, true, true, radius);
            }
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (id != 0) {
            fdb.open();
            Cursor c = fdb.getFile(id);
            String text = "", number = null;
            double latitude = 0, longitude = 0;
            if (c != null && c.moveToFirst()) {
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
            if (c != null) {
                c.close();
            }

            if (myDay > 0 && myMonth > 0 && myYear > 0) {
                cal.set(myYear, myMonth, myDay, myHour, myMinute);
                dateViewLocationOut.setDateTime(cal.getTimeInMillis());
                attachDelayOut.setChecked(true);
            } else {
                attachDelayOut.setChecked(false);
            }

            if (type.matches(Constants.TYPE_LOCATION_OUT_CALL) || type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                actionViewLocationOut.setAction(true);
                actionViewLocationOut.setNumber(number);
                if (type.matches(Constants.TYPE_LOCATION_OUT_CALL)){
                    actionViewLocationOut.setType(ActionView.TYPE_CALL);
                } else {
                    actionViewLocationOut.setType(ActionView.TYPE_MESSAGE);
                }
            } else {
                actionViewLocationOut.setAction(false);
            }

            taskField.setText(text);
            if (longitude != 0 && latitude != 0) {
                LatLng pos = new LatLng(latitude, longitude);
                int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
                if (mapOut != null) {
                    mapOut.addMarker(pos, text, true, true, radius);
                }
                mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
                mapCheck.setChecked(true);
            }
        }
    }

    /**
     * Check if shopping list reminder type layout visible.
     * @return Boolean
     */
    private boolean isShoppingAttached(){
        return findViewById(R.id.shoppingLayout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if date reminder type layout visible.
     * @return Boolean
     */
    private boolean isDateReminderAttached(){
        return findViewById(R.id.by_date_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if weekday reminder type layout visible.
     * @return Boolean
     */
    private boolean isWeekDayReminderAttached(){
        return findViewById(R.id.weekday_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if application reminder type layout visible.
     * @return Boolean
     */
    private boolean isApplicationAttached(){
        return findViewById(R.id.application_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if time reminder type layout visible.
     * @return Boolean
     */
    private boolean isTimeReminderAttached(){
        return findViewById(R.id.after_time_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if Skype reminder type layout visible.
     * @return Boolean
     */
    private boolean isSkypeAttached(){
        return findViewById(R.id.skype_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if call reminder type layout visible.
     * @return Boolean
     */
    private boolean isCallAttached(){
        return findViewById(R.id.call_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if message reminder type layout visible.
     * @return Boolean
     */
    private boolean isMessageAttached(){
        return findViewById(R.id.message_layout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if location reminder type layout visible.
     * @return Boolean
     */
    private boolean isLocationAttached(){
        return findViewById(R.id.geolocationlayout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if monthday reminder type layout visible.
     * @return Boolean
     */
    private boolean isMonthDayAttached(){
        return findViewById(R.id.monthDayLayout).getVisibility() == View.VISIBLE;
    }

    /**
     * Check if location out reminder type layout visible.
     * @return Boolean
     */
    private boolean isLocationOutAttached(){
        return findViewById(R.id.locationOutLayout).getVisibility() == View.VISIBLE;
    }

    /**
     * Hide all reminder types layouts.
     */
    private void clearForm(){
        findViewById(R.id.call_layout).setVisibility(View.GONE);
        findViewById(R.id.weekday_layout).setVisibility(View.GONE);
        findViewById(R.id.by_date_layout).setVisibility(View.GONE);
        findViewById(R.id.after_time_layout).setVisibility(View.GONE);
        findViewById(R.id.geolocationlayout).setVisibility(View.GONE);
        findViewById(R.id.message_layout).setVisibility(View.GONE);
        findViewById(R.id.skype_layout).setVisibility(View.GONE);
        findViewById(R.id.application_layout).setVisibility(View.GONE);
        findViewById(R.id.monthDayLayout).setVisibility(View.GONE);
        findViewById(R.id.locationOutLayout).setVisibility(View.GONE);
        findViewById(R.id.shoppingLayout).setVisibility(View.GONE);
    }

    /**
     * Get type for simple reminder family.
     * @return Type string
     */
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
            if (actionViewLocation.hasAction()){
                if (actionViewLocation.getType() == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_CALL;
                } else {
                    type = Constants.TYPE_LOCATION_MESSAGE;
                }
            } else {
                type = Constants.TYPE_LOCATION;
            }
        } else if (isLocationOutAttached()){
            if (actionViewLocationOut.hasAction()){
                if (actionViewLocationOut.getType() == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_OUT_CALL;
                } else {
                    type = Constants.TYPE_LOCATION_OUT_MESSAGE;
                }
            } else {
                type = Constants.TYPE_LOCATION_OUT;
            }
        }
        return type;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
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
                if (isWeekDayReminderAttached() && actionViewWeek.hasAction()){
                    actionViewWeek.setNumber(number);
                }
                if (isMonthDayAttached() && actionViewMonth.hasAction()){
                    actionViewMonth.setNumber(number);
                }
                if (isLocationAttached() && actionViewLocation.hasAction()){
                    actionViewLocation.setNumber(number);
                }
                if (isLocationOutAttached() && actionViewLocationOut.hasAction()){
                    actionViewLocationOut.setNumber(number);
                }
            }
        }

        if (requestCode == 1111) {
            if (resultCode == RESULT_OK){
                exclusion = data.getStringExtra("excl");
                setExclusion(exclusion);
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
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

    /**
     * Save reminder to database.
     */
    private void saveTask(){
        new SharedPrefs(this).saveBoolean(Prefs.REMINDER_CHANGED, true);
        if (isLocationAttached() || isLocationOutAttached()){
            if (LocationUtil.checkLocationEnable(this)) {
                if (isLocationOutAttached()){
                    if (actionViewLocationOut.hasAction() && !checkNumber()) {
                        saveLocationOut();
                    } else if (actionViewLocationOut.hasAction() && checkNumber()) {
                        actionViewLocationOut.showError();
                    } else {
                        saveLocationOut();
                    }
                } else {
                    if (actionViewLocation.hasAction() && !checkNumber()) {
                        saveLocation();
                    } else if (actionViewLocation.hasAction() && checkNumber()) {
                        actionViewLocation.showError();
                    } else {
                        saveLocation();
                    }
                }
            } else {
                LocationUtil.showLocationAlert(this);
            }
        } else {
            if (isDateReminderAttached()) {
                saveDateTask();
            } else if (isTimeReminderAttached()) {
                saveTimeTask();
            } else if (isShoppingAttached()) {
                saveShoppingTask();
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
                    actionViewWeek.showError();
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
                        Messages.toast(BackupFileEdit.this, getString(R.string.not_selected_application_message));
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
                    actionViewMonth.showError();
                }
            }
        }
    }

    /**
     * Save shopping list reminder type to database.
     */
    private void saveShoppingTask() {
        if (shoppingLists.getCount() == 0){
            Messages.toast(BackupFileEdit.this, getString(R.string.no_tasks_warming));
            return;
        }
        String task = taskField.getText().toString().trim();
        String type = Constants.TYPE_SHOPPING_LIST;
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        if (!isShoppingReminder){
            myDay = 0;
            myMonth = 0;
            myYear = 0;
            myHour = 0;
            myMinute = 0;
        }
        long ids = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                0, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        new AlarmReceiver().setAlarm(this, ids);
        DB.close();
        new ShoppingType(this).saveShopList(ids, shoppingLists.getData(), null);
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Get type for alarm clock reminder.
     * @return Type string
     */
    private String getWeekTaskType(){
        String type;
        if (actionViewWeek.hasAction()){
            if (actionViewWeek.getType() == ActionView.TYPE_CALL){
                type = Constants.TYPE_WEEKDAY_CALL;
            } else {
                type = Constants.TYPE_WEEKDAY_MESSAGE;
            }
        } else {
            type = Constants.TYPE_WEEKDAY;
        }
        return type;
    }

    /**
     * Get type for monthday reminder.
     * @return Type string
     */
    private String getMonthTaskType(){
        String type;
        if (actionViewMonth.hasAction()){
            if (actionViewMonth.getType() == ActionView.TYPE_CALL){
                if (lastCheck.isChecked()) {
                    type = Constants.TYPE_MONTHDAY_CALL_LAST;
                } else {
                    type = Constants.TYPE_MONTHDAY_CALL;
                }
            } else {
                if (lastCheck.isChecked()) {
                    type = Constants.TYPE_MONTHDAY_MESSAGE_LAST;
                } else {
                    type = Constants.TYPE_MONTHDAY_MESSAGE;
                }
            }
        } else {
            if (lastCheck.isChecked()) {
                type = Constants.TYPE_MONTHDAY_LAST;
            } else {
                type = Constants.TYPE_MONTHDAY;
            }
        }
        return type;
    }

    /**
     * Get type for application reminder.
     * @return Type string
     */
    private String getAppTaskType(){
        String type;
        if (application.isChecked()){
            type = Constants.TYPE_APPLICATION;
        } else {
            type = Constants.TYPE_APPLICATION_BROWSER;
        }
        return type;
    }

    /**
     * Get type for Skype reminder.
     * @return Type string
     */
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

    /**
     * Save alarm clock reminder type to database.
     */
    private void saveWeekTask() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getWeekTaskType();
        String number = null;
        if (actionViewWeek.hasAction()) {
            number = actionViewWeek.getNumber();
        }

        Interval interval = new Interval(BackupFileEdit.this);
        String repeat = interval.getWeekRepeat(mondayCheck.isChecked(), tuesdayCheck.isChecked(),
                wednesdayCheck.isChecked(), thursdayCheck.isChecked(), fridayCheck.isChecked(),
                saturdayCheck.isChecked(), sundayCheck.isChecked());
        if (repeat.matches(Constants.NOTHING_CHECKED)) {
            Messages.toast(BackupFileEdit.this, getString(R.string.weekday_nothing_checked));
            return;
        }
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(task, type, 0, 0, 0, myHour, myMinute, 0, number,
                0, 0, 0, 0, 0, uuID, repeat, 0, null, 0, -1, 0, categoryId, exclusion);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        new WeekDayReceiver().setAlarm(BackupFileEdit.this, ids);
        long startTime = ReminderUtils.getWeekTime(myHour, myMinute, repeat);
        ReminderUtils.exportToCalendar(this, task, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && weekTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, task, startTime, ids);
        }
        DB.close();
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save application reminder type to database.
     */
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
            if (!number.startsWith("http://") && !number.startsWith("https://")) {
                number = "http://" + number;
            }
        }

        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                number, repeatCode, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        alarm.setAlarm(BackupFileEdit.this, ids);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, task, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && appTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, task, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save Skype reminder type to database.
     */
    private void saveSkypeTask() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")) {
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getSkypeTaskType();
        String number = skypeUser.getText().toString().trim();

        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                number, repeatCode, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        alarm.setAlarm(BackupFileEdit.this, ids);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, task, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && skypeTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, task, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Check if user select or insert contact number.
     * @return Boolean
     */
    private boolean checkNumber(){
        if (isCallAttached()) {
            return phoneNumber.getText().toString().trim().matches("");
        } else if (isSkypeAttached()){
            return skypeUser.getText().toString().trim().matches("");
        } else if (isMessageAttached()){
            return messageNumber.getText().toString().trim().matches("");
        } else if (isLocationAttached() && actionViewLocation.hasAction()){
            return actionViewLocation.getNumber().matches("");
        } else if (isWeekDayReminderAttached() && actionViewWeek.hasAction()){
            return actionViewWeek.getNumber().matches("");
        } else if (isMonthDayAttached() && actionViewMonth.hasAction()){
            return actionViewMonth.getNumber().matches("");
        } else if (isLocationOutAttached() && actionViewLocationOut.hasAction()){
            return actionViewLocationOut.getNumber().matches("");
        } else {
            return false;
        }
    }

    /**
     * Check if user select application.
     * @return Boolean
     */
    private boolean checkApplication() {
        if (application.isChecked()) {
            return applicationName.getText().toString().trim().matches("");
        } else {
            return browser.isChecked() && browseLink.getText().toString().trim().matches("");
        }
    }

    /**
     * Check if message not empty.
     * @return Boolean
     */
    private boolean checkMessage(){
        if (isMessageAttached()){
            return taskField.getText().toString().trim().matches("");
        } else if (isWeekDayReminderAttached() && actionViewWeek.hasAction()) {
            return taskField.getText().toString().trim().matches("");
        } else {
            return taskField.getText().toString().trim().matches("");
        }
    }

    /**
     * Save monthday reminder type to database.
     */
    private void saveMonthTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }

        String type = getMonthTaskType();
        String number = null;
        if (actionViewMonth.hasAction()) {
            number = actionViewMonth.getNumber();
        }

        int day = myDay;
        if (type.endsWith("_last")) {
            day = 0;
        }

        DB.open();
        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }

        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(text, type, myDay, 0, 0, myHour, myMinute, 0, number, 0, 0, 0, 0, 0,
                uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        new MonthDayReceiver().setAlarm(this, ids);

        long startTime = ReminderUtils.getMonthTime(myHour, myMinute, day);
        ReminderUtils.exportToCalendar(this, text, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && monthDayTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save simple date reminder type to database.
     */
    private void saveDateTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0, null,
                repeatCode, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        alarm.setAlarm(BackupFileEdit.this, ids);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, text, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && dateTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save time reminder type to database.
     */
    private void saveTimeTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        long time = SuperUtil.getAfterTime(this, timeString);
        if (time == 0) {
            return;
        }
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
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }

        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, mySeconds,
                null, 0, time, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        alarm.setAlarm(BackupFileEdit.this, ids);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, time);
        ReminderUtils.exportToCalendar(this, text, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && timeTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save call reminder type to database.
     */
    private void saveCallTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = phoneNumber.getText().toString().trim();
        DB = new DataBase(BackupFileEdit.this);
        DB.open();

        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }

        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                number, repeatCode, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        DB.close();
        alarm.setAlarm(BackupFileEdit.this, ids);
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, text, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && callTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save message reminder type to database.
     */
    private void saveMessageTask(){
        String text = taskField.getText().toString().trim();
        if (text.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = messageNumber.getText().toString().trim();
        DB = new DataBase(BackupFileEdit.this);
        DB.open();

        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }

        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        long ids = DB.insertReminder(text, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                number, repeatCode, 0, 0, 0, 0, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
        DB.updateReminderDateTime(ids);
        DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
        alarm.setAlarm(BackupFileEdit.this, ids);
        DB.close();
        long startTime = ReminderUtils.getTime(myDay, myMonth, myYear, myHour, myMinute, 0);
        ReminderUtils.exportToCalendar(this, text, startTime, ids, isCalendar, isStock);
        if (gtx.isLinked() && messageTaskExport.isChecked()){
            ReminderUtils.exportToTasks(this, text, startTime, ids);
        }
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save location reminder type to database.
     */
    private void saveLocation(){
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = null;
        if (actionViewLocation.hasAction()) {
            number = actionViewLocation.getNumber();
        }
        LatLng dest = null;
        boolean isNull = true;
        if (curPlace != null) {
            dest = curPlace;
            isNull = false;
        }

        if (isNull){
            Messages.toast(BackupFileEdit.this, getString(R.string.point_warning));
            return;
        }

        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }

        if (!LocationUtil.checkLocationEnable(this)){
            LocationUtil.showLocationAlert(this);
            return;
        }

        Double latitude = dest.latitude;
        Double longitude = dest.longitude;
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }
        if (attackDelay.isChecked()){
            long ids = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                    number, 0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
            DB.updateReminderDateTime(ids);
            DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
            new PositionDelayReceiver().setDelay(BackupFileEdit.this, ids);
        } else {
            long ids = DB.insertReminder(task, type, 0, 0, 0, 0, 0, 0, number,
                    0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
            DB.updateReminderDateTime(ids);
            DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
            startService(new Intent(BackupFileEdit.this, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        DB.close();
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Save location out reminder type to database.
     */
    private void saveLocationOut() {
        String task = taskField.getText().toString().trim();
        if (task.matches("")){
            taskField.setError(getString(R.string.empty_field_error));
            return;
        }
        String type = getTaskType();
        String number = null;
        if (actionViewLocationOut.hasAction()) {
            number = actionViewLocationOut.getNumber();
        }
        LatLng dest = null;
        boolean isNull = true;
        if (curPlace != null) {
            dest = curPlace;
            isNull = false;
        }

        if (isNull){
            Messages.toast(BackupFileEdit.this, getString(R.string.point_warning));
            return;
        }

        if (isUID(uuID)){
            Messages.toast(BackupFileEdit.this, getString(R.string.same_uuid_error));
            return;
        }

        if (!LocationUtil.checkLocationEnable(this)){
            LocationUtil.showLocationAlert(this);
            return;
        }

        Double latitude = dest.latitude;
        Double longitude = dest.longitude;
        DB = new DataBase(BackupFileEdit.this);
        DB.open();
        if (categoryId == null) {
            Cursor cf = DB.queryCategories();
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) {
                cf.close();
            }
        }

        if (attachDelayOut.isChecked()){
            long ids = DB.insertReminder(task, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                    number, 0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
            DB.updateReminderDateTime(ids);
            DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
            new PositionDelayReceiver().setDelay(BackupFileEdit.this, ids);
        } else {
            long ids = DB.insertReminder(task, type, 0, 0, 0, 0, 0, 0, number,
                    0, 0, 0, latitude, longitude, uuID, null, 0, null, 0, -1, 0, categoryId, exclusion);
            DB.updateReminderDateTime(ids);
            DB.updateReminderExtra(ids, vibration, voice, notificationRepeat, wake, unlock, auto, limits);
            startService(new Intent(BackupFileEdit.this, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        DB.close();
        new UpdatesHelper(BackupFileEdit.this).updateWidget();
        finish();
    }

    /**
     * Check if reminder unique identifier also exist in database.
     * @param uuId reminder unique identifier.
     * @return Boolean
     */
    private boolean isUID(final String uuId){
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
    public void onClick(final View v) {
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
            default:
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
                if (myDay < 29) {
                    monthDayField.setText(dayOfMonth);
                } else {
                    myDay = 28;
                    Messages.toast(BackupFileEdit.this, getString(R.string.string_max_day_message));
                }
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
    protected void onDestroy() {
        if (mLocList != null) {
            mLocationManager.removeUpdates(mLocList);
        }
        if (DB != null) {
            DB.close();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (map != null && !map.onBackPressed()) {
            return;
        }
        if (mapOut != null && !mapOut.onBackPressed()) {
            return;
        }
        finish();
    }

    @Override
    public void onAddressReceived(final List<Address> addresses) {
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

    @Override
    public void onCategory(final String catId, final String title) {
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

    }

    @Override
    public void onActionChange(boolean b) {
        if (!b){
            taskField.setHint(getString(R.string.tast_hint));
        }
    }

    @Override
    public void onTypeChange(boolean type) {
        if (type) {
            taskField.setHint(getString(R.string.message_field_hint));
        } else {
            taskField.setHint(getString(R.string.tast_hint));
        }
    }

    public class CurrentLocation implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            double currentLat = location.getLatitude();
            double currentLong = location.getLongitude();
            curPlace = new LatLng(currentLat, currentLong);
            String _Location = LocationUtil.getAddress(currentLat, currentLong);
            String text = taskField.getText().toString().trim();
            if (text.matches("")) {
                text = _Location;
            }
            if (isLocationOutAttached()) {
                currentLocation.setText(_Location);
                if (mapOut != null) {
                    int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
                    mapOut.addMarker(new LatLng(currentLat, currentLong), text, true, false, radius);
                    mapOut.moveCamera(new LatLng(currentLat, currentLong));
                }
            }
        }

        @Override
        public void onStatusChanged(final String provider, final int status, final Bundle extras) {
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
        public void onProviderEnabled(final String provider) {
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
        public void onProviderDisabled(final String provider) {
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
