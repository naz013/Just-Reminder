/**
 * Copyright 2015 Nazar Suhovich
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
package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderApp;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.creator.ApplicationFragment;
import com.cray.software.justreminder.creator.BaseFragment;
import com.cray.software.justreminder.creator.CallFragment;
import com.cray.software.justreminder.creator.DateFragment;
import com.cray.software.justreminder.creator.LocationFragment;
import com.cray.software.justreminder.creator.MailFragment;
import com.cray.software.justreminder.creator.MessageFragment;
import com.cray.software.justreminder.creator.MonthFragment;
import com.cray.software.justreminder.creator.OutLocationFragment;
import com.cray.software.justreminder.creator.PlacesFragment;
import com.cray.software.justreminder.creator.ShoppingFragment;
import com.cray.software.justreminder.creator.SkypeFragment;
import com.cray.software.justreminder.creator.TimerFragment;
import com.cray.software.justreminder.creator.WeekFragment;
import com.cray.software.justreminder.databases.NextBase;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.dialogs.ExtraPickerDialog;
import com.cray.software.justreminder.dialogs.LedColor;
import com.cray.software.justreminder.dialogs.SelectVolume;
import com.cray.software.justreminder.dialogs.TargetRadius;
import com.cray.software.justreminder.file_explorer.FileExploreActivity;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
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
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.spinner.SpinnerItem;
import com.cray.software.justreminder.spinner.TitleNavigationAdapter;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.ActionView;
import com.cray.software.justreminder.views.DateTimeView;
import com.cray.software.justreminder.views.RepeatView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Reminder creation activity.
 */
public class ReminderManager extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        MapListener, Dialogues.OnCategorySelectListener, DateTimeView.OnSelectListener,
        RepeatView.OnRepeatListener, ActionView.OnActionListener,
        ActionCallbacksExtended {

    private static final String HAS_SHOWCASE = "create_showcase";
    public static final int FILE_REQUEST = 556;
    public static final int REQUEST_EXTRA = 557;

    /**
     * Extra options views.
     */
    private FrameLayout repeatFrame;
    private RoboTextView repeatLabel;

    /**
     * General views.
     */
    private Toolbar toolbar;
    private Spinner spinner;
    private RoboEditText taskField;
    private RoboTextView category;
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
    private LatLng curPlace;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;
    private boolean isCalendar = false, isStock = false, isDark = false;
    private boolean hasTasks = false, isMessage, hasAction;

    private Type remControl = new Type(this);
    private JModel item;
    private BaseFragment baseFragment;

    private Handler handler = new Handler();

    private Tracker mTracker;
    private ColorSetter colorSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);
        String filePath = intent.getStringExtra(Constants.EDIT_PATH);
        int i = intent.getIntExtra(Constants.EDIT_WIDGET, 0);
        if (i != 0) Reminder.disable(this, id);

        int selection = SharedPrefs.getInstance(this).getInt(Prefs.LAST_USED_REMINDER);
        if (!Module.isPro() && selection == 12) selection = 0;

        colorSetter = new ColorSetter(ReminderManager.this);
        setTheme(colorSetter.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, colorSetter.colorPrimaryDark()));
        }
        setContentView(R.layout.create_edit_layout);
        setRequestedOrientation(colorSetter.getRequestOrientation());

        initFlags();
        initToolbar();
        initLimitView();
        setUpNavigation();
        initFab();
        findViewById(R.id.windowBackground).setBackgroundColor(colorSetter.getBackgroundStyle());
        loadDefaultGroup();

        spinner.setSelection(selection);
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

        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }

        new Handler().postDelayed(() -> ViewUtils.slideInDown(ReminderManager.this, toolbar), 500);
    }

    private void loadDefaultGroup() {
        GroupItem groupItem = GroupHelper.getInstance(this).getDefaultGroup();
        if (groupItem != null) {
            categoryId = groupItem.getUuId();
            category.setText(groupItem.getTitle());
        }
    }

    private void initFab() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(v -> save());
        mFab.setOnLongClickListener(v -> {
            mFab.hide();
            return false;
        });
    }

    private void initLimitView() {
        repeatFrame = (FrameLayout) findViewById(R.id.repeatFrame);
        repeatFrame.setBackgroundResource(colorSetter.getCardDrawableStyle());
        repeatLabel = (RoboTextView) findViewById(R.id.repeatLabel);
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
                new Handler().postDelayed(() -> {
                    ViewUtils.fadeOutAnimation(repeatLabel);
                    ViewUtils.fadeOutAnimation(repeatFrame);
                }, 500);
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Module.isLollipop()) toolbar.setElevation(R.dimen.toolbar_elevation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setVisibility(View.GONE);
        toolbar.setOnMenuItemClickListener(item1 -> {
                    switch (item1.getItemId()) {
                        case R.id.action_add:
                            save();
                            return true;
                        case R.id.action_custom_melody:
                            if(Permissions.checkPermission(ReminderManager.this,
                                    Permissions.READ_EXTERNAL)) {
                                startActivityForResult(new Intent(ReminderManager.this, FileExploreActivity.class),
                                        Constants.REQUEST_CODE_SELECTED_MELODY);
                            } else {
                                Permissions.requestPermission(ReminderManager.this, 330,
                                        Permissions.READ_EXTERNAL);
                            }
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
                });

        navContainer = (LinearLayout) findViewById(R.id.navContainer);
        spinner = (Spinner) findViewById(R.id.navSpinner);
        taskField = (RoboEditText) findViewById(R.id.task_message);
        taskField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (baseFragment != null)
                    baseFragment.setEventTask(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        changeExtra = (ImageButton) findViewById(R.id.changeExtra);
        insertVoice.setOnClickListener(v -> SuperUtil.startVoiceRecognitionActivity(ReminderManager.this,
                VOICE_RECOGNITION_REQUEST_CODE, true));
        changeExtra.setOnClickListener(v -> startActivityForResult(new Intent(ReminderManager.this, ExtraPickerDialog.class)
                        .putExtra("type", getType())
                        .putExtra("prefs", new int[]{voice, vibration, wake, unlock,
                                notificationRepeat, auto}), REQUEST_EXTRA));

        category = (RoboTextView) findViewById(R.id.category);
        category.setOnClickListener(v -> Dialogues.selectCategory(ReminderManager.this, categoryId, ReminderManager.this));
    }

    private void initFlags() {
        isCalendar = SharedPrefs.getInstance(this).getBoolean(Prefs.EXPORT_TO_CALENDAR);
        isStock = SharedPrefs.getInstance(this).getBoolean(Prefs.EXPORT_TO_STOCK);
        isDark = colorSetter.isDark();
        hasTasks = new GTasksHelper(this).isLinked();
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
            long time = item.getEventTime();
            Calendar calendar = Calendar.getInstance();
            if (time > 0) calendar.setTimeInMillis(time);
            else calendar.setTimeInMillis(System.currentTimeMillis());
            myDay = calendar.get(Calendar.DAY_OF_MONTH);
            myMonth = calendar.get(Calendar.MONTH);
            myYear = calendar.get(Calendar.YEAR);
            myHour = calendar.get(Calendar.HOUR_OF_DAY);
            myMinute = calendar.get(Calendar.MINUTE);
            if (radius == 0) radius = -1;
            if (catId != null && !catId.matches("")) categoryId = catId;
            if (categoryId != null && !categoryId.matches("")) {
                GroupItem groupItem = GroupHelper.getInstance(this).getGroup(categoryId);
                if (groupItem != null) {
                    category.setText(groupItem.getTitle());
                }
            }
        } else {
            Messages.toast(this, getString(R.string.something_went_wrong));
            finish();
        }

        if (type == null) {
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

    private void addFragment(int res, Fragment fragment) {
        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction ft = fragMan.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(res, fragment);
        ft.commitAllowingStateLoss();
    }

    /**
     * Set selecting reminder type spinner adapter.
     */
    private void setUpNavigation() {
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        if (isDark) {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_event_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call), R.drawable.ic_call_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.sms), R.drawable.ic_textsms_white_vector));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_place_white_vector));
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
            navSpinner.add(new SpinnerItem(getString(R.string.sms), R.drawable.ic_textsms_black_vector));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_place_black_vector));
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
            int isArchived = c.getInt(c.getColumnIndex(NextBase.DB_LIST));
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
     * Show simple date reminder creation layout.
     */
    private void attachDateReminder(){
        taskField.setHint(getString(R.string.remind_me));
        DateFragment fragment = DateFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_REMINDER, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show by day of month reminder creation layout.
     */
    private void attachMonthDay(){
        taskField.setHint(getString(R.string.remind_me));
        MonthFragment fragment = MonthFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_MONTHDAY, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show alarm clock reminder type creation layout.
     */
    private void attachWeekDayReminder(){
        taskField.setHint(getString(R.string.remind_me));
        WeekFragment fragment = WeekFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_WEEKDAY, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show timer reminder type creation layout.
     */
    private void attachTimeReminder(){
        taskField.setHint(getString(R.string.remind_me));
        TimerFragment fragment = TimerFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_TIME, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            exclusion = item.getExclusion().toString();
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show Skype reminder type creation layout.
     */
    private void attachSkype(){
        taskField.setHint(getString(R.string.remind_me));
        SkypeFragment fragment = SkypeFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_SKYPE, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show application reminder type creation layout.
     */
    private void attachApplication(){
        taskField.setHint(getString(R.string.remind_me));
        ApplicationFragment fragment = ApplicationFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_APPLICATION, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            selectedPackage = item.getAction().getTarget();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show call reminder type creation layout.
     */
    private void attachCall(){
        taskField.setHint(getString(R.string.remind_me));
        CallFragment fragment = CallFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_CALL, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show message reminder type creation layout.
     */
    private void attachMessage(){
        taskField.setHint(getString(R.string.message));
        MessageFragment fragment = MessageFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_MESSAGE, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show mail reminder type creation layout.
     */
    private void attachMail(){
        taskField.setHint(getString(R.string.subject));
        MailFragment fragment = MailFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_MAIL, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            repeatCode = item.getRecurrence().getRepeat();
            taskField.setText(item.getAction().getSubject());
            baseFragment.setEventTime(eventTime);
        }
    }

    @Override
    public void placeChanged(LatLng place) {
        curPlace = place;
    }

    @Override
    public void onBackClick() {
        ViewUtils.expand(toolbar);
    }

    @Override
    public void onZoomClick(boolean isFull) {
        if (isFull) {
            ViewUtils.collapse(toolbar);
        } else {
            ViewUtils.expand(toolbar);
        }
    }

    /**
     * Show location reminder type creation layout.
     */
    private void attachLocation() {
        taskField.setHint(getString(R.string.remind_me));
        LocationFragment fragment = LocationFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new LocationType(this, Constants.TYPE_LOCATION, fragment);
        if (item != null && isSame()) {
            String text = item.getSummary();
            eventTime = item.getStartTime();
            taskField.setText(text);
            JPlace jPlace = item.getPlace();
            radius = jPlace.getRadius();
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
    private void attachLocationOut() {
        taskField.setHint(getString(R.string.remind_me));
        OutLocationFragment fragment = OutLocationFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new LocationType(this, Constants.TYPE_LOCATION_OUT, fragment);
        if (item != null && isSame()) {
            String text = item.getSummary();
            eventTime = item.getStartTime();
            taskField.setText(text);
            JPlace jPlace = item.getPlace();
            radius = jPlace.getRadius();
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show places reminder type creation layout.
     */
    private void attachPLaces() {
        taskField.setHint(getString(R.string.remind_me));
        PlacesFragment fragment = PlacesFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new LocationType(this, Constants.TYPE_PLACES, fragment);
        if (item != null && isSame()) {
            String text = item.getSummary();
            eventTime = item.getStartTime();
            taskField.setText(text);
            baseFragment.setEventTime(eventTime);
        }
    }

    /**
     * Show shopping list reminder type creation layout.
     */
    private void attachShoppingList(){
        taskField.setHint(R.string.title);
        ShoppingFragment fragment = ShoppingFragment.newInstance(item, isDark, isCalendar, isStock, hasTasks);
        baseFragment = fragment;
        addFragment(R.id.layoutContainer, fragment);
        remControl = new DateType(this, Constants.TYPE_SHOPPING_LIST, fragment);
        if (item != null && isSame()) {
            eventTime = item.getEventTime();
            taskField.setText(item.getSummary());
            baseFragment.setEventTime(eventTime);
        }
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
                showSnackbar(getString(R.string.same_reminder_also_present));
                return;
            }
        }
        SharedPrefs.getInstance(this).putBoolean(Prefs.REMINDER_CHANGED, true);
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
                if (hasAction){
                    if (!isMessage)
                        type = Constants.TYPE_LOCATION_OUT_CALL;
                    else type = Constants.TYPE_LOCATION_OUT_MESSAGE;
                } else type = Constants.TYPE_LOCATION_OUT;
            } else if (remControl.getType().startsWith(Constants.TYPE_LOCATION)) {
                if (hasAction){
                    if (!isMessage)
                        type = Constants.TYPE_LOCATION_CALL;
                    else type = Constants.TYPE_LOCATION_MESSAGE;
                } else type = Constants.TYPE_LOCATION;
            } else type = remControl.getType();
        } else {
            if (isSkypeAttached()){
                type = ((SkypeFragment) baseFragment).getType();
            } else if (isApplicationAttached()){
                type = ((ApplicationFragment) baseFragment).getType();
            } else if (isWeekDayReminderAttached()){
                if (hasAction){
                    if (!isMessage) type = Constants.TYPE_WEEKDAY_CALL;
                    else type = Constants.TYPE_WEEKDAY_MESSAGE;
                } else type = Constants.TYPE_WEEKDAY;
            } else if (isMonthDayAttached()) {
                MonthFragment fragment = (MonthFragment) baseFragment;
                if (hasAction){
                    if (!isMessage){
                        if (fragment.isLast()) type = Constants.TYPE_MONTHDAY_CALL_LAST;
                        else type = Constants.TYPE_MONTHDAY_CALL;
                    } else {
                        if (fragment.isLast()) type = Constants.TYPE_MONTHDAY_MESSAGE_LAST;
                        else type = Constants.TYPE_MONTHDAY_MESSAGE;
                    }
                } else {
                    if (fragment.isLast()) type = Constants.TYPE_MONTHDAY_LAST;
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
        if (type == null) return null;
        ArrayList<JShopping> jShoppings = new ArrayList<>();
        if (isShoppingAttached()){
            ShoppingFragment fragment = (ShoppingFragment) baseFragment;
            if (fragment.getCount() == 0) {
                showSnackbar(getString(R.string.shopping_list_is_empty));
                return null;
            } else {
                for (ShoppingList shoppingList : fragment.getData()) {
                    String title = shoppingList.getTitle();
                    String uuid = shoppingList.getUuId();
                    long time = shoppingList.getTime();
                    int status = shoppingList.getIsChecked();
                    int deleted = shoppingList.getStatus();
                    jShoppings.add(new JShopping(title, status, uuid, time, deleted));
                }
            }
            if (!fragment.isShoppingReminder()) {
                myDay = 0;
                myMonth = 0;
                myYear = 0;
                myHour = 0;
                myMinute = 0;
            }
        }
        ArrayList<Integer> weekdays = new ArrayList<>();
        if (isWeekDayReminderAttached()) {
            weekdays = ((WeekFragment) baseFragment).getDays();
            if (!IntervalUtil.isWeekday(weekdays)) {
                showSnackbar(getString(R.string.you_dont_select_any_day));
                return null;
            }
        }
        String task = taskField.getText().toString().trim();
        if (!type.contains(Constants.TYPE_CALL) && !type.matches(Constants.TYPE_SHOPPING_LIST)
                && !type.contains(Constants.TYPE_MAIL)) {
            if (task.matches("")) {
                showSnackbar(getString(R.string.task_summary_is_empty));
                return null;
            }
        }
        String number = getNumber();
        if (type.contains(Constants.TYPE_MESSAGE) || type.contains(Constants.TYPE_CALL)) {
            if (!checkNumber(number)) return null;
        }
        if (isApplicationAttached()) {
            if (type.matches(Constants.TYPE_APPLICATION)) {
                number = selectedPackage;
                if (number == null) {
                    showSnackbar(getString(R.string.you_dont_select_application));
                    return null;
                }
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                number = baseFragment.getNumber();
                if (number == null || number.matches("") || number.matches(".*https?://")) {
                    showSnackbar(getString(R.string.you_dont_insert_link));
                    return null;
                }
                if (!number.startsWith("http://") && !number.startsWith("https://"))
                    number = "http://" + number;
            }
        }
        String subjectString = null;
        if (isMailAttached()) {
            String email = baseFragment.getNumber();
            if (email == null || email.matches("") || !email.matches(".*@.*..*")) {
                showSnackbar(getString(R.string.email_is_incorrect));
                return null;
            } else number = email;

            String subString = baseFragment.getMessage();
            if (subString == null || subString.matches("")) {
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
            LatLng dest;
            if (curPlace == null) {
                showSnackbar(getString(R.string.you_dont_select_place));
                return null;
            } else {
                dest = curPlace;
            }

            if (isLocationAttached()) {
                LocationFragment fragment = (LocationFragment) baseFragment;
                if (fragment.isDelayed()) {
                    myDay = 0;
                    myMonth = 0;
                    myYear = 0;
                    myHour = 0;
                    myMinute = 0;
                }
                style = fragment.getMarker();
            }
            if (isLocationOutAttached()) {
                OutLocationFragment fragment = (OutLocationFragment) baseFragment;
                if (fragment.isDelayed()) {
                    myDay = 0;
                    myMonth = 0;
                    myYear = 0;
                    myHour = 0;
                    myMinute = 0;
                }
                style = fragment.getMarker();
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

            places = ((PlacesFragment) baseFragment).getPlaces();
            if (places == null || places.size() == 0) {
                showSnackbar(getString(R.string.you_dont_select_place));
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
            timeAfter = SuperUtil.getAfterTime(((TimerFragment) baseFragment).getTimeString());
            if (timeAfter == 0) {
                showSnackbar(getString(R.string.you_dont_insert_timer_time));
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
        JExport jExport = new JExport().setGtasks(gTaskSync).setCalendar(calendarSync);
        JPlace jPlace = new JPlace(latitude, longitude, radius, style);

        Log.d("----RECORD_TIME-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d("----EVENT_TIME-----", TimeUtil.getFullDateTime(startTime, true));

        return new JModel(task, type, categoryId, uuId, startTime, startTime, 0, vibration,
                notificationRepeat, voice, wake, unlock, jExclusion, jLed, jMelody,
                jRecurrence, jAction, jExport, jPlace, null, places, jShoppings);
    }

    /**
     * Get export to calendar code for reminder.
     * @return Integer
     */
    private int getExportCode() {
        if (isStock || isCalendar){
            return baseFragment.getCalendar() ? 1 : 0;
        } else return 0;
    }

    /**
     * Get Google Tasks export code for reminder.
     * @return Integer
     */
    private int getSyncCode() {
        return baseFragment.getTasks() ? 1 : 0;
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
        return baseFragment.getNumber();
    }

    /**
     * Check if number inserted.
     * @return Boolean
     */
    private boolean checkNumber(String number){
        if (number == null || number.matches("")) {
            showSnackbar(getString(R.string.you_dont_insert_number));
            return false;
        } else return true;
    }

    @Override
    public void onBackPressed() {
        if (baseFragment != null) {
            if (baseFragment.onBackPressed()) return;
        }

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
                eventTime = c.getLong(c.getColumnIndex(NextBase.EVENT_TIME));
                if (isDone != 1 && isArchive != 1) {
                    if (type.contains(Constants.TYPE_LOCATION) && eventTime > 0) {
                        new PositionDelayReceiver().setDelay(ReminderManager.this, id);
                    } else new AlarmReceiver().enableReminder(ReminderManager.this, id);
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

    /**
     * Show reminder layout.
     * @param position spinner position.
     */
    private void switchIt(int position){
        radius = -1;
        selectedPackage = null;
        switch (position){
            case 0:
                attachDateReminder();
                break;
            case 2:
                if (Permissions.checkPermission(ReminderManager.this, Permissions.CALL_PHONE,
                        Permissions.SEND_SMS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
                    attachWeekDayReminder();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 113, Permissions.CALL_PHONE,
                            Permissions.READ_CONTACTS, Permissions.SEND_SMS, Permissions.READ_CALLS);
                }
                break;
            case 1:
                attachTimeReminder();
                break;
            case 3:
                if (Permissions.checkPermission(ReminderManager.this, Permissions.CALL_PHONE, Permissions.READ_CALLS)) {
                    attachCall();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 109, Permissions.CALL_PHONE, Permissions.READ_CALLS);
                }
                break;
            case 4:
                if (Permissions.checkPermission(ReminderManager.this, Permissions.SEND_SMS, Permissions.READ_CALLS)) {
                    attachMessage();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 108, Permissions.SEND_SMS, Permissions.READ_CALLS);
                }
                break;
            case 5:
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (Permissions.checkPermission(ReminderManager.this, Permissions.ACCESS_FINE_LOCATION,
                            Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
                        attachLocation();
                    } else {
                        Permissions.requestPermission(ReminderManager.this, 105, Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE, Permissions.SEND_SMS,
                                Permissions.READ_CONTACTS, Permissions.READ_CALLS);
                    }
                } else spinner.setSelection(0);
                break;
            case 6:
                if (SuperUtil.isSkypeClientInstalled(ReminderManager.this)) {
                    attachSkype();
                } else {
                    spinner.setSelection(0);
                    SuperUtil.installSkype(ReminderManager.this);
                }
                break;
            case 7:
                attachApplication();
                break;
            case 8:
                if (Permissions.checkPermission(ReminderManager.this, Permissions.CALL_PHONE,
                        Permissions.SEND_SMS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
                    attachMonthDay();
                } else {
                    Permissions.requestPermission(ReminderManager.this, 114, Permissions.CALL_PHONE,
                            Permissions.READ_CONTACTS, Permissions.SEND_SMS, Permissions.READ_CALLS);
                }
                break;
            case 9:
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (Permissions.checkPermission(ReminderManager.this, Permissions.ACCESS_FINE_LOCATION,
                            Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
                        attachLocationOut();
                    } else {
                        Permissions.requestPermission(ReminderManager.this, 106,
                                Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION,
                                Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
                    }
                } else spinner.setSelection(0);
                break;
            case 10:
                attachShoppingList();
                break;
            case 11:
                attachMail();
                break;
            case 12:
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
        SharedPrefs.getInstance(this).putInt(Prefs.LAST_USED_REMINDER, position);
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
            case 330:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(ReminderManager.this, FileExploreActivity.class),
                            Constants.REQUEST_CODE_SELECTED_MELODY);
                }
                break;
            case 331:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(ReminderManager.this, FileExploreActivity.class)
                            .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
                if (isCallAttached()) ((CallFragment) baseFragment).setNumber(number);
                if (isMessageAttached()) ((MessageFragment) baseFragment).setNumber(number);
                if (isWeekDayReminderAttached()) ((WeekFragment) baseFragment).setNumber(number);
                if (isMonthDayAttached()) ((MonthFragment) baseFragment).setNumber(number);
                if (isLocationAttached()) ((LocationFragment) baseFragment).setNumber(number);
                if (isLocationOutAttached()) ((OutLocationFragment) baseFragment).setNumber(number);
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
                    showSnackbar(String.format(getString(R.string.melody_x), musicFile.getName()),
                            R.string.cancel, v -> {
                                melody = null;
                            });
                }
            }
        }

        if (requestCode == FILE_REQUEST) {
            Log.d(Constants.LOG_TAG, "Request file");
            if (resultCode == RESULT_OK){
                Log.d(Constants.LOG_TAG, "Request OK");
                attachment = data.getStringExtra(Constants.FILE_PICKED);
                if (attachment != null) {
                    File file = new File(attachment);
                    ((MailFragment) baseFragment).setFileName(file.getPath());
                    showSnackbar(String.format(getString(R.string.file_x_attached), file.getName()),
                            R.string.cancel, v -> {
                                attachment = null;
                                ((MailFragment) baseFragment).setFileName(null);
                            });
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == RESULT_OK){
                radius = data.getIntExtra(Constants.SELECTED_RADIUS, -1);
                if (radius != -1) {
                    String str = String.format(getString(R.string.radius_x_meters), radius);
                    showSnackbar(str, R.string.cancel, v -> {
                        radius = -1;
                        if (isLocationAttached()) {
                            ((LocationFragment) baseFragment).recreateMarkers(radius);
                        }
                        if (isLocationOutAttached()) {
                            ((OutLocationFragment) baseFragment).recreateMarkers(radius);
                            ((OutLocationFragment) baseFragment).setPointRadius(radius);
                        }
                    });
                    if (isLocationAttached()) {
                        ((LocationFragment) baseFragment).recreateMarkers(radius);
                    }
                    if (isLocationOutAttached()) {
                        ((OutLocationFragment) baseFragment).recreateMarkers(radius);
                        ((OutLocationFragment) baseFragment).setPointRadius(radius);
                    }

                    if (isPlacesAttached()) ((PlacesFragment) baseFragment).recreateMarkers(radius);
                }
            }
        }

        if (requestCode == 1111) {
            if (resultCode == RESULT_OK){
                exclusion = data.getStringExtra("excl");
                ((TimerFragment) baseFragment).setExclusion(exclusion);
                showSnackbar(getString(R.string.exclusion_added), R.string.cancel, v -> {
                    exclusion = null;
                    ((TimerFragment) baseFragment).setExclusion(null);
                });
            }
        }

        if (requestCode == REQUEST_EXTRA) {
            if (resultCode == RESULT_OK){
                int[] array = data.getIntArrayExtra("prefs");
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
                showSnackbar(str, R.string.cancel, v -> {
                    ledColor = -1;
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
                ((ApplicationFragment) baseFragment).setApplication(title);
            }
        }

        if (requestCode == Constants.REQUEST_CODE_VOLUME) {
            if (resultCode == RESULT_OK){
                volume = data.getIntExtra(Constants.SELECTED_VOLUME, -1);
                String str = String.format(getString(R.string.selected_loudness_x_for_reminder), volume);
                showSnackbar(str, R.string.cancel, v -> {
                    volume = -1;
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_menu, menu);
        if (isLocationAttached()){
            menu.getItem(2).setVisible(true);
        } else {
            menu.getItem(2).setVisible(false);
        }
        if (isLocationAttached() || isLocationOutAttached()
                || isShoppingAttached() || isPlacesAttached()){
            menu.getItem(4).setVisible(false);
        } else {
            menu.getItem(4).setVisible(true);
        }
        if (Module.isPro() && SharedPrefs.getInstance(this).getBoolean(Prefs.LED_STATUS)){
            menu.getItem(3).setVisible(true);
        } else {
            menu.getItem(3).setVisible(false);
        }
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isLocationAttached()){
            menu.getItem(2).setVisible(true);
        } else {
            menu.getItem(2).setVisible(false);
        }
        if (isLocationAttached() || isLocationOutAttached()
                || isShoppingAttached() || isPlacesAttached()){
            menu.getItem(4).setVisible(false);
        } else {
            menu.getItem(4).setVisible(true);
        }
        if (Module.isPro() && SharedPrefs.getInstance(this).getBoolean(Prefs.LED_STATUS)){
            menu.getItem(3).setVisible(true);
        } else {
            menu.getItem(3).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showShowcase();
        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Create reminder screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    public void showShowcase() {
        if (!SharedPrefs.getInstance(this).getBoolean(HAS_SHOWCASE)) {
            SharedPrefs.getInstance(this).putBoolean(HAS_SHOWCASE, true);
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
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
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
        hasAction = b;
    }

    @Override
    public void onTypeChange(boolean type) {
        if (type) taskField.setHint(getString(R.string.message));
        else taskField.setHint(getString(R.string.remind_me));
        isMessage = type;
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
}