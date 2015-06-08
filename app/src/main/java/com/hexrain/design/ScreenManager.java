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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.Help;
import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.SettingsActivity;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.adapters.QuickReturnListViewOnScrollListener;
import com.cray.software.justreminder.adapters.QuickReturnRecyclerViewOnScrollListener;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.GetExchangeTasksAsync;
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
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.QuickReturnUtils;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;
import com.hexrain.design.fragments.ActiveFragment;
import com.hexrain.design.fragments.ArchivedRemindersFragment;
import com.hexrain.design.fragments.BackupsFragment;
import com.hexrain.design.fragments.EventsFragment;
import com.hexrain.design.fragments.GeolocationFragment;
import com.hexrain.design.fragments.GroupsFragment;
import com.hexrain.design.fragments.NotesFragment;
import com.hexrain.design.fragments.PlacesFragment;
import com.hexrain.design.fragments.TasksFragment;
import com.hexrain.design.fragments.TemplatesFragment;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ScreenManager extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private String mTag;
    Toolbar toolbar;
    FloatingEditText quickNote;
    CardView noteCard, noteStatusCard, noteReminderCard;
    TextView buttonYes, buttonNo, buttonReminderYes, buttonReminderNo, buttonSave;

    FloatingActionsMenu mainMenu;
    FloatingActionButton addNote, addBirthday, addTask, addReminder, addQuick, mFab, addTemplate,
            addPlace, addGroup;

    ColorSetter cSetter = new ColorSetter(this);
    SharedPrefs sPrefs = new SharedPrefs(this);
    DataBase DB;

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

    private String mTitle;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;
    String accountName;
    private Context ctx = this;
    private Activity a = this;
    RecyclerView currentList;
    ListView currentListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.activity_screen_manager);
        setRequestedOrientation(cSetter.getRequestOrientation());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

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
        buttonSave = (TextView) findViewById(R.id.buttonSave);
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

        new GetExchangeTasksAsync(this, null).execute();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED)) {
            onNavigationDrawerItemSelected(sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT));
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED, false);
        }
    }

    private void initButton() {
        mainMenu = (FloatingActionsMenu) findViewById(R.id.mainMenu);

        addNote = new FloatingActionButton(getBaseContext());
        addNote.setTitle(getString(R.string.new_note));
        addNote.setSize(FloatingActionButton.SIZE_MINI);
        addNote.setIcon(R.drawable.ic_event_note_grey600_24dp);
        addNote.setColorNormal(getResources().getColor(R.color.colorWhite));
        addNote.setColorPressed(getResources().getColor(R.color.grey_light));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScreenManager.this, NotesManager.class));
                    }
                }, 150);
            }
        });

        addTask = new FloatingActionButton(getBaseContext());
        addTask.setTitle(getString(R.string.new_task));
        addTask.setSize(FloatingActionButton.SIZE_MINI);
        addTask.setIcon(R.drawable.google_tasks_grey);
        addTask.setColorNormal(getResources().getColor(R.color.colorWhite));
        addTask.setColorPressed(getResources().getColor(R.color.grey_light));
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (new GTasksHelper(ScreenManager.this).isLinked()) {
                    if (isNoteVisible()) {
                        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                            hide(noteCard);
                        } else noteCard.setVisibility(View.GONE);
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
                    Toast.makeText(ScreenManager.this, getString(R.string.tasks_connection_warming), Toast.LENGTH_SHORT).show();
            }
        });

        addBirthday = new FloatingActionButton(getBaseContext());
        addBirthday.setTitle(getString(R.string.new_birthday));
        addBirthday.setSize(FloatingActionButton.SIZE_MINI);
        addBirthday.setIcon(R.drawable.ic_cake_grey600_24dp);
        addBirthday.setColorNormal(getResources().getColor(R.color.colorWhite));
        addBirthday.setColorPressed(getResources().getColor(R.color.grey_light));
        addBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }

                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_CONTACTS))
                    Toast.makeText(ScreenManager.this, getString(R.string.calendar_birthday_info), Toast.LENGTH_LONG).show();
                else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(ScreenManager.this, AddBirthday.class));
                        }
                    }, 150);
                }
            }
        });

        addQuick = new FloatingActionButton(getBaseContext());
        addQuick.setTitle(getString(R.string.new_quick_note));
        addQuick.setSize(FloatingActionButton.SIZE_MINI);
        addQuick.setIcon(R.drawable.ic_done_grey600_24dp);
        addQuick.setColorNormal(getResources().getColor(R.color.colorWhite));
        addQuick.setColorPressed(getResources().getColor(R.color.grey_light));
        addQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (!isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        show(noteCard);
                    } else noteCard.setVisibility(View.VISIBLE);
                } else {
                    quickNote.setText("");
                    quickNote.setError(null);

                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }
            }
        });

        addReminder = new FloatingActionButton(getBaseContext());
        addReminder.setTitle(getString(R.string.new_reminder));
        addReminder.setSize(FloatingActionButton.SIZE_NORMAL);
        addReminder.setIcon(R.drawable.ic_alarm_add_grey600_24dp);
        addReminder.setColorNormal(getResources().getColor(R.color.colorWhite));
        addReminder.setColorPressed(getResources().getColor(R.color.grey_light));
        addReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScreenManager.this, QuickAddReminder.class)
                                .putExtra("date", dateMills));
                    }
                }, 150);
            }
        });

        addPlace = new FloatingActionButton(getBaseContext());
        addPlace.setTitle(getString(R.string.string_new_place));
        addPlace.setSize(FloatingActionButton.SIZE_MINI);
        addPlace.setIcon(R.drawable.ic_location_on_grey600_24dp);
        addPlace.setColorNormal(getResources().getColor(R.color.colorWhite));
        addPlace.setColorPressed(getResources().getColor(R.color.grey_light));
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (checkGooglePlayServicesAvailability()) {
                            startActivity(new Intent(ScreenManager.this, NewPlace.class));
                        }
                    }
                }, 150);
            }
        });

        addGroup = new FloatingActionButton(getBaseContext());
        addGroup.setTitle(getString(R.string.string_new_category));
        addGroup.setSize(FloatingActionButton.SIZE_MINI);
        addGroup.setIcon(R.drawable.ic_local_offer_grey600_24dp);
        addGroup.setColorNormal(getResources().getColor(R.color.colorWhite));
        addGroup.setColorPressed(getResources().getColor(R.color.grey_light));
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScreenManager.this, CategoryManager.class));
                    }
                }, 150);
            }
        });

        addTemplate = new FloatingActionButton(getBaseContext());
        addTemplate.setTitle(getString(R.string.string_new_template));
        addTemplate.setSize(FloatingActionButton.SIZE_MINI);
        addTemplate.setIcon(R.drawable.ic_textsms_grey600_24dp);
        addTemplate.setColorNormal(getResources().getColor(R.color.colorWhite));
        addTemplate.setColorPressed(getResources().getColor(R.color.grey_light));
        addTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScreenManager.this, NewTemplate.class));
                    }
                }, 150);
            }
        });

        mFab = new AddFloatingActionButton(this);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNoteVisible()) {
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
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
                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        show(noteCard);
                    } else noteCard.setVisibility(View.VISIBLE);
                } else {
                    quickNote.setText("");
                    quickNote.setError(null);

                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                        hide(noteCard);
                    } else noteCard.setVisibility(View.GONE);
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

    FloatingActionButton[] prevButtons;

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
        sPrefs = new SharedPrefs(this);
        boolean isExtend = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON);
        if (mTag.matches(FRAGMENT_EVENTS) || mTag.matches(ACTION_CALENDAR)){
            mFab.setVisibility(View.GONE);
            addReminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mainMenu.isExpanded()) mainMenu.collapse();
                    if (isNoteVisible()) {
                        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                            hide(noteCard);
                        } else noteCard.setVisibility(View.GONE);
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(ScreenManager.this, QuickAddReminder.class)
                                    .putExtra("date", dateMills));
                        }
                    }, 150);
                }
            });
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
                    addReminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainMenu.isExpanded()) mainMenu.collapse();
                            if (isNoteVisible()) {
                                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                    hide(noteCard);
                                } else noteCard.setVisibility(View.GONE);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(ScreenManager.this, ReminderManager.class));
                                }
                            }, 150);
                        }
                    });
                    attachButtons(addBirthday, addTask, addNote, addQuick, addReminder);
                    mainMenu.setVisibility(View.VISIBLE);
                } else {
                    if (mTag.matches(FRAGMENT_LOCATIONS) || mTag.matches(FRAGMENT_PLACES)){
                        attachButtons(addPlace, addReminder);
                        mainMenu.setVisibility(View.VISIBLE);
                    } else if (mTag.matches(FRAGMENT_GROUPS)){
                        attachButtons(addGroup, addReminder);
                        mainMenu.setVisibility(View.VISIBLE);
                    } else if (mTag.matches(FRAGMENT_TEMPLATES)){
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
                                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                    hide(noteCard);
                                } else noteCard.setVisibility(View.GONE);
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
                                    if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                        hide(noteCard);
                                    } else noteCard.setVisibility(View.GONE);
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
                                Toast.makeText(ScreenManager.this, getString(R.string.tasks_connection_warming),
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
                    mFab.setVisibility(View.VISIBLE);
                } else if (mTag.matches(FRAGMENT_NOTE)){
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isNoteVisible()) {
                                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                    hide(noteCard);
                                } else noteCard.setVisibility(View.GONE);
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
                                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                    hide(noteCard);
                                } else noteCard.setVisibility(View.GONE);
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
                                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                    hide(noteCard);
                                } else noteCard.setVisibility(View.GONE);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (checkGooglePlayServicesAvailability()) {
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
                                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                                    hide(noteCard);
                                } else noteCard.setVisibility(View.GONE);
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
    public void onListChange(RecyclerView list) {
        this.currentList = list;
        setScrollListener();
    }

    @Override
    public void onListChange(ListView list) {
        this.currentListView = list;
        setScrollListener();
    }

    private void setScrollListener(){
        if (currentList != null){
            sPrefs = new SharedPrefs(this);
            boolean isExtended = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON);
            QuickReturnRecyclerViewOnScrollListener scrollListener = new
                    QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                    .footer(isExtended ? mainMenu : mFab)
                    .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                    .isSnappable(true)
                    .build();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                currentList.addOnScrollListener(scrollListener);
            } else {
                currentList.setOnScrollListener(scrollListener);
            }
        }
        if (currentListView != null){
            sPrefs = new SharedPrefs(this);
            boolean isExtended = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON);
            QuickReturnListViewOnScrollListener scrollListener = new
                    QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                    .footer(isExtended ? mainMenu : mFab)
                    .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                    .isSnappable(true)
                    .build();
            currentListView.setOnScrollListener(scrollListener);
        }
    }

    long listId;

    @Override
    public void onListIdChanged(long listId) {
        this.listId = listId;
    }

    private long dateMills;

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
        }
        if (colorStatus != 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(colorStatus);
            }
        }
        if (colorChooser != 0){
            mFab.setColorPressed(colorChooser);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String tag) {
        // update the main content by replacing fragments
        if (tag != null) {
            restoreUi();
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (tag.matches(FRAGMENT_ACTIVE)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ActiveFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_ARCHIVE)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ArchivedRemindersFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_NOTE)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, NotesFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_GROUPS)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, GroupsFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_PLACES)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlacesFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_TEMPLATES)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TemplatesFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_TASKS)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TasksFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_BACKUPS)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, BackupsFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_LOCATIONS)) {
                if (checkGooglePlayServicesAvailability()) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, GeolocationFragment.newInstance(), tag)
                            .commitAllowingStateLoss();
                    mTag = tag;
                    sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
                }
            } else if (tag.matches(ACTION_CALENDAR)) {
                showMonth();
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
            } else if (tag.matches(FRAGMENT_EVENTS)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                if (eventsDate != null) {
                    cal.setTime(eventsDate);
                }
                mTag = tag;
                sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, tag);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, EventsFragment.newInstance(cal.getTimeInMillis()), tag)
                        .commitAllowingStateLoss();
                sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 0);
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
                    Toast.makeText(this, "Couldn't launch market", Toast.LENGTH_LONG).show();
                }
            } else if (tag.matches(FRAGMENT_SETTINGS)) {
                Intent intentS = new Intent(this, SettingsActivity.class);
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
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ActiveFragment.newInstance(), tag)
                        .commitAllowingStateLoss();
            }
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ActiveFragment.newInstance(), FRAGMENT_ACTIVE)
                    .commitAllowingStateLoss();
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_LAST_FRAGMENT, FRAGMENT_ACTIVE);
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
        boolean isExtended = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON);
        if (!isExtended) {
            mFab.setColorNormal(cSetter.colorSetter());
            mFab.setColorPressed(cSetter.colorChooser());
        }
    }

    Date eventsDate = null;
    CaldroidFragment calendarView;

    private void showMonth(){
        calendarView = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        sPrefs = new SharedPrefs(this);
        if (sPrefs.loadInt(Constants.APP_UI_PREFERENCES_START_DAY) == 0) {
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.SUNDAY);
        } else {
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        }
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);
        calendarView.setArguments(args);
        calendarView.setMinDate(null);
        calendarView.setMaxDate(null);

        eventsDate = cal.getTime();

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.container, calendarView);
        t.addToBackStack(mTag);
        t.commitAllowingStateLoss();

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                eventsDate = date;
                onNavigationDrawerItemSelected(FRAGMENT_EVENTS);
            }

            @Override
            public void onChangeMonth(int month, int year) {
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
            public void onCaldroidViewCreated() {
            }

        };

        calendarView.setCaldroidListener(listener);

        if (calendarView != null) {
            calendarView.refreshView();
            calendarView.clearSelectedDates();
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR)) {
            loadReminders();
        }

        loadEvents();
        sPrefs.saveInt(Constants.APP_UI_PREFERENCES_LAST_CALENDAR_VIEW, 1);
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
                        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU, true);
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
            menu.findItem(R.id.action_day).setTitle(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
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
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED)) {
            recreate();
        }

        setRequestedOrientation(cSetter.getRequestOrientation());
        showRate();

        if (new ManageModule().isPro()){
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)) {
                thanksDialog().show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        if (mTag != null) onNavigationDrawerItemSelected(mTag);

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)){
            new Notifier(this).recreatePermanent();
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG) &&
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)) {
            isChangesShown();
        }

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
        sPrefs = new SharedPrefs(this);
        boolean isFeature = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS);
        TimeCount mCount = new TimeCount(this);
        ArrayList<Date> dates = new ArrayList<>();
        dates.clear();
        Cursor c = db.queryGroup();
        if (c != null && c.moveToFirst()){
            do {
                int myHour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int myMinute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                int remCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                long afterTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                if ((type.startsWith(Constants.TYPE_SKYPE) ||
                        type.matches(Constants.TYPE_CALL) ||
                        type.startsWith(Constants.TYPE_APPLICATION) ||
                        type.matches(Constants.TYPE_MESSAGE) ||
                        type.matches(Constants.TYPE_REMINDER) ||
                        type.matches(Constants.TYPE_TIME)) && isDone == 0) {
                    long time = mCount.getEventTime(myYear, myMonth, myDay, myHour, myMinute, 0,
                            afterTime, repCode, remCount, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        Date bdDate = getDate(0, month, day);
                        dates.add(bdDate);
                        int days = 0;
                        if (!type.matches(Constants.TYPE_TIME) && isFeature && repCode > 0){
                            do {
                                calendar.setTimeInMillis(calendar.getTimeInMillis() + (repCode *
                                        AlarmManager.INTERVAL_DAY));
                                days = days + repCode;
                                day = calendar.get(Calendar.DAY_OF_MONTH);
                                month = calendar.get(Calendar.MONTH);
                                bdDate = getDate(0, month, day);
                                dates.add(bdDate);
                            } while (days < Configs.MAX_DAYS_COUNT);
                        }
                    }
                } else if (type.startsWith(Constants.TYPE_WEEKDAY) && isDone == 0){
                    long time = mCount.getNextWeekdayTime(myHour, myMinute, weekdays, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        Date bdDate = getDate(0, month, day);
                        dates.add(bdDate);
                    }
                    int days = 0;
                    if (isFeature){
                        ArrayList<Integer> list = getRepeatArray(weekdays);
                        do {
                            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                            days = days + 1;
                            if (list.get(weekDay - 1) == 1){
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int month = calendar.get(Calendar.MONTH);
                                Date bdDate = getDate(0, month, day);
                                dates.add(bdDate);
                            }
                        } while (days < Configs.MAX_DAYS_COUNT);
                    }
                } else if (type.startsWith(Constants.TYPE_MONTHDAY) && isDone == 0){
                    long time = mCount.getNextMonthDayTime(myHour, myMinute, myDay, 0);
                    Calendar calendar = Calendar.getInstance();
                    if (time > 0) {
                        calendar.setTimeInMillis(time);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH);
                        Date bdDate = getDate(0, month, day);
                        dates.add(bdDate);
                    }
                    int days = 1;
                    if (isFeature){
                        do {
                            time = mCount.getNextMonthDayTime(myDay, calendar.getTimeInMillis(), days);
                            days = days + 1;
                            calendar.setTimeInMillis(time);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int month = calendar.get(Calendar.MONTH);
                            Date bdDate = getDate(0, month, day);
                            dates.add(bdDate);
                        } while (days < Configs.MAX_MONTH_COUNT);
                    }
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();

        for (int i = 0; i < dates.size(); i++) {
            if (calendarView != null) {
                calendarView.setBackgroundResourceForDate(cSetter.colorReminderCalendar(), dates.get(i));
            }
        }
    }

    private ArrayList<Integer> getRepeatArray(String weekdays){
        ArrayList<Integer> res = new ArrayList<>();
        if (Character.toString(weekdays.charAt(6)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(0)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(1)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(2)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(3)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(4)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        if (Character.toString(weekdays.charAt(5)).matches(Constants.DAY_CHECKED))res.add(1);
        else res.add(0);
        return res;
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private void loadEvents(){
        DataBase db = new DataBase(this);
        if (!db.isOpen()) db.open();
        ArrayList<Date> dates = new ArrayList<>();
        Cursor c = db.queryEvents();
        if (c != null && c.moveToFirst()){
            do {
                String birthday = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                Date date = null;
                try {
                    date = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                if (date != null) {
                    try {
                        calendar.setTime(date);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    Date bdDate = getDate(0, month, day);
                    Date prevDate = getDate(0 - 1, month, day);
                    Date nextDate = getDate(1, month, day);
                    Date nextTwoDate = getDate(2, month, day);
                    dates.add(bdDate);
                    dates.add(prevDate);
                    dates.add(nextDate);
                    dates.add(nextTwoDate);
                }
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();

        for (int i = 0; i < dates.size(); i++) {
            if (calendarView != null) {
                calendarView.setBackgroundResourceForDate(cSetter.colorBirthdayCalendar(), dates.get(i));
            }
        }
    }

    public static Date getDate(int index, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year + index);
        cal1.set(Calendar.MONTH, month);
        cal1.set(Calendar.DAY_OF_MONTH, day);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
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

    private void showRate(){
        SharedPrefs sPrefs = new SharedPrefs(this);

        if (sPrefs.isString(Constants.APP_UI_PREFERENCES_RATE_SHOW)) {
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_RATE_SHOW)) {
                int counts = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT);
                if (counts < 10) {
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, counts + 1);
                } else {
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, 0);
                    startActivity(new Intent(this, RateDialog.class));
                }
            }
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_RATE_SHOW, false);
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, 0);
        }
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
            Log.d("GooglePlayServicesUtil", "Result is: " + resultCode);
            return true;
        }
    }

    protected Dialog thanksDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.thank_dialog_title))
                .setMessage(getString(R.string.thank_dialog_message) + " " +
                        getString(R.string.thank_dialog_greeting))
                .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new SharedPrefs(ScreenManager.this)
                                .saveBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN, true);
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
        SimpleDateFormat full24Format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date = full24Format.format(calendar1.getTime());

        String uuID = sHelp.generateID();
        NotesBase db = new NotesBase(ScreenManager.this);
        db.open();
        Random r = new Random();
        int color = r.nextInt(15);
        final long id;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
            id = db.saveNote(sHelp.encrypt(note), date, cSetter.getNoteColor(color), uuID, null, 5);
        } else {
            id = db.saveNote(note, date, cSetter.getNoteColor(color), uuID, null, 5);
        }

        new UpdatesHelper(ScreenManager.this).updateNotesWidget();

        quickNote.setText("");
        quickNote.setError(null);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
            hide(noteCard);
        } else noteCard.setVisibility(View.GONE);

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
        sPrefs = new SharedPrefs(ScreenManager.this);
        final boolean animate = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS);
        if (animate) {
            show(noteStatusCard);
        } else noteStatusCard.setVisibility(View.VISIBLE);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notifier(ScreenManager.this).showNoteNotification(note, id);
                if (animate) {
                    hide(noteStatusCard);
                } else noteStatusCard.setVisibility(View.GONE);

                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER)){
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
                if (animate) {
                    hide(noteStatusCard);
                } else noteStatusCard.setVisibility(View.GONE);

                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER)){
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
        sPrefs = new SharedPrefs(ScreenManager.this);

        final boolean animate = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS);
        if (animate) {
            show(noteReminderCard);
        } else noteReminderCard.setVisibility(View.VISIBLE);

        buttonReminderYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animate) {
                    hide(noteReminderCard);
                } else noteReminderCard.setVisibility(View.GONE);

                SyncHelper sHelp = new SyncHelper(ScreenManager.this);
                DB = new DataBase(ScreenManager.this);
                if (!DB.isOpen()) DB.open();
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(System.currentTimeMillis());
                int day = calendar1.get(Calendar.DAY_OF_MONTH);
                int month = calendar1.get(Calendar.MONTH);
                int year = calendar1.get(Calendar.YEAR);
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);
                Cursor cf = DB.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                long remId = DB.insertTask(note, Constants.TYPE_TIME, day, month, year, hour, minute, 0, null,
                        0, sPrefs.loadInt(Constants.APP_UI_PREFERENCES_QUICK_NOTE_REMINDER_TIME),
                        0, 0, 0, sHelp.generateID(), null, 0, null, 0, 0, 0, categoryId);
                new AlarmReceiver().setAlarm(ScreenManager.this, remId);
                DB.updateDateTime(remId);
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
                if (animate) {
                    hide(noteReminderCard);
                } else noteReminderCard.setVisibility(View.GONE);
            }
        });
    }

    private boolean isNoteVisible(){
        return noteCard.getVisibility() == View.VISIBLE;
    }

    public void show(final View v) {
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        v.startAnimation(slide);
        v.setVisibility(View.VISIBLE);
    }

    private void hide(final View v) {
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left);
        v.startAnimation(slide);
        v.setVisibility(View.GONE);
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (isNoteVisible()){
            quickNote.setText("");
            quickNote.setError(null);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                hide(noteCard);
            } else noteCard.setVisibility(View.GONE);

            //mFab.setImageResource(R.drawable.ic_add_white_24dp);

            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(quickNote.getWindowToken(), 0);
            //if (isButtonVisible) hideButtons();
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
        Toast.makeText(this, getString(R.string.press_again), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sPrefs = new SharedPrefs(this);
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
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, new SyncHelper(this).encrypt(accountName));
            new GetTasksListsAsync(this, null).execute();
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            sPrefs.savePrefs(Constants.APP_UI_PREFERENCES_DRIVE_USER, new SyncHelper(this).encrypt(accountName));
            new GetTasksListsAsync(this, null).execute();
        }
    }
}
