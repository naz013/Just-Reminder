package com.cray.software.justreminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.adapters.QuickReturnRecyclerViewOnScrollListener;
import com.cray.software.justreminder.adapters.RemindersRecyclerAdapter;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.async.GetExchangeTasksAsync;
import com.cray.software.justreminder.async.LicenseCheckTask;
import com.cray.software.justreminder.async.SyncTask;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.databases.SystemData;
import com.cray.software.justreminder.datas.ReminderItem;
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.CategoriesList;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.dialogs.PlacesList;
import com.cray.software.justreminder.dialogs.RateDialog;
import com.cray.software.justreminder.dialogs.TemplatesList;
import com.cray.software.justreminder.dialogs.utils.ImportContacts;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.QuickReturnUtils;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SecurePreferences;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.CollapseListener;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.hexrain.design.fragments.ReminderPreviewFragment;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SyncListener, LicenseCheckerCallback, CollapseListener {

    RecyclerView currentList;
    LinearLayout emptyLayout;
    ImageView basket;
    RelativeLayout ads_container;
    DataBase DB = new DataBase(MainActivity.this);
    UpdatesHelper updatesHelper;
    TextView archiveScreen, activeScreen, fragmentSettings, geoScreen, calendar,
            manageBackup, notes, help, helpTranslate, googleTasks, moreApps, templates, places,
            categories;
    FloatingEditText quickNote;

    AlarmReceiver alarm = new AlarmReceiver();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    NotificationManager mNotifyMgr;

    ColorSetter cSetter = new ColorSetter(MainActivity.this);
    SharedPrefs sPrefs = new SharedPrefs(MainActivity.this);
    Typeface typeface;
    private AdView adView;
    RelativeLayout drawerBg;
    private static final String MARKET_APP_PRO = "com.cray.software.justreminderpro";

    boolean licensed;
    boolean checkingLicense;
    boolean didCheck;

    boolean isOpened = false;

    Toolbar toolbar;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private boolean shouldGoInvisible;
    ImageView emptyImage;
    TextView appNameBanner;
    CardView noteCard, noteStatusCard, noteReminderCard;
    TextView buttonYes, buttonNo, buttonReminderYes, buttonReminderNo, buttonSave;

    FloatingActionsMenu mainMenu;
    FloatingActionButton addNote, addBirthday, addTask, addReminder, addQuick, mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cSetter = new ColorSetter(MainActivity.this);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.new_activity_main);
        setRequestedOrientation(cSetter.getRequestOrientation());

        getIntent().setAction("JustActivity Created");

        sPrefs = new SharedPrefs(MainActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (mainMenu.isExpanded()) mainMenu.collapse();
                        switch (item.getItemId()) {
                            case R.id.action_refresh:
                                if (isOpened) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDrawerLayout.closeDrawers();
                                        }
                                    });
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startSync();
                                        }
                                    }, 200);
                                } else startSync();
                                return true;
                            case R.id.action_voice:
                                if (isOpened) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDrawerLayout.closeDrawers();
                                        }
                                    });
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startVoiceRecognitionActivity();
                                        }
                                    }, 200);
                                } else startVoiceRecognitionActivity();
                                return true;
                            case R.id.action_order:
                                if (isOpened) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDrawerLayout.closeDrawers();
                                        }
                                    });
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    }, 200);
                                } else showDialog();
                                return true;
                            case R.id.action_filter:
                                if (isOpened) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDrawerLayout.closeDrawers();
                                        }
                                    });
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            filterDialog();
                                        }
                                    }, 200);
                                } else filterDialog();
                                return true;
                        }
                        return true;
                    }
                });

        toolbar.inflateMenu(R.menu.main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(R.string.drawer_active_reminder);

        drawerBg = (RelativeLayout) findViewById(R.id.drawerBg);
        drawerBg.setBackgroundColor(cSetter.getBackgroundStyle());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setBackgroundColor(cSetter.getBackgroundStyle());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.app_title, R.string.drawer_active_reminder) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                currentList.setEnabled(true);
                shouldGoInvisible = false;
                invalidateOptionsMenu();
                isOpened = false;
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                currentList.setEnabled(false);
                shouldGoInvisible = true;
                invalidateOptionsMenu();
                isOpened = true;
            }

            float mPreviousOffset = 0f;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(slideOffset > mPreviousOffset && !shouldGoInvisible){
                    shouldGoInvisible = true;
                    invalidateOptionsMenu();
                }else if(mPreviousOffset > slideOffset && slideOffset < 0.5f && shouldGoInvisible){
                    shouldGoInvisible = false;
                    invalidateOptionsMenu();
                }
                mPreviousOffset = slideOffset;
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        findViewById(R.id.emptyItem).setVisibility(View.VISIBLE);

        emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            emptyImage.setImageResource(R.drawable.bell_icon);
        } else {
            emptyImage.setImageResource(R.drawable.bell_icon_dark);
        }

        currentList = (RecyclerView) findViewById(R.id.currentList);

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

        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        appNameBanner = (TextView) findViewById(R.id.appNameBanner);
        appNameBanner.setTypeface(typeface);
        if (new ManageModule().isPro()) {
            appNameBanner.setText(R.string.app_name_pro);
        } else {
            appNameBanner.setText(R.string.app_name);
        }

        new GetExchangeTasksAsync(MainActivity.this, null).execute();

        fragmentSettings = (TextView) findViewById(R.id.fragmentSettings);
        fragmentSettings.setOnClickListener(this);

        geoScreen = (TextView) findViewById(R.id.geoScreen);
        geoScreen.setOnClickListener(this);

        manageBackup = (TextView) findViewById(R.id.manageBackup);
        manageBackup.setOnClickListener(this);

        calendar = (TextView) findViewById(R.id.calendar);
        calendar.setOnClickListener(this);

        notes = (TextView) findViewById(R.id.notes);
        notes.setOnClickListener(this);

        googleTasks = (TextView) findViewById(R.id.googleTasks);
        if (new GTasksHelper(MainActivity.this).isLinked()) googleTasks.setVisibility(View.VISIBLE);
        googleTasks.setOnClickListener(this);

        activeScreen = (TextView) findViewById(R.id.activeScreen);
        activeScreen.setOnClickListener(this);
        activeScreen.setEnabled(false);

        archiveScreen = (TextView) findViewById(R.id.archiveScreen);
        archiveScreen.setVisibility(View.VISIBLE);
        archiveScreen.setOnClickListener(this);

        help = (TextView) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (checkGooglePlayServicesAvailability()) {
                            startActivity(new Intent(MainActivity.this, Help.class));
                        }
                    }
                }, 250);
            }
        });
        help.setTypeface(typeface);

        helpTranslate = (TextView) findViewById(R.id.helpTranslate);
        helpTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        translationDialog().show();
                    }
                }, 250);
            }
        });
        helpTranslate.setTypeface(typeface);

        moreApps = (TextView) findViewById(R.id.moreApps);
        moreApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://search?q=pub:Nazar Suhovich"));
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "Couldn't launch market", Toast.LENGTH_LONG).show();
                        }
                    }
                }, 250);
            }
        });
        moreApps.setTypeface(typeface);

        categories = (TextView) findViewById(R.id.categories);
        categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, CategoriesList.class));
                    }
                }, 250);
            }
        });
        categories.setTypeface(typeface);

        places = (TextView) findViewById(R.id.places);
        places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, PlacesList.class));
                    }
                }, 250);
            }
        });
        places.setTypeface(typeface);

        templates = (TextView) findViewById(R.id.templates);
        templates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                if (mainMenu.isExpanded()) mainMenu.collapse();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, TemplatesList.class));
                    }
                }, 250);
            }
        });
        templates.setTypeface(typeface);
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.queryTemplates();
        if (c != null && c.moveToFirst() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_QUICK_SMS)){
            templates.setVisibility(View.VISIBLE);
        }
        if (!DB.isOpen()) DB.open();
        c = DB.queryPlaces();
        if (c != null && c.moveToFirst()){
            places.setVisibility(View.VISIBLE);
        }
        if (c != null) c.close();

        fragmentSettings.setTypeface(typeface);
        geoScreen.setTypeface(typeface);
        calendar.setTypeface(typeface);
        notes.setTypeface(typeface);
        googleTasks.setTypeface(typeface);
        activeScreen.setTypeface(typeface);
        archiveScreen.setTypeface(typeface);
        manageBackup.setTypeface(typeface);
        googleTasks.setTypeface(typeface);

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
                        startActivity(new Intent(MainActivity.this, NotesManager.class));
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
                if (new GTasksHelper(MainActivity.this).isLinked()) {
                    if (isNoteVisible()) {
                        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS)) {
                            hide(noteCard);
                        } else noteCard.setVisibility(View.GONE);
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            long ids = 0;
                            startActivity(new Intent(MainActivity.this, TaskManager.class)
                                    .putExtra(Constants.ITEM_ID_INTENT, ids)
                                    .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
                        }
                    }, 150);

                } else
                    Toast.makeText(MainActivity.this, getString(R.string.tasks_connection_warming), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, getString(R.string.calendar_birthday_info), Toast.LENGTH_LONG).show();
                else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, AddBirthday.class));
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
                        startActivity(new Intent(MainActivity.this, ReminderManager.class));
                    }
                }, 150);
            }
        });

        mainMenu.addButton(addBirthday);
        mainMenu.addButton(addTask);
        mainMenu.addButton(addNote);
        mainMenu.addButton(addQuick);
        mainMenu.addButton(addReminder);

        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON)){
            mFab = new AddFloatingActionButton(MainActivity.this);
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
                            startActivity(new Intent(MainActivity.this, ReminderManager.class));
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

            RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
            wrapper.addView(mFab);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mainMenu.setVisibility(View.GONE);
        } else {
            mainMenu.setVisibility(View.VISIBLE);
        }

        if (!new ManageModule().isPro()){
            emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
            emptyLayout.setVisibility(View.GONE);

            adView = (AdView) findViewById(R.id.adView);
            adView.setVisibility(View.GONE);

            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    adView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    emptyLayout.setVisibility(View.VISIBLE);
                    adView.setVisibility(View.VISIBLE);
                }
            });

            ads_container = (RelativeLayout) findViewById(R.id.ads_container);
            basket = (ImageView) findViewById(R.id.basket);
            if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
                basket.setImageResource(R.drawable.market_icon_white);
            } else basket.setImageResource(R.drawable.market_icon);

            showMarket();

            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG)){
                startActivity(new Intent(MainActivity.this, ImportContacts.class));
            }
        }
    }

    private void showMarket() {
        if (!isAppInstalled(MARKET_APP_PRO)){
            ads_container.setVisibility(View.VISIBLE);
            ads_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawers();
                        }
                    });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            marketDialog().show();
                        }
                    }, 250);
                }
            });
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
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
                        intent.setData(Uri.parse("market://details?id=" + MARKET_APP_PRO));
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

    ArrayList<String> ids;
    private void filterDialog(){
        ids = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.select_dialog_item);
        if (DB != null) DB.open();
        else {
            DB = new DataBase(MainActivity.this);
            DB.open();
        }
        arrayAdapter.add(getString(R.string.simple_all));
        Cursor c = DB.queryCategories();
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                arrayAdapter.add(title);
                ids.add(catId);
            } while (c.moveToNext());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.string_select_category));
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) loaderAdapter(null);
                else {
                    String catId = ids.get(which - 1);
                    loaderAdapter(catId);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialog(){
        final CharSequence[] items = {getString(R.string.sort_item_by_date_a_z),
                getString(R.string.sort_item_by_date_z_a),
                getString(R.string.sort_item_by_date_without_a_z),
                getString(R.string.sort_item_by_date_without_z_a)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.menu_order_by));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(MainActivity.this);
                if (item == 0) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_A_Z);
                } else if (item == 1) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_Z_A);
                } else if (item == 2) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_A_Z);
                } else if (item == 3) {
                    prefs.savePrefs(Constants.APP_UI_PREFERENCES_LIST_ORDER, Constants.ORDER_DATE_WITHOUT_DISABLED_Z_A);
                }
                dialog.dismiss();
                loaderAdapter(null);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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
                        sPrefs = new SharedPrefs(MainActivity.this);
                        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU, true);
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create();
    }

    private void toggle(long id) {
        if (!DB.isOpen()) DB.open();
        Cursor c = DB.getTask(id);
        if (c != null && c.moveToFirst()) {
            int done = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
            if (done == 0) {
                DB.setDone(id);
                AlarmReceiver alarm = new AlarmReceiver();
                WeekDayReceiver week = new WeekDayReceiver();
                DelayReceiver delayReceiver = new DelayReceiver();
                Integer i = (int) (long) id;
                alarm.cancelAlarm(MainActivity.this, i);
                week.cancelAlarm(MainActivity.this, i);
                delayReceiver.cancelAlarm(MainActivity.this, id);
                new RepeatNotificationReceiver().cancelAlarm(MainActivity.this, i);
                new PositionDelayReceiver().cancelDelay(MainActivity.this, i);
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(i);
                loaderAdapter(null);
                new DisableAsync(MainActivity.this).execute();
            } else {
                String type;
                int hour;
                int minute;
                int seconds;
                int day;
                int month;
                int year;
                int repCode;
                int repTime;
                int repCount;
                repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                    DB.setUnDone(id);
                    DB.updateDateTime(id);
                    new WeekDayReceiver().setAlarm(MainActivity.this, id);
                    loaderAdapter(null);
                } else if (type.startsWith(Constants.TYPE_LOCATION)) {
                    DB.setUnDone(id);
                    DB.updateDateTime(id);
                    if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0) {
                        startService(new Intent(MainActivity.this, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        startService(new Intent(MainActivity.this, CheckPosition.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        new PositionDelayReceiver().setDelay(MainActivity.this, id);
                    }
                    loaderAdapter(null);
                } else {
                    if (type.matches(Constants.TYPE_TIME)){
                        final Calendar calendar1 = Calendar.getInstance();
                        int myYear = calendar1.get(Calendar.YEAR);
                        int myMonth = calendar1.get(Calendar.MONTH);
                        int myDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int myHour = calendar1.get(Calendar.HOUR_OF_DAY);
                        int myMinute = calendar1.get(Calendar.MINUTE);
                        int mySeconds = calendar1.get(Calendar.SECOND);
                        DB.updateStartTime(id, myDay, myMonth, myYear, myHour, myMinute, mySeconds);
                        DB.updateDateTime(id);
                        new AlarmReceiver().setAlarm(MainActivity.this, id);
                        loaderAdapter(null);
                    } else {
                        if (new TimeCount(MainActivity.this)
                                .getNextDate(year, month, day, hour, minute, seconds, repTime, repCode, repCount)) {
                            DB.setUnDone(id);
                            DB.updateDateTime(id);
                            new AlarmReceiver().setAlarm(MainActivity.this, id);
                            loaderAdapter(null);
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.edit_reminder_toast), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            new Notifier(MainActivity.this).recreatePermanent();
            new UpdatesHelper(MainActivity.this).updateWidget();
        }
        if (c != null) c.close();
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
        SimpleDateFormat full24Format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date = full24Format.format(calendar1.getTime());

        String uuID = sHelp.generateID();
        NotesBase db = new NotesBase(MainActivity.this);
        db.open();
        Random r = new Random();
        int color = r.nextInt(15);
        final long id;
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
            id = db.saveNote(sHelp.encrypt(note), date, cSetter.getNoteColor(color), uuID, null, 5);
        } else {
            id = db.saveNote(note, date, cSetter.getNoteColor(color), uuID, null, 5);
        }

        new UpdatesHelper(MainActivity.this).updateNotesWidget();

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

        loaderAdapter(null);
    }

    private void askNotification(final String note, final long id){
        sPrefs = new SharedPrefs(MainActivity.this);
        final boolean animate = sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS);
        if (animate) {
            show(noteStatusCard);
        } else noteStatusCard.setVisibility(View.VISIBLE);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notifier(MainActivity.this).showNoteNotification(note, id);
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
        sPrefs = new SharedPrefs(MainActivity.this);

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

                SyncHelper sHelp = new SyncHelper(MainActivity.this);
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
                alarm.setAlarm(MainActivity.this, remId);
                DB.updateDateTime(remId);
                new UpdatesHelper(MainActivity.this).updateWidget();
                NotesBase base = new NotesBase(MainActivity.this);
                base.open();
                base.linkToReminder(noteId, remId);
                base.close();
                loaderAdapter(null);
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

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sPrefs = new SharedPrefs(MainActivity.this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            getIntent().setAction("JustActivity Created");
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognizer(MainActivity.this).selectTask(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setImage(){
        sPrefs = new SharedPrefs(MainActivity.this);
        if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)){
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_grey600_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_storage_grey600_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_grey600_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_grey600_24dp, 0, 0, 0);
            fragmentSettings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_grey600_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_grey600_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_grey600_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_tasks_grey, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_grey600_24dp, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on_grey600_24dp, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_grey600_24dp, 0, 0, 0);
        } else {
            activeScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_white_24dp, 0, 0, 0);
            archiveScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_storage_white_24dp, 0, 0, 0);
            calendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_white_24dp, 0, 0, 0);
            geoScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            fragmentSettings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings_white_24dp, 0, 0, 0);
            manageBackup.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_history_white_24dp, 0, 0, 0);
            notes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_white_24dp, 0, 0, 0);
            googleTasks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google_tasks_white, 0, 0, 0);
            templates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_textsms_white_24dp, 0, 0, 0);
            places.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on_white_24dp, 0, 0, 0);
            categories.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_white_24dp, 0, 0, 0);
        }
    }

    @Override
    protected void onStop() {
        new DisableAsync(MainActivity.this).execute();
        super.onStop();
    }

    private void makeArchive(long id){
        if (!DB.isOpen()) DB.open();
        mNotifyMgr =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        DB.toArchive(id);
        new DisableAsync(MainActivity.this).execute();
    }

    ArrayList<ReminderItem> arrayList;

    public void loaderAdapter(String categoryId){
        if (!DB.isOpen()) DB.open();
        Cursor c;
        if (categoryId != null) {
            c =  DB.queryGroup(categoryId);
        } else {
            c =  DB.queryGroup();
        }

        SyncHelper helper = new SyncHelper(this);

        arrayList = new ArrayList<>();
        arrayList.clear();

        if (c != null && c.moveToFirst()){
            findViewById(R.id.emptyItem).setVisibility(View.GONE);
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String catId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String uuId = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                int repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
                long mId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));

                TimeCount mCount = new TimeCount(this);
                Interval mInterval = new Interval(this);
                long due;
                String repeat = null;
                if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                    due = mCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                            repCode, repCount, delay);
                    if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                            type.startsWith(Constants.TYPE_APPLICATION)) {
                        repeat = mInterval.getInterval(repCode);
                    } else if (type.matches(Constants.TYPE_TIME)) {
                        repeat = mInterval.getTimeInterval(repCode);
                    } else {
                        repeat = getString(R.string.interval_zero);
                    }
                } else {
                    due = mCount.getNextWeekdayTime(hour, minute, weekdays, delay);
                    if (weekdays.length() == 7) {
                        repeat = helper.getRepeatString(weekdays);
                    }
                }

                arrayList.add(new ReminderItem(title, type, repeat, catId, uuId, isDone, due, mId,
                        new double[]{lat, lon}, number));
            } while (c.moveToNext());
        } else findViewById(R.id.emptyItem).setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        RecyclerViewSwipeManager mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final RemindersRecyclerAdapter myItemAdapter = new RemindersRecyclerAdapter(this, arrayList);
        myItemAdapter.setEventListener(new RemindersRecyclerAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                onRemovedEdit(position);
            }

            @Override
            public void onItemPinned(int position) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                onPinnedDelete(position);
            }

            @Override
            public void onItemClicked(int position) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                final long id = arrayList.get(position).getId();
                if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)) {
                    startActivity(new Intent(MainActivity.this, ReminderPreviewFragment.class)
                            .putExtra(Constants.EDIT_ID, id));
                } else {
                    toggle(id);
                }
            }

            @Override
            public void onItemLongClicked(int position) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                final long id = arrayList.get(position).getId();
                editReminder(id);
            }

            @Override
            public void toggleItem(int position) {
                if (mainMenu.isExpanded()) mainMenu.collapse();
                final long id = arrayList.get(position).getId();
                toggle(id);
            }

            @Override
            public void onItemViewClicked(View v, boolean isPinned) {

            }
        });

        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);

        currentList.setLayoutManager(mLayoutManager);
        currentList.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        currentList.setItemAnimator(animator);

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(currentList);
        mRecyclerViewSwipeManager.attachRecyclerView(currentList);

        QuickReturnRecyclerViewOnScrollListener scrollListener = new
                QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON) ? mainMenu : mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .listener(this)
                .isSnappable(true)
                .build();
        if (mWrappedAdapter.getItemCount() > 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                currentList.addOnScrollListener(scrollListener);
            } else {
                currentList.setOnScrollListener(scrollListener);
            }
        }
    }

    public void onRemovedEdit(int position) {
        final long id = arrayList.get(position).getId();
        new AlarmReceiver().cancelAlarm(MainActivity.this, id);
        new WeekDayReceiver().cancelAlarm(MainActivity.this, id);
        new DelayReceiver().cancelAlarm(MainActivity.this, id);
        new PositionDelayReceiver().cancelDelay(MainActivity.this, id);
        editReminder(id);
    }

    public void onPinnedDelete(int position) {
        final long id = arrayList.get(position).getId();
        new AlarmReceiver().cancelAlarm(MainActivity.this, id);
        new WeekDayReceiver().cancelAlarm(MainActivity.this, id);
        new DelayReceiver().cancelAlarm(MainActivity.this, id);
        new PositionDelayReceiver().cancelDelay(MainActivity.this, id);
        disableReminder(id);
    }

    private void editReminder(long id){
        Intent intentId = new Intent(MainActivity.this, ReminderManager.class);
        if (id != 0) {
            intentId.putExtra(Constants.EDIT_ID, id);
            alarm.cancelAlarm(MainActivity.this, id);
            new WeekDayReceiver().cancelAlarm(MainActivity.this, id);
            new DelayReceiver().cancelAlarm(MainActivity.this, id);
            new PositionDelayReceiver().cancelDelay(MainActivity.this, id);
            startActivity(intentId);
            new DisableAsync(MainActivity.this).execute();
        }
    }

    private void disableReminder(long id){
        if (id != 0) {
            makeArchive(id);
            updatesHelper = new UpdatesHelper(MainActivity.this);
            updatesHelper.updateWidget();

            Toast.makeText(MainActivity.this,
                    getString(R.string.archived_result_message),
                    Toast.LENGTH_SHORT).show();

            loaderAdapter(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private void startSync(){
        new SyncTask(this, this).execute();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = shouldGoInvisible;
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void hideMenuItems(Menu menu, boolean visible) {
        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    protected void onResume() {
        String action = getIntent().getAction();
        if(action == null || !action.equals("JustActivity Created")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            getIntent().setAction(null);
        }

        loaderAdapter(null);

        setRequestedOrientation(cSetter.getRequestOrientation());
        setImage();

        showRate();

        if (!new ManageModule().isPro()){
            if (adView != null) {
                adView.resume();
            }
        } else {
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)) {
                thanksDialog().show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)){
            new Notifier(MainActivity.this).recreatePermanent();
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG) &&
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)) {
            isChangesShown();
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU)) {
            helpTranslate.setVisibility(View.GONE);
        }

        if (!DB.isOpen()) DB.open();
        if (DB.getCountActive() > 0){
            if (isListFirstTime() && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)){
                startActivity(new Intent(MainActivity.this, HelpOverflow.class)
                        .putExtra(Constants.ITEM_ID_INTENT, 1));
            }
        }

        isOpened = false;

        new DelayedAsync(MainActivity.this, null).execute();

        super.onResume();
    }

    private boolean isListFirstTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("JustListBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("JustListBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }

    private void showChanges() {
        startActivity(new Intent(MainActivity.this, ChangeDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void isChangesShown() {
        sPrefs = new SharedPrefs(MainActivity.this);
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

    private void startChecking(){
        SystemData systemData = new SystemData(MainActivity.this);
        systemData.open();
        Cursor key = systemData.getKey(encrypt(Constants.DEVICE_ID_KEY));
        if (key != null && key.moveToFirst()){
            String deviceIdDb = decrypt(key.getString(key.getColumnIndex(SystemData.COLUMN_NAME)));
            long keyId = key.getLong(key.getColumnIndex(SystemData.COLUMN_ID));
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId.matches(deviceIdDb)){
                SecurePreferences preferences = new SecurePreferences(getApplicationContext(), LICENCE_PREFS_NAME, LICENCE_SECURE_KEY, true);
                Cursor l = systemData.getKey(encrypt(Constants.LICENSE_KEY));
                if (l != null && l.moveToFirst()){
                    String licenseKey = decrypt(l.getString(l.getColumnIndex(SystemData.COLUMN_NAME)));
                    boolean isLicensed = preferences.containsKey(licenseKey);
                    if (isLicensed){
                        String licStatus = preferences.getString(licenseKey);
                        if (!licStatus.matches("appIsLicensed")){
                            Cursor v = systemData.queryKeys();
                            if (v != null && v.moveToFirst()){
                                do{
                                    long id = v.getLong(v.getColumnIndex(SystemData.COLUMN_ID));
                                    systemData.deleteKey(id);
                                } while (v.moveToNext());
                            }
                            startCheckingLicence();
                        }
                    } else {
                        Cursor v = systemData.queryKeys();
                        if (v != null && v.moveToFirst()){
                            do{
                                long id = v.getLong(v.getColumnIndex(SystemData.COLUMN_ID));
                                systemData.deleteKey(id);
                            } while (v.moveToNext());
                        }
                        startCheckingLicence();
                    }
                } else {
                    Cursor v = systemData.queryKeys();
                    if (v != null && v.moveToFirst()){
                        do{
                            long id = v.getLong(v.getColumnIndex(SystemData.COLUMN_ID));
                            systemData.deleteKey(id);
                        } while (v.moveToNext());
                    }
                    startCheckingLicence();
                }

            } else {
                systemData.deleteKey(keyId);
                Cursor v = systemData.queryKeys();
                if (v != null && v.moveToFirst()){
                    do{
                        long id = v.getLong(v.getColumnIndex(SystemData.COLUMN_ID));
                        systemData.deleteKey(id);
                    } while (v.moveToNext());
                }
                startCheckingLicence();
            }
        } else {
            startCheckingLicence();
        }
    }

    private String decrypt(String string){
        String result = "";
        byte[] byte_string = Base64.decode(string, Base64.DEFAULT);
        try {
            result = new String(byte_string, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    private String encrypt(String string){
        byte[] string_byted = null;
        try {
            string_byted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim();
    }

    private void showRate(){
        sPrefs = new SharedPrefs(MainActivity.this);

        if (sPrefs.isString(Constants.APP_UI_PREFERENCES_RATE_SHOW)) {
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_RATE_SHOW)) {
                int counts = sPrefs.loadInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT);
                if (counts < 10) {
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, counts + 1);
                } else {
                    sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, 0);
                    startActivity(new Intent(MainActivity.this, RateDialog.class));
                }
            }
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_RATE_SHOW, false);
            sPrefs.saveInt(Constants.APP_UI_PREFERENCES_APP_RUNS_COUNT, 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.archiveScreen:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent openArchive = new Intent(MainActivity.this, ArchiveActivity.class);
                        startActivity(openArchive);
                        finish();
                    }
                }, 250);
                break;
            case R.id.fragmentSettings:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intentS = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intentS);
                    }
                }, 250);
                break;
            case R.id.geoScreen:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (checkGooglePlayServicesAvailability()) {
                            startActivity(new Intent(MainActivity.this, GeolocationTasks.class));
                        }
                    }
                }, 250);
                break;
            case R.id.notes:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, NotesActivity.class));
                    }
                }, 250);
                break;
            case R.id.googleTasks:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, TasksActivity.class));
                    }
                }, 250);
                break;
            case R.id.calendar:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                    }
                }, 250);
                break;
            case R.id.manageBackup:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, BackupManager.class));
                    }
                }, 250);
                break;
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

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (isOpened) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();
                }
            });
            return;
        }
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

    @Override
    public void endExecution(boolean result) {
        if (result)
            loaderAdapter(null);
    }

    private void startCheckingLicence(){
        Log.d(Constants.LOG_TAG, "License task started");
        new LicenseCheckTask(MainActivity.this, this).execute();
    }

    private final static String LICENCE_PREFS_NAME = "security_application_system";
    private final static String LICENCE_SECURE_KEY =
            "Ljubov_Myhalckuck_kbhfdkishgbdsjhfbskjhfaajsdfbdskfsdjbfvsdgbfjdshbfjsdagbfkufageufhi78ryewfahge8ofr98ryew97yf9ewyr8ye78tf9ryhadsfgkjadsbfjasdvf";

    @Override
    public void allow(int reason) {
        if (isFinishing()) {
            return;
        }
        SecurePreferences preferences = new SecurePreferences(getApplicationContext(), LICENCE_PREFS_NAME, LICENCE_SECURE_KEY, true);
        SystemData systemData = new SystemData(MainActivity.this);
        systemData.open();
        String dialogKey = "";
        String licenseKey = "";
        sPrefs = new SharedPrefs(MainActivity.this);
        Cursor s = systemData.getKey(encrypt(Constants.LICENSE_DIALOG_KEY));
        if (s != null && s.moveToFirst()){
            dialogKey = decrypt(s.getString(s.getColumnIndex(SystemData.COLUMN_NAME)));
        }

        Cursor l = systemData.getKey(encrypt(Constants.LICENSE_KEY));
        if (l != null && l.moveToFirst()){
            licenseKey = decrypt(l.getString(l.getColumnIndex(SystemData.COLUMN_NAME)));
        }
        if (l != null) l.close();
        Log.d(Constants.LOG_TAG, "License allow");
        boolean licenseStatus = preferences.containsKey(dialogKey);
        if (!licenseStatus){
            preferences.put(dialogKey, "isTrue");
            preferences.put(licenseKey, "appIsLicensed");
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG)){
                thanksDialog().show();
            }
        } else {
            String dialog = preferences.getString(dialogKey);
            if (dialog.matches("isFalse")){
                preferences.put(dialogKey, "isTrue");
                preferences.put(licenseKey, "appIsLicensed");
                if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG)){
                    thanksDialog().show();
                }
            }
        }
        licensed = true;
        checkingLicense = false;
        didCheck = true;
    }

    @Override
    public void dontAllow(int reason) {
        if (isFinishing()) {
            return;
        }
        SystemData systemData = new SystemData(MainActivity.this);
        systemData.open();
        String dialogKey = "";
        String licenseKey = "";
        Cursor s = systemData.getKey(encrypt(Constants.LICENSE_DIALOG_KEY));
        if (s != null && s.moveToFirst()){
            dialogKey = decrypt(s.getString(s.getColumnIndex(SystemData.COLUMN_NAME)));
        }
        Log.d(Constants.LOG_TAG, "License don't allow");
        Cursor l = systemData.getKey(encrypt(Constants.LICENSE_KEY));
        if (l != null && l.moveToFirst()){
            licenseKey = decrypt(l.getString(l.getColumnIndex(SystemData.COLUMN_NAME)));
        }

        SecurePreferences preferences = new SecurePreferences(getApplicationContext(), LICENCE_PREFS_NAME, LICENCE_SECURE_KEY, true);
        preferences.put(licenseKey, "appIsNotLicensed");
        preferences.put(dialogKey, "isFalse");
        licensed = false;
        checkingLicense = false;
        didCheck = true;
        if (!checkDialog().isShowing()) {
            checkDialog().show();
        }
    }

    @Override
    public void applicationError(int reason) {
        if (isFinishing()) {
            return;
        }
        licensed = true;
        checkingLicense = false;
        didCheck = false;
        Log.d(Constants.LOG_TAG, "License error");
        if (!checkDialog().isShowing()) {
            checkDialog().show();
        }
    }

    protected Dialog checkDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.not_licensed_dialog_title))
                .setMessage(getString(R.string.not_licensed_dialog_message))
                .setPositiveButton(getString(R.string.license_dialog_buy_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.license_dialog_close_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton(getString(R.string.dialog_button_retry), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startCheckingLicence();
                    }
                })
                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        finish();
                        return true;
                    }
                })
                .create();
    }

    protected Dialog thanksDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.thank_dialog_title))
                .setMessage(getString(R.string.thank_dialog_message) + " " +
                        getString(R.string.thank_dialog_greeting))
                .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN, true);
                    }
                })
                .setCancelable(false)
                .create();
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

    @Override
    protected void onDestroy() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.destroy();
            }
        }
        if (DB != null && DB.isOpen()) DB.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (!new ManageModule().isPro()) {
            if (adView != null) {
                adView.pause();
            }
        }
        super.onPause();
    }

    @Override
    public void onStartScroll(boolean result) {
        if (mainMenu.isExpanded()) mainMenu.collapse();
    }
}