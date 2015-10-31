package com.hexrain.design;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.Help;
import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.GetTasksListsAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.CategoryManager;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.dialogs.RateDialog;
import com.cray.software.justreminder.dialogs.utils.NewPlace;
import com.cray.software.justreminder.dialogs.utils.NewTemplate;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.QuickReturnRecyclerViewOnScrollListener;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.utils.LocationUtil;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;
import com.hexrain.design.fragments.ActiveFragment;
import com.hexrain.design.fragments.BackupsFragment;
import com.hexrain.design.fragments.EventsFragment;
import com.hexrain.design.fragments.GeolocationFragment;
import com.hexrain.design.fragments.GroupsFragment;
import com.hexrain.design.fragments.NotesFragment;
import com.hexrain.design.fragments.PlacesFragment;
import com.hexrain.design.fragments.TasksFragment;
import com.hexrain.design.fragments.TemplatesFragment;
import com.hexrain.design.fragments.TrashFragment;
import com.hexrain.flextcal.FlextCal;
import com.hexrain.flextcal.FlextHelper;
import com.hexrain.flextcal.FlextListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import hirondelle.date4j.DateTime;

public class ScreenManager extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private Toolbar toolbar;
    private FloatingEditText quickNote;
    private CardView noteCard, noteStatusCard, noteReminderCard;
    private TextView buttonYes;
    private TextView buttonNo;
    private TextView buttonReminderYes;
    private TextView buttonReminderNo;
    private AppBarLayout bar;
    private FloatingActionsMenu mainMenu;
    private FloatingActionButton addNote, addBirthday, addTask, addReminder, addQuick, mFab, addTemplate,
            addPlace, addGroup;
    private FloatingActionButton[] prevButtons;
    private RecyclerView currentList;
    private ListView currentListView;
    private CoordinatorLayout coordinatorLayout;

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
    public static final String TRANSLATION = "translation";
    public static final String MORE_APPS = "more_apps";
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
    private boolean isAnimation = false;
    private boolean doubleBackToExitPressedOnce = false;

    private Context ctx = this;
    private Activity a = this;
    private Date eventsDate = null;
    private FlextCal calendarView;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.activity_screen_manager);
        setRequestedOrientation(cSetter.getRequestOrientation());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        isAnimation = mPrefs.loadBoolean(Prefs.ANIMATIONS);

        bar = (AppBarLayout) findViewById(R.id.bar);

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(R.string.drawer_active_reminder);
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

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        initButton();

        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        if (mPrefs.loadBoolean(Prefs.UI_CHANGED)) {
            onNavigationDrawerItemSelected(mPrefs.loadPrefs(Prefs.LAST_FRAGMENT));
            mPrefs.saveBoolean(Prefs.UI_CHANGED, false);
        }
    }

    private void setUpButton(FloatingActionButton fab, View.OnClickListener listener, String title,
                             int size, int icon){
        fab.setTitle(title);
        fab.setSize(size);
        fab.setIcon(icon);
        fab.setColorNormal(getResources().getColor(R.color.colorWhite));
        fab.setColorPressed(getResources().getColor(R.color.grey_light));
        fab.setOnClickListener(listener);
    }

    private void initButton() {
        mainMenu = (FloatingActionsMenu) findViewById(R.id.mainMenu);

        addNote = new FloatingActionButton(this);
        addPlace = new FloatingActionButton(this);
        addTemplate = new FloatingActionButton(this);
        addQuick = new FloatingActionButton(this);
        addTask = new FloatingActionButton(this);
        addReminder = new FloatingActionButton(this);
        addBirthday = new FloatingActionButton(this);
        addGroup = new FloatingActionButton(this);

        mFab = new AddFloatingActionButton(this);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNoteVisible()) {
                    ViewUtils.hideReveal(noteCard, isAnimation);
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
                    ViewUtils.showReveal(noteCard, isAnimation);
                } else {
                    quickNote.setText("");
                    quickNote.setError(null);

                    ViewUtils.hideReveal(noteCard, isAnimation);
                }
                return true;
            }
        });
        mFab.setColorNormal(cSetter.colorSetter());
        mFab.setColorPressed(cSetter.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.windowBackground);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    }

    private void attachButtons(FloatingActionButton... buttons){
        if (prevButtons != null) {
            for (FloatingActionButton button : prevButtons) {
                mainMenu.removeButton(button);
            }
        }
        prevButtons = buttons;
        for (FloatingActionButton button : buttons){
            mainMenu.addButton(button);
        }
    }

    private void reloadButton(){
        mPrefs = new SharedPrefs(this);
        final boolean isExtend = mPrefs.loadBoolean(Prefs.EXTENDED_BUTTON);
        if (mTag.matches(FRAGMENT_EVENTS) || mTag.matches(ACTION_CALENDAR)){
            mFab.setVisibility(View.GONE);
            setUpButton(addReminder, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mainMenu.isExpanded()) mainMenu.collapse();
                    if (isNoteVisible()) {
                        ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(ScreenManager.this, QuickAddReminder.class)
                                    .putExtra("date", dateMills));
                        }
                    }, 150);
                }
            }, getString(R.string.new_reminder), FloatingActionButton.SIZE_NORMAL, R.drawable.ic_alarm_add_grey600_24dp);

            setUpButton(addBirthday, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mainMenu.isExpanded()) mainMenu.collapse();
                    if (isNoteVisible()) {
                        ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                    }

                    if (!mPrefs.loadBoolean(Prefs.BIRTHDAY_REMINDER))
                        Messages.toast(ScreenManager.this, getString(R.string.calendar_birthday_info));
                    else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(ScreenManager.this, AddBirthday.class));
                            }
                        }, 150);
                    }
                }
            }, getString(R.string.new_birthday), FloatingActionButton.SIZE_MINI, R.drawable.ic_cake_grey600_24dp);
            attachButtons(addBirthday, addReminder);
            mainMenu.setVisibility(View.VISIBLE);
        } else if (mTag.matches(FRAGMENT_BACKUPS)){
            mFab.setVisibility(View.GONE);
            mainMenu.setVisibility(View.GONE);
        } else {
            if (isExtend) {
                mFab.setVisibility(View.GONE);
                mainMenu.setVisibility(View.GONE);
                if (mTag.matches(FRAGMENT_TASKS) || mTag.matches(FRAGMENT_ARCHIVE) ||
                        mTag.matches(FRAGMENT_ACTIVE) || mTag.matches(FRAGMENT_NOTE)){
                    setUpButton(addReminder, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(ScreenManager.this, ReminderManager.class));
                                }
                            }, 150);
                        }
                    }, getString(R.string.new_reminder), FloatingActionButton.SIZE_NORMAL, R.drawable.ic_alarm_add_grey600_24dp);
                    setUpButton(addBirthday, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

                            if (!mPrefs.loadBoolean(Prefs.BIRTHDAY_REMINDER))
                                Messages.toast(ScreenManager.this, getString(R.string.calendar_birthday_info));
                            else {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(ScreenManager.this, AddBirthday.class));
                                    }
                                }, 150);
                            }
                        }
                    }, getString(R.string.new_birthday), FloatingActionButton.SIZE_MINI, R.drawable.ic_cake_grey600_24dp);
                    setUpButton(addTask, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (new GTasksHelper(ScreenManager.this).isLinked()) {
                                if (isNoteVisible()) {
                                    ViewUtils.hideReveal(noteCard, isAnimation);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(ScreenManager.this, TaskManager.class)
                                                .putExtra(Constants.ITEM_ID_INTENT, listId)
                                                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
                                    }
                                }, 150);

                            } else
                                Messages.toast(ScreenManager.this, getString(R.string.tasks_connection_warming));
                        }
                    }, getString(R.string.new_task), FloatingActionButton.SIZE_MINI, R.drawable.google_tasks_grey);
                    setUpButton(addNote, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (isNoteVisible()) {
                                ViewUtils.hideReveal(noteCard, isAnimation);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(ScreenManager.this, NotesManager.class));
                                }
                            }, 150);
                        }
                    }, getString(R.string.new_note), FloatingActionButton.SIZE_MINI, R.drawable.ic_event_note_grey600_24dp);
                    setUpButton(addQuick, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (!isNoteVisible()) {
                                ViewUtils.showReveal(noteCard, isAnimation);
                            } else {
                                quickNote.setText("");
                                quickNote.setError(null);

                                ViewUtils.hideReveal(noteCard, isAnimation);
                            }
                        }
                    }, getString(R.string.new_quick_note), FloatingActionButton.SIZE_MINI, R.drawable.ic_done_grey600_24dp);
                    attachButtons(addBirthday, addTask, addNote, addQuick, addReminder);
                    mainMenu.setVisibility(View.VISIBLE);
                } else {
                    setUpButton(addReminder, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(ScreenManager.this, ReminderManager.class));
                                }
                            }, 150);
                        }
                    }, getString(R.string.new_reminder), FloatingActionButton.SIZE_NORMAL, R.drawable.ic_alarm_add_grey600_24dp);
                    if (mTag.matches(FRAGMENT_LOCATIONS) || mTag.matches(FRAGMENT_PLACES)){
                        setUpButton(addPlace, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mainMenu.isExpanded()) mainMenu.collapse();
                                if (isNoteVisible()) {
                                    ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (LocationUtil.checkGooglePlayServicesAvailability(ScreenManager.this)) {
                                            startActivity(new Intent(ScreenManager.this, NewPlace.class));
                                        }
                                    }
                                }, 150);
                            }
                        }, getString(R.string.string_new_place), FloatingActionButton.SIZE_MINI, R.drawable.ic_location_on_grey600_24dp);
                        attachButtons(addPlace, addReminder);
                        mainMenu.setVisibility(View.VISIBLE);
                    } else if (mTag.matches(FRAGMENT_GROUPS)){
                        setUpButton(addGroup, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mainMenu.isExpanded()) mainMenu.collapse();
                                        if (isNoteVisible()) {
                                            ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                                        }

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startActivity(new Intent(ScreenManager.this, CategoryManager.class));
                                            }
                                        }, 150);
                                    }
                                }, getString(R.string.string_new_category), FloatingActionButton.SIZE_MINI,
                                R.drawable.ic_local_offer_grey600_24dp);
                        attachButtons(addGroup, addReminder);
                        mainMenu.setVisibility(View.VISIBLE);
                    } else if (mTag.matches(FRAGMENT_TEMPLATES)){
                        setUpButton(addTemplate, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mainMenu.isExpanded()) mainMenu.collapse();
                                        if (isNoteVisible()) {
                                            ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                                        }

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startActivity(new Intent(ScreenManager.this, NewTemplate.class));
                                            }
                                        }, 150);
                                    }
                                }, getString(R.string.string_new_template), FloatingActionButton.SIZE_MINI,
                                R.drawable.ic_message_grey600_24dp);
                        attachButtons(addTemplate, addReminder);
                        mainMenu.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                mFab.setVisibility(View.GONE);
                mainMenu.setVisibility(View.GONE);
                if (mTag.matches(FRAGMENT_ARCHIVE) || mTag.matches(FRAGMENT_ACTIVE) ||
                        mTag.matches(FRAGMENT_LOCATIONS)){
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

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
                                if (isNoteVisible()) {
                                    ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(ScreenManager.this, TaskManager.class)
                                                .putExtra(Constants.ITEM_ID_INTENT, listId)
                                                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
                                    }
                                }, 150);

                            } else
                                Messages.toast(ScreenManager.this, getString(R.string.tasks_connection_warming));
                        }
                    });
                    mFab.setVisibility(View.VISIBLE);
                } else if (mTag.matches(FRAGMENT_NOTE)){
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

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
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

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
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (LocationUtil.checkGooglePlayServicesAvailability(ScreenManager.this)) {
                                        startActivity(new Intent(ScreenManager.this, NewPlace.class));
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
                            if (isNoteVisible()) {
                                ViewUtils.hide(ScreenManager.this, noteCard, isAnimation);
                            }

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
    }

    @Override
    public void onListIdChanged(long listId) {
        this.listId = listId;
    }

    @Override
    public void onListChanged(RecyclerView list) {
        setListener(list);
    }

    private QuickReturnRecyclerViewOnScrollListener scrollListener;

    private void setListener(RecyclerView list) {
        if (list != null){
            if (scrollListener != null) list.removeOnScrollListener(scrollListener);
            boolean isExtended = mPrefs.loadBoolean(Prefs.EXTENDED_BUTTON);
            boolean isGrid = mPrefs.loadBoolean(Prefs.LIST_GRID);
            boolean isMain = false;
            if (mTag != null && (mTag.matches(FRAGMENT_ACTIVE) || mTag.matches(FRAGMENT_ARCHIVE) ||
                    mTag.matches(FRAGMENT_NOTE))) {
                isMain = true;
                if (mTag.matches(FRAGMENT_NOTE)) isGrid = mPrefs.loadBoolean(Prefs.NOTES_LIST_STYLE);
            }
            scrollListener = new QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                            .footer(isExtended ? mainMenu : mFab)
                            .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                            .isSnappable(true)
                            .isGrid(isMain && isGrid)
                            .build();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                list.addOnScrollListener(scrollListener);
            } else {
                list.setOnScrollListener(scrollListener);
            }
        }
    }

    @Override
    public void onDateChanged(long dateMills) {
        if (dateMills != 0) this.dateMills = dateMills;
    }

    @Override
    public void isDrawerOpen(boolean isOpen) {
        if (isOpen && mainMenu.isExpanded()) mainMenu.collapse();
    }

    @Override
    public void onTitleChanged(String title) {
        if (title != null) {
            mTitle = title;
            toolbar.setTitle(mTitle);
        }
    }

    @Override
    public void onUiChanged(int colorSetter, int colorStatus, int colorChooser) {
        if (colorSetter != 0){
            toolbar.setBackgroundColor(colorSetter);
            mFab.setColorNormal(colorSetter);
            mainMenu.setButtonColorNormal(colorSetter);
        }
        if (colorStatus != 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(colorStatus);
            }
        }
        if (colorChooser != 0){
            mFab.setColorPressed(colorChooser);
            mainMenu.setButtonColorPressed(colorChooser);
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
    public void onNavigationDrawerItemSelected(String tag) {
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
                replace(EventsFragment.newInstance(cal.getTimeInMillis()), tag);
                mPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 0);
            } else if (tag.matches(HELP)) {
                startActivity(new Intent(this, Help.class));
            } else if (tag.matches(TRANSLATION)) {
                translationDialog().show();
            } else if (tag.matches(MARKET)) {
                marketDialog().show();
            } else if (tag.matches(MORE_APPS)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://search?q=pub:Nazar Suhovich"));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Messages.toast(ScreenManager.this, "Couldn't launch market");
                }
            } else if (tag.matches(FRAGMENT_SETTINGS)) {
                Intent intentS = new Intent(this, com.cray.software.justreminder.SettingsActivity.class);
                startActivity(intentS);
            } else if (tag.matches(VOICE_RECOGNIZER)) {
                startVoiceRecognitionActivity();
            } else if (tag.matches(TASKS_AUTHORIZATION)) {
                if (!new GTasksHelper(this).isLinked()) {
                    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                            new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(intent, REQUEST_AUTHORIZATION);
                }
            } else {
                replace(ActiveFragment.newInstance(), tag);
            }
        } else {
            replace(ActiveFragment.newInstance(), FRAGMENT_ACTIVE);
        }
    }

    public void onSectionAttached(String tag) {
        if (tag.matches(FRAGMENT_ACTIVE)) {
            mTitle = getString(R.string.drawer_active_reminder);
        } else if (tag.matches(FRAGMENT_ARCHIVE)){
            mTitle = getString(R.string.drawer_archive_reminder);
        } else if (tag.matches(ACTION_CALENDAR)){
            mTitle = getString(R.string.calendar_fragment);
        } else if (tag.matches(FRAGMENT_EVENTS)){
            mTitle = getString(R.string.birthdays_dialog_title);
        } else if (tag.matches(FRAGMENT_NOTE)){
            mTitle = getString(R.string.fragment_notes);
        } else if (tag.matches(FRAGMENT_GROUPS)){
            mTitle = getString(R.string.string_manage_categories);
        } else if (tag.matches(FRAGMENT_PLACES)){
            mTitle = getString(R.string.settings_places);
        } else if (tag.matches(FRAGMENT_TEMPLATES)){
            mTitle = getString(R.string.settings_sms_templates_title);
        } else if (tag.matches(FRAGMENT_LOCATIONS)){
            mTitle = getString(R.string.geo_fragment);
        } else if (tag.matches(FRAGMENT_BACKUPS)){
            mTitle = getString(R.string.manage_backup_title);
        }
    }

    private void restoreUi(){
        toolbar.setBackgroundColor(cSetter.colorSetter());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        boolean isExtended = mPrefs.loadBoolean(Prefs.EXTENDED_BUTTON);
        if (!isExtended) {
            mFab.setColorNormal(cSetter.colorSetter());
            mFab.setColorPressed(cSetter.colorStatus());
        } else {
            mainMenu.setButtonColorNormal(cSetter.colorSetter());
            mainMenu.setButtonColorPressed(cSetter.colorStatus());
        }
    }

    private void showMonth(){
        calendarView = new FlextCal();
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
        args.putBoolean(FlextCal.SIX_WEEKS_IN_CALENDAR, true);
        args.putBoolean(FlextCal.ENABLE_IMAGES, mPrefs.loadBoolean(Prefs.CALENDAR_IMAGE));
        calendarView.setArguments(args);
        calendarView.setBackgroundForToday(cSetter.colorCurrentCalendar());
        replace(calendarView, mTag);

        final FlextListener listener = new FlextListener() {

            @Override
            public void onClickDate(Date date, View view) {
                eventsDate = date;
                onNavigationDrawerItemSelected(FRAGMENT_EVENTS);
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
                startActivity(new Intent(ScreenManager.this, QuickAddReminder.class)
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

        if (calendarView != null) {
            calendarView.refreshView();
            calendarView.clearSelectedDates();
        }

        if (mPrefs.loadBoolean(Prefs.REMINDERS_IN_CALENDAR)) {
            loadReminders();
        }

        loadEvents();
        calendarView.populateData();
        mPrefs.saveInt(Prefs.LAST_CALENDAR_VIEW, 1);
        mTitle = getString(R.string.calendar_fragment);
        toolbar.setTitle(mTitle);
        invalidateOptionsMenu();
    }

    protected Dialog translationDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_dialog_title))
                .setMessage(getString(R.string.translation_dialog_message))
                .setPositiveButton(getString(R.string.settings_help_point), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://docs.google.com/spreadsheets/d/1MeSehWucAsNEA67pIUzf3arL4-EtYh7SjwF-I-417UU/edit#gid=0"));
                        try {
                            startActivity(browserIntent);
                        } catch (ActivityNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.hide_dialog_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPrefs sPrefs = new SharedPrefs(ScreenManager.this);
                        sPrefs.saveBoolean(Prefs.HIDE_TRANSLATION_MENU, true);
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    protected Dialog marketDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.buy_pro_string))
                .setMessage(getString(R.string.pro_explanation_1) + "\n" +
                        getString(R.string.pro_explanation_2) + "\n" +
                        getString(R.string.pro_explanation_3) + "\n" +
                        getString(R.string.pro_explanation_4) + "\n" +
                        getString(R.string.pro_explanation_5) + "\n" +
                        getString(R.string.pro_explanation_7) + "\n" +
                        getString(R.string.pro_explanation_6))
                .setPositiveButton(getString(R.string.dialog_button_buy), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + "com.cray.software.justreminderpro"));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTag.matches(ACTION_CALENDAR)) {
            getMenuInflater().inflate(R.menu.calendar_menu, menu);
            menu.findItem(R.id.action_month).setVisible(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            if (eventsDate != null) calendar.setTime(eventsDate);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            menu.findItem(R.id.action_day).setTitle(day + "/" + (month + 1) + "/" + year);
        }
        toolbar.setTitle(mTitle);
        mainMenu.collapse();
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
                onNavigationDrawerItemSelected(FRAGMENT_EVENTS);
                return true;
            case R.id.action_voice:
                startVoiceRecognitionActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPrefs sPrefs = new SharedPrefs(this);
        if (sPrefs.loadBoolean(Prefs.UI_CHANGED)) {
            recreate();
        }

        setRequestedOrientation(cSetter.getRequestOrientation());
        showRate();

        if (Module.isPro() && !sPrefs.loadBoolean(Prefs.THANKS_SHOWN) && hasChanges()) {
            thanksDialog(false).show();
        }

        if (Module.isBeta() && !sPrefs.loadBoolean(Prefs.BETA_SHOWN) && hasChanges()) {
            thanksDialog(true).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        if (mTag != null) {
            onNavigationDrawerItemSelected(mTag);
        }

        if (sPrefs.loadBoolean(Prefs.STATUS_BAR_NOTIFICATION)){
            new Notifier(this).recreatePermanent();
        }

        isChangesShown();

        new DelayedAsync(this, null).execute();
    }

    @Override
    protected void onDestroy() {
        new UpdatesHelper(this).updateWidget();
        super.onDestroy();
    }

    private void loadReminders() {
        DataBase db = new DataBase(this);
        if (!db.isOpen()) db.open();
        mPrefs = new SharedPrefs(this);
        boolean isFeature = mPrefs.loadBoolean(Prefs.CALENDAR_FEATURE_TASKS);
        HashMap<DateTime, String> dates = new HashMap<>();
        dates.clear();
        calendarView.setBackgroundForOne(cSetter.colorReminderCalendar());
        Cursor c = db.getActiveReminders();
        if (c != null && c.moveToFirst()){
            do {
                int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long remCount = c.getLong(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long afterTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String task = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                if ((type.startsWith(Constants.TYPE_SKYPE) ||
                        type.matches(Constants.TYPE_CALL) ||
                        type.startsWith(Constants.TYPE_APPLICATION) ||
                        type.matches(Constants.TYPE_MESSAGE) ||
                        type.matches(Constants.TYPE_REMINDER) ||
                        type.matches(Constants.TYPE_TIME)) && isDone == 0) {
                    long time = TimeCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                            afterTime, repCode, remCount, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);
                        Date date = TimeUtil.getDate(year, month, day);
                        dates.put(FlextHelper.convertDateToDateTime(date), task);
                        int days = 0;
                        if (!type.matches(Constants.TYPE_TIME) && isFeature && repCode > 0){
                            do {
                                calendar.setTimeInMillis(calendar.getTimeInMillis() + (repCode *
                                        AlarmManager.INTERVAL_DAY));
                                days = days + repCode;
                                day = calendar.get(Calendar.DAY_OF_MONTH);
                                month = calendar.get(Calendar.MONTH);
                                year = calendar.get(Calendar.YEAR);
                                date = TimeUtil.getDate(year, month, day);
                                dates.put(FlextHelper.convertDateToDateTime(date), task);
                            } while (days < Configs.MAX_DAYS_COUNT);
                        }
                    }
                } else if (type.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0){
                    long time = TimeCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);
                        Date date = TimeUtil.getDate(year, month, day);
                        dates.put(FlextHelper.convertDateToDateTime(date), task);
                    }
                    int days = 0;
                    if (isFeature){
                        ArrayList<Integer> list = ReminderUtils.getRepeatArray(weekdays);
                        do {
                            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                            days = days + 1;
                            if (list.get(weekDay - 1) == 1){
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int month = calendar.get(Calendar.MONTH);
                                int year = calendar.get(Calendar.YEAR);
                                Date date = TimeUtil.getDate(year, month, day);
                                dates.put(FlextHelper.convertDateToDateTime(date), task);
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (type.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0){
                    long time = TimeCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);
                        Date date = TimeUtil.getDate(year, month, day);
                        dates.put(FlextHelper.convertDateToDateTime(date), task);
                    }
                    int days = 1;
                    if (isFeature){
                        do {
                            time = TimeCount.getNextMonthDayTime(myDay, calendar.getTimeInMillis(), days);
                            days = days + 1;
                            calendar.setTimeInMillis(time);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int month = calendar.get(Calendar.MONTH);
                            int year = calendar.get(Calendar.YEAR);
                            Date date = TimeUtil.getDate(year, month, day);
                            dates.put(FlextHelper.convertDateToDateTime(date), task);
                        } while (days < Configs.MAX_MONTH_COUNT);
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();

        if (calendarView != null) {
            calendarView.setTextForEventOne(dates);
        }
    }

    private void loadEvents(){
        DataBase db = new DataBase(this);
        if (!db.isOpen()) db.open();
        HashMap<DateTime, String> dates = new HashMap<>();
        calendarView.setBackgroundForTwo(cSetter.colorBirthdayCalendar());
        Cursor c = db.getBirthdays();
        if (c != null && c.moveToFirst()){
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                String name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                Date date = null;
                try {
                    date = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int year = calendar.get(Calendar.YEAR);
                if (date != null) {
                    try {
                        calendar.setTime(date);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    Date bdDate = TimeUtil.getDate(year, month, day);
                    Date prevDate = TimeUtil.getDate(year - 1, month, day);
                    Date nextDate = TimeUtil.getDate(year + 1, month, day);
                    Date nextTwoDate = TimeUtil.getDate(year + 2, month, day);
                    dates.put(FlextHelper.convertDateToDateTime(bdDate), name);
                    dates.put(FlextHelper.convertDateToDateTime(prevDate), name);
                    dates.put(FlextHelper.convertDateToDateTime(nextDate), name);
                    dates.put(FlextHelper.convertDateToDateTime(nextTwoDate), name);
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();

        if (calendarView != null) {
            calendarView.setTextForEventTwo(dates);
        }
    }

    private void showChanges() {
        startActivity(new Intent(this, ChangeDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
        if (pInfo != null) {
            version = pInfo.versionName;
        }
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
        if (pInfo != null) {
            version = pInfo.versionName;
        }
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
                    startActivity(new Intent(this, RateDialog.class));
                }
            }
        } else {
            sPrefs.saveBoolean(Prefs.RATE_SHOW, false);
            sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
        }
    }

    protected Dialog thanksDialog(final boolean isBeta) {
        return new AlertDialog.Builder(this)
                .setTitle(isBeta ? getString(R.string.beta_version) : getString(R.string.thank_dialog_title))
                .setMessage(isBeta ? getString(R.string.beta_version_message) : (getString(R.string.thank_dialog_message) + " " +
                        getString(R.string.thank_dialog_greeting)))
                .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (isBeta) {
                            new SharedPrefs(ScreenManager.this)
                                    .saveBoolean(Prefs.BETA_SHOWN, true);
                        } else {
                            new SharedPrefs(ScreenManager.this)
                                    .saveBoolean(Prefs.THANKS_SHOWN, true);
                        }
                    }
                })
                .setCancelable(false)
                .create();
    }

    private void saveNote() {
        SyncHelper sHelp = new SyncHelper(ScreenManager.this);
        final String note = quickNote.getText().toString();
        if (note.matches("")) {
            quickNote.setError(getString(R.string.empty_field_error));
            return;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat full24Format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String date = full24Format.format(calendar1.getTime());

        String uuID = SyncHelper.generateID();
        NotesBase db = new NotesBase(ScreenManager.this);
        db.open();
        Random r = new Random();
        int color = r.nextInt(15);
        final long id;
        if (mPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            id = db.saveNote(sHelp.encrypt(note), date, cSetter.getNoteColor(color), uuID, null, 5);
        } else {
            id = db.saveNote(note, date, cSetter.getNoteColor(color), uuID, null, 5);
        }

        new UpdatesHelper(ScreenManager.this).updateNotesWidget();

        quickNote.setText("");
        quickNote.setError(null);

        ViewUtils.hideReveal(noteCard, isAnimation);

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

        if (mTag.matches(FRAGMENT_NOTE) || mTag.matches(FRAGMENT_ACTIVE))
            onNavigationDrawerItemSelected(mTag);
    }

    private void askNotification(final String note, final long id){
        mPrefs = new SharedPrefs(ScreenManager.this);
        ViewUtils.showReveal(noteStatusCard, isAnimation);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notifier(ScreenManager.this).showNoteNotification(note, id);
                ViewUtils.hideReveal(noteStatusCard, isAnimation);

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
                ViewUtils.hideReveal(noteStatusCard, isAnimation);

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
        ViewUtils.showReveal(noteReminderCard, isAnimation);

        buttonReminderYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideReveal(noteReminderCard, isAnimation);
                DataBase db = new DataBase(ScreenManager.this);
                if (!db.isOpen()) db.open();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(System.currentTimeMillis());
                int day = calendar1.get(Calendar.DAY_OF_MONTH);
                int month = calendar1.get(Calendar.MONTH);
                int year = calendar1.get(Calendar.YEAR);
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);
                Cursor cf = db.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                long remId = db.insertReminder(note, Constants.TYPE_TIME, day, month, year, hour,
                        minute, 0, null, 0, mPrefs.loadInt(Prefs.QUICK_NOTE_REMINDER_TIME) * Intervals.MILLS_INTERVAL_MINUTE,
                        0, 0, 0, SyncHelper.generateID(), null, 0, null, 0, 0, 0, categoryId);
                new AlarmReceiver().setAlarm(ScreenManager.this, remId);
                db.updateReminderDateTime(remId);
                new UpdatesHelper(ScreenManager.this).updateWidget();
                NotesBase base = new NotesBase(ScreenManager.this);
                base.open();
                base.linkToReminder(noteId, remId);
                base.close();
                if (mTag.matches(FRAGMENT_NOTE) || mTag.matches(FRAGMENT_ACTIVE))
                    onNavigationDrawerItemSelected(mTag);
            }
        });

        buttonReminderNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideReveal(noteReminderCard, isAnimation);
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
            ViewUtils.hideReveal(noteCard, isAnimation);

            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(quickNote.getWindowToken(), 0);
            return;
        }
        if (mainMenu.isExpanded()) {
            mainMenu.collapse();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Messages.toast(ScreenManager.this, getString(R.string.press_again));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mPrefs = new SharedPrefs(this);
        if (!mPrefs.loadBoolean(Prefs.AUTO_LANGUAGE)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mPrefs.loadPrefs(Prefs.VOICE_LANGUAGE));
        } else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_say_something));
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e){
            Messages.toast(ScreenManager.this, getString(R.string.recognizer_not_found_error_message));
        }
    }


    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(ScreenManager.this, ProgressDialog.STYLE_SPINNER);
                progressDlg.setMax(100);
                progressDlg.setTitle(getString(R.string.connecting_dialog_title));
                progressDlg.setMessage(getString(R.string.application_verifying_text));
                progressDlg.setCancelable(false);
                progressDlg.setIndeterminate(false);
                progressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface d) {
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

            new Recognizer(this).selectTask(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(this);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(accountName));
            mPrefs.savePrefs(Prefs.DRIVE_USER, new SyncHelper(this).encrypt(accountName));
            new GetTasksListsAsync(this, null).execute();
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            mPrefs.savePrefs(Prefs.DRIVE_USER, new SyncHelper(this).encrypt(accountName));
            new GetTasksListsAsync(this, null).execute();
        }
    }

    @Override
    protected void onStop() {
        if (new SharedPrefs(this).loadBoolean(Prefs.EXPORT_SETTINGS)){
            new SharedPrefs(this).savePrefsBackup();
        }
        super.onStop();
    }

    @Override
    public void showSnackbar(int message) {
        boolean ext = mPrefs.loadBoolean(Prefs.EXTENDED_BUTTON);
        Messages.snackbar(ext ? mainMenu : mFab, message);
    }
}
