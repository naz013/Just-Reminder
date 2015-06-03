package com.hexrain.design;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.Help;
import com.cray.software.justreminder.NotesManager;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.SettingsActivity;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.async.DelayedAsync;
import com.cray.software.justreminder.async.GetExchangeTasksAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.dialogs.RateDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Recognizer;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.modules.ManageModule;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.views.FloatingEditText;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hexrain.design.fragments.ActiveFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class ScreenManager extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTag;
    Toolbar toolbar;
    FloatingEditText quickNote;
    CardView noteCard, noteStatusCard, noteReminderCard;
    TextView buttonYes, buttonNo, buttonReminderYes, buttonReminderNo, buttonSave;

    FloatingActionsMenu mainMenu;
    FloatingActionButton addNote, addBirthday, addTask, addReminder, addQuick, mFab;

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
    public static final String FRAGMENT_CALENDAR = "fragment_calendar";
    public static final String HELP = "help";
    public static final String TRANSLATION = "translation";
    public static final String MORE_APPS = "more_apps";
    public static final String MARKET = "market";
    public static final String VOICE_RECOGNIZER = "sync_reminder";
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cSetter.getStyle());
        setContentView(R.layout.activity_screen_manager);
        setRequestedOrientation(cSetter.getRequestOrientation());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        getIntent().setAction("JustActivity Created");

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.inflateMenu(R.menu.main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(R.string.drawer_active_reminder);

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
                            long ids = 0;
                            startActivity(new Intent(ScreenManager.this, TaskManager.class)
                                    .putExtra(Constants.ITEM_ID_INTENT, ids)
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
                        startActivity(new Intent(ScreenManager.this, ReminderManager.class));
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
            mainMenu.setVisibility(View.GONE);
        } else {
            mainMenu.setVisibility(View.VISIBLE);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        new GetExchangeTasksAsync(this, null).execute();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
    }

    @Override
    public void onNavigationDrawerItemSelected(String tag) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (tag.matches(FRAGMENT_ACTIVE)) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ActiveFragment.newInstance(), tag)
                    .commit();
        } else if (tag.matches(HELP)){
            startActivity(new Intent(this, Help.class));
        } else if (tag.matches(TRANSLATION)){
            translationDialog().show();
        } else if (tag.matches(MARKET)){
            marketDialog().show();
        } else if (tag.matches(MORE_APPS)){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://search?q=pub:Nazar Suhovich"));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Couldn't launch market", Toast.LENGTH_LONG).show();
            }
        } else if (tag.matches(FRAGMENT_SETTINGS)){
            Intent intentS = new Intent(this, SettingsActivity.class);
            startActivity(intentS);
        } else if (tag.matches(VOICE_RECOGNIZER)){
            startVoiceRecognitionActivity();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ActiveFragment.newInstance(), tag)
                    .commit();
        }
    }

    public void onSectionAttached(String tag) {
        if (tag.matches(FRAGMENT_ACTIVE)) {
            mTitle = getString(R.string.drawer_active_reminder);
        } else {
            mTitle = getString(R.string.title_section1);
        }
        toolbar.setTitle(mTitle);
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
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            //getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        if(action == null || !action.equals("JustActivity Created")) {
            Intent intent = new Intent(this, ScreenManager.class);
            startActivity(intent);
            finish();
        } else {
            getIntent().setAction(null);
        }

        setRequestedOrientation(cSetter.getRequestOrientation());

        showRate();

        SharedPrefs sPrefs = new SharedPrefs(this);
        if (new ManageModule().isPro()){
            if (!sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)) {
                thanksDialog().show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION)){
            new Notifier(this).recreatePermanent();
        }

        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG) &&
                sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_THANKS_SHOWN)) {
            isChangesShown();
        }

        new DelayedAsync(this, null).execute();
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

        //loaderAdapter(null);
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
                //loaderAdapter(null);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            getIntent().setAction("JustActivity Created");
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            new Recognizer(this).selectTask(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
