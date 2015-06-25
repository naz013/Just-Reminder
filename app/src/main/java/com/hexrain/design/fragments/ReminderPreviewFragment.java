package com.hexrain.design.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.async.DeleteReminder;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.async.SwitchTaskAsync;
import com.cray.software.justreminder.async.TaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.databases.NotesBase;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.ItemData;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.TasksConstants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.CheckPosition;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.GeolocationService;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.RepeatNotificationReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.utils.Utils;
import com.cray.software.justreminder.views.CircularProgress;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderPreviewFragment extends AppCompatActivity {

    ColorSetter cSetter;
    SharedPrefs sPrefs;
    Toolbar toolbar;
    FloatingActionButton mFab;
    TextView task, statusText, time, location, group, type, number, repeat, melody;
    RelativeLayout switchWrapper;
    SwitchCompat statusSwitch;
    LinearLayout tasksContainer, notesContainer, mapContainer, background;
    TextView listColor, taskText, taskNote, taskDate, noteText;
    CheckBox checkDone;
    ImageView imageView;

    DataBase db;
    private long id;
    CircularProgress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.activity_reminder_preview_fragment);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle("");

        sPrefs = new SharedPrefs(this);

        id = getIntent().getLongExtra(Constants.EDIT_ID, 0);

        findViewById(R.id.windowBackground).setBackgroundColor(cSetter.getBackgroundStyle());

        mFab = new FloatingActionButton(this);
        mFab.setSize(FloatingActionButton.SIZE_MINI);
        mFab.setIcon(R.drawable.ic_create_white_24dp);
        mFab.setColorNormal(cSetter.colorStatus());
        mFab.setColorPressed(cSetter.colorSetter());

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.windowBackground);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams paramsR = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        paramsR.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramsR.addRule(RelativeLayout.BELOW, R.id.toolbar);
        paramsR.setMargins(0, -(QuickReturnUtils.dp2px(this, 28)), 0, 0);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentId = new Intent(ReminderPreviewFragment.this, ReminderManager.class);
                if (id != 0) {
                    intentId.putExtra(Constants.EDIT_ID, id);
                    new AlarmReceiver().cancelAlarm(ReminderPreviewFragment.this, id);
                    new WeekDayReceiver().cancelAlarm(ReminderPreviewFragment.this, id);
                    new DelayReceiver().cancelAlarm(ReminderPreviewFragment.this, id);
                    new PositionDelayReceiver().cancelDelay(ReminderPreviewFragment.this, id);
                    startActivity(intentId);
                    new DisableAsync(ReminderPreviewFragment.this).execute();
                }
            }
        });

        initViews();
    }

    private void initViews() {
        task = (TextView) findViewById(R.id.task);

        switchWrapper = (RelativeLayout) findViewById(R.id.switchWrapper);
        switchWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        statusText = (TextView) findViewById(R.id.statusText);
        statusSwitch = (SwitchCompat) findViewById(R.id.statusSwitch);

        time = (TextView) findViewById(R.id.time);
        location = (TextView) findViewById(R.id.location);
        group = (TextView) findViewById(R.id.group);
        type = (TextView) findViewById(R.id.type);
        number = (TextView) findViewById(R.id.number);
        repeat = (TextView) findViewById(R.id.repeat);
        melody = (TextView) findViewById(R.id.melody);

        tasksContainer = (LinearLayout) findViewById(R.id.tasksContainer);
        tasksContainer.setVisibility(View.GONE);
        listColor = (TextView) findViewById(R.id.listColor);
        taskText = (TextView) findViewById(R.id.taskText);
        taskNote = (TextView) findViewById(R.id.taskNote);
        taskDate = (TextView) findViewById(R.id.taskDate);
        checkDone = (CheckBox) findViewById(R.id.checkDone);

        notesContainer = (LinearLayout) findViewById(R.id.notesContainer);
        notesContainer.setVisibility(View.GONE);
        noteText = (TextView) findViewById(R.id.noteText);
        imageView = (ImageView) findViewById(R.id.imageView);

        progress = (CircularProgress) findViewById(R.id.progress);

        background = (LinearLayout) findViewById(R.id.background);
        mapContainer = (LinearLayout) findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.GONE);

        setDrawables();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new loadAsync(this, progress, id).execute();
    }

    private void setDrawables() {
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME)) {
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_white_24dp, 0, 0, 0);
            type.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_white_24dp, 0, 0, 0);
            group.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_white_24dp, 0, 0, 0);
            location.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_white_24dp, 0, 0, 0);
            number.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_white_24dp, 0, 0, 0);
            repeat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_white_24dp, 0, 0, 0);
            melody.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audiotrack_white_24dp, 0, 0, 0);
        } else {
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_grey600_24dp, 0, 0, 0);
            type.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_grey600_24dp, 0, 0, 0);
            group.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_local_offer_grey600_24dp, 0, 0, 0);
            location.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_grey600_24dp, 0, 0, 0);
            number.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_grey600_24dp, 0, 0, 0);
            repeat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_grey600_24dp, 0, 0, 0);
            melody.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audiotrack_grey600_24dp, 0, 0, 0);
        }
    }

    private void toggle() {
        db = new DataBase(this);
        db.open();
        if (statusSwitch.isChecked()){
            db.setDone(id);
            Integer i = (int) (long) id;
            new AlarmReceiver().cancelAlarm(ReminderPreviewFragment.this, i);
            new WeekDayReceiver().cancelAlarm(ReminderPreviewFragment.this, i);
            new MonthDayReceiver().cancelAlarm(ReminderPreviewFragment.this, i);
            new DelayReceiver().cancelAlarm(ReminderPreviewFragment.this, id);
            new RepeatNotificationReceiver().cancelAlarm(ReminderPreviewFragment.this, i);
            new PositionDelayReceiver().cancelDelay(ReminderPreviewFragment.this, i);
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(i);
            new DisableAsync(ReminderPreviewFragment.this).execute();
            loadData();
        } else {
            Cursor c = db.getTask(id);
            if (c != null && c.moveToFirst()) {
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));

                if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                    db.setUnDone(id);
                    db.updateDateTime(id);
                    new WeekDayReceiver().setAlarm(ReminderPreviewFragment.this, id);
                    loadData();
                } else if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                    db.setUnDone(id);
                    db.updateDateTime(id);
                    new MonthDayReceiver().setAlarm(this, id);
                    loadData();
                } else if (type.startsWith(Constants.TYPE_LOCATION) ||
                        type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                    db.setUnDone(id);
                    db.updateDateTime(id);
                    if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0) {
                        startService(new Intent(ReminderPreviewFragment.this, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        startService(new Intent(ReminderPreviewFragment.this, CheckPosition.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        new PositionDelayReceiver().setDelay(ReminderPreviewFragment.this, id);
                    }
                    loadData();
                } else {
                    if (type.matches(Constants.TYPE_TIME)){
                        final Calendar calendar1 = Calendar.getInstance();
                        int myYear = calendar1.get(Calendar.YEAR);
                        int myMonth = calendar1.get(Calendar.MONTH);
                        int myDay = calendar1.get(Calendar.DAY_OF_MONTH);
                        int myHour = calendar1.get(Calendar.HOUR_OF_DAY);
                        int myMinute = calendar1.get(Calendar.MINUTE);
                        int mySeconds = calendar1.get(Calendar.SECOND);
                        db.updateStartTime(id, myDay, myMonth, myYear, myHour, myMinute, mySeconds);
                        db.updateDateTime(id);
                        new AlarmReceiver().setAlarm(ReminderPreviewFragment.this, id);
                        loadData();
                    } else {
                        if (new TimeCount(ReminderPreviewFragment.this)
                                .getNextDate(year, month, day, hour, minute, seconds, repTime, repCode, repCount)) {
                            db.setUnDone(id);
                            db.updateDateTime(id);
                            new AlarmReceiver().setAlarm(ReminderPreviewFragment.this, id);
                            loadData();
                        } else {
                            Toast.makeText(ReminderPreviewFragment.this, getString(R.string.edit_reminder_toast),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            if (c != null) c.close();
        }
        if (db != null) db.close();
        new Notifier(this).recreatePermanent();
        new UpdatesHelper(this).updateWidget();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();

        if (ids == R.id.action_delete) {
            makeArchive();
            finish();
            return true;
        }
        if (ids == android.R.id.home){
            finish();
        }
        if (ids == R.id.action_make_copy){
            db = new DataBase(this);
            if (!db.isOpen()) db.open();
            Cursor c = db.getTask(id);
            if (c != null && c.moveToFirst()){
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                if (!type.startsWith(Constants.TYPE_LOCATION) && !type.matches(Constants.TYPE_TIME)){
                    showDialog();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    ArrayList<Long> list;

    public void showDialog(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        int hour = 0;
        int minute = 0;
        list = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();

        do {
            if (hour == 23 && minute == 30){
                hour = -1;
            } else {
                long tmp = calendar.getTimeInMillis();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                list.add(tmp);
                String hourStr;
                if (hour < 10) hourStr = "0" + hour;
                else hourStr = String.valueOf(hour);
                String minuteStr;
                if (minute < 10) minuteStr = "0" + minute;
                else minuteStr = String.valueOf(minute);
                time.add(hourStr + ":" + minuteStr);
                calendar.setTimeInMillis(tmp + AlarmManager.INTERVAL_HALF_HOUR);
            }
        } while (hour != -1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.string_select_time));
        builder.setItems(time.toArray(new String[time.size()]), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                createCopy(list.get(item));
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createCopy(long time) {
        db = new DataBase(this);
        sPrefs = new SharedPrefs(this);
        if (!db.isOpen()) db.open();
        Cursor c = db.getTask(id);
        if (c != null && c.moveToFirst()){
            String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
            String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
            String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
            String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
            String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
            String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
            int myHour;
            int myMinute;
            int myDay = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
            int myMonth = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
            int myYear = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
            int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
            int exp = c.getInt(c.getColumnIndex(Constants.COLUMN_EXPORT_TO_CALENDAR));
            int radius = c.getInt(c.getColumnIndex(Constants.COLUMN_CUSTOM_RADIUS));
            int ledColor = c.getInt(c.getColumnIndex(Constants.COLUMN_LED_COLOR));
            int code = c.getInt(c.getColumnIndex(Constants.COLUMN_SYNC_CODE));
            double latitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
            if (!type.matches(Constants.TYPE_TIME)) {
                String uuID = new SyncHelper().generateID();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                myHour = calendar.get(Calendar.HOUR_OF_DAY);
                myMinute = calendar.get(Calendar.MINUTE);
                long idN = db.insertTask(text, type, myDay, myMonth, myYear, myHour, myMinute, 0,
                        number, repCode, 0, 0, latitude, longitude, uuID, weekdays, exp, melody, radius, ledColor,
                        code, categoryId);
                db.updateDateTime(idN);
                if (type.startsWith(Constants.TYPE_LOCATION) ||
                        type.startsWith(Constants.TYPE_LOCATION_OUT)){
                    if (myHour > 0 && myMinute > 0){
                        new PositionDelayReceiver().setDelay(this, idN);
                    } else {
                        startService(new Intent(this, GeolocationService.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
                if (type.startsWith(Constants.TYPE_APPLICATION) || type.matches(Constants.TYPE_CALL) ||
                        type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_REMINDER) ||
                        type.startsWith(Constants.TYPE_SKYPE)){
                    if (exp == 1 && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                            sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))
                        exportToCalendar(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour,
                                myMinute, 0), idN);
                    if (new GTasksHelper(this).isLinked() && code == Constants.SYNC_GTASKS_ONLY ||
                            code == Constants.SYNC_ALL){
                        exportToTasks(text, ReminderUtils.getTime(myDay, myMonth, myYear, myHour,
                                myMinute, 0), idN);
                    }
                    new AlarmReceiver().setAlarm(this, idN);
                }
                if (type.startsWith(Constants.TYPE_WEEKDAY)){
                    if (exp == 1 && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                            sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))
                        exportToCalendar(text, ReminderUtils.getWeekTime(myHour, myMinute, weekdays), idN);
                    if (new GTasksHelper(this).isLinked() && code == Constants.SYNC_GTASKS_ONLY ||
                            code == Constants.SYNC_ALL){
                        exportToTasks(text, ReminderUtils.getWeekTime(myHour, myMinute, weekdays), idN);
                    }
                    new WeekDayReceiver().setAlarm(this, idN);
                }
                if (type.startsWith(Constants.TYPE_MONTHDAY)){
                    if (exp == 1 && sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR) ||
                            sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK))
                        exportToCalendar(text, ReminderUtils.getMonthTime(myHour, myMinute, myDay), idN);
                    if (new GTasksHelper(this).isLinked() && code == Constants.SYNC_GTASKS_ONLY ||
                            code == Constants.SYNC_ALL){
                        exportToTasks(text, ReminderUtils.getMonthTime(myHour, myMinute, myDay), idN);
                    }
                    new MonthDayReceiver().setAlarm(this, idN);
                }
            }
        }
        if (db != null) db.close();
        UpdatesHelper updatesHelper = new UpdatesHelper(this);
        updatesHelper.updateWidget();

        Toast.makeText(this, getString(R.string.string_reminder_created), Toast.LENGTH_SHORT).show();
    }

    private void makeArchive() {
        db = new DataBase(this);
        if (!db.isOpen()) db.open();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        Cursor c = db.getTask(id);
        String uuID = null;
        if (c != null && c.moveToFirst()){
            uuID = c.getString(c.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        db.deleteTask(id);
        new DeleteReminder(ReminderPreviewFragment.this, uuID).execute();
        if (db != null) db.close();
        UpdatesHelper updatesHelper = new UpdatesHelper(this);
        updatesHelper.updateWidget();

        Toast.makeText(this, getString(R.string.archived_result_message), Toast.LENGTH_SHORT).show();
        new DisableAsync(this).execute();
    }

    private void exportToCalendar(String summary, long startTime, long id){
        sPrefs = new SharedPrefs(this);
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_CALENDAR)){
            new CalendarManager(this).addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXPORT_TO_STOCK)){
            new CalendarManager(this).addEventToStock(summary, startTime);
        }
    }

    private void exportToTasks(String summary, long startTime, long mId){
        long localId = new TasksData(this).addTask(summary, null, 0, false, startTime,
                null, null, getString(R.string.string_task_from_just_reminder),
                null, null, null, 0, mId, null, Constants.TASKS_NEED_ACTION, false);
        new TaskAsync(this, summary, null, null,
                TasksConstants.INSERT_TASK, startTime, getString(R.string.string_task_from_just_reminder), localId).execute();
    }

    public class loadAsync extends AsyncTask<Void, Void, String[]>{

        Context mContext;
        CircularProgress mProgress;
        long mId;
        private double lat = 0.0, lon = 0.0;

        public loadAsync(Context context, CircularProgress circularProgress, long id){
            this.mContext = context;
            this.mProgress = circularProgress;
            this.mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            DataBase dataBase = new DataBase(mContext);
            dataBase.open();
            Contacts mContacts = new Contacts(mContext);
            Interval mInterval = new Interval(mContext);
            TimeCount mCount = new TimeCount(mContext);
            Cursor c = dataBase.getTask(mId);
            String doneStr = null, title = null, typeStr = null, timeStr = null, groupStr = null,
                    locationLat = null, locationLon = null, numberStr = null, repeatStr = null,
                    melodyStr = null;
            if (c != null && c.moveToFirst()){
                title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
                String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
                String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
                String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
                String melody = c.getString(c.getColumnIndex(Constants.COLUMN_CUSTOM_MELODY));
                int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
                int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
                int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
                int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
                int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
                int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
                int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
                long repTime = c.getLong(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
                int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
                lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
                lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
                int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
                int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));

                Uri soundUri;
                if (melody != null && !melody.matches("")){
                    File sound = new File(melody);
                    soundUri = Uri.fromFile(sound);
                } else {
                    if (new SharedPrefs(mContext).loadBoolean(Constants.CUSTOM_SOUND)) {
                        String path = new SharedPrefs(mContext).loadPrefs(Constants.CUSTOM_SOUND_FILE);
                        if (path != null) {
                            File sound = new File(path);
                            soundUri = Uri.fromFile(sound);
                        } else {
                            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        }
                    } else {
                        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    }
                }
                File file = new File(soundUri.getPath());
                melodyStr = file.getName();

                Cursor cf = dataBase.getCategory(categoryId);
                if (cf != null && cf.moveToFirst()) {
                    groupStr = cf.getString(cf.getColumnIndex(Constants.COLUMN_TEXT));
                }
                if (cf != null) cf.close();

                if (isDone == 1) doneStr = "done";
                else doneStr = null;

                typeStr = ReminderUtils.getTypeString(mContext, type);
                boolean is24 = new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT);

                if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
                    if (type.matches(Constants.TYPE_CALL) ||
                            type.matches(Constants.TYPE_LOCATION_CALL) ||
                            type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                        numberStr = number;
                        String name = mContacts.getContactNameFromNumber(number, mContext);
                        if (name != null) numberStr = name + "\n" + number;
                    } else if (type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                            type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                        numberStr = number;
                        String name = mContacts.getContactNameFromNumber(number, mContext);
                        if (name != null) numberStr = name + "\n" + number;
                    } else if (type.startsWith(Constants.TYPE_SKYPE)){
                        numberStr = number;
                    } else if (type.matches(Constants.TYPE_APPLICATION)){
                        PackageManager packageManager = mContext.getPackageManager();
                        ApplicationInfo applicationInfo = null;
                        try {
                            applicationInfo = packageManager.getApplicationInfo(number, 0);
                        } catch (final PackageManager.NameNotFoundException ignored) {}
                        final String name = (String)((applicationInfo != null) ?
                                packageManager.getApplicationLabel(applicationInfo) : "???");
                        numberStr = name + "\n" + number;
                    } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                        numberStr = number;
                    }

                    long time = TimeCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                            repCode, repCount, delay);

                    if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                            type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                            type.startsWith(Constants.TYPE_APPLICATION)) {
                        repeatStr = mInterval.getInterval(repCode);
                    } else if (type.matches(Constants.TYPE_TIME)) {
                        repeatStr = mInterval.getTimeInterval(repCode);
                    } else {
                        repeatStr = mContext.getString(R.string.interval_zero);
                    }

                    String[] dT = mCount.
                            getNextDateTime(time);
                    if (lat != 0.0 || lon != 0.0) {
                        locationLat = String.format("%.5f", lat);
                        locationLon = String.format("%.5f", lon);
                    } else {
                        timeStr = dT[0] + "\n" + dT[1];
                    }
                } else if (type.startsWith(Constants.TYPE_MONTHDAY)){
                    if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                        numberStr = number;
                        String name = mContacts.getContactNameFromNumber(number, mContext);
                        if (name != null) numberStr = name + "\n" + number;
                    } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                        numberStr = number;
                        String name = mContacts.getContactNameFromNumber(number, mContext);
                        if (name != null) numberStr = name + "\n" + number;
                    }

                    long time = TimeCount.getNextMonthDayTime(hour, minute, day, delay);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.setTimeInMillis(time);
                    String date = Utils.dateFormat.format(calendar.getTime());

                    repeatStr = getString(R.string.string_by_day_of_month);
                    timeStr = date + "\n" + Utils.getTime(calendar.getTime(), is24);
                } else {
                    if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                        numberStr = number;
                        String name = mContacts.getContactNameFromNumber(number, mContext);
                        if (name != null) numberStr = name + "\n" + number;
                    } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                        numberStr = number;
                        String name = mContacts.getContactNameFromNumber(number, mContext);
                        if (name != null) numberStr = name + "\n" + number;
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);

                    if (weekdays.length() == 7) {
                        repeatStr = ReminderUtils.getRepeatString(mContext, weekdays);
                    }
                    timeStr = Utils.getTime(calendar.getTime(), is24);
                }
            }
            return new String[]{doneStr, title, typeStr, groupStr, timeStr,
                    numberStr, repeatStr, locationLat + "\n" + locationLon, melodyStr};
        }

        @Override
        protected void onPostExecute(String[] aVoid) {
            super.onPostExecute(aVoid);
            mProgress.setVisibility(View.GONE);
            if (aVoid != null && aVoid.length > 0){
                String done = aVoid[0];
                if (done != null && done.matches("done")) {
                    statusSwitch.setChecked(false);
                    statusText.setText(getString(R.string.simple_disabled));
                } else {
                    statusSwitch.setChecked(true);
                    statusText.setText(getString(R.string.simple_enabled));
                }
                task.setText(aVoid[1]);
                type.setText(aVoid[2]);
                group.setText(aVoid[3]);
                String timeStr = aVoid[4];
                if (timeStr != null) {
                    time.setText(timeStr);
                    location.setVisibility(View.GONE);
                    repeat.setText(aVoid[6]);
                } else {
                    location.setText(aVoid[7]);
                    time.setVisibility(View.GONE);
                    repeat.setVisibility(View.GONE);
                    mapContainer.setVisibility(View.VISIBLE);
                    MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                    final GoogleMap mMap = googleMap.getMap();
                    SharedPrefs prefs = new SharedPrefs(mContext);
                    String type = prefs.loadPrefs(Constants.APP_UI_PREFERENCES_MAP_TYPE);
                    if (type.matches(Constants.MAP_TYPE_NORMAL)){
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    } else {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }

                    mMap.setMyLocationEnabled(true);
                    if (mMap.getMyLocation() != null) {
                        double lat = mMap.getMyLocation().getLatitude();
                        double lon = mMap.getMyLocation().getLongitude();
                        LatLng pos = new LatLng(lat, lon);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                    }

                    if (lon != 0 && lat != 0) {
                        LatLng pos = new LatLng(lat, lon);
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(aVoid[1])
                                .icon(BitmapDescriptorFactory.fromResource(new ColorSetter(mContext).getMarkerStyle())));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
                    }
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> listAddresses = geocoder.getFromLocation(lat, lon, 1);
                        if(null != listAddresses&&listAddresses.size() > 0){
                            String _Location = listAddresses.get(0).getAddressLine(0);
                            if (_Location != null && !_Location.matches("")) {
                                location.setText(_Location + "\n" + "("
                                        + String.format("%.5f", lat) + ", " +
                                        String.format("%.5f", lon) + ")");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String numberStr = aVoid[5];
                if (numberStr != null && !numberStr.matches("")) number.setText(numberStr);
                else number.setVisibility(View.GONE);

                String melodyStr = aVoid[8];
                if (melodyStr != null && !melodyStr.matches("")) melody.setText(melodyStr);
                else melody.setVisibility(View.GONE);
            }

            new loadOtherData(mContext, mProgress, mId).execute();
        }
    }

    public class loadOtherData extends AsyncTask<Void, Void, ItemData>{

        Context mContext;
        CircularProgress mProgress;
        long mId;
        SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());

        public loadOtherData(Context context, CircularProgress circularProgress, long id){
            this.mContext = context;
            this.mProgress = circularProgress;
            this.mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ItemData doInBackground(Void... params) {
            NotesBase notesBase = new NotesBase(mContext);
            notesBase.open();
            ItemData itemData = new ItemData();
            Cursor c = notesBase.getNoteByReminder(mId);
            if (c != null && c.moveToFirst()){
                String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
                long noteId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                itemData.setNoteText(note);
                itemData.setImage(image);
                itemData.setNoteId(noteId);
            }
            if (c != null) c.close();
            notesBase.close();

            TasksData data = new TasksData(mContext);
            data.open();
            c = data.getTaskByReminder(mId);
            if (c != null && c.moveToFirst()){
                String task = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TITLE));
                String taskNote = c.getString(c.getColumnIndex(TasksConstants.COLUMN_NOTES));
                String status = c.getString(c.getColumnIndex(TasksConstants.COLUMN_STATUS));
                String taskListId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_LIST_ID));
                String taskId = c.getString(c.getColumnIndex(TasksConstants.COLUMN_TASK_ID));
                long due = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_DUE));
                long id = c.getLong(c.getColumnIndex(TasksConstants.COLUMN_ID));
                itemData.setTaskTitle(task);
                itemData.setTaskNote(taskNote);
                itemData.setTaskStatus(status);
                itemData.setTaskDate(due);
                itemData.setTaskId(id);
                itemData.setTaskListId(taskListId);
                itemData.setTaskIdentifier(taskId);
            }
            return itemData;
        }

        @Override
        protected void onPostExecute(final ItemData itemData) {
            super.onPostExecute(itemData);
            mProgress.setVisibility(View.GONE);
            if (itemData != null){
                if (itemData.getTaskId() > 0){
                    tasksContainer.setVisibility(View.VISIBLE);
                    taskText.setText(itemData.getTaskTitle());
                    String mNote = itemData.getTaskNote();
                    Cursor c = new TasksData(mContext).getTasksList(itemData.getTaskListId());
                    if (c != null && c.moveToFirst()){
                        listColor.setBackgroundColor(
                                mContext.getResources().getColor(
                                        new ColorSetter(mContext)
                                                .getNoteColor(c.getInt(
                                                        c.getColumnIndex(TasksConstants.COLUMN_COLOR)))));
                    } else {
                        listColor.setBackgroundColor(
                                mContext.getResources().getColor(
                                        new ColorSetter(mContext).getNoteColor(8)));
                    }
                    if (mNote != null && !mNote.matches("")) taskNote.setText(mNote);
                    else taskNote.setVisibility(View.GONE);
                    long date = itemData.getTaskDate();
                    Calendar calendar = Calendar.getInstance();
                    if (date != 0) {
                        calendar.setTimeInMillis(date);
                        String update = full24Format.format(calendar.getTime());
                        taskDate.setText(update);
                    } else taskDate.setVisibility(View.INVISIBLE);
                    if (itemData.getTaskStatus().matches(Constants.TASKS_COMPLETE)){
                        checkDone.setChecked(true);
                    } else checkDone.setChecked(false);
                    checkDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            final TasksData data = new TasksData(mContext);
                            data.open();
                            if (isChecked){
                                data.setTaskDone(itemData.getTaskId());
                            } else {
                                data.setTaskUnDone(itemData.getTaskId());
                            }

                            new SwitchTaskAsync(mContext, itemData.getTaskListId(),
                                    itemData.getTaskIdentifier(), isChecked, null).execute();
                            loadData();
                        }
                    });
                    background.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mContext.startActivity(new Intent(mContext, TaskManager.class)
                                    .putExtra(Constants.ITEM_ID_INTENT, itemData.getTaskId())
                                    .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
                        }
                    });
                }

                if (itemData.getNoteId() > 0){
                    notesContainer.setVisibility(View.VISIBLE);
                    String note = itemData.getNoteText();
                    if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_NOTE_ENCRYPT)){
                        note = new SyncHelper().decrypt(note);
                    }
                    noteText.setText(note);
                    noteText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                Intent intent = new Intent(mContext, NotePreviewFragment.class);
                                intent.putExtra(Constants.EDIT_ID, itemData.getNoteId());
                                String transitionName = "image";
                                ActivityOptionsCompat options =
                                        ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, imageView,
                                                transitionName);
                                mContext.startActivity(intent, options.toBundle());
                            } else {
                                mContext.startActivity(
                                        new Intent(mContext, NotePreviewFragment.class)
                                                .putExtra(Constants.EDIT_ID, itemData.getNoteId()));
                            }
                        }
                    });
                    byte[] image = itemData.getImage();
                    if (image != null){
                        final Bitmap imgB = BitmapFactory.decodeByteArray(image, 0,
                                image.length);
                        imageView.setImageBitmap(imgB);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Intent intent = new Intent(mContext, NotePreviewFragment.class);
                                    intent.putExtra(Constants.EDIT_ID, itemData.getNoteId());
                                    String transitionName = "image";
                                    ActivityOptionsCompat options =
                                            ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, imageView,
                                                    transitionName);
                                    mContext.startActivity(intent, options.toBundle());
                                } else {
                                    mContext.startActivity(
                                            new Intent(mContext, NotePreviewFragment.class)
                                                    .putExtra(Constants.EDIT_ID, itemData.getNoteId()));
                                }
                            }
                        });
                    } else imageView.setVisibility(View.GONE);
                }
            }
        }
    }
}
