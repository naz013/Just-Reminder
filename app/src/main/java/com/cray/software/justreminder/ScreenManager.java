package com.cray.software.justreminder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.cray.software.justreminder.activities.AddPlace;
import com.cray.software.justreminder.activities.Help;
import com.cray.software.justreminder.activities.NewTemplate;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.GetTasksListsAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.ActionPickerDialog;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.enums.QuickReturnViewType;
import com.cray.software.justreminder.feedback.SendReportActivity;
import com.cray.software.justreminder.fragments.BackupsFragment;
import com.cray.software.justreminder.fragments.EventsFragment;
import com.cray.software.justreminder.fragments.GeolocationFragment;
import com.cray.software.justreminder.fragments.GroupsFragment;
import com.cray.software.justreminder.fragments.NavigationDrawerFragment;
import com.cray.software.justreminder.fragments.NotesFragment;
import com.cray.software.justreminder.fragments.PlacesFragment;
import com.cray.software.justreminder.fragments.TasksFragment;
import com.cray.software.justreminder.fragments.TemplatesFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.Recognize;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.notes.NotesBase;
import com.cray.software.justreminder.notes.NotesManager;
import com.cray.software.justreminder.reminder.ActiveFragment;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.ReminderManager;
import com.cray.software.justreminder.reminder.TrashFragment;
import com.cray.software.justreminder.settings.SettingsActivity;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.views.ReturnScrollListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;
import com.hexrain.flextcal.FlextCal;
import com.hexrain.flextcal.FlextListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ScreenManager extends AppCompatActivity implements NavigationCallbacks {

    private Toolbar toolbar;
    private FloatingEditText quickNote;
    private CardView noteCard, noteStatusCard, noteReminderCard;
    private TextView buttonYes;
    private TextView buttonNo;
    private TextView buttonReminderYes;
    private TextView buttonReminderNo;
    private FloatingActionButton mFab;

    private ColorSetter cSetter = new ColorSetter(this);
    private SharedPrefs mPrefs = new SharedPrefs(this);

    public static final String FRAGMENT_ACTIVE = "fragment_active";
    public static final String FRAGMENT_ARCHIVE = "fragment_archive";
    public static final String FRAGMENT_NOTE = "fragment_notes";
    public static final String FRAGMENT_TASKS = "fragment_tasks";
    public static final String FRAGMENT_GROUPS = "fragment_groups";
    public static final String FRAGMENT_PLACES = "fragment_places";
    public static final String FRAGMENT_LOCATIONS = "fragment_locations";
    public static final String FRAGMENT_BACKUPS = "fragment_backups";
    public static final String FRAGMENT_TEMPLATES = "fragment_templates";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";
    public static final String ACTION_CALENDAR = "action_calendar";
    public static final String FRAGMENT_EVENTS = "fragment_events";
    public static final String HELP = "help";
    public static final String REPORT = "feedback_report";
    public static final String MARKET = "market";
    public static final String VOICE_RECOGNIZER = "sync_reminder";
    public static final String TASKS_AUTHORIZATION = "authorize";
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    private String mTitle;
    private String mTag;
    private String accountName;
    private long listId;
    private long dateMills;
    private boolean doubleBackToExitPressedOnce = false;
    private int lastEventPosition = -1;

    private Context ctx = this;
    private Activity a = this;
    private Date eventsDate = null;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.activity_screen_manager);
        setRequestedOrientation(cSetter.getRequestOrientation());

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getColor(cSetter.colorPrimaryDark()));
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(R.string.tasks);
        mTitle = getTitle().toString();

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        quickNote = (FloatingEditText) findViewById(R.id.quickNote);

        noteCard = (CardView) findViewById(R.id.noteCard);
        noteCard.setCardBackgroundColor(cSetter.getCardStyle());
        noteStatusCard = (CardView) findViewById(R.id.noteStatusCard);
        noteReminderCard = (CardView) findViewById(R.id.noteReminderCard);
        noteStatusCard.setCardBackgroundColor(cSetter.getCardStyle());
        noteReminderCard.setCardBackgroundColor(cSetter.getCardStyle());

        buttonYes = (TextView) findViewById(R.id.buttonYes);
        TextView buttonSave = (TextView) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        buttonNo = (TextView) findViewById(R.id.buttonNo);
        buttonReminderYes = (TextView) findViewById(R.id.buttonReminderYes);
        buttonReminderNo = (TextView) findViewById(R.id.buttonReminderNo);

        initButton();

        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        if (mPrefs.loadBoolean(Prefs.UI_CHANGED)) {
            onItemSelected(mPrefs.loadPrefs(Prefs.LAST_FRAGMENT));
            mPrefs.saveBoolean(Prefs.UI_CHANGED, false);
        }

        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }

    private void initButton() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNoteVisible()) {
                    ViewUtils.hideReveal(noteCard);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScreenManager.this, ReminderManager.class));
                    }
                }, 150);
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isNoteVisible()) {
                    ViewUtils.showReveal(noteCard);
                } else {
                    quickNote.setText("");
                    quickNote.setError(null);

                    ViewUtils.hideReveal(noteCard);
                }
                return true;
            }
        });
    }

    private void reloadButton(){
        mPrefs = new SharedPrefs(this);
        if (mTag.matches(FRAGMENT_EVENTS) || mTag.matches(ACTION_CALENDAR)){
            mFab.setVisibility(View.VISIBLE);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dateMills == 0) dateMills = System.currentTimeMillis();
                    startActivity(new Intent(ScreenManager.this, ActionPickerDialog.class)
                            .putExtra("date", dateMills));
                }
            });
        } else if (mTag.matches(FRAGMENT_BACKUPS)){
            mFab.setVisibility(View.GONE);
        } else {
            mFab.setVisibility(View.GONE);
            if (mTag.matches(FRAGMENT_ARCHIVE) || mTag.matches(FRAGMENT_ACTIVE) ||
                    mTag.matches(FRAGMENT_LOCATIONS)){
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        collapseViews();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(ScreenManager.this, ReminderManager.class));
                            }
                        }, 150);
                    }
                });
                mFab.setVisibility(View.VISIBLE);
            } else if (mTag.matches(FRAGMENT_TASKS)){
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (new GTasksHelper(ScreenManager.this).isLinked()) {
                            collapseViews();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(ScreenManager.this, TaskManager.class)
                                            .putExtra(Constants.ITEM_ID_INTENT, listId)
                                            .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
                                }
                            }, 150);
                        } else {
                            showSnackbar(getString(R.string.can_not_connect));
                        }
                    }
                });
                mFab.setVisibility(View.VISIBLE);
            } else if (mTag.matches(FRAGMENT_NOTE)){
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        collapseViews();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(ScreenManager.this, NotesManager.class));
                            }
                        }, 150);
                    }
                });
                mFab.setVisibility(View.VISIBLE);
            } else if (mTag.matches(FRAGMENT_GROUPS)){
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        collapseViews();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(ScreenManager.this, CategoryManager.class));
                            }
                        }, 150);
                    }
                });
                mFab.setVisibility(View.VISIBLE);
            } else if (mTag.matches(FRAGMENT_PLACES)){
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        collapseViews();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (LocationUtil.checkGooglePlayServicesAvailability(ScreenManager.this)) {
                                    startActivity(new Intent(ScreenManager.this, AddPlace.class));
                                }
                            }
                        }, 150);
                    }
                });
                mFab.setVisibility(View.VISIBLE);
            } else if (mTag.matches(FRAGMENT_TEMPLATES)){
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        collapseViews();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(ScreenManager.this, NewTemplate.class));
                            }
                        }, 150);
                    }
                });
                mFab.setVisibility(View.VISIBLE);
            }
        }
    }

    private void collapseViews() {
        if (isNoteVisible()) {
            ViewUtils.hideReveal(noteCard);
        }
    }

    @Override
    public void onTitleChanged(String string) {
        if (string != null) {
            mTitle = string;
            toolbar.setTitle(mTitle);
        }
    }

    @Override
    public void onListIdChanged(long listId) {
        this.listId = listId;
    }

    private ReturnScrollListener listener;

    @Override
    public void onListChanged(RecyclerView list) {
        if (list != null){
            if (listener != null){
                list.removeOnScrollListener(listener);
            }

            mPrefs = new SharedPrefs(this);

            listener = new ReturnScrollListener.Builder(QuickReturnViewType.FOOTER)
                    .footer(mFab)
                    .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                    .isSnappable(true)
                    .build();
            if (Module.isLollipop()){
                list.addOnScrollListener(listener);
            } else {
                list.setOnScrollListener(listener);
            }
        }
    }

    @Override
    public void onDateChanged(long dateMills, int position) {
        if (dateMills != 0) {
            this.dateMills = dateMills;
        }
        lastEventPosition = position;
    }

    @Override
    public void isDrawerOpen(boolean isOpen) {

    }

    @Override
    public void onUiChanged(int colorPrimary, int colorPrimaryDark, int colorAccent) {
        if (colorPrimary != 0){
            toolbar.setBackgroundColor(colorPrimary);
        }
        if (colorPrimaryDark != 0){
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(colorPrimaryDark);
            }
        }
        if (colorPrimary != 0 && colorAccent != 0) {
            mFab.setBackgroundTintList(ViewUtils.getFabState(this, colorAccent, colorPrimary));
        }
    }

    private void replace(Fragment fragment, String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
        mTag = tag;
        mPrefs.savePrefs(Prefs.LAST_FRAGMENT, tag);
    }

    @Override
    public void onItemSelected(String tag) {
        // update the main content by replacing fragments
        if (tag != null) {
            restoreUi();
            if (tag.matches(FRAGMENT_ACTIVE)) {
                replace(ActiveFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_ARCHIVE)) {
                replace(TrashFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_NOTE)) {
                replace(NotesFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_GROUPS)) {
                replace(GroupsFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_PLACES)) {
                replace(PlacesFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_TEMPLATES)) {
                replace(TemplatesFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_TASKS) && new GTasksHelper(this).isLinked()) {
                replace(TasksFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_BACKUPS)) {
                replace(BackupsFragment.newInstance(), tag);
            } else if (tag.matches(FRAGMENT_LOCATIONS)) {
                if (LocationUtil.checkGooglePlayServicesAvailability(this)) {
                    replace(GeolocationFragment.newInstance(), tag);
                }
            } else if (tag.matches(ACTION_CALENDAR)) {
                showMonth();
                mPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 1);
                mTag = tag;
                mPrefs.savePrefs(Prefs.LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_EVENTS)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                if (eventsDate != null) {
                    cal.setTime(eventsDate);
                }
                replace(EventsFragment.newInstance(cal.getTimeInMillis(), lastEventPosition), tag);
                mPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
            } else if (tag.matches(HELP)) {
                startActivity(new Intent(this, Help.class));
            } else if (tag.matches(REPORT)) {
                startActivity(new Intent(this, SendReportActivity.class));
            } else if (tag.matches(MARKET)) {
                marketDialog().show();
            } else if (tag.matches(FRAGMENT_SETTINGS)) {
                Intent intentS = new Intent(this, SettingsActivity.class);
                startActivity(intentS);
            } else if (tag.matches(VOICE_RECOGNIZER)) {
                SuperUtil.startVoiceRecognitionActivity(ScreenManager.this,
                        VOICE_RECOGNITION_REQUEST_CODE, false);
                if (LocationUtil.isGooglePlayServicesAvailable(this)) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Voice control")
                            .setAction("Main")
                            .setLabel("Main")
                            .build());
                }
            } else if (tag.matches(TASKS_AUTHORIZATION)) {
                if (!new GTasksHelper(this).isLinked()) {
                    if (Permissions.checkPermission(ScreenManager.this,
                            Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                            Permissions.WRITE_EXTERNAL)) {
                        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                                new String[]{"com.google"}, false, null, null, null, null);
                        startActivityForResult(intent, REQUEST_AUTHORIZATION);
                    } else {
                        Permissions.requestPermission(ScreenManager.this, 103,
                                Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                                Permissions.WRITE_EXTERNAL);
                    }
                }
            } else {
                replace(ActiveFragment.newInstance(), tag);
            }
        } else {
            replace(ActiveFragment.newInstance(), FRAGMENT_ACTIVE);
        }
    }

    @Override
    public void showSnackbar(int message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSnackbar(String message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    @Override
    public void showSnackbar(int message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    public void onSectionAttached(String tag) {
        if(tag != null) {
            if (tag.matches(FRAGMENT_ACTIVE)) {
                mTitle = getString(R.string.tasks);
            } else if (tag.matches(FRAGMENT_ARCHIVE)) {
                mTitle = getString(R.string.trash);
            } else if (tag.matches(ACTION_CALENDAR)) {
                mTitle = getString(R.string.calendar);
            } else if (tag.matches(FRAGMENT_EVENTS)) {
                mTitle = getString(R.string.events);
            } else if (tag.matches(FRAGMENT_NOTE)) {
                mTitle = getString(R.string.notes);
            } else if (tag.matches(FRAGMENT_GROUPS)) {
                mTitle = getString(R.string.groups);
            } else if (tag.matches(FRAGMENT_PLACES)) {
                mTitle = getString(R.string.places);
            } else if (tag.matches(FRAGMENT_TEMPLATES)) {
                mTitle = getString(R.string.messages);
            } else if (tag.matches(FRAGMENT_LOCATIONS)) {
                mTitle = getString(R.string.directions);
            } else if (tag.matches(FRAGMENT_BACKUPS)) {
                mTitle = getString(R.string.backup_files);
            }
            if (mTitle == null) mTitle = getString(R.string.tasks);
            if (toolbar != null) toolbar.setTitle(mTitle);
        }
    }

    private void restoreUi(){
        int colorPrimary = cSetter.getColor(cSetter.colorPrimary());
        int colorAccent = cSetter.getColor(cSetter.colorAccent());
        int colorDark = cSetter.getColor(cSetter.colorPrimaryDark());
        toolbar.setBackgroundColor(colorPrimary);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(colorDark);
        }
        if (colorPrimary != 0 && colorAccent != 0) {
            mFab.setBackgroundTintList(ViewUtils.getFabState(this, colorAccent, colorPrimary));
        }
    }

    private void showMonth(){
        FlextCal calendarView = new FlextCal();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        args.putInt(FlextCal.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(FlextCal.YEAR, cal.get(Calendar.YEAR));
        mPrefs = new SharedPrefs(this);
        if (mPrefs.loadInt(Prefs.START_DAY) == 0) {
            args.putInt(FlextCal.START_DAY_OF_WEEK, FlextCal.SUNDAY);
        } else {
            args.putInt(FlextCal.START_DAY_OF_WEEK, FlextCal.MONDAY);
        }
        args.putBoolean(FlextCal.ENABLE_IMAGES, mPrefs.loadBoolean(Prefs.CALENDAR_IMAGE));
        args.putBoolean(FlextCal.DARK_THEME, cSetter.isDark());
        calendarView.setArguments(args);
        calendarView.setBackgroundForToday(cSetter.getColor(cSetter.colorCurrentCalendar()));
        replace(calendarView, mTag);

        final FlextListener listener = new FlextListener() {

            @Override
            public void onClickDate(Date date, View view) {
                eventsDate = date;
                onItemSelected(FRAGMENT_EVENTS);
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long dateMills = calendar.getTimeInMillis();
                startActivity(new Intent(ScreenManager.this, ActionPickerDialog.class)
                        .putExtra("date", dateMills));
            }

            @Override
            public void onMonthChanged(int month, int year) {

            }

            @Override
            public void onCaldroidViewCreated() {
            }

        };

        calendarView.setCaldroidListener(listener);
        calendarView.refreshView();

        boolean isReminder = mPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR);
        boolean isFeature = mPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        calendarView.setEvents(new ReminderDataProvider(this, isReminder, isFeature).getEvents());
        replace(calendarView, mTag);

        mPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 1);
        mTitle = getString(R.string.calendar);
        toolbar.setTitle(mTitle);
        invalidateOptionsMenu();
    }

    protected Dialog marketDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.buy_pro))
                .setMessage(getString(R.string.pro_advantages) + "\n" +
                        getString(R.string.different_settings_for_birthdays) + "\n" +
                        getString(R.string.additional_reminder) + "\n" +
                        getString(R.string._led_notification_) + "\n" +
                        getString(R.string.led_color_for_each_reminder) + "\n" +
                        getString(R.string.styles_for_marker) + "\n" +
                        getString(R.string.option_for_image_blurring) + "\n" +
                        getString(R.string.additional_app_themes))
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + "com.cray.software.justreminderpro"));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTag != null && mTag.matches(ACTION_CALENDAR)) {
            getMenuInflater().inflate(R.menu.calendar_menu, menu);
            menu.findItem(R.id.action_month).setVisible(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            if (eventsDate != null) {
                calendar.setTime(eventsDate);
            }
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            menu.findItem(R.id.action_day).setTitle(day + "/" + (month + 1) + "/" + year);
        }
        toolbar.setTitle(mTitle);
        reloadButton();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_day:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                eventsDate = calendar.getTime();
                onItemSelected(FRAGMENT_EVENTS);
                return true;
            case R.id.action_voice:
                SuperUtil.startVoiceRecognitionActivity(ScreenManager.this,
                        VOICE_RECOGNITION_REQUEST_CODE, false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPrefs sPrefs = new SharedPrefs(this);
        if (sPrefs.loadBoolean(Prefs.UI_CHANGED)) recreate();

        setRequestedOrientation(cSetter.getRequestOrientation());
        showRate();

        if (Module.isPro() && !sPrefs.loadBoolean(Prefs.THANKS_SHOWN) && hasChanges()) {
            thanksDialog().show();
        }

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cSetter.getColor(cSetter.colorPrimaryDark()));
        }

        if (mTag != null) onItemSelected(mTag);
        if (sPrefs.loadBoolean(Prefs.STATUS_BAR_NOTIFICATION))
            new Notifier(this).recreatePermanent();
        isChangesShown();
        new DelayedAsync(this, null).execute();

        if (LocationUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Main activity");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new UpdatesHelper(this).updateWidget();
    }

    private void showChanges() {
        startActivity(new Intent(this, ChangeDialog.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void isChangesShown() {
        SharedPrefs sPrefs = new SharedPrefs(this);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = null;
        if (pInfo != null) version = pInfo.versionName;
        boolean ranBefore = sPrefs.loadVersionBoolean(version);
        if (!ranBefore) {
            sPrefs.saveVersionBoolean(version);
            showChanges();
        }
    }

    private boolean hasChanges() {
        SharedPrefs sPrefs = new SharedPrefs(this);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = null;
        if (pInfo != null) version = pInfo.versionName;
        return sPrefs.loadVersionBoolean(version);
    }

    private void showRate(){
        SharedPrefs sPrefs = new SharedPrefs(this);
        if (sPrefs.isString(Prefs.RATE_SHOW)) {
            if (!sPrefs.loadBoolean(Prefs.RATE_SHOW)) {
                int counts = sPrefs.loadInt(Prefs.APP_RUNS_COUNT);
                if (counts < 10) {
                    sPrefs.saveInt(Prefs.APP_RUNS_COUNT, counts + 1);
                } else {
                    sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
                    Dialogues.rateDialog(this);
                }
            }
        } else {
            sPrefs.saveBoolean(Prefs.RATE_SHOW, false);
            sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
        }
    }

    protected Dialog thanksDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.thank_you_for_buying_pro)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new SharedPrefs(ScreenManager.this).saveBoolean(Prefs.THANKS_SHOWN, true);
                    }
                })
                .setCancelable(false)
                .create();
    }

    private void saveNote() {
        final String note = quickNote.getText().toString();
        if (note.matches("")) {
            quickNote.setError(getString(R.string.must_be_not_empty));
            return;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat full24Format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String date = full24Format.format(calendar1.getTime());

        String uuID = SyncHelper.generateID();
        NotesBase db = new NotesBase(ScreenManager.this);
        db.open();
        int color = new Random().nextInt(15);
        final long id;
        if (mPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            id = db.saveNote(SyncHelper.encrypt(note), date, color, uuID, null, 5);
        } else {
            id = db.saveNote(note, date, color, uuID, null, 5);
        }
        db.close();

        new UpdatesHelper(ScreenManager.this).updateNotesWidget();

        quickNote.setText("");
        quickNote.setError(null);

        ViewUtils.hideReveal(noteCard);

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(quickNote.getWindowToken(), 0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isNoteVisible()) {
                    askNotification(note, id);
                }
            }
        }, 300);

        if (mTag.matches(FRAGMENT_NOTE) || mTag.matches(FRAGMENT_ACTIVE)) {
            onItemSelected(mTag);
        }
    }

    private void askNotification(final String note, final long id){
        mPrefs = new SharedPrefs(ScreenManager.this);
        ViewUtils.showReveal(noteStatusCard);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notifier(ScreenManager.this).showNoteNotification(note, id);
                ViewUtils.hideReveal(noteStatusCard);
                if (mPrefs.loadBoolean(Prefs.QUICK_NOTE_REMINDER)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            askReminder(note, id);
                        }
                    }, 300);
                }
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideReveal(noteStatusCard);
                if (mPrefs.loadBoolean(Prefs.QUICK_NOTE_REMINDER)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            askReminder(note, id);
                        }
                    }, 300);
                }
            }
        });
    }

    private void askReminder(final String note, final long noteId){
        mPrefs = new SharedPrefs(ScreenManager.this);
        ViewUtils.showReveal(noteReminderCard);

        buttonReminderYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideReveal(noteReminderCard);
                DataBase db = new DataBase(ScreenManager.this);
                if (!db.isOpen()) {
                    db.open();
                }
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(System.currentTimeMillis());
                Cursor cf = new DataBase(ScreenManager.this).open().queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) {
                    cf.close();
                }
                db.close();
                long after = new SharedPrefs(ScreenManager.this).loadInt(Prefs.QUICK_NOTE_REMINDER_TIME) * 1000 * 60;
                long due = calendar1.getTimeInMillis() + after;
                JModel jModel = new JModel(note, Constants.TYPE_REMINDER, categoryId,
                        SyncHelper.generateID(), due, due, null, null, null);
                long remId = new DateType(ScreenManager.this, Constants.TYPE_REMINDER).save(jModel);
                NotesBase base = new NotesBase(ScreenManager.this);
                base.open();
                base.linkToReminder(noteId, remId);
                base.close();
                if (mTag.matches(FRAGMENT_NOTE) || mTag.matches(FRAGMENT_ACTIVE)) {
                    onItemSelected(mTag);
                }
            }
        });

        buttonReminderNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideReveal(noteReminderCard);
            }
        });
    }

    private boolean isNoteVisible(){
        return noteCard.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onBackPressed() {
        if (isNoteVisible()){
            quickNote.setText("");
            quickNote.setError(null);
            ViewUtils.hideReveal(noteCard);

            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(quickNote.getWindowToken(), 0);
            return;
        }
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Messages.toast(ScreenManager.this, getString(R.string.press_again_to_exit));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(ScreenManager.this, ProgressDialog.STYLE_SPINNER);
                progressDlg.setMax(100);
                progressDlg.setMessage(getString(R.string.trying_to_log_in));
                progressDlg.setCancelable(false);
                progressDlg.setIndeterminate(false);
                progressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressDlg.dismiss();
                        me.cancel(true);
                    }
                });
                progressDlg.show();
            }

            @Override
            protected String doInBackground(Account... params) {
                return getAccessToken(params[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    accountName = s;
                }
                progressDlg.dismiss();
            }
        };
        task.execute(account);
    }

    private String getAccessToken(Account account) {
        try {
            return GoogleAuthUtil.getToken(ctx, account.name, "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS);
        } catch (UserRecoverableAuthException e) {
            a.startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
            e.printStackTrace();
            return null;
        } catch (GoogleAuthException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            getIntent().setAction("JustActivity Created");
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            new Recognize(this).parseResults(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(this);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(accountName));
            mPrefs.savePrefs(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
            new GetTasksListsAsync(this, null).execute();
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            mPrefs.savePrefs(Prefs.DRIVE_USER, SyncHelper.encrypt(accountName));
            new GetTasksListsAsync(this, null).execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                            new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(intent, REQUEST_AUTHORIZATION);
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (new SharedPrefs(this).loadBoolean(Prefs.EXPORT_SETTINGS)){
            new SharedPrefs(this).savePrefsBackup();
        }
    }
}
