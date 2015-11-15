package com.hexrain.design;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.dialogs.RateDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Configs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Intervals;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.hexrain.design.fragments.ActiveFragment;
import com.hexrain.design.fragments.NotesFragment;
import com.hexrain.flextcal.FlextCal;
import com.hexrain.flextcal.FlextHelper;
import com.hexrain.flextcal.FlextListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import hirondelle.date4j.DateTime;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private FloatingEditText quickNote;
    private CardView noteCard, noteStatusCard, noteReminderCard;
    private TextView buttonYes;
    private TextView buttonNo;
    private TextView buttonReminderYes;
    private TextView buttonReminderNo;
    private AppBarLayout bar;
    private FloatingActionsMenu mainMenu;
    private com.getbase.floatingactionbutton.FloatingActionButton addNote, addBirthday, addTask, addReminder, addQuick, mFab, addTemplate,
            addPlace, addGroup;
    private com.getbase.floatingactionbutton.FloatingActionButton[] prevButtons;
    private RecyclerView currentList;
    private ListView currentListView;
    private CoordinatorLayout coordinatorLayout;

    private ColorSetter cSetter = new ColorSetter(this);
    private SharedPrefs mPrefs = new SharedPrefs(this);

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

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.activity_main);
        setRequestedOrientation(cSetter.getRequestOrientation());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }

        isAnimation = mPrefs.loadBoolean(Prefs.ANIMATIONS);

        bar = (AppBarLayout) findViewById(R.id.bar);

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_reminder);
    }

    private void setUpButton(com.getbase.floatingactionbutton.FloatingActionButton fab, View.OnClickListener listener, String title,
                             int size, int icon){
        fab.setTitle(title);
        fab.setSize(size);
        fab.setIcon(icon);
        fab.setColorNormal(getResources().getColor(R.color.colorWhite));
        fab.setColorPressed(getResources().getColor(R.color.material_divider));
        fab.setOnClickListener(listener);
    }

    private void initButton() {
        mainMenu = (FloatingActionsMenu) findViewById(R.id.mainMenu);

        addNote = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addPlace = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addTemplate = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addQuick = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addTask = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addReminder = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addBirthday = new com.getbase.floatingactionbutton.FloatingActionButton(this);
        addGroup = new com.getbase.floatingactionbutton.FloatingActionButton(this);

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
                        startActivity(new Intent(MainActivity.this, ReminderManager.class));
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
        mFab.setColorNormal(cSetter.colorPrimary());
        mFab.setColorPressed(cSetter.colorAccent());
        mFab.setSize(com.getbase.floatingactionbutton.FloatingActionButton.SIZE_NORMAL);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.windowBackground);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    }

    private void attachButtons(com.getbase.floatingactionbutton.FloatingActionButton... buttons){
        if (prevButtons != null) {
            for (com.getbase.floatingactionbutton.FloatingActionButton button : prevButtons) {
                mainMenu.removeButton(button);
            }
        }
        prevButtons = buttons;
        for (com.getbase.floatingactionbutton.FloatingActionButton button : buttons){
            mainMenu.addButton(button);
        }
    }

    private void reloadButton(){
        mPrefs = new SharedPrefs(this);
        final boolean isExtend = mPrefs.loadBoolean(Prefs.EXTENDED_BUTTON);
        /*if (mTag.matches(FRAGMENT_EVENTS) || mTag.matches(ACTION_CALENDAR)){
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
            }, getString(R.string.new_reminder), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_NORMAL, R.drawable.ic_alarm_add_grey600_24dp);

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
            }, getString(R.string.new_birthday), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI, R.drawable.ic_cake_grey600_24dp);
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
                    }, getString(R.string.new_reminder), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_NORMAL, R.drawable.ic_alarm_add_grey600_24dp);
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
                    }, getString(R.string.new_birthday), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI, R.drawable.ic_cake_grey600_24dp);
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
                    }, getString(R.string.new_task), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI, R.drawable.google_tasks_grey);
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
                    }, getString(R.string.new_note), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI, R.drawable.ic_event_note_grey600_24dp);
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
                    }, getString(R.string.new_quick_note), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI, R.drawable.ic_done_grey600_24dp);
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
                    }, getString(R.string.new_reminder), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_NORMAL, R.drawable.ic_alarm_add_grey600_24dp);
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
                        }, getString(R.string.string_new_place), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI, R.drawable.ic_location_on_grey600_24dp);
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
                                }, getString(R.string.string_new_category), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI,
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
                                }, getString(R.string.string_new_template), com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI,
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
        }*/
    }

    private void replace(Fragment fragment, String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
        //mTag = tag;
        mPrefs.savePrefs(Prefs.LAST_FRAGMENT, tag);
    }

    private void restoreUi(){
        toolbar.setBackgroundColor(cSetter.colorPrimary());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        boolean isExtended = mPrefs.loadBoolean(Prefs.EXTENDED_BUTTON);
        if (!isExtended) {
            mFab.setColorNormal(cSetter.colorPrimary());
            mFab.setColorPressed(cSetter.colorPrimaryDark());
        } else {
            mainMenu.setButtonColorNormal(cSetter.colorPrimary());
            mainMenu.setButtonColorPressed(cSetter.colorPrimaryDark());
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
        replace(calendarView, null);

        final FlextListener listener = new FlextListener() {

            @Override
            public void onClickDate(Date date, View view) {
                eventsDate = date;
                //onNavigationDrawerItemSelected(FRAGMENT_EVENTS);
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
                startActivity(new Intent(MainActivity.this, QuickAddReminder.class)
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
        //mTitle = getString(R.string.calendar_fragment);
        //toolbar.setTitle(mTitle);
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
                        SharedPrefs sPrefs = new SharedPrefs(MainActivity.this);
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

    protected Dialog thanksDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.thank_dialog_title))
                .setMessage((getString(R.string.thank_dialog_message) + " " +
                        getString(R.string.thank_dialog_greeting)))
                .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new SharedPrefs(MainActivity.this)
                                .saveBoolean(Prefs.THANKS_SHOWN, true);
                    }
                })
                .setCancelable(false)
                .create();
    }

    private void saveNote() {
        SyncHelper sHelp = new SyncHelper(MainActivity.this);
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
        NotesBase db = new NotesBase(MainActivity.this);
        db.open();
        Random r = new Random();
        int color = r.nextInt(15);
        final long id;
        if (mPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            id = db.saveNote(sHelp.encrypt(note), date, cSetter.getNoteColor(color), uuID, null, 5);
        } else {
            id = db.saveNote(note, date, cSetter.getNoteColor(color), uuID, null, 5);
        }

        new UpdatesHelper(MainActivity.this).updateNotesWidget();

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

        //if (mTag.matches(FRAGMENT_NOTE) || mTag.matches(FRAGMENT_ACTIVE))
        //    onNavigationDrawerItemSelected(mTag);
    }

    private void askNotification(final String note, final long id){
        mPrefs = new SharedPrefs(MainActivity.this);
        ViewUtils.showReveal(noteStatusCard, isAnimation);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notifier(MainActivity.this).showNoteNotification(note, id);
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
        mPrefs = new SharedPrefs(MainActivity.this);
        ViewUtils.showReveal(noteReminderCard, isAnimation);

        buttonReminderYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideReveal(noteReminderCard, isAnimation);
                DataBase db = new DataBase(MainActivity.this);
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
                        0, 0, 0, SyncHelper.generateID(), null, 0, null, 0, 0, 0, categoryId, null);
                new AlarmReceiver().setAlarm(MainActivity.this, remId);
                db.updateReminderDateTime(remId);
                new UpdatesHelper(MainActivity.this).updateWidget();
                NotesBase base = new NotesBase(MainActivity.this);
                base.open();
                base.linkToReminder(noteId, remId);
                base.close();
                //if (mTag.matches(FRAGMENT_NOTE) || mTag.matches(FRAGMENT_ACTIVE))
                 //   onNavigationDrawerItemSelected(mTag);
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
            Messages.toast(MainActivity.this, getString(R.string.recognizer_not_found_error_message));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

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
        Messages.toast(MainActivity.this, getString(R.string.press_again));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_day:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                eventsDate = calendar.getTime();
                //onNavigationDrawerItemSelected(FRAGMENT_EVENTS);
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
            thanksDialog().show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }

        if (sPrefs.loadBoolean(Prefs.STATUS_BAR_NOTIFICATION)){
            new Notifier(this).recreatePermanent();
        }

        isChangesShown();

        new DelayedAsync(this, null).execute();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reminder) {
            replace(ActiveFragment.newInstance(), null);
        } else if (id == R.id.nav_note) {
            replace(NotesFragment.newInstance(), null);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        if (new SharedPrefs(this).loadBoolean(Prefs.EXPORT_SETTINGS)){
            new SharedPrefs(this).savePrefsBackup();
        }
        super.onStop();
    }
}
